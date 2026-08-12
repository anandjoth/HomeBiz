package com.yourbiz.inventory.ui.screens.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourbiz.inventory.data.model.Item
import com.yourbiz.inventory.data.repository.InvoiceLineInput
import com.yourbiz.inventory.data.repository.InvoiceRepository
import com.yourbiz.inventory.data.repository.ItemRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreateScreen(
    itemRepo: ItemRepository,
    invoiceRepo: InvoiceRepository,
    onCreated: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    val items by itemRepo.allActive.collectAsState(initial = emptyList())
    var lines by remember { mutableStateOf<List<InvoiceLineInput>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Invoice") },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val id = invoiceRepo.createInvoice(lines = lines)
                                onCreated(id)
                            }
                        },
                        enabled = lines.isNotEmpty()
                    ) { Text("Save & Pay") }
                }
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
            Text("Add items (v1 skeleton – extend with line editor)")
            var qty by remember { mutableStateOf(1L) }
            var priceInr by remember { mutableStateOf("") }
            var costInr by remember { mutableStateOf("") }
            var selected by remember { mutableStateOf<Item?>(null) }

            ExposedDropdown(
                label = "Item",
                options = items,
                selected = selected,
                onSelected = { selected = it },
                displayText = { "${it.name} (${it.unit})" }
            )
            OutlinedTextField(value = qty.toString(), onValueChange = { qty = it.toLongOrNull() ?: 1L }, label = { Text("Qty") })
            OutlinedTextField(value = priceInr, onValueChange = { priceInr = it }, label = { Text("Price (₹)") })
            OutlinedTextField(value = costInr, onValueChange = { costInr = it }, label = { Text("Cost (₹)") })
            Button(
                onClick = {
                    if (selected != null) {
                        lines = lines + InvoiceLineInput(
                            itemId = selected!!.id,
                            qty = qty,
                            unitPricePaise = ((priceInr.toDoubleOrNull() ?: 0.0) * 100).toLong(),
                            costPricePaise = ((costInr.toDoubleOrNull() ?: 0.0) * 100).toLong()
                        )
                    }
                },
                enabled = selected != null
            ) { Text("Add line") }

            Text("Lines: ${lines.size}")
        }
    }
}

@Composable
private fun <T> ExposedDropdown(
    label: String,
    options: List<T>,
    selected: T?,
    onSelected: (T) -> Unit,
    displayText: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let(displayText) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(displayText(opt)) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}