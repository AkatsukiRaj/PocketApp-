package com.pocket.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.pocket.app.data.model.ItemType
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object FileUtils {

    fun getVaultDir(context: Context): File {
        val dir = File(context.filesDir, "vault")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun saveUriToVault(context: Context, uri: Uri): Pair<File, String> {
        val contentResolver = context.contentResolver
        var originalName = getFileName(context, uri) ?: "file_${System.currentTimeMillis()}"
        var extension = getExtension(originalName)

        val mimeFromResolver = try { contentResolver.getType(uri) } catch (e: Exception) { null }

        if (extension.isEmpty() && mimeFromResolver != null) {
            val extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeFromResolver)
            if (!extFromMime.isNullOrEmpty()) {
                extension = extFromMime
                originalName = "$originalName.$extension"
            } else if (mimeFromResolver.startsWith("image/", ignoreCase = true)) {
                extension = "jpg"
                originalName = "$originalName.jpg"
            } else if (mimeFromResolver.equals("application/pdf", ignoreCase = true)) {
                extension = "pdf"
                originalName = "$originalName.pdf"
            }
        }

        val safeName = "${UUID.randomUUID()}_$originalName"
        val destinationFile = File(getVaultDir(context), safeName)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return Pair(destinationFile, originalName)
    }

    fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        return cursor.getString(index)
                    }
                }
            }
        }
        return uri.path?.let { File(it).name }
    }

    fun getExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot != -1 && lastDot < fileName.length - 1) {
            fileName.substring(lastDot + 1).lowercase()
        } else {
            ""
        }
    }

    fun determineItemType(extension: String, mimeType: String? = null): ItemType {
        if (mimeType?.startsWith("image/", ignoreCase = true) == true) {
            return ItemType.PHOTO
        }
        return when (extension.lowercase()) {
            "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "heif", "svg" -> ItemType.PHOTO
            else -> ItemType.DOCUMENT
        }
    }

    fun getMimeType(context: Context, file: File): String {
        val extension = getExtension(file.name)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    }

    fun shareFile(context: Context, filePath: String, title: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File not found / கோப்பு கிடைக்கவில்லை", Toast.LENGTH_SHORT).show()
                return
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val mimeType = getMimeType(context, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share with / பகிர்க").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Cannot share file / பகிர முடியவில்லை: ${e.localizedMessage ?: ""}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun shareText(context: Context, text: String, title: String = "") {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                if (title.isNotBlank()) putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share with / பகிர்க").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Cannot share text / பகிர முடியவில்லை",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File not found / கோப்பு கிடைக்கவில்லை", Toast.LENGTH_SHORT).show()
                return
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val extension = getExtension(file.name)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: getMimeType(context, file)

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(viewIntent, "Open with / திறக்க").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Cannot open file / கோப்பைத் திறக்க முடியவில்லை: ${e.localizedMessage ?: ""}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
        val formatted = String.format("%.1f", sizeInBytes / Math.pow(1024.0, digitGroups.toDouble()))
        return "$formatted ${units[digitGroups]}"
    }
}
