package br.edu.utfpr.unihelper.avaliacao.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.avaliacao.data.local.AvaliacaoEntity
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Border
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LancarNotaBottomSheet(
    avaliacao: AvaliacaoEntity,
    onSalvar: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val initialText = if (avaliacao.valor != null) "%.1f".format(avaliacao.valor) else ""
    var textFieldValue by remember { mutableStateOf(initialText) }
    var numericValue by remember { mutableStateOf(avaliacao.valor ?: 0f) }
    var hasError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Lançar Nota",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = avaliacao.descricao,
                fontSize = 14.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { value ->
                    val filtered = value.filter { c: Char -> c.isDigit() || c == '.' || c == '-' }
                    if (filtered.count { it == '.' } <= 1) {
                        textFieldValue = filtered
                        val parsed = filtered.toFloatOrNull()
                        if (parsed != null) {
                            numericValue = parsed
                            hasError = parsed < 0f || parsed > 10f
                        } else if (filtered.isEmpty() || filtered == "-") {
                            numericValue = 0f
                            hasError = false
                        }
                    }
                },
                label = { Text("Nota (0 a 10)") },
                isError = hasError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Primary,
                    unfocusedContainerColor = Background
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (hasError) {
                Text(
                    text = "Nota deve estar entre 0 e 10",
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!hasError && textFieldValue.isNotEmpty()) {
                        onSalvar(numericValue.coerceIn(0f, 10f))
                    }
                },
                enabled = !hasError && textFieldValue.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Salvar", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = TextGray)
            }
        }
    }
}
