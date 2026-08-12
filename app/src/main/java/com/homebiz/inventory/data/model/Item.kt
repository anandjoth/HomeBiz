package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    indices = [
        Index(value = ["sku"]),
        Index(value = ["categoryId"]),
        Index(value = ["isActive"])
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String? = null,
    val categoryId: Long? = null,
    val unit: String = "pcs",
    val costPricePaise: Long = 0,
    val mrpPaise: Long? = null,
    val trackBatch: Boolean = false,
    val trackExpiry: Boolean = false,
    val isRaw: Boolean = false,
    val isFinished: Boolean = false,
    val imagePath: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)