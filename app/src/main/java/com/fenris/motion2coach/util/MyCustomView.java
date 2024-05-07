package com.fenris.motion2coach.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MyCustomView extends View {
    private List<String> userInputList = new ArrayList<>();
    private List<Float> userXInputList = new ArrayList<>();
    private List<Float> userYInputList = new ArrayList<>();
    private Paint paint;
    String mText;
    EditText editText;
    private MotionEvent lastEvent;

    public MyCustomView(Context context) {
        this(context, null, 0);
        // Create a Paint object to define the text color, size, and other properties
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(100);
    }

    public MyCustomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // Create a Paint object to define the text color, size, and other properties
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(100);
    }

    public MyCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Create a Paint object to define the text color, size, and other properties
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(100);
    }
    public void setEditText(EditText editText1) {
        editText = editText1;
        editText.setVisibility(View.GONE);
    }
    public void setText(String s) {
        editText.setVisibility(View.GONE);
        mText = s;
        userInputList.add(s);
        userXInputList.add(lastEvent.getX());
        userYInputList.add(lastEvent.getY());
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw all the user input text on the canvas
        for (int i =0;i<userInputList.size();i++) {
            canvas.drawText(userInputList.get(i), userXInputList.get(i), userYInputList.get(i), paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

//            if (!firstCalled){
//                firstCalled=true;
//                ((ViewGroup) getParent()).addView(editText);
//            }
            // Show the EditText to allow the user to enter text
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
//            mText = new SpannableStringBuilder("");

            lastEvent=event;
            // Set the target position on the screen (in pixels)
            int targetX = (int) event.getX(); // replace with your target X coordinate
            int targetY = (int) event.getY(); // replace with your target Y coordinate

// Calculate the position of the EditText view within its parent view
            int[] location = new int[2];
            editText.getLocationInWindow(location);
            int editTextX = location[0];
            int editTextY = location[1];

// Calculate the difference between the target position and the current position of the EditText view
            int deltaX = targetX - editTextX;
            int deltaY = targetY - editTextY;

// Move the EditText view to the target position
            editText.setX(editText.getX() + deltaX);
            editText.setY(editText.getY() + deltaY);
            editText.setVisibility(View.VISIBLE);
            editText.requestFocus();

//            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
            return true;
        }

        return false;
    }



}

