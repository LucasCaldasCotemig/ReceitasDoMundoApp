package com.cotemig.receitas.viewmodel

import com.cotemig.receitas.data.AppContainer
import com.cotemig.receitas.data.Favorito
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
fun AppContainer.favoritosDoUsuario(): Flow<List<Favorito>> =
    usuarios.usuarioId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else favoritos.observar(id)
    }
