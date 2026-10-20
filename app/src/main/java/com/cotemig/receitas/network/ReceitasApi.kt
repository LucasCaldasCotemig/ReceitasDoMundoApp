package com.cotemig.receitas.network

import com.cotemig.receitas.data.RespostaCategorias
import com.cotemig.receitas.data.RespostaReceitas
import retrofit2.http.GET
import retrofit2.http.Query

interface ReceitasApi {

    @GET("search.php")
    suspend fun buscar(@Query("s") nome: String): RespostaReceitas

    @GET("filter.php")
    suspend fun porCategoria(@Query("c") categoria: String): RespostaReceitas

    @GET("lookup.php")
    suspend fun detalhe(@Query("i") id: String): RespostaReceitas

    @GET("categories.php")
    suspend fun categorias(): RespostaCategorias
}
