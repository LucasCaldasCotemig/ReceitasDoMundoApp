package com.cotemig.receitas.data

import java.security.MessageDigest

fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }

object Validacao {

    private val padraoEmail = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun nomeValido(nome: String): Boolean = nome.trim().length >= 2

    fun emailValido(email: String): Boolean = padraoEmail.matches(email.trim())

    fun senhaValida(senha: String): Boolean = senha.length >= 6
}
