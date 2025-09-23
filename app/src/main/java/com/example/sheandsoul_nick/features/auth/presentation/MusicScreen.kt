package com.example.sheandsoul_nick.features.auth.presentation

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.data.remote.ApiService
import com.example.sheandsoul_nick.data.remote.MusicDto
import com.example.sheandsoul_nick.data.remote.RetrofitClient
import kotlinx.coroutines.launch

// ======================================================================
// 1. Data Models (DTO for API, UI Model for the screen)
// ======================================================================

data class MusicTrack(
    val id: Long,
    val title: String,
    val audioUrl: String,
    val producer: String = "She&Soul", // Default value
    val imageUrl: String = "https://placehold.co/100x100/A070D0/FFFFFF?text=Music", // Default placeholder
    val gradientColors: List<Color> = listOf(Color(0xFFE0BBFF), Color(0xFFC39BE0))
)

// ======================================================================
// 2. ViewModel with Audio Playback Logic and API Fetching
// ======================================================================

class MusicViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val apiService: ApiService = RetrofitClient.getInstance(authViewModel)
    private var mediaPlayer: MediaPlayer? = null

    // State for the list of music tracks
    private val _musicTracks = mutableStateOf<List<MusicTrack>>(emptyList())
    val musicTracks: State<List<MusicTrack>> = _musicTracks

    // State to indicate if data is currently loading
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    // State to hold any error messages
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // --- ✅ STATE REFACTORED FOR PLAYER STATE ---
    // Holds the track that is loaded in the player (playing or paused)
    private val _activeTrack = mutableStateOf<MusicTrack?>(null)
    val activeTrack: State<MusicTrack?> = _activeTrack

    // Tracks if the player is actually playing audio
    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying

    // New state to track the buffering track
    private val _loadingTrackId = mutableStateOf<Long?>(null)
    val loadingTrackId: State<Long?> = _loadingTrackId

    init {
        fetchMusicTracks()
    }

    private fun fetchMusicTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Assuming you have fixed ApiService to return Response<List<MusicDto>>
                val response = apiService.getMusic()
                if (response.isSuccessful && response.body() != null) {
                    _musicTracks.value = response.body()!!.map { dto ->
                        MusicTrack(
                            id = dto.id,
                            title = dto.title,
                            audioUrl = dto.audioUrl
                        )
                    }
                } else {
                    _errorMessage.value = "Failed to load music: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ --- LOGIC UPDATED FOR BETTER STATE MANAGEMENT ---
    fun onPlayPause(track: MusicTrack) {
        // Case 1: The tapped track is the one already active in the player
        if (_activeTrack.value?.id == track.id) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
            } else {
                mediaPlayer?.start()
                _isPlaying.value = true
            }
        }
        // Case 2: A new track is selected
        else {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            _loadingTrackId.value = track.id // Use the loading indicator

            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(track.audioUrl)
                    prepareAsync()
                    setOnPreparedListener {
                        start()
                        _activeTrack.value = track
                        _isPlaying.value = true
                        _loadingTrackId.value = null // Clear loading state
                    }
                    setOnCompletionListener {
                        _activeTrack.value = null
                        _isPlaying.value = false
                    }
                    setOnErrorListener { _, _, _ ->
                        _errorMessage.value = "Error playing audio."
                        _activeTrack.value = null
                        _isPlaying.value = false
                        _loadingTrackId.value = null // Clear loading state on error
                        true
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Could not play audio: ${e.message}"
                    _loadingTrackId.value = null // Clear loading state on exception
                }
            }
        }
    }

    override fun onCleared() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onCleared()
    }
}

// ======================================================================
// 3. ViewModel Factory
// ======================================================================

class MusicViewModelFactory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ======================================================================
// 4. Main Screen Composable (Stateful)
// ======================================================================
@Composable
fun MusicScreen(
    authViewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToArticles: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    val musicViewModel: MusicViewModel = viewModel(factory = MusicViewModelFactory(authViewModel))
    val musicTracks by musicViewModel.musicTracks
    val isLoading by musicViewModel.isLoading
    val errorMessage by musicViewModel.errorMessage

    // ✅ Get the new state variables
    val activeTrack by musicViewModel.activeTrack
    val isPlaying by musicViewModel.isPlaying
    val loadingTrackId by musicViewModel.loadingTrackId

    errorMessage?.let {
        // You can show a Toast or Snackbar here
    }

    MusicScreenContent(
        musicTracks = musicTracks,
        isLoading = isLoading,
        // ✅ Pass the new state down
        activeTrack = activeTrack,
        isPlaying = isPlaying,
        loadingTrackId = loadingTrackId,
        onPlayPause = { musicViewModel.onPlayPause(it) },
        onNavigateToHome = onNavigateToHome,
        onNavigateToArticles = onNavigateToArticles,
        onNavigateToCommunity = onNavigateToCommunity,
        onNavigateToProfile = onNavigateToProfile
    )
}

// ======================================================================
// 5. UI Content Composable (Stateless)
// ======================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreenContent(
    musicTracks: List<MusicTrack>,
    isLoading: Boolean,
    // ✅ Accept the new state variables
    activeTrack: MusicTrack?,
    isPlaying: Boolean,
    loadingTrackId: Long?,
    onPlayPause: (MusicTrack) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToArticles: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        topBar = { MusicTopAppBar() },
        bottomBar = {
            AppBottomNavBar(
                selectedScreen = "Music",
                onNavigateToHome = onNavigateToHome,
                onNavigateToArticles = onNavigateToArticles,
                onNavigateToCommunity = onNavigateToCommunity,
                onNavigateToMusic = { /* Already here */ },
                onNavigateToProfile = onNavigateToProfile
            )
        },
        containerColor = Color(0xFFF8F8FF)
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (musicTracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No music available right now.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(musicTracks) { track ->
                    MusicTrackItem(
                        track = track,
                        // ✅ Update the logic here
                        isPlaying = activeTrack?.id == track.id && isPlaying,
                        isBuffering = loadingTrackId == track.id,
                        onPlayPause = { onPlayPause(track) }
                    )
                }
            }
        }
    }
}

// ======================================================================
// 6. Helper UI Composables
// ======================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicTopAppBar() {
    // ... (This composable remains unchanged)
    TopAppBar(
        title = {
            Text(
                "Relaxing Music",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9092FF)
            )
        },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.ic_user_avtar),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun MusicTrackItem(
    track: MusicTrack,
    isPlaying: Boolean,
    // ✅ Accept the new buffering state
    isBuffering: Boolean,
    onPlayPause: () -> Unit
) {
    val horizontalGradient = Brush.horizontalGradient(colors = track.gradientColors)

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPlayPause() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(horizontalGradient),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ This is the key UI change for the loading indicator
            Box(
                modifier = Modifier.size(56.dp), // Give the box a consistent size for alignment
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = onPlayPause) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(text = track.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = track.producer, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }

            AsyncImage(
                model = track.imageUrl,
                contentDescription = track.title,
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_official_logo),
                error = painterResource(id = R.drawable.ic_official_logo)
            )
        }
    }
}

// ======================================================================
// 7. Preview
// ======================================================================
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MusicScreenPreview() {
    val sampleTracks = listOf(
        MusicTrack(id = 1, title = "Evening Relaxation", audioUrl = "", imageUrl = ""),
        MusicTrack(id = 2, title = "Midnight Rain ASMR", audioUrl = "", imageUrl = "")
    )

    MusicScreenContent(
        musicTracks = sampleTracks,
        isLoading = false,
        activeTrack = sampleTracks.first(), // Preview with an active track
        isPlaying = true, // Preview in a "playing" state
        loadingTrackId = null,
        onPlayPause = { },
        onNavigateToHome = {},
        onNavigateToArticles = {},
        onNavigateToCommunity = {},
        onNavigateToProfile = {}
    )
}