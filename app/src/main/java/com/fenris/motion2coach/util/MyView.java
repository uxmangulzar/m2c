package com.fenris.motion2coach.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class MyView extends View {
    private Paint paint;
    private Path path;
    private String text;
    private float x, y;
    private float startX, startY;
    private boolean isTextSelected;
    private RectF pathBounds;

    public MyView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        paint.setStyle(Paint.Style.STROKE); // Set the style to stroke
        paint.setStrokeWidth(5); // Set the stroke width to 5 pixels
        path = new Path();
        path.moveTo(50, 50);
        path.lineTo(100, 100);
        text = "Hello, world!";
        x = y = 100; // Initialize the text position

        // Compute the bounding rectangle of the path
        pathBounds = new RectF();
        path.computeBounds(pathBounds, true);
    }

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        paint.setStyle(Paint.Style.STROKE); // Set the style to stroke
        paint.setStrokeWidth(5); // Set the stroke width to 5 pixels
        path = new Path();
        path.moveTo(50, 50);
        path.lineTo(100, 100);
        text = "Hello, world!";
        x = y = 100; // Initialize the text position

        // Compute the bounding rectangle of the path
        pathBounds = new RectF();
        path.computeBounds(pathBounds, true);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        paint.setStyle(Paint.Style.STROKE); // Set the style to stroke
        paint.setStrokeWidth(5); // Set the stroke width to 5 pixels
        path = new Path();
        path.moveTo(50, 50);
        path.lineTo(100, 100);
        text = "Hello, world!";
        x = y = 100; // Initialize the text position

        // Compute the bounding rectangle of the path
        pathBounds = new RectF();
        path.computeBounds(pathBounds, true);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        path = new Path();
        path.moveTo(50, 50);
        path.lineTo(100, 100);
        text = "Hello, world!";
        x = y = 100; // Initialize the text position

        // Compute the bounding rectangle of the path
        pathBounds = new RectF();
        path.computeBounds(pathBounds, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
        canvas.drawText(text, x, y, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check if touch event is within the bounds of the text or path
                if (eventX >= x && eventX <= x + paint.measureText(text) &&
                        eventY >= y - paint.getTextSize() && eventY <= y) {
                    isTextSelected = true;
                    startX = eventX - x;
                    startY = eventY - y;
                    return true;
                } else if (pathBounds.contains((int) eventX, (int) eventY)) {
                    isTextSelected = false;
                    startX = eventX - pathBounds.left;
                    startY = eventY - pathBounds.top;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // Move the text or path based on touch event
                if (isTextSelected) {
                    x = eventX - startX;
                    y = eventY - startY;
                } else {
                    path.offset(eventX - startX - pathBounds.left,
                            eventY - startY - pathBounds.top);
                    pathBounds.offsetTo(eventX - startX, eventY - startY);
                }
                invalidate(); // Redraw the view to update the text and path positions
                return true;
        }

        return super.onTouchEvent(event);
    }
}
