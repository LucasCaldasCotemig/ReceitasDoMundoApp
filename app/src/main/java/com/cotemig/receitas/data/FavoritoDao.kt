package com.cotemig.receitas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritoDao {

    @Query("SELECT * FROM favoritos WHERE usuarioId = :usuarioId ORDER BY nome")
    fun observar(usuarioId: Long): Flow<List<Favorito>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(favorito: Favorito)

    @Query("DELETE FROM favoritos WHERE usuarioId = :usuarioId AND idReceita = :idReceita")
    suspend fun remover(usuarioId: Long, idReceita: String)
}
