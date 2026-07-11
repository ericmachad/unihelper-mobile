package br.edu.utfpr.unihelper.home.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import br.edu.utfpr.unihelper.core.ui.ErrorDialogHandler
import br.edu.utfpr.unihelper.core.ui.SuccessDialogHandler
import br.edu.utfpr.unihelper.core.ui.UiEvent
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import br.edu.utfpr.unihelper.agenda.ui.AgendaCRUDScreen
import br.edu.utfpr.unihelper.agenda.ui.AgendaViewModel
import br.edu.utfpr.unihelper.auth.ui.AuthViewModel
import br.edu.utfpr.unihelper.auth.ui.ProfileScreen
import br.edu.utfpr.unihelper.dashboard.ui.DashboardScreen
import br.edu.utfpr.unihelper.disciplina.ui.DisciplinaTabContent
import br.edu.utfpr.unihelper.disciplina.ui.DisciplinaViewModel
import br.edu.utfpr.unihelper.documento.ui.DocumentosTab
import br.edu.utfpr.unihelper.navigation.Routes
import br.edu.utfpr.unihelper.notificacao.data.repository.NotificacaoRepository
import br.edu.utfpr.unihelper.ui.theme.Alert
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Surface
import br.edu.utfpr.unihelper.ui.theme.TextGray
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

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
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    val activity = LocalActivity.current as ComponentActivity
    val disciplinaViewModel: DisciplinaViewModel = koinViewModel(viewModelStoreOwner = activity)
    val disciplinaState by disciplinaViewModel.uiState.collectAsState()

    val authViewModel: AuthViewModel = koinViewModel(viewModelStoreOwner = activity)
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user

    val agendaViewModel: AgendaViewModel = koinViewModel(viewModelStoreOwner = activity)
    val snackbarHostState = remember { SnackbarHostState() }

    val notificacaoRepository: NotificacaoRepository = koinInject()
    var totalNaoLidas by remember { mutableStateOf(0L) }
    var dashboardRefreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        notificacaoRepository.listar(apenasNaoLidas = true)
            .onSuccess { response ->
                totalNaoLidas = response.totalNaoLidas
            }
    }

    LaunchedEffect(user?.idUsuario) {
        if (user != null) {
            dashboardRefreshKey++
        }
    }

    LaunchedEffect(Unit) {
        disciplinaViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Snackbar -> snackbarHostState.showSnackbar(event.message)
                else -> { }
            }
        }
    }

    SuccessDialogHandler(uiEvent = disciplinaViewModel.uiEvent)
    ErrorDialogHandler(uiEvent = disciplinaViewModel.uiEvent)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Início"
                                1 -> "Disciplinas"
                                2 -> "Agenda"
                                3 -> "Documentos"
                                else -> "Meu Perfil"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (selectedTab) {
                                0 -> "Visão geral do seu semestre"
                                1 -> "Gestão de Notas e Faltas"
                                2 -> "Eventos e compromissos acadêmicos"
                                3 -> "Arquivos e notas por disciplina"
                                else -> "Gerir Conta"
                            },
                            fontSize = 13.sp,
                            color = TextGray
                        )
                    }
                },
                actions = {
                    if (selectedTab == 4) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            IconButton(onClick = { navController.navigate(Routes.NOTIFICACOES) }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificações",
                                    tint = Primary
                                )
                            }
                            if (totalNaoLidas > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(Alert, CircleShape)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (totalNaoLidas > 99) "99+" else totalNaoLidas.toString(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface
                )
            )
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
                0 -> DashboardScreen(refreshKey = dashboardRefreshKey)
                1 -> DisciplinaTabContent(
                    disciplinas = disciplinaState.disciplinas,
                    isLoading = disciplinaState.isLoading,
                    isRefreshing = disciplinaState.isRefreshing,
                    faltasAtualizando = disciplinaState.faltasAtualizando,
                    onNavigateToForm = { navController.navigate("disciplina/criar") },
                    onIncrementFalta = { disciplinaViewModel.alterarFaltas(it, "INCREMENTAR") },
                    onDecrementFalta = { disciplinaViewModel.alterarFaltas(it, "DECREMENTAR") },
                    onRefresh = { disciplinaViewModel.listar(isRefresh = true) },
                    onClickCard = { id -> navController.navigate("disciplina/$id") },
                    onEditClick = { id -> navController.navigate("disciplina/editar/$id") },
                    onDeleteClick = { id -> disciplinaViewModel.excluir(id) }
                )
                2 -> AgendaCRUDScreen(viewModel = agendaViewModel)
                3 -> DocumentosTab(
                    disciplinas = disciplinaState.disciplinas,
                    isLoadingDisciplinas = disciplinaState.isLoading
                )
                4 -> ProfileScreen(
                    onNavigateToEditProfile = { navController.navigate(Routes.EDITAR_PERFIL) },
                    onNavigateToChangePassword = { navController.navigate(Routes.ALTERAR_SENHA) },
                    onNavigateToNotificacoes = { navController.navigate(Routes.NOTIFICACOES) },
                    onLogout = {
                        authViewModel.logout {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}


