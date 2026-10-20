package com.cotemig.receitas.userinterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cotemig.receitas.viewmodel.FavoritosViewModel

@Composable
fun TelaFavoritos(
    viewModel: FavoritosViewModel,
    onAbrirDetalhe: (String) -> Unit
) {
    val favoritos by viewModel.favoritos.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Favoritos",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)
        )
        if (favoritos.isEmpty()) {
            EstadoVazio("Você ainda não favoritou nenhuma receita. Toque no coração em uma receita para guardá-la aqui.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favoritos, key = { it.idReceita }) { favorito ->
                    CartaoReceita(
                        nome = favorito.nome,
                        imagem = favorito.imagem,
                        detalhe = descricaoCurta(favorito.categoria, favorito.area),
                        favorita = true,
                        onClick = { onAbrirDetalhe(favorito.idReceita) },
                        onFavoritar = { viewModel.remover(favorito.idReceita) }
                    )
                }
            }
        }
    }
}
