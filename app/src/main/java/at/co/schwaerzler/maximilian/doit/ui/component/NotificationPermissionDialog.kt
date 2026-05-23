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

package at.co.schwaerzler.maximilian.doit.ui.component

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import at.co.schwaerzler.maximilian.doit.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionDialog(
    notificationPermissionState: PermissionState,
    onDismissRequest: () -> Unit,
    onDoNotShowAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(notificationPermissionState) {
        if (notificationPermissionState.status.isGranted) {
            Toast.makeText(context, R.string.permission_granted, Toast.LENGTH_SHORT).show()
        }
    }

    if (!notificationPermissionState.status.isGranted) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = { notificationPermissionState.launchPermissionRequest() }) {
                    Text(stringResource(R.string.yes_dialog))
                }
            },
            modifier = modifier,
            text = {
                Text(stringResource(R.string.notification_permission_dialog_text))
            },
            title = {
                Text(stringResource(R.string.notification_permission_dialog_title))
            },
            dismissButton = {
                TextButton(onClick = onDoNotShowAgain) {
                    Text(stringResource(R.string.do_not_ask_again_dialog))
                }
            }
        )
    }
}