package com.elsewhere.eyris.ui.export

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Export your leads and contacted businesses to CSV or PDF.", style = MaterialTheme.typography.bodyLarge)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { /* Export CSV */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Export to CSV")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { /* Export PDF */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Export to PDF")
            }
        }
    }
}
