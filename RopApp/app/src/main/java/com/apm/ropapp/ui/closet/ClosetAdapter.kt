package com.apm.ropapp.ui.closet

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.apm.ropapp.AddClothes
import com.apm.ropapp.CreateOutfit
import com.apm.ropapp.R
import com.apm.ropapp.utils.CLOTHES
import com.bumptech.glide.Glide


class ClosetAdapter(
    private val dataList: MutableList<HashMap<String, Any>>,
    private val imageList: MutableList<Uri>,
    private val startForResult: ActivityResultLauncher<Intent>,
    private val type: String
) : RecyclerView.Adapter<ClosetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
        val imageButton: ImageButton = view.findViewById(R.id.imageButton)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_closet, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (imageList[position] != Uri.EMPTY)
            Glide.with(viewHolder.itemView.context).load(imageList[position])
                .into(viewHolder.imageButton)

        if (type == CLOTHES) {
            val nameString = dataList.elementAt(position)["category"].toString()
            viewHolder.textView.text = nameString.substring(1, nameString.length - 1)

            viewHolder.imageButton.setOnClickListener {
                Log.d("Closet", "Has pulsado la prenda $position")
                val intent = Intent(viewHolder.itemView.context, AddClothes::class.java)
                intent.putExtra("clothesValues", dataList.elementAt(position))
                if (imageList[position] != Uri.EMPTY)
                    intent.putExtra("image", imageList[position].toString())
                startForResult.launch(intent)
            }
        } else {
            viewHolder.textView.text = dataList.elementAt(position)["outfitName"].toString()

            viewHolder.imageButton.setOnClickListener {
                Log.d("Closet", "Has pulsado el conjunto $position")
                val intent = Intent(viewHolder.itemView.context, CreateOutfit::class.java)
                intent.putExtra("outfitValues", dataList.elementAt(position))
                if (imageList[position] != Uri.EMPTY)
                    intent.putExtra("image", imageList[position].toString())
                startForResult.launch(intent)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataList.size

}
