package com.elsewhere.eyris.ui.leads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elsewhere.eyris.domain.models.Lead
import com.elsewhere.eyris.domain.models.ContactedLead
import com.elsewhere.eyris.domain.models.ContactStatus
import com.elsewhere.eyris.ui.search.LeadItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsContactedScreen(
    onNavigateToProfile: (String) -> Unit,
    viewModel: LeadsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Leads", "Contacted")
    
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                TopAppBar(
                    title = { Text("CRM Pipeline", fontWeight = FontWeight.Black) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.then(
                                with(TabRowDefaults) {
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                                }
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            when (val state = uiState) {
                is LeadsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LeadsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadData() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is LeadsUiState.Success -> {
                    when (selectedTab) {
                        0 -> LeadsList(state.leads, onNavigateToProfile)
                        1 -> ContactedList(state.contactedLeads, onNavigateToProfile)
                    }
                }
            }
        }
    }
}

@Composable
fun LeadsList(leads: List<Lead>, onNavigateToProfile: (String) -> Unit) {
    if (leads.isEmpty()) {
        EmptyState(message = "No leads saved yet.")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(leads) { lead ->
                LeadItem(
                    lead = lead,
                    onSocialClick = { /* Handled in profile */ },
                    onContactClick = { onNavigateToProfile(lead.leadId) }
                )
            }
        }
    }
}

@Composable
fun ContactedList(contacted: List<ContactedLead>, onNavigateToProfile: (String) -> Unit) {
    var filterStatus by remember { mutableStateOf<ContactStatus?>(null) }
    
    val filteredList = if (filterStatus == null) contacted else contacted.filter { it.status == filterStatus }

    Column {
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = filterStatus == null,
                    onClick = { filterStatus = null },
                    label = { Text("All") },
                    shape = CircleShape
                )
            }
            ContactStatus.values().forEach { status ->
                item {
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text(status.name) },
                        shape = CircleShape
                    )
                }
            }
        }
        
        if (filteredList.isEmpty()) {
            EmptyState(message = "No contacted leads found.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        onClick = { onNavigateToProfile(item.contactedId) }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.businessName, fontWeight = FontWeight.Bold)
                                Text(item.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            StatusBadge(item.status)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ContactStatus) {
    val color = when (status) {
        ContactStatus.ACCEPTED -> Color(0xFF4CAF50)
        ContactStatus.REJECTED -> Color(0xFFF44336)
        ContactStatus.ANSWERED -> Color(0xFF2196F3)
        ContactStatus.GHOSTED -> Color(0xFF9E9E9E)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray)
    }
}
