package com.cotemig.receitas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cotemig.receitas.data.AppContainer
import com.cotemig.receitas.data.Favorito
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritosViewModel(application: Application) : AndroidViewModel(application) {

    private val container = AppContainer.obter(application)

    val favoritos: StateFlow<List<Favorito>> = container.favoritosDoUsuario()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remover(idReceita: String) {
        val usuarioId = container.usuarios.usuarioId.value ?: return
        viewModelScope.launch {
            container.favoritos.remover(usuarioId, idReceita)
        }
    }
}
