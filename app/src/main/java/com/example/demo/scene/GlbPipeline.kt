package com.example.demo.scene

import com.pico.spatial.core.ecs.DirectionalLightComponent
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.EnvironmentLightingSettingsComponent
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector3

/**
 * 外部 3D 模型（Tripo / Rodin / 混元 3D 导出的 GLB）接入管道。
 *
 * 两个关键点（都实测过）：
 * 1. 只能用 `Entity.load("asset://scene/xxx.glb")`，`scene/xxx.glb` 会报 Unsupported URI scheme。
 * 2. GLB 自带的是 PBR 材质，Stage 里默认没有光，加载出来是全黑——
 *    必须给场景加 EnvironmentLightingSettingsComponent（环境光）+ DirectionalLightComponent（太阳）。
 *    以前用「自发光」是绕路，模型没有明暗过渡，也就没有体积感。
 */

/** 给场景打光：环境光负责整体亮度，方向光负责山体明暗与轮廓 */
fun addStageLights(
    root: Entity,
    ambient: Float = 1.6f,
    sunColor: Color4 = Color4(1f, 0.96f, 0.86f, 1f),
    sunIntensity: Float = 2.2f,
    sunPitch: Float = -38f,
    sunYaw: Float = 24f
) {
    root.components.set(EnvironmentLightingSettingsComponent(ambient))
    val sun = Entity().apply {
        components.set(DirectionalLightComponent(sunColor, sunIntensity, false))
        components.set(
            TransformComponent(
                Vector3(0f, 9f, -6f),
                EulerAngles(sunPitch, sunYaw, 0f),
                Vector3.ONE
            )
        )
    }
    root.addChild(sun)
    println("STAGE_LIGHTS ambient=$ambient sun=$sunIntensity yaw=$sunYaw")
}

/**
 * 加载一个 GLB 模型到场景。
 *
 * @param assetPath 相对 assets 的路径，例如 "scene/tripo/scene.glb"
 * @param scale 统一缩放（AI 生成的模型单位通常是米，但尺度差很多，需要按 bbox 调）
 * @param position 世界坐标，注意相机在原点朝 -Z
 */
fun loadGlbModel(
    parent: Entity,
    assetPath: String,
    scale: Float = 1f,
    position: Vector3 = Vector3(0f, 0f, -6f),
    rotation: EulerAngles = EulerAngles(0f, 0f, 0f),
    name: String = "glb"
): Boolean {
    return runCatching {
        val model = Entity.load("asset://$assetPath")
        model.components.set(
            TransformComponent(position, rotation, Vector3(scale, scale, scale))
        )
        parent.addChild(model)
        println("GLB_LOADED $assetPath scale=$scale at=(${position.x},${position.y},${position.z})")
        true
    }.onFailure {
        println("GLB_LOAD_FAILED $assetPath: ${it.message}")
    }.getOrDefault(false)
}
