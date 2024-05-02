package com.apm.ropapp.ui.closet

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apm.ropapp.R
import com.bumptech.glide.Glide


class CustomAdapter(
    private val stringList: MutableList<String>,
    private val imageList: MutableList<Uri>
) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
        val imageButton: ImageButton = view.findViewById(R.id.imageButton)

        init {
            // Define click listener for the ViewHolder's View
            imageButton.setOnClickListener {
                Log.d("Closet", "Has pulsado el armario ${this.layoutPosition}")
            }
        }
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
        viewHolder.textView.text = stringList.elementAt(position)
        if (imageList[position] != Uri.EMPTY)
            Glide.with(viewHolder.itemView.context).load(imageList[position])
                .into(viewHolder.imageButton)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = stringList.size

}
