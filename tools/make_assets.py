"""
生成比赛提交用视觉物料：图标、海报（两种尺寸）、详情图。
素材：用户自己用豆包生成的青绿山水原图（版权归用户）。
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

REF = Path("E:/pico/refs/ref.jpg")
OUT = Path("E:/pico/projects/demo/docs/submission")
OUT.mkdir(parents=True, exist_ok=True)

KAI = "C:/Windows/Fonts/simkai.ttf"

INK = (28, 27, 25)
PAPER = (242, 234, 218)
CINNABAR = (168, 50, 45)

art = Image.open(REF).convert("RGB")


def font(size):
    return ImageFont.truetype(KAI, size)


def cover(img, w, h):
    """等比放大并居中裁切"""
    r = max(w / img.width, h / img.height)
    nw, nh = int(img.width * r + 1), int(img.height * r + 1)
    im = img.resize((nw, nh), Image.LANCZOS)
    x = (nw - w) // 2
    y = (nh - h) // 2
    return im.crop((x, y, x + w, y + h))


def vgrad(w, h, top, bottom):
    img = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(img)
    for y in range(h):
        t = y / max(1, h - 1)
        d.line([(0, y), (w, y)], fill=tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)))
    return img


def seal(size, text="诗境"):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([2, 2, size - 2, size - 2], radius=int(size * 0.12),
                        outline=CINNABAR + (255,), width=max(3, size // 34))
    f = font(int(size * 0.40))
    bbox = d.textbbox((0, 0), text, font=f)
    d.text(((size - (bbox[2] - bbox[0])) / 2 - bbox[0],
            (size - (bbox[3] - bbox[1])) / 2 - bbox[1]), text, font=f, fill=CINNABAR + (255,))
    return img


# ── 图标 1024×1024 ──
icon = Image.new("RGB", (1024, 1024), PAPER)
bg = cover(art, 1024, 1024).filter(ImageFilter.GaussianBlur(1.5))
icon.paste(bg, (0, 0))
veil = Image.new("RGBA", (1024, 1024), (242, 234, 218, 92))
icon = Image.alpha_composite(icon.convert("RGBA"), veil)
ic = Image.new("RGBA", (1024, 1024), (0, 0, 0, 0))
d = ImageDraw.Draw(ic)
f_big = font(300)
title = "诗境"
bbox = d.textbbox((0, 0), title, font=f_big)
d.text(((1024 - (bbox[2] - bbox[0])) / 2 - bbox[0], 250), title, font=f_big, fill=INK + (255,))
d.text((300, 640), "漫游", font=font(170), fill=INK + (235,))
ic.alpha_composite(seal(170), (700, 700))
icon = Image.alpha_composite(icon, ic).convert("RGB")
icon.save(OUT / "icon-1024.png")
print("icon-1024.png")

TAGLINE = "戴上头显，走到诗人站过的位置，看他看的山，听他听的水"


def poster(w, h, square=False):
    base = cover(art, w, h).convert("RGBA")
    # 左侧压暗，给文字留空间
    shade = Image.new("L", (w, h), 0)
    sd = ImageDraw.Draw(shade)
    band = int(w * (0.46 if not square else 0.62))
    for x in range(band):
        sd.line([(x, 0), (x, h)], fill=int(200 * (1 - x / band) ** 1.4))
    dark = Image.new("RGBA", (w, h), (18, 22, 20, 255))
    dark.putalpha(shade)
    base = Image.alpha_composite(base, dark)

    layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    x0 = int(w * 0.075)
    title_size = int(h * (0.155 if not square else 0.115))
    f_title = font(title_size)
    d.text((x0, int(h * 0.28)), "诗境漫游", font=f_title, fill=PAPER + (255,))
    d.text((x0, int(h * 0.28) + int(title_size * 1.28)),
           "望庐山瀑布 · 空间诗词漫游", font=font(int(title_size * 0.36)), fill=(226, 216, 196, 240))
    # 朱砂细线
    ly = int(h * 0.28) + int(title_size * 1.95)
    d.line([(x0, ly), (x0 + int(title_size * 3.1), ly)], fill=CINNABAR + (255,), width=max(3, h // 320))
    # 定位语换行
    f_tag = font(int(title_size * 0.26))
    words = TAGLINE
    line_len = 13 if not square else 15
    ty = ly + int(title_size * 0.45)
    for i in range(0, len(words), line_len):
        d.text((x0, ty), words[i:i + line_len], font=f_tag, fill=(236, 230, 214, 235))
        ty += int(title_size * 0.42)
    d.text((x0, h - int(h * 0.115)), "PICO Space Program 2026 · PICO Spatial 赛道",
           font=font(int(title_size * 0.22)), fill=(210, 200, 180, 220))
    layer.alpha_composite(seal(int(h * 0.085)), (x0, int(h * 0.135)))
    out = Image.alpha_composite(base, layer).convert("RGB")
    name = f"poster-{w}x{h}.png"
    out.save(OUT / name)
    print(name)


poster(2560, 1440)
poster(1024, 1024, square=True)

# ── 详情图 2560×1440：三栏说明 ──
W, H = 2560, 1440
detail = Image.new("RGB", (W, H), (233, 230, 217))
dd = ImageDraw.Draw(detail)

shot = Image.open("E:/pico/shot_layers2.png").convert("RGB")
shot = cover(shot, 1180, 900)
detail.paste(shot, (110, 300))

head = Image.new("RGBA", (W, H), (0, 0, 0, 0))
hd = ImageDraw.Draw(head)
hd.text((110, 120), "诗境漫游 · 一首诗的两个空间", font=font(96), fill=INK + (255,))
hd.text((110, 240), "书房态：一卷三栏读诗（题签 / 正文 / 注释赏析）；点句高亮，随读随注",
        font=font(40), fill=(70, 66, 58, 255))

right_x = 1370
hd.text((right_x, 300), "入画态", font=font(64), fill=CINNABAR + (255,))
hd.text((right_x + 230, 318), "Full Space", font=font(30), fill=(120, 112, 98, 255))

items = [
    ("01", "入诗", "点印章「入诗」，卷轴化作一幅山水，人在岩台之上"),
    ("02", "视差", "远景 10 米、前景 6 米，走动时山会错位，像真的站在谷中"),
    ("03", "观景", "瀑布、紫云、松影、水潭都在原位，转个头就是另一重山"),
    ("04", "出画", "一句读完回到书房，继续读下一句、写自己的注"),
]
ty = 420
for no, name, desc in items:
    hd.text((right_x, ty), no, font=font(34), fill=CINNABAR + (255,))
    hd.text((right_x + 90, ty - 8), name, font=font(50), fill=INK + (255,))
    hd.text((right_x + 260, ty + 6), desc, font=font(31), fill=(66, 62, 55, 255))
    ty += 175

hd.text((110, H - 120), "自制程序化分层 + 用户自绘青绿山水主稿；无第三方素材",
        font=font(32), fill=(110, 104, 92, 255))
detail = Image.alpha_composite(detail.convert("RGBA"), head).convert("RGB")
detail.save(OUT / "detail-2560x1440.png")
print("detail-2560x1440.png")
print("输出目录:", OUT)
