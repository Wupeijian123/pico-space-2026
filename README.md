# 诗境漫游 · Poemscape

PICO Space Program 2026 · PICO Spatial 空间应用创作赛道参赛作品。

把古诗词鉴赏从"读注释"变成"走进画里"。第一首样板是李白的《望庐山瀑布》。

> 戴上头显，走到诗人站过的位置，看他看的山，听他听的水。

---

## 一、核心体验：一首诗，两个空间

**书房态（Shared Space，多窗口）**
- 一卷三栏的批注本：左栏题签与印章，中栏四句正文，右栏注释与赏析
- 点任意一句，该句转为朱砂红并出现界格，右栏对应的注释放大加粗
- 底部印章：注释 / 赏析 / 入诗。前两个展开独立窗口，可与主卷并排

**入画态（Full Space Stage）**
- 山水铺满视野：紫云、远山、主瀑布、松影、水潭，岩台上有白衣文人的背影
- 画面分两层：远景 10 米、前景（松树 / 崖台）6.2 米。走动时前后景错位，产生真实视差
- 光照随诗句推进：晨光 → 上午 → 正午 → 夜，四段各是一套预烘焙贴图
- 读满 30 秒自动出画，回到卷轴继续读下一句

## 二、五个核心动作

| 动作 | 状态 |
| --- | --- |
| 展卷（打开诗卷） | 已实现 |
| 入诗（进入山水） | 已实现 |
| 品鉴（注释与赏析） | 已实现 |
| 留印（收藏与题跋） | 待实现 |
| 出画（回到书房） | 已实现（印章切换 + 30 秒兜底 + 返回键） |

## 三、技术实现

- **语言 / 框架**：Kotlin + PICO Spatial SDK 6.2.8（Spatial UI / Entity 场景 / SpatialView / Stage）
- **窗口**：`WindowContainer` + `Form.Planar` + `WorldScale.Fixed`，窗口内是 Compose 界面
- **场景**：`SpatialView` + 自建带 UV 四边形网格（`MeshResource.createWithMeshModel`）
- **材质**：自发光 + 贴图（Stage 内无环境光，纯 PBR 材质会是全黑）
- **光照**：四套预烘焙图 + 移动实体切换（本 SDK 上运行时改材质颜色不生效，已实测）

### 三个踩过的坑（写在这里，方便复现）

1. `MeshResource.createPlane` **不带 UV**，贴图只会采样 (0,0) 那一个像素，整块消失。
2. 顶点顺序必须逆时针（从 +Z 看），顺时针会被背面剔除。
3. `TextureResource` 必须保活引用，否则被回收后材质变白；`Stage` 常驻声明会在启动时直接把人扔进场景，必须按需创建 + 显式 `openStage`。

## 四、目录

```
app/src/main/java/com/example/demo/
  Main.kt                 应用入口，多窗口与 Stage 声明
  AppState.kt             是否在入画态、读到第几句
  content/ScrollPage.kt   一卷三栏主窗（点句、印章）
  content/NotesAndCommentary.kt  注释窗 / 赏析窗
  content/PoemData.kt     诗的数据结构（正文 / 注释 / 赏析 / 意象）
  scene/LushanLayers.kt   场景：四光段实体、带 UV 四边形、切换逻辑
  scene/LightVeil.kt      光段枚举
  scene/LushanImmersion.kt 入画态入口（自动光段循环、30 秒出画兜底）
app/src/main/assets/
  scene/                  主稿 bg、前景 fore、天幕 skyfill
  scene_baked/            四段光照预烘焙图（晨 / 上午 / 正午 / 夜）
tools/
  bake_lights.py          生成四段光照图
  make_layers.py          从原图拆远景 / 前景
  make_assets.py          生成图标 / 海报 / 详情图
docs/
  design-doc.md           设计框架与场景规格
  submission/             提交物料与三份说明
```

## 五、构建与运行

```bash
export JAVA_HOME=< jdk21 路径 >
export ANDROID_SDK_ROOT=< android-sdk 路径 >
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.demo/.platform.LaunchActivity
```

环境：JDK 21、Android SDK（build-tools 35）、PICO Spatial SDK 6.2.8、PICO Emulator 6.2.1。

## 六、素材与版权

- 青绿山水主稿由团队成员使用豆包生成，版权归团队所有；程序只做裁切、拆层、光照烘焙
- 不使用任何第三方图库、模型或音频素材
- 字体：应用未内嵌字体文件

详见 `docs/submission/依赖与素材说明.md`。

## 七、AI 工具说明

- 主视觉：豆包（图像生成）
- 代码、构建脚本、文档：Claude / Hermes Agent 辅助，全部经本地编译与模拟器运行验证

## 八、进度

- [x] 书房态一卷三栏、点句高亮
- [x] 入画态原图满屏、两层视差
- [x] 四段光照预烘焙与切换
- [x] 诗句与光段绑定
- [x] 出画（印章 / 返回 / 30 秒兜底）
- [ ] 留印（收藏与题跋）
- [ ] 更多诗词扩展
- [ ] 空间音频（瀑布声、水声）
