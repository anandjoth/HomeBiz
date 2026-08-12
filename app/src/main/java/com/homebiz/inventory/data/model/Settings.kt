package com.yourbiz.inventory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "Your Business",
    val nextInvoiceNumber: Long = 1,
    val defaultCostFromLastIntake: Boolean = true,
    val backupEnabled: Boolean = true,
    val backupTimeHour: Int = 2, // 2 AM
    val driveFolderId: String? = null
)