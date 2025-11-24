package dji.sampleV5.aircraft.pages


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.DocumentsContract
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.dji.industry.mission.DocumentsUtils
import com.dji.wpmzsdk.common.data.HeightMode
import com.dji.wpmzsdk.common.data.Template
import com.dji.wpmzsdk.common.utils.kml.model.WaypointActionType
import com.dji.wpmzsdk.manager.WPMZManager
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.databinding.FragWaypointv3PageBinding
import dji.sampleV5.aircraft.models.MissionGlobalModel
import dji.sampleV5.aircraft.models.WayPointV3VM
import dji.sampleV5.aircraft.util.DialogUtil
import dji.sampleV5.aircraft.util.ToastUtils
import dji.sampleV5.aircraft.utils.KMZTestUtil
import dji.sampleV5.aircraft.utils.KMZTestUtil.createWaylineMission
import dji.sampleV5.aircraft.utils.WaypointWebSocketClient
import dji.sampleV5.aircraft.utils.wpml.WaypointInfoModel
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.value.common.LocationCoordinate2D
import dji.sdk.keyvalue.value.flightcontroller.FlightMode
import dji.sdk.wpmz.jni.JNIWPMZManager
import dji.sdk.wpmz.value.mission.Wayline
import dji.sdk.wpmz.value.mission.WaylineActionInfo
import dji.sdk.wpmz.value.mission.WaylineActionType
import dji.sdk.wpmz.value.mission.WaylineExecuteWaypoint
import dji.sdk.wpmz.value.mission.WaylineExitOnRCLostAction
import dji.sdk.wpmz.value.mission.WaylineFinishedAction
import dji.sdk.wpmz.value.mission.WaylineLocationCoordinate2D
import dji.sdk.wpmz.value.mission.WaylineLocationCoordinate3D
import dji.sdk.wpmz.value.mission.WaylineMission
import dji.sdk.wpmz.value.mission.WaylineMissionConfig
import dji.sdk.wpmz.value.mission.WaylineWaypoint
import dji.sdk.wpmz.value.mission.WaylineWaypointGimbalHeadingMode
import dji.sdk.wpmz.value.mission.WaylineWaypointGimbalHeadingParam
import dji.sdk.wpmz.value.mission.WaylineWaypointYawMode
import dji.sdk.wpmz.value.mission.WaylineWaypointYawParam
import dji.sdk.wpmz.value.mission.WaylineWaypointYawPathMode
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.common.utils.GpsUtils
import dji.v5.manager.KeyManager
import dji.v5.manager.aircraft.simulator.SimulatorManager
import dji.v5.manager.aircraft.waypoint3.WPMZParserManager
import dji.v5.manager.aircraft.waypoint3.WaylineExecutingInfoListener
import dji.v5.manager.aircraft.waypoint3.WaypointActionListener
import dji.v5.manager.aircraft.waypoint3.WaypointMissionManager
import dji.v5.manager.aircraft.waypoint3.model.BreakPointInfo
import dji.v5.manager.aircraft.waypoint3.model.RecoverActionType
import dji.v5.manager.aircraft.waypoint3.model.WaylineExecutingInfo
import dji.v5.manager.aircraft.waypoint3.model.WaypointMissionExecuteState
import dji.v5.utils.common.AndUtil
import dji.v5.utils.common.ContextUtil
import dji.v5.utils.common.DeviceInfoUtil.getPackageName
import dji.v5.utils.common.DiskUtil
import dji.v5.utils.common.DocumentUtil
import dji.v5.utils.common.FileUtils
import dji.v5.utils.common.LogPath
import dji.v5.utils.common.LogUtils
import dji.v5.ux.accessory.DescSpinnerCell
import dji.v5.ux.map.MapWidget
import dji.v5.ux.mapkit.core.maps.DJIMap
import dji.v5.ux.mapkit.core.models.DJIBitmapDescriptor
import dji.v5.ux.mapkit.core.models.DJIBitmapDescriptorFactory
import dji.v5.ux.mapkit.core.models.DJILatLng
import dji.v5.ux.mapkit.core.models.annotations.DJIMarker
import dji.v5.ux.mapkit.core.models.annotations.DJIMarkerOptions
import dji.v5.ux.mapkit.core.models.annotations.DJIPolylineOptions
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.IOException
import dji.sdk.wpmz.value.mission.ActionAircraftHoverParam
import dji.sdk.wpmz.value.mission.ActionGimbalRotateParam


/**
 * @author feel.feng
 * @time 2022/02/27 9:30 上午
 * @description:
 */
class WayPointV3Fragment : DJIFragment() {

    private val wayPointV3VM: WayPointV3VM by activityViewModels()
    private var binding: FragWaypointv3PageBinding? = null
    private val WAYPOINT_SAMPLE_FILE_NAME: String = "waypointsample.kmz"
    private val WAYPOINT_SAMPLE_FILE_DIR: String = "waypoint/"
    private val WAYPOINT_SAMPLE_FILE_CACHE_DIR: String = "waypoint/cache/"
    private val WAYPOINT_FILE_TAG = ".kmz"
    private var unzipChildDir = "temp/"
    private var unzipDir = "wpmz/"
    private var mDisposable: Disposable? = null
    private val OPEN_FILE_CHOOSER = 0
    private val OPEN_DOCUMENT_TREE = 1
    private val OPEN_MANAGE_EXTERNAL_STORAGE = 2
    private val missionGlobalModel= MissionGlobalModel(10.0)

    private val showWaypoints: ArrayList<WaypointInfoModel> = ArrayList()
    private val interestPoint : WaylineLocationCoordinate3D = WaylineLocationCoordinate3D()
    private val pointMarkers: ArrayList<DJIMarker?> = ArrayList()
    var curMissionPath = ""
    val rootDir = DiskUtil.getExternalCacheDirPath(ContextUtil.getContext(), WAYPOINT_SAMPLE_FILE_DIR)
    var validLenth: Int = 2
    var curMissionExecuteState: WaypointMissionExecuteState? = null
    var selectWaylines: ArrayList<Int> = ArrayList()

    // websocket 相关
    private var webSocketClient: WaypointWebSocketClient? = null
    // 接收到的航点列表
    private var receivedWaypoints: ArrayList<WaypointWebSocketClient.CoordinateDto> = ArrayList()

    val hoverSeconds = 10.0



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragWaypointv3PageBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareMissionData()
        initView(savedInstanceState)
        initData()
        WPMZManager.getInstance().init(ContextUtil.getContext())

        // 界面初始化后执行 websocket 初始化
        initWebSocket()
    }

    /**
     * 初始化 websocket
     */
    private fun initWebSocket() {
        webSocketClient = WaypointWebSocketClient(
//            serverUrl = "ws://172.20.10.4:8080/waypoint",
            serverUrl = "ws://192.168.50.167:8080/waypoint",
            onWaypointsReceived = { waypoints ->
                requireActivity().runOnUiThread {
                    receivedWaypoints.clear()
                    receivedWaypoints.addAll(waypoints)
                    ToastUtils.showToast("收到 ${waypoints.size} 个航点")
                }
            },
            onMissionCommand = { command ->
                requireActivity().runOnUiThread {
                    handleMissionCommand(command)
                }
            },
            onKnowledgeCommand = { sceneType ->
                // 收到知识图谱命令，根据场景类型自动跳转
                requireActivity().runOnUiThread {
                    val sceneName = when (sceneType) {
                        "scene2" -> "无人机监控场景"
                        else -> "交通路口场景"
                    }
                    ToastUtils.showToast("收到知识图谱命令，正在跳转到$sceneName...")
                    navigateToKnowledgeGraph(sceneType)
                }
            }
        )

        webSocketClient?.connect()
    }

    private fun handleMissionCommand(command: WaypointWebSocketClient.MissionCommand) {
        when (command.action) {
            "start" -> {
                // 检查是否有航点数据
                if (receivedWaypoints.isEmpty()) {
                    ToastUtils.showToast("没有可用的航点数据")
                    return
                }
                // 执行启动任务的逻辑
                startMission()
            }

            "pause" -> {
                // 暂停任务
                pauseMission()
                ToastUtils.showToast("pause command received")
            }

            "resume" -> {
                // 恢复任务
                resumeMission()
                ToastUtils.showToast("resume command received")
            }

            "stop" -> {
                // 停止任务
                stopMission()
                ToastUtils.showToast("stop command received")
            }

            "knowledge" -> {
                requireActivity().runOnUiThread {
                    val dataMap = command.params as? Map<*, *>
                    val sceneType = dataMap?.get("sceneType") as? String ?: "scene1"
                    ToastUtils.showToast("正在进行知识融合处理...")
                    navigateToKnowledgeGraph(sceneType)
                }
            }


//            "waypoints" -> {
//                // 传入新的航点
//                // 停止当前任务
//                stopMission()
//                ToastUtils.showToast("waypoints command received")
//                //开始新任务
//                startMission()
//            }

            else -> {
                ToastUtils.showToast("未知命令: ${command.action}")
            }
        }
    }

    private fun startMission() {
        // 生成 Waypoint 信息
        showWaypoints.clear()
        receivedWaypoints.forEachIndexed { index, loc ->
            val wp = WaypointInfoModel()
            // 创建航点
            val waypoint = WaylineWaypoint().apply {
                waypointIndex = index
                location = WaylineLocationCoordinate2D(loc.latitude, loc.longitude)
                height = loc.altitude
                ellipsoidHeight = loc.altitude
                speed = 10.0
                useGlobalTurnParam = true
            }

            // 创建悬停动作 设置云台
            val hoverAction = WaylineActionInfo().apply {
                actionType = WaylineActionType.HOVER
                aircraftHoverParam = ActionAircraftHoverParam().apply {
                    hoverTime = loc.duration
                }
                // 设置云台动作为不动
                gimbalRotateParam = ActionGimbalRotateParam().apply {
                    enablePitch = false
                    enableYaw = false;
                    enableRoll = false;
                }
            }

            // 为当前航点绑定动作
            wp.waylineWaypoint = waypoint
            wp.actionInfos = listOf(hoverAction)   // ✅ 每个航点都包含悬停动作
            showWaypoints.add(wp)
        }

        // 发送状态更新
        webSocketClient?.sendStatus("mission_starting")

        // 执行任务启动逻辑
        // 生成KMZ文件、上传并执行任务
        generateAndStartMissionbyDJI()

        ToastUtils.showToast("任务已启动")
    }

    private fun pauseMission() {
        // 实现暂停逻辑
        webSocketClient?.sendStatus("mission_paused")
        wayPointV3VM.pauseMission(object : CommonCallbacks.CompletionCallback {
            override fun onSuccess() {
                ToastUtils.showToast("pauseMission Success")
            }

            override fun onFailure(error: IDJIError) {
                ToastUtils.showToast("pauseMission Failed " + getErroMsg(error))
            }
        })
    }

    private fun resumeMission() {
        // 实现恢复逻辑
        webSocketClient?.sendStatus("mission_resumed")
        wayPointV3VM.resumeMission(object : CommonCallbacks.CompletionCallback {
            override fun onSuccess() {
                ToastUtils.showToast("resumeMission Success")
            }

            override fun onFailure(error: IDJIError) {
                ToastUtils.showToast("resumeMission Failed " + getErroMsg(error))
            }
        })
    }

    private fun stopMission() {
        showWaypoints.clear()
        // 实现停止逻辑
        webSocketClient?.sendStatus("mission_stopped")
        if (curMissionExecuteState == WaypointMissionExecuteState.READY) {
            ToastUtils.showToast("Mission not start")
        }
        if (TextUtils.isEmpty(curMissionPath)) {
            ToastUtils.showToast("curMissionPath is Empty")
        }
        wayPointV3VM.stopMission(
            FileUtils.getFileName(curMissionPath, WAYPOINT_FILE_TAG),
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    ToastUtils.showToast("stopMission Success")
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("stopMission Failed " + getErroMsg(error))
                }
            })
    }

    private fun generateNewMission(){
        // 生成新的航点任务
    }

    private fun prepareMissionData() {

        val dir = File(rootDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val cachedirName = DiskUtil.getExternalCacheDirPath(
            ContextUtil.getContext(),
            WAYPOINT_SAMPLE_FILE_CACHE_DIR
        )
        val cachedir = File(cachedirName)
        if (!cachedir.exists()) {
            cachedir.mkdirs()
        }
        val destPath = rootDir + WAYPOINT_SAMPLE_FILE_NAME
        if (!File(destPath).exists()) {
            FileUtils.copyAssetsFile(
                ContextUtil.getContext(),
                WAYPOINT_SAMPLE_FILE_NAME,
                destPath
            )
        }
    }

    private fun initView(savedInstanceState: Bundle?) {
        binding?.spMapSwitch?.adapter = wayPointV3VM.getMapSpinnerAdapter()

        addListener()
        binding?.btnMissionUpload?.setOnClickListener {
            if (showWaypoints.isNotEmpty()) {
                saveKmz(false)
            }
            val waypointFile = File(curMissionPath)
            if (waypointFile.exists()) {
                wayPointV3VM.pushKMZFileToAircraft(curMissionPath)
            } else {
                ToastUtils.showToast("Mission file not found!")
                return@setOnClickListener
            }
            markWaypoints()
        }

        wayPointV3VM.missionUploadState.observe(viewLifecycleOwner) {
            it?.let {
                when {
                    it.error != null -> {
                        binding?.missionUploadStateTv?.text = "Upload State: error:${getErroMsg(it.error)} "
                    }

                    it.tips.isNotEmpty() -> {
                        binding?.missionUploadStateTv?.text = it.tips
                    }

                    else -> {
                        binding?.missionUploadStateTv?.text = "Upload State: progress:${it.updateProgress} "
                    }
                }

            }
        }

        binding?.btnMissionStart?.setOnClickListener {
//            val waypointFile = File(curMissionPath)
//            if (!waypointFile.exists()) {
//                ToastUtils.showToast("Please select file")
//                return@setOnClickListener
//            }
//
//            var curFlightMode = wayPointV3VM.getFlightMode()
//            if (curFlightMode == FlightMode.GO_HOME || curFlightMode ==FlightMode.AUTO_LANDING) {
//                ToastUtils.showToast("Please exit ${curFlightMode.name} mode")
//                return@setOnClickListener
//            }
//
//            wayPointV3VM.startMission(
//                FileUtils.getFileName(curMissionPath, WAYPOINT_FILE_TAG),
//                selectWaylines,
//                object : CommonCallbacks.CompletionCallback {
//                    override fun onSuccess() {
//                        ToastUtils.showToast("startMission Success")
//                    }
//
//                    override fun onFailure(error: IDJIError) {
//                        ToastUtils.showToast("startMission Failed " + getErroMsg(error))
//                    }
//                })
            // ✅ Step 1：准备自定义航点数据（示例）
            val autoWaypoints = arrayListOf(
                WaylineLocationCoordinate3D(22.540000, 113.940000, 50.0),
                WaylineLocationCoordinate3D(22.540000, 113.940195, 50.0),
                WaylineLocationCoordinate3D(22.540180, 113.940195, 50.0),
                WaylineLocationCoordinate3D(22.540180, 113.940000, 50.0),
                WaylineLocationCoordinate3D(22.540000, 113.940000, 50.0)
            )

            // ✅ Step 2：根据这些点生成 Waypoint 信息
            showWaypoints.clear()
            autoWaypoints.forEachIndexed { index, loc ->
                val wp = WaypointInfoModel()
                val waypoint = WaylineWaypoint()
                waypoint.waypointIndex = index
                waypoint.location = WaylineLocationCoordinate2D(loc.latitude, loc.longitude)
                waypoint.height = loc.altitude
                waypoint.ellipsoidHeight = loc.altitude
                waypoint.speed = 10.0
                waypoint.useGlobalTurnParam = true
                wp.waylineWaypoint = waypoint
                showWaypoints.add(wp)
            }

            // 生成KMZ文件、上传并执行任务
            generateAndStartMissionbyDJI()
        }

        binding?.btnMissionPause?.setOnClickListener {
            wayPointV3VM.pauseMission(object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    ToastUtils.showToast("pauseMission Success")
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("pauseMission Failed " + getErroMsg(error))
                }
            })

        }

        observeBtnResume()

        binding?.btnWaylineSelect?.setOnClickListener {
            selectWaylines.clear()
            val waylineids = wayPointV3VM.getAvailableWaylineIDs(curMissionPath)
            showMultiChoiceDialog(waylineids)
        }

        binding?.kmzBtn?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                val intent = Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION")
                startActivityForResult(intent, OPEN_MANAGE_EXTERNAL_STORAGE)
            } else {
                showFileChooser()
            }
        }

        binding?.mapLocate?.setOnClickListener {
            binding?.mapWidget?.setMapCenterLock(MapWidget.MapCenterLock.AIRCRAFT)
        }

        binding?.spMapSwitch?.setSelection(wayPointV3VM.getMapType(context))

        binding?.btnMissionStop?.setOnClickListener {
            if (curMissionExecuteState == WaypointMissionExecuteState.READY) {
                ToastUtils.showToast("Mission not start")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(curMissionPath)) {
                ToastUtils.showToast("curMissionPath is Empty")
                return@setOnClickListener
            }
            wayPointV3VM.stopMission(
                FileUtils.getFileName(curMissionPath, WAYPOINT_FILE_TAG),
                object : CommonCallbacks.CompletionCallback {
                    override fun onSuccess() {
                        ToastUtils.showToast("stopMission Success")
                    }

                    override fun onFailure(error: IDJIError) {
                        ToastUtils.showToast("stopMission Failed " + getErroMsg(error))
                    }
                })
        }
        binding?.btnEditKmz?.setOnClickListener {
            showEditDialog()
        }

        binding?.waypointsClear?.setOnClickListener {
            showWaypoints.clear()
            removeAllPoint()
            updateSaveBtn()
        }

        binding?.kmzSave?.setOnClickListener {
            saveKmz(true)
        }

        binding?.btnBreakpointResume?.setOnClickListener {
            var missionName = FileUtils.getFileName(curMissionPath, WAYPOINT_FILE_TAG);
            WaypointMissionManager.getInstance().queryBreakPointInfoFromAircraft(missionName, object :
                CommonCallbacks.CompletionCallbackWithParam<BreakPointInfo> {
                override fun onSuccess(breakPointInfo: BreakPointInfo?) {
                    breakPointInfo?.let {
                        resumeFromBreakPoint(missionName, it)
                    }
                }
                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("queryBreakPointInfo error $error")
                }
            })
        }

        // 知识图谱场景1按钮点击事件
        binding?.btnKnowledgeGraph?.setOnClickListener {
            navigateToKnowledgeGraph("scene1")
        }

        // 知识图谱场景2按钮点击事件
        binding?.btnKnowledgeGraphScene2?.setOnClickListener {
            navigateToKnowledgeGraph("scene2")
        }

        addMapListener()

        createMapView(savedInstanceState)
        observeAircraftLocation()
    }

    // 生成 kmz 文件并上传任务
    private fun generateAndStartMissionbyDJI() {
        if (showWaypoints.isEmpty()) {
            ToastUtils.showToast("没有航点数据，无法生成任务")
            return
        }
        // 构建与时间对应的文件名
        var fileName = "auto_mission_" + System.currentTimeMillis() + ".kmz"

        // ✅ Step 3：自动生成 KMZ 文件
        val kmzOutPath = rootDir + fileName
        val waylineMission = KMZTestUtil.createWaylineMission()
        val missionConfig = KMZTestUtil.createMissionConfig(missionGlobalModel)
        //
        val template = KMZTestUtil.createTemplate(showWaypoints)

        //
        WPMZManager.getInstance().generateKMZFile(kmzOutPath, waylineMission, missionConfig, template)
        curMissionPath = kmzOutPath
        ToastUtils.showToast("自动生成KMZ成功: $kmzOutPath")

        // ✅ Step 4：执行任务前检查
        var curFlightMode = wayPointV3VM.getFlightMode()
        if (curFlightMode == FlightMode.GO_HOME || curFlightMode == FlightMode.AUTO_LANDING) {
            ToastUtils.showToast("Please exit ${curFlightMode.name} mode")
            return
        }

        // 选择默认的航点
//        selectWaylines.add(0)

        // ✅ Step 5：上传任务并执行
        wayPointV3VM.pushKMZFileToAircraft(curMissionPath)
        wayPointV3VM.startMission(
            FileUtils.getFileName(curMissionPath, WAYPOINT_FILE_TAG),
            selectWaylines,
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    ToastUtils.showToast("自动任务启动成功")
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("自动任务启动失败: " + getErroMsg(error))
                }
            })


    }

    private fun saveKmz(showToast: Boolean) {
        val kmzOutPath = rootDir + "generate_test.kmz"
        val waylineMission: WaylineMission = createWaylineMission()
        val missionConfig: WaylineMissionConfig = KMZTestUtil.createMissionConfig(missionGlobalModel)
        val template: Template = KMZTestUtil.createTemplate(showWaypoints)
        WPMZManager.getInstance()
            .generateKMZFile(kmzOutPath, waylineMission, missionConfig, template)
        curMissionPath = kmzOutPath
        if (showToast) {
            ToastUtils.showToast("Save Kmz Success Path is : $kmzOutPath")
        }

        binding?.waypointAdd?.isChecked = false
    }

    private fun observeAircraftLocation() {
        val location = KeyManager.getInstance()
            .getValue(KeyTools.createKey(FlightControllerKey.KeyAircraftLocation), LocationCoordinate2D(0.0, 0.0))
        val isEnable = SimulatorManager.getInstance().isSimulatorEnabled
        if (!GpsUtils.isLocationValid(location) && !isEnable) {
            ToastUtils.showToast("please open simulator")
        }
    }

    private fun observeBtnResume() {
        binding?.btnMissionQuery?.setOnClickListener {
            var missionName = FileUtils.getFileName(curMissionPath, WAYPOINT_FILE_TAG);
            WaypointMissionManager.getInstance().queryBreakPointInfoFromAircraft(missionName, object :
                CommonCallbacks.CompletionCallbackWithParam<BreakPointInfo> {
                override fun onSuccess(breakPointInfo: BreakPointInfo?) {
                    breakPointInfo?.let {
                        ToastUtils.showLongToast(
                            "BreakPointInfo : waypointID-${breakPointInfo.waypointID} " +
                                    "progress:${breakPointInfo.segmentProgress}  location:${breakPointInfo.location}"
                        )
                    }
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("queryBreakPointInfo error $error")
                }

            })
        }
        binding?.btnMissionResume?.setOnClickListener {
            wayPointV3VM.resumeMission(object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    ToastUtils.showToast("resumeMission Success")
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("resumeMission Failed " + getErroMsg(error))
                }
            })
        }

        binding?.btnMissionResumeWithBp?.setOnClickListener {
            val wp_breakinfo_index = binding?.wpBreakIndex?.text.toString()
            val wp_breakinfo_progress = binding?.wpBreakProgress?.text.toString()
            val resume_type = getResumeType()
            if (!TextUtils.isEmpty(wp_breakinfo_index) && !TextUtils.isEmpty(wp_breakinfo_progress)) {
                val breakPointInfo = BreakPointInfo(0, wp_breakinfo_index.toInt(), wp_breakinfo_progress.toDouble(), null, resume_type)
                wayPointV3VM.resumeMission(breakPointInfo, object :
                    CommonCallbacks.CompletionCallback {
                    override fun onSuccess() {
                        ToastUtils.showToast("resumeMission with BreakInfo Success")
                    }

                    override fun onFailure(error: IDJIError) {
                        ToastUtils.showToast("resumeMission with BreakInfo Failed " + getErroMsg(error))
                    }
                })
            } else {
                ToastUtils.showToast("Please Input breakpoint index or progress")
            }
        }
    }

    //断电续飞
    private fun resumeFromBreakPoint(missionName: String, breakPointInfo: BreakPointInfo) {
        var wp_breakinfo_index = binding?.wpBreakIndex?.text.toString()
        var wp_breakinfo_progress = binding?.wpBreakProgress?.text.toString()
        if (!TextUtils.isEmpty(wp_breakinfo_index) && !TextUtils.isEmpty(wp_breakinfo_progress)) {
            breakPointInfo.segmentProgress = wp_breakinfo_progress.toDouble()
            breakPointInfo.waypointID = wp_breakinfo_index.toInt()
        }
        wayPointV3VM.startMission(missionName, breakPointInfo, object :
            CommonCallbacks.CompletionCallback {
            override fun onSuccess() {
                ToastUtils.showToast("resume success");
            }

            override fun onFailure(error: IDJIError) {
                ToastUtils.showToast("resume error $error")
            }

        })
    }

    private fun addMapListener() {
        binding?.waypointAdd?.setOnCheckedChangeListener { _, isOpen ->
            if (isOpen) {
                binding?.waypointInterest?.isChecked = false
                binding?.mapWidget?.map?.setOnMapClickListener {
                    showWaypointDlg(it, object :
                        CommonCallbacks.CompletionCallbackWithParam<WaypointInfoModel> {
                        override fun onSuccess(waypointInfoModel: WaypointInfoModel) {
                            showWaypoints.add(waypointInfoModel)
                            showWaypoints()
                            updateSaveBtn()
                            ToastUtils.showToast("lat" + it.latitude + " lng" + it.longitude)
                        }

                        override fun onFailure(error: IDJIError) {
                            ToastUtils.showToast("add Failed ")
                        }
                    })
                }
            } else {
                binding?.mapWidget?.map?.removeAllOnMapClickListener()
            }
        }

        binding?.waypointInterest?.setOnCheckedChangeListener { _, isOpen ->
            if (isOpen) {
                binding?.waypointAdd?.isChecked = false
                binding?.mapWidget?.map?.setOnMapClickListener {
                    showWaypointPoiDlg(it)
                    binding?.waypointInterest?.isChecked = false
                }
            } else {
                binding?.mapWidget?.map?.removeAllOnMapClickListener()
            }
        }


    }

    private fun addListener() {
        wayPointV3VM.addMissionStateListener {
            binding?.missionExecuteStateTv?.text = "Mission Execute State : ${it.name}"
            binding?.btnMissionUpload?.isEnabled = it == WaypointMissionExecuteState.READY
            curMissionExecuteState = it
            if (it == WaypointMissionExecuteState.FINISHED) {
                ToastUtils.showToast("Mission Finished")
            }
            LogUtils.i(LogPath.SAMPLE, "State is ${it.name}")
        }
        wayPointV3VM.addWaylineExecutingInfoListener(object : WaylineExecutingInfoListener {
            override fun onWaylineExecutingInfoUpdate(it: WaylineExecutingInfo) {
                binding?.waylineExecuteStateTv?.text = "Wayline Execute Info WaylineID:${it.waylineID} \n" +
                        "WaypointIndex:${it.currentWaypointIndex} \n" +
                        "MissionName : ${it.missionFileName}"
            }

            override fun onWaylineExecutingInterruptReasonUpdate(error: IDJIError?) {
                if (error != null) {
                    val originStr = binding?.missionExecuteStateTv?.text.toString()
                    binding?.waylineExecuteStateTv?.text = "$originStr\n InterruptReason:${error.errorCode()}"
                    LogUtils.e(LogPath.SAMPLE, "interrupt error:${error.errorCode()}")
                }
            }

        })

        wayPointV3VM.addWaypointActionListener(object : WaypointActionListener {
            @Deprecated("Deprecated in Java")
            override fun onExecutionStart(actionId: Int) {
                binding?.waypintActionStateTv?.text = "onExecutionStart: ${actionId} "
            }

            override fun onExecutionStart(actionGroup: Int, actionId: Int) {
                binding?.waypintActionStateTv?.text = "onExecutionStart:${actionGroup}: ${actionId} "
            }

            @Deprecated("Deprecated in Java")
            override fun onExecutionFinish(actionId: Int, error: IDJIError?) {
                binding?.waypintActionStateTv?.text = "onExecutionFinish: ${actionId} "
            }

            override fun onExecutionFinish(actionGroup: Int, actionId: Int, error: IDJIError?) {
                binding?.waypintActionStateTv?.text = "onExecutionFinish:${actionGroup}: ${actionId} "
            }

        })
    }

    fun updateSaveBtn() {
        binding?.kmzSave?.isEnabled = showWaypoints.isNotEmpty()
    }

    private fun showEditDialog() {
        val waypointFile = File(curMissionPath)
        if (!waypointFile.exists()) {
            ToastUtils.showToast("Please upload kmz file")
            return
        }

        val unzipFolder = File(rootDir, unzipChildDir)
        // 解压后的waylines路径
        val templateFile = File(rootDir + unzipChildDir + unzipDir, WPMZParserManager.TEMPLATE_FILE)
        val waylineFile = File(rootDir + unzipChildDir + unzipDir, WPMZParserManager.WAYLINE_FILE)

        mDisposable = Single.fromCallable {
            //在cache 目录创建一个wmpz文件夹，并将template.kml 与 waylines.wpml 拷贝进wpmz ，然后压缩wpmz文件夹
            WPMZParserManager.unZipFolder(ContextUtil.getContext(), curMissionPath, unzipFolder.path, false)
            FileUtils.readFile(waylineFile.path, null)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { wpmlContent: String? ->
                    DialogUtil.showInputDialog(requireActivity(), "", wpmlContent, "", false, object :
                        CommonCallbacks.CompletionCallbackWithParam<String> {
                        override fun onSuccess(newContent: String?) {
                            newContent?.let {
                                updateWPML(it)
                            }
                        }

                        override fun onFailure(error: IDJIError) {
                            LogUtils.e(LogPath.SAMPLE, "show input Dialog Failed ${error.description()} ")
                        }

                    })
                }
            ) { throwable: Throwable ->
                LogUtils.e(LogPath.SAMPLE, "show input Dialog Failed ${throwable.message} ")
            }
    }

    private fun updateWPML(newContent: String) {
        val waylineFile = File(rootDir + unzipChildDir + unzipDir, WPMZParserManager.WAYLINE_FILE)

        Single.fromCallable {
            FileUtils.writeFile(waylineFile.path, newContent, false)
            //将修改后的waylines.wpml重新压缩打包成 kmz
            val zipFiles = mutableListOf<String>()
            val cacheFolder = File(rootDir, unzipChildDir + unzipDir)
            var zipFile = File(rootDir + unzipChildDir + "waypoint.kmz")
            if (waylineFile.exists()) {
                zipFiles.add(cacheFolder.path)
                zipFile.createNewFile()
                WPMZParserManager.zipFiles(ContextUtil.getContext(), zipFiles, zipFile.path)
            }
            //将用户选择的kmz用修改的后的覆盖
            FileUtils.copyFileByChannel(zipFile.path, curMissionPath)
        }.subscribeOn(Schedulers.io()).subscribe()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_DOCUMENT_TREE) {
            grantUriPermission(data)
        }


        if (requestCode == OPEN_MANAGE_EXTERNAL_STORAGE
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            showFileChooser()
        }
    }

    fun checkPath() {
        LogUtils.i(LogPath.SAMPLE, "curPath is $curMissionPath")
        if (!curMissionPath.contains(".kmz") && !curMissionPath.contains(".kml")) {
            ToastUtils.showToast("Please choose KMZ/KML file")
        } else {

            // Choose a directory using the system's file picker.
            showPermisssionDucument()

            if (curMissionPath.contains(".kml")) {
                if (WPMZManager.getInstance().transKMLtoKMZ(curMissionPath, "", getHeightMode())) {
                    curMissionPath = Environment.getExternalStorageDirectory()
                        .toString() + "/DJI/" + requireContext().packageName + "/KMZ/OutPath/" + getName(curMissionPath) + ".kmz"
                    ToastUtils.showToast("Trans kml success " + curMissionPath)
                } else {
                    ToastUtils.showToast("Trans kml failed!")
                }
            } else {
                ToastUtils.showToast("KMZ file path:${curMissionPath}")
                markWaypoints()
            }
        }
    }

    fun getName(path: String): String? {
        val start = path.lastIndexOf("/")
        val end = path.lastIndexOf(".")
        return if (start != -1 && end != -1) {
            path.substring(start + 1, end)
        } else {
            "unknow"
        }
    }

    fun showPermisssionDucument() {
        val canWrite: Boolean =
            DocumentsUtils.checkWritableRootPath(context, curMissionPath)
        if (!canWrite && Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            val storageManager =
                requireActivity().getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val volume: StorageVolume? =
                storageManager.getStorageVolume(File(curMissionPath))
            if (volume != null) {
                val intent = volume.createOpenDocumentTreeIntent()
                startActivityForResult(intent, OPEN_DOCUMENT_TREE)
                return
            }
        }
    }

    var lancher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result?.apply {
            val uri = data?.data
            curMissionPath = DocumentUtil.getPath(requireContext(), uri)
            checkPath()
        }
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        lancher.launch(intent)
    }

    @SuppressLint("WrongConstant")
    private fun grantUriPermission(data: Intent?) {
        val uri = data!!.data
        requireActivity().grantUriPermission(
            getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        val takeFlags = data.flags and (Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_READ_URI_PERMISSION)
        requireActivity().contentResolver.takePersistableUriPermission(uri!!, takeFlags)
    }

    fun getPath(context: Context?, uri: Uri?): String {
        if (DocumentsContract.isDocumentUri(context, uri) && isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).toTypedArray()
            if (split.size != validLenth) {
                return ""
            }
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else {
                return getExtSdCardPaths(requireContext()).get(0)!! + "/" + split[1]
            }
        }
        return ""
    }

    private fun getExtSdCardPaths(context: Context): ArrayList<String?> {
        var sExtSdCardPaths = ArrayList<String?>()
        for (file in context.getExternalFilesDirs("external")) {
            if (file != null && file != context.getExternalFilesDir("external")) {
                val index = file.absolutePath.lastIndexOf("/Android/data")
                if (index >= 0) {
                    var path: String? = file.absolutePath.substring(0, index)
                    try {
                        path = File(path).canonicalPath
                    } catch (e: IOException) {
                        LogUtils.e(LogPath.SAMPLE, e.message)
                    }
                    sExtSdCardPaths.add(path)
                }
            }
        }
        if (sExtSdCardPaths.isEmpty()) {
            sExtSdCardPaths.add("/storage/sdcard1")
        }
        return sExtSdCardPaths
    }

    fun isExternalStorageDocument(uri: Uri?): Boolean {
        return "com.android.externalstorage.documents" == uri?.authority
    }

    private fun initData() {
        wayPointV3VM.listenFlightControlState()

        wayPointV3VM.flightControlState.observe(viewLifecycleOwner) {
            it?.let {
                binding?.waylineAircraftHeight?.text = String.format("Aircraft Height: %.2f", it.height)
                binding?.waylineAircraftDistance?.text = String.format("Aircraft Distance: %.2f", it.distance)
                binding?.waylineAircraftSpeed?.text = String.format("Aircraft Speed: %.2f", it.speed)
            }
        }
    }

    private fun createMapView(savedInstanceState: Bundle?) {
        binding?.mapWidget?.initMapLibreMap(requireContext()) {
            it.setMapType(DJIMap.MapType.NORMAL)
        }
        binding?.mapWidget?.onCreate(savedInstanceState) //需要再init后调用否则Amap无法显示
    }

    override fun onPause() {
        super.onPause()
        binding?.mapWidget?.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding?.mapWidget?.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.mapWidget?.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        wayPointV3VM.cancelListenFlightControlState()
        wayPointV3VM.removeAllMissionStateListener()
        wayPointV3VM.clearAllWaylineExecutingInfoListener()
        wayPointV3VM.clearAllWaypointActionListener()

        mDisposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
        webSocketClient?.disconnect()
    }

    fun getErroMsg(error: IDJIError): String {
        if (!TextUtils.isEmpty(error.description())) {
            return error.description();
        }
        return error.errorCode()
    }


    fun showMultiChoiceDialog(waylineids: List<Int>) {
        var items: ArrayList<String> = ArrayList()
        waylineids
            .filter {
                it >= 0
            }
            .map {
                items.add(it.toString())
            }

        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle("Select Wayline")
        builder.setPositiveButton("OK", null)
        builder.setMultiChoiceItems(
            items.toTypedArray(),
            null,
            object : OnMultiChoiceClickListener {
                override fun onClick(p0: DialogInterface?, index: Int, isSelect: Boolean) {
                    if (isSelect) {
                        selectWaylines.add(index)
                    } else {
                        selectWaylines.remove(index)
                    }
                }
            }).create().show()

    }

    fun markWaypoints() {
        // version参数实际未用到
        var waypoints: ArrayList<WaylineExecuteWaypoint> = ArrayList<WaylineExecuteWaypoint>()
        val parseInfo  = WPMZManager.getInstance().getKMZInfo(curMissionPath).waylineWaylinesParseInfo
        var waylines = parseInfo.waylines
        waylines.forEach() {
            waypoints.addAll(it.waypoints)
            markLine(it.waypoints)
            checkSpecialActionType(it)
        }
        waypoints.forEach() {
            markWaypoint(DJILatLng(it.location.latitude, it.location.longitude), it.waypointIndex)
        }
    }

    private fun checkSpecialActionType(it: Wayline) : Boolean {
        it.actionGroups.forEach(){
            it.actions.forEach(){
                if (it.actionType == WaylineActionType.MEGAPHONE || it.actionType == WaylineActionType.SEARCHLIGHT){
                    LogUtils.i(LogPath.WAYPOINT , "kmz contains metaphone or light ${it.actionType}")
                    return true
                }

            }
        }
        return false
    }

    fun markWaypoint(latlong: DJILatLng, waypointIndex: Int) : DJIMarker?{
        var markOptions = DJIMarkerOptions()
        markOptions.position(latlong)
        markOptions.icon(getMarkerRes(waypointIndex.toString(), 0f))
        markOptions.title(waypointIndex.toString())
        markOptions.isInfoWindowEnable = true
        return binding?.mapWidget?.map?.addMarker(markOptions)
    }

    fun markPoiWaypoint(latlong: DJILatLng): DJIMarker? {
        var markOptions = DJIMarkerOptions()
        markOptions.position(latlong)
        markOptions.icon(getMarkerRes("POI", 0f))
        markOptions.title("POI")
        markOptions.isInfoWindowEnable = true
        return binding?.mapWidget?.map?.addMarker(markOptions)
    }

    fun markLine(waypoints: List<WaylineExecuteWaypoint>) {

        var djiwaypoints = waypoints.filter {
            true
        }.map {
            DJILatLng(it.location.latitude, it.location.longitude)
        }
        var lineOptions = DJIPolylineOptions()
        lineOptions.width(5f)
        lineOptions.color(Color.GREEN)
        lineOptions.addAll(djiwaypoints)
        binding?.mapWidget?.map?.addPolyline(lineOptions)
    }


    /**
     * Convert view to bitmap
     * Notice: recycle the bitmap after use
     */
    fun getMarkerBitmap(
        index: String,
        rotation: Float,
    ): Bitmap? {
        // create View for marker
        @SuppressLint("InflateParams") val markerView: View =
            LayoutInflater.from(activity)
                .inflate(R.layout.waypoint_marker_style_layout, null)
        val markerBg = markerView.findViewById<ImageView>(R.id.image_content)
        val markerTv = markerView.findViewById<TextView>(R.id.image_text)
        markerTv.text = index
        markerTv.setTextColor(AndUtil.getResColor(R.color.blue))
        markerTv.textSize =
            AndUtil.getDimension(R.dimen.mission_waypoint_index_text_large_size)

        markerBg.setImageResource(R.mipmap.mission_edit_waypoint_normal)

        markerBg.rotation = rotation
        // convert view to bitmap
        markerView.destroyDrawingCache()
        markerView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
        markerView.isDrawingCacheEnabled = true
        return markerView.getDrawingCache(true)
    }

    private fun getMarkerRes(
        index: String,
        rotation: Float,
    ): DJIBitmapDescriptor? {
        return DJIBitmapDescriptorFactory.fromBitmap(
            getMarkerBitmap(index, rotation)
        )
    }

    fun showWaypoints() {
        var loction2D = showWaypoints.last().waylineWaypoint.location
        val waypoint = DJILatLng(loction2D.latitude, loction2D.longitude)
        var pointMarker = markWaypoint(waypoint, getCurWaypointIndex())
        pointMarkers.add(pointMarker)
    }

    fun getCurWaypointIndex(): Int {
        if (showWaypoints.size <= 0) {
            return 0
        }
        return showWaypoints.size
    }

    private fun showWaypointDlg(djiLatLng: DJILatLng, callbacks: CommonCallbacks.CompletionCallbackWithParam<WaypointInfoModel>) {
        val builder = AlertDialog.Builder(requireActivity())
        val dialog = builder.create()
        val dialogView = View.inflate(requireActivity(), R.layout.dialog_add_waypoint, null)
        dialog.setView(dialogView)

        val etHeight = dialogView.findViewById<View>(R.id.et_height) as EditText
        val etSpd = dialogView.findViewById<View>(R.id.et_speed) as EditText
        val viewActionType = dialogView.findViewById<View>(R.id.action_type) as DescSpinnerCell
        val actionValueView = dialogView.findViewById<EditText>(R.id.action_value)
        var showMultiAction = dialogView.findViewById<Button>(R.id.show_multiAction)

        val viewActionType1 = dialogView.findViewById<View>(R.id.action_type1) as DescSpinnerCell
        val actionValueView1 = dialogView.findViewById<EditText>(R.id.action_value1)
        viewActionType.addOnItemSelectedListener(object : DescSpinnerCell.OnItemSelectedListener{
            override fun onItemSelected(position: Int) {
               val curAction =  getCurActionType(position)
                if ( curAction== WaypointActionType.CAMERA_ZOOM || curAction ==  WaypointActionType.STAY){
                    actionValueView.visibility = View.VISIBLE
                } else {
                    actionValueView.visibility = View.GONE
                }
            }

        })

        viewActionType1.addOnItemSelectedListener(object : DescSpinnerCell.OnItemSelectedListener{
            override fun onItemSelected(position: Int) {
                val curAction =  getCurActionType(position)
                if ( curAction== WaypointActionType.CAMERA_ZOOM || curAction ==  WaypointActionType.STAY){
                    actionValueView1.visibility = View.VISIBLE
                } else {
                    actionValueView1.visibility = View.GONE
                }
            }

        })

        showMultiAction.setOnClickListener{
            if (viewActionType1.visibility == View.VISIBLE) {
                viewActionType1.visibility = View.GONE
            } else {
                viewActionType1.visibility = View.VISIBLE
            }
        }


        val btnLogin = dialogView.findViewById<View>(R.id.btn_add) as Button
        val btnCancel = dialogView.findViewById<View>(R.id.btn_cancel) as Button
        val cbUseGlobalSpeed = dialogView.findViewById<View>(R.id.cb_use_global_speed) as CheckBox

        val etGlobalSpeed =  dialogView.findViewById<View>(R.id.global_speed) as EditText
        val missionFinishType = dialogView.findViewById<View>(R.id.mission_finish_type) as DescSpinnerCell
        val missionLostAction = dialogView.findViewById<View>(R.id.lost_action) as DescSpinnerCell

        //机头朝向
        val aircraftHeadingModeView = dialogView.findViewById<View>(R.id.aircraft_heading_mode) as DescSpinnerCell
        val headingAngleView = dialogView.findViewById<EditText>(R.id.aircraft_heading_angle)
        val poiLongitudeView = dialogView.findViewById<EditText>(R.id.poi_longitude)
        val poiLatitudeView = dialogView.findViewById<EditText>(R.id.poi_latitude)
        val poiHeightView = dialogView.findViewById<EditText>(R.id.poi_height)

        //航点间云台俯仰
        val gimbalHeadingView = dialogView.findViewById<View>(R.id.gimbal_heading_mode) as DescSpinnerCell
        val gimbalHeadingGimbalView = dialogView.findViewById<EditText>(R.id.gimbal_heading_angle)



        etGlobalSpeed.setText(missionGlobalModel.globalSpeed.toString())
        missionFinishType.select(MissionGlobalModel.transFinishActionToIndex(missionGlobalModel.finishAction))//
        missionLostAction.select(missionGlobalModel.lostAction.value())
        poiLongitudeView.setText(interestPoint.longitude.toString())
        poiLatitudeView.setText(interestPoint.latitude.toString())
        poiHeightView.setText(interestPoint.altitude.toString())

        btnLogin.setOnClickListener {
            var waypointInfoModel = WaypointInfoModel()
            val waypoint = WaylineWaypoint()
            waypoint.waypointIndex = getCurWaypointIndex()
            val location = WaylineLocationCoordinate2D(djiLatLng.latitude, djiLatLng.longitude)
            waypoint.location = location
            waypoint.height = etHeight.text.toString().toDouble()
            // 根据坐标类型，如果为egm96 需要加上高程差
            waypoint.ellipsoidHeight = etHeight.text.toString().toDouble()
            if (cbUseGlobalSpeed.isChecked){
                waypoint.speed = etGlobalSpeed.text.toString().toDouble()
            } else {
                waypoint.speed = etSpd.text.toString().toDouble()
            }
            waypoint.useGlobalTurnParam = true
            waypoint.gimbalPitchAngle = gimbalHeadingGimbalView.text.toString().toDouble()

            //
            val aircraftHeadingMode = WaylineWaypointYawMode.find(aircraftHeadingModeView.getSelectPosition())
            val yawParam = WaylineWaypointYawParam()

            yawParam.enableYawAngle = (aircraftHeadingMode == WaylineWaypointYawMode.SMOOTH_TRANSITION)
            yawParam.yawAngle = headingAngleView.text.toString().toDouble()
            yawParam.yawMode = aircraftHeadingMode
            yawParam.yawPathMode = WaylineWaypointYawPathMode.FOLLOW_BAD_ARC
            yawParam.poiLocation = WaylineLocationCoordinate3D(
                poiLatitudeView.text.toString().toDouble(),
                poiLongitudeView.text.toString().toDouble(),
                poiHeightView.text.toString().toDouble()
            )
            waypoint.yawParam = yawParam
            waypoint.useGlobalYawParam = false
            waypoint.isWaylineWaypointYawParamSet = true
            //

            //
            val gimbalYawMode = WaylineWaypointGimbalHeadingMode.find(gimbalHeadingView.getSelectPosition())
            val gimbalYawParam = WaylineWaypointGimbalHeadingParam()
            gimbalYawParam.headingMode = gimbalYawMode
            gimbalYawParam.pitchAngle =  gimbalHeadingGimbalView.text.toString().toDouble()
            waypoint.gimbalHeadingParam = gimbalYawParam
            waypoint.isWaylineWaypointGimbalHeadingParamSet = true

            //

            waypointInfoModel.waylineWaypoint = waypoint
            val actionInfos: MutableList<WaylineActionInfo> = ArrayList()
            val curSelectAction = getCurActionType(viewActionType.getSelectPosition())
            val curSelectAction1 = getCurActionType(viewActionType1.getSelectPosition())
            actionInfos.add(KMZTestUtil.createActionInfo(curSelectAction, actionValueView.text.toString().toInt()))
            if (viewActionType1.visibility == View.VISIBLE){
                actionInfos.add(KMZTestUtil.createActionInfo(curSelectAction1, actionValueView1.text.toString().toInt()))
            }

            waypointInfoModel.waylineWaypoint = waypoint
            waypointInfoModel.actionInfos = actionInfos

            missionGlobalModel.globalSpeed = etGlobalSpeed.text.toString().toDouble()
            missionGlobalModel.finishAction = WaylineFinishedAction.find(missionFinishType.getSelectPosition())
            missionGlobalModel.lostAction = WaylineExitOnRCLostAction.find(missionLostAction.getSelectPosition())
            callbacks.onSuccess(waypointInfoModel)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }


    private fun showWaypointPoiDlg(djiLatLng: DJILatLng) {
        val builder = AlertDialog.Builder(requireActivity())
        val dialog = builder.create()
        val dialogView = View.inflate(requireActivity(), R.layout.dialog_add_poipoint, null)
        dialog.setView(dialogView)

        val poiHeight = dialogView.findViewById<View>(R.id.poi_height) as EditText
        val btnAdd = dialogView.findViewById<View>(R.id.btn_add) as Button
        val btnCancel = dialogView.findViewById<View>(R.id.btn_cancel) as Button

        btnAdd.setOnClickListener {
            interestPoint.longitude = djiLatLng.longitude
            interestPoint.latitude = djiLatLng.latitude
            interestPoint.altitude = poiHeight.text.toString().toDouble()
            pointMarkers.add(markPoiWaypoint(djiLatLng))
            dialog.dismiss()

        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun getHeightMode(): HeightMode {
        return when (binding?.heightmode?.getSelectPosition()) {
            0 -> HeightMode.WGS84
            1 -> HeightMode.EGM96
            2 -> HeightMode.RELATIVE
            else -> {
                HeightMode.WGS84
            }
        }
    }

    private fun getResumeType(): RecoverActionType {
        return when (binding?.resumeType?.getSelectPosition()) {
            0 -> RecoverActionType.GoBackToRecordPoint
            1 -> RecoverActionType.GoBackToNextPoint
            2 -> RecoverActionType.GoBackToNextNextPoint
            else -> {
                RecoverActionType.GoBackToRecordPoint
            }
        }
    }

    private fun getCurActionType(position: Int): WaypointActionType? {
        return when (position) {
            0 -> WaypointActionType.START_TAKE_PHOTO
            1 -> WaypointActionType.START_RECORD
            2 -> WaypointActionType.STOP_RECORD
            3 -> WaypointActionType.GIMBAL_PITCH
            4 -> WaypointActionType.CAMERA_ZOOM
            5 -> WaypointActionType.STAY
            6 -> WaypointActionType.ROTATE_AIRCRAFT
            else -> {
                WaypointActionType.START_TAKE_PHOTO
            }
        }
    }

    private fun removeAllPoint() {
        pointMarkers.forEach {
            it?.let {
                it.remove()
            }
        }
    }

    /**
     * 导航到知识图谱页面
     * @param sceneType 场景类型: "scene1" - 交通路口场景, "scene2" - 无人机监控场景
     */
    private fun navigateToKnowledgeGraph(sceneType: String = "scene1") {
        val fragment = KnowledgeGraphFragment.newInstance(sceneType)

        // 使用 FragmentManager 进行页面跳转
        // 获取当前fragment的容器ID
        val containerId = (view?.parent as? ViewGroup)?.id ?: android.R.id.content

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()

        val sceneName = if (sceneType == "scene2") "无人机监控场景" else "交通路口场景"
        ToastUtils.showToast("正在加载$sceneName")
    }

//    fun startReturnHome() {
//        // 创建“返航”ActionKey
//        val goHomeKey = KeyTools.createKey(FlightControllerKey.KeyStartGoHome)
//
//        // 执行返航动作
//        try {
//            // 使用泛型 performAction<>
//            KeyManager.getInstance().performAction<Boolean>(goHomeKey)
//            ToastUtils.showToast("✅ Start go home success")
//        } catch (e: Exception) {
//            ToastUtils.showToast("❌ Start go home failed: ${e.message}")
//        }
//    }
}

