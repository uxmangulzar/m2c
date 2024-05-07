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
import com.fenris.motion2coach.databinding.ItemStrikerBinding
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.AcceptInvitationRequestModel
import com.fenris.motion2coach.network.requests.RemoveInvitationRequestModel
import com.fenris.motion2coach.network.responses.CoachesResponse
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.activity.dashboard.DashboardActivity
import com.fenris.motion2coach.viewmodel.InvitationViewModel
import kotlin.collections.ArrayList


class StrikerAdapter(
    private val mList: ArrayList<CoachesResponse>, private val context: Activity,
    private val viewModel: InvitationViewModel,
    private val linNoRequests: LinearLayout,
    var indexUsed: Int
) : RecyclerView.Adapter<StrikerAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val binding = ItemStrikerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position],position,mList.size,context, viewModel,mList,this@StrikerAdapter,linNoRequests,indexUsed)
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(val binding: ItemStrikerBinding) :RecyclerView.ViewHolder(binding.root) {
        lateinit var progressDialog :com.techiness.progressdialoglibrary.ProgressDialog


        fun bind(
            allowedUserResponse: CoachesResponse,
            pos: Int,
            total: Int,
            con: Activity,
            viewModel: InvitationViewModel?,
            mList: ArrayList<CoachesResponse>,
            strikerAdapter: StrikerAdapter,
            linNoRequests: LinearLayout,
            indexUsed: Int
        ) {
            with(itemView) {

                if (mList[pos].userSelected){
                    cardSelected(con)
                }
                binding.tvName.text = allowedUserResponse.userfirstName+" "+allowedUserResponse.userlastName
                if (SessionManager.getStringPref(HelperKeys.ROLE_NAME,con)=="Coach"){
                    binding.tvType.text = "Striker"
                }else{
                    binding.tvType.text = "Coach"
                }
                if (allowedUserResponse.accepted){
                    binding.ivAccept.visibility = View.INVISIBLE
                }
                val options = RequestOptions()
                    .placeholder(R.drawable.elipse_avatar)
                    .error(R.drawable.elipse_avatar)
                    .transform(CenterCrop(), RoundedCorners(24))


                Glide.with(con)
                    .load(allowedUserResponse.userpicture)
                    .apply(options)
                    .transform(CenterCrop(), RoundedCorners(24))
                    .into(binding.ivThumbnail)


                binding.cardView.setOnClickListener {
                    if (SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,con)){
                        cardUnSelected(con)
                        mList[indexUsed].userSelected = false
                        strikerAdapter.notifyItemChanged(indexUsed)
                    }else{
                        strikersData(allowedUserResponse,con)
                    }

                }
                binding.ivThumbnail.setOnClickListener {
                    if (SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,con)){
                        cardUnSelected(con)
                        mList[indexUsed].userSelected = false
                        strikerAdapter.notifyItemChanged(indexUsed)
                    }else{
                        strikersData(allowedUserResponse,con)
                    }
                }
                binding.tvName.setOnClickListener {
                    if (SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,con)){
                        cardUnSelected(con)
                        mList[indexUsed].userSelected = false
                        strikerAdapter.notifyItemChanged(indexUsed)
                    }else{
                        strikersData(allowedUserResponse,con)
                    }

                }

                binding.tvType.setOnClickListener {
                    if (SessionManager.getBoolPref(HelperKeys.STRIKER_MODE,con)){
                        cardUnSelected(con)
                        mList[indexUsed].userSelected = false
                        strikerAdapter.notifyItemChanged(indexUsed)
                    }else{
                        strikersData(allowedUserResponse,con)
                    }

                }
                binding.ivAccept.setOnClickListener {
                    val alert = ViewDialog()
                    alert.showAcceptDialog(con,viewModel!!,mList,pos,strikerAdapter,linNoRequests)
                }


            }
            binding.ivReject.setOnClickListener {
                val alert = ViewDialog()
                alert.showRemoveDialog(con,  viewModel!!,mList,pos,strikerAdapter,linNoRequests,binding)
            }

        }
        class ViewDialog {


            fun showAcceptDialog(
                activity: Activity?,
                viewModel: InvitationViewModel,
                mList: ArrayList<CoachesResponse>,
                pos: Int,
                strikerAdapter: StrikerAdapter,
                linNoRequests: LinearLayout
            ) {
                val dialog = Dialog(activity!!)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                val viewBinding = CustomDialogDeleteBinding.inflate(activity.layoutInflater)
                dialog.setContentView(viewBinding.root)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                viewBinding.tvMessage.text ="Are you sure you want to accept\nthis striker?"

                viewBinding.tvNo.setOnClickListener { dialog.dismiss() }
                viewBinding.tvYes.setOnClickListener {
                    dialog.dismiss()
                    fetchAcceptInvitation(mList[pos].userId,activity,viewModel,mList,strikerAdapter,pos,linNoRequests)
                }
                viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }
            fun showRemoveDialog(
                activity: Activity?,
                viewModel: InvitationViewModel,
                mList: ArrayList<CoachesResponse>,
                pos: Int,
                strikerAdapter: StrikerAdapter,
                linNoRequests: LinearLayout,
                binding: ItemStrikerBinding

            ) {
                val dialog = Dialog(activity!!)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                val viewBinding = CustomDialogDeleteBinding.inflate(activity.layoutInflater)
                dialog.setContentView(viewBinding.root)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                viewBinding.tvMessage.text ="Are you sure you want to remove request of this striker?"

                viewBinding.tvNo.setOnClickListener { dialog.dismiss() }
                viewBinding.tvYes.setOnClickListener {
                    dialog.dismiss()
                    fetchRemoveInvitation(mList[pos].userId,activity,viewModel,mList,strikerAdapter,pos,linNoRequests,binding)
                }
                viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }


            private fun fetchAcceptInvitation(
                id: Int?,
                activty: Activity,
                viewModel: InvitationViewModel,
                mList: ArrayList<CoachesResponse>,
                strikerAdapter: StrikerAdapter,
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
                                strikerAdapter.notifyDataSetChanged()
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
            private fun fetchRemoveInvitation(
                id: Int?,
                activty: Activity,
                viewModel: InvitationViewModel,
                mList: ArrayList<CoachesResponse>,
                strikerAdapter: StrikerAdapter,
                pos: Int,
                linNoRequests: LinearLayout,
                binding: ItemStrikerBinding,
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
                                binding.cardView.setCardBackgroundColor(activty.getColor(R.color.white))
                                binding.ivAccept.setColorFilter(activty.getColor(R.color.color_blue), android.graphics.PorterDuff.Mode.SRC_IN)
                                binding.ivReject.setColorFilter(activty.getColor(R.color.color_blue), android.graphics.PorterDuff.Mode.SRC_IN)

                                binding.tvType.setTextColor(activty.getColor(R.color.darker_gray))
                                binding.tvName.setTextColor(activty.getColor(R.color.black))
                                SessionManager.putBoolPref(
                                    HelperKeys.STRIKER_MODE,
                                    false,
                                    activty
                                )

                                SessionManager.putStringPref(
                                    HelperKeys.STRIKER_NAME,
                                    "",
                                    activty
                                )
                                SessionManager.putStringPref(
                                    HelperKeys.STRIKER_ID,
                                    "",
                                    activty
                                )
                                SessionManager.putStringPref(
                                    HelperKeys.STRIKER_HAND_USED,
                                    "",
                                    activty
                                )
                                SessionManager.putStringPref(
                                    HelperKeys.STRIKER_IMAGE,
                                    "",
                                    activty
                                )
                                SessionManager.putStringPref(
                                    HelperKeys.STRIKER_WEIGHT,
                                    "",
                                    activty
                                )
                                SessionManager.putStringPref(
                                    HelperKeys.STRIKER_HEIGHT,
                                    "",
                                    activty
                                )
                                Toast.makeText(activty,it.data!!.message,Toast.LENGTH_SHORT).show()
                                mList.remove(mList[pos])
                                strikerAdapter.notifyDataSetChanged()
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


        private fun cardSelected(con :Activity){
            binding.cardView.setCardBackgroundColor(con.getColor(R.color.color_blue))
            binding.ivAccept.setColorFilter(con.getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
            binding.ivReject.setColorFilter(con.getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)

            binding.tvType.setTextColor(con.getColor(R.color.white))
            binding.tvName.setTextColor(con.getColor(R.color.white))
        }
        private fun cardUnSelected(con :Activity){
            binding.cardView.setCardBackgroundColor(con.getColor(R.color.white))
            binding.ivAccept.setColorFilter(con.getColor(R.color.color_blue), android.graphics.PorterDuff.Mode.SRC_IN)
            binding.ivReject.setColorFilter(con.getColor(R.color.color_blue), android.graphics.PorterDuff.Mode.SRC_IN)

            binding.tvType.setTextColor(con.getColor(R.color.darker_gray))
            binding.tvName.setTextColor(con.getColor(R.color.black))
            SessionManager.putBoolPref(
                HelperKeys.STRIKER_MODE,
                false,
                con
            )

            SessionManager.putStringPref(
                HelperKeys.STRIKER_NAME,
                "",
                con
            )
            SessionManager.putStringPref(
                HelperKeys.STRIKER_ID,
                "",
                con
            )
            SessionManager.putStringPref(
                HelperKeys.STRIKER_HAND_USED,
                "",
                con
            )
            SessionManager.putStringPref(
                HelperKeys.STRIKER_IMAGE,
                "",
                con
            )
            SessionManager.putStringPref(
                HelperKeys.STRIKER_WEIGHT,
                "",
                con
            )
            SessionManager.putStringPref(
                HelperKeys.STRIKER_HEIGHT,
                "",
                con
            )
        }
        private fun strikersData(allowedUserResponse: CoachesResponse, con: Activity) {
            if (allowedUserResponse.accepted){
                SessionManager.putBoolPref(
                    HelperKeys.STRIKER_MODE,
                    true,
                    con
                )
                SessionManager.putStringPref(
                    HelperKeys.STRIKER_ID,
                    allowedUserResponse.userId.toString(),
                    con
                )
                SessionManager.putStringPref(
                    HelperKeys.STRIKER_NAME,
                    allowedUserResponse.userfirstName+" "+allowedUserResponse.userlastName,
                    con
                )
                SessionManager.putStringPref(
                    HelperKeys.STRIKER_HAND_USED,
                    allowedUserResponse.userplayerType,
                    con
                )
                SessionManager.putStringPref(
                    HelperKeys.STRIKER_IMAGE,
                    allowedUserResponse.userpicture,
                    con
                )
                val intent = Intent(con, DashboardActivity::class.java)
                intent.putExtra("userId", allowedUserResponse.userId)
                intent.putExtra("fromStriker", true)
                con.startActivity(intent)
            }
        }
        }

    }





