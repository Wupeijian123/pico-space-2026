package com.example.demo.content

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.pico.spatial.ui.platform.containers.StageStyle
import kotlinx.coroutines.launch

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
            Modifier.width(88.dp)
                .height(40.dp)
                .background(Color(0x14A8322D), sealShape)
                .border(1.5.dp, Cinnabar, sealShape)
                .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 15.sp, color = Cinnabar, letterSpacing = 3.sp)
    }
}

/**
 * 书房态主窗：一卷三栏。
 * 左为题签，中为正文，右为注释与赏析——像一幅展开的批注本。
 */
@Composable
fun ScrollPage() {
    val nav = LocalSpatialNavigator.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val poem = lushanPoem

    // 入画态的开/关：Stage 只声明不会显示，必须显式 openStage / closeStage
    androidx.compose.runtime.LaunchedEffect(com.example.demo.AppState.inStage) {
        if (com.example.demo.AppState.inStage) {
            nav.openStage(id = "lushan", style = StageStyle.Full)
        } else {
            runCatching { nav.closeStage() }
        }
    }

    var selected by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(Paper)) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 28.dp)) {

            // ── 左：题签 ──
            Column(
                modifier = Modifier.width(184.dp).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = poem.title, fontSize = 25.sp, color = Ink, letterSpacing = 3.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${poem.dynasty} · ${poem.author}",
                    fontSize = 13.sp,
                    color = InkFaint,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier.width(46.dp).height(46.dp).border(2.dp, Cinnabar, sealShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "诗境", fontSize = 12.sp, color = Cinnabar, lineHeight = 14.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SealButton("注释") { nav.openWindowContainer("notes", "lushan", Bundle()) }
                    Spacer(modifier = Modifier.height(10.dp))
                    SealButton("赏析") { nav.openWindowContainer("commentary", "lushan", Bundle()) }
                    Spacer(modifier = Modifier.height(10.dp))
                    // 同一枚印章：书房态是「入诗」，入画态变成「出画」
                    if (com.example.demo.AppState.inStage) {
                        SealButton("出画") { com.example.demo.AppState.inStage = false }
                    } else {
                        SealButton("入诗") { com.example.demo.AppState.inStage = true }
                    }
                }
            }

            // ── 分隔线 ──
            Box(
                modifier =
                    Modifier.padding(horizontal = 22.dp)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0x2E1C1B19))
            )

            // ── 中：正文 ──
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                poem.lines.forEachIndexed { index, line ->
                    val active = index == selected
                    Row(
                        modifier = Modifier
                            .clickable { selected = index }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(34.dp)
                                .background(if (active) Cinnabar else Color(0x00000000))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = line.text,
                            fontSize = if (active) 25.sp else 24.sp,
                            lineHeight = 46.sp,
                            color = if (active) Cinnabar else Ink,
                            letterSpacing = 5.sp
                        )
                    }
                }
            }

            // ── 右：注释与赏析 ──
            Column(modifier = Modifier.width(330.dp).fillMaxHeight()) {
                Text(text = "注", fontSize = 13.sp, color = Cinnabar, letterSpacing = 3.sp)
                Spacer(modifier = Modifier.height(8.dp))
                poem.lines.forEachIndexed { index, line ->
                    val active = index == selected
                    Text(
                        text = "${index + 1}. ${line.note}",
                        fontSize = if (active) 14.sp else 12.sp,
                        lineHeight = 21.sp,
                        color = if (active) Ink else InkFaint
                    )
                    Spacer(modifier = Modifier.height(if (active) 8.dp else 4.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "赏", fontSize = 13.sp, color = Cinnabar, letterSpacing = 3.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = poem.commentary, fontSize = 13.sp, lineHeight = 22.sp, color = InkSoft)
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    poem.images.forEach { tag ->
                        Box(
                            modifier =
                                Modifier.width(46.dp)
                                    .height(24.dp)
                                    .background(Color(0x1FA8322D), sealShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = tag, fontSize = 11.sp, color = Cinnabar, letterSpacing = 2.sp)
                        }
                    }
                }
            }
        }
    }
}
