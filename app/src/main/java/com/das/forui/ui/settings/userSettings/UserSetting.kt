package com.das.forui.ui.settings.userSettings

import android.content.Context
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.das.forui.databased.ThemePreferences.loadDarkModeState
import com.das.forui.databased.ThemePreferences.saveDarkMode
import com.das.forui.objectsAndData.ForUIDataClass.ThemePreference


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingComposable(
    navController: NavController
){

    val context = LocalContext.current

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

            item { DarkModeToggleWithPrefs(context) }
            item { LanguageSelector() }
        }
        }
    }

}


@Composable
fun DarkModeDropdown(
    currentPreference: ThemePreference,
    onPreferenceSelected: (ThemePreference) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (currentPreference) {
                    ThemePreference.DARK -> Icons.Default.DarkMode
                    ThemePreference.LIGHT -> Icons.Default.LightMode
                    ThemePreference.SYSTEM -> Icons.Default.Contrast
                },
                contentDescription = null
            )
            Spacer(Modifier.width(16.dp))
            Text(
                when (currentPreference) {
                    ThemePreference.DARK -> "Dark Mode"
                    ThemePreference.LIGHT -> "Light Mode"
                    ThemePreference.SYSTEM -> "Follow System"
                },
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Follow System") },
                onClick = {
                    onPreferenceSelected(ThemePreference.SYSTEM)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Light Mode") },
                onClick = {
                    onPreferenceSelected(ThemePreference.LIGHT)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Dark Mode") },
                onClick = {
                    onPreferenceSelected(ThemePreference.DARK)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun DarkModeToggle(
    isDarkTheme: Boolean,
    onToggle: (Boolean) -> Unit) {

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
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
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun DarkModeToggleWithPrefs(context: Context) {

    var isDarkTheme by loadDarkModeState(context)

    DarkModeToggle(
        isDarkTheme = isDarkTheme,
        onToggle = { newValue ->
            isDarkTheme = newValue
            saveDarkMode(context, newValue)
        }
    )
}

//@Composable
//fun DarkModeDropdownWithPrefs(context: Context) {
//    var preference by loadDarkModeState(context)
//
//    DarkModeDropdown(
//        currentPreference = preference,
//        onPreferenceSelected = { newPref ->
//            preference = newPref
//            saveDarkMode(context, newPref)
//        }
//    )
//}

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

