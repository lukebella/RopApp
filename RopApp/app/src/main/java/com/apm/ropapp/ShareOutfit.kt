package com.apm.ropapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.apm.ropapp.databinding.ShareoutfitBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.ByteArrayOutputStream

class ShareOutfit : AppCompatActivity() {
    private lateinit var binding: ShareoutfitBinding
    private var combinedBitmap: Bitmap? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ShareoutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val outfitName = intent.getStringExtra("outfitName")
        if (!outfitName.isNullOrEmpty()) {
            binding.textView.text = outfitName
        }

        // Retrieve the data correctly
        val clothesUriMap = intent.getSerializableExtra("clothesUriBundle", HashMap<String, String>().javaClass) as? HashMap<String, String>

        if (clothesUriMap != null) {
            loadAndCombineOutfitImages(clothesUriMap)
        } else {
            Log.e("ShareOutfit", "Failed to retrieve clothes URI map")
        }

        binding.cancelButton.setOnClickListener {
            Log.d("ShareOutfit", "Back to Main Activity")
            intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.editButton.setOnClickListener {
            Log.d("ShareOutfit", "Edit Outfit")
            finish()
        }
        binding.shareButton.setOnClickListener {
            // Share the outfit
            shareCombinedImage()
            Log.d("ShareOutfit", "Share Outfit")
        }
    }

    private fun loadAndCombineOutfitImages(clothesUriMap: HashMap<String, String>) {
        val bitmaps = mutableListOf<Bitmap>()
        val uris = clothesUriMap.values.map { Uri.parse(it) }

        uris.forEach { uri ->
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    bitmaps.add(bitmap)
                }
            } catch (e: Exception) {
                Log.e("ShareOutfit", "Failed to load image: $uri", e)
            }
        }

        if (bitmaps.isNotEmpty()) {
            combinedBitmap = combineImagesVertically(bitmaps)
            binding.imageCombined.setImageBitmap(combinedBitmap)
        }
    }


    private fun combineImagesVertically(bitmaps: List<Bitmap>): Bitmap {
        val width = bitmaps.maxOf { it.width }
        val height = bitmaps.sumOf { it.height }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        var currentHeight = 0

        val paint = Paint()
        bitmaps.forEach { bitmap ->
            canvas.drawBitmap(bitmap, 0f, currentHeight.toFloat(), paint)
            currentHeight += bitmap.height
        }

        return result
    }

    private fun shareCombinedImage() {
        combinedBitmap?.let { bitmap ->
            val uri = getImageUri(this, bitmap)
            val shareIntent = ShareCompat.IntentBuilder(this)
                .setType("image/png")
                .setStream(uri)
                .intent
                .setAction(Intent.ACTION_SEND)
                .setDataAndType(uri, "image/png")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(shareIntent, "Share outfit via"))
        }
    }

    private fun getImageUri(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Outfit", null)
        return Uri.parse(path)
    }
}
