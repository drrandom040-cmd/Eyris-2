package com.elsewhere.eyris.ui.export

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsewhere.eyris.data.repositories.ContactedRepository
import com.elsewhere.eyris.domain.models.ContactedLead
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val contactedRepository: ContactedRepository
) : ViewModel() {

    private val _leads = MutableStateFlow<List<ContactedLead>>(emptyList())
    val leads: StateFlow<List<ContactedLead>> = _leads

    init {
        viewModelScope.launch {
            contactedRepository.getAllContacted().collect {
                _leads.value = it
            }
        }
    }

    fun generateCsv(context: Context): File? {
        val leadsToExport = _leads.value
        if (leadsToExport.isEmpty()) return null

        val fileName = "Eyris_Leads_Export_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
        val file = File(context.cacheDir, fileName)

        try {
            FileOutputStream(file).use { out ->
                val header = "Business Name,Category,Address,Phone,Email,Status,Contacted At,Notes\n"
                out.write(header.toByteArray())

                leadsToExport.forEach { lead ->
                    val row = "\"${lead.businessName}\",\"${lead.category}\",\"${lead.address}\",\"${lead.phone ?: ""}\",\"${lead.email ?: ""}\",\"${lead.status}\",\"${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(lead.contactedAt))}\",\"${lead.notes.replace("\"", "\"\"")}\"\n"
                    out.write(row.toByteArray())
                }
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val leads by viewModel.leads.collectAsState()

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
            Text(
                "Export your contacted businesses to CSV.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Currently supporting CSV export for all contacted leads.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ready to export: ${leads.size} leads", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val file = viewModel.generateCsv(context)
                            if (file != null) {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share CSV"))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = leads.isNotEmpty()
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download CSV")
                    }
                }
            }

            if (leads.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "You haven't contacted any leads yet. Move leads to 'Contacted' to export them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
