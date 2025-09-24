package com.example.sheandsoul_nick.features.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
// Make sure these imports are present
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
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
                            // ✅ Title is now part of content (as # ...)
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
@Composable
fun FormattedArticleContent(content: String) {
    val lines = content.lines()

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lines.forEach { line ->
            val trimmedLine = line.trim()
            when {
                // Handle #, ##, ###, ####
                trimmedLine.startsWith("#") -> {
                    var level = 0
                    var i = 0
                    while (i < trimmedLine.length && trimmedLine[i] == '#') {
                        level++
                        i++
                    }

                    // Must have at least one space after #
                    val hasSpaceAfter = i < trimmedLine.length && trimmedLine[i] == ' '
                    if (level in 1..6 && hasSpaceAfter) {
                        val headingText = trimmedLine.substring(i + 1).trim()

                        val headingStyle = when (level) {
                            1 -> Triple(24.sp, FontWeight.Bold, Color.Black)
                            2 -> Triple(22.sp, FontWeight.SemiBold, Color.DarkGray)
                            3 -> Triple(20.sp, FontWeight.Medium, Color.DarkGray)
                            4 -> Triple(18.sp, FontWeight.Normal, Color.DarkGray)
                            else -> Triple(16.sp, FontWeight.Normal, Color.Gray)
                        }

                        val padding = when (level) {
                            1 -> Pair(20.dp, 12.dp)
                            2 -> Pair(16.dp, 10.dp)
                            3 -> Pair(12.dp, 8.dp)
                            4 -> Pair(10.dp, 6.dp)
                            else -> Pair(8.dp, 4.dp)
                        }

                        Text(
                            text = headingText,
                            fontSize = headingStyle.first,
                            fontWeight = headingStyle.second,
                            color = headingStyle.third,
                            modifier = Modifier.padding(top = padding.first, bottom = padding.second)
                        )
                    } else {
                        // Not a valid heading (e.g., "###no space"), treat as paragraph
                        renderParagraph(trimmedLine)
                    }
                }
                trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ") -> {
                    val listItemText = trimmedLine.substring(2).trim()
                    RichText(
                        text = listItemText,
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        lineHeight = 24.sp
                    )
                }
                trimmedLine.isNotEmpty() -> {
                    renderParagraph(trimmedLine)
                }
                else -> {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun renderParagraph(text: String) {
    RichText(
        text = text,
        fontSize = 16.sp,
        color = Color.DarkGray,
        lineHeight = 24.sp
    )
}

// ✅ Helper Functions Below

fun parseMarkdownBold(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val regex = Regex("""(\*\*|__)(.*?)\1""")
    var lastIndex = 0

    for (match in regex.findAll(text)) {
        if (match.range.first > lastIndex) {
            builder.append(text.substring(lastIndex, match.range.first))
        }
        val boldContent = match.groupValues[2]
        builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(boldContent)
        }
        lastIndex = match.range.last + 1
    }

    if (lastIndex < text.length) {
        builder.append(text.substring(lastIndex))
    }

    return builder.toAnnotatedString()
}

@Composable
fun RichText(
    text: String,
    fontSize: TextUnit = 16.sp,
    color: Color = Color.DarkGray,
    lineHeight: TextUnit = 24.sp
) {
    val annotated = parseMarkdownBold(text)
    Text(
        text = annotated,
        fontSize = fontSize,
        color = color,
        lineHeight = lineHeight
    )
}