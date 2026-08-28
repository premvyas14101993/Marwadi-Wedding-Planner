package com.example.ui.screens.expenses

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavedAttachmentResult(
    val filePath: String,
    val fileName: String,
    val mimeType: String
)

object ExpenseFileUtils {

    fun copyUriToLocalStorage(context: Context, sourceUri: Uri): SavedAttachmentResult? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(sourceUri) ?: inferMimeType(sourceUri.toString())

            var originalName = "receipt_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}"
            contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val name = cursor.getString(nameIndex)
                    if (!name.isNullOrBlank()) {
                        originalName = name
                    }
                }
            }

            // Determine appropriate extension
            val extension = when {
                mimeType.contains("pdf", ignoreCase = true) || originalName.endsWith(".pdf", ignoreCase = true) -> ".pdf"
                mimeType.contains("png", ignoreCase = true) || originalName.endsWith(".png", ignoreCase = true) -> ".png"
                mimeType.contains("webp", ignoreCase = true) || originalName.endsWith(".webp", ignoreCase = true) -> ".webp"
                else -> ".jpg"
            }

            val storageDir = File(context.filesDir, "expense_proofs")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val safePrefix = originalName.substringBeforeLast(".").take(30).replace("[^a-zA-Z0-9_]".toRegex(), "_")
            val targetFile = File(storageDir, "${safePrefix}_${System.currentTimeMillis()}$extension")

            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            SavedAttachmentResult(
                filePath = targetFile.absolutePath,
                fileName = originalName,
                mimeType = mimeType
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun inferMimeType(path: String): String {
        return when {
            path.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            path.endsWith(".png", ignoreCase = true) -> "image/png"
            path.endsWith(".webp", ignoreCase = true) -> "image/webp"
            else -> "image/jpeg"
        }
    }

    fun openFileWithExternalApp(context: Context, filePath: String, mimeType: String?) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val resolvedMimeType = mimeType?.ifBlank { null } ?: inferMimeType(filePath)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, resolvedMimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Open with")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun shareFile(context: Context, filePath: String, expenseName: String, amount: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = inferMimeType(filePath)
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Proof: $expenseName ($amount)")
                putExtra(Intent.EXTRA_TEXT, "Expense Proof for $expenseName: $amount")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Expense Proof")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share file", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
