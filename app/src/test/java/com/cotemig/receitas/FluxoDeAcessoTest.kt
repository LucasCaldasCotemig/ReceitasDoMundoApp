package com.cotemig.receitas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cotemig.receitas.userinterface.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FluxoDeAcessoTest {

    @get:Rule
    val regra = createAndroidComposeRule<MainActivity>()

    private fun esperarTexto(texto: String, substring: Boolean = false) {
        regra.waitUntil(timeoutMillis = 15_000) {
            regra.onAllNodesWithText(texto, substring = substring).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun esperarSumir(texto: String) {
        regra.waitUntil(timeoutMillis = 15_000) {
            regra.onAllNodesWithText(texto).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun cadastroLoginSairEEntrarDeNovo() {
        val email = "teste${System.nanoTime()}@cotemig.com"

        esperarTexto("Entrar")
        regra.onNodeWithText("Entrar").assertIsDisplayed()

        regra.onNodeWithText("Ainda não tem conta? Cadastre-se").performClick()
        esperarTexto("Criar conta")

        regra.onNodeWithText("Nome").performTextInput("Lucas")
        regra.onNodeWithText("E-mail").performTextInput(email)
        regra.onNodeWithText("Senha (mínimo", substring = true).performTextInput("segredo1")
        regra.onNodeWithText("Confirmar senha").performTextInput("segredo1")
        regra.onNodeWithText("Cadastrar").performClick()

        esperarTexto("Perfil")
        esperarSumir("Cadastrar")

        regra.onNodeWithText("Favoritos").performClick()
        esperarTexto("Você ainda não favoritou", substring = true)

        regra.onNodeWithText("Perfil").performClick()
        esperarTexto(email)
        regra.onNodeWithText("Sair").performClick()

        esperarTexto("Entrar")
        esperarSumir("Perfil")

        regra.onNodeWithText("E-mail").performTextInput(email)
        regra.onNodeWithText("Senha").performTextInput("senhaerrada")
        regra.onNodeWithText("Entrar").performClick()
        esperarTexto("E-mail ou senha incorretos.")

        regra.onNodeWithText("Senha").performTextInput("x")
    }
}
