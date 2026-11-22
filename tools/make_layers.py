"""
把用户豆包原图拆成「远景」「前景」两层，用于空间视差。

远景 bg.jpg：整幅图，略微模糊并压暗一点点（视觉上更远）
前景 fore.png：按「越暗越靠下越是前景」的规则抠出松树、崖台、水潭

纯 PIL 实现（本机没有 numpy）。
"""
from pathlib import Path

from PIL import Image, ImageChops, ImageFilter

SRC = Path("E:/pico/refs/ref.jpg")
OUT = Path("E:/pico/projects/demo/app/src/main/assets/scene")
OUT.mkdir(parents=True, exist_ok=True)

src = Image.open(SRC).convert("RGB")
W, H = src.size
ratio = 16 / 9
if W / H > ratio:
    nw = int(H * ratio)
    off = (W - nw) // 2
    box = (off, 0, off + nw, H)
else:
    nh = int(W / ratio)
    off = (H - nh) // 2
    box = (0, off, W, off + nh)

img = src.crop(box).resize((1280, 720), Image.LANCZOS)

# ── 远景：整幅，轻模糊 + 略压暗，营造空气透视 ──
bg = img.filter(ImageFilter.GaussianBlur(1.1))
bg = Image.eval(bg, lambda v: int(v * 0.94))
bg.save(OUT / "bg.jpg", quality=90)
print("写出 bg.jpg")

# ── 前景遮罩：暗部 × 画面下半部 ──
lum = img.convert("L")
# 暗部：亮度低于 110 的逐渐算作前景
dark = lum.point(lambda v: max(0, min(255, int((110 - v) * 255 / 110))))

# 竖向权重：画面 45% 以下才开始算，越靠下越强
grad = Image.linear_gradient("L").resize((1280, 720), Image.BILINEAR)
vert = grad.point(lambda v: max(0, min(255, int((v - 115) * 255 / (255 - 115)))))

mask = ImageChops.multiply(dark, vert)
# 提对比：砍掉低位噪声，保住主体，再轻微羽化
mask = mask.point(lambda v: max(0, min(255, int((v - 26) * 1.7))))
mask = mask.filter(ImageFilter.GaussianBlur(5))

fore = img.convert("RGBA")
fore.putalpha(mask)
fore.save(OUT / "fore.png")
print("写出 fore.png")

# 统计前景占比，确认没抠成整片黑
hist = mask.histogram()
tot = sum(hist)
solid = sum(hist[80:])
print(f"前景遮罩覆盖 {solid/tot*100:.1f}% 的画面")
