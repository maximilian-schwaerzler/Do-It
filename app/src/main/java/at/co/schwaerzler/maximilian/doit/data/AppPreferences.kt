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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "appPreferences")

object AppPreferenceKeys {
    val THEME = stringPreferencesKey("theme")
    val NOTIFICATION_LEAD_TIME = stringPreferencesKey("notification_lead_time")
    val DO_NOT_SHOW_WIDGET_PIN_DIALOG = booleanPreferencesKey("do_not_show_widget_pin_dialog")
    val DO_NOT_SHOW_NOTIFICATION_PERMISSION_DIALOG = booleanPreferencesKey("do_not_show_notification_permission_dialog")
    val TODOS_DONE_COUNT = intPreferencesKey("todos_done_count")
}

suspend fun DataStore<Preferences>.setTheme(themeMode: AppThemeMode) {
    edit { it[AppPreferenceKeys.THEME] = themeMode.toString() }
}

fun DataStore<Preferences>.themeFlow(): Flow<AppThemeMode> =
    data.map { preferences ->
        preferences[AppPreferenceKeys.THEME]?.let { themeModeString ->
            try {
                AppThemeMode.valueOf(themeModeString)
            } catch (_: IllegalArgumentException) {
                AppThemeMode.FOLLOW_SYSTEM
            }
        } ?: AppThemeMode.FOLLOW_SYSTEM
    }

suspend fun DataStore<Preferences>.setNotificationLeadTime(leadTime: NotificationLeadTime) {
    edit { it[AppPreferenceKeys.NOTIFICATION_LEAD_TIME] = leadTime.name }
}

fun DataStore<Preferences>.notificationLeadTimeFlow(): Flow<NotificationLeadTime> =
    data.map { prefs ->
        prefs[AppPreferenceKeys.NOTIFICATION_LEAD_TIME]?.let {
            try { NotificationLeadTime.valueOf(it) } catch (_: IllegalArgumentException) { null }
        } ?: NotificationLeadTime.THIRTY_MINUTES
    }

suspend fun DataStore<Preferences>.doNotShowWidgetDialogAgain() {
    edit { it[AppPreferenceKeys.DO_NOT_SHOW_WIDGET_PIN_DIALOG] = true }
}

fun DataStore<Preferences>.doNotShowWidgetDialogAgainFlow(): Flow<Boolean> =
    data.map { preferences ->
        preferences[AppPreferenceKeys.DO_NOT_SHOW_WIDGET_PIN_DIALOG] ?: false
    }

suspend fun DataStore<Preferences>.doNotShowNotificationDialogAgain() {
    edit { it[AppPreferenceKeys.DO_NOT_SHOW_NOTIFICATION_PERMISSION_DIALOG] = true }
}

suspend fun DataStore<Preferences>.resetDoNotShowNotificationDialog() {
    edit { it.remove(AppPreferenceKeys.DO_NOT_SHOW_NOTIFICATION_PERMISSION_DIALOG) }
}

fun DataStore<Preferences>.doNotShowNotificationDialogAgainFlow(): Flow<Boolean> =
    data.map { preferences ->
        preferences[AppPreferenceKeys.DO_NOT_SHOW_NOTIFICATION_PERMISSION_DIALOG] ?: false
    }

suspend fun DataStore<Preferences>.incrementTodosDoneCount() {
    edit { it[AppPreferenceKeys.TODOS_DONE_COUNT] = (it[AppPreferenceKeys.TODOS_DONE_COUNT] ?: 0) + 1 }
}

fun DataStore<Preferences>.todosDoneCountFlow(): Flow<Int> =
    data.map { preferences ->
        preferences[AppPreferenceKeys.TODOS_DONE_COUNT] ?: 0
    }