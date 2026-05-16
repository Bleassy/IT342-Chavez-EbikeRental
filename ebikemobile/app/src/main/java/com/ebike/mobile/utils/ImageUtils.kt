package com.ebike.mobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import id.zelory.compressor.Compressor
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

object ImageUtils {
    
    /**
     * Compress image from URI and convert to Base64
     * 
     * @param context Android context
     * @param imageUri URI of the image to compress
     * @param maxWidth Maximum width in pixels
     * @param maxHeight Maximum height in pixels
     * @param quality Compression quality (0-100)
     * @return Base64 encoded compressed image or null if error
     */
    suspend fun compressImageToBase64(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 80
    ): String? {
        return try {
            Timber.d("🖼️ Starting image compression: $imageUri")
            
            // Get file from URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return null.also {
                    Timber.e("Failed to open input stream from URI")
                }
            
            // Decode to bitmap
            val bitmap = BitmapFactory.decodeStream(inputStream)
                ?: return null.also {
                    Timber.e("Failed to decode bitmap from stream")
                }
            
            inputStream.close()
            
            // Scale bitmap if needed
            val scaledBitmap = scaleBitmap(bitmap, maxWidth, maxHeight)
            
            // Compress to JPEG
            val compressed = compressBitmap(scaledBitmap, quality)
            
            // Convert to Base64
            val base64 = bitmapToBase64(compressed)
            
            Timber.d("✅ Image compressed successfully: ${base64.length} bytes")
            
            base64
        } catch (e: Exception) {
            Timber.e(e, "Failed to compress image to Base64")
            null
        }
    }
    
    /**
     * Scale bitmap to fit within maxWidth and maxHeight while maintaining aspect ratio
     */
    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        
        val (newWidth, newHeight) = if (width > height) {
            // Landscape
            Pair(maxWidth, (maxWidth / aspectRatio).toInt())
        } else {
            // Portrait
            Pair((maxHeight * aspectRatio).toInt(), maxHeight)
        }
        
        Timber.d("Scaling bitmap from ${width}x${height} to ${newWidth}x${newHeight}")
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Compress bitmap to JPEG with specified quality
     */
    private fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val compressedData = outputStream.toByteArray()
        
        Timber.d("Compressed to ${compressedData.size} bytes at quality $quality")
        
        return BitmapFactory.decodeByteArray(compressedData, 0, compressedData.size)
    }
    
    /**
     * Convert bitmap to Base64 encoded string
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val compressedData = outputStream.toByteArray()
        return Base64.encodeToString(compressedData, Base64.NO_WRAP)
    }
    
    /**
     * Get file size in human readable format
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes <= 0 -> "0 B"
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
    
    /**
     * Check if image size is acceptable (< 5MB)
     */
    fun isImageSizeAcceptable(base64String: String, maxSizeMB: Int = 5): Boolean {
        val bytes = base64String.length / 4 * 3 // Base64 is ~33% larger
        val maxBytes = maxSizeMB * 1024 * 1024
        return bytes <= maxBytes
    }
}
