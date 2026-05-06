package com.elsewhere.eyris.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsewhere.eyris.data.repositories.ContactedRepository
import com.elsewhere.eyris.data.repositories.LeadsRepository
import com.elsewhere.eyris.domain.models.ContactStatus
import com.elsewhere.eyris.domain.models.ContactedLead
import com.elsewhere.eyris.domain.models.Lead
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val leadsRepository: LeadsRepository,
    private val contactedRepository: ContactedRepository
) : ViewModel() {

    private val _lead = MutableStateFlow<Lead?>(null)
    val lead: StateFlow<Lead?> = _lead

    fun loadLead(leadId: String) {
        viewModelScope.launch {
            val leads = leadsRepository.getLeads()
            _lead.value = leads.find { it.leadId == leadId }
        }
    }

    fun contactLead(lead: Lead, platform: String) {
        viewModelScope.launch {
            val contacted = ContactedLead(
                contactedId = UUID.randomUUID().toString(),
                userId = lead.userId,
                businessName = lead.businessName,
                category = lead.category,
                address = lead.address,
                lat = lead.lat,
                lng = lead.lng,
                phone = lead.phone,
                email = lead.email,
                coverImageUrl = lead.coverImageUrl,
                openingHours = lead.openingHours,
                instagram = lead.instagram,
                facebook = lead.facebook,
                tiktok = lead.tiktok,
                whatsapp = lead.whatsapp,
                hasWebsite = lead.hasWebsite,
                websiteUrl = lead.websiteUrl,
                rating = lead.rating,
                reviewCount = lead.reviewCount,
                weightedScore = lead.weightedScore,
                status = ContactStatus.ANSWERED,
                contactedAt = System.currentTimeMillis(),
                socialHandleTapped = platform
            )
            contactedRepository.saveContacted(contacted)
            // Optionally delete from leads
            // leadsRepository.deleteLead(lead.leadId)
        }
    }
}
