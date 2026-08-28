package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.formatDate
import com.example.ui.screens.calendar.WeddingCalendarScreen
import com.example.ui.screens.dashboard.WeddingDashboardScreen
import com.example.ui.screens.expenses.ExpenseListScreen
import com.example.ui.screens.family.FamilyManagementScreen
import com.example.ui.screens.gifts.GiftManagementScreen
import com.example.ui.screens.guests.GuestManagementScreen
import com.example.ui.screens.home.WeddingHomeScreen
import com.example.ui.screens.notes.NotesIdeasScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.rituals.RitualDetailScreen
import com.example.ui.screens.rituals.RitualListScreen
import com.example.ui.screens.search.GlobalSearchScreen
import com.example.ui.screens.shopping.ShoppingPlannerScreen
import com.example.ui.screens.sync.CloudSyncScreen
import com.example.ui.screens.tasks.TaskManagementScreen
import com.example.ui.screens.vendors.VendorListScreen
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.WeddingViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Weddings", Icons.Default.SwapHoriz)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object CloudSync : Screen("cloud_sync", "Cloud Sync & Share", Icons.Default.CloudSync)
    object Rituals : Screen("rituals", "26 Rituals", Icons.Default.Celebration)
    object Expenses : Screen("expenses", "Expenses", Icons.Default.ReceiptLong)
    object Shopping : Screen("shopping", "Shopping", Icons.Default.ShoppingCart)
    object Guests : Screen("guests", "Guests", Icons.Default.Groups)
    object Vendors : Screen("vendors", "Vendors", Icons.Default.Store)
    object Tasks : Screen("tasks", "Tasks", Icons.Default.Assignment)
    object Family : Screen("family", "Family Roles", Icons.Default.People)
    object Calendar : Screen("calendar", "Timeline", Icons.Default.CalendarMonth)
    object Gifts : Screen("gifts", "Shagun & Gifts", Icons.Default.CardGiftcard)
    object Notes : Screen("notes", "Traditions & Geet", Icons.Default.Note)
    object Reports : Screen("reports", "Reports & Audit", Icons.Default.Assessment)
    object Search : Screen("search", "Search", Icons.Default.Search)
}

val BOTTOM_BAR_ITEMS = listOf(
    Screen.Dashboard,
    Screen.Rituals,
    Screen.Expenses,
    Screen.Shopping,
    Screen.Guests
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeddingMainApp(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentWedding by viewModel.currentWedding.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isHomeScreen = currentRoute == Screen.Home.route
    val isRitualDetail = currentRoute?.startsWith("ritual_detail/") == true

    LaunchedEffect(currentRoute) {
        if (drawerState.isOpen) {
            drawerState.snapTo(DrawerValue.Closed)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isHomeScreen,
        drawerContent = {
            if (currentWedding != null) {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.width(300.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RoyalMaroonDark)
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Marwadi Wedding Planner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = currentWedding!!.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = RoyalGoldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${currentWedding!!.groomName} ❤️ ${currentWedding!!.brideName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = RoyalMaroon) },
                            label = { Text("Switch / Manage Weddings") },
                            selected = false,
                            onClick = {
                                scope.launch {
                                    drawerState.snapTo(DrawerValue.Closed)
                                    navController.navigate(Screen.Home.route) {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

                        val drawerItems = listOf(
                            Screen.Dashboard,
                            Screen.CloudSync,
                            Screen.Rituals,
                            Screen.Expenses,
                            Screen.Shopping,
                            Screen.Guests,
                            Screen.Vendors,
                            Screen.Tasks,
                            Screen.Family,
                            Screen.Calendar,
                            Screen.Gifts,
                            Screen.Notes,
                            Screen.Reports,
                            Screen.Search
                        )

                        drawerItems.forEach { screen ->
                            NavigationDrawerItem(
                                icon = { Icon(screen.icon, contentDescription = null, tint = if (currentRoute == screen.route) RoyalMaroon else Color.Gray) },
                                label = { Text(screen.title, fontWeight = if (currentRoute == screen.route) FontWeight.Bold else FontWeight.Normal) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (currentRoute != screen.route) {
                                        if (screen.route == Screen.Dashboard.route) {
                                            navController.navigate(Screen.Dashboard.route) {
                                                popUpTo(Screen.Dashboard.route) {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            navController.navigate(screen.route) {
                                                popUpTo(Screen.Dashboard.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = RoyalMaroon.copy(alpha = 0.12f),
                                    selectedTextColor = RoyalMaroon
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isHomeScreen && !isRitualDetail && currentWedding != null) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = currentWedding!!.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${currentWedding!!.groomName} ❤️ ${currentWedding!!.brideName} • ${formatDate(currentWedding!!.weddingDate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RoyalGoldLight
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(Screen.CloudSync.route) }) {
                                Icon(
                                    Icons.Default.CloudSync,
                                    contentDescription = "Cloud Sync & Share",
                                    tint = RoyalGoldLight
                                )
                            }
                            IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = RoyalGoldLight)
                            }
                            IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Wedding", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalMaroonDark)
                    )
                }
            },
            bottomBar = {
                if (!isHomeScreen && !isRitualDetail && currentWedding != null) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = RoyalMaroon
                    ) {
                        BOTTOM_BAR_ITEMS.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        letterSpacing = (-0.3).sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        if (screen.route == Screen.Dashboard.route) {
                                            navController.navigate(Screen.Dashboard.route) {
                                                popUpTo(Screen.Dashboard.route) {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            navController.navigate(screen.route) {
                                                popUpTo(Screen.Dashboard.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RoyalMaroon,
                                    selectedTextColor = RoyalMaroon,
                                    indicatorColor = RoyalMaroon.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("bottom_nav_${screen.route}")
                            )
                        }
                    }
                }
            },
            modifier = modifier
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    WeddingHomeScreen(
                        viewModel = viewModel,
                        onWeddingSelected = {
                            scope.launch {
                                drawerState.snapTo(DrawerValue.Closed)
                            }
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSync = {
                            navController.navigate(Screen.CloudSync.route)
                        }
                    )
                }

                composable(Screen.Dashboard.route) {
                    WeddingDashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }

                composable(Screen.CloudSync.route) {
                    CloudSyncScreen(viewModel = viewModel)
                }

                composable(Screen.Rituals.route) {
                    RitualListScreen(
                        viewModel = viewModel,
                        onRitualSelected = { ritualId ->
                            navController.navigate("ritual_detail/$ritualId")
                        }
                    )
                }

                composable(
                    route = "ritual_detail/{ritualId}",
                    arguments = listOf(navArgument("ritualId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val ritualId = backStackEntry.arguments?.getLong("ritualId") ?: 0L
                    RitualDetailScreen(
                        ritualId = ritualId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Expenses.route) {
                    ExpenseListScreen(viewModel = viewModel)
                }

                composable(Screen.Shopping.route) {
                    ShoppingPlannerScreen(viewModel = viewModel)
                }

                composable(Screen.Guests.route) {
                    GuestManagementScreen(viewModel = viewModel)
                }

                composable(Screen.Vendors.route) {
                    VendorListScreen(viewModel = viewModel)
                }

                composable(Screen.Tasks.route) {
                    TaskManagementScreen(viewModel = viewModel)
                }

                composable(Screen.Family.route) {
                    FamilyManagementScreen(viewModel = viewModel)
                }

                composable(Screen.Calendar.route) {
                    WeddingCalendarScreen(viewModel = viewModel)
                }

                composable(Screen.Gifts.route) {
                    GiftManagementScreen(viewModel = viewModel)
                }

                composable(Screen.Notes.route) {
                    NotesIdeasScreen(viewModel = viewModel)
                }

                composable(Screen.Reports.route) {
                    ReportsScreen(viewModel = viewModel)
                }

                composable(Screen.Search.route) {
                    GlobalSearchScreen(
                        viewModel = viewModel,
                        onNavigateToRitual = { id -> navController.navigate("ritual_detail/$id") },
                        onNavigateToModule = { route -> navController.navigate(route) }
                    )
                }
            }
        }
    }
}
