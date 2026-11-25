package com.example.demo.scene

import android.content.Context
import android.graphics.BitmapFactory
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * 全景包裹：把整幅画弯成一段圆柱内壁，围绕观看者展开。
 *
 * 为什么不用平面：平面只能"看一幅画"，永远没有包围感。把画布按角度绕成
 * 140°~220° 的弧面，人站在弧心，左右张望时画面会跟着转动——这才是"走进画里"。
 * 高度仍由 heightmap 决定，山水因此有起伏；再加上环境光 + 方向光，明暗有了过渡。
 *
 * @param arcDegrees 画的环绕角度。180° 半圈、220° 更包围（太大时 UV 会拉伸）
 * @param radius 观看者到画布的距离（米）
 * @param baseHeight 弧面中心高度
 * @param reliefHeight 起伏幅度（米）；0 就是纯弧幕
 */
suspend fun buildPanorama(
    parent: Entity,
    context: Context,
    heightmapAsset: String,
    textureAsset: String,
    arcDegrees: Float = 200f,
    radius: Float = 11f,
    baseHeight: Float = 1.9f,
    reliefHeight: Float = 5.5f,
    yawOffsetDegrees: Float = 0f
) {
    val bmp = context.assets.open(heightmapAsset).use { BitmapFactory.decodeStream(it) }
        ?: run {
            println("PANO_HEIGHTMAP_MISSING $heightmapAsset")
            return
        }
    val cols = bmp.width - 1
    val rows = bmp.height - 1
    if (cols < 2 || rows < 2) return

    val arc = Math.toRadians(arcDegrees.toDouble())
    val yaw0 = Math.toRadians(yawOffsetDegrees.toDouble())

    val positions = ArrayList<Vector3>((cols + 1) * (rows + 1))
    val uvs = ArrayList<Vector2>((cols + 1) * (rows + 1))
    val normals = ArrayList<Vector3>((cols + 1) * (rows + 1))

    for (r in 0..rows) {
        val fv = r.toFloat() / rows                       // 0 = 画面上沿（天空）
        val angle = yaw0 - arc / 2 + arc * (r / rows.toDouble())
        val sinA = sin(angle).toFloat()
        val cosA = cos(angle).toFloat()
        for (c in 0..cols) {
            val fu = c.toFloat() / cols
            val hRaw = (bmp.getPixel(c, r) shr 16 and 0xFF).toFloat() / 255f
            // 上沿往上抬（天），下沿落地，中间按高度图起伏
            val vertical = (0.5f - fv) * 2f                 // +1 顶部, -1 底部
            val y = baseHeight + vertical * 4.2f + (hRaw - 0.5f) * reliefHeight
            positions.add(Vector3(sinA * radius, y, -cosA * radius))
            uvs.add(Vector2(fu, 1f - fv))
            // 法线朝圆心（观看者在圆心）
            normals.add(Vector3(-sinA, 0f, cosA))
        }
    }

    val idx = ArrayList<Int>(cols * rows * 6)
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val a = r * (cols + 1) + c
            val b = a + 1
            val d = a + (cols + 1)
            val e = d + 1
            idx.add(a); idx.add(b); idx.add(d)
            idx.add(b); idx.add(e); idx.add(d)
        }
    }

    val mesh = MeshResource.createWithMeshModel(
        MeshModel(positions, idx, normals, emptyList(), uvs),
        name = "panorama"
    )
    val tex = TextureResource.load(path = textureAsset, loadType = LoadType.FROM_ASSETS)
    retainedTextures.add(tex)

    val material = PhysicallyBasedMaterial.create(BlendingMode.OPAQUE).apply {
        setBaseColor(Color4(1f, 1f, 1f, 1f))
        setBaseColorTexture(tex)
        setEmissiveColor(Color4(0.55f, 0.55f, 0.55f, 1f))   // 一点自发光兜底，避免背光面全黑
        setEmissiveTexture(tex)
        setRoughness(1f)
    }

    val e = Entity().apply {
        components.set(ModelComponent(mesh, material))
        components.set(TransformComponent(Vector3.ZERO, EulerAngles(0f, 0f, 0f), Vector3.ONE))
    }
    parent.addChild(e)
    println("PANO_BUILT arc=$arcDegrees radius=$radius 顶点${positions.size}")
}
