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
import kotlinx.coroutines.flow.map

class AppPreferences(private val dataStore: DataStore<Preferences>) {
    val theme = dataStore.data.map { preferences ->
        preferences[THEME]?.let { themeModeString ->
            try {
                AppThemeMode.valueOf(themeModeString)
            } catch (_: IllegalArgumentException) {
                AppThemeMode.FOLLOW_SYSTEM
            }
        } ?: AppThemeMode.FOLLOW_SYSTEM
    }

    suspend fun setTheme(themeMode: AppThemeMode) =
        dataStore.edit { it[THEME] = themeMode.name }

    val notificationLeadTimeFlow =
        dataStore.data.map { prefs ->
            prefs[NOTIFICATION_LEAD_TIME]?.let {
                try {
                    NotificationLeadTime.valueOf(it)
                } catch (_: IllegalArgumentException) {
                    null
                }
            } ?: NotificationLeadTime.THIRTY_MINUTES
        }

    suspend fun setNotificationLeadTime(leadTime: NotificationLeadTime) =
        dataStore.edit { it[NOTIFICATION_LEAD_TIME] = leadTime.name }

    val doNotShowWidgetDialogAgain = dataStore.data.map {
        it[DO_NOT_SHOW_WIDGET_PIN_DIALOG] ?: false
    }

    suspend fun doNotShowWidgetDialogAgain() = dataStore.edit {
        it[DO_NOT_SHOW_WIDGET_PIN_DIALOG] = true
    }

    suspend fun resetDoNotShowNotificationDialog() = dataStore.edit {
        it[DO_NOT_SHOW_NOTIFICATION_PERMISSION_DIALOG] = false
    }

    val doNotShowNotificationDialogAgainFlow = dataStore.data.map {
        it[DO_NOT_SHOW_NOTIFICATION_PERMISSION_DIALOG] ?: false
    }

    suspend fun incrementTodosDoneCount() = dataStore.edit {
        it[TODOS_DONE_COUNT] = (it[TODOS_DONE_COUNT] ?: 0) + 1
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

//fun DataStore<Preferences>.todosDoneCountFlow(): Flow<Int> =
//    data.map { preferences ->
//        preferences[AppPreferenceKeys.TODOS_DONE_COUNT] ?: 0
//    }
//
//suspend fun DataStore<Preferences>.setUseDynamicTheme(use: Boolean) {
//    edit { it[AppPreferenceKeys.USE_DYNAMIC_COLOR] = use }
//}
//
//fun DataStore<Preferences>.useDynamicColorFlow(): Flow<Boolean> =
//    data.map { preferences ->
//        preferences[AppPreferenceKeys.USE_DYNAMIC_COLOR] ?: true
//    }