package dji.sampleV5.aircraft.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dji.sdk.wpmz.value.mission.WaylineLocationCoordinate3D
import dji.v5.utils.common.LogUtils
import okhttp3.*
import okio.ByteString
import java.util.UUID
import kotlin.time.Duration

class WaypointWebSocketClient(
    private val serverUrl: String,
    private val onWaypointsReceived: (List<CoordinateDto>) -> Unit,
    private val onMissionCommand: (MissionCommand) -> Unit,
    private val onKnowledgeCommand: ((String) -> Unit)? = null  // 修改：接收场景类型参数
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()

    fun connect() {
        val request = Request.Builder()
            .url(serverUrl)
            .addHeader("X-Client-Id", UUID.randomUUID().toString())  // 通过
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                LogUtils.i("WebSocket", "连接成功")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    // 尝试解析为统一的消息格式
                    val message = gson.fromJson(text, WebSocketMessage::class.java)

                    when (message.type) {
                        "waypoints" -> {
                            // 解析航点数据
                            val type = object : TypeToken<List<CoordinateDto>>() {}.type
                            // 转换 data 部分为 JSON 字符串再解析为 List<CoordinateDto>
                            val coordinates: List<CoordinateDto> = gson.fromJson(
                                gson.toJson(message.data),
                                type
                            )

//                            val waypoints = coordinates.map {
//                                WaylineLocationCoordinate3D(it.latitude, it.longitude, it.altitude)
//                            }
                            // 直接回调航点数据
                            onWaypointsReceived(coordinates)
                            LogUtils.i("WebSocket", "收到 ${coordinates.size} 个航点")
                        }

                        "command" -> {
                            // 解析任务命令
                            val command = gson.fromJson(
                                gson.toJson(message.data),
                                MissionCommand::class.java
                            )

                            onMissionCommand(command)
                            LogUtils.i("WebSocket", "收到命令: ${command.action}")
                        }

                        "knowledge" -> {
                            // 触发跳转到知识图谱界面，支持场景类型参数
                            try {
                                val dataMap = message.data as? Map<*, *>
                                val sceneType = dataMap?.get("sceneType") as? String ?: "scene1"
                                onKnowledgeCommand?.invoke(sceneType)
                                LogUtils.i("WebSocket", "收到知识图谱跳转命令，场景类型: $sceneType")
                            } catch (e: Exception) {
                                // 如果解析失败，使用默认场景
                                onKnowledgeCommand?.invoke("scene1")
                                LogUtils.i("WebSocket", "收到知识图谱跳转命令（使用默认场景）")
                            }
                        }

                        else -> {
                            LogUtils.w("WebSocket", "未知消息类型: ${message.type}")
                        }
                    }
                } catch (e: Exception) {
                    LogUtils.e("WebSocket", "解析消息失败: ${e.message}")
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                LogUtils.i("WebSocket", "收到二进制消息")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                LogUtils.i("WebSocket", "连接关闭: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                LogUtils.e("WebSocket", "连接失败: ${t.message}")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "客户端主动断开")
    }

    fun sendStatus(status: String) {
        val message = mapOf(
            "type" to "status",
            "data" to mapOf("status" to status)
        )
        webSocket?.send(gson.toJson(message))
    }

    // 数据传输对象
    data class CoordinateDto(
        val latitude: Double,
        val longitude: Double,
        val altitude: Double,
        val duration: Double
    )

    data class WebSocketMessage(
        val type: String,
        val data: Any
    )

    data class MissionCommand(
        val action: String,  // "start", "pause", "resume", "stop"
        val params: Map<String, Any>? = null
    )
}