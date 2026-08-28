package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.DefaultTemplates
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
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

class WeddingRepository(private val database: AppDatabase) {

    private val weddingDao = database.weddingDao()
    private val ritualDao = database.ritualDao()
    private val materialDao = database.materialDao()
    private val personDao = database.personDao()
    private val vendorDao = database.vendorDao()
    private val expenseDao = database.expenseDao()
    private val guestDao = database.guestDao()
    private val taskDao = database.taskDao()
    private val giftDao = database.giftDao()
    private val noteDao = database.noteDao()

    // Weddings
    val allActiveWeddings: Flow<List<WeddingEntity>> = weddingDao.getAllActiveWeddings()
    val allWeddings: Flow<List<WeddingEntity>> = weddingDao.getAllWeddings()

    fun getWeddingById(id: Long): Flow<WeddingEntity?> = weddingDao.getWeddingById(id)

    suspend fun createNewWedding(wedding: WeddingEntity, includeDefaultRituals: Boolean = true): Long {
        val weddingId = weddingDao.insertWedding(wedding)
        if (includeDefaultRituals) {
            populateDefaultRitualsForWedding(weddingId, wedding.weddingDate)
        }
        return weddingId
    }

    suspend fun updateWedding(wedding: WeddingEntity) {
        weddingDao.updateWedding(wedding)
    }

    suspend fun deleteWedding(wedding: WeddingEntity) {
        weddingDao.deleteWedding(wedding)
    }

    suspend fun duplicateWedding(sourceWeddingId: Long, newName: String): Long {
        val source = weddingDao.getWeddingByIdOnce(sourceWeddingId) ?: return -1
        val newWedding = source.copy(
            id = 0,
            name = newName,
            createdAt = System.currentTimeMillis()
        )
        val newWeddingId = weddingDao.insertWedding(newWedding)
        populateDefaultRitualsForWedding(newWeddingId, newWedding.weddingDate)
        return newWeddingId
    }

    private suspend fun populateDefaultRitualsForWedding(weddingId: Long, weddingDate: Long) {
        DefaultTemplates.MARWADI_RITUALS.forEachIndexed { index, template ->
            val ritualId = ritualDao.insertRitual(
                RitualEntity(
                    weddingId = weddingId,
                    name = template.name,
                    hindiName = template.hindiName,
                    description = template.description,
                    culturalSignificance = template.culturalSignificance,
                    vidhiDetails = template.vidhiDetails,
                    status = "PENDING",
                    priority = if (index in listOf(0, 5, 6, 13, 14, 15, 16, 21, 23)) "HIGH" else "MEDIUM",
                    orderIndex = index + 1,
                    date = weddingDate - ((26 - index) * 86400000L / 4)
                )
            )

            val checklists = template.defaultChecklist.map { item ->
                RitualChecklistItemEntity(
                    weddingId = weddingId,
                    ritualId = ritualId,
                    title = item,
                    isCompleted = false
                )
            }
            ritualDao.insertChecklistItems(checklists)

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
                    purchasedQuantity = 0.0,
                    isPurchased = false
                )
            }
            materialDao.insertMaterials(materials)
        }
    }

    // Rituals
    fun getRituals(weddingId: Long): Flow<List<RitualEntity>> = ritualDao.getRitualsForWedding(weddingId)
    fun getRitualById(id: Long): Flow<RitualEntity?> = ritualDao.getRitualById(id)
    suspend fun insertRitual(ritual: RitualEntity): Long = ritualDao.insertRitual(ritual)
    suspend fun updateRitual(ritual: RitualEntity) = ritualDao.updateRitual(ritual)
    suspend fun deleteRitual(ritual: RitualEntity) {
        ritualDao.deleteChecklistsForRitual(ritual.id)
        materialDao.deleteMaterialsForRitual(ritual.id)
        ritualDao.deleteRitual(ritual)
    }

    // Checklist
    fun getChecklistForRitual(ritualId: Long): Flow<List<RitualChecklistItemEntity>> = ritualDao.getChecklistForRitual(ritualId)
    fun getAllChecklistsForWedding(weddingId: Long): Flow<List<RitualChecklistItemEntity>> = ritualDao.getAllChecklistsForWedding(weddingId)
    suspend fun insertChecklistItem(item: RitualChecklistItemEntity): Long = ritualDao.insertChecklistItem(item)
    suspend fun updateChecklistItem(item: RitualChecklistItemEntity) = ritualDao.updateChecklistItem(item)
    suspend fun deleteChecklistItem(item: RitualChecklistItemEntity) = ritualDao.deleteChecklistItem(item)

    // Materials / Shopping / Inventory
    fun getMaterials(weddingId: Long): Flow<List<MaterialEntity>> = materialDao.getMaterialsForWedding(weddingId)
    fun getMaterialsForRitual(ritualId: Long): Flow<List<MaterialEntity>> = materialDao.getMaterialsForRitual(ritualId)
    suspend fun insertMaterial(material: MaterialEntity): Long = materialDao.insertMaterial(material)
    suspend fun updateMaterial(material: MaterialEntity) = materialDao.updateMaterial(material)
    suspend fun deleteMaterial(material: MaterialEntity) = materialDao.deleteMaterial(material)

    // Expenses
    fun getExpenses(weddingId: Long): Flow<List<ExpenseEntity>> = expenseDao.getExpensesForWedding(weddingId)
    suspend fun insertExpense(expense: ExpenseEntity): Long = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)

    // Vendors
    fun getVendors(weddingId: Long): Flow<List<VendorEntity>> = vendorDao.getVendorsForWedding(weddingId)
    fun getVendorById(id: Long): Flow<VendorEntity?> = vendorDao.getVendorById(id)
    suspend fun insertVendor(vendor: VendorEntity): Long = vendorDao.insertVendor(vendor)
    suspend fun updateVendor(vendor: VendorEntity) = vendorDao.updateVendor(vendor)
    suspend fun deleteVendor(vendor: VendorEntity) = vendorDao.deleteVendor(vendor)

    // Vendor Quotations
    fun getQuotations(weddingId: Long): Flow<List<VendorQuotationEntity>> = vendorDao.getQuotationsForWedding(weddingId)
    suspend fun insertQuotation(quote: VendorQuotationEntity): Long = vendorDao.insertQuotation(quote)
    suspend fun updateQuotation(quote: VendorQuotationEntity) = vendorDao.updateQuotation(quote)
    suspend fun deleteQuotation(quote: VendorQuotationEntity) = vendorDao.deleteQuotation(quote)

    // People / Family
    fun getPeople(weddingId: Long): Flow<List<PersonEntity>> = personDao.getPeopleForWedding(weddingId)
    suspend fun insertPerson(person: PersonEntity): Long = personDao.insertPerson(person)
    suspend fun updatePerson(person: PersonEntity) = personDao.updatePerson(person)
    suspend fun deletePerson(person: PersonEntity) = personDao.deletePerson(person)

    // Guests
    fun getGuests(weddingId: Long): Flow<List<GuestEntity>> = guestDao.getGuestsForWedding(weddingId)
    suspend fun insertGuest(guest: GuestEntity): Long = guestDao.insertGuest(guest)
    suspend fun updateGuest(guest: GuestEntity) = guestDao.updateGuest(guest)
    suspend fun deleteGuest(guest: GuestEntity) = guestDao.deleteGuest(guest)

    // Tasks
    fun getTasks(weddingId: Long): Flow<List<TaskEntity>> = taskDao.getTasksForWedding(weddingId)
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    // Gifts
    fun getGifts(weddingId: Long): Flow<List<GiftEntity>> = giftDao.getGiftsForWedding(weddingId)
    suspend fun insertGift(gift: GiftEntity): Long = giftDao.insertGift(gift)
    suspend fun updateGift(gift: GiftEntity) = giftDao.updateGift(gift)
    suspend fun deleteGift(gift: GiftEntity) = giftDao.deleteGift(gift)

    // Notes
    fun getNotes(weddingId: Long): Flow<List<NoteEntity>> = noteDao.getNotesForWedding(weddingId)
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    // Export / Backup JSON for a wedding
    suspend fun exportWeddingToJson(weddingId: Long): String {
        val wedding = weddingDao.getWeddingByIdOnce(weddingId) ?: return "{}"
        val rituals = ritualDao.getRitualsForWedding(weddingId).firstOrNull() ?: emptyList()
        val expenses = expenseDao.getExpensesForWedding(weddingId).firstOrNull() ?: emptyList()
        val vendors = vendorDao.getVendorsForWedding(weddingId).firstOrNull() ?: emptyList()
        val guests = guestDao.getGuestsForWedding(weddingId).firstOrNull() ?: emptyList()
        val tasks = taskDao.getTasksForWedding(weddingId).firstOrNull() ?: emptyList()
        val people = personDao.getPeopleForWedding(weddingId).firstOrNull() ?: emptyList()
        val materials = materialDao.getMaterialsForWedding(weddingId).firstOrNull() ?: emptyList()
        val gifts = giftDao.getGiftsForWedding(weddingId).firstOrNull() ?: emptyList()
        val notes = noteDao.getNotesForWedding(weddingId).firstOrNull() ?: emptyList()

        val root = JSONObject()
        val wObj = JSONObject().apply {
            put("name", wedding.name)
            put("brideName", wedding.brideName)
            put("groomName", wedding.groomName)
            put("weddingDate", wedding.weddingDate)
            put("venue", wedding.venue)
            put("city", wedding.city)
            put("familyName", wedding.familyName)
            put("overallBudget", wedding.overallBudget)
            put("notes", wedding.notes)
        }
        root.put("wedding", wObj)

        val expArr = JSONArray()
        expenses.forEach { exp ->
            expArr.put(JSONObject().apply {
                put("expenseName", exp.expenseName)
                put("amount", exp.amount)
                put("date", exp.date)
                put("category", exp.category)
                put("paidBy", exp.paidBy)
                put("paymentMode", exp.paymentMode)
            })
        }
        root.put("expenses", expArr)

        val gstArr = JSONArray()
        guests.forEach { g ->
            gstArr.put(JSONObject().apply {
                put("name", g.name)
                put("side", g.side)
                put("rsvpStatus", g.rsvpStatus)
                put("numberOfMembers", g.numberOfMembers)
                put("phone", g.phone)
            })
        }
        root.put("guests", gstArr)

        return root.toString(2)
    }

    suspend fun resetWithDemoData() {
        com.example.data.local.populateInitialData(database)
    }
}
