package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_adjustments")
data class StockAdjustment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // GRN, SALE_RETURN, MANUAL, PRODUCTION_CONSUME, PRODUCTION_PRODUCE
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)