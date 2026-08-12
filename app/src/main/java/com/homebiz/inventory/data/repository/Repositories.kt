package com.yourbiz.inventory.data.repository

import com.yourbiz.inventory.data.dao.*
import com.yourbiz.inventory.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepository(private val dao: SettingsDao) {
    val settings: Flow<Settings?> = dao.getSettings()

    suspend fun initIfMissing() {
        if (dao.getSettings().first() == null) {
            dao.insert(Settings())
        }
    }

    suspend fun update(nextInvoiceNumber: Long? = null, businessName: String? = null) {
        val current = dao.getSettings().first() ?: Settings()
        dao.insert(
            current.copy(
                businessName = businessName ?: current.businessName,
                nextInvoiceNumber = nextInvoiceNumber ?: current.nextInvoiceNumber
            )
        )
    }

    suspend fun nextInvoiceNumber(): String {
        val s = dao.getSettings().first() ?: Settings()
        val next = "INV-" + s.nextInvoiceNumber.toString().padStart(6, '0')
        dao.incrementInvoiceNumber()
        return next
    }
}

class UpiAccountRepository(private val dao: UpiAccountDao) {
    val all: Flow<List<UpiAccount>> = dao.getAll()

    suspend fun insert(account: UpiAccount): Long {
        if (account.isDefault) {
            dao.clearDefault()
        }
        return dao.insert(account)
    }

    suspend fun setDefault(id: Long) {
        dao.clearDefault()
        dao.setAsDefault(id)
    }

    suspend fun delete(account: UpiAccount) {
        dao.delete(account)
    }

    suspend fun getById(id: Long): UpiAccount? = dao.getById(id)
}

class ItemRepository(private val dao: ItemDao) {
    val allActive: Flow<List<Item>> = dao.getAllActive()
    fun search(query: String): Flow<List<Item>> = dao.search("%$query%")

    suspend fun getById(id: Long): Item? = dao.getById(id)

    suspend fun insert(item: Item): Long = dao.insert(item)

    suspend fun update(item: Item) {
        dao.update(item.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(item: Item) {
        dao.delete(item)
    }
}

class BatchRepository(private val dao: BatchDao) {
    fun getByItem(itemId: Long): Flow<List<Batch>> = dao.getByItem(itemId)
    suspend fun getById(id: Long): Batch? = dao.getById(id)
    suspend fun insert(batch: Batch): Long = dao.insert(batch)
    suspend fun update(batch: Batch) = dao.update(batch)
    suspend fun adjustQty(id: Long, delta: Long) = dao.adjustQty(id, delta)
}

class StockAdjustmentRepository(
    private val adjDao: StockAdjustmentDao,
    private val batchDao: BatchDao
) {
    suspend fun goodsReceived(
        itemId: Long,
        qty: Long,
        costPaise: Long,
        batchNo: String? = null,
        mfgDate: Long? = null,
        expDate: Long? = null,
        notes: String? = null
    ) = withContext(Dispatchers.IO) {
        // Create adjustment header
        val adjId = adjDao.insert(
            StockAdjustment(type = "GRN", notes = notes)
        )
        // Create or reuse batch
        val existingBatches = batchDao.getByItem(itemId).first()
        val batch = existingBatches.find { it.batchNo == batchNo }
        val batchId = if (batch != null) {
            batchDao.update(
                batch.copy(
                    qtyOnHand = batch.qtyOnHand + qty,
                    lastIntakeCostPaise = costPaise
                )
            )
            batch.id
        } else {
            batchDao.insert(
                Batch(
                    itemId = itemId,
                    batchNo = batchNo,
                    mfgDate = mfgDate,
                    expDate = expDate,
                    qtyOnHand = qty,
                    lastIntakeCostPaise = costPaise
                )
            )
        }
        // Insert line
        adjDao.insertLines(
            listOf(
                StockAdjustmentLine(
                    adjustmentId = adjId,
                    itemId = itemId,
                    batchId = batchId,
                    qty = qty,
                    costPricePaise = costPaise
                )
            )
        )
        adjId
    }

    suspend fun saleReturn(
        itemId: Long,
        batchId: Long?,
        qty: Long,
        costPaise: Long,
        invoiceId: Long? = null,
        reason: String? = null
    ) = withContext(Dispatchers.IO) {
        val adjId = adjDao.insert(
            StockAdjustment(type = "SALE_RETURN", notes = reason)
        )
        adjDao.insertLines(
            listOf(
                StockAdjustmentLine(
                    adjustmentId = adjId,
                    itemId = itemId,
                    batchId = batchId,
                    qty = qty, // positive (in)
                    costPricePaise = costPaise,
                    reason = reason
                )
            )
        )
        if (batchId != null) {
            batchDao.adjustQty(batchId, qty)
        }
        adjId
    }
}

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val lineDao: InvoiceLineDao,
    private val paymentDao: PaymentDao,
    private val settingsRepo: SettingsRepository,
    private val batchDao: BatchDao
) {
    val all: Flow<List<Invoice>> = invoiceDao.getAll()

    suspend fun createInvoice(
        customerId: Long? = null,
        billingAddress: String? = null,
        shippingAddress: String? = null,
        lines: List<InvoiceLineInput>,
        notes: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val invoiceNo = settingsRepo.nextInvoiceNumber()
        val subtotal = lines.sumOf { it.qty * it.unitPricePaise }
        val discountTotal = lines.sumOf { it.discountPaise }
        val taxTotal = lines.sumOf { it.taxPaise }
        val grandTotal = subtotal - discountTotal + taxTotal

        val invoice = Invoice(
            invoiceNo = invoiceNo,
            customerId = customerId,
            billingAddress = billingAddress,
            shippingAddress = shippingAddress,
            status = "DRAFT",
            subtotalPaise = subtotal,
            discountTotalPaise = discountTotal,
            taxTotalPaise = taxTotal,
            grandTotalPaise = grandTotal,
            balanceDuePaise = grandTotal
        )
        val invId = invoiceDao.insert(invoice)

        val dbLines = lines.map { l ->
            InvoiceLine(
                invoiceId = invId,
                itemId = l.itemId,
                batchId = l.batchId,
                qty = l.qty,
                unitPricePaise = l.unitPricePaise,
                discountPaise = l.discountPaise,
                taxPaise = l.taxPaise,
                costPricePaise = l.costPricePaise,
                lineTotalPaise = l.qty * l.unitPricePaise - l.discountPaise + l.taxPaise
            )
        }
        lineDao.insertLines(dbLines)

        // For v1, we don't adjust batches here; production/GRN/returns do that.
        invId
    }

    suspend fun addPayment(
        invoiceId: Long,
        method: String,
        amountPaise: Long,
        upiAccountId: Long? = null,
        refNo: String? = null,
        notes: String? = null
    ) = withContext(Dispatchers.IO) {
        paymentDao.insert(
            Payment(
                invoiceId = invoiceId,
                method = method,
                amountPaise = amountPaise,
                upiAccountId = upiAccountId,
                refNo = refNo,
                notes = notes
            )
        )
        // Update balance
        val inv = invoiceDao.getById(invoiceId)!!
        val paid = paymentDao.getByInvoice(invoiceId).first().sumOf { it.amountPaise }
        val balance = inv.grandTotalPaise - paid
        invoiceDao.update(
            inv.copy(
                balanceDuePaise = balance,
                status = if (balance <= 0) "PAID" else if (paid > 0) "PARTIAL" else "DRAFT",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getById(id: Long): Invoice? = invoiceDao.getById(id)
    fun getLines(invoiceId: Long): Flow<List<InvoiceLine>> = lineDao.getByInvoice(invoiceId)
    fun getPayments(invoiceId: Long): Flow<List<Payment>> = paymentDao.getByInvoice(invoiceId)
}

data class InvoiceLineInput(
    val itemId: Long,
    val batchId: Long? = null,
    val qty: Long = 0,
    val unitPricePaise: Long = 0,
    val discountPaise: Long = 0,
    val taxPaise: Long = 0,
    val costPricePaise: Long = 0
)

class ProductionRepository(
    private val runDao: ProductionRunDao,
    private val batchDao: BatchDao
) {
    suspend fun createRun(
        finishedItemId: Long,
        finishedQty: Long,
        consumed: List<ConsumptionInput>,
        notes: String? = null
    ) = withContext(Dispatchers.IO) {
        val runId = runDao.insert(
            ProductionRun(
                finishedItemId = finishedItemId,
                finishedQty = finishedQty,
                notes = notes
            )
        )
        val consumptions = consumed.map { c ->
            ProductionConsumption(
                runId = runId,
                itemId = c.itemId,
                qty = c.qty,
                costPricePaise = c.costPaise
            )
        }
        runDao.insertConsumptions(consumptions)

        // Adjust batches: deduct raw, add finished
        for (c in consumed) {
            if (c.batchId != null) {
                batchDao.adjustQty(c.batchId, -c.qty)
            }
        }
        // Add finished goods to a default batch if needed (simplified)
        val finishedBatches = batchDao.getByItem(finishedItemId).first()
        if (finishedBatches.isEmpty()) {
            batchDao.insert(
                Batch(
                    itemId = finishedItemId,
                    batchNo = "MAIN",
                    qtyOnHand = finishedQty,
                    lastIntakeCostPaise = 0
                )
            )
        } else {
            val b = finishedBatches.first()
            batchDao.update(b.copy(qtyOnHand = b.qtyOnHand + finishedQty))
        }

        runId
    }
}

data class ConsumptionInput(
    val itemId: Long,
    val batchId: Long? = null,
    val qty: Long,
    val costPaise: Long
)