package com.das.forui.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.das.forui.NavScreens.Searcher
import com.das.forui.NavScreens.Downloads



@Composable
fun HomePageComposable(navController: NavController) {
    val mContext = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "YouTube Downloader",
                        style = MaterialTheme.typography.headlineMedium
                            .copy(textAlign = TextAlign.Center),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Toast.makeText(mContext, "Coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile"
                        )
                    }
                },
                actions = {

                    IconButton(
                        onClick = {
                            navController.navigate(Downloads.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Downloads"
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {


            Button(
                onClick = {
                    navController.navigate(Searcher.route)
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(325.dp, 60.dp),
                shape = RoundedCornerShape(35),
                elevation = ButtonDefaults.elevatedButtonElevation()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.Search),
                        "",
                        modifier = Modifier
                            .align(Alignment.CenterStart)

                    )

                    Text(
                        text = "Search or Insert URL",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }

            }

        }

    }

}