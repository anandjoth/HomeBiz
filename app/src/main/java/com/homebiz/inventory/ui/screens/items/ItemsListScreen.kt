package com.yourbiz.inventory.ui.screens.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yourbiz.inventory.data.model.Item
import com.yourbiz.inventory.data.repository.ItemRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsListScreen(
    itemRepo: ItemRepository,
    onAddItem: () -> Unit,
    onEditItem: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    val items by itemRepo.allActive.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }

    val filtered = if (query.isBlank()) {
        items
    } else {
        items.filter {
            it.name.contains(query, ignoreCase = true) || (it.sku?.contains(query, ignoreCase = true) == true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Items") },
                actions = {
                    IconButton(onClick = onAddItem) {
                        Icon(Icons.Default.Add, contentDescription = "Add item")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search by name or SKU") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, { it.id }) { item ->
                    ItemRow(item = item, onClick = { onEditItem(item.id) })
                }
            }
        }
    }
}

@Composable
private fun ItemRow(item: Item, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {}
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "₹${item.costPricePaise / 100.0} • MRP ${item.mrpPaise?.let { "₹${it / 100.0}" } ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${item.unit} • Raw: ${if (item.isRaw) "Yes" else "No"} | Finished: ${if (item.isFinished) "Yes" else "No"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}