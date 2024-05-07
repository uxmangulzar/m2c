package com.fenris.motion2coach.view.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.fenris.motion2coach.R
import com.fenris.motion2coach.util.MyCustomView


class TestActivity : AppCompatActivity() {
    var processFinished = false
    private var canvas: MyCustomView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        canvas = findViewById<View>(R.id.canvas) as MyCustomView
        var mEditText = findViewById<EditText>(R.id.editText_search)
//        canvas!!.setEditText(mEditText)
////        mEditText.addTextChangedListener(object : TextWatcher {
////            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
////            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
////                // Update the custom view with the new text
////
////                // Redraw the custom view
////
////            }
////
////            override fun afterTextChanged(s: Editable) {}
////        })
//        mEditText.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                canvas!!.setText(mEditText.text.toString())
//                mEditText.setText("")
//                hideKeyboard(this)
//                return@setOnEditorActionListener true
//            }
//            false
//        }
//        canvas!!.drawer = CanvasView.Drawer.LINE
//        canvas!!.paintStyle = Paint.Style.STROKE
        // Getter
//        val lineView = findViewById<LineView>(R.id.canvas)
//        lineView.setStartPoint(PointF(100f, 100f))
//        lineView.setEndPoint(PointF(300f, 300f))
    }
    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}