package com.example.demo.content

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector3
import com.example.demo.R
import com.example.demo.scene.buildLushanMorning
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.content.SpatialView

@Composable
fun HomePage() {
    // 先把它放在窗口里看效果；后续换成 Stage 沉浸态
    SpatialView(
        modifier = Modifier.fillMaxSize(),
        initial = { content, _ ->
            buildLushanMorning(content, scale = 0.035f)
        }
    )
}
