package com.das.forui.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.das.forui.Screen.Searcher
import com.das.forui.Screen.Downloads




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageComposable(navController: NavController) {
    val mContext = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "YouTube Downloader",
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    Button(
                        onClick = {
                            Toast.makeText(mContext, "Coming soon!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(72.dp, 48.dp),
                        shape = RoundedCornerShape(35)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile"
                        )
                    }
                },
                actions = {

                    Button(
                        onClick = {
                            navController.navigate(Downloads.route)
                        },
                        modifier = Modifier
                            .size(72.dp, 48.dp),
                        shape = RoundedCornerShape(35)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Downloads"
                        )
                    }
                }
            )
        }
    )
    {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
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