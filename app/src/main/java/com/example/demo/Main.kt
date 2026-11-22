package com.example.demo

import androidx.compose.ui.unit.dp
import com.example.demo.content.CommentaryPage
import com.example.demo.content.NotesPage
import com.example.demo.content.ScrollPage
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.DefaultWindowContainer
import com.pico.spatial.ui.foundation.dsl.Form
import com.pico.spatial.ui.foundation.dsl.Immersion
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope
import com.pico.spatial.ui.foundation.dsl.Stage
import com.pico.spatial.ui.foundation.dsl.WindowContainer
import com.pico.spatial.ui.foundation.dsl.WindowContainerSize
import com.pico.spatial.ui.foundation.dsl.WorldScale
import com.pico.spatial.ui.platform.resize.ContainerResizeType
import com.example.demo.scene.LushanImmersion

fun mainApp(scope: SpatialAppScope) =
    with(scope) {
        DefaultWindowContainer {
            PicoTheme {
                ScrollPage()
            }
        }

        // 注释窗（按需打开）
        WindowContainer(
            id = "notes",
            resizeType = ContainerResizeType.ContentSize,
            defaultSize = WindowContainerSize(width = 720.dp, height = 640.dp),
            form = Form.Planar,
            worldScale = WorldScale.Fixed,
            enableMaterialBackground = false
        ) {
            PicoTheme { NotesPage() }
        }

        // 赏析窗（按需打开）
        WindowContainer(
            id = "commentary",
            resizeType = ContainerResizeType.ContentSize,
            defaultSize = WindowContainerSize(width = 720.dp, height = 640.dp),
            form = Form.Planar,
            worldScale = WorldScale.Fixed,
            enableMaterialBackground = false
        ) {
            PicoTheme { CommentaryPage() }
        }

        // 入画态：只在用户点「入诗」后才创建，出画时销毁
        if (AppState.inStage) {
            Stage(
                id = "lushan",
                immersion = Immersion(default = 100, min = 0, max = 100)
            ) {
                LushanImmersion()
            }
        }
    }
