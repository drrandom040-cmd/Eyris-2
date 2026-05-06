package Com.elsewhere.eyris.ui.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import Com.elsewhere.eyris.data.repositories.ContactedRepository
import Com.elsewhere.eyris.data.repositories.LeadsRepository
import Com.elsewhere.eyris.domain.models.ContactedLead
import Com.elsewhere.eyris.domain.models.Lead
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

    private val _leads = MutableStateFlow<List<Lead>>(emptyList())
    val leads: StateFlow<List<Lead>> = _leads

    private val _contactedLeads = MutableStateFlow<List<ContactedLead>>(emptyList())
    val contactedLeads: StateFlow<List<ContactedLead>> = _contactedLeads

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _leads.value = leadsRepository.getLeads()
                _contactedLeads.value = contactedRepository.getContactedLeads()
            } catch (e: Exception) {
                // Error handling
            }
        }
    }
}
