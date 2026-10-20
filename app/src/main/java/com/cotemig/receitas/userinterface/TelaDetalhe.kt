package com.cotemig.receitas.userinterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cotemig.receitas.data.Receita
import com.cotemig.receitas.viewmodel.DetalheEstado
import com.cotemig.receitas.viewmodel.DetalheViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhe(
    idReceita: String,
    viewModel: DetalheViewModel,
    onVoltar: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val favoritos by viewModel.favoritos.collectAsStateWithLifecycle()

    LaunchedEffect(idReceita) {
        viewModel.carregar(idReceita)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Receita") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { espacamento ->
        when (val atual = estado) {
            DetalheEstado.Carregando -> EstadoCarregando(Modifier.padding(espacamento))
            is DetalheEstado.Erro -> EstadoErro(
                mensagem = atual.mensagem,
                onTentarNovamente = { viewModel.carregar(idReceita, forcar = true) },
                modifier = Modifier.padding(espacamento)
            )
            is DetalheEstado.Sucesso -> ConteudoDetalhe(
                receita = atual.receita,
                favorita = atual.receita.id in favoritos,
                onFavoritar = { viewModel.alternarFavorito(atual.receita) },
                modifier = Modifier.padding(espacamento)
            )
        }
    }
}

@Composable
private fun ConteudoDetalhe(
    receita: Receita,
    favorita: Boolean,
    onFavoritar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val abrirLink = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = receita.imagem,
            contentDescription = receita.nome,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.4f)
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = receita.nome, style = MaterialTheme.typography.headlineSmall)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                if (receita.categoria.isNotBlank()) {
                    AssistChip(onClick = {}, label = { Text(receita.categoria) })
                }
                if (receita.area.isNotBlank()) {
                    AssistChip(onClick = {}, label = { Text(receita.area) })
                }
            }

            if (favorita) {
                Button(onClick = onFavoritar, modifier = Modifier.padding(top = 12.dp)) {
                    Icon(Icons.Filled.Favorite, contentDescription = null)
                    Text("Nos favoritos", modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                OutlinedButton(onClick = onFavoritar, modifier = Modifier.padding(top = 12.dp)) {
                    Icon(Icons.Filled.FavoriteBorder, contentDescription = null)
                    Text("Adicionar aos favoritos", modifier = Modifier.padding(start = 8.dp))
                }
            }

            if (receita.ingredientes.isNotEmpty()) {
                Text(
                    text = "Ingredientes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                receita.ingredientes.forEach { ingrediente ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = ingrediente.nome,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = ingrediente.medida,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    HorizontalDivider()
                }
            }

            if (receita.instrucoes.isNotBlank()) {
                Text(
                    text = "Modo de preparo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                Text(text = receita.instrucoes, style = MaterialTheme.typography.bodyLarge)
            }

            if (receita.youtube.isNotBlank()) {
                OutlinedButton(
                    onClick = { abrirLink.openUri(receita.youtube) },
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text("Ver vídeo", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
