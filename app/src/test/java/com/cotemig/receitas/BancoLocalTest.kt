package com.cotemig.receitas

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cotemig.receitas.data.AppDatabase
import com.cotemig.receitas.data.Receita
import com.cotemig.receitas.data.FavoritoRepository
import com.cotemig.receitas.data.ResultadoAuth
import com.cotemig.receitas.data.SessaoStorage
import com.cotemig.receitas.data.UsuarioRepository
import com.cotemig.receitas.data.sha256
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BancoLocalTest {

    private lateinit var contexto: Context
    private lateinit var banco: AppDatabase
    private lateinit var sessao: SessaoStorage
    private lateinit var usuarios: UsuarioRepository
    private lateinit var favoritos: FavoritoRepository

    private val feijoada = Receita(id = "10", nome = "Feijoada", imagem = "https://exemplo.com/10.jpg", categoria = "Pork", area = "Brazilian")
    private val flan = Receita(id = "20", nome = "Flan", imagem = "https://exemplo.com/20.jpg", categoria = "Dessert", area = "Uruguayan")

    @Before
    fun preparar() {
        contexto = ApplicationProvider.getApplicationContext()
        contexto.getSharedPreferences("sessao", Context.MODE_PRIVATE).edit().clear().commit()
        banco = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessao = SessaoStorage(contexto)
        usuarios = UsuarioRepository(banco.usuarioDao(), sessao)
        favoritos = FavoritoRepository(banco.favoritoDao())
    }

    @After
    fun encerrar() {
        banco.close()
    }

    @Test
    fun cadastroGuardaSenhaComoHashEIniciaSessao() = runBlocking {
        val resultado = usuarios.cadastrar("Lucas", "Lucas@Teste.com", "segredo1")

        assertTrue(resultado is ResultadoAuth.Sucesso)
        val salvo = banco.usuarioDao().buscarPorEmail("lucas@teste.com")
        assertNotNull(salvo)
        assertEquals("segredo1".sha256(), salvo!!.senhaHash)
        assertNotEquals("segredo1", salvo.senhaHash)
        assertEquals(salvo.id, usuarios.usuarioId.value)
        assertEquals(salvo.id, sessao.obter())
    }

    @Test
    fun emailRepetidoNaoPodeCadastrarDeNovo() = runBlocking {
        usuarios.cadastrar("Lucas", "lucas@teste.com", "segredo1")
        usuarios.sair()

        val resultado = usuarios.cadastrar("Outro", "LUCAS@teste.com", "outrasenha")

        assertEquals(ResultadoAuth.Erro(UsuarioRepository.MENSAGEM_EMAIL_REPETIDO), resultado)
        assertNull(usuarios.usuarioId.value)
    }

    @Test
    fun loginComSenhaErradaMostraMensagemGenericaESemSessao() = runBlocking {
        usuarios.cadastrar("Lucas", "lucas@teste.com", "segredo1")
        usuarios.sair()

        val senhaErrada = usuarios.entrar("lucas@teste.com", "errada123")
        val emailInexistente = usuarios.entrar("naoexiste@teste.com", "segredo1")

        assertEquals(ResultadoAuth.Erro(UsuarioRepository.MENSAGEM_LOGIN_INVALIDO), senhaErrada)
        assertEquals(senhaErrada, emailInexistente)
        assertNull(usuarios.usuarioId.value)
        assertNull(sessao.obter())
    }

    @Test
    fun loginCorretoIniciaSessao() = runBlocking {
        usuarios.cadastrar("Lucas", "lucas@teste.com", "segredo1")
        usuarios.sair()

        val resultado = usuarios.entrar(" Lucas@teste.com ", "segredo1")

        assertTrue(resultado is ResultadoAuth.Sucesso)
        assertNotNull(usuarios.usuarioId.value)
    }

    @Test
    fun sessaoSobreviveAoFecharEReabrirOApp() = runBlocking {
        usuarios.cadastrar("Lucas", "lucas@teste.com", "segredo1")
        val id = usuarios.usuarioId.value

        val repositorioReaberto = UsuarioRepository(banco.usuarioDao(), SessaoStorage(contexto))

        assertEquals(id, repositorioReaberto.usuarioId.value)
        assertEquals("Lucas", repositorioReaberto.restaurarSessao()?.nome)
    }

    @Test
    fun sairLimpaASessao() = runBlocking {
        usuarios.cadastrar("Lucas", "lucas@teste.com", "segredo1")

        usuarios.sair()

        assertNull(usuarios.usuarioId.value)
        assertNull(SessaoStorage(contexto).obter())
    }

    @Test
    fun sessaoDeUsuarioInexistenteELimpa() = runBlocking {
        sessao.salvar(999)
        val repositorio = UsuarioRepository(banco.usuarioDao(), sessao)

        assertNull(repositorio.restaurarSessao())
        assertNull(repositorio.usuarioId.value)
        assertNull(sessao.obter())
    }

    @Test
    fun favoritosFicamGuardadosEPertencemACadaUsuario() = runBlocking {
        usuarios.cadastrar("Lucas", "lucas@teste.com", "segredo1")
        val idLucas = usuarios.usuarioId.value!!
        usuarios.sair()
        usuarios.cadastrar("Yslan", "yslan@teste.com", "segredo2")
        val idYslan = usuarios.usuarioId.value!!

        favoritos.adicionar(idLucas, feijoada)
        favoritos.adicionar(idLucas, flan)
        favoritos.adicionar(idYslan, flan)

        assertEquals(listOf("Feijoada", "Flan"), favoritos.observar(idLucas).first().map { it.nome })
        assertEquals(listOf("Flan"), favoritos.observar(idYslan).first().map { it.nome })

        favoritos.remover(idYslan, flan.id)

        assertTrue(favoritos.observar(idYslan).first().isEmpty())
        assertEquals(2, favoritos.observar(idLucas).first().size)
    }

    @Test
    fun alternarAdicionaERemoveOFavorito() = runBlocking {
        usuarios.cadastrar("Lucas", "lucas@teste.com", "segredo1")
        val id = usuarios.usuarioId.value!!

        favoritos.alternar(id, feijoada, jaFavorita = false)
        assertEquals(1, favoritos.observar(id).first().size)

        favoritos.alternar(id, feijoada, jaFavorita = true)
        assertTrue(favoritos.observar(id).first().isEmpty())
    }

    @Test
    fun favoritarDuasVezesNaoDuplica() = runBlocking {
        usuarios.cadastrar("Lucas", "lucas@teste.com", "segredo1")
        val id = usuarios.usuarioId.value!!

        favoritos.adicionar(id, feijoada)
        favoritos.adicionar(id, feijoada)

        assertEquals(1, favoritos.observar(id).first().size)
    }
}
