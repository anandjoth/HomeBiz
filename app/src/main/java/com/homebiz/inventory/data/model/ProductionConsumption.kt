package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "production_consumption",
    indices = [Index(value = ["runId"])]
)
data class ProductionConsumption(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val runId: Long,
    val itemId: Long,
    val qty: Long = 0,
    val costPricePaise: Long = 0
)