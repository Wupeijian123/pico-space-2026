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

    // 兜底出画：看满 30 秒自动回卷轴，避免卡在山水里出不来
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(40_000)
        com.example.demo.AppState.inStage = false
    }

    PicoTheme {
        AutoLightCycle(startIndex = com.example.demo.AppState.lineIndex)
        val ctx = androidx.compose.ui.platform.LocalContext.current
        SpatialView(
            initial = { content, _ ->
                buildLushanMorning(content, ctx)
            }
        )
    }
}

/** 演示用：每 7 秒推进一个光段（后面接诗句进度时改这里） */
@Composable
private fun AutoLightCycle(startIndex: Int) {
    androidx.compose.runtime.LaunchedEffect(startIndex) {
        val begin = LightPhase.of(startIndex)
        applyLightPhase(begin)
        var i = begin.ordinal
        while (true) {
            kotlinx.coroutines.delay(7000)
            i += 1
            applyLightPhase(LightPhase.of(i))
        }
    }
}
