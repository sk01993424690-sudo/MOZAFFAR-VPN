package com.example

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.ThemeMode
import com.example.localization.AppLanguage
import com.example.localization.BengaliStrings
import com.example.localization.EnglishStrings
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.HomeRoute
import com.example.ui.LegalRoute
import com.example.ui.MozaffarVpnViewModel
import com.example.ui.PremiumRoute
import com.example.ui.ServersRoute
import com.example.ui.SettingsRoute
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LegalDocumentScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LocalVpnTokens
import com.example.ui.theme.MozaffarVpnTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MozaffarVpnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (uiState.preferences.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> systemDark
            }
            val appLanguage = uiState.preferences.language
            val appStrings = if (appLanguage == AppLanguage.BENGALI) BengaliStrings else EnglishStrings

            CompositionLocalProvider(
                LocalAppLanguage provides appLanguage,
                LocalAppStrings provides appStrings
            ) {
                MozaffarVpnTheme(darkTheme = darkTheme) {
                    MozaffarVpnRoot(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshSystemLockdownStatus()
    }
}

@Composable
private fun MozaffarVpnRoot(viewModel: MozaffarVpnViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val tokens = LocalVpnTokens.current
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onVpnPermissionResult(result.resultCode == Activity.RESULT_OK)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.toggleVpnConnection { intent ->
            vpnPermissionLauncher.launch(intent)
        }
    }

    fun triggerVpnToggle() {
        val currentState = uiState.connectionState
        val isDisconnecting = currentState is com.example.data.VpnConnectionState.Connected ||
            currentState is com.example.data.VpnConnectionState.Connecting ||
            currentState is com.example.data.VpnConnectionState.Reconnecting
        if (!isDisconnecting && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        viewModel.toggleVpnConnection { intent ->
            vpnPermissionLauncher.launch(intent)
        }
    }

    val isTopLevelRoute = currentDestination?.let { dest ->
        dest.hasRoute<HomeRoute>() || dest.hasRoute<ServersRoute>() || dest.hasRoute<SettingsRoute>()
    } ?: true

    fun navigateTopLevel(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.backgroundGradient)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (useRail && isTopLevelRoute) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                        containerColor = tokens.cardSurface
                    ) {
                        NavigationRailItem(
                            selected = currentDestination?.hasRoute<HomeRoute>() == true,
                            onClick = { navigateTopLevel(HomeRoute) },
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination?.hasRoute<HomeRoute>() == true)
                                        Icons.Filled.Shield else Icons.Outlined.Shield,
                                    contentDescription = strings.navHome
                                )
                            },
                            label = { Text(strings.navHome) },
                            modifier = Modifier.testTag("nav_rail_home")
                        )
                        NavigationRailItem(
                            selected = currentDestination?.hasRoute<ServersRoute>() == true,
                            onClick = { navigateTopLevel(ServersRoute) },
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination?.hasRoute<ServersRoute>() == true)
                                        Icons.Filled.Public else Icons.Outlined.Public,
                                    contentDescription = strings.navServers
                                )
                            },
                            label = { Text(strings.navServers) },
                            modifier = Modifier.testTag("nav_rail_servers")
                        )
                        NavigationRailItem(
                            selected = currentDestination?.hasRoute<SettingsRoute>() == true,
                            onClick = { navigateTopLevel(SettingsRoute) },
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination?.hasRoute<SettingsRoute>() == true)
                                        Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = strings.navSettings
                                )
                            },
                            label = { Text(strings.navSettings) },
                            modifier = Modifier.testTag("nav_rail_settings")
                        )
                    }
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (!useRail && isTopLevelRoute) {
                            NavigationBar(
                                containerColor = tokens.cardSurface,
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                NavigationBarItem(
                                    selected = currentDestination?.hasRoute<HomeRoute>() == true,
                                    onClick = { navigateTopLevel(HomeRoute) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentDestination?.hasRoute<HomeRoute>() == true)
                                                Icons.Filled.Shield else Icons.Outlined.Shield,
                                            contentDescription = strings.navHome
                                        )
                                    },
                                    label = { Text(strings.navHome) },
                                    modifier = Modifier.testTag("nav_bar_home")
                                )
                                NavigationBarItem(
                                    selected = currentDestination?.hasRoute<ServersRoute>() == true,
                                    onClick = { navigateTopLevel(ServersRoute) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentDestination?.hasRoute<ServersRoute>() == true)
                                                Icons.Filled.Public else Icons.Outlined.Public,
                                            contentDescription = strings.navServers
                                        )
                                    },
                                    label = { Text(strings.navServers) },
                                    modifier = Modifier.testTag("nav_bar_servers")
                                )
                                NavigationBarItem(
                                    selected = currentDestination?.hasRoute<SettingsRoute>() == true,
                                    onClick = { navigateTopLevel(SettingsRoute) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentDestination?.hasRoute<SettingsRoute>() == true)
                                                Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = strings.navSettings
                                        )
                                    },
                                    label = { Text(strings.navSettings) },
                                    modifier = Modifier.testTag("nav_bar_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable<HomeRoute> {
                            HomeScreen(
                                uiState = uiState,
                                onConnectClick = { triggerVpnToggle() },
                                onOpenServers = { navigateTopLevel(ServersRoute) },
                                onOpenPremium = { navController.navigate(PremiumRoute) },
                                onOpenVpnSettings = { viewModel.openSystemVpnSettings() },
                                onRefreshIp = { viewModel.refreshPublicIp() },
                                onToggleLanguage = {
                                    val next = if (uiState.preferences.language == AppLanguage.ENGLISH)
                                        AppLanguage.BENGALI else AppLanguage.ENGLISH
                                    viewModel.setLanguage(next)
                                },
                                onToggleTheme = {
                                    val next = if (tokens.isDark) ThemeMode.LIGHT else ThemeMode.DARK
                                    viewModel.setThemeMode(next)
                                },
                                onWatchRewardedAd = { activity ->
                                    viewModel.showRewardedAdForPass(activity) {
                                        Toast.makeText(
                                            context,
                                            "Rewarded ad is loading. Try again in a moment.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onImportConfigAndConnect = { serverId, rawConfig ->
                                    viewModel.importConfigAndConnect(serverId, rawConfig) { intent ->
                                        vpnPermissionLauncher.launch(intent)
                                    }
                                }
                            )
                        }

                        composable<ServersRoute> {
                            ServersScreen(
                                uiState = uiState,
                                onSelectServer = { serverId, activity ->
                                    viewModel.selectServer(serverId, activity)
                                    navigateTopLevel(HomeRoute)
                                },
                                onRefreshPings = { viewModel.refreshAllServerPings() },
                                onImportConfig = { serverId, raw ->
                                    viewModel.importConfigForServer(serverId, raw)
                                },
                                onClearConfig = { serverId ->
                                    viewModel.clearConfigForServer(serverId)
                                }
                            )
                        }

                        composable<SettingsRoute> {
                            SettingsScreen(
                                uiState = uiState,
                                onLanguageSelected = { viewModel.setLanguage(it) },
                                onThemeModeSelected = { viewModel.setThemeMode(it) },
                                onAutoReconnectChanged = { viewModel.setAutoReconnect(it) },
                                onDnsLeakProtectionChanged = { viewModel.setDnsLeakProtection(it) },
                                onDnsProviderSelected = { viewModel.setDnsProvider(it) },
                                onOpenVpnSettings = { viewModel.openSystemVpnSettings() },
                                onRegenerateKeys = { viewModel.regenerateDeviceKeyPair() },
                                onOpenPremium = { navController.navigate(PremiumRoute) },
                                onOpenLegalDoc = { docType ->
                                    navController.navigate(LegalRoute(docType))
                                }
                            )
                        }

                        composable<PremiumRoute> {
                            PremiumScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onSubscribe = { activity, sku ->
                                    viewModel.subscribe(activity, sku)
                                },
                                onRestorePurchases = { viewModel.restorePurchases() },
                                onWatchRewardedAd = { activity, onUnavailable ->
                                    viewModel.showRewardedAdForPass(activity, onUnavailable)
                                }
                            )
                        }

                        composable<LegalRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<LegalRoute>()
                            LegalDocumentScreen(
                                docType = route.docType,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
