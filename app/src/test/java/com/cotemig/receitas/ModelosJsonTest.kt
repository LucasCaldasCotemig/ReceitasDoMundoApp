package com.cotemig.receitas

import com.cotemig.receitas.data.RespostaCategorias
import com.cotemig.receitas.data.RespostaReceitas
import com.cotemig.receitas.data.paraReceitas
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelosJsonTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun arquivo(nome: String): String =
        javaClass.classLoader!!.getResource(nome)!!.readText()

    @Test
    fun detalheDaReceitaVemComIngredientesEMedidas() {
        val receitas = json.decodeFromString<RespostaReceitas>(arquivo("busca_arrabiata.json")).paraReceitas()

        assertEquals(1, receitas.size)
        val receita = receitas.first()
        assertEquals("52771", receita.id)
        assertEquals("Spicy Arrabiata Penne", receita.nome)
        assertEquals("Vegetarian", receita.categoria)
        assertEquals("Italian", receita.area)
        assertEquals(8, receita.ingredientes.size)
        assertEquals("penne rigate", receita.ingredientes.first().nome)
        assertEquals("1 pound", receita.ingredientes.first().medida)
        assertTrue(receita.instrucoes.startsWith("Bring a large pot of water"))
        assertTrue("\r" !in receita.instrucoes)
    }

    @Test
    fun buscaSemResultadoVoltaListaVazia() {
        val receitas = json.decodeFromString<RespostaReceitas>(arquivo("busca_sem_resultado.json")).paraReceitas()

        assertTrue(receitas.isEmpty())
    }

    @Test
    fun filtroPorCategoriaTrazIdNomeEImagem() {
        val receitas = json.decodeFromString<RespostaReceitas>(arquivo("filtro_categoria.json")).paraReceitas()

        assertEquals(3, receitas.size)
        receitas.forEach {
            assertTrue(it.id.isNotEmpty())
            assertTrue(it.nome.isNotEmpty())
            assertTrue(it.imagem.startsWith("https://"))
            assertTrue(it.ingredientes.isEmpty())
        }
    }

    @Test
    fun categoriasSaoLidasComONome() {
        val resposta = json.decodeFromString<RespostaCategorias>(arquivo("categorias.json"))

        assertEquals(listOf("Beef", "Chicken", "Dessert"), resposta.categories.map { it.nome })
    }
}
