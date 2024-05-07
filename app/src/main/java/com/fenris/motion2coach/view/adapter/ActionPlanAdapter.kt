package com.fenris.motion2coach.view.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fenris.motion2coach.R
import com.fenris.motion2coach.model.ActionToWorkModel

class ActionPlanAdapter(private val mList: List<ActionToWorkModel>, private val context: Activity) : RecyclerView.Adapter<ActionPlanAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_action_plan, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


//        val ItemsViewModel = mList[position]
//
//        // sets the image to the imageview from our itemHolder class
//        holder.imageView.setImageResource(ItemsViewModel.image)
//
//        // sets the text to the textview from our itemHolder class
//        holder.textView.text = ItemsViewModel.text

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
//        return mList.size
        return 3
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
//        val textView: TextView = itemView.findViewById(R.id.textView)
    }
}