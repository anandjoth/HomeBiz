package com.yourbiz.inventory.data.dao

import androidx.room.*
import com.yourbiz.inventory.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<Settings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: Settings)

    @Query("UPDATE settings SET nextInvoiceNumber = nextInvoiceNumber + 1 WHERE id = 1")
    suspend fun incrementInvoiceNumber()
}

@Dao
interface UpiAccountDao {
    @Query("SELECT * FROM upi_accounts ORDER BY isDefault DESC, createdAt DESC")
    fun getAll(): Flow<List<UpiAccount>>

    @Query("SELECT * FROM upi_accounts WHERE id = :id")
    suspend fun getById(id: Long): UpiAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: UpiAccount): Long

    @Delete
    suspend fun delete(account: UpiAccount)

    @Query("UPDATE upi_accounts SET isDefault = 0 WHERE isDefault = 1")
    suspend fun clearDefault()

    @Query("UPDATE upi_accounts SET isDefault = 1 WHERE id = :id")
    suspend fun setAsDefault(id: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    fun getAll(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Delete
    suspend fun delete(category: Category)
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE isActive = 1 ORDER BY name")
    fun getAllActive(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: Long): Item?

    @Query("SELECT * FROM items WHERE name LIKE :query OR sku LIKE :query")
    fun search(query: String): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item): Long

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)
}

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches WHERE itemId = :itemId ORDER BY createdAt")
    fun getByItem(itemId: Long): Flow<List<Batch>>

    @Query("SELECT * FROM batches WHERE id = :id")
    suspend fun getById(id: Long): Batch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batch: Batch): Long

    @Update
    suspend fun update(batch: Batch)

    @Query("UPDATE batches SET qtyOnHand = qtyOnHand + :delta WHERE id = :id")
    suspend fun adjustQty(id: Long, delta: Long)
}

@Dao
interface StockAdjustmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(adj: StockAdjustment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<StockAdjustmentLine>)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isActive = 1 ORDER BY name")
    fun getAllActive(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: Long): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: Invoice): Long

    @Update
    suspend fun update(invoice: Invoice)

    @Delete
    suspend fun delete(invoice: Invoice)
}

@Dao
interface InvoiceLineDao {
    @Query("SELECT * FROM invoice_lines WHERE invoiceId = :invoiceId")
    fun getByInvoice(invoiceId: Long): Flow<List<InvoiceLine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(line: InvoiceLine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<InvoiceLine>)

    @Query("DELETE FROM invoice_lines WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoice(invoiceId: Long)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY date")
    fun getByInvoice(invoiceId: Long): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long
}

@Dao
interface ProductionRunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: ProductionRun): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsumptions(list: List<ProductionConsumption>)
}