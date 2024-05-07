package com.fenris.motion2coach.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView


@SuppressLint("AppCompatCustomView")
class CustomEditImageView : ImageView, OnTouchListener {
    var startX = 0f
    var startY = 0f
    var endX = 0f
    var endY = 0f
    lateinit var canvas: Canvas
    lateinit var paint: Paint
    lateinit var EditImagematrix: Matrix

    constructor(context: Context?) : super(context) {
        setOnTouchListener(this)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        setOnTouchListener(this)
    }

    constructor(
        context: Context?, attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        setOnTouchListener(this)
    }

    fun setNewImage( bmp: Bitmap, edit: Boolean?=false) {
        if(edit == true){
            canvas = Canvas(bmp)
            paint = Paint()
            paint.color = Color.BLUE
            paint.strokeWidth=9f

            canvas.drawColor(Color.TRANSPARENT)
            EditImagematrix = Matrix()
//            canvas.drawCircle(x, y, 16f, paint)
//            canvas.drawBitmap(bmp, EditImagematrix, paint)
            setImageBitmap(bmp)
        }else{
            canvas = Canvas(bmp)
            paint = Paint()
            paint.color = Color.TRANSPARENT
            paint.strokeWidth=0f
            EditImagematrix = Matrix()
//            canvas.drawBitmap(bmp, EditImagematrix, paint)
            setImageBitmap(bmp)
        }

    }
//    fun setNewImage(alteredBitmap: Bitmap, bmp: Bitmap, edit: Boolean?=false) {
//        if(edit == true){
//            canvas = Canvas(alteredBitmap)
//            paint = Paint()
//            paint.color = Color.BLUE
//            paint.strokeWidth=9f
//            canvas.drawColor(Color.argb(0, 255, 255, 255));
//            EditImagematrix = Matrix()
//            canvas.drawBitmap(alteredBitmap, EditImagematrix, paint)
//            setImageBitmap(alteredBitmap)
//        }else{
//            canvas = Canvas(alteredBitmap)
//            paint = Paint()
//            EditImagematrix = Matrix()
//            canvas.drawBitmap(bmp, EditImagematrix, paint)
//            setImageBitmap(alteredBitmap)
//        }
//
//    }

    override fun onDraw(canvas1: Canvas?) {
        if (::paint.isInitialized){
            super.onDraw(canvas1)
            canvas1!!.drawLine(startX, startY, endX, endY, paint)
            invalidate()
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                startX = getPointerCoords(event)[0]
                startY = getPointerCoords(event)[1]
                // Set the end to prevent initial jump (like on the demo recording)
                endX = getPointerCoords(event)[0]
                endY = getPointerCoords(event)[1]
//                invalidate()
//                downx = getPointerCoords(event)[0] //event.getX();
//                downy = getPointerCoords(event)[1] //event.getY();
            }
            MotionEvent.ACTION_MOVE -> {
                endX = getPointerCoords(event)[0]
                endY = getPointerCoords(event)[1]
//                invalidate()
//                upx = getPointerCoords(event)[0] //event.getX();
//                upy = getPointerCoords(event)[1] //event.getY();
//                canvas.drawCircle(downx, downy, 30f, paint)
//                invalidate()
//                downx = upx
//                downy = upy
            }
            MotionEvent.ACTION_UP -> {
                endX = getPointerCoords(event)[0]
                endY = getPointerCoords(event)[1]
//                invalidate()
//                upx = getPointerCoords(event)[0] //event.getX();
//                upy = getPointerCoords(event)[1] //event.getY();
//                canvas.drawLine(downx, downy, upx, upy, paint)
////                canvas.drawCircle(upx, upy, 60f, paint)
//                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
            }
            else -> {
            }
        }
        return true
    }

    private fun getPointerCoords(e: MotionEvent): FloatArray {
        val index = e.actionIndex
        val coords = floatArrayOf(e.getX(index), e.getY(index))
        val matrix = Matrix()
        imageMatrix.invert(matrix)
        matrix.postTranslate(scrollX.toFloat(), scrollY.toFloat())
        matrix.mapPoints(coords)
        return coords
    }
}