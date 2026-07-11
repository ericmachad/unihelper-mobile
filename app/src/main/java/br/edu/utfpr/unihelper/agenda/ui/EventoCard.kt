package br.edu.utfpr.unihelper.agenda.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.agenda.data.local.EventoEntity
import br.edu.utfpr.unihelper.ui.theme.Accent
import br.edu.utfpr.unihelper.ui.theme.Alert
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Border
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Success
import br.edu.utfpr.unihelper.ui.theme.Surface
import br.edu.utfpr.unihelper.ui.theme.TextGray
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun EventoCard(
    evento: EventoEntity,
    mediaMinima: Float,
    onEditar: () -> Unit,
    onLancarNota: () -> Unit,
    onExcluir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notaTexto = if (evento.valor != null) {
        "%.1f".format(evento.valor)
    } else {
        "—"
    }

    val notaColor = when {
        evento.valor == null -> TextGray
        evento.valor >= mediaMinima -> Success
        else -> Alert
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Border),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = evento.titulo,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (evento.tipo) {
                                "PROVA" -> "Prova"
                                "TRABALHO" -> "Trabalho"
                                else -> evento.tipo
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Accent,
                            modifier = Modifier
                                .background(Accent.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    val dataExibicao = try {
                        LocalDateTime.parse(evento.dataHoraInicio).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    } catch (_: Exception) {
                        evento.dataHoraInicio.take(10)
                    }
                    Text(
                        text = "Peso: ${(evento.peso ?: 0f).toInt()}  |  $dataExibicao",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
                Text(
                    text = notaTexto,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = notaColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextGray, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onExcluir) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Alert, modifier = Modifier.size(18.dp))
                }
                OutlinedButton(
                    onClick = onLancarNota,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Primary)
                ) {
                    Text(
                        text = if (evento.valor == null) "Lançar Nota" else "Alterar Nota",
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
