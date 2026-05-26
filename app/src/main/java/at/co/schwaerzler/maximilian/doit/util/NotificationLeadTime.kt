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

import androidx.annotation.StringRes
import at.co.schwaerzler.maximilian.doit.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * The four user-selectable lead times for deadline notifications.
 *
 * @property labelRes Localized long-form label, used both in the settings dropdown and the
 *   notification body ("5 minutes", "30 minutes", …).
 * @property duration How far before the deadline the notification should fire.
 */
enum class NotificationLeadTime(@param:StringRes val labelRes: Int, val duration: Duration) {
    FIVE_MINUTES(R.string.notif_lead_time_5_minutes, 5.minutes),
    THIRTY_MINUTES(R.string.notif_lead_time_30_minutes, 30.minutes),
    TWO_HOURS(R.string.notif_lead_time_2_hours, 2.hours),
    ONE_DAY(R.string.notif_lead_time_1_day, 24.hours),
}
