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

/**
 * 真 3D 地形：按 heightmap.png（由 tools/make_terrain.py 从原图明暗估出的深度）建起伏网格。
 *
 * 为什么不直接用 glTF：`Entity.load` 能加载 GLB，但 glTF 自带的是 PBR 材质，
 * Stage 里没有环境光，加载出来是全黑。这里自己建网格，材质用「自发光 + 贴图」，
 * 和画面其它图层保持一致。
 */
suspend fun buildTerrain(
    parent: Entity,
    context: Context,
    heightmapAsset: String,
    textureAsset: String,
    widthMeters: Float = 46f,
    nearZ: Float = -6f,
    farZ: Float = -62f,
    maxHeight: Float = 13f,
    baseY: Float = -1.6f,
    tiltX: Float = -32f
) {
    val bmp = context.assets.open(heightmapAsset).use { BitmapFactory.decodeStream(it) }
        ?: run {
            println("TERRAIN_HEIGHTMAP_MISSING $heightmapAsset")
            return
        }
    val cols = bmp.width - 1
    val rows = bmp.height - 1
    if (cols < 2 || rows < 2) return

    val positions = ArrayList<Vector3>((cols + 1) * (rows + 1))
    val uvs = ArrayList<Vector2>((cols + 1) * (rows + 1))
    val normals = ArrayList<Vector3>((cols + 1) * (rows + 1))

    for (r in 0..rows) {
        val fv = r.toFloat() / rows              // 0 = 画面顶部 = 最远
        val z = farZ + (nearZ - farZ) * fv
        for (c in 0..cols) {
            val fu = c.toFloat() / cols
            val x = (fu - 0.5f) * widthMeters
            val hRaw = (bmp.getPixel(c, r) shr 16 and 0xFF).toFloat() / 255f
            val y = baseY + hRaw * maxHeight
            positions.add(Vector3(x, y, z))
            uvs.add(Vector2(fu, 1f - fv))
            normals.add(Vector3(0f, 1f, 0f))
        }
    }

    val idx = ArrayList<Int>(cols * rows * 6)
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val a = r * (cols + 1) + c
            val b = a + 1
            val d = a + (cols + 1)
            val e = d + 1
            // 逆时针（从上方看），法线朝上
            idx.add(a); idx.add(b); idx.add(d)
            idx.add(b); idx.add(e); idx.add(d)
        }
    }

    val mesh = MeshResource.createWithMeshModel(
        MeshModel(positions, idx, normals, emptyList(), uvs),
        name = "terrain"
    )
    val tex = TextureResource.load(path = textureAsset, loadType = LoadType.FROM_ASSETS)
    retainedTextures.add(tex)

    val material = PhysicallyBasedMaterial.create(BlendingMode.OPAQUE).apply {
        setBaseColor(Color4(1f, 1f, 1f, 1f))
        setBaseColorTexture(tex)
        setEmissiveColor(Color4(1f, 1f, 1f, 1f))
        setEmissiveTexture(tex)
        setRoughness(1f)
    }

    val e = Entity().apply {
        components.set(ModelComponent(mesh, material))
        components.set(TransformComponent(Vector3.ZERO, EulerAngles(tiltX, 0f, 0f), Vector3.ONE))
    }
    parent.addChild(e)
    println("TERRAIN_BUILT ${cols}x${rows} 顶点${positions.size}")
}
