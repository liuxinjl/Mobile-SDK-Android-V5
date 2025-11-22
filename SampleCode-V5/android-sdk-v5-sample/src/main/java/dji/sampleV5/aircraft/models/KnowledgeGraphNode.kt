package dji.sampleV5.aircraft.models

/**
 * 三元节点数据模型
 * 表示知识图谱中的三元组关系：主体 - 关系 - 客体
 */
data class KnowledgeGraphNode(
    val id: String,
    val subject: String,      // 主体
    val predicate: String,    // 关系/谓词
    val `object`: String,     // 客体
    val description: String = "",  // 描述
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 示例数据生成器
 */
object KnowledgeGraphDataGenerator {

    fun getSampleData(): List<KnowledgeGraphNode> {
        return listOf(
            KnowledgeGraphNode(
                id = "1",
                subject = "无人机",
                predicate = "执行",
                `object` = "航点任务",
                description = "无人机通过航点任务进行自动飞行"
            ),
            KnowledgeGraphNode(
                id = "2",
                subject = "航点任务",
                predicate = "包含",
                `object` = "航点坐标",
                description = "航点任务由多个航点坐标组成"
            ),
            KnowledgeGraphNode(
                id = "3",
                subject = "航点坐标",
                predicate = "具有",
                `object` = "经纬度高度",
                description = "每个航点包含经度、纬度和高度信息"
            ),
            KnowledgeGraphNode(
                id = "4",
                subject = "无人机",
                predicate = "连接",
                `object` = "WebSocket服务器",
                description = "无人机通过WebSocket接收实时指令"
            ),
            KnowledgeGraphNode(
                id = "5",
                subject = "WebSocket服务器",
                predicate = "发送",
                `object` = "航点数据",
                description = "服务器向无人机推送航点坐标列表"
            ),
            KnowledgeGraphNode(
                id = "6",
                subject = "航点任务",
                predicate = "支持",
                `object` = "云台控制",
                description = "任务执行期间可以控制云台角度"
            ),
            KnowledgeGraphNode(
                id = "7",
                subject = "无人机",
                predicate = "具有",
                `object` = "飞行状态",
                description = "包括准备、执行、暂停、完成等状态"
            ),
            KnowledgeGraphNode(
                id = "8",
                subject = "航点",
                predicate = "设置",
                `object` = "悬停动作",
                description = "航点可配置到达后的悬停时间"
            ),
            KnowledgeGraphNode(
                id = "9",
                subject = "KMZ文件",
                predicate = "存储",
                `object` = "航点任务",
                description = "航点任务可以导出为KMZ格式文件"
            ),
            KnowledgeGraphNode(
                id = "10",
                subject = "WebSocket",
                predicate = "传输",
                `object` = "控制命令",
                description = "支持start、pause、resume、stop命令"
            ),
            KnowledgeGraphNode(
                id = "11",
                subject = "无人机",
                predicate = "使用",
                `object` = "DJI SDK V5",
                description = "基于DJI Mobile SDK V5进行开发"
            ),
            KnowledgeGraphNode(
                id = "12",
                subject = "航点任务",
                predicate = "配置",
                `object` = "飞行速度",
                description = "可为每个航点设置飞行速度"
            )
        )
    }
}

