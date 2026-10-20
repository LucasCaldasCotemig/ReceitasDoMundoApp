package com.cotemig.receitas.data

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

interface Tradutor {
    suspend fun traduzir(textos: List<String>): List<String>
}

object SemTraducao : Tradutor {
    override suspend fun traduzir(textos: List<String>): List<String> = textos
}

class TradutorMlKit : Tradutor {

    private val cliente: Translator by lazy {
        Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.PORTUGUESE)
                .build()
        )
    }

    private val cache = ConcurrentHashMap<String, String>()
    @Volatile
    private var modeloPronto = false

    override suspend fun traduzir(textos: List<String>): List<String> {
        if (textos.all { it.isBlank() }) return textos
        if (!garantirModelo()) return textos
        return textos.map { traduzirTrecho(it) }
    }

    private suspend fun traduzirTrecho(texto: String): String {
        if (texto.isBlank()) return texto
        cache[texto]?.let { return it }
        val linhas = texto.split("\n")
        val resultado = linhas.map { linha ->
            if (linha.isBlank()) linha else traduzirLinha(linha)
        }.joinToString("\n")
        cache[texto] = resultado
        return resultado
    }

    private suspend fun traduzirLinha(linha: String): String {
        val original = linha.trim()
        cache[original]?.let { return it }
        val traduzida = suspendCancellableCoroutine { continuacao ->
            cliente.translate(original)
                .addOnSuccessListener { continuacao.resume(it) }
                .addOnFailureListener { continuacao.resume(original) }
        }
        cache[original] = traduzida
        return traduzida
    }

    private suspend fun garantirModelo(): Boolean {
        if (modeloPronto) return true
        val ok = suspendCancellableCoroutine { continuacao ->
            cliente.downloadModelIfNeeded(DownloadConditions.Builder().build())
                .addOnSuccessListener { continuacao.resume(true) }
                .addOnFailureListener { continuacao.resume(false) }
        }
        modeloPronto = ok
        return ok
    }
}
