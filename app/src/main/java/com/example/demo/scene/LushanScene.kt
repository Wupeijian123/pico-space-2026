package com.example.demo.scene

import com.pico.spatial.core.ecs.DirectionalLightComponent
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.ModelComponent
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.ecs.resource.BlendingMode
import com.pico.spatial.core.ecs.resource.MeshResource
import com.pico.spatial.core.ecs.resource.PhysicallyBasedMaterial
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.content.SpatialView

/**
 * 望庐山瀑布 · 入画态 第一帧（晨光）
 *
 * 坐标系：+X 右，+Y 上，+Z 朝用户。相机在原点附近看向 -Z，
 * 所以山体、瀑布都摆在负 Z。
 */
private fun c(hex: Long, alpha: Float = 1f) =
    Color4(
        ((hex shr 16) and 0xFF) / 255f,
        ((hex shr 8) and 0xFF) / 255f,
        (hex and 0xFF) / 255f,
        alpha
    )

private class PlaneFactory {
    private val meshes = HashMap<String, MeshResource>()
    private val materials = HashMap<String, PhysicallyBasedMaterial>()

    fun plane(w: Float, h: Float, hex: Long, alpha: Float = 1f): Entity {
        val key = "$w-$h"
        val mesh =
            meshes.getOrPut(key) {
                MeshResource.createPlane(w, h, 0f)
            }
        val mkey = "$hex-$alpha"
        val mat =
            materials.getOrPut(mkey) {
                PhysicallyBasedMaterial.create(
                    if (alpha < 1f) BlendingMode.TRANSPARENT else BlendingMode.OPAQUE
                ).apply {
                    setBaseColor(c(hex, alpha * 0.35f))
                    // 自发光：让青绿山水不依赖场景光照也能出颜色（平面场景用不到 PBR 光照）
                    setEmissiveColor(c(hex, alpha))
                    setOpacity(alpha)
                    setRoughness(1f)
                }
            }
        return Entity().apply { components.set(ModelComponent(mesh, mat)) }
    }
}

private fun Entity.place(x: Float, y: Float, z: Float, pitch: Float = 0f, yaw: Float = 0f) {
    components.set(
        TransformComponent(
            Vector3(x, y, z),
            EulerAngles(pitch, yaw, 0f),
            Vector3.ONE
        )
    )
}

/** 入画态：晨光下的庐山瀑布（静态一帧）。scale 用于把 30 米宽的场景塞进窗口 */
fun buildLushanMorning(
    content: com.pico.spatial.core.container.SpatialViewContent,
    scale: Float = 0.12f
) {
    val f = PlaneFactory()
    val root = Entity()

    // 天光：一盏低角度暖光，模拟清晨侧逆光
    val sun =
        Entity().apply {
            place(6f, 8f, -16f, pitch = 25f, yaw = 200f)
            components.set(DirectionalLightComponent(c(0xF2C98A), 2.6f))
        }
    root.addChild(sun)

    // 天幕（背景大平面，暖白）
    root.addChild(f.plane(60f, 30f, 0xEFE6D3).apply { place(0f, 2f, -34f) })

    // 远山（最淡、最远）
    root.addChild(f.plane(26f, 7.5f, 0x9AA79B).apply { place(0f, 2.6f, -28f) })
    // 中山（左右两组，青绿）
    root.addChild(f.plane(14f, 11f, 0x7B9688).apply { place(-9f, 3.5f, -24f) })
    root.addChild(f.plane(14f, 12f, 0x7B9688).apply { place(9f, 3.8f, -24f) })
    // 近山（深青灰，压住画面两侧）
    root.addChild(f.plane(9f, 15f, 0x556E68).apply { place(-11f, 4.5f, -18f) })
    root.addChild(f.plane(9f, 16f, 0x556E68).apply { place(11f, 5f, -18f) })

    // 紫云（两层，半透明，压在山腰）
    root.addChild(f.plane(30f, 5f, 0x8B7BA8, 0.35f).apply { place(-6f, 9.5f, -26f) })
    root.addChild(f.plane(24f, 4f, 0x8B7BA8, 0.28f).apply { place(8f, 11f, -25f) })

    // 主瀑布：几条半透明竖带叠出层次
    root.addChild(f.plane(1.8f, 13f, 0xFFFFFF, 0.55f).apply { place(0f, 5.5f, -22f) })
    root.addChild(f.plane(0.9f, 12f, 0xFFFFFF, 0.75f).apply { place(-0.9f, 5f, -21.9f) })
    root.addChild(f.plane(0.9f, 12.5f, 0xFFFFFF, 0.7f).apply { place(0.9f, 5f, -21.9f) })

    // 水潭（横向平面，浅色微透明）
    root.addChild(f.plane(14f, 8f, 0xDDE7E0, 0.85f).apply { place(0f, -1.2f, -16f, pitch = -90f) })

    // 水雾（瀑布底部，横条）
    root.addChild(f.plane(6f, 1.6f, 0xE9E6D9, 0.45f).apply { place(0f, 0.2f, -20.5f) })

    // 前景岩台（机位所在的平台）
    root.addChild(f.plane(7f, 4f, 0x3A423D).apply { place(0f, -3.4f, -8f, pitch = -90f) })

    // 左右前景墨松剪影
    root.addChild(f.plane(1.2f, 7f, 0x1B1F1D).apply { place(-4.2f, 0.5f, -6f) })
    root.addChild(f.plane(1.2f, 7.5f, 0x1B1F1D).apply { place(4.2f, 0.8f, -6f) })

    // 把整幅山水收成一幅悬在空中的画：缩到 0.12 倍，放到眼前 4 米、高 1.4 米处
    root.components.set(
        TransformComponent(
            Vector3(0f, 1.4f, -4f),
            EulerAngles(0f, 0f, 0f),
            Vector3(scale, scale, scale)
        )
    )
    content.addEntity(root)

    // 调试标记：一个红色方块，用来确认相机方向与可见性（确认后可删）
    val probe =
        Entity().apply {
            components.set(ModelComponent(MeshResource.createBox(Vector3(0.2f, 0.2f, 0.2f), 0.02f), redMaterial()))
            place(0f, 0f, -0.8f)
        }
    content.addEntity(probe)
}

private fun redMaterial(): PhysicallyBasedMaterial =
    PhysicallyBasedMaterial.create(BlendingMode.OPAQUE).apply {
        setBaseColor(Color4(1f, 0f, 0f, 1f))
        setRoughness(1f)
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
