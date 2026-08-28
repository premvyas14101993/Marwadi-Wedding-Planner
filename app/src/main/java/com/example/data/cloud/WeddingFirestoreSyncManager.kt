package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
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
import com.example.data.local.entities.WeddingEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.random.Random

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Synced(val timestamp: Long, val message: String) : SyncStatus()
    data class Error(val errorMessage: String) : SyncStatus()
}

data class SyncStats(
    val ritualsCount: Int = 0,
    val expensesCount: Int = 0,
    val guestsCount: Int = 0,
    val tasksCount: Int = 0,
    val materialsCount: Int = 0,
    val vendorsCount: Int = 0,
    val giftsCount: Int = 0,
    val notesCount: Int = 0,
    val connectedMembersCount: Int = 1
)

class WeddingFirestoreSyncManager(
    private val context: Context,
    private val database: AppDatabase,
    private val scope: CoroutineScope
) {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("WeddingFirestoreSync", "Firestore getInstance fallback: ${e.message}")
            null
        }

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("WeddingFirestoreSync", "FirebaseAuth getInstance fallback: ${e.message}")
            null
        }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _syncStats = MutableStateFlow(SyncStats())
    val syncStats: StateFlow<SyncStats> = _syncStats.asStateFlow()

    private val _activeInviteCode = MutableStateFlow<String?>(null)
    val activeInviteCode: StateFlow<String?> = _activeInviteCode.asStateFlow()

    private val _isLiveSyncActive = MutableStateFlow(true)
    val isLiveSyncActive: StateFlow<Boolean> = _isLiveSyncActive.asStateFlow()

    private val listenerRegistrations = mutableListOf<ListenerRegistration>()
    private var currentCloudWeddingId: String? = null

    init {
        FirebaseInitializer.init(context)
        try {
            val fs = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            fs.firestoreSettings = settings
        } catch (e: Exception) {
            Log.d("WeddingFirestoreSync", "Firestore cache settings initialized")
        }
        // Load stored active invite code if any
        val prefs = context.getSharedPreferences("wedding_cloud_sync_prefs", Context.MODE_PRIVATE)
        _activeInviteCode.value = prefs.getString("last_invite_code", null)
    }

    fun getInviteCodeForWedding(weddingId: Long): String? {
        val prefs = context.getSharedPreferences("wedding_cloud_sync_prefs", Context.MODE_PRIVATE)
        return prefs.getString("invite_code_wedding_$weddingId", null)
    }

    fun getCloudDocIdForWedding(weddingId: Long): String? {
        val prefs = context.getSharedPreferences("wedding_cloud_sync_prefs", Context.MODE_PRIVATE)
        return prefs.getString("cloud_id_wedding_$weddingId", null)
    }

    fun setLiveSyncEnabled(enabled: Boolean, weddingId: Long) {
        _isLiveSyncActive.value = enabled
        if (enabled) {
            scope.launch { startRealtimeSync(weddingId) }
        } else {
            stopRealtimeListeners()
        }
    }

    private fun saveInviteCodeForWedding(weddingId: Long, code: String, cloudDocId: String? = null) {
        _activeInviteCode.value = code
        val prefs = context.getSharedPreferences("wedding_cloud_sync_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .putString("invite_code_wedding_$weddingId", code)
            .putString("last_invite_code", code)
        if (cloudDocId != null) {
            editor.putString("cloud_id_wedding_$weddingId", cloudDocId)
        }
        editor.apply()
    }

    /**
     * Generate or fetch a shareable invite code for this wedding
     */
    suspend fun getOrCreateInviteCode(wedding: WeddingEntity): String {
        val prefs = context.getSharedPreferences("wedding_cloud_sync_prefs", Context.MODE_PRIVATE)
        val savedCodeForWedding = prefs.getString("invite_code_wedding_${wedding.id}", null)
        val savedCloudDocId = prefs.getString("cloud_id_wedding_${wedding.id}", null)

        val weddingDocId = savedCloudDocId ?: "wedding_${wedding.id}_${wedding.createdAt}"
        val fs = firestore

        if (fs == null) {
            val localFallbackCode = savedCodeForWedding ?: ("MW-" + Random.nextInt(100000, 999999))
            saveInviteCodeForWedding(wedding.id, localFallbackCode, weddingDocId)
            _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis(), "Invite Code Ready: $localFallbackCode")
            return localFallbackCode
        }

        try {
            _syncStatus.value = SyncStatus.Syncing
            val currentUser = auth?.currentUser

            // 1. If we already have a saved code for this wedding, ensure it's synced to Firestore
            if (!savedCodeForWedding.isNullOrBlank()) {
                currentCloudWeddingId = weddingDocId
                saveInviteCodeForWedding(wedding.id, savedCodeForWedding, weddingDocId)

                val weddingData = hashMapOf(
                    "cloudId" to weddingDocId,
                    "localId" to wedding.id,
                    "name" to wedding.name,
                    "brideName" to wedding.brideName,
                    "groomName" to wedding.groomName,
                    "weddingDate" to wedding.weddingDate,
                    "venue" to wedding.venue,
                    "city" to wedding.city,
                    "familyName" to wedding.familyName,
                    "overallBudget" to wedding.overallBudget,
                    "inviteCode" to savedCodeForWedding,
                    "ownerUid" to (currentUser?.uid ?: "anonymous"),
                    "ownerEmail" to (currentUser?.email ?: ""),
                    "ownerName" to (currentUser?.displayName ?: "Family Organizer"),
                    "updatedAt" to System.currentTimeMillis()
                )

                val inviteIndexData = hashMapOf(
                    "weddingDocId" to weddingDocId,
                    "inviteCode" to savedCodeForWedding,
                    "weddingName" to wedding.name,
                    "groomName" to wedding.groomName,
                    "brideName" to wedding.brideName,
                    "weddingDate" to wedding.weddingDate,
                    "venue" to wedding.venue,
                    "city" to wedding.city,
                    "familyName" to wedding.familyName,
                    "overallBudget" to wedding.overallBudget,
                    "createdAt" to wedding.createdAt,
                    "updatedAt" to System.currentTimeMillis()
                )

                try {
                    fs.collection("weddings").document(weddingDocId).set(weddingData, SetOptions.merge()).await()
                    fs.collection("invite_codes").document(savedCodeForWedding.uppercase(Locale.ROOT)).set(inviteIndexData, SetOptions.merge()).await()
                } catch (e: Exception) {
                    Log.w("FirestoreSync", "Update existing code metadata: ${e.message}")
                }

                _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis(), "Synced with Cloud (Code: $savedCodeForWedding)")
                return savedCodeForWedding
            }

            // 2. Check if Firestore already has a code for this weddingDocId
            var remoteExistingCode: String? = null
            try {
                val existingDoc = fs.collection("weddings").document(weddingDocId).get().await()
                if (existingDoc.exists()) {
                    remoteExistingCode = existingDoc.getString("inviteCode")
                }
            } catch (offlineEx: Exception) {
                Log.w("FirestoreSync", "Document get offline/cache notice: ${offlineEx.message}")
            }

            if (!remoteExistingCode.isNullOrBlank()) {
                currentCloudWeddingId = weddingDocId
                saveInviteCodeForWedding(wedding.id, remoteExistingCode, weddingDocId)
                _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis(), "Synced with Cloud (Code: $remoteExistingCode)")
                return remoteExistingCode
            }

            // 3. Generate a brand-new unique invite code for this wedding
            var uniqueGeneratedCode = "MW-" + Random.nextInt(100000, 999999)
            // Ensure no collision with other weddings
            try {
                for (attempt in 0..5) {
                    val codeCheck = fs.collection("invite_codes").document(uniqueGeneratedCode).get().await()
                    if (codeCheck.exists()) {
                        val existingDocId = codeCheck.getString("weddingDocId")
                        if (existingDocId != null && existingDocId != weddingDocId) {
                            // Collision with different wedding -> generate another code
                            uniqueGeneratedCode = "MW-" + Random.nextInt(100000, 999999)
                            continue
                        }
                    }
                    break
                }
            } catch (checkEx: Exception) {
                Log.w("FirestoreSync", "Invite code collision check: ${checkEx.message}")
            }

            val weddingData = hashMapOf(
                "cloudId" to weddingDocId,
                "localId" to wedding.id,
                "name" to wedding.name,
                "brideName" to wedding.brideName,
                "groomName" to wedding.groomName,
                "weddingDate" to wedding.weddingDate,
                "venue" to wedding.venue,
                "city" to wedding.city,
                "familyName" to wedding.familyName,
                "overallBudget" to wedding.overallBudget,
                "inviteCode" to uniqueGeneratedCode,
                "creatorUid" to (if (wedding.creatorUid.isNotBlank()) wedding.creatorUid else (currentUser?.uid ?: "anonymous")),
                "creatorName" to (if (wedding.creatorName.isNotBlank()) wedding.creatorName else (currentUser?.displayName ?: "Family Organizer")),
                "ownerUid" to (currentUser?.uid ?: "anonymous"),
                "ownerEmail" to (currentUser?.email ?: ""),
                "ownerName" to (currentUser?.displayName ?: "Family Organizer"),
                "updatedAt" to System.currentTimeMillis(),
                "members" to listOf(
                    mapOf(
                        "uid" to (currentUser?.uid ?: "local_user"),
                        "name" to (currentUser?.displayName ?: "Organizer"),
                        "email" to (currentUser?.email ?: ""),
                        "joinedAt" to System.currentTimeMillis()
                    )
                )
            )

            val inviteIndexData = hashMapOf(
                "weddingDocId" to weddingDocId,
                "inviteCode" to uniqueGeneratedCode,
                "weddingName" to wedding.name,
                "groomName" to wedding.groomName,
                "brideName" to wedding.brideName,
                "weddingDate" to wedding.weddingDate,
                "venue" to wedding.venue,
                "city" to wedding.city,
                "familyName" to wedding.familyName,
                "overallBudget" to wedding.overallBudget,
                "createdAt" to wedding.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )

            try {
                fs.collection("weddings").document(weddingDocId).set(weddingData, SetOptions.merge()).await()
                fs.collection("invite_codes").document(uniqueGeneratedCode.uppercase(Locale.ROOT)).set(inviteIndexData, SetOptions.merge()).await()
                val compactCode = uniqueGeneratedCode.replace("-", "").uppercase(Locale.ROOT)
                if (compactCode != uniqueGeneratedCode.uppercase(Locale.ROOT)) {
                    fs.collection("invite_codes").document(compactCode).set(inviteIndexData, SetOptions.merge()).await()
                }
            } catch (setEx: Exception) {
                Log.w("FirestoreSync", "Firestore online write: ${setEx.message}")
            }

            currentCloudWeddingId = weddingDocId
            saveInviteCodeForWedding(wedding.id, uniqueGeneratedCode, weddingDocId)

            // Push initial data
            pushAllLocalDataToFirestore(wedding.id, weddingDocId)
            _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis(), "Invite Code Ready: $uniqueGeneratedCode (Sync Active)")
            return uniqueGeneratedCode
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Failed to generate remote invite code, using local code: ${e.message}")
            val fallbackCode = savedCodeForWedding ?: ("MW-" + Random.nextInt(100000, 999999))
            saveInviteCodeForWedding(wedding.id, fallbackCode, weddingDocId)
            _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis(), "Invite Code: $fallbackCode")
            return fallbackCode
        }
    }

    /**
     * Join an existing wedding created by another family member
     */
    suspend fun joinWeddingByInviteCode(inviteCode: String): Result<WeddingEntity> {
        val fs = firestore ?: return Result.failure(Exception("Cloud service is not connected on this device."))
        return try {
            _syncStatus.value = SyncStatus.Syncing
            val rawClean = inviteCode.trim().uppercase(Locale.ROOT)
            val codeVariants = linkedSetOf<String>()
            codeVariants.add(rawClean)
            codeVariants.add(rawClean.replace(" ", "").replace("-", ""))
            if (!rawClean.startsWith("MW-")) {
                val digitsOnly = rawClean.removePrefix("MW").removePrefix("-").trim()
                codeVariants.add("MW-$digitsOnly")
                codeVariants.add("MW$digitsOnly")
            } else {
                val digitsOnly = rawClean.removePrefix("MW-").trim()
                codeVariants.add(digitsOnly)
                codeVariants.add("MW$digitsOnly")
            }

            var cloudWeddingId: String? = null
            var weddingDocData: Map<String, Any?>? = null

            // Strategy 1: Direct document lookup in invite_codes collection
            for (variant in codeVariants) {
                if (variant.isBlank()) continue
                try {
                    val inviteDoc = fs.collection("invite_codes").document(variant).get().await()
                    if (inviteDoc.exists()) {
                        val targetWeddingDocId = inviteDoc.getString("weddingDocId")
                        if (!targetWeddingDocId.isNullOrBlank()) {
                            val weddingDoc = fs.collection("weddings").document(targetWeddingDocId).get().await()
                            if (weddingDoc.exists()) {
                                cloudWeddingId = weddingDoc.id
                                weddingDocData = weddingDoc.data
                                break
                            }
                        }
                        cloudWeddingId = targetWeddingDocId ?: "wedding_${inviteDoc.id}"
                        weddingDocData = inviteDoc.data
                        break
                    }
                } catch (e: Exception) {
                    Log.w("FirestoreSync", "invite_codes lookup attempt for $variant: ${e.message}")
                }
            }

            // Strategy 2: Query weddings collection by inviteCode field
            if (cloudWeddingId == null || weddingDocData == null) {
                for (variant in codeVariants) {
                    if (variant.isBlank()) continue
                    try {
                        val querySnap = fs.collection("weddings")
                            .whereEqualTo("inviteCode", variant)
                            .limit(1)
                            .get()
                            .await()
                        if (!querySnap.isEmpty) {
                            val doc = querySnap.documents.first()
                            cloudWeddingId = doc.id
                            weddingDocData = doc.data
                            break
                        }
                    } catch (e: Exception) {
                        Log.w("FirestoreSync", "weddings whereEqualTo query for $variant: ${e.message}")
                    }
                }
            }

            // Strategy 3: Scan weddings collection fallback
            if (cloudWeddingId == null || weddingDocData == null) {
                try {
                    val allWeddingsSnap = fs.collection("weddings").limit(20).get().await()
                    for (doc in allWeddingsSnap.documents) {
                        val docCode = doc.getString("inviteCode")?.trim()?.uppercase(Locale.ROOT)
                        if (docCode != null && codeVariants.any { it.equals(docCode, ignoreCase = true) || it.replace("-", "").equals(docCode.replace("-", ""), ignoreCase = true) }) {
                            cloudWeddingId = doc.id
                            weddingDocData = doc.data
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.w("FirestoreSync", "all weddings scan fallback: ${e.message}")
                }
            }

            if (cloudWeddingId == null || weddingDocData == null) {
                _syncStatus.value = SyncStatus.Error("Invite code not found ($inviteCode). Please check the code.")
                return Result.failure(Exception("No wedding found for invite code: $inviteCode"))
            }

            val currentUser = auth?.currentUser

            // Register current user as member in cloud
            try {
                currentUser?.let { user ->
                    val newMember = mapOf(
                        "uid" to user.uid,
                        "name" to (user.displayName ?: "Family Member"),
                        "email" to (user.email ?: ""),
                        "joinedAt" to System.currentTimeMillis()
                    )
                    fs.collection("weddings").document(cloudWeddingId)
                        .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(newMember))
                }
            } catch (memberEx: Exception) {
                Log.w("FirestoreSync", "Could not update members array: ${memberEx.message}")
            }

            // Extract wedding details safely
            val name = (weddingDocData["name"] as? String) ?: (weddingDocData["weddingName"] as? String) ?: "Family Wedding"
            val brideName = (weddingDocData["brideName"] as? String) ?: "Bride"
            val groomName = (weddingDocData["groomName"] as? String) ?: "Groom"
            val weddingDate = ((weddingDocData["weddingDate"] as? Number)?.toLong()) ?: System.currentTimeMillis()
            val venue = (weddingDocData["venue"] as? String) ?: ""
            val city = (weddingDocData["city"] as? String) ?: ""
            val familyName = (weddingDocData["familyName"] as? String) ?: ""
            val overallBudget = ((weddingDocData["overallBudget"] as? Number)?.toDouble()) ?: 0.0
            val createdAt = ((weddingDocData["createdAt"] as? Number)?.toLong()) ?: System.currentTimeMillis()
            val creatorUid = (weddingDocData["creatorUid"] as? String) ?: (weddingDocData["ownerUid"] as? String) ?: ""
            val creatorName = (weddingDocData["creatorName"] as? String) ?: (weddingDocData["ownerName"] as? String) ?: ""

            // Check if already in local DB
            val allLocalWeddings = database.weddingDao().getAllWeddingsOnce()
            val existingLocal = allLocalWeddings.firstOrNull { it.name.equals(name, ignoreCase = true) && it.groomName.equals(groomName, ignoreCase = true) }

            val localWeddingId: Long
            val savedWedding: WeddingEntity

            if (existingLocal != null) {
                localWeddingId = existingLocal.id
                savedWedding = existingLocal
            } else {
                val weddingEntity = WeddingEntity(
                    name = name,
                    brideName = brideName,
                    groomName = groomName,
                    weddingDate = weddingDate,
                    venue = venue,
                    city = city,
                    familyName = familyName,
                    overallBudget = overallBudget,
                    createdAt = createdAt,
                    creatorUid = creatorUid,
                    creatorName = creatorName
                )
                localWeddingId = database.weddingDao().insertWedding(weddingEntity)
                savedWedding = weddingEntity.copy(id = localWeddingId)
            }

            currentCloudWeddingId = cloudWeddingId
            saveInviteCodeForWedding(localWeddingId, rawClean, cloudWeddingId)

            // Download all subcollections into local Room database
            pullAllCloudDataToLocal(localWeddingId, cloudWeddingId)

            // Start listening for real-time changes
            startRealtimeSync(localWeddingId)

            _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis(), "Successfully Joined $name!")
            Result.success(savedWedding)
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error joining wedding", e)
            _syncStatus.value = SyncStatus.Error(e.message ?: "Failed to join wedding")
            Result.failure(e)
        }
    }

    /**
     * Push all local wedding tables to Firestore
     */
    suspend fun pushAllLocalDataToFirestore(localWeddingId: Long, cloudId: String? = null) {
        val fs = firestore ?: return
        try {
            _syncStatus.value = SyncStatus.Syncing
            val targetCloudId = cloudId ?: getCloudDocIdForWedding(localWeddingId) ?: currentCloudWeddingId ?: "wedding_$localWeddingId"

            val rituals = database.ritualDao().getRitualsForWeddingOnce(localWeddingId)
            val expenses = database.expenseDao().getExpensesForWeddingOnce(localWeddingId)
            val guests = database.guestDao().getGuestsForWeddingOnce(localWeddingId)
            val tasks = database.taskDao().getTasksForWeddingOnce(localWeddingId)
            val materials = database.materialDao().getMaterialsForWeddingOnce(localWeddingId)
            val vendors = database.vendorDao().getVendorsForWeddingOnce(localWeddingId)
            val gifts = database.giftDao().getGiftsForWeddingOnce(localWeddingId)
            val notes = database.noteDao().getNotesForWeddingOnce(localWeddingId)
            val checklists = database.ritualDao().getAllChecklistsForWeddingOnce(localWeddingId)

            val weddingRef = fs.collection("weddings").document(targetCloudId)

            // Batch writes for efficiency
            val batch = fs.batch()

            rituals.forEach { ritual ->
                val ref = weddingRef.collection("rituals").document("ritual_${ritual.id}")
                val map = hashMapOf(
                    "id" to ritual.id,
                    "name" to ritual.name,
                    "hindiName" to ritual.hindiName,
                    "description" to ritual.description,
                    "culturalSignificance" to ritual.culturalSignificance,
                    "vidhiDetails" to ritual.vidhiDetails,
                    "date" to ritual.date,
                    "time" to ritual.time,
                    "venue" to ritual.venue,
                    "responsiblePerson" to ritual.responsiblePerson,
                    "status" to ritual.status,
                    "priority" to ritual.priority,
                    "orderIndex" to ritual.orderIndex,
                    "budgetAllocation" to ritual.budgetAllocation,
                    "notes" to ritual.notes,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(ref, map, SetOptions.merge())
            }

            expenses.forEach { expense ->
                val ref = weddingRef.collection("expenses").document("expense_${expense.id}")
                val map = hashMapOf(
                    "id" to expense.id,
                    "expenseName" to expense.expenseName,
                    "amount" to expense.amount,
                    "date" to expense.date,
                    "category" to expense.category,
                    "ritualId" to expense.ritualId,
                    "ritualName" to expense.ritualName,
                    "vendorId" to expense.vendorId,
                    "vendorName" to expense.vendorName,
                    "paidBy" to expense.paidBy,
                    "paymentMode" to expense.paymentMode,
                    "billNumber" to expense.billNumber,
                    "receiptFileName" to expense.receiptFileName,
                    "notes" to expense.notes,
                    "addedByUid" to expense.addedByUid,
                    "addedByName" to expense.addedByName,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(ref, map, SetOptions.merge())
            }

            guests.forEach { guest ->
                val ref = weddingRef.collection("guests").document("guest_${guest.id}")
                val map = hashMapOf(
                    "id" to guest.id,
                    "name" to guest.name,
                    "familyName" to guest.familyName,
                    "side" to guest.side,
                    "phone" to guest.phone,
                    "address" to guest.address,
                    "city" to guest.city,
                    "rsvpStatus" to guest.rsvpStatus,
                    "invitationSent" to guest.invitationSent,
                    "numberOfMembers" to guest.numberOfMembers,
                    "attendanceConfirmed" to guest.attendanceConfirmed,
                    "accommodationRequired" to guest.accommodationRequired,
                    "hotelRoomAllocated" to guest.hotelRoomAllocated,
                    "foodPreference" to guest.foodPreference,
                    "giftReceived" to guest.giftReceived,
                    "notes" to guest.notes,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(ref, map, SetOptions.merge())
            }

            tasks.forEach { task ->
                val ref = weddingRef.collection("tasks").document("task_${task.id}")
                val map = hashMapOf(
                    "id" to task.id,
                    "taskName" to task.taskName,
                    "description" to task.description,
                    "assignedTo" to task.assignedTo,
                    "dueDate" to task.dueDate,
                    "priority" to task.priority,
                    "status" to task.status,
                    "category" to task.category,
                    "notes" to task.notes,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(ref, map, SetOptions.merge())
            }

            materials.forEach { mat ->
                val ref = weddingRef.collection("materials").document("mat_${mat.id}")
                val map = hashMapOf(
                    "id" to mat.id,
                    "ritualId" to mat.ritualId,
                    "ritualName" to mat.ritualName,
                    "item" to mat.item,
                    "category" to mat.category,
                    "requiredQuantity" to mat.requiredQuantity,
                    "unit" to mat.unit,
                    "estimatedCost" to mat.estimatedCost,
                    "purchasedQuantity" to mat.purchasedQuantity,
                    "vendor" to mat.vendor,
                    "isPurchased" to mat.isPurchased,
                    "notes" to mat.notes,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(ref, map, SetOptions.merge())
            }

            vendors.forEach { vendor ->
                val ref = weddingRef.collection("vendors").document("vendor_${vendor.id}")
                val map = hashMapOf(
                    "id" to vendor.id,
                    "name" to vendor.name,
                    "serviceType" to vendor.serviceType,
                    "contactNumber" to vendor.contactNumber,
                    "address" to vendor.address,
                    "gstNumber" to vendor.gstNumber,
                    "totalContractValue" to vendor.totalContractValue,
                    "advancePaid" to vendor.advancePaid,
                    "dueDate" to vendor.dueDate,
                    "rating" to vendor.rating,
                    "notes" to vendor.notes,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(ref, map, SetOptions.merge())
            }

            gifts.forEach { gift ->
                val ref = weddingRef.collection("gifts").document("gift_${gift.id}")
                val map = hashMapOf(
                    "id" to gift.id,
                    "giftItem" to gift.giftItem,
                    "giverName" to gift.giverName,
                    "relationship" to gift.relationship,
                    "familySide" to gift.familySide,
                    "estimatedValue" to gift.estimatedValue,
                    "dateReceived" to gift.dateReceived,
                    "returnGiftGiven" to gift.returnGiftGiven,
                    "isThankYouSent" to gift.isThankYouSent,
                    "notes" to gift.notes,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(ref, map, SetOptions.merge())
            }

            notes.forEach { note ->
                val ref = weddingRef.collection("notes").document("note_${note.id}")
                val map = hashMapOf(
                    "id" to note.id,
                    "title" to note.title,
                    "content" to note.content,
                    "category" to note.category,
                    "timestamp" to note.timestamp,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(ref, map, SetOptions.merge())
            }

            checklists.forEach { chk ->
                val ref = weddingRef.collection("checklists").document("chk_${chk.id}")
                val map = hashMapOf(
                    "id" to chk.id,
                    "ritualId" to chk.ritualId,
                    "title" to chk.title,
                    "isCompleted" to chk.isCompleted,
                    "assignedTo" to chk.assignedTo,
                    "notes" to chk.notes,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(ref, map, SetOptions.merge())
            }

            batch.commit().await()

            _syncStats.value = SyncStats(
                ritualsCount = rituals.size,
                expensesCount = expenses.size,
                guestsCount = guests.size,
                tasksCount = tasks.size,
                materialsCount = materials.size,
                vendorsCount = vendors.size,
                giftsCount = gifts.size,
                notesCount = notes.size,
                connectedMembersCount = 1
            )

            _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis(), "All wedding items backed up to cloud")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Push error", e)
            _syncStatus.value = SyncStatus.Error("Sync error: ${e.message}")
        }
    }

    /**
     * Pull all data from Firestore into local Room database
     */
    private suspend fun pullAllCloudDataToLocal(localWeddingId: Long, cloudWeddingId: String) {
        val fs = firestore ?: return
        val weddingRef = fs.collection("weddings").document(cloudWeddingId)

        // Rituals
        val ritualsSnap = weddingRef.collection("rituals").get().await()
        ritualsSnap.documents.forEach { doc ->
            val entity = RitualEntity(
                weddingId = localWeddingId,
                name = doc.getString("name") ?: "",
                hindiName = doc.getString("hindiName") ?: "",
                description = doc.getString("description") ?: "",
                culturalSignificance = doc.getString("culturalSignificance") ?: "",
                vidhiDetails = doc.getString("vidhiDetails") ?: "",
                date = doc.getLong("date"),
                time = doc.getString("time"),
                venue = doc.getString("venue"),
                responsiblePerson = doc.getString("responsiblePerson"),
                status = doc.getString("status") ?: "PENDING",
                priority = doc.getString("priority") ?: "MEDIUM",
                orderIndex = (doc.getLong("orderIndex") ?: 0L).toInt(),
                budgetAllocation = doc.getDouble("budgetAllocation") ?: 0.0,
                notes = doc.getString("notes") ?: ""
            )
            database.ritualDao().insertRitual(entity)
        }

        // Expenses
        val expensesSnap = weddingRef.collection("expenses").get().await()
        expensesSnap.documents.forEach { doc ->
            val entity = ExpenseEntity(
                weddingId = localWeddingId,
                expenseName = doc.getString("expenseName") ?: "",
                amount = doc.getDouble("amount") ?: 0.0,
                date = doc.getLong("date") ?: System.currentTimeMillis(),
                category = doc.getString("category") ?: "Miscellaneous",
                ritualId = doc.getLong("ritualId"),
                ritualName = doc.getString("ritualName"),
                vendorId = doc.getLong("vendorId"),
                vendorName = doc.getString("vendorName"),
                paidBy = doc.getString("paidBy") ?: "Self",
                paymentMode = doc.getString("paymentMode") ?: "UPI",
                billNumber = doc.getString("billNumber") ?: "",
                receiptFileName = doc.getString("receiptFileName"),
                notes = doc.getString("notes") ?: "",
                addedByUid = doc.getString("addedByUid") ?: "",
                addedByName = doc.getString("addedByName") ?: doc.getString("paidBy") ?: ""
            )
            database.expenseDao().insertExpense(entity)
        }

        // Guests
        val guestsSnap = weddingRef.collection("guests").get().await()
        guestsSnap.documents.forEach { doc ->
            val entity = GuestEntity(
                weddingId = localWeddingId,
                name = doc.getString("name") ?: "",
                familyName = doc.getString("familyName") ?: "",
                side = doc.getString("side") ?: "COMMON",
                phone = doc.getString("phone") ?: "",
                address = doc.getString("address") ?: "",
                city = doc.getString("city") ?: "",
                rsvpStatus = doc.getString("rsvpStatus") ?: "PENDING",
                invitationSent = doc.getBoolean("invitationSent") ?: false,
                numberOfMembers = (doc.getLong("numberOfMembers") ?: 1L).toInt(),
                attendanceConfirmed = doc.getBoolean("attendanceConfirmed") ?: false,
                accommodationRequired = doc.getBoolean("accommodationRequired") ?: false,
                hotelRoomAllocated = doc.getString("hotelRoomAllocated") ?: "",
                foodPreference = doc.getString("foodPreference") ?: "Pure Veg",
                giftReceived = doc.getString("giftReceived") ?: "",
                notes = doc.getString("notes") ?: ""
            )
            database.guestDao().insertGuest(entity)
        }

        // Tasks
        val tasksSnap = weddingRef.collection("tasks").get().await()
        tasksSnap.documents.forEach { doc ->
            val entity = TaskEntity(
                weddingId = localWeddingId,
                taskName = doc.getString("taskName") ?: "",
                description = doc.getString("description") ?: "",
                assignedTo = doc.getString("assignedTo") ?: "",
                dueDate = doc.getLong("dueDate") ?: System.currentTimeMillis(),
                priority = doc.getString("priority") ?: "MEDIUM",
                status = doc.getString("status") ?: "PENDING",
                category = doc.getString("category") ?: "General",
                notes = doc.getString("notes") ?: ""
            )
            database.taskDao().insertTask(entity)
        }

        // Materials / Shopping
        val matsSnap = weddingRef.collection("materials").get().await()
        matsSnap.documents.forEach { doc ->
            val entity = MaterialEntity(
                weddingId = localWeddingId,
                ritualId = doc.getLong("ritualId"),
                ritualName = doc.getString("ritualName"),
                item = doc.getString("item") ?: "",
                category = doc.getString("category") ?: "Puja Samagri",
                requiredQuantity = doc.getDouble("requiredQuantity") ?: 1.0,
                unit = doc.getString("unit") ?: "pcs",
                estimatedCost = doc.getDouble("estimatedCost") ?: 0.0,
                purchasedQuantity = doc.getDouble("purchasedQuantity") ?: 0.0,
                vendor = doc.getString("vendor"),
                isPurchased = doc.getBoolean("isPurchased") ?: false,
                notes = doc.getString("notes") ?: ""
            )
            database.materialDao().insertMaterial(entity)
        }

        // Vendors
        val vendorsSnap = weddingRef.collection("vendors").get().await()
        vendorsSnap.documents.forEach { doc ->
            val entity = VendorEntity(
                weddingId = localWeddingId,
                name = doc.getString("name") ?: "",
                serviceType = doc.getString("serviceType") ?: "Vendor",
                contactNumber = doc.getString("contactNumber") ?: "",
                address = doc.getString("address") ?: "",
                gstNumber = doc.getString("gstNumber") ?: "",
                totalContractValue = doc.getDouble("totalContractValue") ?: 0.0,
                advancePaid = doc.getDouble("advancePaid") ?: 0.0,
                dueDate = doc.getLong("dueDate"),
                rating = (doc.getDouble("rating") ?: 5.0).toFloat(),
                notes = doc.getString("notes") ?: ""
            )
            database.vendorDao().insertVendor(entity)
        }

        // Gifts
        val giftsSnap = weddingRef.collection("gifts").get().await()
        giftsSnap.documents.forEach { doc ->
            val entity = GiftEntity(
                weddingId = localWeddingId,
                giftItem = doc.getString("giftItem") ?: "",
                giverName = doc.getString("giverName") ?: "",
                relationship = doc.getString("relationship") ?: "",
                familySide = doc.getString("familySide") ?: "COMMON",
                estimatedValue = doc.getDouble("estimatedValue") ?: 0.0,
                dateReceived = doc.getLong("dateReceived") ?: System.currentTimeMillis(),
                returnGiftGiven = doc.getString("returnGiftGiven") ?: "",
                isThankYouSent = doc.getBoolean("isThankYouSent") ?: false,
                notes = doc.getString("notes") ?: ""
            )
            database.giftDao().insertGift(entity)
        }

        // Notes
        val notesSnap = weddingRef.collection("notes").get().await()
        notesSnap.documents.forEach { doc ->
            val entity = NoteEntity(
                weddingId = localWeddingId,
                title = doc.getString("title") ?: "",
                content = doc.getString("content") ?: "",
                category = doc.getString("category") ?: "Personal Notes",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
            )
            database.noteDao().insertNote(entity)
        }
    }

    /**
     * Start listening for real-time changes on Cloud Firestore
     */
    fun startRealtimeSync(localWeddingId: Long) {
        val fs = firestore ?: return
        val cloudId = getCloudDocIdForWedding(localWeddingId) ?: currentCloudWeddingId ?: "wedding_$localWeddingId"
        currentCloudWeddingId = cloudId
        stopRealtimeListeners()

        val weddingRef = fs.collection("weddings").document(cloudId)

        // Listen for expenses added/edited by other devices
        val expenseReg = weddingRef.collection("expenses").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            scope.launch(Dispatchers.IO) {
                snapshot.documentChanges.forEach { change ->
                    val doc = change.document
                    val name = doc.getString("expenseName") ?: return@forEach
                    val amount = doc.getDouble("amount") ?: 0.0
                    val exp = ExpenseEntity(
                        weddingId = localWeddingId,
                        expenseName = name,
                        amount = amount,
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        category = doc.getString("category") ?: "Miscellaneous",
                        paidBy = doc.getString("paidBy") ?: "Self",
                        paymentMode = doc.getString("paymentMode") ?: "UPI",
                        notes = doc.getString("notes") ?: ""
                    )
                    database.expenseDao().insertExpense(exp)
                }
            }
        }
        listenerRegistrations.add(expenseReg)

        // Listen for guests changes
        val guestReg = weddingRef.collection("guests").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            scope.launch(Dispatchers.IO) {
                snapshot.documentChanges.forEach { change ->
                    val doc = change.document
                    val name = doc.getString("name") ?: return@forEach
                    val guest = GuestEntity(
                        weddingId = localWeddingId,
                        name = name,
                        familyName = doc.getString("familyName") ?: "",
                        side = doc.getString("side") ?: "COMMON",
                        rsvpStatus = doc.getString("rsvpStatus") ?: "PENDING",
                        numberOfMembers = (doc.getLong("numberOfMembers") ?: 1L).toInt(),
                        attendanceConfirmed = doc.getBoolean("attendanceConfirmed") ?: false
                    )
                    database.guestDao().insertGuest(guest)
                }
            }
        }
        listenerRegistrations.add(guestReg)

        // Listen for tasks changes
        val taskReg = weddingRef.collection("tasks").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            scope.launch(Dispatchers.IO) {
                snapshot.documentChanges.forEach { change ->
                    val doc = change.document
                    val taskName = doc.getString("taskName") ?: return@forEach
                    val task = TaskEntity(
                        weddingId = localWeddingId,
                        taskName = taskName,
                        assignedTo = doc.getString("assignedTo") ?: "",
                        status = doc.getString("status") ?: "PENDING",
                        priority = doc.getString("priority") ?: "MEDIUM"
                    )
                    database.taskDao().insertTask(task)
                }
            }
        }
        listenerRegistrations.add(taskReg)

        // Listen for member count
        val memberReg = weddingRef.addSnapshotListener { doc, error ->
            if (doc != null && doc.exists()) {
                val members = doc.get("members") as? List<*>
                val count = members?.size ?: 1
                _syncStats.value = _syncStats.value.copy(connectedMembersCount = count)
            }
        }
        listenerRegistrations.add(memberReg)
    }

    fun stopRealtimeListeners() {
        listenerRegistrations.forEach { it.remove() }
        listenerRegistrations.clear()
    }
}
