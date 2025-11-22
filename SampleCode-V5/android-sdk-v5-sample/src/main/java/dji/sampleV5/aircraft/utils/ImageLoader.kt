package dji.sampleV5.aircraft.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * 图片加载工具类
 * 支持从网络URL或本地路径加载图片
 */
object ImageLoader {

    /**
     * 加载图片到 ImageView
     * @param imageView 目标 ImageView
     * @param imageUrl 图片URL（支持 http/https、file://、assets:// 路径）
     * @param placeholder 占位符颜色
     */
    fun loadImage(imageView: ImageView, imageUrl: String?, placeholder: Int = 0xFFE0E0E0.toInt()) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setBackgroundColor(placeholder)
            return
        }

        // 显示占位符
        imageView.setBackgroundColor(placeholder)

        // 异步加载图片
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    when {
                        imageUrl.startsWith("http://") || imageUrl.startsWith("https://") -> {
                            // 从网络加载
                            loadFromNetwork(imageUrl)
                        }
                        imageUrl.startsWith("assets://") -> {
                            // 从 assets 目录加载
                            val assetPath = imageUrl.removePrefix("assets://")
                            loadFromAssets(imageView.context, assetPath)
                        }
                        imageUrl.startsWith("file://") -> {
                            // 从本地文件加载
                            val path = imageUrl.removePrefix("file://")
                            BitmapFactory.decodeFile(path)
                        }
                        else -> {
                            // 尝试作为本地路径加载
                            BitmapFactory.decodeFile(imageUrl)
                        }
                    }
                }

                bitmap?.let {
                    imageView.setImageBitmap(it)
                    imageView.setBackgroundColor(0x00000000) // 透明背景
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 加载失败，保持占位符
            }
        }
    }

    /**
     * 从网络加载图片
     */
    private fun loadFromNetwork(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            val inputStream = connection.getInputStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从 assets 目录加载图片
     * @param context Context 上下文
     * @param assetPath assets 中的文件路径，如 "images/traffic_scene.jpg"
     */
    private fun loadFromAssets(context: android.content.Context, assetPath: String): Bitmap? {
        return try {
            val inputStream = context.assets.open(assetPath)
            BitmapFactory.decodeStream(inputStream).also {
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从 Base64 字符串加载图片
     */
    fun loadFromBase64(imageView: ImageView, base64String: String) {
        try {
            val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

