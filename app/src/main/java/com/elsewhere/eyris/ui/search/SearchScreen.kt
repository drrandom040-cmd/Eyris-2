package com.elsewhere.eyris.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elsewhere.eyris.domain.models.Lead

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (e.g. Cafe)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.search(location, category) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search Leads")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is SearchUiState.Loading -> CircularProgressIndicator()
            is SearchUiState.Success -> {
                LazyColumn {
                    items(state.leads) { lead ->
                        LeadItem(lead)
                    }
                }
            }
            is SearchUiState.Error -> Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }
}

@Composable
fun LeadItem(lead: Lead) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = lead.businessName, style = MaterialTheme.typography.titleLarge)
            Text(text = lead.address, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Rating: ${lead.rating} (${lead.reviewCount} reviews)", style = MaterialTheme.typography.labelSmall)
            Text(text = "Weighted Score: ${"%.2f".format(lead.weightedScore)}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
