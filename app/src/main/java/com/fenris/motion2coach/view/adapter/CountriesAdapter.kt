package com.fenris.motion2coach.view.adapter

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fenris.motion2coach.databinding.ItemCountryBinding
import com.fenris.motion2coach.network.responses.CountriesResponse
import com.fenris.motion2coach.util.Helper.loadUrl


class CountriesAdapter(private var mList: List<CountriesResponse>, private val context: Activity) : RecyclerView.Adapter<CountriesAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
    fun filterList(filterlist: ArrayList<CountriesResponse>) {
        // below line is to add our filtered
        // list in our course array list.
        mList = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }

    // Holds the views for adding it to image and text
    class ViewHolder(val binding: ItemCountryBinding) :RecyclerView.ViewHolder(binding.root) {
        fun bind(
            country: CountriesResponse,
            pos: Int,
            con: Activity
        ) {
            with(itemView) {

                binding.tvName.setText(country.name)

                country.image?.let { binding.image.loadUrl(it) }



                itemView.setOnClickListener(View.OnClickListener {

                    con.setResult(RESULT_OK, Intent().putExtra("model", country))
                    con.finish();
                })

            }
        }

    }

}