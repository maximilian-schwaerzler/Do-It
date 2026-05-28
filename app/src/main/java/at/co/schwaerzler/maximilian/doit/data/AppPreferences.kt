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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import at.co.schwaerzler.maximilian.doit.util.AppThemeMode
import at.co.schwaerzler.maximilian.doit.util.NotificationLeadTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppPreferences(
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope
) {
    val themeMode = dataStore.data.map { preferences ->
        preferences[THEME]?.let { themeModeString ->
            try {
                AppThemeMode.valueOf(themeModeString)
            } catch (_: IllegalArgumentException) {
                null
            }
        } ?: AppThemeMode.FOLLOW_SYSTEM
    }.stateIn(scope, SharingStarted.Eagerly, AppThemeMode.FOLLOW_SYSTEM)

    fun saveThemeMode(themeMode: AppThemeMode) = scope.launch {
        dataStore.edit { it[THEME] = themeMode.name }
    }

    val notificationLeadTime =
        dataStore.data.map { prefs ->
            prefs[NOTIFICATION_LEAD_TIME]?.let {
                try {
                    NotificationLeadTime.valueOf(it)
                } catch (_: IllegalArgumentException) {
                    null
                }
            } ?: NotificationLeadTime.THIRTY_MINUTES
        }.stateIn(scope, SharingStarted.Eagerly, NotificationLeadTime.THIRTY_MINUTES)

    fun saveNotificationLeadTime(leadTime: NotificationLeadTime) = scope.launch {
        dataStore.edit { it[NOTIFICATION_LEAD_TIME] = leadTime.name }
    }

    val widgetDialogSuppressed = dataStore.data.map {
        it[DO_NOT_SHOW_WIDGET_PIN_DIALOG] ?: false
    }.stateIn(scope, SharingStarted.Eagerly, false)

    fun saveWidgetDialogSuppressed(suppressed: Boolean) = scope.launch {
        dataStore.edit { it[DO_NOT_SHOW_WIDGET_PIN_DIALOG] = suppressed }
    }

    fun saveNotificationDialogSuppressed(suppressed: Boolean) = scope.launch {
        dataStore.edit { it[DO_NOT_SHOW_NOTIFICATION_PERMISSION_DIALOG] = suppressed }
    }

    val notificationDialogSuppressed = dataStore.data.map {
        it[DO_NOT_SHOW_NOTIFICATION_PERMISSION_DIALOG] ?: false
    }.stateIn(scope, SharingStarted.Eagerly, false)

    val todosDone = dataStore.data.map {
        it[TODOS_DONE_COUNT] ?: 0
    }.stateIn(scope, SharingStarted.Eagerly, 0)

    fun incrementTodosDone() = scope.launch {
        dataStore.edit { it[TODOS_DONE_COUNT] = (it[TODOS_DONE_COUNT] ?: 0) + 1 }
    }

    val useDynamicColors = dataStore.data.map {
        it[USE_DYNAMIC_COLOR] ?: true
    }.stateIn(scope, SharingStarted.Eagerly, true)

    fun saveUseDynamicColors(use: Boolean) = scope.launch {
        dataStore.edit { it[USE_DYNAMIC_COLOR] = use }
    }

    companion object {
        val THEME = stringPreferencesKey("theme")
        val NOTIFICATION_LEAD_TIME = stringPreferencesKey("notification_lead_time")
        val DO_NOT_SHOW_WIDGET_PIN_DIALOG = booleanPreferencesKey("do_not_show_widget_pin_dialog")
        val DO_NOT_SHOW_NOTIFICATION_PERMISSION_DIALOG =
            booleanPreferencesKey("do_not_show_notification_permission_dialog")
        val TODOS_DONE_COUNT = intPreferencesKey("todos_done_count")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
    }
}

val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "appPreferences")