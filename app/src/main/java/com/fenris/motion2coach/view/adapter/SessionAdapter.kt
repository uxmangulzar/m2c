package com.fenris.motion2coach.view.adapter

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.CustomDialogDeleteBinding
import com.fenris.motion2coach.databinding.ItemSessionBinding
import com.fenris.motion2coach.model.Video
import com.fenris.motion2coach.model.SessionsMainModel
import com.fenris.motion2coach.util.Coroutines
import com.fenris.motion2coach.util.Helper.convertLongToDate
import com.fenris.motion2coach.util.Helper.toTimeAgo
import com.fenris.motion2coach.view.activity.history.SessionsActivity
import com.fenris.motion2coach.viewmodel.SessionViewModel


class SessionAdapter(
    private val mList: ArrayList<SessionsMainModel>,
    private val context: SessionsActivity,
    private val viewModel: SessionViewModel,
    private val totalVideos: Int
) : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position], position, context, viewModel, totalVideos, viewModel,this@SessionAdapter,mList)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(val binding: ItemSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        class ViewDialog1 {
            fun showDialog1(activity: Activity?, viewModel: SessionViewModel, date: Long) {
                val dialog = Dialog(activity!!)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                val viewBinding = CustomDialogDeleteBinding.inflate(activity.layoutInflater)
                dialog.setContentView(viewBinding.root)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                viewBinding.tvMessage.text = "Are you sure you want to delete this session?"
                viewBinding.tvNo.setOnClickListener { dialog.dismiss() }
                viewBinding.tvYes.setOnClickListener {
                    dialog.dismiss()

                }
                viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }
        }

        fun bind(
            video: SessionsMainModel,
            pos: Int,
            context: SessionsActivity,
            database: SessionViewModel,
            totalVideos: Any?,
            viewModel: SessionViewModel?,
            sessionAdapter: SessionAdapter,
            mList: ArrayList<SessionsMainModel>
        ) {
            with(itemView) {


                // Save/restore the open/close state.
                // You need to provide a String id which uniquely defines the data object.

                // Save/restore the open/close state.
                // You need to provide a String id which uniquely defines the data object.
                binding.tvDay.text = video.dateTime.toTimeAgo()
                binding.tvDate.text = convertLongToDate(video.dateTime)
                binding.tvTotal.text = "Total Videos: $totalVideos"

                if (pos == 0) {
                    binding.tvTotal.visibility = View.VISIBLE
                    binding.viewLine.visibility = View.GONE

                } else {
                    binding.tvTotal.visibility = View.GONE
                    binding.viewLine.visibility = View.VISIBLE

                }

                binding.mainLay.setOnLongClickListener(View.OnLongClickListener {
                    val alert = ViewDialog1()
                    alert.showDialog1(context, viewModel!!, video.dateTime)
                    return@OnLongClickListener true
                })
                binding.mainLay.setOnClickListener {
                    if (binding.rv.isVisible) {
                        binding.ivExpand.setImageDrawable(
                            context.resources.getDrawable(
                                R.drawable.ic_baseline_keyboard_arrow_down_24,
                                context.applicationContext.theme
                            )
                        )
                        binding.rv.visibility = View.GONE
                    } else {
                        binding.ivExpand.setImageDrawable(
                            context.resources.getDrawable(
                                R.drawable.ic_baseline_keyboard_arrow_up_24,
                                context.applicationContext.theme
                            )
                        )
                        binding.rv.visibility = View.VISIBLE
                    }
                }

                binding.rv.layoutManager = LinearLayoutManager(context)
                var data = ArrayList<Video>()
                data = video.videoData as ArrayList<Video>
                val adapter = VideoAdapter(data, context, viewModel!!,sessionAdapter,mList,
                    mList[pos]
                )
                binding.rv.adapter = adapter
            }


        }
    }

}


