package com.cotemig.receitas.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class RespostaReceitas(
    val meals: List<JsonObject>? = null
)

@Serializable
data class CategoriaDto(
    @SerialName("idCategory") val id: String = "",
    @SerialName("strCategory") val nome: String
)

@Serializable
data class RespostaCategorias(
    val categories: List<CategoriaDto> = emptyList()
)

data class Ingrediente(
    val nome: String,
    val medida: String
)

data class Receita(
    val id: String,
    val nome: String,
    val imagem: String,
    val categoria: String = "",
    val area: String = "",
    val instrucoes: String = "",
    val youtube: String = "",
    val ingredientes: List<Ingrediente> = emptyList()
)

fun JsonObject.texto(chave: String): String {
    val valor = this[chave]
    return if (valor is JsonPrimitive && valor.isString) valor.content.trim() else ""
}

fun JsonObject.paraReceita(): Receita {
    val ingredientes = (1..20).mapNotNull { posicao ->
        val nome = texto("strIngredient$posicao")
        if (nome.isEmpty()) null else Ingrediente(nome, texto("strMeasure$posicao"))
    }
    return Receita(
        id = texto("idMeal"),
        nome = texto("strMeal"),
        imagem = texto("strMealThumb"),
        categoria = texto("strCategory"),
        area = texto("strArea"),
        instrucoes = texto("strInstructions").replace("\r\n", "\n"),
        youtube = texto("strYoutube"),
        ingredientes = ingredientes
    )
}

fun RespostaReceitas.paraReceitas(): List<Receita> =
    (meals ?: emptyList()).map { it.paraReceita() }.filter { it.id.isNotEmpty() }
