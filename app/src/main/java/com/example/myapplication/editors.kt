
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Button
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@Composable
fun addnotescreen() {

}

@Composable
fun addrecordnote(){

}

fun saveToInternalStorage(context: Context, inputStream: InputStream, fileName: String): String {
    val file = File(context.filesDir, fileName)
    try {
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return ""
    }
    return file.absolutePath
}
fun saveVideoToInternalStorage(context: Context, uri: Uri, fileName: String): String {
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        val filePath = saveToInternalStorage(context, inputStream, fileName)
        // 保存视频第一帧
        saveFirstFrameToInternalStorage(context, uri, "frame_${System.currentTimeMillis()}.jpg")
        return filePath
    }
    return ""
}
fun saveFirstFrameToInternalStorage(context: Context, videoUri: Uri, frameFileName: String) {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, videoUri)
        val bitmap = retriever.getFrameAtTime(0)
        val frameFile = File(context.filesDir, frameFileName)
        FileOutputStream(frameFile).use { out ->
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        retriever.release()
    }
}
@Composable
fun AddVideoNote()
    {
    val context = LocalContext.current
    // 初始化视频录制启动器
    val recordVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // 生成文件名
                val fileName = "recorded_${System.currentTimeMillis()}.mp4"
                val filePath = saveVideoToInternalStorage(context, uri, fileName)
            }
        }
    }

    // 初始化视频选择启动器
    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "selected_${System.currentTimeMillis()}.mp4"
            val filePath = saveVideoToInternalStorage(context, uri, fileName)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { recordVideoLauncher.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE)) }) {
            Text("录制视频")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { pickVideoLauncher.launch("video/*") }) {
            Text("选择视频文件")
        }
    }
}
