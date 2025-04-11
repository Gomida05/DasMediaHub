package com.das.forui.ui.userSettings

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingComposable(
    navController: NavController
){
    val mContext = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip { Text("Navigate Up") }
                        },
                        state = rememberTooltipState()
                    ) {
                        Button(
                            onClick = {
                                navController.navigateUp()
                            },
                            shape = RoundedCornerShape(22)
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.AutoMirrored.Default.ArrowBack),
                                ""
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)

        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                .align(Alignment.Center)

        ) {



                Button(
                    onClick = {
                        expanded = !expanded

                    },
                    modifier = Modifier
                ) {
                    Text(
                        text = "Change Theme Mode"
                    )
                }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Dark Mode") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            ""
                        )
                    },
                    onClick = {
                        changeTheme(
                            mContext,
                            UiModeManager.MODE_NIGHT_YES
                        )
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Light Mode") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            ""
                        )
                    },
                    onClick = {
                        changeTheme(
                            mContext,
                            UiModeManager.MODE_NIGHT_NO
                        )
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Auto") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Contrast,
                            ""
                        )
                    },
                    onClick = {
                        expanded = false
                        changeTheme(
                            mContext,
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        )
                    }
                )

            }


                Button(
                    onClick = {

                    },
                    modifier = Modifier
                ) {
                    Text(
                        text = "          "
                    )
                }

            }
        }
    }

}




fun changeTheme(
    context: Context,
    setUiMode: Int
): Int {
    val sharedPref: SharedPreferences =
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putInt("isNightModeOn", setUiMode)
        apply()

        AppCompatDelegate.setDefaultNightMode(setUiMode)
    }
    return setUiMode
}