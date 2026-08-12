package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val billingAddress: String? = null,
    val shippingAddress: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)