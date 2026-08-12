package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    indices = [Index(value = ["invoiceId"])]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val date: Long = System.currentTimeMillis(),
    val method: String, // UPI, CASH, ONLINE_TRANSFER
    val upiAccountId: Long? = null,
    val amountPaise: Long = 0,
    val refNo: String? = null,
    val notes: String? = null
)