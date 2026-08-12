package com.yourbiz.inventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yourbiz.inventory.data.dao.*
import com.yourbiz.inventory.data.model.*

@Database(
    entities = [
        Settings::class,
        UpiAccount::class,
        Category::class,
        Item::class,
        Batch::class,
        StockAdjustment::class,
        StockAdjustmentLine::class,
        Customer::class,
        Invoice::class,
        InvoiceLine::class,
        Payment::class,
        ProductionRun::class,
        ProductionConsumption::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun upiAccountDao(): UpiAccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun itemDao(): ItemDao
    abstract fun batchDao(): BatchDao
    abstract fun stockAdjustmentDao(): StockAdjustmentDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceLineDao(): InvoiceLineDao
    abstract fun paymentDao(): PaymentDao
    abstract fun productionRunDao(): ProductionRunDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yourbiz_inventory_db"
                )
                    .fallbackToDestructiveMigration() // for early dev; change later
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}