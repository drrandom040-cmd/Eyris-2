package com.elsewhere.eyris.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsewhere.eyris.data.repositories.ContactedRepository
import com.elsewhere.eyris.data.repositories.LeadsRepository
import com.elsewhere.eyris.domain.models.ContactedLead
import com.elsewhere.eyris.domain.models.Lead
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val leadsRepository: LeadsRepository,
    private val contactedRepository: ContactedRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        leadsRepository.getAllLeads(),
        contactedRepository.getAllContacted()
    ) { leads, contacted ->
        val user = auth.currentUser
        DashboardUiState.Success(
            userName = user?.displayName ?: "Prospector",
            userPhotoUrl = user?.photoUrl?.toString(),
            totalLeads = leads.size,
            contactedCount = contacted.size,
            recentLeads = leads.take(5),
            topRankedLeads = leads.sortedByDescending { it.weightedScore }.take(5)
        ) as DashboardUiState
    }.catch { e ->
        emit(DashboardUiState.Error(e.message ?: "Unknown error"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )
}

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val userName: String,
        val userPhotoUrl: String?,
        val totalLeads: Int,
        val contactedCount: Int,
        val recentLeads: List<Lead>,
        val topRankedLeads: List<Lead>
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
