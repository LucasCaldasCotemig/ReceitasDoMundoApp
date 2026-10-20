package com.cotemig.receitas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cotemig.receitas.data.AppContainer
import com.cotemig.receitas.data.ErroCarregamento
import com.cotemig.receitas.data.Receita
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ListaEstado {
    data object Carregando : ListaEstado
    data class Erro(val mensagem: String) : ListaEstado
    data class Sucesso(val receitas: List<Receita>) : ListaEstado
}

@OptIn(FlowPreview::class)
class ReceitasViewModel(application: Application) : AndroidViewModel(application) {

    private val container = AppContainer.obter(application)

    private val _busca = MutableStateFlow("")
    private val _categoria = MutableStateFlow<String?>(null)
    private val _categorias = MutableStateFlow<List<String>>(emptyList())
    private val _lista = MutableStateFlow<ListaEstado>(ListaEstado.Carregando)
    private val tentativa = MutableStateFlow(0)

    val categoria: StateFlow<String?> = _categoria.asStateFlow()
    val categorias: StateFlow<List<String>> = _categorias.asStateFlow()
    val lista: StateFlow<ListaEstado> = _lista.asStateFlow()

    val favoritos: StateFlow<Set<String>> = container.favoritosDoUsuario()
        .map { favoritos -> favoritos.map { it.idReceita }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    init {
        carregarCategorias()
        viewModelScope.launch {
            combine(
                _busca.debounce { texto -> if (texto.isEmpty()) 0L else 400L },
                _categoria,
                tentativa
            ) { busca, categoria, _ -> busca to categoria }
                .collectLatest { (busca, categoria) ->
                    _lista.value = ListaEstado.Carregando
                    _lista.value = try {
                        ListaEstado.Sucesso(container.receitas.listar(busca, categoria))
                    } catch (erro: ErroCarregamento) {
                        ListaEstado.Erro(erro.message ?: "Não foi possível carregar as receitas.")
                    }
                }
        }
    }

    fun rotuloCategoria(categoria: String): String = container.receitas.rotuloCategoria(categoria)

    fun buscar(texto: String) {
        _busca.value = texto
    }

    fun selecionarCategoria(categoria: String?) {
        _categoria.value = categoria
    }

    fun tentarNovamente() {
        if (_categorias.value.isEmpty()) carregarCategorias()
        tentativa.update { it + 1 }
    }

    fun alternarFavorito(receita: Receita) {
        val usuarioId = container.usuarios.usuarioId.value ?: return
        val jaFavorita = receita.id in favoritos.value
        viewModelScope.launch {
            container.favoritos.alternar(usuarioId, receita, jaFavorita)
        }
    }

    private fun carregarCategorias() {
        viewModelScope.launch {
            _categorias.value = try {
                container.receitas.categorias()
            } catch (erro: ErroCarregamento) {
                emptyList()
            }
        }
    }
}
