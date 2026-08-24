package com.rogue.scorequest.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

private const val MAX_DIMENSION_PX = 1024
private const val JPEG_QUALITY = 85

object ImageStorage {

    fun createCaptureUri(context: Context): Uri {
        val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(cameraDir, "${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun persistImage(context: Context, sourceUri: Uri): String {
        val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
        val destFile = File(imagesDir, "${UUID.randomUUID()}.jpg")

        val bytes = context.contentResolver.openInputStream(sourceUri).use { input -> input!!.readBytes() }
        val orientation = ByteArrayInputStream(bytes).use { stream ->
            ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }
        val bitmap = rotateIfNeeded(BitmapFactory.decodeByteArray(bytes, 0, bytes.size), orientation)
        val resized = resizeIfNeeded(bitmap)
        FileOutputStream(destFile).use { output ->
            resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        }

        return destFile.absolutePath
    }

    fun deleteImage(path: String?) {
        if (path.isNullOrBlank()) return
        File(path).takeIf { it.exists() }?.delete()
    }

    private fun rotateIfNeeded(bitmap: Bitmap, exifOrientation: Int): Bitmap {
        val degrees = when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun resizeIfNeeded(bitmap: Bitmap): Bitmap {
        val largestDimension = maxOf(bitmap.width, bitmap.height)
        if (largestDimension <= MAX_DIMENSION_PX) return bitmap

        val scale = MAX_DIMENSION_PX.toFloat() / largestDimension
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
