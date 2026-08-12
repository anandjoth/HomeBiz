package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["invoiceNo"], unique = true)]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNo: String,
    val date: Long = System.currentTimeMillis(),
    val customerId: Long? = null,
    val billingAddress: String? = null,
    val shippingAddress: String? = null,
    val status: String = "DRAFT", // DRAFT, PAID, PARTIAL, RETURNED
    val subtotalPaise: Long = 0,
    val discountTotalPaise: Long = 0,
    val taxTotalPaise: Long = 0,
    val grandTotalPaise: Long = 0,
    val balanceDuePaise: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)