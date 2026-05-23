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

package at.co.schwaerzler.maximilian.doit.data

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.co.schwaerzler.maximilian.doit.DoItApplication
import at.co.schwaerzler.maximilian.doit.R
import at.co.schwaerzler.maximilian.doit.util.AppThemeMode
import at.co.schwaerzler.maximilian.doit.util.appPreferencesDataStore
import at.co.schwaerzler.maximilian.doit.util.setTheme
import at.co.schwaerzler.maximilian.doit.util.themeFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/** ViewModel for the settings screen, owning theme preference, locale, and app-version state. */
class SettingsViewModel(
    application: Application,
    private val appPreferences: DataStore<Preferences>
) : ViewModel() {

    val themeMode = appPreferences.themeFlow()

    val versionName: String? =
        application.packageManager.getPackageInfo(application.packageName, 0).versionName

    private val _currentLocaleTag = MutableStateFlow(run {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) "" else locales.toLanguageTags().split(",").first().trim()
    })
    val currentLocaleTag: StateFlow<String> = _currentLocaleTag.asStateFlow()

    val supportedLocales: List<Pair<String, String>> =
        application.resources.getStringArray(R.array.supported_locales).map { tag ->
            val locale = Locale.forLanguageTag(tag)
            tag to locale.getDisplayName(locale).replaceFirstChar { it.uppercaseChar() }
        }

    fun setTheme(mode: AppThemeMode) {
        viewModelScope.launch {
            appPreferences.setTheme(mode)
        }
    }

    fun setLanguage(tag: String) {
        AppCompatDelegate.setApplicationLocales(
            if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(tag)
        )
        _currentLocaleTag.value = tag
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as DoItApplication
                SettingsViewModel(
                    application = app,
                    appPreferences = app.appPreferencesDataStore
                )
            }
        }
    }
}