package com.yourbiz.inventory.ui.screens.items

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourbiz.inventory.data.model.Item
import com.yourbiz.inventory.data.repository.ItemRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormScreen(
    itemRepo: ItemRepository,
    itemId: Long,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var item by remember { mutableStateOf<Item?>(null) }
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }
    var costPaise by remember { mutableStateOf(0L) }
    var mrpPaise by remember { mutableStateOf<Long?>(null) }
    var isRaw by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var trackBatch by remember { mutableStateOf(false) }
    var trackExpiry by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        if (itemId > 0) {
            item = itemRepo.getById(itemId)
            item?.let {
                name = it.name
                sku = it.sku ?: ""
                unit = it.unit
                costPaise = it.costPricePaise
                mrpPaise = it.mrpPaise
                isRaw = it.isRaw
                isFinished = it.isFinished
                trackBatch = it.trackBatch
                trackExpiry = it.trackExpiry
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId > 0) "Edit Item" else "New Item") },
                actions = {
                    TextButton(onClick = {
                        scope.launch {
                            val toSave = (item ?: Item(name = name)).copy(
                                name = name,
                                sku = sku.ifBlank { null },
                                unit = unit,
                                costPricePaise = costPaise,
                                mrpPaise = mrpPaise,
                                isRaw = isRaw,
                                isFinished = isFinished,
                                trackBatch = trackBatch,
                                trackExpiry = trackExpiry
                            )
                            if (itemId > 0) itemRepo.update(toSave) else itemRepo.insert(toSave)
                            onSaved()
                        }
                    }) { Text("Save") }
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
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU (optional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (pcs, kg, box)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = (costPaise / 100.0).toString(),
                onValueChange = { costPaise = ((it.toDoubleOrNull() ?: 0.0) * 100).toLong() },
                label = { Text("Cost Price (₹)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = mrpPaise?.let { (it / 100.0).toString() } ?: "",
                onValueChange = { mrpPaise = if (it.isBlank()) null else ((it.toDoubleOrNull() ?: 0.0) * 100).toLong() },
                label = { Text("MRP (₹, optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CheckboxWithLabel("Raw", isRaw) { isRaw = it }
                CheckboxWithLabel("Finished", isFinished) { isFinished = it }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CheckboxWithLabel("Track batch", trackBatch) { trackBatch = it }
                CheckboxWithLabel("Track expiry", trackExpiry) { trackExpiry = it }
            }
        }
    }
}

@Composable
private fun CheckboxWithLabel(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text(text = label, modifier = Modifier.padding(top = 14.dp))
    }
}