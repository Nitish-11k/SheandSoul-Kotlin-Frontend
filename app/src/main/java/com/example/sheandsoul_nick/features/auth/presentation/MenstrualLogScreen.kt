package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.data.remote.ApiService
import com.example.sheandsoul_nick.data.remote.MenstrualCycleLogDto
import com.example.sheandsoul_nick.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// --- ViewModel for fetching logs ---
sealed class LogUiState {
    object Loading : LogUiState()
    data class Success(val logs: List<MenstrualCycleLogDto>) : LogUiState()
    data class Error(val message: String) : LogUiState()
}

class MenstrualLogViewModel(authViewModel: AuthViewModel) : ViewModel() {
    private val apiService: ApiService = RetrofitClient.getInstance(authViewModel)
    private val _uiState = MutableStateFlow<LogUiState>(LogUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchLogs()
    }

    private fun fetchLogs() {
        viewModelScope.launch {
            _uiState.value = LogUiState.Loading
            try {
                val response = apiService.getMenstrualLogs()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = LogUiState.Success(response.body()!!)
                } else {
                    _uiState.value = LogUiState.Error("Failed to load logs.")
                }
            } catch (e: Exception) {
                _uiState.value = LogUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
}

class MenstrualLogViewModelFactory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenstrualLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenstrualLogViewModel(authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// --- UI for the Log Screen ---
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenstrualLogScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val logViewModel: MenstrualLogViewModel = viewModel(factory = MenstrualLogViewModelFactory(authViewModel))
    val uiState by logViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_sheandsoul_text),
                        contentDescription = "She & Soul Logo",
                        modifier = Modifier.width(130.dp)
                        .height(30.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is LogUiState.Loading -> CircularProgressIndicator()
                is LogUiState.Error -> Text(state.message, color = Color.Red, textAlign = TextAlign.Center)
                is LogUiState.Success -> {
                    if (state.logs.isEmpty()) {
                        Text("No past cycle logs found.", textAlign = TextAlign.Center)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    "Your Cycle History",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(state.logs) { log ->
                                LogEntryCard(log = log)
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogEntryCard(log: MenstrualCycleLogDto) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val startDate = LocalDate.parse(log.periodStartDate).format(dateFormatter)
    val endDate = LocalDate.parse(log.periodEndDate).format(dateFormatter)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Period: $startDate - $endDate",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoColumn("Period Length", "${log.periodLength} Days")
                InfoColumn("Cycle Length", "${log.cycleLength} Days")
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}