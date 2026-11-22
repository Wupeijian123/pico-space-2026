package com.example.demo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 应用级最简状态：现在是否在入画态。
 *
 * Stage 常驻声明的话，系统会在启动时就把 Stage 显示出来（人被直接扔进山水里，
 * 找不到书房态）。所以改成按需创建：inStage 为 true 才声明 Stage 容器，
 * 出画时置回 false，Stage 连同其中的场景一起销毁。
 */
object AppState {
    var inStage by mutableStateOf(false)
}
