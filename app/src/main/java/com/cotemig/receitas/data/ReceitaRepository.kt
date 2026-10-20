package com.cotemig.receitas.data

import com.cotemig.receitas.network.ReceitasApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class ErroCarregamento(mensagem: String) : Exception(mensagem)

class ReceitaRepository(
    private val api: ReceitasApi,
    private val tradutor: Tradutor = SemTraducao
) {

    suspend fun listar(busca: String, categoria: String?): List<Receita> = executar {
        val termo = busca.trim()
        val receitas = when {
            termo.isNotEmpty() -> {
                val encontradas = api.buscar(termo).paraReceitas()
                if (categoria == null) encontradas
                else encontradas.filter { it.categoria.equals(categoria, ignoreCase = true) }
            }
            categoria != null -> api.porCategoria(categoria).paraReceitas()
            else -> api.buscar("").paraReceitas()
        }
        traduzirLista(receitas)
    }

    suspend fun detalhe(id: String): Receita? = executar {
        api.detalhe(id).paraReceitas().firstOrNull()?.let { traduzirDetalhe(it) }
    }

    fun rotuloCategoria(categoria: String): String = Traducoes.categoria(categoria)

    private suspend fun traduzirLista(receitas: List<Receita>): List<Receita> {
        val nomes = tradutor.traduzir(receitas.map { it.nome })
        return receitas.mapIndexed { indice, receita ->
            receita.copy(
                nome = nomes.getOrElse(indice) { receita.nome },
                categoria = Traducoes.categoria(receita.categoria),
                area = Traducoes.area(receita.area)
            )
        }
    }

    private suspend fun traduzirDetalhe(receita: Receita): Receita {
        val textos = buildList {
            add(receita.nome)
            add(receita.instrucoes)
            receita.ingredientes.forEach {
                add(it.nome)
                add(it.medida)
            }
        }
        val traduzidos = tradutor.traduzir(textos)
        if (traduzidos.size != textos.size) return receita
        val ingredientes = receita.ingredientes.mapIndexed { indice, ingrediente ->
            Ingrediente(
                nome = traduzidos[2 + indice * 2],
                medida = traduzidos[3 + indice * 2]
            )
        }
        return receita.copy(
            nome = traduzidos[0],
            instrucoes = traduzidos[1],
            categoria = Traducoes.categoria(receita.categoria),
            area = Traducoes.area(receita.area),
            ingredientes = ingredientes
        )
    }

    suspend fun categorias(): List<String> = executar {
        api.categorias().categories.map { it.nome }
    }

    private suspend fun <T> executar(bloco: suspend () -> T): T = withContext(Dispatchers.IO) {
        try {
            bloco()
        } catch (erro: CancellationException) {
            throw erro
        } catch (erro: IOException) {
            throw ErroCarregamento("Sem conexão com a internet. Verifique a rede e tente novamente.")
        } catch (erro: HttpException) {
            throw ErroCarregamento("O servidor respondeu com erro (código ${erro.code()}).")
        } catch (erro: SerializationException) {
            throw ErroCarregamento("A resposta do servidor veio em um formato inesperado.")
        }
    }
}
