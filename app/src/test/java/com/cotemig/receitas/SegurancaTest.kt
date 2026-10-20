package com.cotemig.receitas

import com.cotemig.receitas.data.Validacao
import com.cotemig.receitas.data.sha256
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SegurancaTest {

    @Test
    fun sha256GeraOHashConhecido() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            "abc".sha256()
        )
    }

    @Test
    fun sha256NaoDevolveASenhaOriginal() {
        assertNotEquals("123456", "123456".sha256())
        assertEquals(64, "123456".sha256().length)
    }

    @Test
    fun emailComFormatoValidoEAceito() {
        assertTrue(Validacao.emailValido("lucas@aluno.cotemig.com.br"))
        assertTrue(Validacao.emailValido("  ana.silva+teste@gmail.com "))
    }

    @Test
    fun emailComFormatoInvalidoERecusado() {
        assertFalse(Validacao.emailValido(""))
        assertFalse(Validacao.emailValido("semarroba.com"))
        assertFalse(Validacao.emailValido("a@b"))
        assertFalse(Validacao.emailValido("a b@c.com"))
    }

    @Test
    fun senhaPrecisaTerSeisOuMaisCaracteres() {
        assertFalse(Validacao.senhaValida("12345"))
        assertTrue(Validacao.senhaValida("123456"))
    }

    @Test
    fun nomePrecisaTerAoMenosDoisCaracteres() {
        assertFalse(Validacao.nomeValido(" a "))
        assertTrue(Validacao.nomeValido("Yslan"))
    }
}
