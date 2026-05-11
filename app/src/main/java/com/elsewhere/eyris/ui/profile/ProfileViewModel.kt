package com.elsewhere.eyris.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsewhere.eyris.data.repositories.ContactedRepository
import com.elsewhere.eyris.data.repositories.LeadsRepository
import com.elsewhere.eyris.domain.models.LeadStatus
import com.elsewhere.eyris.domain.models.ContactedLead
import com.elsewhere.eyris.domain.models.Lead
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val leadsRepository: LeadsRepository,
    private val contactedRepository: ContactedRepository
) : ViewModel() {

    private val _leadId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfileUiState> = _leadId.flatMapLatest { id ->
        if (id == null) return@flatMapLatest flowOf(ProfileUiState.Loading)

        combine(
            leadsRepository.getAllLeads(),
            contactedRepository.getAllContacted()
        ) { leads, contacted ->
            val lead = leads.find { it.leadId == id }
            if (lead != null) {
                return@combine ProfileUiState.Success(lead)
            }

            val contactedLead = contacted.find { it.contactedId == id }
            if (contactedLead != null) {
                return@combine ProfileUiState.Success(contactedLead.toLead())
            }

            ProfileUiState.Error("Lead not found")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState.Loading
    )

    fun loadLead(leadId: String) {
        _leadId.value = leadId
    }

    fun contactLead(lead: Lead, platform: String, status: LeadStatus, notes: String) {
        viewModelScope.launch {
            val contacted = ContactedLead(
                contactedId = lead.leadId,
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
                status = status,
                notes = notes,
                contactedAt = System.currentTimeMillis(),
                socialHandleTapped = platform
            )
            contactedRepository.saveContacted(contacted)
            leadsRepository.deleteLead(lead.leadId)
        }
    }

    private fun ContactedLead.toLead() = Lead(
        leadId = contactedId,
        userId = userId,
        businessName = businessName,
        category = category,
        address = address,
        lat = lat,
        lng = lng,
        phone = phone,
        email = email,
        coverImageUrl = coverImageUrl,
        openingHours = openingHours,
        instagram = instagram,
        facebook = facebook,
        tiktok = tiktok,
        whatsapp = whatsapp,
        hasWebsite = hasWebsite,
        websiteUrl = websiteUrl,
        rating = rating,
        reviewCount = reviewCount,
        weightedScore = weightedScore,
        savedAt = contactedAt,
        synced = synced
    )
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val lead: Lead) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
