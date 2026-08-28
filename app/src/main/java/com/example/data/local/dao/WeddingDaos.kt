package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.GiftEntity
import com.example.data.local.entities.GuestEntity
import com.example.data.local.entities.MaterialEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.PersonEntity
import com.example.data.local.entities.RitualChecklistItemEntity
import com.example.data.local.entities.RitualEntity
import com.example.data.local.entities.TaskEntity
import com.example.data.local.entities.VendorEntity
import com.example.data.local.entities.VendorQuotationEntity
import com.example.data.local.entities.WeddingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeddingDao {
    @Query("SELECT * FROM weddings WHERE isArchived = 0 ORDER BY weddingDate ASC")
    fun getAllActiveWeddings(): Flow<List<WeddingEntity>>

    @Query("SELECT * FROM weddings WHERE id = :id LIMIT 1")
    fun getWeddingById(id: Long): Flow<WeddingEntity?>

    @Query("SELECT * FROM weddings WHERE id = :id LIMIT 1")
    suspend fun getWeddingByIdOnce(id: Long): WeddingEntity?

    @Query("SELECT * FROM weddings ORDER BY weddingDate ASC")
    fun getAllWeddings(): Flow<List<WeddingEntity>>

    @Query("SELECT * FROM weddings ORDER BY weddingDate ASC")
    suspend fun getAllWeddingsOnce(): List<WeddingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWedding(wedding: WeddingEntity): Long

    @Update
    suspend fun updateWedding(wedding: WeddingEntity)

    @Delete
    suspend fun deleteWedding(wedding: WeddingEntity)

    @Query("DELETE FROM weddings WHERE id = :id")
    suspend fun deleteWeddingById(id: Long)
}

@Dao
interface RitualDao {
    @Query("SELECT * FROM rituals WHERE weddingId = :weddingId ORDER BY orderIndex ASC, date ASC")
    fun getRitualsForWedding(weddingId: Long): Flow<List<RitualEntity>>

    @Query("SELECT * FROM rituals WHERE weddingId = :weddingId ORDER BY orderIndex ASC, date ASC")
    suspend fun getRitualsForWeddingOnce(weddingId: Long): List<RitualEntity>

    @Query("SELECT * FROM rituals WHERE id = :id LIMIT 1")
    fun getRitualById(id: Long): Flow<RitualEntity?>

    @Query("SELECT * FROM rituals WHERE id = :id LIMIT 1")
    suspend fun getRitualByIdOnce(id: Long): RitualEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRitual(ritual: RitualEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRituals(rituals: List<RitualEntity>): List<Long>

    @Update
    suspend fun updateRitual(ritual: RitualEntity)

    @Delete
    suspend fun deleteRitual(ritual: RitualEntity)

    @Query("DELETE FROM rituals WHERE weddingId = :weddingId")
    suspend fun deleteAllRitualsForWedding(weddingId: Long)

    // Ritual Checklists
    @Query("SELECT * FROM ritual_checklists WHERE ritualId = :ritualId ORDER BY id ASC")
    fun getChecklistForRitual(ritualId: Long): Flow<List<RitualChecklistItemEntity>>

    @Query("SELECT * FROM ritual_checklists WHERE weddingId = :weddingId")
    fun getAllChecklistsForWedding(weddingId: Long): Flow<List<RitualChecklistItemEntity>>

    @Query("SELECT * FROM ritual_checklists WHERE weddingId = :weddingId")
    suspend fun getAllChecklistsForWeddingOnce(weddingId: Long): List<RitualChecklistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: RitualChecklistItemEntity): Long


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<RitualChecklistItemEntity>)

    @Update
    suspend fun updateChecklistItem(item: RitualChecklistItemEntity)

    @Delete
    suspend fun deleteChecklistItem(item: RitualChecklistItemEntity)

    @Query("DELETE FROM ritual_checklists WHERE ritualId = :ritualId")
    suspend fun deleteChecklistsForRitual(ritualId: Long)
}

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials WHERE weddingId = :weddingId ORDER BY category ASC, item ASC")
    fun getMaterialsForWedding(weddingId: Long): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE weddingId = :weddingId ORDER BY category ASC, item ASC")
    suspend fun getMaterialsForWeddingOnce(weddingId: Long): List<MaterialEntity>

    @Query("SELECT * FROM materials WHERE ritualId = :ritualId ORDER BY item ASC")
    fun getMaterialsForRitual(ritualId: Long): Flow<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<MaterialEntity>)

    @Update
    suspend fun updateMaterial(material: MaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: MaterialEntity)

    @Query("DELETE FROM materials WHERE ritualId = :ritualId")
    suspend fun deleteMaterialsForRitual(ritualId: Long)

    @Query("DELETE FROM materials WHERE weddingId = :weddingId")
    suspend fun deleteAllMaterialsForWedding(weddingId: Long)
}

@Dao
interface PersonDao {
    @Query("SELECT * FROM people WHERE weddingId = :weddingId ORDER BY name ASC")
    fun getPeopleForWedding(weddingId: Long): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE weddingId = :weddingId ORDER BY name ASC")
    suspend fun getPeopleForWeddingOnce(weddingId: Long): List<PersonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeople(people: List<PersonEntity>)

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Delete
    suspend fun deletePerson(person: PersonEntity)
}

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors WHERE weddingId = :weddingId ORDER BY serviceType ASC, name ASC")
    fun getVendorsForWedding(weddingId: Long): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE weddingId = :weddingId ORDER BY serviceType ASC, name ASC")
    suspend fun getVendorsForWeddingOnce(weddingId: Long): List<VendorEntity>

    @Query("SELECT * FROM vendors WHERE id = :id LIMIT 1")
    fun getVendorById(id: Long): Flow<VendorEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: VendorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendors(vendors: List<VendorEntity>)

    @Update
    suspend fun updateVendor(vendor: VendorEntity)

    @Delete
    suspend fun deleteVendor(vendor: VendorEntity)

    // Vendor Quotations
    @Query("SELECT * FROM vendor_quotations WHERE weddingId = :weddingId ORDER BY serviceType ASC, quoteAmount ASC")
    fun getQuotationsForWedding(weddingId: Long): Flow<List<VendorQuotationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quote: VendorQuotationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotations(quotes: List<VendorQuotationEntity>)

    @Update
    suspend fun updateQuotation(quote: VendorQuotationEntity)

    @Delete
    suspend fun deleteQuotation(quote: VendorQuotationEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE weddingId = :weddingId ORDER BY date DESC")
    fun getExpensesForWedding(weddingId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE weddingId = :weddingId ORDER BY date DESC")
    suspend fun getExpensesForWeddingOnce(weddingId: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE weddingId = :weddingId AND ritualId = :ritualId ORDER BY date DESC")
    fun getExpensesForRitual(weddingId: Long, ritualId: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE weddingId = :weddingId")
    suspend fun deleteAllExpensesForWedding(weddingId: Long)
}

@Dao
interface GuestDao {
    @Query("SELECT * FROM guests WHERE weddingId = :weddingId ORDER BY name ASC")
    fun getGuestsForWedding(weddingId: Long): Flow<List<GuestEntity>>

    @Query("SELECT * FROM guests WHERE weddingId = :weddingId ORDER BY name ASC")
    suspend fun getGuestsForWeddingOnce(weddingId: Long): List<GuestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuest(guest: GuestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuests(guests: List<GuestEntity>)

    @Update
    suspend fun updateGuest(guest: GuestEntity)

    @Delete
    suspend fun deleteGuest(guest: GuestEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE weddingId = :weddingId ORDER BY dueDate ASC, priority DESC")
    fun getTasksForWedding(weddingId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE weddingId = :weddingId ORDER BY dueDate ASC, priority DESC")
    suspend fun getTasksForWeddingOnce(weddingId: Long): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}

@Dao
interface GiftDao {
    @Query("SELECT * FROM gifts WHERE weddingId = :weddingId ORDER BY dateReceived DESC")
    fun getGiftsForWedding(weddingId: Long): Flow<List<GiftEntity>>

    @Query("SELECT * FROM gifts WHERE weddingId = :weddingId ORDER BY dateReceived DESC")
    suspend fun getGiftsForWeddingOnce(weddingId: Long): List<GiftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGift(gift: GiftEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGifts(gifts: List<GiftEntity>)

    @Update
    suspend fun updateGift(gift: GiftEntity)

    @Delete
    suspend fun deleteGift(gift: GiftEntity)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE weddingId = :weddingId ORDER BY timestamp DESC")
    fun getNotesForWedding(weddingId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE weddingId = :weddingId ORDER BY timestamp DESC")
    suspend fun getNotesForWeddingOnce(weddingId: Long): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}
