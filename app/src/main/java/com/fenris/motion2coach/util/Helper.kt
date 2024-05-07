package com.fenris.motion2coach.util

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.Editable
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.properties.Delegates


object Helper {

    private const val SECOND = 1
    private const val MINUTE = 60 * SECOND
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR
    private const val MONTH = 30 * DAY
    private const val YEAR = 12 * MONTH
    const val kMaxChannelValue = 262143
    private val REQUIRED_PERMISSIONS =
        mutableListOf (
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    private fun currentDate(): Long {
        val calendar = Calendar.getInstance()
        return calendar.timeInMillis
    }
    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

     fun isMyServiceRunning(serviceClass: Class<*>, con: Context): Boolean {
        try {
            val manager =
                con.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(
                Int.MAX_VALUE
            )) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }
    fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap? {
        val matrix = Matrix()
        matrix.preScale(
            ((if (horizontal) -1 else 1.toFloat())).toFloat(),
            ((if (vertical) -1 else 1.toFloat())).toFloat()
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    class SingleLiveEvent<T> : MutableLiveData<T>() {

        var curUser: Boolean by Delegates.vetoable(false) { property, oldValue, newValue ->
            newValue != oldValue
        }

        override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
            super.observe(owner, Observer<T> { t ->
                if (curUser) {
                    observer.onChanged(t)
                    curUser = false
                }
            })
        }

        override fun setValue(t: T?) {

            curUser = true
            super.setValue(t)
        }

        fun call() {
            postValue(null)
        }

    }
    open class OnSwipeTouchListener(val context: Context?) : View.OnTouchListener {
        companion object {
            private const val SwipeThreshold = 100
            private const val SwipeVelocityThreshold = 100
        }

        private val gestureDetector = GestureDetector(context, GestureListener())

        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        open fun onSwipeRight(): Boolean {
            return false
        }

        open fun onSwipeLeft(): Boolean {
            return false
        }

        open fun onSwipeUp(): Boolean {
            return false
        }

        open fun onSwipeDown(): Boolean {
            return false
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float,
            ): Boolean {
                try {
                    val diffY = e2!!.y - e1!!.y
                    val diffX = e2.x - e1.x

                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SwipeThreshold && Math.abs(velocityX!!) > SwipeVelocityThreshold) {
                            return when {
                                diffX > 0 -> onSwipeRight()
                                else -> onSwipeLeft()
                            }
                        }
                    } else if (Math.abs(diffY) > SwipeThreshold && Math.abs(velocityY!!) > SwipeVelocityThreshold) {
                        return when {
                            diffY > 0 -> onSwipeDown()
                            else -> onSwipeUp()
                        }
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                return false
            }
        }
    }
    fun <T> LiveData<T>.observeAsEvent(owner: LifecycleOwner, observer: Observer<in T>) {
        var previousKey: Any? = value?: NULL
        observe(owner) { value ->
            if (previousKey == NULL || previousKey != value) {
                previousKey = value
                observer.onChanged(value)
            }
        }
    }

    private const val NULL = "NULL"
    open class Event<out T>(private val content: T) {

        @Suppress("MemberVisibilityCanBePrivate")
        var hasBeenHandled = false
            private set // Allow external read but not write

        /**
         * Returns the content and prevents its use again.
         */
        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        /**
         * Returns the content, even if it's already been handled.
         */
        fun peekContent(): T = content
    }

    fun allPermissionsGranted(context: Context) = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            context, it) == PackageManager.PERMISSION_GRANTED
    }
    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    fun isValidEmailAddress(email: String?): Boolean {
        val ePattern =
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$"
        val p = Pattern.compile(ePattern)
        val m = p.matcher(email)
        return m.matches()
    }

    private fun hasLength(data: CharSequence): Boolean {
        return data.toString().length >= 8
    }

    fun hasSymbol(data: CharSequence): Boolean {
        val password = data.toString()
        val special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]")
        val digit = Pattern.compile("[0-9]")

        val hasSpecial: Matcher = special.matcher(password)
        val hasDigit: Matcher = digit.matcher(password)

        return hasSpecial.find() || hasDigit.find()
    }

    fun hasUpperCase(data: CharSequence): Boolean {
        val password = data.toString()
        return password != password.lowercase(Locale.getDefault())
    }

    fun hasLowerCase(data: CharSequence): Boolean {
        val password = data.toString()
        return password != password.uppercase(Locale.getDefault())
    }
    @Throws(IOException::class)
    fun convert(path: String?): ByteArray? {
        val fis = FileInputStream(path)
        val bos = ByteArrayOutputStream()
        val b = ByteArray(1024)
        var readNum: Int
        while (fis.read(b).also { readNum = it } != -1) {
            bos.write(b, 0, readNum)
        }

        return bos.toByteArray()
    }
    fun encodeTobase64(bitmapObtained: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream();
        bitmapObtained.compress(Bitmap.CompressFormat.PNG, 100, stream);
        val byteArray = stream.toByteArray()
        return byteArray
    }

    fun onImageCapture(activity: Activity,type : String) {
        Dexter.withActivity(activity)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    if (type == "take"){
                        ImagePicker.with(activity)
                            .cameraOnly()	//User can only capture image using Camera
                            .start()
                    }else if (type == "choose"){

                        ImagePicker.with(activity)
                            .crop()
                            .galleryOnly()	//User can only select image from Gallery
                            .start()
                    }

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        openPermissionSettings(activity)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    fun openPermissionSettings(activity: Activity){
        AlertDialog.Builder(activity)
            .setTitle("Permission required")
            .setMessage("Grant Permission to access the Camera")
            .setPositiveButton(
                "yes"
            ) { dialogInterface, i ->
                val i = Intent()
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS.also { i.action = it }
                i.addCategory(Intent.CATEGORY_DEFAULT)
                i.data = Uri.parse("package:" + activity.getPackageName())
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                activity.startActivity(i)
                activity.finish()
            }
            .setNegativeButton("No", null)
            .show()
    }



    fun getUserCountry(context: Activity): String? {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simCountry = tm.simCountryIso
            if (simCountry != null && simCountry.length == 2) { // SIM country code is available
                return simCountry.lowercase(Locale.US)
            } else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                val networkCountry = tm.networkCountryIso
                if (networkCountry != null && networkCountry.length == 2) { // network country code is available
                    return networkCountry.lowercase(Locale.US)
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    fun ImageView.loadUrl(url: String) {
        val imageLoader = ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()

        val request = ImageRequest.Builder(this.context)
            .crossfade(true)
            .crossfade(500)
            .data(url)
            .target(this)
            .build()

        imageLoader.enqueue(request)
    }

    //1 minute = 60 seconds
    //1 hour = 60 x 60 = 3600
    //1 day = 3600 x 24 = 86400
    fun printDifference12(startDate: Date, endDate: Date) : String{
        //milliseconds
        var different = endDate.time - startDate.time
        println("startDate : $startDate")
        println("endDate : $endDate")
        println("different : $different")
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val elapsedDays = different / daysInMilli
        different = different % daysInMilli
        val elapsedHours = different / hoursInMilli
        different = different % hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different = different % minutesInMilli
        val elapsedSeconds = different / secondsInMilli
        System.out.printf(
            "%d days, %d hours, %d minutes, %d seconds%n",
            elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds
        )
        if (elapsedDays!=0L&&elapsedDays>0L){
            return "$elapsedDays Days Left"
        }else if (elapsedHours!=0L&&elapsedHours>0L){
            return "$elapsedHours Hours Left"
        }else if (elapsedMinutes!=0L&&elapsedMinutes>0L){
            return "$elapsedMinutes Minutes Left"
        }else{
            return ""
        }

    }
    // Long: time in millisecond
    fun Long.toTimeAgo(): String {
        val time = this
        val now = currentDate()

        // convert back to second
        val diff = (now - time) / 1000

        return when {
            diff < MINUTE -> "Today"
            diff < 2 * MINUTE -> "Today"
            diff < 60 * MINUTE -> "Today"
            diff < 2 * HOUR -> "Today"
            diff < 24 * HOUR -> "Today"
            diff < 2 * DAY -> "Yesterday"
            diff < 30 * DAY -> "${diff / DAY} days ago"
            diff < 2 * MONTH -> "a month ago"
            diff < 12 * MONTH -> "${diff / MONTH} months ago"
            diff < 2 * YEAR -> "a year ago"
            else -> "${diff / YEAR} years ago"
        }
    }
    // Long: time in millisecond
//    fun Long.toTimeAgo(): String {
//        val time = this
//        val now = currentDate()
//
//        // convert back to second
//        val diff = (now - time) / 1000
//
//        return when {
//            diff < MINUTE -> "Today"
//            diff < 2 * MINUTE -> "Today"
//            diff < 60 * MINUTE -> "Today"
//            diff < 2 * HOUR -> "Today"
//            diff < 24 * HOUR -> "Today"
//            diff < 2 * DAY -> "Yesterday"
//            diff < 7 * DAY  -> "This week"
//            diff < 14 * DAY -> "Last week"
//            diff < 30 * DAY -> "Last 30 Days"
//            diff < 2 * MONTH -> "A Month ago"
//            diff < 12 * MONTH -> "${diff / MONTH} Months ago"
//            diff < 2 * YEAR -> "A Year ago"
//            else -> "${diff / YEAR} Years ago"
//        }
//    }




    inline fun <T:Any, R> whenNotNull(input: T?, callback: (T)->R): R? {
        return input?.let(callback)
    }

    infix fun <T> Boolean.then(param: T): T? = if(this) param else null
    fun TextInputEditText.datePicker(fm:FragmentManager, tag: String) {

        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build()


        setOnClickListener {

            datePicker.show(fm,"tag")
        }

        datePicker.addOnPositiveButtonClickListener {
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = it
            val format = SimpleDateFormat("yyyy-MM-dd")
            val formatted: String = format.format(utc.time)
            setText(formatted)
            Log.d("TAG", "datePicker: \n $it")
        }}
    fun datePicker( text1: TextInputEditText,text2: TextInputEditText, text3: TextInputEditText,fm:FragmentManager) {


        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build()




            datePicker.show(fm,"tag")


        datePicker.addOnPositiveButtonClickListener {
            val items: List<String> = datePicker.headerText.split(",")
            val items1: List<String> =items.get(0).split(" ")
            text1.setText(items1.get(0))
            text2.setText(items1.get(1))
            text3.setText(items.get(1))
            Log.d("TAG", "datePicker: \n $it")
        }}
    inline fun FragmentManager.doTransaction(func: FragmentTransaction.() ->
    FragmentTransaction
    ) {
        beginTransaction().func().commit()
    }


    fun convertDateToLong(): Long {
        val df = SimpleDateFormat("dd/MM/yyyy")
        return df.parse(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())).time
    }
    fun convertDateToLong1(date:Date): Long {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        return df.parse(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(date)).time
    }

    fun convertTimeToLong1(date:Date): Long {
        val df = SimpleDateFormat("hh:mm:ss")
        return df.parse(SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(date))!!.time
    }
    fun convertTimeToLong(): Long {
        val df = SimpleDateFormat("hh:mm:ss")
        return df.parse(SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))!!.time
    }


    fun calendarToDate(context: Context, calendar: Calendar?, dateFormat: String?): String? {
        if (calendar == null) {
            return null
        }
        val locale: Locale = context.resources.configuration.locale
        val df: DateFormat = SimpleDateFormat(dateFormat, locale)
        val timeZone = TimeZone.getTimeZone("UTC")
        df.timeZone = timeZone
        val d = calendar.time
        return df.format(d)
    }
    fun convertLongToMins(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("mm:ss")
        return format.format(date)
    }
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("hh:mm:ss a")
        return format.format(date)
    }
    fun convertLongToDate(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }



    fun configOtpEditText(vararg etList: EditText) {
        val afterTextChanged = { index: Int, e: Editable? ->
            val view = etList[index]
            val text = e.toString()

            when (view.id) {
                // first text changed
                etList[0].id -> {
                    if (text.isNotEmpty()) etList[index + 1].requestFocus()
                }

                // las text changed
                etList[etList.size - 1].id -> {
                    if (text.isEmpty()) etList[index - 1].requestFocus()
                }

                // middle text changes
                else -> {
                    if (text.isNotEmpty()) etList[index + 1].requestFocus()
                    else etList[index - 1].requestFocus()
                }
            }
            false
        }
        etList.forEachIndexed { index, editText ->
            editText.doAfterTextChanged { afterTextChanged(index, it) }
        }
    }
    fun AppCompatActivity.addFragment(frameId: Int, fragment: Fragment){

        supportFragmentManager.doTransaction {
            addToBackStack(null)
            add(frameId, fragment) }
    }
    fun AppCompatActivity.onBackpress(view: View){

        onBackPressed()

    }

    fun AppCompatActivity.preventSleep(){
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    fun Fragment.replaceFromFragment(frameId: Int, fragment: Fragment){
        activity!!.supportFragmentManager.doTransaction {
            addToBackStack(null)
            replace(frameId, fragment) }
    }
    fun AppCompatActivity.replaceFromActivity(frameId: Int, fragment: Fragment){
        supportFragmentManager.doTransaction {
            addToBackStack(null)
            replace(frameId, fragment) }
    }

    fun AppCompatActivity.getVersionCode(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
    } else {
        packageManager.getPackageInfo(packageName, 0).versionCode
    }
    fun AppCompatActivity.getAppVersionName(): String {
        var appVersionName = ""
        try {
            appVersionName =
                packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appVersionName
    }
    fun AppCompatActivity.removeFragment(fragment: Fragment) {
        supportFragmentManager.doTransaction{remove(fragment)}
    }
    fun getMimeType(context: Context, uri: Uri): String? {
        var mimeType: String? = null
        mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cr = context.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri
                    .toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase(Locale.getDefault())
            )
        }
        return mimeType
    }
    fun AppCompatActivity.replaceFragment(frameId: Int, fragment: Fragment) {
        supportFragmentManager.doTransaction{replace(frameId, fragment)}
    }
    fun getCurrentDate():String{
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(Date())
    }
    fun getCurrentDateAppFormat():String{
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        return sdf.format(Date())
    }
    fun convertStringToDate(time: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd")
        val objDate: Date = format.parse(time)

        return format.format(objDate)
    }
    fun convertYUV420ToARGB8888(
        yData: ByteArray,
        uData: ByteArray,
        vData: ByteArray,
        width: Int,
        height: Int,
        yRowStride: Int,
        uvRowStride: Int,
        uvPixelStride: Int,
        out: IntArray
    ) {
        var yp = 0
        for (j in 0 until height) {
            val pY = yRowStride * j
            val pUV = uvRowStride * (j shr 1)
            for (i in 0 until width) {
                val uv_offset = pUV + (i shr 1) * uvPixelStride
                out[yp++] = YUV2RGB(
                    0xff and yData[pY + i].toInt(), 0xff and uData[uv_offset]
                        .toInt(), 0xff and vData[uv_offset].toInt()
                )
            }
        }
    }
    private fun YUV2RGB(y: Int, u: Int, v: Int): Int {
        // Adjust and check YUV values
        var y = y
        var u = u
        var v = v
        y = if (y - 16 < 0) 0 else y - 16
        u -= 128
        v -= 128

        // This is the floating point equivalent. We do the conversion in integer
        // because some Android devices do not have floating point in hardware.
        // nR = (int)(1.164 * nY + 2.018 * nU);
        // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
        // nB = (int)(1.164 * nY + 1.596 * nV);
        val y1192 = 1192 * y
        var r = y1192 + 1634 * v
        var g = y1192 - 833 * v - 400 * u
        var b = y1192 + 2066 * u

        // Clipping RGB values to be inside boundaries [ 0 , kMaxChannelValue ]
        r =
            if (r > kMaxChannelValue) kMaxChannelValue else if (r < 0) 0 else r
        g =
            if (g > kMaxChannelValue) kMaxChannelValue else if (g < 0) 0 else g
        b =
            if (b > kMaxChannelValue) kMaxChannelValue else if (b < 0) 0 else b
        return -0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
    }


}