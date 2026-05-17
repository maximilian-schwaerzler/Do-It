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

package at.co.schwaerzler.maximilian.doit.util

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import at.co.schwaerzler.maximilian.doit.R
import kotlinx.serialization.Serializable

/**
 * The three user-selectable theme options for the app.
 *
 * @property labelRes String resource with the localized display name for this mode.
 */
@Serializable
enum class AppThemeMode(@param:StringRes val labelRes: Int, @param:DrawableRes val iconRes: Int) {
    LIGHT(R.string.theme_light, R.drawable.light_mode_24px),
    DARK(R.string.theme_dark, R.drawable.dark_mode_24px),

    /** Defers to the system-wide dark mode setting. */
    FOLLOW_SYSTEM(R.string.theme_follow_system, R.drawable.brightness_auto_24px),
}

/**
 * Returns the current night-mode state reported by [UiModeManager].
 *
 * Maps [UiModeManager.MODE_NIGHT_NO] → [AppThemeMode.LIGHT],
 * [UiModeManager.MODE_NIGHT_YES] → [AppThemeMode.DARK], and anything else
 * (e.g. auto, custom schedule, or unset) → [AppThemeMode.FOLLOW_SYSTEM].
 */
fun Context.getAppTheme(): AppThemeMode {
    val uiModeManager = getSystemService(UiModeManager::class.java)
    Log.d("AppTheme", "Current mode: ${uiModeManager.nightMode}")
    return when (uiModeManager.nightMode) {
        UiModeManager.MODE_NIGHT_NO -> AppThemeMode.LIGHT
        UiModeManager.MODE_NIGHT_YES -> AppThemeMode.DARK
        else -> AppThemeMode.FOLLOW_SYSTEM
    }
}

fun Context.setAppTheme(themeMode: AppThemeMode) {
    Log.d("AppTheme", "Setting mode: $themeMode")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val uiModeManager = getSystemService(UiModeManager::class.java)
        uiModeManager.setApplicationNightMode(
            when (themeMode) {
                AppThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
                AppThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
                AppThemeMode.FOLLOW_SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
            }
        )
    } else {
        AppCompatDelegate.setDefaultNightMode(
            when (themeMode) {
                AppThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
                AppThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
                AppThemeMode.FOLLOW_SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
            }
        )
    }
}