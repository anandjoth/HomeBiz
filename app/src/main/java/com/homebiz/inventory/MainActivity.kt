package com.yourbiz.inventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourbiz.inventory.data.AppDatabase
import com.yourbiz.inventory.data.repository.*
import com.yourbiz.inventory.ui.screens.grn.GoodsReceivedScreen
import com.yourbiz.inventory.ui.screens.invoice.InvoiceCreateScreen
import com.yourbiz.inventory.ui.screens.invoice.InvoicePayScreen
import com.yourbiz.inventory.ui.screens.items.ItemFormScreen
import com.yourbiz.inventory.ui.screens.items.ItemsListScreen
import com.yourbiz.inventory.ui.screens.settings.SettingsScreen
import com.yourbiz.inventory.ui.screens.settings.UpiAccountsScreen
import com.yourbiz.inventory.ui.theme.YourBizInventoryTheme
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Items : Screen("items")
    object ItemForm : Screen("item_form/{itemId}") {
        fun create(itemId: Long = 0) = "item_form/$itemId"
    }
    object GoodsReceived : Screen("goods_received")
    object InvoiceCreate : Screen("invoice_create")
    object InvoicePay : Screen("invoice_pay/{invoiceId}") {
        fun create(invoiceId: Long) = "invoice_pay/$invoiceId"
    }
    object Settings : Screen("settings")
    object UpiAccounts : Screen("upi_accounts")
}

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getDatabase(this) }
    private val settingsRepo by lazy { SettingsRepository(db.settingsDao()) }
    private val upiRepo by lazy { UpiAccountRepository(db.upiAccountDao()) }
    private val itemRepo by lazy { ItemRepository(db.itemDao()) }
    private val batchRepo by lazy { BatchRepository(db.batchDao()) }
    private val stockAdjRepo by lazy { StockAdjustmentRepository(db.stockAdjustmentDao(), db.batchDao()) }
    private val invoiceRepo by lazy {
        InvoiceRepository(
            db.invoiceDao(),
            db.invoiceLineDao(),
            db.paymentDao(),
            settingsRepo,
            db.batchDao()
        )
    }
    private val productionRepo by lazy { ProductionRepository(db.productionRunDao(), db.batchDao()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            settingsRepo.initIfMissing()
        }

        setContent {
            YourBizInventoryTheme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("HomeBiz") })
                    },
                    contentWindowInsets = WindowInsets.systemBars
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route
                        ) {
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    onNavigateItems = { navController.navigate(Screen.Items.route) },
                                    onNavigateGRN = { navController.navigate(Screen.GoodsReceived.route) },
                                    onNavigateInvoiceCreate = { navController.navigate(Screen.InvoiceCreate.route) },
                                    onNavigateSettings = { navController.navigate(Screen.Settings.route) }
                                )
                            }
                            composable(Screen.Items.route) {
                                ItemsListScreen(
                                    itemRepo = itemRepo,
                                    onAddItem = { navController.navigate(Screen.ItemForm.create(0)) },
                                    onEditItem = { id -> navController.navigate(Screen.ItemForm.create(id)) }
                                )
                            }
                            composable(
                                route = Screen.ItemForm.route,
                                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0
                                ItemFormScreen(
                                    itemRepo = itemRepo,
                                    itemId = itemId,
                                    onSaved = { navController.popBackStack() }
                                )
                            }
                            composable(Screen.GoodsReceived.route) {
                                GoodsReceivedScreen(
                                    itemRepo = itemRepo,
                                    batchRepo = batchRepo,
                                    stockAdjRepo = stockAdjRepo,
                                    onDone = { navController.popBackStack() }
                                )
                            }
                            composable(Screen.InvoiceCreate.route) {
                                InvoiceCreateScreen(
                                    itemRepo = itemRepo,
                                    invoiceRepo = invoiceRepo,
                                    onCreated = { invId ->
                                        navController.navigate(Screen.InvoicePay.create(invId))
                                    }
                                )
                            }
                            composable(
                                route = Screen.InvoicePay.route,
                                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: return@composable
                                InvoicePayScreen(
                                    invoiceRepo = invoiceRepo,
                                    upiRepo = upiRepo,
                                    invoiceId = invoiceId,
                                    onDone = { navController.popBackStack() }
                                )
                            }
                            composable(Screen.Settings.route) {
                                SettingsScreen(
                                    settingsRepo = settingsRepo,
                                    onNavigateUpi = { navController.navigate(Screen.UpiAccounts.route) }
                                )
                            }
                            composable(Screen.UpiAccounts.route) {
                                UpiAccountsScreen(
                                    upiRepo = upiRepo,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateItems: () -> Unit,
    onNavigateGRN: () -> Unit,
    onNavigateInvoiceCreate: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Welcome", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onNavigateItems, modifier = Modifier.fillMaxWidth()) { Text("Items") }
        Button(onClick = onNavigateGRN, modifier = Modifier.fillMaxWidth()) { Text("Goods Received") }
        Button(onClick = onNavigateInvoiceCreate, modifier = Modifier.fillMaxWidth()) { Text("New Invoice") }
        Button(onClick = onNavigateSettings, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
    }
}