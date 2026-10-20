package com.cotemig.receitas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cotemig.receitas.data.AppContainer
import com.cotemig.receitas.data.ErroCarregamento
import com.cotemig.receitas.data.Receita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetalheEstado {
    data object Carregando : DetalheEstado
    data class Erro(val mensagem: String) : DetalheEstado
    data class Sucesso(val receita: Receita) : DetalheEstado
}

class DetalheViewModel(application: Application) : AndroidViewModel(application) {

    private val container = AppContainer.obter(application)

    private val _estado = MutableStateFlow<DetalheEstado>(DetalheEstado.Carregando)
    val estado: StateFlow<DetalheEstado> = _estado.asStateFlow()

    val favoritos: StateFlow<Set<String>> = container.favoritosDoUsuario()
        .map { favoritos -> favoritos.map { it.idReceita }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private var idCarregado: String? = null

    fun carregar(idReceita: String, forcar: Boolean = false) {
        if (!forcar && idCarregado == idReceita && _estado.value is DetalheEstado.Sucesso) return
        idCarregado = idReceita
        viewModelScope.launch {
            _estado.value = DetalheEstado.Carregando
            _estado.value = try {
                val receita = container.receitas.detalhe(idReceita)
                if (receita == null) DetalheEstado.Erro("Receita não encontrada.")
                else DetalheEstado.Sucesso(receita)
            } catch (erro: ErroCarregamento) {
                DetalheEstado.Erro(erro.message ?: "Não foi possível carregar a receita.")
            }
        }
    }

    fun alternarFavorito(receita: Receita) {
        val usuarioId = container.usuarios.usuarioId.value ?: return
        val jaFavorita = receita.id in favoritos.value
        viewModelScope.launch {
            container.favoritos.alternar(usuarioId, receita, jaFavorita)
        }
    }
}
