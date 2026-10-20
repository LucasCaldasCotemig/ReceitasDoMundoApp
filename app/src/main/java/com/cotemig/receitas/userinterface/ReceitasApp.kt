package com.cotemig.receitas.userinterface

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cotemig.receitas.viewmodel.AuthViewModel
import com.cotemig.receitas.viewmodel.DetalheViewModel
import com.cotemig.receitas.viewmodel.FavoritosViewModel
import com.cotemig.receitas.viewmodel.ReceitasViewModel

private data class Aba(val rotulo: String, val rota: Any, val icone: ImageVector)

private val abas = listOf(
    Aba("Receitas", RotaReceitas, Icons.Filled.Restaurant),
    Aba("Favoritos", RotaFavoritos, Icons.Filled.Favorite),
    Aba("Perfil", RotaPerfil, Icons.Filled.Person)
)

@Composable
fun ReceitasApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val auth by authViewModel.estado.collectAsStateWithLifecycle()
    val logado = auth.usuarioId != null
    val inicio: Any = remember { if (logado) RotaReceitas else RotaLogin }

    LaunchedEffect(logado) {
        val destino = navController.currentDestination
        val naTelaDeAcesso = destino?.hasRoute<RotaLogin>() == true ||
            destino?.hasRoute<RotaCadastro>() == true
        if (logado && naTelaDeAcesso) {
            navController.navigate(RotaReceitas) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        } else if (!logado && destino != null && !naTelaDeAcesso) {
            navController.navigate(RotaLogin) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    val entradaAtual by navController.currentBackStackEntryAsState()
    val destinoAtual = entradaAtual?.destination
    val mostrarBarra = abas.any { destinoAtual?.hasRoute(it.rota::class) == true }

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar {
                    abas.forEach { aba ->
                        NavigationBarItem(
                            selected = destinoAtual?.hasRoute(aba.rota::class) == true,
                            onClick = {
                                navController.navigate(aba.rota) {
                                    popUpTo<RotaReceitas> { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(aba.icone, contentDescription = aba.rotulo) },
                            label = { Text(aba.rotulo) }
                        )
                    }
                }
            }
        }
    ) { espacamento ->
        NavHost(
            navController = navController,
            startDestination = inicio,
            modifier = Modifier.padding(espacamento)
        ) {
            composable<RotaLogin> {
                TelaLogin(
                    estado = auth,
                    onEntrar = authViewModel::entrar,
                    onIrParaCadastro = {
                        authViewModel.limparErro()
                        navController.navigate(RotaCadastro)
                    }
                )
            }
            composable<RotaCadastro> {
                TelaCadastro(
                    estado = auth,
                    onCadastrar = authViewModel::cadastrar,
                    onVoltar = {
                        authViewModel.limparErro()
                        navController.popBackStack()
                    }
                )
            }
            composable<RotaReceitas> {
                val viewModel: ReceitasViewModel = viewModel()
                TelaReceitas(
                    viewModel = viewModel,
                    onAbrirDetalhe = { id -> navController.navigate(RotaDetalhe(id)) }
                )
            }
            composable<RotaFavoritos> {
                val viewModel: FavoritosViewModel = viewModel()
                TelaFavoritos(
                    viewModel = viewModel,
                    onAbrirDetalhe = { id -> navController.navigate(RotaDetalhe(id)) }
                )
            }
            composable<RotaPerfil> {
                TelaPerfil(
                    estado = auth,
                    onSair = authViewModel::sair
                )
            }
            composable<RotaDetalhe> { entrada ->
                val rota = entrada.toRoute<RotaDetalhe>()
                val viewModel: DetalheViewModel = viewModel()
                TelaDetalhe(
                    idReceita = rota.idReceita,
                    viewModel = viewModel,
                    onVoltar = { navController.popBackStack() }
                )
            }
        }
    }
}
