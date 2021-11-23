package github.xtvj.cleanx.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import java.io.File

object FileUtils {
    fun getFileUri(
        context: Context,
        @ShareContentType shareContentType: String?,
        file: File?
    ): Uri? {
        if (file == null || !file.exists()) {
            log("getFileUri file is null or not exists.")
            return null
        }
        var uri: Uri? = null
        uri = when (shareContentType) {
            ShareContentType.IMAGE -> getImageContentUri(context, file)
            ShareContentType.VIDEO -> getVideoContentUri(context, file)
            ShareContentType.AUDIO -> getAudioContentUri(context, file)
            ShareContentType.FILE -> getFileContentUri(context, file)
            else -> {
                getFileContentUri(context, file)
            }
        }
        if (uri == null) {
            uri = forceGetFileUri(file)
        }
        return uri
    }

    private fun getFileContentUri(context: Context, file: File): Uri? {
        val volumeName = "external"
        val filePath = file.absolutePath
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        var uri: Uri? = null
        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri(volumeName), projection,
            MediaStore.Images.Media.DATA + "=? ", arrayOf(filePath), null
        )

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") val id =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                uri = MediaStore.Files.getContentUri(volumeName, id.toLong())
            }
            cursor.close()
        }
        return uri
    }

    private fun getImageContentUri(context: Context, imageFile: File): Uri? {
        val filePath = imageFile.absolutePath
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ",
            arrayOf(filePath),
            null
        )
        var uri: Uri? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") val id =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                val baseUri = Uri.parse("content://media/external/images/media")
                uri = Uri.withAppendedPath(baseUri, "" + id)
            }
            cursor.close()
        }
        if (uri == null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, filePath)
            uri =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
        return uri
    }

    private fun getVideoContentUri(context: Context, videoFile: File): Uri? {
        var uri: Uri? = null
        val filePath = videoFile.absolutePath
        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Video.Media._ID),
            MediaStore.Video.Media.DATA + "=? ",
            arrayOf(filePath),
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") val id =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                val baseUri = Uri.parse("content://media/external/video/media")
                uri = Uri.withAppendedPath(baseUri, "" + id)
            }
            cursor.close()
        }
        if (uri == null) {
            val values = ContentValues()
            values.put(MediaStore.Video.Media.DATA, filePath)
            uri =
                context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        }
        return uri
    }

    private fun getAudioContentUri(context: Context, audioFile: File): Uri? {
        var uri: Uri? = null
        val filePath = audioFile.absolutePath
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID),
            MediaStore.Audio.Media.DATA + "=? ",
            arrayOf(filePath),
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") val id =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                val baseUri = Uri.parse("content://media/external/audio/media")
                uri = Uri.withAppendedPath(baseUri, "" + id)
            }
            cursor.close()
        }
        if (uri == null) {
            val values = ContentValues()
            values.put(MediaStore.Audio.Media.DATA, filePath)
            uri =
                context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        }
        return uri
    }

    private fun forceGetFileUri(shareFile: File): Uri {
        try {
            @SuppressLint("PrivateApi", "DiscouragedPrivateApi") val rMethod =
                StrictMode::class.java.getDeclaredMethod("disableDeathOnFileUriExposure")
            rMethod.invoke(null)
        } catch (e: Exception) {
            log(Log.getStackTraceString(e))
        }
        return Uri.parse("file://" + shareFile.absolutePath)
    }
}