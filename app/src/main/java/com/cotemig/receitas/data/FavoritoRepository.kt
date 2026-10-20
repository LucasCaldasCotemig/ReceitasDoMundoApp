package com.cotemig.receitas.data

import kotlinx.coroutines.flow.Flow

class FavoritoRepository(private val dao: FavoritoDao) {

    fun observar(usuarioId: Long): Flow<List<Favorito>> = dao.observar(usuarioId)

    suspend fun adicionar(usuarioId: Long, receita: Receita) {
        dao.inserir(
            Favorito(
                usuarioId = usuarioId,
                idReceita = receita.id,
                nome = receita.nome,
                imagem = receita.imagem,
                categoria = receita.categoria,
                area = receita.area
            )
        )
    }

    suspend fun remover(usuarioId: Long, idReceita: String) {
        dao.remover(usuarioId, idReceita)
    }

    suspend fun alternar(usuarioId: Long, receita: Receita, jaFavorita: Boolean) {
        if (jaFavorita) remover(usuarioId, receita.id) else adicionar(usuarioId, receita)
    }
}
