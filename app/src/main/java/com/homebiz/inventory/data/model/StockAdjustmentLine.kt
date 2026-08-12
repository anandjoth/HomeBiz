package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_adjustment_lines",
    indices = [
        Index(value = ["adjustmentId"]),
        Index(value = ["itemId"])
    ]
)
data class StockAdjustmentLine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val adjustmentId: Long,
    val itemId: Long,
    val batchId: Long? = null,
    val qty: Long, // positive for in, negative for out
    val costPricePaise: Long = 0,
    val reason: String? = null
)