package Com.elsewhere.eyris.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import Com.elsewhere.eyris.data.repositories.ContactedRepository
import Com.elsewhere.eyris.data.repositories.LeadsRepository
import Com.elsewhere.eyris.domain.models.ContactStatus
import Com.elsewhere.eyris.domain.models.Lead
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val leadsRepository: LeadsRepository,
    private val contactedRepository: ContactedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val leads = leadsRepository.getLeads()
                val contacted = contactedRepository.getContactedLeads()
                
                val conversionRate = if (contacted.isNotEmpty()) {
                    (contacted.filter { it.status == ContactStatus.ACCEPTED }.size.toDouble() / contacted.size.toDouble()) * 100.0
                } else 0.0

                _uiState.value = DashboardUiState(
                    leadsCount = leads.size,
                    contactedCount = contacted.size,
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
    val leadsCount: Int = 0,
    val contactedCount: Int = 0,
    val topLeads: List<Lead> = emptyList(),
    val conversionRate: Double = 0.0
)
