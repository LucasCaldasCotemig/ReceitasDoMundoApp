package com.cotemig.receitas.data

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ResultadoAuth {
    data class Sucesso(val usuario: Usuario) : ResultadoAuth
    data class Erro(val mensagem: String) : ResultadoAuth
}

class UsuarioRepository(
    private val dao: UsuarioDao,
    private val sessao: SessaoStorage
) {

    private val _usuarioId = MutableStateFlow(sessao.obter())
    val usuarioId: StateFlow<Long?> = _usuarioId.asStateFlow()

    suspend fun restaurarSessao(): Usuario? {
        val id = sessao.obter() ?: return null
        val usuario = dao.buscarPorId(id)
        if (usuario == null) {
            sessao.limpar()
            _usuarioId.value = null
        }
        return usuario
    }

    suspend fun cadastrar(nome: String, email: String, senha: String): ResultadoAuth {
        val emailLimpo = email.trim().lowercase()
        if (dao.buscarPorEmail(emailLimpo) != null) {
            return ResultadoAuth.Erro(MENSAGEM_EMAIL_REPETIDO)
        }
        return try {
            val novo = Usuario(nome = nome.trim(), email = emailLimpo, senhaHash = senha.sha256())
            val id = dao.inserir(novo)
            iniciarSessao(id)
            ResultadoAuth.Sucesso(novo.copy(id = id))
        } catch (erro: SQLiteConstraintException) {
            ResultadoAuth.Erro(MENSAGEM_EMAIL_REPETIDO)
        }
    }

    suspend fun entrar(email: String, senha: String): ResultadoAuth {
        val usuario = dao.buscarPorEmail(email.trim().lowercase())
        if (usuario == null || usuario.senhaHash != senha.sha256()) {
            return ResultadoAuth.Erro(MENSAGEM_LOGIN_INVALIDO)
        }
        iniciarSessao(usuario.id)
        return ResultadoAuth.Sucesso(usuario)
    }

    fun sair() {
        sessao.limpar()
        _usuarioId.value = null
    }

    private fun iniciarSessao(id: Long) {
        sessao.salvar(id)
        _usuarioId.value = id
    }

    companion object {
        const val MENSAGEM_EMAIL_REPETIDO = "Este e-mail já está cadastrado."
        const val MENSAGEM_LOGIN_INVALIDO = "E-mail ou senha incorretos."
    }
}
