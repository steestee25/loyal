package com.app.loyal.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/** Risale la catena dei ContextWrapper fino all'Activity che ospita la composizione. */
private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
actual fun KeepScreenBright() {
    val activity = LocalContext.current.findActivity() ?: return

    DisposableEffect(activity) {
        val window = activity.window
        val previousBrightness = window.attributes.screenBrightness

        window.attributes = window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            window.attributes = window.attributes.apply {
                screenBrightness = previousBrightness
            }
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
