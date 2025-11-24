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
    val imageUrl: String? = null,  // 图片URL（可选）
    val compressionRatio: Double = 0.9,  // 信息压缩比（默认0.9%）
    val nodeType: String = "临时节点（用户请求）",  // 节点类型
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 示例数据生成器
 */
object KnowledgeGraphDataGenerator {

    /**
     * 获取示例数据
     * @param sceneType 场景类型: "scene1" - 交通路口场景, "scene2" - 无人机监控场景, "scene3" - 停车场场景
     */
    fun getSampleData(sceneType: String = "scene1"): List<KnowledgeGraphNode> {
        return when (sceneType) {
            "scene2" -> getScene2Data()
            "scene3" -> getScene3Data()
            else -> getScene1Data()
        }
    }

    /**
     * 场景1：交通路口场景数据
     */
    private fun getScene1Data(): List<KnowledgeGraphNode> {
        return listOf(
            // 场景描述信息
            KnowledgeGraphNode(
                id = "scene",
                subject = "场景",
                predicate = "描述",
                `object` = "街道交叉路口",
                description = "这是一个俯视视角的街道交叉路口场景。路口有清晰的斑马线，路面为灰色沥青材质，分布有多个井盖。画面中有多名行人和车辆。左侧路边有两名行人正在行走，旁边停放着一辆银色轿车。路口中央有三辆电动车正在行驶，其中一辆为绿色，骑车人穿着粉色上衣；另一辆为蓝色，骑车人穿着白色上衣；第三辆为浅色，骑车人穿着深色衣物。右侧道路上有一辆银色轿车正在行驶，同时还有两辆电动车在行驶，骑车人分别穿着浅色和深色衣物。整体场景显示了日常交通状况，无明显异常活动",
                imageUrl = "assets://images/knowledge.jpg",
                compressionRatio = 0.92  // 场景整体压缩比 0.92%
            ),
            // 对象信息三元组
            KnowledgeGraphNode(
                id = "1",
                subject = "行人",
                predicate = "行走",
                `object` = "路边",
                description = "左侧路边有两名行人正在行走",
                compressionRatio = 0.89
            ),
            KnowledgeGraphNode(
                id = "2",
                subject = "行人",
                predicate = "行走",
                `object` = "人行道",
                description = "行人在人行道上正常行走",
                compressionRatio = 0.91
            ),
            KnowledgeGraphNode(
                id = "3",
                subject = "电动车",
                predicate = "行驶",
                `object` = "道路",
                description = "路口中央有三辆电动车正在行驶",
                compressionRatio = 0.88
            ),
            KnowledgeGraphNode(
                id = "4",
                subject = "电动车",
                predicate = "行驶",
                `object` = "交叉路口",
                description = "电动车通过交叉路口，包括绿色、蓝色和浅色电动车",
                compressionRatio = 0.93
            ),
            KnowledgeGraphNode(
                id = "5",
                subject = "电动车",
                predicate = "行驶",
                `object` = "右侧道路",
                description = "右侧道路上有两辆电动车在行驶，骑车人分别穿着浅色和深色衣物",
                compressionRatio = 0.87
            ),
            KnowledgeGraphNode(
                id = "6",
                subject = "轿车",
                predicate = "行驶",
                `object` = "右侧道路",
                description = "右侧道路上有一辆银色轿车正在行驶",
                compressionRatio = 0.94
            ),
            // 补充细节信息
            KnowledgeGraphNode(
                id = "7",
                subject = "银色轿车",
                predicate = "停放",
                `object` = "左侧路边",
                description = "左侧路边停放着一辆银色轿车",
                compressionRatio = 0.90
            ),
            KnowledgeGraphNode(
                id = "8",
                subject = "路口",
                predicate = "具有",
                `object` = "斑马线",
                description = "路口有清晰的斑马线标识",
                compressionRatio = 0.85
            ),
            KnowledgeGraphNode(
                id = "9",
                subject = "路面",
                predicate = "材质为",
                `object` = "灰色沥青",
                description = "路面为灰色沥青材质，分布有多个井盖",
                compressionRatio = 0.92
            ),
            KnowledgeGraphNode(
                id = "10",
                subject = "绿色电动车",
                predicate = "骑车人穿着",
                `object` = "粉色上衣",
                description = "绿色电动车的骑车人穿着粉色上衣",
                compressionRatio = 0.88
            ),
            KnowledgeGraphNode(
                id = "11",
                subject = "蓝色电动车",
                predicate = "骑车人穿着",
                `object` = "白色上衣",
                description = "蓝色电动车的骑车人穿着白色上衣",
                compressionRatio = 0.91
            ),
            KnowledgeGraphNode(
                id = "12",
                subject = "交通状况",
                predicate = "评估为",
                `object` = "日常正常",
                description = "整体场景显示了日常交通状况，无明显异常活动",
                compressionRatio = 0.86
            )
        )
    }

    /**
     * 场景2：无人机监控场景数据
     */
    private fun getScene2Data(): List<KnowledgeGraphNode> {
        return listOf(
            // 场景描述信息
            KnowledgeGraphNode(
                id = "scene2",
                subject = "场景",
                predicate = "描述",
                `object` = "道路监控场景",
                description = "图像显示一名女孩在道路上行走，她背着一个彩色背包，穿着红色裤子和白色上衣。一辆白色轿车正在道路上行驶，旁边停放着一台黄色的起重机。图像由无人机拍摄，无人机处于待命状态，操控员和指挥官各一人。无人机的经纬度为经度-77.0023°，纬度38.8974°，高度150.00米，电量为432000瓦秒。",
                imageUrl = "assets://images/knowledge2.jpg",
                compressionRatio = 0.49,  // 0.0049 转换为百分比 0.49%
                nodeType = "临时节点（用户请求）"
            ),
            // 对象信息三元组
            KnowledgeGraphNode(
                id = "s2_1",
                subject = "女孩",
                predicate = "行走",
                `object` = "道路",
                description = "一名女孩在道路上行走，背着彩色背包，穿着红色裤子和白色上衣",
                compressionRatio = 0.48,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s2_2",
                subject = "车辆",
                predicate = "行驶",
                `object` = "道路",
                description = "一辆白色轿车正在道路上行驶",
                compressionRatio = 0.51,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s2_3",
                subject = "起重机",
                predicate = "停放",
                `object` = "道路",
                description = "一台黄色的起重机停放在道路旁边",
                compressionRatio = 0.47,
                nodeType = "临时节点（用户请求）"
            ),
            // 补充细节信息
            KnowledgeGraphNode(
                id = "s2_4",
                subject = "女孩",
                predicate = "穿着",
                `object` = "红色裤子",
                description = "女孩穿着红色裤子",
                compressionRatio = 0.52,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s2_5",
                subject = "女孩",
                predicate = "穿着",
                `object` = "白色上衣",
                description = "女孩穿着白色上衣",
                compressionRatio = 0.50,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s2_6",
                subject = "女孩",
                predicate = "背着",
                `object` = "彩色背包",
                description = "女孩背着一个彩色背包",
                compressionRatio = 0.49,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s2_7",
                subject = "白色轿车",
                predicate = "颜色为",
                `object` = "白色",
                description = "轿车为白色",
                compressionRatio = 0.48,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s2_8",
                subject = "起重机",
                predicate = "颜色为",
                `object` = "黄色",
                description = "起重机为黄色",
                compressionRatio = 0.46,
                nodeType = "临时节点（用户请求）"
            ),
            // 无人机信息
            KnowledgeGraphNode(
                id = "s2_9",
                subject = "无人机",
                predicate = "状态为",
                `object` = "待命",
                description = "无人机处于待命状态",
                compressionRatio = 0.53,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s2_10",
                subject = "无人机",
                predicate = "位置",
                `object` = "经度-77.0023°纬度38.8974°",
                description = "无人机经纬度为经度-77.0023°，纬度38.8974°，高度150.00米",
                compressionRatio = 0.45,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s2_11",
                subject = "无人机",
                predicate = "电量",
                `object` = "432000瓦秒",
                description = "无人机电量为432000瓦秒",
                compressionRatio = 0.50,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s2_12",
                subject = "操作人员",
                predicate = "配置",
                `object` = "操控员和指挥官各一人",
                description = "操控员和指挥官各一人",
                compressionRatio = 0.51,
                nodeType = "临时节点（用户请求）"
            )
        )
    }

    /**
     * 场景3：停车场场景数据
     */
    private fun getScene3Data(): List<KnowledgeGraphNode> {
        return listOf(
            // 场景描述信息
            KnowledgeGraphNode(
                id = "scene3",
                subject = "场景",
                predicate = "描述",
                `object` = "停车场监控场景",
                description = "设备类型:图像ZC无人机, 经度:-77.0023°, 纬度:38.8974°, 高度:150.00m, 电量:432000ws, 战斗状态: 待命, 操控员: 1人, 指挥官: 1人; 停车场内停放着多辆汽车，包括白色轿车、黑色轿车和一辆白色面包车，一名行人正在停车场内行走。",
                imageUrl = "assets://images/knowledge3.jpg",
                compressionRatio = 0.23,  // 0.0023 转换为百分比 0.23%
                nodeType = "临时节点（用户请求）"
            ),
            // 对象信息三元组
            KnowledgeGraphNode(
                id = "s3_1",
                subject = "行人",
                predicate = "行走",
                `object` = "停车场",
                description = "一名行人正在停车场内行走",
                compressionRatio = 0.22,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s3_2",
                subject = "车辆",
                predicate = "停放",
                `object` = "停车场",
                description = "停车场内停放着多辆汽车",
                compressionRatio = 0.24,
                nodeType = "临时节点（用户请求）"
            ),
            // 车辆详细信息
            KnowledgeGraphNode(
                id = "s3_3",
                subject = "白色轿车",
                predicate = "停放",
                `object` = "停车场",
                description = "停车场内停放着白色轿车",
                compressionRatio = 0.21,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s3_4",
                subject = "黑色轿车",
                predicate = "停放",
                `object` = "停车场",
                description = "停车场内停放着黑色轿车",
                compressionRatio = 0.23,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s3_5",
                subject = "白色面包车",
                predicate = "停放",
                `object` = "停车场",
                description = "停车场内停放着一辆白色面包车",
                compressionRatio = 0.25,
                nodeType = "临时节点（用户请求）"
            ),
            // 无人机设备信息
            KnowledgeGraphNode(
                id = "s3_6",
                subject = "图像ZC无人机",
                predicate = "设备类型",
                `object` = "图像采集",
                description = "设备类型为图像ZC无人机",
                compressionRatio = 0.20,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s3_7",
                subject = "图像ZC无人机",
                predicate = "位置",
                `object` = "经度-77.0023°纬度38.8974°",
                description = "无人机经纬度为经度-77.0023°，纬度38.8974°，高度150.00m",
                compressionRatio = 0.22,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s3_8",
                subject = "图像ZC无人机",
                predicate = "电量",
                `object` = "432000ws",
                description = "无人机电量为432000ws",
                compressionRatio = 0.24,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s3_9",
                subject = "图像ZC无人机",
                predicate = "战斗状态",
                `object` = "待命",
                description = "无人机战斗状态为待命",
                compressionRatio = 0.23,
                nodeType = "临时节点（用户请求）"
            ),
            // 操作人员信息
            KnowledgeGraphNode(
                id = "s3_10",
                subject = "操作人员",
                predicate = "操控员",
                `object` = "1人",
                description = "操控员1人",
                compressionRatio = 0.25,
                nodeType = "临时节点（用户请求）"
            ),
            KnowledgeGraphNode(
                id = "s3_11",
                subject = "操作人员",
                predicate = "指挥官",
                `object` = "1人",
                description = "指挥官1人",
                compressionRatio = 0.24,
                nodeType = "临时节点（用户请求）"
            ),
            // 压缩信息
            KnowledgeGraphNode(
                id = "s3_12",
                subject = "数据压缩",
                predicate = "压缩大小",
                `object` = "389.0000",
                description = "压缩后数据大小为389.0000",
                compressionRatio = 0.23,
                nodeType = "临时节点（用户请求）"
            )
        )
    }
}

