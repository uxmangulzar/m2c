package com.fenris.motion2coach.view.adapter

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.fenris.motion2coach.R
import com.fenris.motion2coach.databinding.CustomDialogDeleteBinding
import com.fenris.motion2coach.databinding.CustomDialogProcessInfoBinding
import com.fenris.motion2coach.databinding.ItemVideoBinding
import com.fenris.motion2coach.model.Video
import com.fenris.motion2coach.model.SessionsMainModel
import com.fenris.motion2coach.network.NetworkResult
import com.fenris.motion2coach.network.requests.DeleteVideoRequestModel
import com.fenris.motion2coach.util.Constants
import com.fenris.motion2coach.util.Helper.convertLongToTime
import com.fenris.motion2coach.util.Helper.observeAsEvent
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import com.fenris.motion2coach.view.activity.login.LoginActivity
import com.fenris.motion2coach.view.activity.player.PlayVideoActivity
import com.fenris.motion2coach.view.activity.report.ReportOneActivity
import com.fenris.motion2coach.view.activity.report.PdfViewerActivity
import com.fenris.motion2coach.viewmodel.SessionViewModel
import okhttp3.*
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import kotlin.collections.ArrayList


class VideoAdapter(
    private val mList: ArrayList<Video>, private val context: Activity,
    private val viewModel: SessionViewModel,
    private val sessionAdapter: SessionAdapter,
    private val sessionList1: ArrayList<SessionsMainModel>,
    private val posSessionModel: SessionsMainModel
) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {
    private var webSocket: WebSocket? = null



    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position],position,mList.size,context, viewModel,mList,this@VideoAdapter,sessionAdapter,sessionList1,posSessionModel)

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(val binding: ItemVideoBinding) :RecyclerView.ViewHolder(binding.root) {
        private var webSocket: WebSocket? = null
        lateinit var progressDialog :com.techiness.progressdialoglibrary.ProgressDialog

        private fun instantiateWebSocket(
            activity: Activity?,
            url: String,
            video: Video,
            binding: ItemVideoBinding
        ) {
            //Create OKhttp client Object
            progressDialog = com.techiness.progressdialoglibrary.ProgressDialog(activity!!)
            progressDialog.setMessage("Checking status .Please wait..")
            progressDialog.show()
            val client = OkHttpClient()
            //Build Okhttp request
            val request: Request =
                Request.Builder().url(Constants.BASE_URL_SOCKETS)
                    .build() //Ip address
            //Create Object of socket listener and pass Main Activity its parameter
            val socketListner =
                SocketListner(activity!!,url,progressDialog,video,binding)
            //Create Web Socket Object pass
            webSocket = client.newWebSocket(request, socketListner)
        }

        //Create SocketListener class that extend websocket listener
        // that implement different methods that will will called after query fires up
        //All thses methods call from background threads we cannot touch directly android views
        class SocketListner(
            activity: Activity,
            actualUrl: String,
            progressDialog: com.techiness.progressdialoglibrary.ProgressDialog,
            video: Video,
            binding: ItemVideoBinding
        ) :
            WebSocketListener() {
            var activity: Activity
            private var actualUrl:String
            private var video: Video
            private var binding: ItemVideoBinding
            private var progressDialog: com.techiness.progressdialoglibrary.ProgressDialog

            //These constructor will make a reference to main activity class
            init {
                this.activity = activity
                this.actualUrl = actualUrl
                this.progressDialog = progressDialog
                this.video = video
                this.binding = binding
            }

            //Call when connection establish with the server
            override fun onOpen(webSocket: WebSocket, response: Response) {
                activity.runOnUiThread(Runnable {

//                    Toast.makeText(
//                        activity,
//                        "Connection Successfully Established",
//                        Toast.LENGTH_LONG
//                    ).show()
                    val jsonObject = JSONObject()
                    try {
                        jsonObject.put("action", "status")
                        //Tell whether the message is send by the server or not true mean yes message is sent by the server
                        jsonObject.put(
                            "file_id",
                            actualUrl
                        )

                        webSocket.send(jsonObject.toString()) //If message is not empty the we will send message to the server

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    progressDialog.dismiss()
                })
            }

            //Call when their is a new messages in strings
            override fun onMessage(webSocket: WebSocket, text: String) {
                activity.runOnUiThread(Runnable {

                    try {
                        webSocket.cancel()
//                        webSocket.close(1100,"just closing")

                        val obj = JSONObject(text)
                        val status = obj.getString("status")
                        if (status == "done") {

                            val fileName1=File(video.video).name.substringBefore(".mp4")
                            val nameString: String =fileName1.replace(".mp4", "")
                            val newString: String = video.overlayUrl!!.replace(".mp4", "")

                            SessionManager.putStringPref(HelperKeys.SWING_TYPE_ID,video.swingTypeId.toString(),activity)
                            val splitArray: List<String> = newString.split("/")

                            val url =
                                "https://motion2coach-computervision-results.s3.eu-central-1.amazonaws.com/$newString.mp4"
                            Log.e("TAG", url)
                            val intent = Intent(activity, ReportOneActivity::class.java)
                            intent.putExtra("file_id", video.uid)
                            intent.putExtra("path", video.overlayUrl)
                            intent.putExtra("original_video_path", video.video)
                            intent.putExtra("status", video.status)
                            intent.putExtra("u_id", video.user_id!!.toInt())
                            intent.putExtra("file_name", nameString)
                            intent.putExtra("highlight_fileid", splitArray[3]+"/"+splitArray[4]+".json")
                            activity.startActivity(intent)
                        } else if (status == "error") {
                            val alert = ViewDialog()
                            alert.showDialog12(activity,status)
                        }else{
                            val alert = ViewDialog()
                            alert.showDialog12(activity,status)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }


                })
            }

            //Call when their is a new messages in bytes
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
            }

            //Call just before the connecton is close
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
            }

            //Call when the connection closes
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
            }

            //Call when some error occurs
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                activity.runOnUiThread(Runnable {
//                Toast.makeText(
//                    activity,
//                    "Some Error Occur $t",
//                    Toast.LENGTH_LONG
//                ).show()
                })
            }
        }

        class ViewDialog {



            fun showDialog12(
                activity: Activity?,
                status: String?
            ) {
                val dialog = Dialog(activity!!)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                val viewBinding = CustomDialogProcessInfoBinding.inflate(activity.layoutInflater)
                dialog.setContentView(viewBinding.root)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                viewBinding.info.text=status
                if (status=="error"){
                    viewBinding.infoDetails.text="You have uploaded the video with wrong metadata. Please record again"
                }else{
                    viewBinding.infoDetails.text="Generating results , please wait.."
                }


                viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }
            fun showDialog(
                activity: Activity?,
                viewModel: SessionViewModel,
                url: String?,
                mList: ArrayList<Video>,
                pos: Int,
                videoAdapter: VideoAdapter,
                sessionAdapter: SessionAdapter,
                sessionList1: ArrayList<SessionsMainModel>?,
                posSession: SessionsMainModel,
                video: Video
            ) {
                val dialog = Dialog(activity!!)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                val viewBinding = CustomDialogDeleteBinding.inflate(activity.layoutInflater)
                dialog.setContentView(viewBinding.root)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                viewBinding.tvNo.setOnClickListener { dialog.dismiss() }
                viewBinding.tvYes.setOnClickListener {
                    dialog.dismiss()
                    fetchDeleteVideo(url,activity,viewModel,mList,pos,videoAdapter,sessionAdapter,sessionList1,posSession,video)
                }
                viewBinding.ivCross.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }
            private fun fetchDeleteVideo(
                url: String?,
                activty: Activity,
                viewModel: SessionViewModel,
                mList: ArrayList<Video>,
                pos: Int,
                videoAdapter: VideoAdapter,
                sessionAdapter: SessionAdapter,
                sessionList1: ArrayList<SessionsMainModel>?,
                posSession: SessionsMainModel,
                video: Video
            ) {
                val progressDialog = ProgressDialog(activty)
                progressDialog.setMessage("Loading.Please wait..")
                progressDialog.setCancelable(false)
                progressDialog.show()
                val deleteVideoRequestModel = DeleteVideoRequestModel(
                    SessionManager.getStringPref(HelperKeys.USER_ID, activty).toInt(), url.toString()
                )
                viewModel.fetchDeleteVideo(deleteVideoRequestModel)
                viewModel.response_deleteVideo.observeAsEvent((activty as? LifecycleOwner)!!, androidx.lifecycle.Observer {
                    if (it != null) {
                        when (it) {
                            is NetworkResult.Success -> {

                                progressDialog.dismiss()
                                // bind data to the view
                                Toast.makeText(activty,"Deletion success",Toast.LENGTH_SHORT).show()
                                mList.remove(video)
                                videoAdapter.notifyDataSetChanged()
//                                if (mList!!.size==0){
//                                    sessionList1!!.remove(posSession!!)
//                                }
//                                sessionAdapter.notifyDataSetChanged()

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
            video: Video,
            pos: Int,
            total: Int,
            con: Activity,
            viewModel: SessionViewModel?,
            mList: ArrayList<Video>,
            videoAdapter: VideoAdapter,
            sessionAdapter: SessionAdapter,
            sessionList1: ArrayList<SessionsMainModel>?,
            posSession: SessionsMainModel
        ) {
            with(itemView) {
                if (pos == 0){
                    binding.tvRec.visibility = View.VISIBLE
                }else{
                    binding.tvRec.visibility = View.GONE
                }
                binding.tvRec.text = "Recording: $total"
                val fileName1=File(video.video).name.substringBefore(".mp4")
                if (SessionManager.getStringPref(HelperKeys.ROLE_NAME,context)=="Striker"){
                    binding.report.isEnabled = false
                }
                binding.tvName.text = video.title
                if (video.swingTypename!=""){
                    binding.tvSwingType.text = video.swingTypename

                }else{
                    binding.tvSwingType.text = "Full Swing"

                }
                if (video.clubTypename!=""){
                    binding.tvClubType.text = video.clubTypename

                }else{
                    binding.tvClubType.text = "Iron"

                }
//                binding.tvTime.text = convertLongToTime(video.time!!)
                val options = RequestOptions()
                    .placeholder(R.drawable.thumbnail)
                    .error(R.drawable.thumbnail)
                    .transform(CenterCrop(), RoundedCorners(24))


                Glide.with(con)
                    .load(R.drawable.thumbnail)
                    .apply(options)
                    .transform(CenterCrop(), RoundedCorners(24))
                    .into(binding.ivThumbnail)


                binding.deleteLayout.setOnClickListener {
                    binding.swipeLayout.close(true)
//                    videoDeleteDialog(con, viewModel!!, video.uid.toInt())
                    val alert = ViewDialog()
                    alert.showDialog(con,  viewModel!!, video.video,mList,pos,videoAdapter,sessionAdapter,sessionList1,posSession,video)
                }
                binding.ivThumbnail.setOnClickListener {
                    val fileName1=File(video.video).name.substringBefore(".mp4")

                    var nameString: String = fileName1.replace(".mp4", "")
                    nameString += ""
                    val intent = Intent(con, PlayVideoActivity::class.java)
                    intent.putExtra("path", video.video)
                    intent.putExtra("file_name", nameString)

                    con.startActivity(intent)
                }
                binding.tvName.setOnClickListener {
                    val fileName1=File(video.video).name.substringBefore(".mp4")

                    var nameString: String = fileName1.replace(".mp4", "")
                    nameString += ""
                    val intent = Intent(con, PlayVideoActivity::class.java)
                    intent.putExtra("path", video.video)
                    intent.putExtra("file_name", nameString)

                    con.startActivity(intent)
                }
                binding.report.setOnClickListener {

                    if (SessionManager.getStringPref(HelperKeys.ROLE_NAME,context)!="Striker"){
                        val fileName1=File(video.video).name.substringBefore(".mp4")

                        val vid = mList[pos]
                        val newString: String = vid.overlayUrl!!.replace(".mp4", "")
                        val nameString: String = fileName1.replace(".mp4", "")


                        val splitArray: List<String> = newString.split("/")
                        val url =
                            "https://motion2coach-computervision-results.s3.eu-central-1.amazonaws.com/$newString.mp4"
                        Log.e("TAG", url)
                        val intent = Intent(con, PdfViewerActivity::class.java)
                        intent.putExtra("path", vid.overlayUrl)
                        intent.putExtra("status", vid.status)
                        intent.putExtra("file_id", vid.uid)
                        intent.putExtra("file_name", nameString)
                        intent.putExtra("highlight_fileid", splitArray[3]+"/"+splitArray[4]+".json")

                        con.startActivity(intent)
                    }

                }
                binding.visualize.setOnClickListener {
//                    val intent=Intent(con, VisualizeActivity::class.java)
//                    con.startActivity(intent)
                    instantiateWebSocket(activity = con,url = video.video!!,video,binding)
                }

            }
        }

    }





}