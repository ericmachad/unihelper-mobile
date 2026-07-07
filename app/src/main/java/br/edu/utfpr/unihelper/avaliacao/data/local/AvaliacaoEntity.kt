package br.edu.utfpr.unihelper.avaliacao.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avaliacoes")
data class AvaliacaoEntity(
    @PrimaryKey
    val id: String,
    val descricao: String,
    val peso: Float,
    val data: String,
    val valor: Float? = null,
    val disciplinaId: String
)
