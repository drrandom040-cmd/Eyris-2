package com.elsewhere.eyris.ui.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsewhere.eyris.data.repositories.ContactedRepository
import com.elsewhere.eyris.data.repositories.LeadsRepository
import com.elsewhere.eyris.domain.models.ContactedLead
import com.elsewhere.eyris.domain.models.Lead
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeadsViewModel @Inject constructor(
    private val leadsRepository: LeadsRepository,
    private val contactedRepository: ContactedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LeadsUiState>(LeadsUiState.Loading)
    val uiState: StateFlow<LeadsUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = LeadsUiState.Loading
            try {
                val leads = leadsRepository.getLeads()
                val contactedLeads = contactedRepository.getContactedLeads()
                _uiState.value = LeadsUiState.Success(leads, contactedLeads)
            } catch (e: Exception) {
                _uiState.value = LeadsUiState.Error(e.message ?: "Failed to load pipeline data")
            }
        }
    }
}

sealed class LeadsUiState {
    object Loading : LeadsUiState()
    data class Success(val leads: List<Lead>, val contactedLeads: List<ContactedLead>) : LeadsUiState()
    data class Error(val message: String) : LeadsUiState()
}
