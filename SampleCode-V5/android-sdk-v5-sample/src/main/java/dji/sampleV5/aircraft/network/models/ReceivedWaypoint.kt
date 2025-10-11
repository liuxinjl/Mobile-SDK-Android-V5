package dji.sampleV5.aircraft.network.models

import com.google.gson.annotations.SerializedName

/**
 * 从WebSocket请求中接收到的单个航点数据模型。
 */
data class ReceivedWaypoint(
    @SerializedName("lat") val lat: Double,      // 纬度
    @SerializedName("lng") val lng: Double,      // 经度
    @SerializedName("alt") val alt: Double,      // 相对高度 (米)
    @SerializedName("speed") val speed: Double = 5.0, // 飞行速度 (米/秒)，默认 5.0
    @SerializedName("id") val missionId: Long = 0L // 任务唯一ID，用于去重，建议服务器提供时间戳
)