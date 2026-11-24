package com.example.demo.scene

import com.pico.spatial.core.container.SpatialViewContent
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.LoadType
import com.pico.spatial.core.ecs.ModelComponent
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.ecs.resource.BlendingMode
import com.pico.spatial.core.ecs.resource.MeshModel
import com.pico.spatial.core.ecs.resource.MeshResource
import com.pico.spatial.core.ecs.resource.PhysicallyBasedMaterial
import com.pico.spatial.core.ecs.resource.TextureResource
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector2
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.content.SpatialView

/**
 * 望庐山瀑布 · 入画态
 *
 * 画面由四层自制剪影 PNG 叠出（tools/gen_scene.py 程序化生成，不依赖任何外部素材）：
 * 远景山峦 → 中景主峰与瀑布 → 水雾 → 近景崖台松树。
 * 每层放在不同的 Z 上，走动时自然产生视差。
 *
 * 材质用「自发光 + 贴图」：Stage 里没有环境光，纯 PBR 材质会是黑的。
 */

private val WHITE = Color4(1f, 1f, 1f, 1f)

/**
 * 带 UV 的四边形网格。
 * 注意：MeshResource.createPlane 不带 UV，贴图会全部采样到 (0,0) 透明点，整块消失。
 */
private fun uvQuad(width: Float, height: Float): MeshResource {
    val hw = width / 2f
    val hh = height / 2f
    // 逆时针（从 +Z 看过去）才是正面，否则被背面剔除
    val positions = listOf(
        Vector3(-hw, -hh, 0f),
        Vector3(hw, -hh, 0f),
        Vector3(hw, hh, 0f),
        Vector3(-hw, hh, 0f)
    )
    val normals = List(4) { Vector3(0f, 0f, 1f) }
    val uv0 = listOf(
        Vector2(0f, 0f),
        Vector2(1f, 0f),
        Vector2(1f, 1f),
        Vector2(0f, 1f)
    )
    val indices = listOf(0, 1, 2, 0, 2, 3)
    val model = MeshModel(positions, indices, normals, emptyList(), uv0)
    return MeshResource.createWithMeshModel(model, name = "uvQuad")
}


/** 贴图必须保活，否则被回收后材质变白 */
private val retainedTextures = mutableListOf<TextureResource>()

/** 四组光段实体，索引与 LightPhase.ordinal 对齐 */
private val phaseGroups = mutableListOf<Entity>()

/** 隐藏时挪到相机背后的距离 */
private const val HIDDEN_Z = 800f

private suspend fun addLayerTo(
    root: Entity,
    asset: String,
    width: Float,
    height: Float,
    x: Float,
    y: Float,
    z: Float,
    opacity: Float = 1f
) {
    val tex = TextureResource.load(path = asset, loadType = LoadType.FROM_ASSETS)
    retainedTextures.add(tex)

    val material =
        PhysicallyBasedMaterial.create(BlendingMode.TRANSPARENT).apply {
            setBaseColor(Color4(1f, 1f, 1f, opacity))
            setBaseColorTexture(tex)
            setEmissiveColor(WHITE)
            setEmissiveTexture(tex)
            setRoughness(1f)
        }

    val layer =
        Entity().apply {
            components.set(ModelComponent(uvQuad(width, height), material))
            components.set(
                TransformComponent(Vector3(x, y, z), EulerAngles(0f, 0f, 0f), Vector3.ONE)
            )
        }
    root.addChild(layer)
}

suspend fun buildLushanMorning(content: SpatialViewContent) {
    val root = Entity()
    // 根实体必须有 TransformComponent，否则子实体的变换链无效、整层不渲染
    root.components.set(
        TransformComponent(Vector3.ZERO, EulerAngles(0f, 0f, 0f), Vector3.ONE)
    )
    content.addEntity(root)

    // 天幕共用一份（原图顶部拉伸模糊），不分光段
    addLayerTo(root, "scene/skyfill.jpg", 44f, 24.75f, 0f, 3.4f, -15f)

    // 每个光段一组实体：远景（10 米）+ 前景（6.2 米，等比缩放保证对齐）
    phaseGroups.clear()
    LightPhase.entries.forEachIndexed { index, phase ->
        val g = Entity().apply {
            components.set(
                TransformComponent(
                    Vector3(0f, 0f, if (index == 0) 0f else HIDDEN_Z),
                    EulerAngles(0f, 0f, 0f),
                    Vector3.ONE
                )
            )
        }
        root.addChild(g)
        phaseGroups.add(g)
        addLayerTo(g, "scene_baked/bg_${phase.asset}.jpg", 20f, 11.25f, 0f, 2.2f, -10f)
        addLayerTo(g, "scene_baked/fore_${phase.asset}.png", 12.4f, 6.98f, 0f, 1.97f, -6.2f)
        // 诗句竖幅：叠在画面右侧，随光段换句
        addLayerTo(g, "scene_verse/verse_$index.png", 1.55f, 3.49f, 4.9f, 2.6f, -5.6f)
    }
    currentPhase = LightPhase.MORNING

}

/** 切换光段：只移动实体（挪到相机背后即等于隐藏），不碰材质 */
fun applyLightPhase(phase: LightPhase) {
    currentPhase = phase
    phaseGroups.forEachIndexed { index, g ->
        val z = if (index == phase.ordinal) 0f else HIDDEN_Z
        g.components.set(
            TransformComponent(Vector3(0f, 0f, z), EulerAngles(0f, 0f, 0f), Vector3.ONE)
        )
    }
    println("LIGHT_PHASE -> ${phase.label} (${phase.asset})")
}
