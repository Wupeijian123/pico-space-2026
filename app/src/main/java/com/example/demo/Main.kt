package com.example.demo

import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.DefaultWindowContainer
import com.pico.spatial.ui.foundation.dsl.Immersion
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope
import com.pico.spatial.ui.foundation.dsl.Stage
import com.example.demo.content.HomePage
import com.example.demo.scene.LushanImmersion

fun mainApp(scope: SpatialAppScope) =
    with(scope) {
        DefaultWindowContainer {
            PicoTheme {
                HomePage()
            }
        }

        // 入画态：望庐山瀑布（第一帧，晨光）
        Stage(
            id = "lushan",
            immersion = Immersion(default = 100, min = 0, max = 100)
        ) {
            LushanImmersion()
        }
    }
