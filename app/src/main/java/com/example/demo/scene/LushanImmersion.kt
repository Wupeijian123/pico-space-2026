package com.example.demo.scene

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.content.SpatialView
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import kotlinx.coroutines.launch

/**
 * 入画态入口：Stage 里放一个 SpatialView
 *
 * 出画：监听返回（返回键 / 返回手势），关闭 Stage 回到书房态。
 * 注意：不要用 closeWindowContainer —— 它会把整个应用关掉。
 */
@Composable
fun LushanImmersion() {
    val nav = LocalSpatialNavigator.current
    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        com.example.demo.AppState.inStage = false
    }

    PicoTheme {
        AutoLightCycle()
        SpatialView(
            initial = { content, _ ->
                buildLushanMorning(content)
            }
        )
    }
}

/** 演示用：每 7 秒推进一个光段（后面接诗句进度时改这里） */
@Composable
private fun AutoLightCycle() {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(7000)
            setLightPhase(LightPhase.of(currentPhase.ordinal + 1))
        }
    }
}
