package com.das.mediaHub.ui.players.videoPlayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.openCustomTab


@Composable
fun ShowDescriptionDialog(
    text: String,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    val linkColor = Color(0xFF0000FF)

    val urlPattern = """https?://\S+""".toRegex()

    val hashtagPattern = """(?<=\s|^)#\w+""".toRegex()
    val mentionPattern = """(?<=\s|^)@\w+""".toRegex()

    val annotation = remember(text) {
        val builder = AnnotatedString.Builder(text)

        // URLs
        urlPattern.findAll(text).forEach { match ->
            val url = match.value

            builder.addLink(
                LinkAnnotation.Clickable(
                    tag = url,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    linkInteractionListener = {
                        context.openCustomTab(url.toUri())
                    }
                ),
                match.range.first,
                match.range.last + 1
            )
        }

        // Hashtags
        hashtagPattern.findAll(text).forEach { match ->
            val hashtag = match.value

            builder.addLink(
                LinkAnnotation.Clickable(
                    tag = hashtag,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor
                        )
                    ),
                    linkInteractionListener = {
                        context.openCustomTab(
                            "https://www.youtube.com/results?search_query=${hashtag.removePrefix("#")}".toUri()
                        )
                    }
                ),
                match.range.first,
                match.range.last + 1
            )
        }

        // Mentions
        mentionPattern.findAll(text).forEach { match ->
            val mention = match.value

            builder.addLink(
                LinkAnnotation.Clickable(
                    tag = mention,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor
                        )
                    ),
                    linkInteractionListener = {
                        context.openCustomTab(
                            "https://www.youtube.com/${mention.removePrefix("@")}".toUri()
                        )
                    }
                ),
                match.range.first,
                match.range.last + 1
            )
        }

        builder.toAnnotatedString()
    }


    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),

    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                // Title
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Divider feel (subtle)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Content
                SelectionContainer {
                    Box(
                        modifier = Modifier
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = annotation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action row (aligned right like modern dialogs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
