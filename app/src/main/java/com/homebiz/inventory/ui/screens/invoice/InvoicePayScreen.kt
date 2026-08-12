package com.yourbiz.inventory.ui.screens.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourbiz.inventory.data.model.Invoice
import com.yourbiz.inventory.data.repository.InvoiceRepository
import com.yourbiz.inventory.data.repository.UpiAccountRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePayScreen(
    invoiceRepo: InvoiceRepository,
    upiRepo: UpiAccountRepository,
    invoiceId: Long,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var invoice by remember { mutableStateOf<Invoice?>(null) }
    LaunchedEffect(invoiceId) {
        invoice = invoiceRepo.getById(invoiceId)
    }
    val amount = (invoice?.grandTotalPaise ?: 0L) / 100.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay Invoice") },
                actions = {
                    TextButton(onClick = onDone) { Text("Done") }
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
            Text("Amount: ₹$amount")
            Text("UPI: dynamic QR (wire up with ZXing + UPI URI) – v1.1")
            Button(onClick = {
                scope.launch {
                    invoiceRepo.addPayment(
                        invoiceId = invoiceId,
                        method = "CASH",
                        amountPaise = invoice?.grandTotalPaise ?: 0L
                    )
                    onDone()
                }
            }) { Text("Mark as Cash Paid") }
        }
    }
}