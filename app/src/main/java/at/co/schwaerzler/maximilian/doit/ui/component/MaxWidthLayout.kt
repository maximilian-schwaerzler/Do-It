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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

@Composable
fun MaxWidthLayout(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val maxWidth =
        if (windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            600.dp
        } else {
            Dp.Unspecified
        }

    Box(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth(), content = content
        )
    }
}