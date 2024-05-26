package com.apm.ropapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SelectAdapter(
    private val dataList: List<HashMap<String, Any>>,
    private val imageList: MutableMap<String, Uri>,
    private val activity: Activity
) : RecyclerView.Adapter<SelectAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageButton: ImageButton = view.findViewById(R.id.imageButton)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_select, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val photoUri = imageList[dataList.elementAt(position)["photo"].toString()]
        if (photoUri != null)
            Glide.with(viewHolder.itemView.context).load(photoUri).into(viewHolder.imageButton)

        viewHolder.imageButton.setOnClickListener {
            Log.d("SelectItem", "Has pulsado la prenda $position")

            val intent = Intent()
            intent.putExtra("clothesValues", dataList.elementAt(position))
            if (photoUri != null) intent.putExtra("image", photoUri.toString())
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataList.size

}