package com.cotemig.receitas.userinterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cotemig.receitas.viewmodel.ListaEstado
import com.cotemig.receitas.viewmodel.ReceitasViewModel

@Composable
fun TelaReceitas(
    viewModel: ReceitasViewModel,
    onAbrirDetalhe: (String) -> Unit
) {
    val lista by viewModel.lista.collectAsStateWithLifecycle()
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()
    val categoriaSelecionada by viewModel.categoria.collectAsStateWithLifecycle()
    val favoritos by viewModel.favoritos.collectAsStateWithLifecycle()
    var texto by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Receitas",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)
        )
        OutlinedTextField(
            value = texto,
            onValueChange = {
                texto = it
                viewModel.buscar(it)
            },
            placeholder = { Text("Buscar receita pelo nome (em inglês)") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (texto.isNotEmpty()) {
                    IconButton(onClick = {
                        texto = ""
                        viewModel.buscar("")
                    }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Limpar busca")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (categorias.isNotEmpty()) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = categoriaSelecionada == null,
                        onClick = { viewModel.selecionarCategoria(null) },
                        label = { Text("Todas") }
                    )
                }
                items(categorias) { categoria ->
                    FilterChip(
                        selected = categoriaSelecionada == categoria,
                        onClick = { viewModel.selecionarCategoria(categoria) },
                        label = { Text(viewModel.rotuloCategoria(categoria)) }
                    )
                }
            }
        }

        when (val estado = lista) {
            ListaEstado.Carregando -> EstadoCarregando()
            is ListaEstado.Erro -> EstadoErro(
                mensagem = estado.mensagem,
                onTentarNovamente = viewModel::tentarNovamente
            )
            is ListaEstado.Sucesso -> {
                if (estado.receitas.isEmpty()) {
                    EstadoVazio("Nenhuma receita encontrada. Tente outro nome ou outra categoria.")
                } else {
                    LazyColumn(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(estado.receitas, key = { it.id }) { receita ->
                            CartaoReceita(
                                nome = receita.nome,
                                imagem = receita.imagem,
                                detalhe = descricaoCurta(receita.categoria, receita.area),
                                favorita = receita.id in favoritos,
                                onClick = { onAbrirDetalhe(receita.id) },
                                onFavoritar = { viewModel.alternarFavorito(receita) }
                            )
                        }
                    }
                }
            }
        }
    }
}
