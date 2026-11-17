package com.example.demo.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.Text
import kotlinx.coroutines.launch
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.pico.spatial.ui.platform.containers.StageStyle
import android.os.Bundle

// 文人墨客配色：宣纸 / 墨 / 朱砂 / 淡墨
val Paper = Color(0xFFF2EADA)
val Ink = Color(0xFF1C1B19)
val InkSoft = Color(0xFF5B564E)
val InkFaint = Color(0xFF8C867C)
val Cinnabar = Color(0xFFA8322D)

private val sealShape = RoundedCornerShape(6.dp)

@Composable
private fun SealButton(label: String, onClick: () -> Unit) {
    Box(
        modifier =
            Modifier.width(96.dp)
                .height(44.dp)
                .background(Color(0x14A8322D), sealShape)
                .border(1.5.dp, Cinnabar, sealShape)
                .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 16.sp, color = Cinnabar, letterSpacing = 3.sp)
    }
}

@Composable
fun ScrollPage() {
    val nav = LocalSpatialNavigator.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val poem = lushanPoem

    // 启动时把注释窗与赏析窗一并打开：一是让用户一眼看到多窗口，
    // 二是演示/评审时的 30 秒脚本从这里开始
    androidx.compose.runtime.LaunchedEffect(Unit) {
        nav.openWindowContainer("notes", "lushan", Bundle())
        nav.openWindowContainer("commentary", "lushan", Bundle())
    }

    Box(modifier = Modifier.fillMaxSize().background(Paper)) {
        Row(modifier = Modifier.fillMaxSize().padding(40.dp)) {
            // 左：题签
            Column(
                modifier = Modifier.width(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = poem.title, fontSize = 34.sp, color = Ink, letterSpacing = 4.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "${poem.dynasty} · ${poem.author}", fontSize = 16.sp, color = InkFaint, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier =
                        Modifier.width(56.dp)
                            .height(56.dp)
                            .border(2.dp, Cinnabar, sealShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "诗境", fontSize = 13.sp, color = Cinnabar, lineHeight = 16.sp)
                }
            }

            Spacer(modifier = Modifier.width(36.dp))

            // 右：正文 + 印章按钮
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(6.dp))
                poem.lines.forEach { line ->
                    Text(
                        text = line.text,
                        fontSize = 30.sp,
                        lineHeight = 56.sp,
                        color = Ink,
                        letterSpacing = 6.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    SealButton("注释") { nav.openWindowContainer("notes", "lushan", Bundle()) }
                    SealButton("赏析") { nav.openWindowContainer("commentary", "lushan", Bundle()) }
                    SealButton("入诗") {
                        scope.launch { nav.openStage(id = "lushan", style = StageStyle.Full) }
                    }
                }
            }
        }
    }
}
