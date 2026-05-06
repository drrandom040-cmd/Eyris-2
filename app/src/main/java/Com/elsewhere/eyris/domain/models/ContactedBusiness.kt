package Com.elsewhere.eyris.domain.models

enum class LeadStatus {
    ANSWERED, ACCEPTED, REJECTED, GHOSTED
}

data class ContactedBusiness(
    val contactedId: String = "",
    val userId: String = "",
    val lead: Lead = Lead(),
    val status: LeadStatus = LeadStatus.ANSWERED,
    val contactedAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val socialHandleTapped: String? = null,
    val synced: Boolean = false
)
