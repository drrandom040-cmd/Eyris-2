package com.elsewhere.eyris.ui.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsewhere.eyris.data.repositories.ContactedRepository
import com.elsewhere.eyris.data.repositories.LeadsRepository
import com.elsewhere.eyris.domain.models.ContactedLead
import com.elsewhere.eyris.domain.models.Lead
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LeadsViewModel @Inject constructor(
    private val leadsRepository: LeadsRepository,
    private val contactedRepository: ContactedRepository
) : ViewModel() {

    val uiState: StateFlow<LeadsUiState> = combine(
        leadsRepository.getAllLeads(),
        contactedRepository.getAllContacted()
    ) { leads, contacted ->
        LeadsUiState.Success(leads, contacted) as LeadsUiState
    }.catch { e ->
        emit(LeadsUiState.Error(e.message ?: "Unknown error"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LeadsUiState.Loading
    )
}

sealed interface LeadsUiState {
    data object Loading : LeadsUiState
    data class Success(
        val leads: List<Lead>,
        val contactedLeads: List<ContactedLead>
    ) : LeadsUiState
    data class Error(val message: String) : LeadsUiState
}
