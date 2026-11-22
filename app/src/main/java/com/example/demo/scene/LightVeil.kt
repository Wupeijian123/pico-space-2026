package com.example.demo.scene

import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.LoadType
import com.pico.spatial.core.ecs.ModelComponent
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.ecs.resource.BlendingMode
import com.pico.spatial.core.ecs.resource.MeshModel
import com.pico.spatial.core.ecs.resource.MeshResource
import com.pico.spatial.core.ecs.resource.PhysicallyBasedMaterial
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector2
import com.pico.spatial.core.math.Vector3

/**
 * 四段光照：随诗句推进，一层半透明色罩叠在整幅画上。
 * 不重画贴图，只改色罩颜色和浓度——省资源，也保证四段画面构图完全一致。
 *
 *   0 晨光   : 暖金低照，雾浓
 *   1 上午   : 白亮微冷，雾中
 *   2 正午   : 硬光高对比
 *   3 夜景   : 冷月光，星空
 */
enum class LightPhase(val label: String, val tint: Long, val alpha: Float) {
    MORNING("晨光", 0xF2C98A, 0.22f),
    FORENOON("上午", 0xEAF2F6, 0.10f),
    NOON("正午", 0xFFF4DC, 0.06f),
    NIGHT("夜", 0x1B2A4A, 0.55f);

    companion object {
        fun of(index: Int): LightPhase = entries[((index % entries.size) + entries.size) % entries.size]
    }
}

private fun color4(hex: Long, alpha: Float) = Color4(
    ((hex shr 16) and 0xFF) / 255f,
    ((hex shr 8) and 0xFF) / 255f,
    (hex and 0xFF) / 255f,
    alpha
)

/** 带 UV 的四边形（贴图平面必须自己带 UV，见 LushanLayers 的说明） */
private fun quad(width: Float, height: Float, z: Float, name: String): MeshResource {
    val hw = width / 2f
    val hh = height / 2f
    val positions = listOf(
        Vector3(-hw, -hh, z),
        Vector3(hw, -hh, z),
        Vector3(hw, hh, z),
        Vector3(-hw, hh, z)
    )
    val model = MeshModel(
        positions,
        listOf(0, 1, 2, 0, 2, 3),
        List(4) { Vector3(0f, 0f, 1f) },
        emptyList(),
        listOf(Vector2(0f, 0f), Vector2(1f, 0f), Vector2(1f, 1f), Vector2(0f, 1f))
    )
    return MeshResource.createWithMeshModel(model, name = name)
}

/** 色罩实体与它的材质，供外部切换光段 */
private var veilMaterial: PhysicallyBasedMaterial? = null
private var veilEntity: Entity? = null

@Volatile
var currentPhase: LightPhase = LightPhase.MORNING
    private set

/** 建一层光照色罩，挂在整幅画前面（画在 6~10 米处，色罩放在 5.8 米） */
fun attachLightVeil(root: Entity): Entity {
    val material = PhysicallyBasedMaterial.create(BlendingMode.TRANSPARENT).apply {
        setBaseColor(color4(LightPhase.MORNING.tint, 1f))
        setEmissiveColor(color4(LightPhase.MORNING.tint, 1f))
        setOpacity(LightPhase.MORNING.alpha)
        setRoughness(1f)
    }
    val e = Entity().apply {
        components.set(ModelComponent(quad(13f, 7.4f, 0f, "lightVeil"), material))
        components.set(
            TransformComponent(Vector3(0f, 1.95f, -5.8f), EulerAngles(0f, 0f, 0f), Vector3.ONE)
        )
    }
    root.addChild(e)
    veilEntity = e
    veilMaterial = material
    return e
}

/** 切换光段：诗句推进时调用 */
fun setLightPhase(phase: LightPhase) {
    currentPhase = phase
    println("LIGHT_PHASE -> ${phase.label} tint=${Integer.toHexString(phase.tint.toInt())} alpha=${phase.alpha}")
    val m = veilMaterial ?: return
    m.setBaseColor(color4(phase.tint, 1f))
    m.setEmissiveColor(color4(phase.tint, 1f))
    m.setOpacity(phase.alpha)
}
