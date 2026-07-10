package br.edu.utfpr.unihelper.dashboard.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.dashboard.data.DashboardEvent
import br.edu.utfpr.unihelper.ui.theme.Accent
import br.edu.utfpr.unihelper.ui.theme.Alert
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Border
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Success
import br.edu.utfpr.unihelper.ui.theme.Surface
import br.edu.utfpr.unihelper.ui.theme.TextGray
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.showFcmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissFcmDialog() },
            title = { Text("FCM Token", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (uiState.isLoadingToken) {
                        Text("Buscando token...", color = TextGray)
                    } else if (uiState.fcmToken != null) {
                        Text(
                            text = uiState.fcmToken!!,
                            fontSize = 11.sp,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("FCM Token", uiState.fcmToken))
                        }) {
                            Text("Copiar", color = Accent)
                        }
                    } else {
                        Text("Nenhum token encontrado", color = TextGray)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissFcmDialog() }) {
                    Text("Fechar")
                }
            }
        )
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Carregando...", color = TextGray)
        }
        return
    }

    if (uiState.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(uiState.error ?: "Erro", color = Alert)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            DashboardHeader(
                mesAno = viewModel.formatarMesAno(),
                onPreviousMonth = { viewModel.onMonthChange(false) },
                onNextMonth = { viewModel.onMonthChange(true) }
            )
        }

        item {
            CalendarCard(
                mesAtual = uiState.mesAtual,
                selectedDate = uiState.selectedDate,
                eventos = uiState.eventos,
                onDateSelected = { viewModel.onDateSelected(it) }
            )
        }

        item {
            val filtered = if (uiState.selectedDate != null) {
                uiState.eventos.filter { it.day == uiState.selectedDate }
            } else {
                uiState.eventos.sortedBy { it.dataHora }
            }
            val tituloLista = if (uiState.selectedDate != null) {
                "Eventos do dia ${uiState.selectedDate}"
            } else {
                "Próximos Eventos"
            }

            Text(
                text = tituloLista,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )

            if (filtered.isEmpty()) {
                EmptyEvents()
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                val pagerState = rememberPagerState(pageCount = { filtered.size })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    pageSpacing = 12.dp
                ) { page ->
                    EventCard(evento = filtered[page])
                }

                if (filtered.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(filtered.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(
                                        width = if (pagerState.currentPage == index) 24.dp else 8.dp,
                                        height = 8.dp
                                    )
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (pagerState.currentPage == index) Primary
                                        else Border
                                    )
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.showFcmDialog() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("🔑 Ver FCM Token", color = Surface)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DashboardHeader(
    mesAno: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mesAno,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Row {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Mês anterior",
                    tint = Primary
                )
            }
            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Próximo mês",
                    tint = Primary
                )
            }
        }
    }
}

@Composable
private fun CalendarCard(
    mesAtual: YearMonth,
    selectedDate: Int?,
    eventos: List<DashboardEvent>,
    onDateSelected: (Int) -> Unit
) {
    val diasSemana = listOf("D", "S", "T", "Q", "Q", "S", "S")
    val hoje = LocalDate.now()
    val primeiroDia = mesAtual.atDay(1)
    var offset = primeiroDia.dayOfWeek.value % 7

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Border),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                diasSemana.forEach { dia ->
                    Text(
                        text = dia,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val diasNoMes = mesAtual.lengthOfMonth()
            val totalCells = offset + diasNoMes
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dia = cellIndex - offset + 1

                        if (dia in 1..diasNoMes) {
                            val dayEventos = eventos.filter { it.day == dia }
                            val isSelected = dia == selectedDate
                            val isToday = hoje.year == mesAtual.year &&
                                    hoje.month == mesAtual.month &&
                                    hoje.dayOfMonth == dia

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> Primary
                                            isToday -> Primary.copy(alpha = 0.1f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDateSelected(dia) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dia.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Surface
                                            isToday -> Primary
                                            else -> Color.Black
                                        }
                                    )
                                    if (dayEventos.isNotEmpty()) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            dayEventos.take(3).forEach { e ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(
                                                            corPorTipo(e.type),
                                                            CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Border)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendaItem(cor = Alert, label = "Prova")
                LegendaItem(cor = Accent, label = "Trabalho")
                LegendaItem(cor = Success, label = "Outros")
            }
        }
    }
}

@Composable
private fun LegendaItem(cor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(cor, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 12.sp, color = TextGray)
    }
}

@Composable
private fun EventCard(evento: DashboardEvent) {
    val corBorda = corPorTipo(evento.type)
    val mesAbreviado = listOf("", "JAN", "FEV", "MAR", "ABR", "MAI", "JUN",
        "JUL", "AGO", "SET", "OUT", "NOV", "DEZ")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Border),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(modifier = Modifier.padding(end = 12.dp)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxWidth(0.015f)
                    .background(corBorda)
            )

            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .background(Background, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = mesAbreviado.getOrElse(evento.dataHora.substring(5, 7).toIntOrNull() ?: 0) { "" },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                    Text(
                        text = evento.day.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evento.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (evento.subject != null) {
                        Text(
                            text = evento.subject,
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }

                    if (evento.type == "PROVA") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Alert.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Alert,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Importante",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Alert
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyEvents() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = TextGray.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nenhum evento agendado",
                fontSize = 14.sp,
                color = TextGray
            )
        }
    }
}

private fun corPorTipo(type: String): Color {
    return when (type) {
        "PROVA" -> Alert
        "TRABALHO" -> Accent
        else -> Success
    }
}
