package com.fenris.motion2coach.view.adapter

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fenris.motion2coach.databinding.ItemCitiesBinding
import com.fenris.motion2coach.network.responses.CitiesResponse


class CitiesAdapter(private var mList: List<CitiesResponse>, private val context: Activity) : RecyclerView.Adapter<CitiesAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val binding = ItemCitiesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position],position,context)
    }



    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }
    fun filterList(filterlist: ArrayList<CitiesResponse>) {
        // below line is to add our filtered
        // list in our course array list.
        mList = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }

    // Holds the views for adding it to image and text
    class ViewHolder(val binding: ItemCitiesBinding) :RecyclerView.ViewHolder(binding.root) {
        fun bind(
            citiesResponse: CitiesResponse,
            pos: Int,
            con: Activity
        ) {
            with(itemView) {

                binding.tvName.text = citiesResponse.name




                itemView.setOnClickListener(View.OnClickListener {

                    con.setResult(RESULT_OK, Intent().putExtra("model", citiesResponse))
                    con.finish();
                })

            }
        }

    }

}