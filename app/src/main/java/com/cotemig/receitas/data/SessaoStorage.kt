package com.cotemig.receitas.data

import android.content.Context

class SessaoStorage(context: Context) {

    private val preferencias = context.applicationContext
        .getSharedPreferences("sessao", Context.MODE_PRIVATE)

    fun salvar(usuarioId: Long) {
        preferencias.edit().putLong(CHAVE_USUARIO, usuarioId).apply()
    }

    fun obter(): Long? =
        if (preferencias.contains(CHAVE_USUARIO)) preferencias.getLong(CHAVE_USUARIO, -1L) else null

    fun limpar() {
        preferencias.edit().remove(CHAVE_USUARIO).apply()
    }

    private companion object {
        const val CHAVE_USUARIO = "usuario_id"
    }
}
