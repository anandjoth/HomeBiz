package com.yourbiz.inventory.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourbiz.inventory.data.repository.SettingsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepo: SettingsRepository,
    onNavigateUpi: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val settings by settingsRepo.settings.collectAsState(initial = null)
    var businessName by remember { mutableStateOf(settings?.businessName ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business name") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    scope.launch {
                        settingsRepo.update(businessName = businessName)
                    }
                }
            ) { Text("Save") }

            Button(onClick = onNavigateUpi) { Text("UPI Accounts") }
        }
    }
}