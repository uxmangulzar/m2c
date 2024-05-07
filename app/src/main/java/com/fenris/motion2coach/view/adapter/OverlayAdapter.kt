package com.fenris.motion2coach.view.adapter

import android.app.Activity
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.ItemOverlayBinding
import com.fenris.motion2coach.model.OverlayModel


class OverlayAdapter(private val mList: List<OverlayModel>, private val context: Activity) : RecyclerView.Adapter<OverlayAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val binding = ItemOverlayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(mList.get(position),position,context)



    }



    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(val binding: ItemOverlayBinding) :RecyclerView.ViewHolder(binding.root) {
        fun bind(
            overlayModel: OverlayModel,
            pos: Int,
            con: Activity
        ) {
            with(itemView) {

                binding.tvTitle.text = overlayModel.title
                binding.tvUnit.text = overlayModel.unit
                binding.tvRange.text = overlayModel.range
                binding.tvValue.text = overlayModel.value
                if(overlayModel.unit == ""){
                    binding.tvUnit.visibility = View.GONE
                }



                    if(pos == 0 ||pos == 3||pos == 6){
                    binding.linCard.background = con.resources.getDrawable(R.drawable.container_overlay_blue)
                    binding.ivIndicator.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_blue))

                }
                if(pos == 1 ||pos == 4||pos == 7){
                    binding.linCard.background = con.resources.getDrawable(R.drawable.contain_overlay_yellow)
                    binding.ivIndicator.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_fen_yellow))
                }
                if(pos == 2 ||pos == 5||pos == 8){
                    binding.linCard.background = con.resources.getDrawable(R.drawable.container_overlay_green)
                    binding.ivIndicator.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_fen_green))
                }

                if(overlayModel.isLocked){
                    binding.ivLock.visibility = View.VISIBLE
                    binding.linCard.background = con.resources.getDrawable(R.drawable.container_overlay_blur)

                }else{
                    binding.ivLock.visibility = View.GONE

                }

                itemView.setOnClickListener(View.OnClickListener {


                })

            }
        }

    }

}