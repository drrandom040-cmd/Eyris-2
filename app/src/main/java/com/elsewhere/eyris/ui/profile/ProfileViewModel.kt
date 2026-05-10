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

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun loadLead(leadId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val leads = leadsRepository.getLeads()
                val lead = leads.find { it.leadId == leadId }
                if (lead != null) {
                    _uiState.value = ProfileUiState.Success(lead)
                } else {
                    // Check in contacted repository too
                    val contactedLeads = contactedRepository.getContactedLeads()
                    val contactedAsLead = contactedLeads.find { it.contactedId == leadId }?.let {
                        Lead(
                            leadId = it.contactedId,
                            userId = it.userId,
                            businessName = it.businessName,
                            category = it.category,
                            address = it.address,
                            lat = it.lat,
                            lng = it.lng,
                            phone = it.phone,
                            email = it.email,
                            coverImageUrl = it.coverImageUrl,
                            openingHours = it.openingHours,
                            instagram = it.instagram,
                            facebook = it.facebook,
                            tiktok = it.tiktok,
                            whatsapp = it.whatsapp,
                            hasWebsite = it.hasWebsite,
                            websiteUrl = it.websiteUrl,
                            rating = it.rating,
                            reviewCount = it.reviewCount,
                            weightedScore = it.weightedScore
                        )
                    }
                    if (contactedAsLead != null) {
                        _uiState.value = ProfileUiState.Success(contactedAsLead)
                    } else {
                        _uiState.value = ProfileUiState.Error("Lead not found")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun contactLead(lead: Lead, platform: String, status: ContactStatus, notes: String) {
        viewModelScope.launch {
            try {
                val contacted = ContactedLead(
                    contactedId = lead.leadId, // Keep same ID for consistency
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
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Failed to move lead: ${e.message}")
            }
        }
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val lead: Lead) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
