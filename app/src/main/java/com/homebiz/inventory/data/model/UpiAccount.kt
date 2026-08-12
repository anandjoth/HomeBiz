package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upi_accounts")
data class UpiAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g., "Main", "Secondary"
    val vpa: String,  // UPI ID
    val payeeName: String,
    val isDefault: Boolean = false,
    val colorTag: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)