package dji.sampleV5.aircraft.network

import android.util.Log
import dji.sampleV5.aircraft.models.WayPointV3VM
import dji.sampleV5.aircraft.network.models.ReceivedWaypoint
import dji.sampleV5.aircraft.util.ToastUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dji.sampleV5.aircraft.utils.KMZTestUtil
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.utils.common.DiskUtil
import dji.v5.utils.common.ContextUtil
import dji.sdk.wpmz.value.mission.WaylineMission
import dji.sdk.wpmz.value.mission.WaylineMissionConfig
import com.dji.wpmzsdk.common.data.Template
import com.dji.wpmzsdk.manager.WPMZManager
import okhttp3.*
import okio.ByteString // 新增导入
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit // 新增导入

// 注意：不再继承 WebSocketListener，而是在内部实例化

class WebSocketMissionManager(private val viewModel: WayPointV3VM) {

    private val TAG = "WebSocketMissionManager"

    // 使用 OkHttpClient.Builder 创建客户端，用于 WebSocket 连接
    private val CLIENT: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // WebSocket 不需要读取超时
        .build()

    private var webSocket: WebSocket? = null

    // 状态锁：防止同时处理多个任务，实现去重
    @Volatile private var isMissionProcessing = false
    private var isConnected = false

    /**
     * 【核心启动方法】：启动 WebSocket 连接并等待接收任务数据。
     */
    fun startAndListen(websocketUrl: String) {
        if (isConnected) {
            ToastUtils.showToast("WebSocket连接已启动")
            return
        }

        connectToWebSocket(websocketUrl)
        ToastUtils.showToast("正在连接到 $websocketUrl...")
    }

    /**
     * 【核心实现】：建立 WebSocket 连接，包含所有回调逻辑。
     */
    private fun connectToWebSocket(websocketUrl: String) {
        val request = Request.Builder().url(websocketUrl).build()

        // 【关键】：这里创建并赋值 webSocket 实例
        webSocket = CLIENT.newWebSocket(request, object : WebSocketListener() {

            // 1. 连接成功
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // 确保在主线程执行 UI/状态操作
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Log.i(TAG, "Connection opened successfully.")
                    isConnected = true
                    ToastUtils.showToast("✅ WebSocket连接成功！等待任务数据...")
                }
            }

            // 2. 接收到文本消息
            override fun onMessage(webSocket: WebSocket, text: String) {
                // 确保在主线程执行 UI/逻辑操作
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Log.i(TAG, "Received message: $text")

                    if (isMissionProcessing) {
                        Log.w(TAG, "Ignoring incoming data: Previous mission is still processing.")
                        ToastUtils.showToast("🚨 忽略重复数据，任务处理中...")
                        return@post
                    }
                    isMissionProcessing = true // 锁定状态

                    // 解析和处理数据
                    handleIncomingJson(text)
                }
            }

            // 3. 连接故障
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                // 确保在主线程执行 UI/状态操作
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Log.e(TAG, "Connection failed: ${t.message}")
                    ToastUtils.showToast("🔴 WebSocket连接失败: ${t.message}")
                    this@WebSocketMissionManager.webSocket = null
                    isConnected = false
                    isMissionProcessing = false // 释放锁
                }
            }

            // 4. 连接关闭
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                // 确保在主线程执行 UI/状态操作
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Log.i(TAG, "Connection closed. Code: $code, Reason: $reason")
                    isConnected = false
                    isMissionProcessing = false
                    ToastUtils.showToast("❌ WebSocket连接已断开")
                }
            }

            // 可选：接收到二进制数据
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // 根据需要实现二进制数据处理
            }
        })
    }

    /**
     * 关闭 WebSocket 连接。
     */
    fun stop() {
        webSocket?.close(1000, "User initiated disconnect")
        webSocket = null
        isConnected = false
        isMissionProcessing = false
        CLIENT.dispatcher.cancelAll() // 取消所有挂起的调用
        ToastUtils.showToast("WebSocket连接已断开")
    }

    // --- 核心数据处理逻辑 ---

    private fun handleIncomingJson(jsonString: String) {
        val gson = Gson()
        val type: Type = object : TypeToken<List<ReceivedWaypoint>>() {}.type

        val receivedPoints: List<ReceivedWaypoint> = try {
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing failed: ${e.message}")
            ToastUtils.showToast("数据解析失败: 请检查JSON格式")
            isMissionProcessing = false // 解析失败，释放锁
            return
        }

        if (receivedPoints.isEmpty()) {
            ToastUtils.showToast("接收到的航点列表为空")
            isMissionProcessing = false
            return
        }

        // 转换为 MSDK V5 任务对象，并上传执行
        generateUploadAndStartMission(receivedPoints)
    }

    // ... (其他方法保持不变) ...

    /**
     * 【新增】自动化启动入口：直接接受航点列表，并开始生成、上传和启动流程。
     */
    fun startMissionProcessWithData(waypoints: List<ReceivedWaypoint>) {
        generateUploadAndStartMission(waypoints)
    }

    /**
     * 任务生成、写入 KMZ 文件、上传和执行。
     */
    private fun generateUploadAndStartMission(points: List<ReceivedWaypoint>) {
        // ... (内容保持不变，这是你之前修正过的 KMZ 生成和上传启动逻辑) ...

        // 1. 生成内存中的 WaylineMission 对象
        val mission: WaylineMission = KMZTestUtil.createMissionFromReceivedPoints(points)
        val missionConfig: WaylineMissionConfig = KMZTestUtil.createMissionConfig(viewModel.missionGlobalModel)

        // 2. 写入临时 KMZ 文件
        val tempKmzPath = DiskUtil.getExternalCacheDirPath(ContextUtil.getContext(), "runtime_mission.kmz")

        WPMZManager.getInstance().generateKMZFile(
            tempKmzPath,
            mission,
            missionConfig,
            Template()
        )
        // 检查文件是否存在于本地磁盘
        val generatedFile = File(tempKmzPath)

        if (!generatedFile.exists()) {
            ToastUtils.showToast("❌ 任务文件生成失败，文件不存在!")
            isMissionProcessing = false
            return
        }

        ToastUtils.showToast("✅ 任务文件已生成，开始上传...")

        // 3. 上传任务
        viewModel.pushKMZFileToAircraft(
            tempKmzPath,
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    ToastUtils.showToast("🚀 任务上传成功，即将启动...")

                    val missionFileName = File(tempKmzPath).name

                    viewModel.startMission(
                        missionFileName,
                        listOf(0),
                        object : CommonCallbacks.CompletionCallback {
                            override fun onSuccess() {
                                ToastUtils.showToast("✅ 任务启动指令已发送")
                                isMissionProcessing = false
                            }

                            override fun onFailure(error: IDJIError) {
                                ToastUtils.showToast("❌ 任务启动失败: ${error.description()}")
                                isMissionProcessing = false
                            }
                        }
                    )
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("❌ 任务上传失败: ${error.description()}")
                    isMissionProcessing = false
                }
            }
        )
    }
}