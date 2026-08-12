package com.yourbiz.inventory.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourbiz.inventory.data.model.UpiAccount
import com.yourbiz.inventory.data.repository.UpiAccountRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiAccountsScreen(
    upiRepo: UpiAccountRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val accounts by upiRepo.all.collectAsState(initial = emptyList())
    var name by remember { mutableStateOf("") }
    var vpa by remember { mutableStateOf("") }
    var payee by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UPI Accounts") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
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
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            OutlinedTextField(value = vpa, onValueChange = { vpa = it }, label = { Text("UPI ID (VPA)") })
            OutlinedTextField(value = payee, onValueChange = { payee = it }, label = { Text("Payee name") })
            Button(
                onClick = {
                    scope.launch {
                        upiRepo.insert(UpiAccount(name = name, vpa = vpa, payeeName = payee, isDefault = accounts.isEmpty()))
                        name = ""; vpa = ""; payee = ""
                    }
                },
                enabled = name.isNotBlank() && vpa.isNotBlank() && payee.isNotBlank()
            ) { Text("Add UPI Account") }

            accounts.forEach { acc ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(acc.name, style = MaterialTheme.typography.titleMedium)
                            Text("${acc.vpa} • ${acc.payeeName}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            if (!acc.isDefault) {
                                TextButton(onClick = { scope.launch { upiRepo.setDefault(acc.id) } }) { Text("Set default") }
                            } else {
                                Text("Default", color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(onClick = { scope.launch { upiRepo.delete(acc) } }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}