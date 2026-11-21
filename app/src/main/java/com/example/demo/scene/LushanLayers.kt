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

    // 远景：11 米外，画幅 12×7.5 米
    addLayerTo(root, "scene/far.png", 12f, 7.5f, 0f, 1.9f, -11f)
    // 中景：主峰与主瀑布，8 米外
    addLayerTo(root, "scene/mid.png", 9.2f, 5.75f, 0f, 1.75f, -8f)
    // 水雾：瀑布脚下
    addLayerTo(root, "scene/mist.png", 4.4f, 2.75f, 0f, 1.05f, -6.6f, opacity = 0.9f)
    // 近景：崖台与松树，压住画面下缘
    addLayerTo(root, "scene/near.png", 7.6f, 4.75f, 0f, 1.05f, -5.5f)
}


/** 诊断用：四种材质各放一块 1 米见方的板，看哪一种能显示 */
suspend fun buildTestQuads(content: SpatialViewContent) {
    val root = Entity()
    root.components.set(TransformComponent(Vector3.ZERO, EulerAngles(0f, 0f, 0f), Vector3.ONE))
    content.addEntity(root)

    // A 对照：纯自发光红色
    val matA = PhysicallyBasedMaterial.create(BlendingMode.OPAQUE).apply {
        setBaseColor(Color4(1f, 0f, 0f, 1f)); setEmissiveColor(Color4(1f, 0f, 0f, 1f)); setRoughness(1f)
    }
    // B：贴图 + 不透明
    val matB = TextureResource.load(path = "scene/mid.png", loadType = LoadType.FROM_ASSETS).use { t ->
        PhysicallyBasedMaterial.create(BlendingMode.OPAQUE).apply {
            setBaseColor(Color4(1f, 1f, 1f, 1f)); setBaseColorTexture(t); setRoughness(1f)
        }
    }
    // C：贴图 + 自发光贴图 + 不透明
    val matC = TextureResource.load(path = "scene/mid.png", loadType = LoadType.FROM_ASSETS).use { t ->
        PhysicallyBasedMaterial.create(BlendingMode.OPAQUE).apply {
            setBaseColor(Color4(1f, 1f, 1f, 1f)); setBaseColorTexture(t)
            setEmissiveColor(WHITE); setEmissiveTexture(t); setRoughness(1f)
        }
    }
    // D：贴图 + 自发光贴图 + 透明混合
    val matD = TextureResource.load(path = "scene/mid.png", loadType = LoadType.FROM_ASSETS).use { t ->
        PhysicallyBasedMaterial.create(BlendingMode.TRANSPARENT).apply {
            setBaseColor(Color4(1f, 1f, 1f, 1f)); setBaseColorTexture(t)
            setEmissiveColor(WHITE); setEmissiveTexture(t); setRoughness(1f)
        }
    }

    listOf(matA, matB, matC, matD).forEachIndexed { i, m ->
        val e = Entity().apply {
            components.set(ModelComponent(MeshResource.createPlane(1f, 1f, 0f), m))
            components.set(
                TransformComponent(
                    Vector3(-1.5f + i * 1.0f, 1.5f, -3f),
                    EulerAngles(0f, 0f, 0f),
                    Vector3.ONE
                )
            )
        }
        root.addChild(e)
    }
}

/** 入画态入口：Stage 里放一个 SpatialView */
@androidx.compose.runtime.Composable
fun LushanImmersion() {
    PicoTheme {
        SpatialView(
            initial = { content, _ ->
                buildLushanMorning(content)
            }
        )
    }
}
