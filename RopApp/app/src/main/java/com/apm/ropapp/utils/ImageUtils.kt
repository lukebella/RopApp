package com.apm.ropapp.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ImageUtils {
    private val imageUriCache = mutableMapOf<String, Uri>()

    suspend fun getImageUri(
        fileName: String, folder: String, photosStorage: StorageReference, context: Context
    ): Uri {
        // Check if the URI is in the cache
        if (imageUriCache.containsKey(fileName)) return imageUriCache[fileName]!!

        val dir = File("${context.getExternalFilesDir(null)}/$folder")
        if (!dir.exists()) dir.mkdirs()
        val imageFile = File(dir, fileName)

        if (!imageFile.exists()) {
            withContext(Dispatchers.IO) {
                imageFile.createNewFile()
                photosStorage.child(fileName).getFile(imageFile)
            }
        }
        val uri = FileProvider.getUriForFile(
            context, "com.apm.ropapp.FileProvider", imageFile
        )
        imageUriCache[fileName] = uri
        return uri
    }
}
