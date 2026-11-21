"""
诗境漫游 · 望庐山瀑布 分层剪影生成器
纯程序化绘制，不依赖任何外部素材（规避版权）。
输出 4 张 RGBA PNG 到 app/src/main/assets/scene/：
  far.png   远景：淡青绿山峦 + 云气
  mid.png   中景：青绿主峰 + 紫云 + 主瀑布
  near.png  近景：崖台 + 水潭 + 松树剪影
  mist.png  水雾
"""
import math
import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

W, H = 2048, 1280
OUT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "assets" / "scene"
OUT.mkdir(parents=True, exist_ok=True)

random.seed(20261121)


def hex2rgb(h):
    return ((h >> 16) & 0xFF, (h >> 8) & 0xFF, h & 0xFF)


def ridge(x, base, amp, freqs, phases):
    """多重正弦叠出的山脊线，x ∈ [0,1]"""
    y = base
    for a, f, p in zip(amp, freqs, phases):
        y += a * math.sin(2 * math.pi * f * x + p)
    return y


def fill_ridge(draw, w, h, pts, top, bottom):
    """按山脊折线填充，颜色从上到下渐变"""
    poly = pts + [(w, h), (0, h)]
    draw.polygon(poly, fill=top)
    # 上色渐变：用逐行叠加实现
    mask = Image.new("L", (w, h), 0)
    ImageDraw.Draw(mask).polygon(poly, fill=255)
    grad = Image.new("RGB", (w, h))
    gd = ImageDraw.Draw(grad)
    for y in range(h):
        t = y / max(1, h - 1)
        col = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        gd.line([(0, y), (w, y)], fill=col)
    return grad, mask


def make_layer(ridges, filename, blur=1.2):
    """ridges: list of (base, amp, freqs, phases, color_top, color_bottom)"""
    canvas = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    for base, amp, freqs, phases, ctop, cbot in ridges:
        pts = []
        for i in range(0, W + 1, 4):
            x = i / W
            y = ridge(x, base, amp, freqs, phases) * H
            pts.append((i, y))
        grad, mask = fill_ridge(ImageDraw.Draw(canvas), W, H, pts, hex2rgb(ctop), hex2rgb(cbot))
        layer = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        layer.paste(grad, (0, 0), mask)
        canvas = Image.alpha_composite(canvas, layer)
    if blur:
        canvas = canvas.filter(ImageFilter.GaussianBlur(blur))
    canvas.save(OUT / filename)
    print("写出", OUT / filename)


# ── 远景：三叠远山，淡到几乎融进天光 ──
make_layer(
    [
        (0.52, [0.030, 0.018, 0.010], [0.7, 1.9, 3.4], [0.4, 2.1, 0.8], 0xCFD6C9, 0xB9C3B5),
        (0.60, [0.038, 0.020, 0.012], [0.9, 2.3, 4.1], [2.4, 0.6, 1.7], 0xBFC9BB, 0xA6B2A4),
    ],
    "far.png",
    blur=2.4,
)

# ── 中景：左右主峰，中间留出瀑布的缺口 ──
mid = Image.new("RGBA", (W, H), (0, 0, 0, 0))
d = ImageDraw.Draw(mid)

def peak(cx, half_w, top, base=1.02, smooth=1.9):
    """以 cx 为中心、宽 half_w 的尖峰"""
    pts = []
    for i in range(0, W + 1, 3):
        x = i / W
        dx = abs(x - cx) / half_w
        if dx >= 1:
            y = 1.05
        else:
            y = top + (base - top) * (dx ** smooth)
            y += 0.012 * math.sin(29 * x + cx * 20)
        pts.append((i, y * H))
    return pts

for cx, hw, tp, ctop, cbot in [
    (0.20, 0.34, 0.24, 0x8FA796, 0x5F7A72),
    (0.80, 0.34, 0.21, 0x8FA796, 0x5F7A72),
    (0.36, 0.20, 0.38, 0x7B9688, 0x546C65),
    (0.64, 0.20, 0.40, 0x7B9688, 0x546C65),
]:
    pts = peak(cx, hw, tp)
    grad, mask = fill_ridge(d, W, H, pts, hex2rgb(ctop), hex2rgb(cbot))
    sub = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sub.paste(grad, (0, 0), mask)
    mid = Image.alpha_composite(mid, sub)
    d = ImageDraw.Draw(mid)

# 主瀑布：中间缺口里的白色竖带
fall = Image.new("RGBA", (W, H), (0, 0, 0, 0))
fd = ImageDraw.Draw(fall)
for cx, w0, y0, y1, a0, a1 in [
    (0.500, 46, 0.30, 0.78, 235, 150),
    (0.470, 16, 0.36, 0.74, 150, 90),
    (0.531, 14, 0.34, 0.75, 140, 85),
]:
    fd.polygon(
        [
            (W * cx - w0, H * y0),
            (W * cx + w0, H * y0),
            (W * cx + w0 * 1.5, H * y1),
            (W * cx - w0 * 1.5, H * y1),
        ],
        fill=(255, 255, 255, a0),
    )
fall = fall.filter(ImageFilter.GaussianBlur(3))
mid = Image.alpha_composite(mid, fall)

# 紫云：压在山腰的横向云气
cloud = Image.new("RGBA", (W, H), (0, 0, 0, 0))
cd = ImageDraw.Draw(cloud)
for cx, cy, rw, rh, a in [
    (0.30, 0.44, 0.26, 0.035, 110),
    (0.66, 0.40, 0.28, 0.030, 95),
    (0.50, 0.50, 0.34, 0.026, 80),
]:
    cd.ellipse(
        [W * (cx - rw), H * (cy - rh), W * (cx + rw), H * (cy + rh)],
        fill=(0x8B, 0x7B, 0xA8, a),
    )
cloud = cloud.filter(ImageFilter.GaussianBlur(26))
mid = Image.alpha_composite(mid, cloud)
mid.save(OUT / "mid.png")
print("写出", OUT / "mid.png")

# ── 近景：崖台 + 水潭 + 松树 ──
near = Image.new("RGBA", (W, H), (0, 0, 0, 0))
nd = ImageDraw.Draw(near)

# 崖台（底部整条）
ledge = []
for i in range(0, W + 1, 4):
    x = i / W
    y = 0.80 + 0.03 * math.sin(6.0 * x) + 0.012 * math.sin(17 * x + 1.2)
    ledge.append((i, y * H))
grad, mask = fill_ridge(nd, W, H, ledge, hex2rgb(0x3E4641), hex2rgb(0x171B18))
sub = Image.new("RGBA", (W, H), (0, 0, 0, 0))
sub.paste(grad, (0, 0), mask)
near = Image.alpha_composite(near, sub)
nd = ImageDraw.Draw(near)

# 水潭
nd.ellipse([W * 0.33, H * 0.795, W * 0.67, H * 0.875], fill=(0xDD, 0xE7, 0xE0, 210))
nd.ellipse([W * 0.38, H * 0.805, W * 0.62, H * 0.845], fill=(0xEF, 0xF3, 0xEC, 190))

# 左右松树剪影
def pine(cx, base_y, scale, color=(0x1B, 0x1F, 0x1D, 255)):
    trunk_w = 16 * scale
    nd.rectangle(
        [W * cx - trunk_w, H * base_y - 420 * scale, W * cx + trunk_w, H * base_y],
        fill=color,
    )
    for k in range(5):
        ty = H * base_y - (140 + k * 78) * scale
        half = (150 - k * 24) * scale
        nd.polygon(
            [
                (W * cx - half, ty + 60 * scale),
                (W * cx + half, ty + 60 * scale),
                (W * cx, ty - 40 * scale),
            ],
            fill=color,
        )

pine(0.055, 1.02, 1.15)
pine(0.945, 1.03, 1.05)
near = near.filter(ImageFilter.GaussianBlur(0.8))
near.save(OUT / "near.png")
print("写出", OUT / "near.png")

# ── 水雾 ──
mist = Image.new("RGBA", (W, H), (0, 0, 0, 0))
md = ImageDraw.Draw(mist)
for cx, cy, rw, rh, a in [
    (0.50, 0.74, 0.16, 0.045, 150),
    (0.44, 0.70, 0.09, 0.030, 110),
    (0.57, 0.71, 0.08, 0.026, 100),
]:
    md.ellipse([W * (cx - rw), H * (cy - rh), W * (cx + rw), H * (cy + rh)],
               fill=(0xEC, 0xF1, 0xE8, a))
mist = mist.filter(ImageFilter.GaussianBlur(24))
mist.save(OUT / "mist.png")
print("写出", OUT / "mist.png")
print("全部完成")
