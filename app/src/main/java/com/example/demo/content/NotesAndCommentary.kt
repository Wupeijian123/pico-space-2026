package com.example.demo.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.Text

/** 注释窗：逐句训诂 */
@Composable
fun NotesPage() {
    val poem = lushanPoem
    Box(modifier = Modifier.fillMaxSize().background(Paper)) {
        Column(modifier = Modifier.fillMaxSize().padding(28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "注释", fontSize = 22.sp, color = Ink, letterSpacing = 4.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = poem.title, fontSize = 13.sp, color = InkFaint)
            }
            Spacer(modifier = Modifier.height(18.dp))
            poem.lines.forEach { line ->
                Column(modifier = Modifier.padding(vertical = 9.dp)) {
                    Text(text = line.text, fontSize = 17.sp, color = Ink, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = line.note, fontSize = 13.sp, lineHeight = 21.sp, color = InkSoft)
                }
            }
        }
    }
}

/** 赏析窗：评点 + 意象标签 */
@Composable
fun CommentaryPage() {
    val poem = lushanPoem
    Box(modifier = Modifier.fillMaxSize().background(Paper)) {
        Column(modifier = Modifier.fillMaxSize().padding(28.dp)) {
            Text(text = "赏析", fontSize = 22.sp, color = Ink, letterSpacing = 4.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = poem.commentary, fontSize = 14.sp, lineHeight = 24.sp, color = InkSoft)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "意象", fontSize = 13.sp, color = InkFaint, letterSpacing = 3.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                poem.images.forEach { tag ->
                    Box(
                        modifier =
                            Modifier.width(58.dp)
                                .height(30.dp)
                                .background(androidx.compose.ui.graphics.Color(0x1FA8322D))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = tag, fontSize = 13.sp, color = Cinnabar, letterSpacing = 2.sp)
                        }
                    }
                }
            }
        }
    }
}
