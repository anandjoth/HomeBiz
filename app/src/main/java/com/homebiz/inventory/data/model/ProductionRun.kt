package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "production_runs")
data class ProductionRun(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val finishedItemId: Long,
    val finishedQty: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)