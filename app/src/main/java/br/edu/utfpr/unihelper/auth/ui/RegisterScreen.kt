package br.edu.utfpr.unihelper.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.ui.theme.Background
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.TextGray

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val viewModel: AuthViewModel = org.koin.androidx.compose.koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var nomeCompleto by remember { mutableStateOf("") }
    var apelido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val fieldErrors = remember { mutableStateMapOf<String, String?>() }
    val focusManager = LocalFocusManager.current
    val nomeTouched = remember { mutableStateOf(false) }
    val emailTouched = remember { mutableStateOf(false) }
    val senhaTouched = remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
        }
    }

    fun validarNomeCompleto(): Boolean {
        return when {
            nomeCompleto.isBlank() -> {
                fieldErrors["nomeCompleto"] = "Campo obrigatório"
                false
            }
            else -> {
                fieldErrors.remove("nomeCompleto")
                true
            }
        }
    }

    fun validarEmail(): Boolean {
        val emailNormalizado = email.trim().lowercase()
        return when {
            email.isBlank() -> {
                fieldErrors["email"] = "Campo obrigatório"
                false
            }
            !EMAIL_REGEX.matches(emailNormalizado) -> {
                fieldErrors["email"] = "Email inválido"
                false
            }
            else -> {
                fieldErrors.remove("email")
                true
            }
        }
    }

    fun validarSenha(): Boolean {
        return when {
            senha.isBlank() -> {
                fieldErrors["senha"] = "Campo obrigatório"
                false
            }
            senha.length < 6 -> {
                fieldErrors["senha"] = "Mínimo 6 caracteres"
                false
            }
            else -> {
                fieldErrors.remove("senha")
                true
            }
        }
    }

    fun validate(): Boolean {
        fieldErrors.clear()
        val nomeValido = validarNomeCompleto()
        val emailValido = validarEmail()
        val senhaValida = validarSenha()
        return nomeValido && emailValido && senhaValida
    }

    fun submit() {
        focusManager.clearFocus()
        if (validate()) {
            viewModel.register(
                nomeCompleto = nomeCompleto.trim(),
                apelido = apelido.trim().ifBlank { null },
                email = email.trim().lowercase(),
                senha = senha,
                curso = curso.trim().ifBlank { null }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Criar Conta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Preencha seus dados acadêmicos",
                fontSize = 14.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nomeCompleto,
                onValueChange = { nomeCompleto = it; fieldErrors.remove("nomeCompleto") },
                label = { Text("Nome completo") },
                placeholder = { Text("Seu nome") },
                isError = fieldErrors["nomeCompleto"] != null,
                supportingText = fieldErrors["nomeCompleto"]?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        if (state.isFocused) nomeTouched.value = true
                        if (!state.isFocused && nomeTouched.value) validarNomeCompleto()
                    },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = apelido,
                onValueChange = { apelido = it },
                label = { Text("Apelido") },
                placeholder = { Text("Como prefere ser chamado") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; fieldErrors.remove("email") },
                label = { Text("Email") },
                placeholder = { Text("seu@email.com") },
                isError = fieldErrors["email"] != null,
                supportingText = fieldErrors["email"]?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        if (state.isFocused) emailTouched.value = true
                        if (!state.isFocused && emailTouched.value) validarEmail()
                    },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = curso,
                onValueChange = { curso = it },
                label = { Text("Curso") },
                placeholder = { Text("Ex: Engenharia de Software") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it; fieldErrors.remove("senha") },
                label = { Text("Senha") },
                placeholder = { Text("Mínimo 6 caracteres") },
                isError = fieldErrors["senha"] != null,
                supportingText = fieldErrors["senha"]?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        if (state.isFocused) senhaTouched.value = true
                        if (!state.isFocused && senhaTouched.value) validarSenha()
                    },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { submit() }
                ),
                singleLine = true
            )

            uiState.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                ),
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = "Cadastrar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Primary,
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary),
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = "Já tenho conta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Color(0xFFE5E7EB),
    focusedBorderColor = Primary,
    unfocusedContainerColor = Color(0xFFF9FAFB),
    focusedContainerColor = Color(0xFFF9FAFB),
)
