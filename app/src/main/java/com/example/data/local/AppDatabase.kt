package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.GiftDao
import com.example.data.local.dao.GuestDao
import com.example.data.local.dao.MaterialDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.PersonDao
import com.example.data.local.dao.RitualDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.VendorDao
import com.example.data.local.dao.WeddingDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WeddingEntity::class,
        RitualEntity::class,
        RitualChecklistItemEntity::class,
        MaterialEntity::class,
        PersonEntity::class,
        VendorEntity::class,
        VendorQuotationEntity::class,
        ExpenseEntity::class,
        GuestEntity::class,
        TaskEntity::class,
        GiftEntity::class,
        NoteEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun weddingDao(): WeddingDao
    abstract fun ritualDao(): RitualDao
    abstract fun materialDao(): MaterialDao
    abstract fun personDao(): PersonDao
    abstract fun vendorDao(): VendorDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun guestDao(): GuestDao
    abstract fun taskDao(): TaskDao
    abstract fun giftDao(): GiftDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope? = null): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marwadi_wedding_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope?
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                (scope ?: CoroutineScope(Dispatchers.IO)).launch {
                    populateInitialData(database)
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database ->
                (scope ?: CoroutineScope(Dispatchers.IO)).launch {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: AppDatabase) {
    // Check if weddings exist
    val weddingDao = database.weddingDao()
    val ritualDao = database.ritualDao()
    val materialDao = database.materialDao()
    val expenseDao = database.expenseDao()
    val vendorDao = database.vendorDao()
    val personDao = database.personDao()
    val guestDao = database.guestDao()
    val taskDao = database.taskDao()
    val noteDao = database.noteDao()
    val giftDao = database.giftDao()

    val demoWedding = DefaultTemplates.createDemoWedding()
    val weddingId = weddingDao.insertWedding(demoWedding)

    // Populate the 26 Default Marwadi Rituals for this wedding
    DefaultTemplates.MARWADI_RITUALS.forEachIndexed { index, template ->
        val ritualId = ritualDao.insertRitual(
            RitualEntity(
                weddingId = weddingId,
                name = template.name,
                hindiName = template.hindiName,
                description = template.description,
                culturalSignificance = template.culturalSignificance,
                vidhiDetails = template.vidhiDetails,
                status = if (index < 3) "COMPLETED" else if (index == 3) "IN_PROGRESS" else "PENDING",
                priority = if (index in listOf(0, 5, 6, 13, 14, 15, 16, 21, 23)) "HIGH" else "MEDIUM",
                orderIndex = index + 1,
                date = demoWedding.weddingDate - ((26 - index) * 86400000L / 4)
            )
        )

        // Insert checklists for ritual
        val checklists = template.defaultChecklist.mapIndexed { cIndex, item ->
            RitualChecklistItemEntity(
                weddingId = weddingId,
                ritualId = ritualId,
                title = item,
                isCompleted = index < 3 || (index == 3 && cIndex < 2)
            )
        }
        ritualDao.insertChecklistItems(checklists)

        // Insert materials for ritual
        val materials = template.defaultMaterials.map { mat ->
            MaterialEntity(
                weddingId = weddingId,
                ritualId = ritualId,
                ritualName = template.name,
                item = mat.item,
                category = mat.category,
                requiredQuantity = mat.quantity,
                unit = mat.unit,
                estimatedCost = mat.estimatedCost,
                purchasedQuantity = if (index < 3) mat.quantity else if (index == 3) mat.quantity / 2 else 0.0,
                isPurchased = index < 3
            )
        }
        materialDao.insertMaterials(materials)
    }

    // Insert demo vendors, expenses, people, guests, tasks, notes, gifts
    expenseDao.insertExpenses(DefaultTemplates.createDemoExpenses(weddingId))
    vendorDao.insertVendors(DefaultTemplates.createDemoVendors(weddingId))
    personDao.insertPeople(DefaultTemplates.createDemoPeople(weddingId))
    guestDao.insertGuests(DefaultTemplates.createDemoGuests(weddingId))
    taskDao.insertTasks(DefaultTemplates.createDemoTasks(weddingId))
    noteDao.insertNotes(DefaultTemplates.createDemoNotes(weddingId))
    giftDao.insertGifts(DefaultTemplates.createDemoGifts(weddingId))
}
