package com.lillytech.aischool.mobile.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lillytech.aischool.core.model.Course
import com.lillytech.aischool.core.model.AiSchoolEndpoints
import com.lillytech.aischool.core.model.AiSchoolPillars
import com.lillytech.aischool.mobile.R
import com.lillytech.aischool.mobile.ui.SyllabusUiState
import com.lillytech.aischool.mobile.ui.SyllabusViewModel

private const val COMPANY_URL = "https://www.lillytechsystems.com"

/**
 * Home screen: the top-level learning tracks (categories), so the catalog opens
 * collapsed and the user picks a track first. The large, centered "ai school"
 * lockup is pinned (stays while the list scrolls); Lilly Tech Systems is shown
 * with its own flower logo in the footer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    viewModel: SyllabusViewModel,
    onCategoryClick: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(topBar = { BrandHeader(context) }) { padding ->
        when (val s = state) {
            is SyllabusUiState.Loading -> CenteredColumn(padding) {
                CircularProgressIndicator()
                Text(stringResource(R.string.loading), modifier = Modifier.padding(top = 16.dp))
            }

            is SyllabusUiState.Error -> CenteredColumn(padding) {
                Text(s.message, style = MaterialTheme.typography.bodyLarge)
                Button(onClick = viewModel::refresh, modifier = Modifier.padding(top = 16.dp)) {
                    Text(stringResource(R.string.retry))
                }
            }

            is SyllabusUiState.Ready -> CategoryList(s.courses, padding, onCategoryClick)
        }
    }
}

/** Large, centered, pinned AI School lockup: big mark + lowercase wordmark + link. */
@Composable
internal fun BrandHeader(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(top = 14.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.brand_mark),
            contentDescription = null,
            modifier = Modifier.size(68.dp).clip(RoundedCornerShape(17.dp)),
        )
        Text(
            text = "ai school",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp).clickable { open(context, AiSchoolEndpoints.BASE_URL) },
        ) {
            Text(
                stringResource(R.string.courses_subtitle),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.open_ai_school_website),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun CategoryList(
    courses: List<Course>,
    contentPadding: PaddingValues,
    onCategoryClick: (String) -> Unit,
) {
    val grouped = courses.groupBy { it.category }
    val ordered =
        AiSchoolPillars.ALL.filter { it in grouped } + (grouped.keys - AiSchoolPillars.ALL.toSet())

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "intro") {
            Text(
                "Choose a track to start learning.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
            )
        }
        items(ordered, key = { it }) { category ->
            val inCat = grouped.getValue(category)
            val lessons = inCat.sumOf { it.lessons.size }
            CategoryCard(category, inCat.size, lessons) { onCategoryClick(category) }
        }
        item(key = "footer") { LillyTechFooter() }
    }
}

@Composable
private fun CategoryCard(category: String, courses: Int, lessons: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = categoryIcon(category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text(
                    category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    "$courses ${if (courses == 1) "course" else "courses"} · $lessons lessons",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LillyTechFooter() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "An AI School product",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Image(
            painter = painterResource(R.drawable.lillytech_logo),
            contentDescription = "Lilly Tech Systems",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(top = 12.dp)
                .height(96.dp)
                .clickable { open(context, COMPANY_URL) },
        )
    }
}

private fun open(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: ActivityNotFoundException) {
    }
}

private fun categoryIcon(category: String) = when {
    category.contains("Tool", ignoreCase = true) -> Icons.Filled.Build
    category.contains("Model", ignoreCase = true) -> Icons.Filled.Memory
    category.contains("Infrastructure", ignoreCase = true) -> Icons.Filled.Memory
    category.contains("Hardware", ignoreCase = true) -> Icons.Filled.Memory
    category.contains("Tuning", ignoreCase = true) -> Icons.Filled.Tune
    else -> Icons.Filled.AutoAwesome
}

@Composable
private fun CenteredColumn(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { content() }
    }
}
