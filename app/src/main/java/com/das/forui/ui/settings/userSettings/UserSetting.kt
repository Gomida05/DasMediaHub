package com.das.forui.ui.settings.userSettings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingComposable(
    navController: NavController
){

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
        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        ) {

            item { DarkModeToggle() }
            item { LanguageSelector() }
        }
        }
    }

}




@Composable
fun DarkModeToggle() {
    var isDarkTheme by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
            contentDescription = null
        )
        Spacer(Modifier.width(16.dp))
        Text(if (isDarkTheme) "Dark Mode" else "Light Mode", modifier = Modifier.weight(1f))
        Switch(
            checked = isDarkTheme,
            onCheckedChange = {
                isDarkTheme = it

                // Apply theme change logic here
            }
        )
    }
}


@Composable
fun LanguageSelector() {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("English", "French", "Spanish")
    var selectedLanguage by remember { mutableStateOf(languages[0]) }

    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Language: $selectedLanguage", modifier = Modifier.clickable { expanded = true })
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { lang ->
                DropdownMenuItem(
                    text = {
                        Text(lang)
                    },
                    onClick = {
                        selectedLanguage = lang
                        expanded = false
                        // Persist language preference
                    })
            }
        }
    }
}

