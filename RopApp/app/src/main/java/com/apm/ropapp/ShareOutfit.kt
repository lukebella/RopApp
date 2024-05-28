package com.apm.ropapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
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
        val clothesUriMap = intent.getSerializableExtra("clothesUriBundle", HashMap<String, String>().javaClass)

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
            shareContent("Check out my outfit: $outfitName")
            Log.d("ShareOutfit", "Share Outfit")
        }
    }

    private fun loadAndCombineOutfitImages(clothesUriMap: HashMap<String, String>) {
        val bitmaps = mutableListOf<Bitmap>()
        val accessories = mutableListOf<Bitmap>()
        val order = listOf("Prenda Exterior", "Parte de Arriba", "Parte de Abajo", "Calzado")

        // Sort the URIs according to the defined order, separate accessories
        val sortedUris = order.mapNotNull { key ->
            clothesUriMap[key]?.let { uriString -> key to Uri.parse(uriString) }
        }
        val accessoryUris = clothesUriMap.entries
            .filter { it.key.contains("accesorio", ignoreCase = true) }
            .map { it.key to Uri.parse(it.value) }

        var loadCount = 0
        val totalImagesToLoad = sortedUris.size + accessoryUris.size

        sortedUris.forEach { (key, uri) ->
            Log.d("ShareOutfit", "Loading URI: $uri for $key")
            Glide.with(this)
                .asBitmap()
                .load(uri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        bitmaps.add(resource)
                        loadCount++
                        if (loadCount == totalImagesToLoad) {
                            displayCombinedImages(bitmaps, accessories)
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        Log.e("ShareOutfit", "Failed to load image: $uri for $key")
                        loadCount++
                        if (loadCount == totalImagesToLoad) {
                            displayCombinedImages(bitmaps, accessories)
                        }
                    }
                })
        }

        accessoryUris.forEach { (key, uri) ->
            Log.d("ShareOutfit", "Loading URI: $uri for $key")
            Glide.with(this)
                .asBitmap()
                .load(uri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        accessories.add(resource)
                        loadCount++
                        if (loadCount == totalImagesToLoad) {
                            displayCombinedImages(bitmaps, accessories)
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        Log.e("ShareOutfit", "Failed to load image: $uri for $key")
                        loadCount++
                        if (loadCount == totalImagesToLoad) {
                            displayCombinedImages(bitmaps, accessories)
                        }
                    }
                })
        }
    }

    private fun displayCombinedImages(clothesBitmaps: List<Bitmap>, accessoriesBitmaps: List<Bitmap>) {
        val combinedClothesBitmap = combineImagesVertically(clothesBitmaps)
        binding.imageCombined.setImageBitmap(combinedClothesBitmap)

        if (accessoriesBitmaps.isNotEmpty()) {
            if (accessoriesBitmaps.size > 0) {
                binding.imageAccesories1.setImageBitmap(accessoriesBitmaps[0])
                binding.imageAccesories1.visibility = ImageView.VISIBLE
            } else {
                binding.imageAccesories1.visibility = ImageView.GONE
            }

            if (accessoriesBitmaps.size > 1) {
                binding.imageAccesories2.setImageBitmap(accessoriesBitmaps[1])
                binding.imageAccesories2.visibility = ImageView.VISIBLE
            } else {
                binding.imageAccesories2.visibility = ImageView.GONE
            }
        } else {
            binding.imageAccesories1.visibility = ImageView.GONE
            binding.imageAccesories2.visibility = ImageView.GONE
        }
    }

    private fun combineImagesVertically(bitmaps: List<Bitmap>): Bitmap {
        val width = bitmaps.maxOfOrNull { it.width } ?: 0
        val height = bitmaps.sumOf { it.height }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        var currentHeight = 0
        bitmaps.forEach { bitmap ->
            canvas.drawBitmap(bitmap, 0f, currentHeight.toFloat(), paint)
            currentHeight += bitmap.height
        }

        return result
    }

    private fun shareCombinedImage(bitmap: Bitmap) {
        val uri = getImageUri(this, bitmap)
        ShareCompat.IntentBuilder(this)
            .setType("image/png")
            .setChooserTitle("Share via")
            .setStream(uri)
            .startChooser()
    }

    private fun getImageUri(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Outfit", null)
        return Uri.parse(path)
    }

    private fun shareContent(content: String) {
        ShareCompat.IntentBuilder(this)
            .setType("text/plain")
            .setChooserTitle("Share via")
            .setText(content)
            .startChooser()
    }
}
