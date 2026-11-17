package com.example.demo.content

/** 一首诗的完整数据：正文、逐句注释、赏析、意象标签、诗境参数 */
data class PoemLine(val text: String, val note: String)

data class Poem(
    val title: String,
    val dynasty: String,
    val author: String,
    val lines: List<PoemLine>,
    val commentary: String,
    val images: List<String>
)

val lushanPoem =
    Poem(
        title = "望庐山瀑布",
        dynasty = "唐",
        author = "李白",
        lines =
            listOf(
                PoemLine("日照香炉生紫烟", "香炉：香炉峰。紫烟：日光下云雾呈紫色，如香烟缭绕。"),
                PoemLine("遥看瀑布挂前川", "挂前川：瀑布垂落如白练悬挂在山前的水面上。"),
                PoemLine("飞流直下三千尺", "飞流：水流疾驰。三千尺：极言其高，非实数。"),
                PoemLine("疑是银河落九天", "疑：仿佛。九天：天的最高处。以银河比瀑布，既写形，也写神。")
            ),
        commentary =
            "全诗四句，一句一景，一句一转。前两句写所见：日光、紫烟、远山、白练，是静观；" +
                "第三句骤然拉近，写水势之疾、落差之大，是动势；第四句忽然抬头，把瀑布认作银河，由实入虚。" +
                "李白的高明在于：他没有形容水，而是形容“看水的人”心里的那一下恍惚。",
        images = listOf("山", "水", "烟", "银河")
    )
