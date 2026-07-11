package br.edu.utfpr.unihelper.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.edu.utfpr.unihelper.ui.theme.Primary
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SuccessDialogHandler(uiEvent: SharedFlow<UiEvent>) {
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.SuccessDialog -> message = event.message
                else -> { }
            }
        }
    }

    message?.let { msg ->
        AlertDialog(
            onDismissRequest = { message = null },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Sucesso",
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = msg,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { message = null },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }
            }
        )
    }
}
