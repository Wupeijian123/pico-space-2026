"""
把平面主稿变成真 3D 地形（浮雕网格 → GLB）。

思路：按画面明暗估深度——越暗越近（松、崖）、越亮越远（天、雾），
叠加"上远下近"的先验，生成高度场网格，贴同一张图。
走动时会有真实的视差与遮挡，不再只是两块板子。

输出：app/src/main/assets/scene/terrain.glb
"""
from pathlib import Path

import numpy as np
import trimesh
from PIL import Image, ImageFilter


SRC = Path("E:/pico/projects/demo/app/src/main/assets/scene/bg.jpg")
OUT = Path("E:/pico/projects/demo/app/src/main/assets/scene/terrain.glb")

COLS, ROWS = 220, 124        # 网格密度（顶点数 = 221×125 ≈ 2.7 万）
NEAR_Z, FAR_Z = -6.0, -62.0  # 近处 6 米、远处 62 米
WIDTH = 46.0                 # 近处画幅宽（米）
HEIGHT_MAX = 13.0            # 山体最高


def depth_map(img: Image.Image) -> np.ndarray:
    """返回 0(近) ~ 1(远) 的深度图"""
    small = img.convert("L").resize((COLS + 1, ROWS + 1), Image.BILINEAR).filter(ImageFilter.GaussianBlur(3))
    g = np.asarray(small, dtype=np.float32) / 255.0  # 行 0 = 画面顶部（远）
    v = np.linspace(0.0, 1.0, ROWS + 1)[:, None]  # 0 顶部 → 1 底部
    # 上远下近（权重 0.62）+ 亮远暗近（权重 0.38）
    d = 0.62 * (1.0 - v) + 0.38 * g
    d = np.clip(d, 0.0, 1.0)
    # 轻平滑，去掉照片噪点带来的毛刺
    k = np.ones((3, 3), dtype=np.float32) / 9.0
    for _ in range(2):
        pad = np.pad(d, 1, mode="edge")
        d = sum(k[i, j] * pad[i:i + d.shape[0], j:j + d.shape[1]] for i in range(3) for j in range(3))
    return d


def build() -> trimesh.Trimesh:
    img = Image.open(SRC).convert("RGB")
    d = depth_map(img)

    us = np.linspace(0.0, 1.0, COLS + 1)
    vs = np.linspace(0.0, 1.0, ROWS + 1)[::-1]  # 从底部开始建行，便于索引
    U, V = np.meshgrid(us, vs)

    # v=0 顶部(远) v=1 底部(近)
    z = NEAR_Z + (FAR_Z - NEAR_Z) * (1.0 - V)
    x = (U - 0.5) * WIDTH
    # 高度：越"近"（d 小）抬得越高，山体因此有厚度
    y = -1.6 + (1.0 - d) * HEIGHT_MAX

    verts = np.stack([x, y, z], axis=-1).reshape(-1, 3)

    faces = []
    for r in range(ROWS):
        for c in range(COLS):
            i0 = r * (COLS + 1) + c
            i1 = i0 + 1
            i2 = i0 + (COLS + 1)
            i3 = i2 + 1
            faces.append([i0, i2, i1])
            faces.append([i1, i2, i3])

    uv = np.stack([U, V], axis=-1).reshape(-1, 2)
    uv[:, 1] = 1.0 - uv[:, 1]  # trimesh/glTF 的 v 轴向上

    mesh = trimesh.Trimesh(vertices=verts, faces=np.array(faces), process=False)
    mesh.visual = trimesh.visual.texture.TextureVisuals(uv=uv, image=img.resize((1024, 576)))
    return mesh


def save_heightmap() -> Path:
    """导出 8bit 高度图，供 Kotlin 端建网格（避免 glTF 自带 PBR 材质在无光环境变黑）"""
    img = Image.open(SRC).convert("RGB")
    d = depth_map(img)
    h = np.clip((1.0 - d) * 255.0, 0, 255).astype(np.uint8)
    out = SRC.parent / "heightmap.png"
    Image.fromarray(h, mode="L").save(out)
    print("heightmap.png", Image.open(out).size)
    return out


if __name__ == "__main__":
    save_heightmap()
    m = build()
    m.export(OUT)
    size = OUT.stat().st_size / 1024 / 1024
    print(f"terrain.glb 顶点 {len(m.vertices)} 面 {len(m.faces)} 尺寸 {size:.2f} MB")
    print("包围盒", m.bounds.round(2).tolist())
