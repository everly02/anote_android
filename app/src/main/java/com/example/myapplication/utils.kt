
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStream

fun captureFrameAndSaveAsImage(context: Context, videoUri: Uri): Uri? {
    val retriever = MediaMetadataRetriever()
    try {
        // 使用MediaMetadataRetriever获取视频帧
        retriever.setDataSource(context, videoUri)
        val bitmap = retriever.getFrameAtTime(1000000) // 获取第一秒的帧

        // 保存图片
        bitmap?.let {
            val imageUri = saveImageToGallery(context, it)
            return imageUri
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        retriever.release()
    }
    return null
}

fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri? {
    val filename = "IMG_${System.currentTimeMillis()}.jpg"
    var fos: OutputStream? = null
    var imageUri: Uri? = null
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
    }

    // 插入图片并返回URI
    context.contentResolver?.also { resolver ->
        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = imageUri?.let { resolver.openOutputStream(it) }
    }

    fos?.use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }

    return imageUri
}
