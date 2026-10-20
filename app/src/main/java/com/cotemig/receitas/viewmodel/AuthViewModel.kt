package com.cotemig.receitas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cotemig.receitas.data.AppContainer
import com.cotemig.receitas.data.ResultadoAuth
import com.cotemig.receitas.data.Usuario
import com.cotemig.receitas.data.Validacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthEstado(
    val carregando: Boolean = false,
    val erro: String? = null,
    val usuarioId: Long? = null,
    val nome: String = "",
    val email: String = ""
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = AppContainer.obter(application).usuarios

    private val _estado = MutableStateFlow(AuthEstado(usuarioId = repositorio.usuarioId.value))
    val estado: StateFlow<AuthEstado> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = repositorio.restaurarSessao()
            _estado.value = usuario.paraEstado()
        }
    }

    fun entrar(email: String, senha: String) {
        when {
            !Validacao.emailValido(email) -> mostrarErro("Informe um e-mail válido.")
            senha.isEmpty() -> mostrarErro("Informe sua senha.")
            else -> executar { repositorio.entrar(email, senha) }
        }
    }

    fun cadastrar(nome: String, email: String, senha: String, confirmacao: String) {
        when {
            !Validacao.nomeValido(nome) -> mostrarErro("Informe seu nome.")
            !Validacao.emailValido(email) -> mostrarErro("Informe um e-mail válido.")
            !Validacao.senhaValida(senha) -> mostrarErro("A senha precisa ter 6 ou mais caracteres.")
            senha != confirmacao -> mostrarErro("As senhas não conferem.")
            else -> executar { repositorio.cadastrar(nome, email, senha) }
        }
    }

    fun sair() {
        repositorio.sair()
        _estado.value = AuthEstado()
    }

    fun limparErro() {
        _estado.update { it.copy(erro = null) }
    }

    private fun mostrarErro(mensagem: String) {
        _estado.update { it.copy(erro = mensagem) }
    }

    private fun executar(acao: suspend () -> ResultadoAuth) {
        if (_estado.value.carregando) return
        viewModelScope.launch {
            _estado.update { it.copy(carregando = true, erro = null) }
            when (val resultado = acao()) {
                is ResultadoAuth.Sucesso -> _estado.value = resultado.usuario.paraEstado()
                is ResultadoAuth.Erro -> _estado.update {
                    it.copy(carregando = false, erro = resultado.mensagem)
                }
            }
        }
    }

    private fun Usuario?.paraEstado(): AuthEstado =
        if (this == null) AuthEstado()
        else AuthEstado(usuarioId = id, nome = nome, email = email)
}
