"""
预烘焙四段光照：晨光 / 上午 / 正午 / 夜
输入 bg.jpg + fore.png（1280×720），输出 *_morning / *_forenoon / *_noon / *_night。
运行时不再改材质颜色（那条路验证失效），直接换贴图 / 换实体。
"""
from pathlib import Path

from PIL import Image

SRC = Path("E:/pico/projects/demo/app/src/main/assets/scene")
OUT = Path("E:/pico/projects/demo/app/src/main/assets/scene")
BAKED = Path("E:/pico/projects/demo/app/src/main/assets/scene_baked")
BAKED.mkdir(parents=True, exist_ok=True)

PHASES = {
    # 名称: (tint 颜色, 混合比例, 亮度增益)
    "morning": ((242, 201, 138), 0.30, 1.04),
    "forenoon": ((234, 242, 246), 0.16, 1.06),
    "noon": ((255, 244, 220), 0.10, 1.12),
    "night": ((27, 42, 74), 0.62, 0.78),
}


def bake(img: Image.Image, tint, alpha, gain) -> Image.Image:
    rgb = img.convert("RGB")
    px = rgb.load()
    w, h = rgb.size
    tr, tg, tb = tint
    for y in range(h):
        for x in range(w):
            r, g, b = px[x, y]
            r = r * (1 - alpha) + tr * alpha
            g = g * (1 - alpha) + tg * alpha
            b = b * (1 - alpha) + tb * alpha
            px[x, y] = (
                min(255, int(r * gain)),
                min(255, int(g * gain)),
                min(255, int(b * gain)),
            )
    if img.mode == "RGBA":
        rgb = rgb.convert("RGBA")
        rgb.putalpha(img.getchannel("A"))
    return rgb


bg = Image.open(SRC / "bg.jpg")
fore = Image.open(SRC / "fore.png")

for name, (tint, alpha, gain) in PHASES.items():
    bake(bg, tint, alpha, gain).convert("RGB").save(BAKED / f"bg_{name}.jpg", quality=88)
    bake(fore, tint, alpha, gain).save(BAKED / f"fore_{name}.png")
    print(f"bg_{name}.jpg / fore_{name}.png")

total = sum(f.stat().st_size for f in BAKED.glob("*"))
print(f"四套共 {total/1024/1024:.1f} MB，目录 {BAKED}")
