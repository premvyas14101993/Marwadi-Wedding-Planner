package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "weddings")
data class WeddingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brideName: String,
    val groomName: String,
    val weddingDate: Long,
    val engagementDate: Long? = null,
    val venue: String = "",
    val city: String = "",
    val familyName: String = "",
    val overallBudget: Double = 0.0,
    val coverImageUrl: String? = null,
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val creatorUid: String = "",
    val creatorName: String = ""
)

@Entity(
    tableName = "rituals",
    indices = [Index("weddingId")]
)
data class RitualEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weddingId: Long,
    val name: String,
    val hindiName: String = "",
    val description: String = "",
    val culturalSignificance: String = "",
    val vidhiDetails: String = "",
    val date: Long? = null,
    val time: String? = null,
    val venue: String? = null,
    val responsiblePerson: String? = null,
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val orderIndex: Int = 0,
    val budgetAllocation: Double = 0.0,
    val notes: String = ""
)

@Entity(
    tableName = "ritual_checklists",
    indices = [Index("weddingId"), Index("ritualId")]
)
data class RitualChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weddingId: Long,
    val ritualId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val assignedTo: String? = null,
    val notes: String = ""
)

@Entity(
    tableName = "materials",
    indices = [Index("weddingId"), Index("ritualId")]
)
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weddingId: Long,
    val ritualId: Long? = null, // null if general inventory
    val ritualName: String? = null,
    val item: String,
    val category: String = "Puja Samagri", // Puja Samagri, Decoration, Clothing, Gifts, Sweets & Dryfruits, General
    val requiredQuantity: Double = 1.0,
    val unit: String = "pcs", // kg, pcs, packets, meters, liters, boxes, gm
    val estimatedCost: Double = 0.0,
    val purchasedQuantity: Double = 0.0,
    val vendor: String? = null,
    val purchaseDate: Long? = null,
    val storageLocation: String? = null,
    val notes: String = "",
    val isPurchased: Boolean = false
)

@Entity(
    tableName = "people",
    indices = [Index("weddingId")]
)
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weddingId: Long,
    val name: String,
    val phone: String = "",
    val familySide: String = "COMMON", // BRIDE_SIDE, GROOM_SIDE, COMMON, VENDOR, EXTERNAL
    val relationship: String = "",
    val role: String = "",
    val responsibility: String = "",
    val assignedRituals: String = "",
    val notes: String = ""
)

@Entity(
    tableName = "vendors",
    indices = [Index("weddingId")]
)
data class VendorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weddingId: Long,
    val name: String,
    val serviceType: String, // Caterer, Decorator, Photographer, Videographer, Makeup Artist, Mehendi Artist, Band & Dhol, DJ & Sound, Transportation, Venue, Pandit, Florist, Clothing & Poshak, Jewellery, Invitations, Entertainment, Other
    val contactNumber: String = "",
    val address: String = "",
    val gstNumber: String = "",
    val totalContractValue: Double = 0.0,
    val advancePaid: Double = 0.0,
    val dueDate: Long? = null,
    val rating: Float = 5.0f,
    val notes: String = ""
)

@Entity(
    tableName = "vendor_quotations",
    indices = [Index("weddingId")]
)
data class VendorQuotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weddingId: Long,
    val vendorName: String,
    val serviceType: String,
    val quoteAmount: Double,
    val servicesIncluded: String = "",
    val availability: String = "Available",
    val rating: Float = 4.5f,
    val isSelected: Boolean = false,
    val notes: String = ""
)

@Entity(
    tableName = "expenses",
    indices = [Index("weddingId"), Index("ritualId"), Index("vendorId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weddingId: Long,
    val expenseName: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val category: String, // Food & Catering, Decoration & Mandap, Clothing & Poshak, Jewellery, Ritual Materials (Samagri), Transportation & Logistics, Photography & Video, Entertainment & Music, Gifts & Shagun, Venue & Accommodation, Miscellaneous
    val ritualId: Long? = null,
    val ritualName: String? = null,
    val vendorId: Long? = null,
    val vendorName: String? = null,
    val paidBy: String = "Self",
    val paymentMode: String = "UPI", // Cash, UPI, Bank Transfer, Card, Cheque, Other
    val billNumber: String = "",
    val receiptUri: String? = null,
    val receiptFileName: String? = null,
    val receiptMimeType: String? = null,
    val notes: String = "",
    val addedByUid: String = "",
    val addedByName: String = ""
)

@Entity(
    tableName = "guests",
    indices = [Index("weddingId")]
)
data class GuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weddingId: Long,
    val name: String,
    val familyName: String = "",
    val side: String = "BRIDE_SIDE", // BRIDE_SIDE, GROOM_SIDE, COMMON
    val phone: String = "",
    val address: String = "",
    val city: String = "",
    val rsvpStatus: String = "PENDING", // ACCEPTED, TENTATIVE, DECLINED, PENDING
    val invitationSent: Boolean = false,
    val numberOfMembers: Int = 1,
    val attendanceConfirmed: Boolean = false,
    val accommodationRequired: Boolean = false,
    val hotelRoomAllocated: String = "",
    val foodPreference: String = "Pure Veg", // Pure Veg, Jain Food, Special
    val giftReceived: String = "",
    val notes: String = ""
)

@Entity(
    tableName = "tasks",
    indices = [Index("weddingId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weddingId: Long,
    val taskName: String,
    val description: String = "",
    val assignedTo: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED
    val category: String = "General",
    val notes: String = ""
)

@Entity(
    tableName = "gifts",
    indices = [Index("weddingId")]
)
data class GiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weddingId: Long,
    val giftItem: String,
    val giverName: String,
    val relationship: String = "",
    val familySide: String = "COMMON",
    val estimatedValue: Double = 0.0,
    val dateReceived: Long = System.currentTimeMillis(),
    val returnGiftGiven: String = "",
    val isThankYouSent: Boolean = false,
    val notes: String = ""
)

@Entity(
    tableName = "notes",
    indices = [Index("weddingId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weddingId: Long,
    val title: String,
    val content: String,
    val category: String = "Personal Notes", // Decoration Ideas, Menu & Food, Clothing & Poshak, Jewellery & Ornaments, Sangeet Playlist, Pandit Ji Instructions, Personal Notes
    val timestamp: Long = System.currentTimeMillis()
)
