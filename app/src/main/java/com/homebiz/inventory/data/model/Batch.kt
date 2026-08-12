package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "batches",
    indices = [Index(value = ["itemId"])]
)
data class Batch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val batchNo: String? = null,
    val mfgDate: Long? = null,
    val expDate: Long? = null,
    val qtyOnHand: Long = 0,
    val lastIntakeCostPaise: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)