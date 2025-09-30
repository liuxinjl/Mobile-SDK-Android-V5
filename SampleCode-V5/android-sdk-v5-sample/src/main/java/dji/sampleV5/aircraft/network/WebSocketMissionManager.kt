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
import java.io.File
import java.lang.reflect.Type

class WebSocketMissionManager(private val viewModel: WayPointV3VM) : WebSocketListener() {

    private val TAG = "WebSocketMissionManager"
    private val CLIENT = OkHttpClient()
    private var webSocket: WebSocket? = null

    // 状态锁：防止同时处理多个任务，实现去重
    @Volatile private var isMissionProcessing = false
    private var isConnected = false

    /**
     * 连接到 WebSocket 服务器并开始监听。
     */
    fun start(url: String) {
        if (isConnected) {
            ToastUtils.showToast("WebSocket连接已启动")
            return
        }

        val request = Request.Builder().url(url).build()
        webSocket = CLIENT.newWebSocket(request, this)
        ToastUtils.showToast("正在连接到 $url...")
    }

    /**
     * 关闭 WebSocket 连接。
     */
    fun stop() {
        webSocket?.close(1000, "User initiated disconnect")
        webSocket = null
        isConnected = false
        isMissionProcessing = false
        ToastUtils.showToast("WebSocket连接已断开")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.i(TAG, "Connection opened successfully.")
        isConnected = true
        ToastUtils.showToast("✅ WebSocket连接成功")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.i(TAG, "Received message: $text")

        // --- 策略 1B: 状态锁去重 ---
        if (isMissionProcessing) {
            Log.w(TAG, "Ignoring incoming data: Previous mission is still processing.")
            ToastUtils.showToast("🚨 忽略重复数据，任务处理中...")
            return
        }
        isMissionProcessing = true // 锁定状态

        // 解析和处理数据
        handleIncomingJson(text)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "Connection failed: ${t.message}")
        ToastUtils.showToast("❌ WebSocket连接失败: ${t.message}")
        this.webSocket = null
        isConnected = false
        isMissionProcessing = false
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.i(TAG, "Connection closed. Code: $code, Reason: $reason")
        isConnected = false
        isMissionProcessing = false
    }

    // --- 核心数据处理逻辑 ---

    private fun handleIncomingJson(jsonString: String) {
        val gson = Gson()
        // 使用 TypeToken 处理 List<ReceivedWaypoint> 这种泛型类型
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

    /**
     * 任务生成、写入 KMZ 文件、上传和执行。
     */
    private fun generateUploadAndStartMission(points: List<ReceivedWaypoint>) {
        // 1. 生成内存中的 WaylineMission 对象
        val mission: WaylineMission = KMZTestUtil.createMissionFromReceivedPoints(points)
        // 从 ViewModel 获取全局配置模型
        val missionConfig: WaylineMissionConfig = KMZTestUtil.createMissionConfig(viewModel.missionGlobalModel)

        // 2. 写入临时 KMZ 文件
        val tempKmzPath = DiskUtil.getExternalCacheDirPath(ContextUtil.getContext(), "runtime_mission.kmz")
        val result = WPMZManager.getInstance().generateKMZFile(tempKmzPath, mission, missionConfig, Template())

        if (!result) {
            ToastUtils.showToast("❌ 任务文件生成失败!")
            isMissionProcessing = false // 失败，释放锁
            return
        }

        ToastUtils.showToast("✅ 任务文件已生成，开始上传...")

        // 3. 上传任务
        viewModel.pushKMZFileToAircraft(
            tempKmzPath,
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    // 上传成功，立即启动任务
                    ToastUtils.showToast("🚀 任务上传成功，即将启动...")

                    val missionFileName = File(tempKmzPath).name
                    // 启动任务 (假设只执行第一个 Wayline，ID=0)
                    viewModel.startMission(missionFileName, listOf(0))

                    isMissionProcessing = false // 成功，释放锁
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("❌ 任务上传失败: ${error.description()}")
                    isMissionProcessing = false // 失败，释放锁
                }
            }
        )
    }
}