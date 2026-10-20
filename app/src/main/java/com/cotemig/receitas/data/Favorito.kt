package com.cotemig.receitas.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "favoritos",
    primaryKeys = ["usuarioId", "idReceita"],
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class Favorito(
    val usuarioId: Long,
    val idReceita: String,
    val nome: String,
    val imagem: String,
    val categoria: String,
    val area: String
)
