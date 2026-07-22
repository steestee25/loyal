package com.app.loyal.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIScreen

@Composable
actual fun KeepScreenBright() {
    DisposableEffect(Unit) {
        val screen = UIScreen.mainScreen
        val previousBrightness = screen.brightness

        screen.brightness = 1.0
        UIApplication.sharedApplication.idleTimerDisabled = true

        onDispose {
            screen.brightness = previousBrightness
            UIApplication.sharedApplication.idleTimerDisabled = false
        }
    }
}
