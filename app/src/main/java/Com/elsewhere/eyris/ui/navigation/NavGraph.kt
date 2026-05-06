package Com.elsewhere.eyris.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import Com.elsewhere.eyris.ui.auth.AuthScreen
import Com.elsewhere.eyris.ui.search.SearchScreen
import Com.elsewhere.eyris.ui.dashboard.DashboardScreen
import Com.elsewhere.eyris.ui.leads.LeadsContactedScreen
import Com.elsewhere.eyris.ui.profile.BusinessProfileScreen
import Com.elsewhere.eyris.ui.export.ExportScreen
import Com.elsewhere.eyris.ui.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Auth.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(onAuthSuccess = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            })
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToLead = { leadId -> navController.navigate(Screen.BusinessProfile.createRoute(leadId)) }
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToProfile = { leadId -> navController.navigate(Screen.BusinessProfile.createRoute(leadId)) }
            )
        }
        
        composable(Screen.LeadsContacted.route) {
            LeadsContactedScreen(
                onNavigateToProfile = { leadId -> navController.navigate(Screen.BusinessProfile.createRoute(leadId)) }
            )
        }
        
        composable(Screen.BusinessProfile.route) { backStackEntry ->
            val leadId = backStackEntry.arguments?.getString("leadId") ?: ""
            BusinessProfileScreen(
                leadId = leadId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Export.route) {
            ExportScreen(onBack = { navController.popBackStack() })
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
