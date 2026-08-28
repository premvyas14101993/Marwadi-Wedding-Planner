package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.cloud.AuthState
import com.example.data.cloud.FirebaseAuthManager
import com.example.data.cloud.SyncStats
import com.example.data.cloud.SyncStatus
import com.example.data.cloud.WeddingFirestoreSyncManager
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
import com.example.data.repository.WeddingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Consolidated Shopping Item across multiple rituals
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

data class ConsolidatedShoppingItem(
    val item: String,
    val category: String,
    val totalRequired: Double,
    val totalPurchased: Double,
    val remaining: Double,
    val unit: String,
    val estimatedTotalCost: Double,
    val ritualBreakdowns: List<String>,
    val isFullyPurchased: Boolean
)

// Combined Calendar Event
data class CalendarEventItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val date: Long,
    val type: String, // "RITUAL", "TASK", "VENDOR_PAYMENT"
    val status: String,
    val priority: String = "MEDIUM"
)

// Global Search Result Item
data class SearchMatchItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val category: String,
    val entityType: String
)

@OptIn(ExperimentalCoroutinesApi::class)
class WeddingViewModel(
    private val repository: WeddingRepository,
    private val authManager: FirebaseAuthManager? = null,
    private val syncManager: WeddingFirestoreSyncManager? = null
) : ViewModel() {

    val authState: StateFlow<AuthState> = authManager?.authState
        ?: MutableStateFlow(AuthState.Unauthenticated())

    val syncStatus: StateFlow<SyncStatus> = syncManager?.syncStatus
        ?: MutableStateFlow(SyncStatus.Idle)

    val syncStats: StateFlow<SyncStats> = syncManager?.syncStats
        ?: MutableStateFlow(SyncStats())

    val isLiveSyncActive: StateFlow<Boolean> = syncManager?.isLiveSyncActive
        ?: MutableStateFlow(false)

    val allWeddings: StateFlow<List<WeddingEntity>> = repository.allActiveWeddings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedWeddingId = MutableStateFlow<Long?>(null)
    val selectedWeddingId: StateFlow<Long?> = _selectedWeddingId.asStateFlow()

    val activeInviteCode: StateFlow<String?> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else flowOf(syncManager?.getInviteCodeForWedding(id))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Auto-select first wedding if available
        viewModelScope.launch {
            allWeddings.collect { list ->
                if (_selectedWeddingId.value == null && list.isNotEmpty()) {
                    _selectedWeddingId.value = list.first().id
                }
            }
        }
    }

    fun selectWedding(weddingId: Long) {
        _selectedWeddingId.value = weddingId
    }

    val currentWedding: StateFlow<WeddingEntity?> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getWeddingById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Rituals
    val rituals: StateFlow<List<RitualEntity>> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getRituals(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Checklists
    val allChecklists: StateFlow<List<RitualChecklistItemEntity>> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getAllChecklistsForWedding(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getChecklistForRitual(ritualId: Long): Flow<List<RitualChecklistItemEntity>> {
        return repository.getChecklistForRitual(ritualId)
    }

    // Materials
    val materials: StateFlow<List<MaterialEntity>> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMaterials(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Consolidated Shopping Planner
    val consolidatedShopping: StateFlow<List<ConsolidatedShoppingItem>> = materials
        .map { matList ->
            matList.groupBy { it.item.trim().lowercase() }
                .map { (_, group) ->
                    val first = group.first()
                    val totalReq = group.sumOf { it.requiredQuantity }
                    val totalPur = group.sumOf { it.purchasedQuantity }
                    val remaining = maxOf(0.0, totalReq - totalPur)
                    val estCost = group.sumOf { it.estimatedCost }
                    val breakdowns = group.mapNotNull {
                        if (it.ritualName != null) "${it.ritualName}: ${it.requiredQuantity} ${it.unit}" else null
                    }
                    ConsolidatedShoppingItem(
                        item = first.item,
                        category = first.category,
                        totalRequired = totalReq,
                        totalPurchased = totalPur,
                        remaining = remaining,
                        unit = first.unit,
                        estimatedTotalCost = estCost,
                        ritualBreakdowns = breakdowns,
                        isFullyPurchased = remaining <= 0.0 && totalReq > 0.0
                    )
                }
                .sortedBy { it.isFullyPurchased }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expenses
    val expenses: StateFlow<List<ExpenseEntity>> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getExpenses(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Analytics
    val totalExpense: StateFlow<Double> = expenses
        .map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expenseByCategory: StateFlow<Map<String, Double>> = expenses
        .map { list ->
            list.groupBy { it.category }
                .mapValues { (_, items) -> items.sumOf { it.amount } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val expenseByRitual: StateFlow<Map<String, Double>> = expenses
        .map { list ->
            list.filter { it.ritualName != null && it.ritualName.isNotEmpty() }
                .groupBy { it.ritualName!! }
                .mapValues { (_, items) -> items.sumOf { it.amount } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val expenseByPerson: StateFlow<Map<String, Double>> = expenses
        .map { list ->
            list.groupBy { it.paidBy }
                .mapValues { (_, items) -> items.sumOf { it.amount } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val expenseByPaymentMode: StateFlow<Map<String, Double>> = expenses
        .map { list ->
            list.groupBy { it.paymentMode }
                .mapValues { (_, items) -> items.sumOf { it.amount } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Vendors & Quotations
    val vendors: StateFlow<List<VendorEntity>> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getVendors(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendorQuotations: StateFlow<List<VendorQuotationEntity>> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getQuotations(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // People / Family
    val people: StateFlow<List<PersonEntity>> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getPeople(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Guests
    val guests: StateFlow<List<GuestEntity>> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getGuests(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasks
    val tasks: StateFlow<List<TaskEntity>> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getTasks(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gifts
    val gifts: StateFlow<List<GiftEntity>> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getGifts(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notes
    val notes: StateFlow<List<NoteEntity>> = _selectedWeddingId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getNotes(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined Calendar Events
    val calendarEvents: StateFlow<List<CalendarEventItem>> = combine(
        rituals,
        tasks,
        vendors
    ) { rList, tList, vList ->
        val events = mutableListOf<CalendarEventItem>()
        rList.forEach { r ->
            if (r.date != null) {
                events.add(
                    CalendarEventItem(
                        id = "R_${r.id}",
                        title = r.name,
                        subtitle = r.hindiName.ifEmpty { r.venue ?: "Ritual" },
                        date = r.date,
                        type = "RITUAL",
                        status = r.status,
                        priority = r.priority
                    )
                )
            }
        }
        tList.forEach { t ->
            events.add(
                CalendarEventItem(
                    id = "T_${t.id}",
                    title = t.taskName,
                    subtitle = "Assigned: ${t.assignedTo}",
                    date = t.dueDate,
                    type = "TASK",
                    status = t.status,
                    priority = t.priority
                )
            )
        }
        vList.forEach { v ->
            if (v.dueDate != null && (v.totalContractValue - v.advancePaid) > 0) {
                events.add(
                    CalendarEventItem(
                        id = "V_${v.id}",
                        title = "Payment Due: ${v.name}",
                        subtitle = "Pending: ₹${(v.totalContractValue - v.advancePaid).toInt()}",
                        date = v.dueDate,
                        type = "VENDOR_PAYMENT",
                        status = "PENDING",
                        priority = "HIGH"
                    )
                )
            }
        }
        events.sortedBy { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search Query State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val searchResults: StateFlow<List<SearchMatchItem>> = combine(
        _searchQuery,
        combine(rituals, expenses, guests, vendors) { r, e, g, v -> Quadruple(r, e, g, v) },
        combine(tasks, materials, notes) { t, m, n -> Triple(t, m, n) }
    ) { query, quad, trip ->
        if (query.isBlank()) return@combine emptyList<SearchMatchItem>()
        val q = query.trim().lowercase()
        val results = mutableListOf<SearchMatchItem>()
        val (rList, eList, gList, vList) = quad
        val (tList, mList, nList) = trip

        rList.filter { it.name.lowercase().contains(q) || it.hindiName.lowercase().contains(q) || it.description.lowercase().contains(q) }
            .forEach { results.add(SearchMatchItem(it.id, it.name, it.hindiName, "Rituals", "RITUAL")) }

        eList.filter { it.expenseName.lowercase().contains(q) || it.category.lowercase().contains(q) || it.paidBy.lowercase().contains(q) }
            .forEach { results.add(SearchMatchItem(it.id, it.expenseName, "₹${it.amount.toInt()} • Paid by: ${it.paidBy}", "Expenses", "EXPENSE")) }

        gList.filter { it.name.lowercase().contains(q) || it.city.lowercase().contains(q) || it.familyName.lowercase().contains(q) }
            .forEach { results.add(SearchMatchItem(it.id, it.name, "${it.side.replace("_", " ")} • ${it.rsvpStatus}", "Guests", "GUEST")) }

        vList.filter { it.name.lowercase().contains(q) || it.serviceType.lowercase().contains(q) }
            .forEach { results.add(SearchMatchItem(it.id, it.name, "${it.serviceType} • Pending: ₹${(it.totalContractValue - it.advancePaid).toInt()}", "Vendors", "VENDOR")) }

        tList.filter { it.taskName.lowercase().contains(q) || it.assignedTo.lowercase().contains(q) }
            .forEach { results.add(SearchMatchItem(it.id, it.taskName, "Assigned to: ${it.assignedTo} • ${it.status}", "Tasks", "TASK")) }

        mList.filter { it.item.lowercase().contains(q) || it.category.lowercase().contains(q) }
            .forEach { results.add(SearchMatchItem(it.id, it.item, "${it.category} • Required: ${it.requiredQuantity} ${it.unit}", "Materials", "MATERIAL")) }

        nList.filter { it.title.lowercase().contains(q) || it.content.lowercase().contains(q) }
            .forEach { results.add(SearchMatchItem(it.id, it.title, it.category, "Notes", "NOTE")) }

        results
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Action Functions ---

    // Wedding Actions
    fun createWedding(
        name: String,
        brideName: String,
        groomName: String,
        weddingDate: Long,
        engagementDate: Long?,
        venue: String,
        city: String,
        familyName: String,
        overallBudget: Double,
        notes: String,
        onCreated: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val user = authManager?.currentUser
            val currentUid = user?.uid ?: ""
            val currentName = user?.displayName ?: user?.email ?: "Project Admin"
            val entity = WeddingEntity(
                name = name,
                brideName = brideName,
                groomName = groomName,
                weddingDate = weddingDate,
                engagementDate = engagementDate,
                venue = venue,
                city = city,
                familyName = familyName,
                overallBudget = overallBudget,
                notes = notes,
                creatorUid = currentUid,
                creatorName = currentName
            )
            val newId = repository.createNewWedding(entity, includeDefaultRituals = true)
            _selectedWeddingId.value = newId
            onCreated?.invoke(newId)
        }
    }

    fun updateWedding(wedding: WeddingEntity) {
        viewModelScope.launch { repository.updateWedding(wedding) }
    }

    fun deleteWedding(wedding: WeddingEntity) {
        viewModelScope.launch {
            repository.deleteWedding(wedding)
            if (_selectedWeddingId.value == wedding.id) {
                val remaining = allWeddings.value.filter { it.id != wedding.id }
                _selectedWeddingId.value = remaining.firstOrNull()?.id
            }
        }
    }

    fun duplicateWedding(sourceId: Long, newName: String) {
        viewModelScope.launch {
            val newId = repository.duplicateWedding(sourceId, newName)
            if (newId > 0) _selectedWeddingId.value = newId
        }
    }

    // Ritual Actions
    fun addRitual(ritual: RitualEntity) {
        viewModelScope.launch { repository.insertRitual(ritual) }
    }

    fun updateRitual(ritual: RitualEntity) {
        viewModelScope.launch { repository.updateRitual(ritual) }
    }

    fun deleteRitual(ritual: RitualEntity) {
        viewModelScope.launch { repository.deleteRitual(ritual) }
    }

    // Checklist Actions
    fun toggleChecklistItem(item: RitualChecklistItemEntity) {
        viewModelScope.launch { repository.updateChecklistItem(item.copy(isCompleted = !item.isCompleted)) }
    }

    fun addChecklistItem(item: RitualChecklistItemEntity) {
        viewModelScope.launch { repository.insertChecklistItem(item) }
    }

    fun deleteChecklistItem(item: RitualChecklistItemEntity) {
        viewModelScope.launch { repository.deleteChecklistItem(item) }
    }

    // Material Actions
    fun addMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.insertMaterial(material) }
    }

    fun updateMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.updateMaterial(material) }
    }

    fun toggleMaterialPurchased(material: MaterialEntity) {
        viewModelScope.launch {
            val newPurchased = !material.isPurchased
            val newQty = if (newPurchased) material.requiredQuantity else 0.0
            repository.updateMaterial(material.copy(isPurchased = newPurchased, purchasedQuantity = newQty))
        }
    }

    fun deleteMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.deleteMaterial(material) }
    }

    // Expense Actions with Author and Admin Deletion Validation
    fun canDeleteExpense(expense: ExpenseEntity): Boolean {
        val user = authManager?.currentUser
        val currentUid = user?.uid ?: ""
        val currentName = user?.displayName ?: user?.email ?: ""
        val currentWeddingObj = currentWedding.value

        // 1. If project was created locally with empty creatorUid and no cloud sync, allow creator/user
        if (currentWeddingObj == null) return true

        // 2. Check if current user is the Project Admin (creator of the wedding)
        val isProjectAdmin = when {
            currentWeddingObj.creatorUid.isNotBlank() && currentUid.isNotBlank() && currentWeddingObj.creatorUid == currentUid -> true
            currentWeddingObj.creatorName.isNotBlank() && currentName.isNotBlank() && currentWeddingObj.creatorName.equals(currentName, ignoreCase = true) -> true
            // If the user created the project on this local device without signing in
            currentWeddingObj.creatorUid.isBlank() && currentUid.isBlank() -> true
            else -> false
        }
        if (isProjectAdmin) return true

        // 3. Check if current user is the one who added this expense
        val isExpenseAuthor = when {
            expense.addedByUid.isNotBlank() && currentUid.isNotBlank() && expense.addedByUid == currentUid -> true
            expense.addedByName.isNotBlank() && currentName.isNotBlank() && expense.addedByName.equals(currentName, ignoreCase = true) -> true
            expense.addedByName.isNotBlank() && currentName.isNotBlank() && expense.paidBy.equals(currentName, ignoreCase = true) -> true
            // Legacy expenses with no creator tracking
            expense.addedByUid.isBlank() && expense.addedByName.isBlank() -> true
            else -> false
        }

        return isExpenseAuthor
    }

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            val user = authManager?.currentUser
            val currentUid = user?.uid ?: ""
            val currentName = user?.displayName ?: user?.email ?: expense.paidBy
            val entityWithAuthor = if (expense.addedByUid.isBlank() && expense.addedByName.isBlank()) {
                expense.copy(
                    addedByUid = currentUid,
                    addedByName = if (currentName.isNotBlank()) currentName else "Family Member"
                )
            } else {
                expense
            }
            repository.insertExpense(entityWithAuthor)
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.updateExpense(expense) }
    }

    fun deleteExpense(expense: ExpenseEntity, onError: ((String) -> Unit)? = null) {
        if (!canDeleteExpense(expense)) {
            val author = expense.addedByName.ifBlank { expense.paidBy }
            val admin = currentWedding.value?.creatorName?.ifBlank { "Project Admin" } ?: "Project Admin"
            onError?.invoke("Permission Denied: Only '$author' (who added this expense) or '$admin' (Project Admin) can delete this expense.")
            return
        }
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    // Vendor Actions
    fun addVendor(vendor: VendorEntity) {
        viewModelScope.launch { repository.insertVendor(vendor) }
    }

    fun updateVendor(vendor: VendorEntity) {
        viewModelScope.launch { repository.updateVendor(vendor) }
    }

    fun deleteVendor(vendor: VendorEntity) {
        viewModelScope.launch { repository.deleteVendor(vendor) }
    }

    // Vendor Quotations Actions
    fun addQuotation(quote: VendorQuotationEntity) {
        viewModelScope.launch { repository.insertQuotation(quote) }
    }

    fun selectQuotation(quote: VendorQuotationEntity) {
        viewModelScope.launch {
            // Select this and deselect other quotes for same service
            repository.updateQuotation(quote.copy(isSelected = true))
        }
    }

    fun deleteQuotation(quote: VendorQuotationEntity) {
        viewModelScope.launch { repository.deleteQuotation(quote) }
    }

    // People / Family Actions
    fun addPerson(person: PersonEntity) {
        viewModelScope.launch { repository.insertPerson(person) }
    }

    fun updatePerson(person: PersonEntity) {
        viewModelScope.launch { repository.updatePerson(person) }
    }

    fun deletePerson(person: PersonEntity) {
        viewModelScope.launch { repository.deletePerson(person) }
    }

    // Guest Actions
    fun addGuest(guest: GuestEntity) {
        viewModelScope.launch { repository.insertGuest(guest) }
    }

    fun updateGuest(guest: GuestEntity) {
        viewModelScope.launch { repository.updateGuest(guest) }
    }

    fun deleteGuest(guest: GuestEntity) {
        viewModelScope.launch { repository.deleteGuest(guest) }
    }

    // Task Actions
    fun addTask(task: TaskEntity) {
        viewModelScope.launch { repository.insertTask(task) }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun toggleTaskStatus(task: TaskEntity) {
        viewModelScope.launch {
            val nextStatus = when (task.status) {
                "PENDING" -> "IN_PROGRESS"
                "IN_PROGRESS" -> "COMPLETED"
                else -> "PENDING"
            }
            repository.updateTask(task.copy(status = nextStatus))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    // Gift Actions
    fun addGift(gift: GiftEntity) {
        viewModelScope.launch { repository.insertGift(gift) }
    }

    fun updateGift(gift: GiftEntity) {
        viewModelScope.launch { repository.updateGift(gift) }
    }

    fun deleteGift(gift: GiftEntity) {
        viewModelScope.launch { repository.deleteGift(gift) }
    }

    // Note Actions
    fun addNote(note: NoteEntity) {
        viewModelScope.launch { repository.insertNote(note) }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch { repository.updateNote(note) }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun resetToDemoData(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.resetWithDemoData()
            onComplete?.invoke()
        }
    }

    // Cloud Authentication & Sync Actions
    fun signInWithGoogleAccount(email: String, displayName: String? = null, onComplete: (Boolean) -> Unit = {}) {
        val manager = authManager ?: run {
            onComplete(false)
            return
        }
        val user = manager.signInWithGoogleAccount(email, displayName)
        currentWedding.value?.let { wedding ->
            viewModelScope.launch {
                syncManager?.getOrCreateInviteCode(wedding)
            }
        }
        onComplete(true)
    }

    fun signInWithGoogle(webClientId: String? = null, onComplete: (Boolean, String?) -> Unit) {
        val manager = authManager ?: run {
            onComplete(false, "Authentication manager not initialized")
            return
        }
        viewModelScope.launch {
            val result = manager.signInWithGoogle(webClientId)
            result.fold(
                onSuccess = { user ->
                    // Auto-sync current wedding when signed in
                    currentWedding.value?.let { wedding ->
                        syncManager?.getOrCreateInviteCode(wedding)
                    }
                    onComplete(true, null)
                },
                onFailure = { error ->
                    onComplete(false, error.message ?: "Sign-in failed")
                }
            )
        }
    }

    fun signInAsFamilyMember(displayName: String, email: String? = null, onComplete: (Boolean) -> Unit) {
        val manager = authManager ?: run {
            onComplete(false)
            return
        }
        val user = manager.signInAsFamilyMember(displayName, email)
        currentWedding.value?.let { wedding ->
            viewModelScope.launch {
                syncManager?.getOrCreateInviteCode(wedding)
            }
        }
        onComplete(true)
    }

    fun signOut() {
        authManager?.signOut()
    }

    fun syncNow() {
        val wedding = currentWedding.value ?: return
        viewModelScope.launch {
            syncManager?.pushAllLocalDataToFirestore(wedding.id)
        }
    }

    fun getInviteCodeForWedding(weddingId: Long): String? {
        return syncManager?.getInviteCodeForWedding(weddingId)
    }

    fun generateOrFetchInviteCode(onCodeReady: (String) -> Unit) {
        val wedding = currentWedding.value ?: return
        viewModelScope.launch {
            val code = syncManager?.getOrCreateInviteCode(wedding) ?: "MW-849201"
            onCodeReady(code)
        }
    }

    fun joinWeddingWithInviteCode(code: String, onResult: (Boolean, String) -> Unit) {
        val manager = syncManager ?: run {
            onResult(false, "Sync manager not available")
            return
        }
        viewModelScope.launch {
            val result = manager.joinWeddingByInviteCode(code)
            result.fold(
                onSuccess = { joinedWedding ->
                    _selectedWeddingId.value = joinedWedding.id
                    onResult(true, "Joined ${joinedWedding.name} successfully!")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Could not join wedding")
                }
            )
        }
    }

    fun toggleLiveSync(enabled: Boolean) {
        val weddingId = selectedWeddingId.value ?: return
        syncManager?.setLiveSyncEnabled(enabled, weddingId)
    }
}

class WeddingViewModelFactory(
    private val repository: WeddingRepository,
    private val authManager: FirebaseAuthManager? = null,
    private val syncManager: WeddingFirestoreSyncManager? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeddingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeddingViewModel(repository, authManager, syncManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
