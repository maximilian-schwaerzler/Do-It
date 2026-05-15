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

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import at.co.schwaerzler.maximilian.doit.R
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

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsScreenContent(
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val githubUrl = stringResource(R.string.github_repo_url)
    val fdroidUrl = stringResource(R.string.fdroid_package_url)
    val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentTag: String =
        if (currentLocales.isEmpty) "" else currentLocales.toLanguageTags().split(",").first()
            .trim()

    val supportedLocaleTags = listOf(
        "de",
        "de-AT",
        "de-CH",
        "en",
        "es",
        "fr",
        "it",
        "ko",
        "lt",
        "pl",
        "ro",
        "sr",
        "ta",
        "tr",
        "zh"
    )
    val supportedLocales: List<Pair<String, String>> = remember {
        supportedLocaleTags.map { tag ->
            val locale = Locale.forLanguageTag(tag)
            tag to locale.getDisplayName(locale).replaceFirstChar { it.uppercaseChar() }
        }
    }

    val currentLanguageDisplay = if (currentTag.isEmpty()) {
        stringResource(R.string.settings_language_system_default)
    } else {
        val locale = Locale.forLanguageTag(currentTag)
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
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
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
            SettingsSectionHeader(stringResource(R.string.settings_section_about))
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.app_version))
                },
                supportingContent = versionName?.let { version -> { Text(version) } },
                leadingContent = {
                    Icon(painterResource(R.drawable.info_24px), contentDescription = null)
                }
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

    if (showLanguageDialog) {
        val systemDefaultLabel = stringResource(R.string.settings_language_system_default)
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language_dialog_title)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.getEmptyLocaleList()
                                )
                                showLanguageDialog = false
                            }
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
                                .clickable {
                                    AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.forLanguageTags(tag)
                                    )
                                    showLanguageDialog = false
                                }
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
}
