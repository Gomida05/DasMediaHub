package com.das.mediaHub.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.das.mediaHub.navigation.AppBackStack

data class FAQItem(
    val id: String,
    val icon: ImageVector,
    val question: String,
    val answer: String
)

@Composable
fun HelpScreen(backStack: AppBackStack) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val faqs = retain { getFAQs() }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeContent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Help & Support",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->

        LazyVerticalGrid(
            columns = GridCells.Adaptive(340.dp),
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .background(aboutBackgroundBrush()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item( span = { GridItemSpan(maxLineSpan) } ) {
                HelpHeroCard()
            }

            items(items = faqs, key = { it.id }) { faq ->
                FAQCard(faq)
            }

            item {

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                            alpha = 0.45f
                        )
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Quick tip",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "For the best experience, make sure your download folders are set correctly in Settings before saving large audio or video files.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

fun getFAQs(): List<FAQItem> {
    return listOf(
        FAQItem(
            id = "supported_platforms",
            icon = Icons.Default.Verified,
            question = "What platforms are supported?",
            answer = "DasMediaHub currently supports content discovery and downloads from YouTube, TikTok, and Instagram. We are constantly working to add support for more platforms."
        ),
        FAQItem(
            id = "download_media",
            icon = Icons.Default.Download,
            question = "How do I download media?",
            answer = "Search for a video or paste a supported link. Once the media is loaded, use the download button to choose your preferred format (Video or Audio) and quality to start saving it."
        ),
        FAQItem(
            id = "social_media",
            icon = Icons.Default.Link,
            question = "How do I save Reels or TikToks?",
            answer = "Simply copy the link of the Reel or TikTok video from its original app, paste it into the respective downloader screen in DasMediaHub, and tap 'Analyze Link' to prepare it for download."
        ),
        FAQItem(
            id = "pause_resume",
            icon = Icons.Default.Pause,
            question = "Can I pause or resume downloads?",
            answer = "Yes. Active downloads can be managed in the Library section. You can pause any ongoing download and resume it at any time, or cancel it if you no longer need it."
        ),
        FAQItem(
            id = "save_location",
            icon = Icons.Default.Folder,
            question = "Where are my files saved?",
            answer = "By default, files are saved to your device's Media folder. You can customize the specific save locations for Audio and Video in Settings > Storage."
        ),
        FAQItem(
            id = "background_playback",
            icon = Icons.Default.Headphones,
            question = "Does background playback work?",
            answer = "Yes. DasMediaHub supports background audio playback. Your media will continue playing even if you lock your screen or switch to another application."
        ),
        FAQItem(
            id = "picture_in_picture",
            icon = Icons.Default.LiveTv,
            question = "How do I use Picture-in-Picture?",
            answer = "While a video is playing, simply return to your home screen. The player will automatically shrink into a floating window, allowing you to multitask."
        ),
        FAQItem(
            id = "download_quality",
            icon = Icons.Default.HighQuality,
            question = "How do I change download quality?",
            answer = "When you initiate a download, you'll be presented with several quality options (e.g., 360p, 720p, 1080p). High-resolution options are available depending on the source video."
        ),
        FAQItem(
            id = "audio_only",
            icon = Icons.Default.MusicNote,
            question = "Can I download just the audio?",
            answer = "Yes. When choosing download options, select 'Audio' or 'MP3' to save only the sound file. This is perfect for building your offline music library."
        ),
        FAQItem(
            id = "privacy_data",
            icon = Icons.Default.Security,
            question = "Is my data private?",
            answer = "Absolutely. DasMediaHub does not collect or store your personal data on external servers. Your history and preferences are stored locally on your device and are never shared."
        ),
        FAQItem(
            id = "clear_history",
            icon = Icons.Default.History,
            question = "Can I clear my watch history?",
            answer = "Yes. You can clear your entire history in Settings > Privacy, or remove individual items by long-pressing them in the Library history section."
        ),
        FAQItem(
            id = "offline_mode",
            icon = Icons.Default.CloudOff,
            question = "Can I use the app offline?",
            answer = "You can access and play all your previously downloaded media without an internet connection. Search and download features require an active connection."
        ),
        FAQItem(
            id = "dark_mode",
            icon = Icons.Default.Palette,
            question = "Does the app support dark mode?",
            answer = "Yes. DasMediaHub fully supports Light and Dark modes. On Android 12+, it also supports Dynamic Color to match your system's theme."
        ),
        FAQItem(
            id = "app_update",
            icon = Icons.Default.Update,
            question = "How do I update the app?",
            answer = "You can check for the latest version in Settings > About. If an update is available, you can download and install it directly from there."
        ),
        FAQItem(
            id = "report_issue",
            icon = Icons.Default.BugReport,
            question = "How can I report an issue?",
            answer = "If you encounter a bug or have a suggestion, please use the 'Send Feedback' option in Settings. Your input helps us make DasMediaHub better!"
        )
    )
}

@Composable
fun FAQCard(faq: FAQItem) {
    var expanded by retain { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")

    Card(
        onClick =  { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = faq.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpHeroCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Box(
                    modifier = Modifier.padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Need help getting started?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Here are quick answers to the most common questions about downloads, saved files, playback, and support.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}
