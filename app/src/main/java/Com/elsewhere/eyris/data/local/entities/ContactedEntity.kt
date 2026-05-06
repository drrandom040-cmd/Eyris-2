package Com.elsewhere.eyris.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import Com.elsewhere.eyris.domain.models.LeadStatus

@Entity(tableName = "contacted")
data class ContactedEntity(
    @PrimaryKey val contactedId: String,
    val userId: String,
    @Embedded(prefix = "lead_") val lead: LeadEntity,
    val status: LeadStatus,
    val contactedAt: Long,
    val lastUpdatedAt: Long,
    val notes: String,
    val socialHandleTapped: String?,
    val synced: Boolean
)
