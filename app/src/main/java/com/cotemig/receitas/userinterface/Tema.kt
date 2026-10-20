package com.cotemig.receitas.userinterface

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Color(0xFFB4441A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCE),
    onPrimaryContainer = Color(0xFF3A0B00),
    secondary = Color(0xFF5E6B2E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0EBA8),
    onSecondaryContainer = Color(0xFF1A2000),
    background = Color(0xFFFFFBF8),
    surface = Color(0xFFFFFBF8)
)

private val EsquemaEscuro = darkColorScheme(
    primary = Color(0xFFFFB59C),
    onPrimary = Color(0xFF5E1700),
    primaryContainer = Color(0xFF862D07),
    onPrimaryContainer = Color(0xFFFFDBCE),
    secondary = Color(0xFFC4CF8F),
    onSecondary = Color(0xFF2F3400),
    secondaryContainer = Color(0xFF454C14),
    onSecondaryContainer = Color(0xFFE0EBA8),
    background = Color(0xFF1F1B19),
    surface = Color(0xFF1F1B19)
)

@Composable
fun ReceitasTema(conteudo: @Composable () -> Unit) {
    val esquema = if (isSystemInDarkTheme()) EsquemaEscuro else EsquemaClaro
    MaterialTheme(colorScheme = esquema, content = conteudo)
}
