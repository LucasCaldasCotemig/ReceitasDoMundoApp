package com.cotemig.receitas.userinterface

import kotlinx.serialization.Serializable

@Serializable
object RotaLogin

@Serializable
object RotaCadastro

@Serializable
object RotaReceitas

@Serializable
object RotaFavoritos

@Serializable
object RotaPerfil

@Serializable
data class RotaDetalhe(val idReceita: String)
