package dji.sampleV5.aircraft.utils;

import dji.sampleV5.aircraft.network.models.ReceivedWaypoint;
import dji.sdk.wpmz.value.mission.* ;
import dji.v5.utils.common.LogUtils;
import dji.v5.utils.common.LogPath;

import com.dji.wpmzsdk.common.data.Template;
import com.dji.wpmzsdk.common.utils.kml.model.WaypointActionType;

import java.util.ArrayList;
import java.util.List;

import dji.sampleV5.aircraft.models.MissionGlobalModel;
import dji.sampleV5.aircraft.utils.wpml.WaypointInfoModel;
import dji.sdk.wpmz.value.mission.ActionAircraftHoverParam;
import dji.sdk.wpmz.value.mission.ActionGimbalRotateParam;
import dji.sdk.wpmz.value.mission.ActionStartRecordParam;
import dji.sdk.wpmz.value.mission.ActionStopRecordParam;
import dji.sdk.wpmz.value.mission.ActionTakePhotoParam;
import dji.sdk.wpmz.value.mission.ActionZoomParam;
import dji.sdk.wpmz.value.mission.CameraLensType;
import dji.sdk.wpmz.value.mission.WaylineActionGroup;
import dji.sdk.wpmz.value.mission.WaylineActionInfo;
import dji.sdk.wpmz.value.mission.WaylineActionNodeList;
import dji.sdk.wpmz.value.mission.WaylineActionTreeNode;
import dji.sdk.wpmz.value.mission.WaylineActionTrigger;
import dji.sdk.wpmz.value.mission.WaylineActionTriggerType;
import dji.sdk.wpmz.value.mission.WaylineActionType;
import dji.sdk.wpmz.value.mission.WaylineActionsRelationType;
import dji.sdk.wpmz.value.mission.WaylineAltitudeMode;
import dji.sdk.wpmz.value.mission.WaylineCoordinateMode;
import dji.sdk.wpmz.value.mission.WaylineCoordinateParam;
import dji.sdk.wpmz.value.mission.WaylineDroneInfo;
import dji.sdk.wpmz.value.mission.WaylineExitOnRCLostAction;
import dji.sdk.wpmz.value.mission.WaylineExitOnRCLostBehavior;
import dji.sdk.wpmz.value.mission.WaylineFinishedAction;
import dji.sdk.wpmz.value.mission.WaylineFlyToWaylineMode;
import dji.sdk.wpmz.value.mission.WaylineGimbalActuatorRotateMode;
import dji.sdk.wpmz.value.mission.WaylineLocationCoordinate3D;
import dji.sdk.wpmz.value.mission.WaylineMission;
import dji.sdk.wpmz.value.mission.WaylineMissionConfig;
import dji.sdk.wpmz.value.mission.WaylinePayloadInfo;
import dji.sdk.wpmz.value.mission.WaylinePositioningType;
import dji.sdk.wpmz.value.mission.WaylineTemplateWaypointInfo;
import dji.sdk.wpmz.value.mission.WaylineWaypoint;
import dji.sdk.wpmz.value.mission.WaylineWaypointPitchMode;
import dji.sdk.wpmz.value.mission.WaylineWaypointTurnMode;
import dji.sdk.wpmz.value.mission.WaylineWaypointYawMode;
import dji.sdk.wpmz.value.mission.WaylineWaypointYawParam;

/**
 * @author feel.feng
 * @time 2023/07/05 3:30 下午
 * @description:
 */
public class KMZTestUtil {

    public static final WaylineFlyToWaylineMode DEF_WAYLINE_MODE = WaylineFlyToWaylineMode.SAFELY;
    public static final WaylineFinishedAction DEF_FINISH_ACTION = WaylineFinishedAction.GO_HOME;
    public static final Double DEF_TAKE_OFF_HEIGHT = 20d;
    public static final WaylineExitOnRCLostBehavior DEF_EXIT_RC_LOST_BEHAV = WaylineExitOnRCLostBehavior.EXCUTE_RC_LOST_ACTION;
    public static final WaylineExitOnRCLostAction DEF_RC_LOST_ACTION =  WaylineExitOnRCLostAction.GO_BACK;
    public static final Double DEF_GLOBAL_TRANSITION_SPEED = 10d;
    public static final Double DEF_AUTO_FLIGHT_SPEED = 5d;
    public static final Double DEF_GLOBAL_FLIGHT_HEIGHT = 100d;
    public static final WaylineCoordinateMode DEF_COR_MODE = WaylineCoordinateMode.WGS84;
    public static final WaylinePositioningType DEF_POSITION_TYPE = WaylinePositioningType.GPS;
    public static final WaylineAltitudeMode DEF_ALTITUDE_MODE = WaylineAltitudeMode.RELATIVE_TO_START_POINT;
    public static final Double DEF_PITCH_ANGLE = -30d;
    public static final Double DEF_HOVER_TIME = 10d;
    public static final Double DEF_FOCAL_LENGTH = 5d;

    private KMZTestUtil(){}

    public static WaylineMission createWaylineMission(){
        WaylineMission waylineMission = new WaylineMission();
        waylineMission.setCreateTime(((Long)System.currentTimeMillis()).doubleValue());
        waylineMission.setUpdateTime(((Long)System.currentTimeMillis()).doubleValue());
        return waylineMission;
    }

    public static WaylineMissionConfig createMissionConfig(MissionGlobalModel missionGlobalModel){
        WaylineMissionConfig config = new WaylineMissionConfig();
        config.setFlyToWaylineMode(DEF_WAYLINE_MODE);
        config.setFinishAction(missionGlobalModel.getFinishAction());
        WaylineDroneInfo droneInfo = new WaylineDroneInfo();
        config.setDroneInfo(droneInfo);
        config.setSecurityTakeOffHeight(DEF_TAKE_OFF_HEIGHT);
        config.setIsSecurityTakeOffHeightSet(true);
        config.setExitOnRCLostBehavior(DEF_EXIT_RC_LOST_BEHAV);
        config.setExitOnRCLostType(missionGlobalModel.getLostAction());
        config.setGlobalTransitionalSpeed(DEF_GLOBAL_TRANSITION_SPEED);
        List<WaylinePayloadInfo> payloadInfos = new ArrayList<>();
        config.setPayloadInfo(payloadInfos);
        return config;
    }

    public static Template createTemplate(List<WaypointInfoModel> waypointInfoModels){
        Template template = new Template();
        WaylineTemplateWaypointInfo waypointInfo = createTemplateWaypointInfo(waypointInfoModels);
        template.setWaypointInfo(waypointInfo);
        WaylineCoordinateParam coordinateParam = transCoordinateParamFrom();
        template.setCoordinateParam(coordinateParam);
        template.setUseGlobalTransitionalSpeed(true);
        template.setAutoFlightSpeed(DEF_AUTO_FLIGHT_SPEED);
        template.setPayloadParam(new ArrayList<>());
        return template;
    }

    public static  WaylineCoordinateParam transCoordinateParamFrom() {
        WaylineCoordinateParam coordinateParam = new WaylineCoordinateParam();
        coordinateParam.setCoordinateMode(DEF_COR_MODE);
        coordinateParam.setPositioningType(DEF_POSITION_TYPE);
        coordinateParam.setIsWaylinePositioningTypeSet(true);
        coordinateParam.setAltitudeMode(DEF_ALTITUDE_MODE);
        return coordinateParam;
    }


    public static  WaylineTemplateWaypointInfo createTemplateWaypointInfo(List<WaypointInfoModel> waypointInfoModels) {
        WaylineLocationCoordinate3D poiLocation = new WaylineLocationCoordinate3D();
        List<WaylineWaypoint> waypoints = new ArrayList<>();
        for (WaypointInfoModel infoModel:waypointInfoModels){
            waypoints.add(infoModel.getWaylineWaypoint());
            poiLocation =  infoModel.getWaylineWaypoint().getYawParam().getPoiLocation();
        }

        WaylineTemplateWaypointInfo waypointInfo = new WaylineTemplateWaypointInfo();
        waypointInfo.setWaypoints(waypoints);
        waypointInfo.setActionGroups(transformActionsFrom(waypointInfoModels));
        waypointInfo.setGlobalFlightHeight(DEF_GLOBAL_FLIGHT_HEIGHT);
        waypointInfo.setIsGlobalFlightHeightSet(true);
        waypointInfo.setGlobalTurnMode(WaylineWaypointTurnMode.TO_POINT_AND_STOP_WITH_DISCONTINUITY_CURVATURE);
        waypointInfo.setUseStraightLine(true);
        waypointInfo.setIsTemplateGlobalTurnModeSet(true);
        WaylineWaypointYawParam yawParam = new WaylineWaypointYawParam();
        yawParam.setYawMode(WaylineWaypointYawMode.FOLLOW_WAYLINE);
        yawParam.setPoiLocation(poiLocation);
        waypointInfo.setGlobalYawParam(yawParam);
        waypointInfo.setIsTemplateGlobalYawParamSet(true);
        waypointInfo.setPitchMode(WaylineWaypointPitchMode.USE_POINT_SETTING);

        return waypointInfo;
    }

    /**
     * 【新增】将接收到的 ReceivedWaypoint 列表转换为 WaylineMission 对象。
     * 这个方法从 WebSocketManager中调用。
     * * @param receivedPoints 包含经纬高坐标点的列表 (来自 WebSocket)
     * @return 完整的 WaylineMission 对象
     */
    public static WaylineMission createMissionFromReceivedPoints(List<ReceivedWaypoint> receivedPoints) {
        // 1. 创建 Wayline (航线) 和 Waypoints (航点) 列表
        Wayline wayline = new Wayline();
        List<WaylineWaypoint> waypoints = new ArrayList<>();

        int index = 0;
        for (ReceivedWaypoint point : receivedPoints) {
            WaylineLocationCoordinate3D location = new WaylineLocationCoordinate3D(point.getLat(), point.getLng(), point.getAlt());

            // --- V5 SDK WaylineWaypoint 构造参数 ---

            // 1. 设置航向参数 (必须先创建)
            WaylineWaypointYawParam yawParam = new WaylineWaypointYawParam();
            yawParam.setYawMode(WaylineWaypointYawMode.CONTROL_BY_MISSION);
            // setYawParam 接受一个 WaylineWaypointYawParam 对象，而不是直接在 Waypoint 上设置 yaw

            // 2. 设置云台参数 (必须先创建)
            // 注意：WaylineWaypointYawParam 和 WaylineWaypointGimbalHeadingParam 是 WaylineWaypoint 的必填字段
            WaylineWaypointGimbalHeadingParam gimbalParam = new WaylineWaypointGimbalHeadingParam();
            gimbalParam.setHeadingMode(WaylineWaypointGimbalHeadingMode.NONE);

            // 3. 使用构造函数创建 WaylineWaypoint 对象
            // WaylineWaypoint的构造函数接受所有关键参数！
            WaylineWaypoint waypoint = new WaylineWaypoint(
                    index,                      // waypointID (int)
                    location,                   // location (WaylineLocationCoordinate3D)
                    point.getSpeed().doubleValue(), // autoFlightSpeed (double)
                    WaylineWaypointTurnMode.TO_POINT_AND_STOP_WITH_DISCONTINUITY_CURVATURE, // turnMode (WaylineWaypointTurnMode)
                    WaylineWaypointPitchMode.USE_POINT_SETTING, // pitchMode (WaylineWaypointPitchMode)
                    yawParam,                   // yawParam (WaylineWaypointYawParam)
                    gimbalParam,                // gimbalHeadingParam (WaylineWaypointGimbalHeadingParam)
                    new ArrayList<>(),          // actionInfos (List<WaylineActionInfo>)
                    null,                       // actionGroupIds (List<Integer>)
                    null                        // waylinePointActions (List<WaylineActionNodeList>)
            );

            waypoints.add(waypoint);
            LogUtils.i(LogPath.SAMPLE, "Waypoint " + index + " created: Lat=" + point.getLat() + ", Alt=" + point.getAlt());
            index++;
        }

        // 3. 封装到 Wayline 中
        wayline.setWaylineID(0); // 第一条航线 ID 设为 0
        wayline.setWaypoints(waypoints);

        // 4. 封装到 WaylineMission 中
        WaylineMission mission = new WaylineMission();
        List<Wayline> waylines = new ArrayList<>();
        waylines.add(wayline);
        mission.setWaylines(waylines);

        // 设置创建和更新时间
        mission.setCreateTime(((Long)System.currentTimeMillis()).doubleValue());
        mission.setUpdateTime(((Long)System.currentTimeMillis()).doubleValue());

        return mission;
    }


    public static  List<WaylineActionGroup> transformActionsFrom(List<WaypointInfoModel> waypointInfoModels) {
        List<WaylineActionGroup> actionGroups = new ArrayList<>();

        List<WaylineWaypoint> waypoints = new ArrayList<>();
        for (WaypointInfoModel infoModel:waypointInfoModels){
            waypoints.add(infoModel.getWaylineWaypoint());
        }

        for (int i = 0; i < waypoints.size(); ++i) {
            List<WaylineActionInfo> actionInfos = waypointInfoModels.get(i).getActionInfos();
            if (actionInfos.size() > 0) {
                WaylineActionGroup actionGroup = new WaylineActionGroup();
                WaylineActionTrigger trigger = new WaylineActionTrigger();
                trigger.setTriggerType(WaylineActionTriggerType.REACH_POINT);
                actionGroup.setTrigger(trigger);
                actionGroup.setGroupId(actionGroups.size());
                actionGroup.setStartIndex(i);
                actionGroup.setEndIndex(i);
                actionGroups.add(actionGroup);
                actionGroup.setActions(actionInfos);

                List<WaylineActionNodeList> nodeLists = new ArrayList<>();

                WaylineActionNodeList root = new WaylineActionNodeList();
                List<WaylineActionTreeNode> treeNodes = new ArrayList<>();
                WaylineActionTreeNode rootNode = new WaylineActionTreeNode();
                rootNode.setNodeType(WaylineActionsRelationType.SEQUENCE);
                rootNode.setChildrenNum(actionInfos.size());
                treeNodes.add(rootNode);
                root.setNodes(treeNodes);
                nodeLists.add(root);

                WaylineActionNodeList children = new WaylineActionNodeList();
                List<WaylineActionTreeNode> childrenNodeList = new ArrayList<>();
                for (int j = 0; j <  actionInfos.size(); ++j) {
                    WaylineActionTreeNode child = new WaylineActionTreeNode();
                    child.setNodeType(WaylineActionsRelationType.LEAF);
                    child.setActionIndex(j);
                    childrenNodeList.add(child);
                }
                children.setNodes(childrenNodeList);
                nodeLists.add(children);

                actionGroup.setNodeLists(nodeLists);
            }
        }

        return actionGroups;
    }



    public static WaylineActionInfo createActionInfo(WaypointActionType actionType ,  Integer actionValue) {

        switch (actionType) {
            case START_TAKE_PHOTO:
                return transTakePhoto();
            case START_RECORD:
                return transStartRecord();
            case STOP_RECORD:
                return transStopRecord();
            case GIMBAL_PITCH:
                return transGimbalPitch();
            case STAY:
                return transAircraftStay(actionValue);
            case CAMERA_ZOOM:
                return transCameraZoom(actionValue);
            default:
                return null;

        }
    }

    private static WaylineActionInfo transCameraZoom(Integer focalLength) {
        WaylineActionInfo info = new WaylineActionInfo();
        info.setActionType(WaylineActionType.ZOOM);
        ActionZoomParam param = new ActionZoomParam();
        param.setPayloadPositionIndex(0);
        param.setFocalFactor(Double.valueOf(focalLength));
        param.setIsUseFocalFactor(true);
        info.setZoomParam(param);
        return info;
    }

    private static WaylineActionInfo transAircraftStay(Integer hoverTime) {
        WaylineActionInfo info  = new WaylineActionInfo();
        info.setActionType(WaylineActionType.HOVER);
        ActionAircraftHoverParam param = new ActionAircraftHoverParam();
        param.setHoverTime(Double.valueOf(hoverTime));
        info.setAircraftHoverParam(param);
        return info;
    }

    private static WaylineActionInfo transGimbalPitch() {
        WaylineActionInfo info = new WaylineActionInfo();
        info.setActionType(WaylineActionType.GIMBAL_ROTATE);

        ActionGimbalRotateParam param = new ActionGimbalRotateParam();
        param.setEnablePitch(true);
        param.setPitch(DEF_PITCH_ANGLE);
        param.setRotateMode(WaylineGimbalActuatorRotateMode.ABSOLUTE_ANGLE);
        param.setPayloadPositionIndex(0);
        info.setGimbalRotateParam(param);
        return info;
    }

    public static WaylineActionInfo transTakePhoto() {
        WaylineActionInfo info = new WaylineActionInfo();
        info.setActionType(WaylineActionType.TAKE_PHOTO);
        List<CameraLensType> photoTypes = new ArrayList<>();

        ActionTakePhotoParam param = new ActionTakePhotoParam(
                0, true, photoTypes, "djitest");
        param.setPayloadPositionIndex(0);
        info.setTakePhotoParam(param);
        return info;
    }

    private static WaylineActionInfo transStartRecord() {
        WaylineActionInfo info = new WaylineActionInfo();
        info.setActionType(WaylineActionType.START_RECORD);
        List<CameraLensType> photoTypes = new ArrayList<>();

        ActionStartRecordParam param = new ActionStartRecordParam(0,
                true, photoTypes, "djitest");
        info.setStartRecordParam(param);
        return info;
    }

    private static WaylineActionInfo transStopRecord() {
        WaylineActionInfo info = new WaylineActionInfo();
        info.setActionType(WaylineActionType.STOP_RECORD);
        ActionStopRecordParam param = new ActionStopRecordParam();
        param.setPayloadPositionIndex(0);
        info.setStopRecordParam(param);
        return info;
    }

}
