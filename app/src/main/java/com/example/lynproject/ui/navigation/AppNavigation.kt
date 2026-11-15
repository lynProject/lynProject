package com.example.lynproject.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lynproject.ui.repertoire.RepertoireScreen
import com.example.lynproject.ui.repertoire.SongEditScreen
import com.example.lynproject.ui.repertoire.SongEditViewModel
import com.example.lynproject.ui.setlist.SetlistPlaceholderScreen

/** All screen addresses ("routes") in one place. */
object Routes {
    const val REPERTOIRE = "repertoire"
    const val SETLISTS = "setlists"
    const val SONG_EDIT = "song_edit?${SongEditViewModel.SONG_ID_ARG}={${SongEditViewModel.SONG_ID_ARG}}"

    fun songEdit(songId: Long = SongEditViewModel.NEW_SONG_ID) =
        "song_edit?${SongEditViewModel.SONG_ID_ARG}=$songId"
}

private data class BottomTab(val route: String, val label: String, val icon: String)

private val bottomTabs = listOf(
    BottomTab(Routes.REPERTOIRE, "Repertoire", "🎵"),  // Lyn
    BottomTab(Routes.SETLISTS, "Setlists", "📋")       // Rym
)

/**
 * The app shell: bottom bar with two tabs + the NavHost that swaps screens.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        // Each screen draws its own top bar, so the shell only reserves space for the bottom bar
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // Hide the bottom bar on the add/edit form
            if (currentRoute in bottomTabs.map { it.route }) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(tab.icon) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.REPERTOIRE,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            // ---- Lyn: Repertoire feature ----
            composable(Routes.REPERTOIRE) {
                RepertoireScreen(
                    onAddSong = { navController.navigate(Routes.songEdit()) },
                    onEditSong = { id -> navController.navigate(Routes.songEdit(id)) }
                )
            }
            composable(
                route = Routes.SONG_EDIT,
                arguments = listOf(navArgument(SongEditViewModel.SONG_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = SongEditViewModel.NEW_SONG_ID
                })
            ) {
                SongEditScreen(onBack = { navController.popBackStack() })
            }

            // ---- Rym: Setlist feature ----
            composable(Routes.SETLISTS) {
                SetlistPlaceholderScreen()
            }
        }
    }
}
