package com.yourbiz.inventory.ui.screens.grn

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourbiz.inventory.data.model.Item
import com.yourbiz.inventory.data.repository.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsReceivedScreen(
    itemRepo: ItemRepository,
    batchRepo: BatchRepository,
    stockAdjRepo: StockAdjustmentRepository,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val items by itemRepo.allActive.collectAsState(initial = emptyList())
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var qty by remember { mutableStateOf(1L) }
    var costInr by remember { mutableStateOf("") }
    var batchNo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goods Received") },
                actions = {
                    TextButton(
                        onClick = {
                            val costPaise = (costInr.toDoubleOrNull() ?: 0.0) * 100
                            scope.launch {
                                stockAdjRepo.goodsReceived(
                                    itemId = selectedItem?.id ?: return@launch,
                                    qty = qty,
                                    costPaise = costPaise.toLong(),
                                    batchNo = batchNo.ifBlank { null },
                                    notes = notes.ifBlank { null }
                                )
                                onDone()
                            }
                        },
                        enabled = selectedItem != null && qty > 0 && costInr.isNotBlank()
                    ) { Text("Save") }
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
            ExposedDropdown(
                label = "Item",
                options = items,
                selected = selectedItem,
                onSelected = { selectedItem = it },
                displayText = { "${it.name} (${it.unit})" }
            )
            OutlinedTextField(
                value = qty.toString(),
                onValueChange = { qty = it.toLongOrNull() ?: 1L },
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = costInr,
                onValueChange = { costInr = it },
                label = { Text("Cost per unit (₹)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = batchNo,
                onValueChange = { batchNo = it },
                label = { Text("Batch No (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
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