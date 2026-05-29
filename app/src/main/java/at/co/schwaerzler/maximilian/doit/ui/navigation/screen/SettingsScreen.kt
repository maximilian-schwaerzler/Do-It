/*
 * Copyright 2026 Maximilian Schwärzler
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package at.co.schwaerzler.maximilian.doit.ui.navigation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.co.schwaerzler.maximilian.doit.R
import at.co.schwaerzler.maximilian.doit.data.SettingsViewModel
import at.co.schwaerzler.maximilian.doit.ui.component.MaxWidthLayout
import at.co.schwaerzler.maximilian.doit.ui.theme.DoItTheme
import at.co.schwaerzler.maximilian.doit.util.AppThemeMode
import at.co.schwaerzler.maximilian.doit.util.NotificationLeadTime
import at.co.schwaerzler.maximilian.doit.util.openUrl
import java.util.Locale

@Composable
private fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

/**
 * Settings screen showing app version, theme selector, and external links (GitHub, F-Droid).
 *
 * @param onNavigateBack Called when the user taps the back navigation icon.
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentLocaleTag by viewModel.currentLocaleTag.collectAsStateWithLifecycle()
    val useDynamicColors by viewModel.useDynamicColors.collectAsStateWithLifecycle()
    val notificationLeadTime by viewModel.notificationLeadTime.collectAsStateWithLifecycle()
    SettingsScreenContent(
        onNavigateBack = onNavigateBack,
        themeMode = themeMode,
        onSetTheme = viewModel::setTheme,
        versionName = viewModel.versionName,
        currentLocaleTag = currentLocaleTag,
        supportedLocales = viewModel.supportedLocales,
        onSetLanguage = viewModel::setLanguage,
        notificationLeadTime = notificationLeadTime,
        onSetNotificationLeadTime = viewModel::setNotificationLeadTime,
        useDynamicColors = useDynamicColors,
        onSetUseDynamicColors = viewModel::setUseDynamicColor,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    onNavigateBack: () -> Unit,
    themeMode: AppThemeMode,
    onSetTheme: (AppThemeMode) -> Unit,
    versionName: String?,
    currentLocaleTag: String,
    supportedLocales: List<Pair<String, String>>,
    onSetLanguage: (String) -> Unit,
    notificationLeadTime: NotificationLeadTime,
    onSetNotificationLeadTime: (NotificationLeadTime) -> Unit,
    modifier: Modifier = Modifier,
    useDynamicColors: Boolean,
    onSetUseDynamicColors: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val githubUrl = stringResource(R.string.github_repo_url)
    val githubReleasesBaseUrl = stringResource(R.string.github_releases_base_url)
    val fdroidUrl = stringResource(R.string.fdroid_package_url)

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var notifLeadTimeExpanded by remember { mutableStateOf(false) }

    val currentLanguageDisplay = if (currentLocaleTag.isEmpty()) {
        stringResource(R.string.settings_language_system_default)
    } else {
        val locale = Locale.forLanguageTag(currentLocaleTag)
        locale.getDisplayName(locale).replaceFirstChar { it.uppercaseChar() }
    }

    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.settings_app_bar))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(R.drawable.arrow_back_24px), contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        MaxWidthLayout(Modifier.padding(innerPadding)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                SettingsSectionHeader(stringResource(R.string.settings_section_general))
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.theme))
                    },
                    supportingContent = {
                        Text(stringResource(themeMode.labelRes))
                    },
                    leadingContent = {
                        Icon(painterResource(themeMode.iconRes), contentDescription = null)
                    },
                    modifier = Modifier.clickable { showThemeDialog = true }
                )

                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.dynamic_color_setting_label))
                    },
                    leadingContent = {
                        Icon(painterResource(R.drawable.colors_24px), contentDescription = null)
                    },
                    trailingContent = { Switch(useDynamicColors, { onSetUseDynamicColors(it) }) }
                )

                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.settings_language))
                    },
                    supportingContent = {
                        Text(currentLanguageDisplay)
                    },
                    leadingContent = {
                        Icon(painterResource(R.drawable.language_24px), contentDescription = null)
                    },
                    modifier = Modifier.clickable { showLanguageDialog = true }
                )

                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.settings_notif_lead_time))
                    },
                    supportingContent = {
                        ExposedDropdownMenuBox(
                            expanded = notifLeadTimeExpanded,
                            onExpandedChange = { notifLeadTimeExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = stringResource(notificationLeadTime.labelRes),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = notifLeadTimeExpanded)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                            )
                            ExposedDropdownMenu(
                                expanded = notifLeadTimeExpanded,
                                onDismissRequest = { notifLeadTimeExpanded = false }
                            ) {
                                NotificationLeadTime.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(option.labelRes)) },
                                        onClick = {
                                            onSetNotificationLeadTime(option)
                                            notifLeadTimeExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    },
                    leadingContent = {
                        Icon(painterResource(R.drawable.event_24px), contentDescription = null)
                    }
                )
                SettingsSectionHeader(stringResource(R.string.settings_section_about))
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.app_version))
                    },
                    supportingContent = versionName?.let { version -> { Text(version) } },
                    leadingContent = {
                        Icon(painterResource(R.drawable.info_24px), contentDescription = null)
                    },
                    modifier = versionName?.let { version ->
                        Modifier.clickable {
                            context.openUrl("$githubReleasesBaseUrl/v$version")
                        }
                    } ?: Modifier
                )
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.source_code))
                    },
                    leadingContent = {
                        Icon(painterResource(R.drawable.code_24px), contentDescription = null)
                    },
                    modifier = Modifier.clickable { context.openUrl(githubUrl) }
                )
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.f_droid_package))
                    },
                    leadingContent = {
                        Icon(painterResource(R.drawable.fdroid), contentDescription = null)
                    },
                    modifier = Modifier.clickable { context.openUrl(fdroidUrl) }
                )
            }
        }
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentTag = currentLocaleTag,
            supportedLocales = supportedLocales,
            onDismiss = { showLanguageDialog = false },
            onSelectTag = { tag ->
                onSetLanguage(tag)
                showLanguageDialog = false
            }
        )
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            themeMode,
            onDismiss = {
                showThemeDialog = false
            },
            onSelection = { mode ->
                onSetTheme(mode)
                showThemeDialog = false
            }
        )
    }
}

@Composable
private fun LanguagePickerDialog(
    currentTag: String,
    supportedLocales: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSelectTag: (String) -> Unit,
) {
    val systemDefaultLabel = stringResource(R.string.settings_language_system_default)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTag("") }
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = currentTag.isEmpty(), onClick = null)
                    Text(
                        text = systemDefaultLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                supportedLocales.forEach { (tag, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTag(tag) }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = tag == currentTag, onClick = null)
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun ThemePickerDialog(
    currentThemeMode: AppThemeMode,
    onDismiss: () -> Unit,
    onSelection: (AppThemeMode) -> Unit,
) {
    var currentSelection by rememberSaveable {
        mutableStateOf(currentThemeMode)
    }
    AlertDialog(
        icon = {
            Icon(painterResource(R.drawable.colors_24px), contentDescription = null)
        },
        title = {
            Text("Theme")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentSelection = mode }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentSelection == mode, onClick = null)
                        Text(
                            stringResource(mode.labelRes),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSelection(currentSelection) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    DoItTheme {
        SettingsScreenContent(
            onNavigateBack = {},
            themeMode = AppThemeMode.FOLLOW_SYSTEM,
            onSetTheme = {},
            versionName = "1.0.0",
            currentLocaleTag = "",
            supportedLocales = emptyList(),
            onSetLanguage = {},
            notificationLeadTime = NotificationLeadTime.THIRTY_MINUTES,
            onSetNotificationLeadTime = {},
            useDynamicColors = true,
            onSetUseDynamicColors = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenDarkPreview() {
    DoItTheme(darkTheme = true) {
        Surface {
            SettingsScreenContent(
                onNavigateBack = {},
                themeMode = AppThemeMode.DARK,
                onSetTheme = {},
                versionName = "1.0.0",
                currentLocaleTag = "",
                supportedLocales = emptyList(),
                onSetLanguage = {},
                notificationLeadTime = NotificationLeadTime.THIRTY_MINUTES,
                onSetNotificationLeadTime = {},
                useDynamicColors = true,
                onSetUseDynamicColors = {},
            )
        }
    }
}
