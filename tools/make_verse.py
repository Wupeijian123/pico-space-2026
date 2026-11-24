"""
生成四句诗的文字贴图（透明底，竖排），入画态里叠在山水上。
每句一张，与光段一一对应：晨光→第一句，上午→第二句，正午→第三句，夜→第四句。
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

OUT = Path("E:/pico/projects/demo/app/src/main/assets/scene_verse")
OUT.mkdir(parents=True, exist_ok=True)

KAI = "C:/Windows/Fonts/simkai.ttf"
W, H = 480, 1080          # 竖幅
FONT_SIZE = 92
INK = (244, 239, 226)     # 近宣纸白，压在深色画面上可读
SEAL = (168, 50, 45)

LINES = [
    ("日照香炉生紫烟", "李白"),
    ("遥看瀑布挂前川", ""),
    ("飞流直下三千尺", ""),
    ("疑是银河落九天", ""),
]


def vertical(text: str, font: ImageFont.FreeTypeFont, gap: int) -> Image.Image:
    """竖排：每个字一张小图竖着摞"""
    chars = list(text)
    cell = int(font.size * 1.35)
    img = Image.new("RGBA", (cell, cell * len(chars) + gap * (len(chars) - 1)), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    y = 0
    for ch in chars:
        bbox = d.textbbox((0, 0), ch, font=font)
        w = bbox[2] - bbox[0]
        h = bbox[3] - bbox[1]
        d.text(((cell - w) / 2 - bbox[0], y + (cell - h) / 2 - bbox[1]), ch, font=font, fill=INK + (255,))
        y += cell + gap
    return img


f = ImageFont.truetype(KAI, FONT_SIZE)

for i, (line, author) in enumerate(LINES):
    canvas = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    body = vertical(line, f, gap=26)
    x = (W - body.width) // 2
    canvas.alpha_composite(body, (x, 80))

    d = ImageDraw.Draw(canvas)
    if author:
        fa = ImageFont.truetype(KAI, 44)
        a = vertical(author, fa, gap=10)
        canvas.alpha_composite(a, (W - a.width - 40, 80))
        # 朱砂小印
        d.rounded_rectangle([W - 130, 80 + a.height + 24, W - 60, 80 + a.height + 94],
                            radius=8, outline=SEAL + (255,), width=4)
        fs = ImageFont.truetype(KAI, 30)
        d.text((W - 118, 80 + a.height + 36), "太白", font=fs, fill=SEAL + (255,))

    # 句子序号，淡淡的
    fn = ImageFont.truetype(KAI, 30)
    d.text((26, H - 70), f"{i + 1} / 4", font=fn, fill=(244, 239, 226, 150))

    name = OUT / f"verse_{i}.png"
    canvas.save(name)
    print(name.name, canvas.size)
