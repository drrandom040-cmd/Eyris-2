package com.elsewhere.eyris.ui.leads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elsewhere.eyris.domain.models.LeadStatus
import com.elsewhere.eyris.domain.models.Lead
import com.elsewhere.eyris.domain.models.ContactedLead

@Composable
fun LeadsContactedScreen(
    onNavigateToProfile: (String) -> Unit,
    viewModel: LeadsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pipeline", "Contacted")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is LeadsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LeadsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is LeadsUiState.Success -> {
                    if (selectedTabIndex == 0) {
                        PipelineList(state.leads, onNavigateToProfile)
                    } else {
                        ContactedList(state.contactedLeads, onNavigateToProfile)
                    }
                }
            }
        }
    }
}

@Composable
fun PipelineList(leads: List<Lead>, onNavigateToProfile: (String) -> Unit) {
    if (leads.isEmpty()) {
        EmptyState(Icons.Default.Search, "No leads found. Start a search to find new businesses.")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(leads) { lead ->
                LeadCard(lead) { onNavigateToProfile(lead.leadId) }
            }
        }
    }
}

@Composable
fun ContactedList(leads: List<ContactedLead>, onNavigateToProfile: (String) -> Unit) {
    if (leads.isEmpty()) {
        EmptyState(Icons.Default.Contacts, "No contacted businesses. Move leads from the pipeline once contacted.")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(leads) { lead ->
                ContactedCard(lead) { onNavigateToProfile(lead.contactedId) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadCard(lead: Lead, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        lead.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Score: ${String.format("%.1f", lead.weightedScore)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(lead.businessName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(lead.address, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactedCard(lead: ContactedLead, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(lead.status)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    lead.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(lead.businessName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (lead.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(lead.notes, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
    }
}

@Composable
fun StatusBadge(status: LeadStatus) {
    val color = when (status) {
        LeadStatus.ACCEPTED -> Color(0xFF4CAF50)
        LeadStatus.REJECTED -> Color(0xFFF44336)
        LeadStatus.GHOSTED -> Color(0xFF9E9E9E)
        LeadStatus.ANSWERED -> Color(0xFF2196F3)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = color)
    ) {
        Text(
            status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun EmptyState(icon: ImageVector, text: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
    }
}
