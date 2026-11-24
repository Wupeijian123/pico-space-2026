package com.example.demo.scene

/**
 * 四段光照：随诗句推进，晨光 → 上午 → 正午 → 夜。
 *
 * 实现方式是「预烘焙」：四套图（bg_/fore_ + asset 后缀）由 tools/bake_lights.py 生成，
 * 运行时只移动实体切换，不改材质颜色（本 SDK 上运行时改颜色不生效）。
 */
enum class LightPhase(val label: String, val asset: String) {
    MORNING("晨光", "morning"),
    FORENOON("上午", "forenoon"),
    NOON("正午", "noon"),
    NIGHT("夜", "night");

    companion object {
        fun of(index: Int): LightPhase = entries[((index % entries.size) + entries.size) % entries.size]
    }
}

@Volatile
var currentPhase: LightPhase = LightPhase.MORNING
    internal set
