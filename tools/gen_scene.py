"""
诗境漫游 · 望庐山瀑布 分层图（第二版：贴近青绿山水参考图的构图与气质）

构图约定：
  天幕 sky：上暖下淡，晨光在左上
  远景 far：两层淡青远山，山脚被云气吃掉
  中景 mid：左右两座高峰夹出中央峡谷，主瀑布落下，山腰紫云横带
  近景 near：左右墨松框住画面，底部崖台与水潭
  水雾 mist：瀑布脚下
全部程序化绘制，不使用任何外部素材。
"""
import math
import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

W, H = 2048, 1280
OUT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "assets" / "scene"
OUT.mkdir(parents=True, exist_ok=True)

random.seed(715)


def rgba(h, a=255):
    return ((h >> 16) & 0xFF, (h >> 8) & 0xFF, h & 0xFF, a)


def _rgb(c):
    return c if isinstance(c, tuple) else ((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF)


def vgrad(size, top, bottom, alpha_top=255, alpha_bottom=255):
    top, bottom = _rgb(top), _rgb(bottom)
    w, h = size
    img = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(img)
    for y in range(h):
        t = y / max(1, h - 1)
        col = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        a = int(alpha_top + (alpha_bottom - alpha_top) * t)
        d.line([(0, y), (w, y)], fill=col + (a,))
    return img


def ridge_curve(width, peaks, wobble=0.012, steps=4):
    pts = []
    for i in range(0, width + 1, steps):
        x = i / width
        y = 1.05
        for cx, hw, top in peaks:
            dx = abs(x - cx) / hw
            if dx < 1:
                y = min(y, top + (1.05 - top) * (dx ** 1.55))
        if y < 1.04:
            y += wobble * math.sin(23 * x + 1.7) + wobble * 0.6 * math.sin(61 * x + 0.4)
        pts.append((i, y * H))
    return pts


def mountain(width, height, peaks, top_color, bottom_color, rim=None):
    layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    pts = ridge_curve(width, peaks)
    poly = pts + [(width, height), (0, height)]

    mask = Image.new("L", (width, height), 0)
    ImageDraw.Draw(mask).polygon(poly, fill=255)
    grad = vgrad((width, height), top_color, bottom_color)
    layer.paste(grad, (0, 0), mask)

    if rim:
        edge = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        ed = ImageDraw.Draw(edge)
        ed.line(pts, fill=_rgb(rim) + (255,), width=4, joint="curve")
        edge = edge.filter(ImageFilter.GaussianBlur(3))
        edge.putalpha(edge.getchannel("A").point(lambda v: int(v * 0.55)))
        layer = Image.alpha_composite(layer, edge)
    return layer


def cloud_band(width, height, cy, color, alpha, thick, blur):
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for _ in range(26):
        x = random.uniform(-0.05, 1.05) * width
        rw = random.uniform(0.08, 0.22) * width
        rh = thick * height * random.uniform(0.5, 1.4)
        dx = random.uniform(-0.01, 0.01) * height
        d.ellipse([x - rw, cy * height + dx - rh, x + rw, cy * height + dx + rh],
                  fill=color + (alpha,))
    return img.filter(ImageFilter.GaussianBlur(blur))


# ── 天幕 ──
sky = vgrad((W, H), (0xF7, 0xEE, 0xD9), (0xD9, 0xDE, 0xCB))
glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
ImageDraw.Draw(glow).ellipse([-0.25 * W, -0.35 * H, 0.55 * W, 0.45 * H],
                             fill=(0xFF, 0xE6, 0xB8, 130))
glow = glow.filter(ImageFilter.GaussianBlur(120))
sky = Image.alpha_composite(sky, glow)
sky.save(OUT / "sky.png")
print("写出 sky.png")

# ── 远景 ──
far = Image.new("RGBA", (W, H), (0, 0, 0, 0))
far = Image.alpha_composite(
    far,
    mountain(W, H, [(0.12, 0.42, 0.40), (0.46, 0.36, 0.46), (0.86, 0.40, 0.38)],
             0xC7D0C0, 0xADBAA8),
)
far = Image.alpha_composite(
    far,
    mountain(W, H, [(0.30, 0.26, 0.50), (0.70, 0.24, 0.48), (0.50, 0.14, 0.55)],
             0xB6C2B2, 0x9AA895),
)
far = Image.alpha_composite(far, cloud_band(W, H, 0.60, (0xFA, 0xF5, 0xE8), 150, 0.035, 26))
far.save(OUT / "far.png")
print("写出 far.png")

# ── 中景 ──
mid = Image.new("RGBA", (W, H), (0, 0, 0, 0))
mid = Image.alpha_composite(
    mid,
    mountain(W, H, [(0.18, 0.40, 0.22), (0.36, 0.16, 0.34)], 0x8FA895, 0x5A7268,
             rim=0xD7E2CF),
)
mid = Image.alpha_composite(
    mid,
    mountain(W, H, [(0.82, 0.40, 0.20), (0.64, 0.16, 0.32)], 0x8FA895, 0x5A7268,
             rim=0xD7E2CF),
)
mid = Image.alpha_composite(
    mid,
    mountain(W, H, [(0.42, 0.13, 0.40)], 0x7B9688, 0x4E665F, rim=0xCFDCC8),
)
mid = Image.alpha_composite(
    mid,
    mountain(W, H, [(0.58, 0.13, 0.38)], 0x7B9688, 0x4E665F, rim=0xCFDCC8),
)

fall = Image.new("RGBA", (W, H), (0, 0, 0, 0))
fd = ImageDraw.Draw(fall)
for cx, w0, y0, y1, a in [
    (0.500, 30, 0.33, 0.72, 246),
    (0.500, 54, 0.72, 0.80, 220),
    (0.470, 12, 0.40, 0.70, 150),
    (0.532, 12, 0.38, 0.70, 150),
]:
    fd.polygon([(W * cx - w0, H * y0), (W * cx + w0, H * y0),
                (W * cx + w0 * 1.35, H * y1), (W * cx - w0 * 1.35, H * y1)],
               fill=(255, 255, 255, a))
fall = fall.filter(ImageFilter.GaussianBlur(2))
mid = Image.alpha_composite(mid, fall)

mid = Image.alpha_composite(mid, cloud_band(W, H, 0.42, (0x8B, 0x7B, 0xA8), 120, 0.028, 30))
mid = Image.alpha_composite(mid, cloud_band(W, H, 0.50, (0xF6, 0xF1, 0xE4), 140, 0.030, 26))
mid.save(OUT / "mid.png")
print("写出 mid.png")

# ── 近景 ──
near = Image.new("RGBA", (W, H), (0, 0, 0, 0))
ledge = ridge_curve(W, [(0.30, 0.34, 0.80), (0.70, 0.34, 0.82), (0.50, 0.12, 0.86)],
                    wobble=0.010)
mask = Image.new("L", (W, H), 0)
ImageDraw.Draw(mask).polygon(ledge + [(W, H), (0, H)], fill=255)
near.paste(vgrad((W, H), (0x3E4641), (0x151917)), (0, 0), mask)
nd = ImageDraw.Draw(near)

nd.ellipse([W * 0.34, H * 0.80, W * 0.66, H * 0.90], fill=rgba(0xDCE9E4, 220))
nd.ellipse([W * 0.39, H * 0.815, W * 0.61, H * 0.86], fill=rgba(0xF1F6EE, 200))


def pine(cx, base_y, scale, tilt=0.0):
    col = rgba(0x171B18)
    tw = 15 * scale
    nd.polygon([(W * cx - tw, H * base_y), (W * cx + tw, H * base_y),
                (W * cx + tw * 0.5 + W * tilt, H * base_y - 470 * scale),
                (W * cx - tw * 0.5 + W * tilt, H * base_y - 470 * scale)], fill=col)
    for k in range(6):
        ty = H * base_y - (150 + k * 62) * scale
        half = (170 - k * 22) * scale
        sy = ty - 46 * scale
        nd.polygon([(W * cx - half + W * tilt * 0.6, ty),
                    (W * cx + half + W * tilt * 0.6, ty),
                    (W * cx + W * tilt, sy)], fill=col)


pine(0.075, 1.03, 1.25, tilt=0.004)
pine(0.155, 1.02, 0.85, tilt=-0.003)
pine(0.925, 1.04, 1.15, tilt=-0.004)
pine(0.855, 1.03, 0.80, tilt=0.003)
near = near.filter(ImageFilter.GaussianBlur(0.7))
near.save(OUT / "near.png")
print("写出 near.png")

# ── 水雾 ──
mist = Image.new("RGBA", (W, H), (0, 0, 0, 0))
md = ImageDraw.Draw(mist)
for cx, cy, rw, rh, a in [
    (0.500, 0.735, 0.150, 0.040, 170),
    (0.440, 0.700, 0.075, 0.026, 120),
    (0.565, 0.705, 0.070, 0.024, 115),
    (0.500, 0.790, 0.120, 0.030, 140),
]:
    md.ellipse([W * (cx - rw), H * (cy - rh), W * (cx + rw), H * (cy + rh)],
               fill=(0xEC, 0xF3, 0xEA, a))
mist = mist.filter(ImageFilter.GaussianBlur(22))
mist.save(OUT / "mist.png")
print("写出 mist.png")
print("第二版全部完成")
