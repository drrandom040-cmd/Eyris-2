package com.elsewhere.eyris.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsewhere.eyris.data.repositories.ContactedRepository
import com.elsewhere.eyris.data.repositories.LeadsRepository
import com.elsewhere.eyris.domain.models.ContactStatus
import com.elsewhere.eyris.domain.models.Lead
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val leadsRepository: LeadsRepository,
    private val contactedRepository: ContactedRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                val userName = user?.displayName?.split(" ")?.firstOrNull() ?: "Prospector"
                val userPhotoUrl = user?.photoUrl?.toString()

                val leads = leadsRepository.getLeads()
                val contactedLeads = contactedRepository.getContactedLeads()
                
                val conversionRate = if (contactedLeads.isNotEmpty()) {
                    (contactedLeads.filter { it.status == ContactStatus.ACCEPTED }.size.toDouble() / contactedLeads.size.toDouble()) * 100.0
                } else 0.0

                _uiState.value = DashboardUiState(
                    userName = userName,
                    userPhotoUrl = userPhotoUrl,
                    leadsCount = leads.size,
                    contactedCount = contactedLeads.size,
                    topLeads = leads.sortedByDescending { it.weightedScore }.take(3),
                    conversionRate = conversionRate
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

data class DashboardUiState(
    val userName: String = "Prospector",
    val userPhotoUrl: String? = null,
    val leadsCount: Int = 0,
    val contactedCount: Int = 0,
    val topLeads: List<Lead> = emptyList(),
    val conversionRate: Double = 0.0
)
