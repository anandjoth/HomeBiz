package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_lines",
    indices = [
        Index(value = ["invoiceId"]),
        Index(value = ["itemId"])
    ]
)
data class InvoiceLine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val itemId: Long,
    val batchId: Long? = null,
    val qty: Long = 0,
    val unitPricePaise: Long = 0,
    val discountPaise: Long = 0,
    val taxPaise: Long = 0,
    val costPricePaise: Long = 0, // exact cost used for COGS
    val lineTotalPaise: Long = 0
)