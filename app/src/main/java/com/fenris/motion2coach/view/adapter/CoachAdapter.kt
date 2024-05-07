package com.fenris.motion2coach.view.adapter

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.CustomDialogDeleteBinding
import com.fenris.motion2coach.databinding.ItemCoachBinding
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.AcceptInvitationRequestModel
import com.fenris.motion2coach.network.requests.RemoveInvitationRequestModel
import com.fenris.motion2coach.network.responses.CoachesResponse
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.viewmodel.InvitationViewModel
import kotlin.collections.ArrayList


class CoachAdapter(
    private val mList: ArrayList<CoachesResponse>, private val context: Activity,
    private val viewModel: InvitationViewModel,
    private val linNoRequests: LinearLayout
) : RecyclerView.Adapter<CoachAdapter.ViewHolder>() {



    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val binding = ItemCoachBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position],position,mList.size,context, viewModel,mList,this@CoachAdapter,linNoRequests)

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(val binding: ItemCoachBinding) :RecyclerView.ViewHolder(binding.root) {
        lateinit var progressDialog :com.techiness.progressdialoglibrary.ProgressDialog



        class ViewDialog {


            fun showAcceptDialog(
                activity: Activity?,
                viewModel: InvitationViewModel,
                mList: ArrayList<CoachesResponse>,
                pos: Int,
                coachAdapter: CoachAdapter,
                linNoRequests: LinearLayout

            ) {
                val dialog = Dialog(activity!!)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                val viewBinding = CustomDialogDeleteBinding.inflate(activity.layoutInflater)
                dialog.setContentView(viewBinding.root)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                viewBinding.tvMessage.text ="Are you sure you want to accept\nthis coach?"

                viewBinding.tvNo.setOnClickListener { dialog.dismiss() }
                viewBinding.tvYes.setOnClickListener {
                    dialog.dismiss()
                    fetchAcceptInvitation(mList[pos].coachId,activity,viewModel,mList,coachAdapter,pos,linNoRequests)
                }
                viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }
            fun showRemoveDialog(
                activity: Activity?,
                viewModel: InvitationViewModel,
                mList: ArrayList<CoachesResponse>,
                pos: Int,
                coachAdapter: CoachAdapter,
                linNoRequests: LinearLayout

            ) {
                val dialog = Dialog(activity!!)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                val viewBinding = CustomDialogDeleteBinding.inflate(activity.layoutInflater)
                dialog.setContentView(viewBinding.root)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                viewBinding.tvMessage.text ="Are you sure you want to remove request of this coach?"

                viewBinding.tvNo.setOnClickListener { dialog.dismiss() }
                viewBinding.tvYes.setOnClickListener {
                    dialog.dismiss()
                    fetchRemoveInvitation(mList[pos].coachId,activity,viewModel,mList,coachAdapter,pos,linNoRequests)
                }
                viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }


            private fun fetchAcceptInvitation(
                id: Int?,
                activty: Activity,
                viewModel: InvitationViewModel,
                mList: ArrayList<CoachesResponse>,
                coachAdapter: CoachAdapter,
                pos: Int,
                linNoRequests: LinearLayout
            ) {
                val progressDialog = ProgressDialog(activty)
                progressDialog.setMessage("Loading.Please wait..")
                progressDialog.setCancelable(false)
                progressDialog.show()
                val invitationRequestModel = AcceptInvitationRequestModel(
                    SessionManager.getStringPref(HelperKeys.USER_ID, activty).toInt(),
                    id!!,
                    SessionManager.getStringPref(HelperKeys.ROLE_ID, activty).toInt()


                )
                viewModel.fetchAcceptInvitation(invitationRequestModel)
                viewModel.response_acceptInvitation.observeAsEvent((activty as? LifecycleOwner)!!, androidx.lifecycle.Observer {
                    if (it != null) {
                        when (it) {
                            is NetworkResult.Success -> {

                                progressDialog.dismiss()
                                // bind data to the view
                                Toast.makeText(activty,it.data!!.message,Toast.LENGTH_SHORT).show()
                                mList.remove(mList[pos])
                                coachAdapter.notifyDataSetChanged()
                                if (mList.size==0){
                                    linNoRequests.visibility=View.VISIBLE
                                }else{
                                    linNoRequests.visibility=View.GONE

                                }
                            }
                            is NetworkResult.Error -> {
                                progressDialog.dismiss()
                                if (it.statusCode==401){
                                    activty.startActivity(
                                        Intent(activty,
                                            LoginActivity::class.java)
                                    )
                                    activty.finishAffinity()
                                }
                                // show error message
                                Toast.makeText(
                                    activty,
                                    it.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is NetworkResult.Loading -> {
                                // show a progress bar
                            }
                        }
                    }
                })

            }
            private fun fetchRemoveInvitation(
                id: Int?,
                activty: Activity,
                viewModel: InvitationViewModel,
                mList: ArrayList<CoachesResponse>,
                coachAdapter: CoachAdapter,
                pos: Int,
                linNoRequests: LinearLayout
            ) {
                val progressDialog = ProgressDialog(activty)
                progressDialog.setMessage("Loading.Please wait..")
                progressDialog.setCancelable(false)
                progressDialog.show()
                val invitationRequestModel = RemoveInvitationRequestModel(
                    SessionManager.getStringPref(HelperKeys.USER_ID, activty).toInt(),
                    id!!,
                    SessionManager.getStringPref(HelperKeys.ROLE_ID, activty).toInt()
                )
                viewModel.fetchRemoveInvitation(invitationRequestModel)
                viewModel.response_removeInvitation.observeAsEvent((activty as? LifecycleOwner)!!, androidx.lifecycle.Observer {
                    if (it != null) {
                        when (it) {
                            is NetworkResult.Success -> {

                                progressDialog.dismiss()
                                // bind data to the view
                                Toast.makeText(activty,it.data!!.message,Toast.LENGTH_SHORT).show()
                                mList.remove(mList[pos])
                                coachAdapter.notifyDataSetChanged()
                                if (mList.size==0){
                                    linNoRequests.visibility=View.VISIBLE
                                }else{
                                    linNoRequests.visibility=View.GONE

                                }
                            }
                            is NetworkResult.Error -> {
                                progressDialog.dismiss()
                                if (it.statusCode==401){
                                    activty.startActivity(Intent(activty, LoginActivity::class.java))
                                    activty.finishAffinity()
                                }
                                // show error message
                                Toast.makeText(
                                    activty,
                                    it.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is NetworkResult.Loading -> {
                                // show a progress bar
                            }
                        }
                    }
                })

            }
        }

        fun bind(
            allowedUserResponse: CoachesResponse,
            pos: Int,
            total: Int,
            con: Activity,
            viewModel: InvitationViewModel?,
            mList: ArrayList<CoachesResponse>,
            coachAdapter: CoachAdapter,
            linNoRequests: LinearLayout
        ) {
            with(itemView) {


                binding.tvName.text = allowedUserResponse.coachfirstName+" "+allowedUserResponse.coachlastName
                if (SessionManager.getStringPref(HelperKeys.ROLE_NAME,con)=="Coach"){
                    binding.tvType.text = "Striker"

                }else{
                    binding.tvType.text = "Coach"

                }
                if (allowedUserResponse.accepted){
                    binding.ivAccept.visibility = View.INVISIBLE
                }
                val options = RequestOptions()
                    .placeholder(R.drawable.thumbnail)
                    .error(R.drawable.thumbnail)
                    .transform(CenterCrop(), RoundedCorners(24))


                Glide.with(con)
                    .load(allowedUserResponse.coachpicture)
                    .apply(options)
                    .transform(CenterCrop(), RoundedCorners(24))
                    .into(binding.ivThumbnail)


//                binding.deleteLayout.setOnClickListener {
//                    videoDeleteDialog(con, viewModel!!, video.uid.toInt())
//                    alert.showDialog(con,  viewModel!!, allowedUserResponse.firstName,mList,pos,coachAdapter)
//                }
                binding.ivThumbnail.setOnClickListener {
//                    var nameString: String = binding.tvName.text.toString().replace(".mp4", "")
//                    nameString += ""
//                    val intent = Intent(con, PlayVideoActivity::class.java)
//                    intent.putExtra("path", allowedUserResponse.)
//                    intent.putExtra("file_name", nameString)
//
//                    con.startActivity(intent)
                }
                binding.tvName.setOnClickListener {
//                    var nameString: String = binding.tvName.text.toString().replace(".mp4", "")
//                    nameString += ""
//                    val intent = Intent(con, PlayVideoActivity::class.java)
//                    intent.putExtra("path", video.video)
//                    intent.putExtra("file_name", nameString)
//
//                    con.startActivity(intent)
                }
                binding.ivAccept.setOnClickListener {

                    val alert = ViewHolder.ViewDialog()
                    alert.showAcceptDialog(con,  viewModel!!,mList,pos,coachAdapter,linNoRequests)
                }


            }
            binding.ivReject.setOnClickListener {


                val alert = ViewHolder.ViewDialog()
                alert.showRemoveDialog(con,  viewModel!!,mList,pos,coachAdapter,linNoRequests)


            }

            }
        }

    }





