package br.edu.utfpr.unihelper.home.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import br.edu.utfpr.unihelper.agenda.ui.AgendaCRUDScreen
import br.edu.utfpr.unihelper.agenda.ui.AgendaViewModel
import br.edu.utfpr.unihelper.auth.ui.AuthViewModel
import br.edu.utfpr.unihelper.dashboard.ui.DashboardScreen
import br.edu.utfpr.unihelper.disciplina.ui.DisciplinaTabContent
import br.edu.utfpr.unihelper.disciplina.ui.DisciplinaViewModel
import br.edu.utfpr.unihelper.navigation.Routes
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Surface
import br.edu.utfpr.unihelper.ui.theme.TextGray
import org.koin.androidx.compose.koinViewModel

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

private val navItems = listOf(
    BottomNavItem("Início", Icons.Default.Home),
    BottomNavItem("Disciplinas", Icons.Default.Book),
    BottomNavItem("Agenda", Icons.Default.CalendarMonth),
    BottomNavItem("Documentos", Icons.Default.Folder),
    BottomNavItem("Perfil", Icons.Default.Person)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController
) {
    var selectedTab by remember { mutableStateOf(1) }

    val activity = LocalActivity.current as ComponentActivity
    val disciplinaViewModel: DisciplinaViewModel = koinViewModel(viewModelStoreOwner = activity)
    val disciplinaState by disciplinaViewModel.uiState.collectAsState()

    val authViewModel: AuthViewModel = koinViewModel(viewModelStoreOwner = activity)
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user

    val deleteState by disciplinaViewModel.deleteState.collectAsState()
    val agendaViewModel: AgendaViewModel = koinViewModel(viewModelStoreOwner = activity)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(deleteState.sucesso, deleteState.error) {
        when {
            deleteState.sucesso -> {
                snackbarHostState.showSnackbar("Disciplina excluída")
                disciplinaViewModel.limparDeleteState()
            }
            deleteState.error != null -> {
                snackbarHostState.showSnackbar(deleteState.error ?: "Erro ao excluir")
                disciplinaViewModel.limparDeleteState()
            }
        }
    }

    LaunchedEffect(disciplinaState.error) {
        disciplinaState.error?.let { msg ->
            if (disciplinaState.disciplinas.isNotEmpty()) {
                snackbarHostState.showSnackbar(msg)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            when (selectedTab) {
                1 -> {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Disciplinas",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Gestão de Notas e Faltas",
                                    fontSize = 13.sp,
                                    color = TextGray
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Surface
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                1 -> {
                    FloatingActionButton(
                        onClick = { navController.navigate("disciplina/criar") },
                        containerColor = Primary,
                        contentColor = Surface
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Disciplina")
                    }
                }
                2 -> {
                    FloatingActionButton(
                        onClick = { agendaViewModel.abrirCriarEvento() },
                        containerColor = Primary,
                        contentColor = Surface
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Evento")
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                tonalElevation = 0.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                maxLines = 1,
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray,
                            indicatorColor = Primary.copy(alpha = 0.08f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> DashboardScreen()
                1 -> DisciplinaTabContent(
                    disciplinas = disciplinaState.disciplinas,
                    isLoading = disciplinaState.isLoading,
                    isRefreshing = disciplinaState.isRefreshing,
                    error = disciplinaState.error,
                    faltasAtualizando = disciplinaState.faltasAtualizando,
                    onNavigateToForm = { navController.navigate("disciplina/criar") },
                    onIncrementFalta = { disciplinaViewModel.alterarFaltas(it, "INCREMENTAR") },
                    onDecrementFalta = { disciplinaViewModel.alterarFaltas(it, "DECREMENTAR") },
                    onRefresh = { disciplinaViewModel.listar(isRefresh = true) },
                    onClickCard = { id -> navController.navigate("disciplina/$id") },
                    onDeleteClick = { id -> disciplinaViewModel.excluir(id) },
                    userName = user?.nomeCompleto,
                    userCurso = user?.curso
                )
                2 -> AgendaCRUDScreen(viewModel = agendaViewModel)
                3 -> DocumentosTab()
                4 -> PerfilTab(navController = navController)
            }
        }
    }
}

@Composable
private fun DocumentosTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Documentos",
            style = MaterialTheme.typography.titleLarge,
            color = TextGray
        )
    }
}

@Composable
private fun PerfilTab(navController: NavHostController) {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = user?.nomeCompleto ?: "Usuário",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )

            androidx.compose.material3.Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                modifier = Modifier.padding(top = 32.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFEF4444)
                )
            ) {
                Text("Sair")
            }
        }
    }
}
