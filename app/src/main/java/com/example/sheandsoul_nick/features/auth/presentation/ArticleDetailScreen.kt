package com.example.sheandsoul_nick.features.auth.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.features.articles.ArticleViewModel
import com.example.sheandsoul_nick.features.articles.ArticleViewModelFactory
import com.example.sheandsoul_nick.features.articles.DataState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: Long,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val articleViewModel: ArticleViewModel = viewModel(
        factory = ArticleViewModelFactory(authViewModel)
    )

    LaunchedEffect(key1 = articleId) {
        articleViewModel.fetchArticleById(articleId)
    }

    val articleState by articleViewModel.selectedArticle.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = articleState) {
                is DataState.Loading -> {
                    CircularProgressIndicator()
                }
                is DataState.Success -> {
                    val article = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        AsyncImage(
                            model = article.imageUrl,
                            contentDescription = article.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_launcher_background),
                            error = painterResource(id = R.drawable.ic_launcher_background)
                        )
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = article.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            // ✅ FIX: Using the new FormattedArticleContent composable
                            FormattedArticleContent(content = article.content ?: "No content available.")
                        }
                    }
                }
                is DataState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                null -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * A composable that takes a raw string and formats it based on Markdown-style hashtags.
 * - Lines starting with "# " become main headings.
 * - Lines starting with "## " become subheadings.
 * - All other lines are treated as regular paragraph text.
 */
@Composable
fun FormattedArticleContent(content: String) {
    val annotatedString = buildAnnotatedString {
        content.lines().forEach { line ->
            when {
                // Style for "# Heading 1" (Higher priority)
                line.startsWith("# ") -> {
                    withStyle(style = SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append(line.removePrefix("# ").trim())
                    }
                    append("\n\n") // Add extra space after main headings
                }
                // Style for "## Heading 2" (Lower priority)
                line.startsWith("## ") -> {
                    withStyle(style = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)) {
                        append(line.removePrefix("## ").trim())
                    }
                    append("\n\n") // Add extra space after subheadings
                }
                // Default style for normal paragraph text
                else -> {
                    withStyle(style = SpanStyle(fontSize = 16.sp, lineHeight = 24.sp, color = Color.DarkGray)) {
                        append(line.trim())
                    }
                    append("\n") // Regular line break for paragraphs
                }
            }
        }
    }
    Text(text = annotatedString)
}

