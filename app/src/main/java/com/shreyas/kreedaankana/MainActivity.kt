package com.shreyas.kreedaankana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.core.ui.theme.KreedaTheme
import com.shreyas.kreedaankana.features.auth.ui.LoginScreen
import com.shreyas.kreedaankana.features.booking.ui.*
import com.shreyas.kreedaankana.features.booking.viewmodel.BookingViewModel
import com.shreyas.kreedaankana.features.challenge.ui.*
import com.shreyas.kreedaankana.features.home.ui.HomeScreen
import com.shreyas.kreedaankana.features.score.ui.*
import com.shreyas.kreedaankana.features.profile.ui.UserProfileScreen
import com.shreyas.kreedaankana.features.notifications.ui.NotificationScreen
import com.shreyas.kreedaankana.features.team.ui.*
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KreedaTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val bottomBarRoutes = listOf("home", "grounds", "challenges", "scoreWall", "userProfile")
                val showBottomBar = currentDestination?.route in bottomBarRoutes
                val startDestination = if (AuthManager.isLoggedIn()) "home" else "login"
                val teamViewModel: TeamViewModel = viewModel()
                val teamState by teamViewModel.state.collectAsState()

                LaunchedEffect(Unit) {
                    if (AuthManager.isLoggedIn()) {
                        teamViewModel.loadMyTeams(AuthManager.getUserId())
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).clip(RoundedCornerShape(32.dp)),
                                containerColor = Color.White, tonalElevation = 10.dp
                            ) {
                                val items = listOf(
                                    Triple("Home", "home", Icons.Default.Home),
                                    Triple("Grounds", "grounds", Icons.Default.Sports),
                                    Triple("Challenges", "challenges", Icons.AutoMirrored.Filled.CompareArrows),
                                    Triple("Scores", "scoreWall", Icons.Default.BarChart),
                                    Triple("Profile", "userProfile", Icons.Default.Person)
                                )
                                items.forEach { (label, route, icon) ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == route } == true
                                    NavigationBarItem(
                                        icon = { Icon(icon, contentDescription = label) },
                                        label = { Text(label) },
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF00C853),
                                            selectedTextColor = Color(0xFF00C853),
                                            unselectedIconColor = Color.Gray,
                                            indicatorColor = Color(0xFFE8F5E9)
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.padding(innerPadding)) {
                        composable("login") {
                            LoginScreen(
                                onSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                                onError = { message -> scope.launch { snackbarHostState.showSnackbar(message) } }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                onNavigateToBooking = { navController.navigate("grounds") },
                                onNavigateToPostChallenge = { navController.navigate("postChallenge") },
                                onNavigateToSchedule = { navController.navigate("teamCalendar") },
                                onNavigateToScores = { navController.navigate("scoreWall") },
                                onNavigateToNotifications = { navController.navigate("notifications") },
                                onNavigateToCreateTeam = { navController.navigate("createTeam") },
                                onNavigateToDiscoverTeams = { navController.navigate("teamDiscovery") },
                                onNavigateToMyTeams = { navController.navigate("manageTeams") }
                            )
                        } // 🔹 FIXED: Restored this missing closing brace!

                        composable("teamDiscovery") {
                            TeamDiscoveryScreen(
                                onBack = { navController.popBackStack() },
                                onChallengeSuccess = {
                                    navController.popBackStack()
                                    scope.launch { snackbarHostState.showSnackbar("Request sent! 🤝") }
                                }
                            )
                        }

                        composable("createTeam") { CreateTeamScreen(onBack = { navController.popBackStack() }) }

                        composable("notifications") {
                            NotificationScreen(
                                onBack = { navController.popBackStack() },
                                onNotificationClick = { type, relatedId ->
                                    if (relatedId.isNotEmpty()) {
                                        when (type) {
                                            "CHALLENGE" -> navController.navigate("challenges")
                                            "CHALLENGE_RESPONSE" -> navController.navigate("challengeDetail/$relatedId")
                                            "MATCH" -> navController.navigate("matchDetail/$relatedId")
                                            else -> navController.navigate("home")
                                        }
                                    }
                                }
                            )
                        }

                        composable("userProfile") {
                            val context = LocalContext.current
                            val activity = remember(context) {
                                var c = context
                                while (c is android.content.ContextWrapper) {
                                    if (c is android.app.Activity) break
                                    c = c.baseContext
                                }
                                c as? android.app.Activity
                            }
                            UserProfileScreen(
                                onBack = { navController.popBackStack() },
                                onLogout = {
                                    activity?.let {
                                        AuthManager.logout(activity = it)
                                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                    }
                                },
                                onManageGrounds = { navController.navigate("manageGrounds") },
                                onManageTeam = { navController.navigate("manageTeams") },
                            )
                        }

                        composable("manageTeams") { ManageTeamsScreen(onBack = { navController.popBackStack() }, onTeamClick = { teamId -> navController.navigate("teamRoster/$teamId") }) }

                        composable("grounds") { GroundListScreen(onGroundClick = { id, name, type -> navController.navigate("groundCalendar/$id/$name") }, onAddGroundClick = { navController.navigate("addGround") }, onBack = { navController.popBackStack() }) }

                        composable("addGround") {
                            AddGroundScreen(
                                onSave = { ground ->
                                    lifecycleScope.launch {
                                        try {
                                            val db = FirebaseFirestore.getInstance()
                                            val groundWithOwner = ground.copy(ownerId = AuthManager.getUserId())
                                            db.collection("grounds").add(groundWithOwner).await()
                                            navController.popBackStack()
                                        } catch (e: Exception) {}
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("manageGrounds") { ManageGroundsScreen(onEditGround = { id -> navController.navigate("editGround/$id") }, onBack = { navController.popBackStack() }) }

                        composable("editGround/{groundId}", arguments = listOf(navArgument("groundId") { type = NavType.StringType })) { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("groundId") ?: ""
                            EditGroundScreen(groundId = id, onBack = { navController.popBackStack() })
                        }

                        composable("groundCalendar/{groundId}/{groundName}", arguments = listOf(navArgument("groundId") { type = NavType.StringType }, navArgument("groundName") { type = NavType.StringType })) { backStackEntry ->
                            val groundId = backStackEntry.arguments?.getString("groundId") ?: ""
                            val groundName = backStackEntry.arguments?.getString("groundName") ?: ""
                            GroundCalendarScreen(groundId = groundId, groundName = groundName, onBack = { navController.popBackStack() }, onBookClick = { date, slots ->
                                val slotString = slots.joinToString(",")
                                navController.navigate("bookingForm/$groundId/$groundName/$date/$slotString")
                            })
                        }

                        composable("bookingForm/{groundId}/{groundName}/{date}/{slots}", arguments = listOf(navArgument("groundId") { type = NavType.StringType }, navArgument("groundName") { type = NavType.StringType }, navArgument("date") { type = NavType.StringType }, navArgument("slots") { type = NavType.StringType })) { backStackEntry ->
                            val groundId = backStackEntry.arguments?.getString("groundId") ?: ""
                            val groundName = backStackEntry.arguments?.getString("groundName") ?: ""
                            val date = backStackEntry.arguments?.getString("date") ?: ""
                            val slotsString = backStackEntry.arguments?.getString("slots") ?: ""
                            val slotsList = if (slotsString.isNotEmpty()) slotsString.split(",") else emptyList()

                            BookingFormScreen(
                                groundId = groundId, groundName = groundName, date = date, slots = slotsList, onBack = { navController.popBackStack() },
                                onSuccess = { navController.navigate("myBookings") { popUpTo("home") { inclusive = false } } }
                            )
                        }

                        composable("myBookings") { MyBookingsScreen(onBack = { navController.popBackStack() }, onBookAnother = { navController.navigate("grounds") { popUpTo("home") { inclusive = false } } }) }

                        composable("teamRoster/{teamId}", arguments = listOf(navArgument("teamId") { type = NavType.StringType })) { backStackEntry ->
                            val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                            TeamHubScreen(teamId = teamId, onBack = { navController.popBackStack() }, onNavigateToNotifications = { navController.navigate("teamNotifications/$teamId") }, onNavigateToChat = { tId, tName -> navController.navigate("teamChat/$tId/$tName") })
                        }

                        composable("teamChat/{teamId}/{teamName}", arguments = listOf(navArgument("teamId") { type = NavType.StringType }, navArgument("teamName") { type = NavType.StringType })) { backStackEntry ->
                            val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                            val teamName = backStackEntry.arguments?.getString("teamName") ?: "Team"
                            TeamChatScreen(teamId = teamId, teamName = teamName, onBack = { navController.popBackStack() })
                        }

                        composable("teamNotifications/{teamId}", arguments = listOf(navArgument("teamId") { type = NavType.StringType })) { backStackEntry ->
                            val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                            TeamNotificationsScreen(teamId = teamId, onBack = { navController.popBackStack() })
                        }

                        composable("challenges") {
                            val myTeamIds = teamState.myTeams.map { it.id }
                            ChallengeListScreen(
                                myTeamIds = myTeamIds,
                                onBack = { navController.popBackStack() },
                                onChallengeClick = { challengeId -> navController.navigate("challengeDetail/$challengeId") },
                                onPostChallenge = { navController.navigate("postChallenge") }
                            )
                        }

                        composable("challengeDetail/{challengeId}", arguments = listOf(navArgument("challengeId") { type = NavType.StringType })) { backStackEntry ->
                            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: ""
                            ChallengeDetailScreen(
                                challengeId = challengeId,
                                onBack = { navController.popBackStack() },
                                onStartChat = { cId, responderName -> navController.navigate("negotiationChat/$cId/$responderName") }
                            )
                        }

                        composable("postChallenge") { PostChallengeScreen(onBack = { navController.popBackStack() }) }

                        composable("negotiationChat/{challengeId}/{responderName}", arguments = listOf(navArgument("challengeId") { type = NavType.StringType }, navArgument("responderName") { type = NavType.StringType })) { backStackEntry ->
                            val cId = backStackEntry.arguments?.getString("challengeId") ?: ""
                            val responderName = backStackEntry.arguments?.getString("responderName") ?: ""

                            NegotiationChatScreen(
                                challengeId = cId,
                                matchTitle = "Chat with $responderName",
                                onBack = { navController.popBackStack() },
                                onProposeMatch = { navController.navigate("confirmMatch/$cId") }
                            )
                        }

                        composable("confirmMatch/{challengeId}", arguments = listOf(navArgument("challengeId") { type = NavType.StringType })) { backStackEntry ->
                            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: ""
                            ConfirmMatchScreen(
                                challengeId = challengeId,
                                onBack = { navController.popBackStack() },
                                onConfirmLocked = { navController.navigate("teamCalendar") { popUpTo("challenges") { inclusive = false } } }
                            )
                        }

                        composable("teamCalendar") {
                            TeamCalendarScreen(
                                onBack = { navController.popBackStack() },
                                onMatchClick = { matchId ->
                                    navController.navigate("matchDetail/$matchId")
                                }
                            )
                        }

                        composable(
                            route = "matchDetail/{matchId}",
                            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                            MatchDetailScreen(
                                matchId = matchId,
                                onBack = { navController.popBackStack() },
                                onSubmitResult = {
                                    navController.navigate("submitResult/$matchId")
                                }
                            )
                        }

                        composable("scoreWall") { ScoreWallScreen(onBack = { navController.popBackStack() }) }

                        composable("leaderboard") { LeaderboardScreen(onBack = { navController.popBackStack() }) }

                        composable("submitResult/{matchId}", arguments = listOf(navArgument("matchId") { type = NavType.StringType })) { backStackEntry ->
                            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                            SubmitResultScreen(matchId = matchId, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}