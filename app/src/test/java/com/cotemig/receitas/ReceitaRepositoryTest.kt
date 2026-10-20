package com.cotemig.receitas

import com.cotemig.receitas.data.ErroCarregamento
import com.cotemig.receitas.data.ReceitaRepository
import com.cotemig.receitas.data.RespostaCategorias
import com.cotemig.receitas.data.RespostaReceitas
import com.cotemig.receitas.network.ReceitasApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ReceitaRepositoryTest {

    private fun refeicao(id: String, nome: String, categoria: String? = null) = JsonObject(
        buildMap {
            put("idMeal", JsonPrimitive(id))
            put("strMeal", JsonPrimitive(nome))
            put("strMealThumb", JsonPrimitive("https://exemplo.com/$id.jpg"))
            if (categoria != null) put("strCategory", JsonPrimitive(categoria))
        }
    )

    private class ApiFalsa(
        var buscaPorNome: (String) -> RespostaReceitas = { RespostaReceitas() },
        var filtroPorCategoria: (String) -> RespostaReceitas = { RespostaReceitas() }
    ) : ReceitasApi {
        val chamadas = mutableListOf<String>()

        override suspend fun buscar(nome: String): RespostaReceitas {
            chamadas += "buscar:$nome"
            return buscaPorNome(nome)
        }

        override suspend fun porCategoria(categoria: String): RespostaReceitas {
            chamadas += "categoria:$categoria"
            return filtroPorCategoria(categoria)
        }

        override suspend fun detalhe(id: String): RespostaReceitas {
            chamadas += "detalhe:$id"
            return buscaPorNome(id)
        }

        override suspend fun categorias(): RespostaCategorias = RespostaCategorias()
    }

    @Test
    fun semBuscaENemCategoriaListaTodas() = runTest {
        val api = ApiFalsa(buscaPorNome = { RespostaReceitas(listOf(refeicao("1", "Flan"))) })

        val receitas = ReceitaRepository(api).listar("", null)

        assertEquals(listOf("buscar:"), api.chamadas)
        assertEquals(listOf("Flan"), receitas.map { it.nome })
    }

    @Test
    fun comCategoriaUsaOFiltro() = runTest {
        val api = ApiFalsa(filtroPorCategoria = { RespostaReceitas(listOf(refeicao("2", "Sushi"))) })

        val receitas = ReceitaRepository(api).listar("", "Seafood")

        assertEquals(listOf("categoria:Seafood"), api.chamadas)
        assertEquals(listOf("Sushi"), receitas.map { it.nome })
    }

    @Test
    fun buscaPorNomeComCategoriaFiltraOResultado() = runTest {
        val api = ApiFalsa(
            buscaPorNome = {
                RespostaReceitas(
                    listOf(
                        refeicao("1", "Chicken Pie", "Chicken"),
                        refeicao("2", "Chicken Salad", "Side")
                    )
                )
            }
        )

        val receitas = ReceitaRepository(api).listar("chicken", "Side")

        assertEquals(listOf("Chicken Salad"), receitas.map { it.nome })
    }

    @Test
    fun respostaNulaVirouListaVazia() = runTest {
        val api = ApiFalsa(buscaPorNome = { RespostaReceitas(null) })

        assertTrue(ReceitaRepository(api).listar("zzz", null).isEmpty())
    }

    @Test
    fun falhaDeRedeViraMensagemDeConexao() = runTest {
        val api = ApiFalsa(buscaPorNome = { throw IOException("sem rede") })

        try {
            ReceitaRepository(api).listar("", null)
            fail("era esperado ErroCarregamento")
        } catch (erro: ErroCarregamento) {
            assertTrue(erro.message!!.contains("conexão"))
        }
    }

    @Test
    fun erroHttpViraMensagemComCodigo() = runTest {
        val api = ApiFalsa(
            buscaPorNome = {
                throw HttpException(Response.error<RespostaReceitas>(503, "".toResponseBody()))
            }
        )

        try {
            ReceitaRepository(api).listar("", null)
            fail("era esperado ErroCarregamento")
        } catch (erro: ErroCarregamento) {
            assertTrue(erro.message!!.contains("503"))
        }
    }

    @Test
    fun detalheDeIdInexistenteVoltaNulo() = runTest {
        val api = ApiFalsa(buscaPorNome = { RespostaReceitas(null) })

        assertEquals(null, ReceitaRepository(api).detalhe("0"))
    }
}
