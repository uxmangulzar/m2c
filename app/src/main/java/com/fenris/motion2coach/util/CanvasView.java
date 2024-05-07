package com.fenris.motion2coach.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import com.fenris.motion2coach.model.CanvasItemModel;
import com.fenris.motion2coach.model.PathModel;
import com.fenris.motion2coach.model.ShapeModel;
import com.fenris.motion2coach.model.TextModel;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;




public class CanvasView extends View {
    private Canvas canvas = null;
    private Bitmap bitmap = null;
    private List<CanvasItemModel> pathLists = new ArrayList();
    private List<CanvasItemModel> pathListsForSelectedDelete = new ArrayList();
    //    private List<Paint> paintLists = new ArrayList();
    private final Paint emptyPaint = new Paint();
    private int baseColor = -1;
    private int historyPointer = 0;
    private Mode mode;
    private Drawer drawer;
    private boolean isDown;
    private Style paintStyle;
    private int paintStrokeColor;
    private int paintFillColor;
    private float paintStrokeWidth;
    private int opacity;
    private float blur;
    private Cap lineCap;
    private PathEffect drawPathEffect;
    private String text;
    private Typeface fontFamily;
    private float fontSize;
    private Align textAlign;
    private Paint textPaint;
    private float textX;
    private float textY;

    private float textXMove;
    private float textYMove;

    private float startX;
    private float startXTxt;
    private float startYTxt;
    private float startY;
    int width;
    int height;
    Rect mMeasuredRect = new Rect();
    private float firstStartX;
    private float firstStartY;
    private float controlX;
    private float controlY;
    private Boolean lineDrawn = false;
    private float startDrawLine = 0f;
    float x1 = 0, y1 = 0;
    float x2 = 0, y2 = 0;
    float x3 = 0, y3 = 0;
    float x4 = 0, y4 = 0;

    boolean isDrawingFirstLine = true;
    boolean isDrawingSecondLine = false;
    private Path selectedPath;
    private TextModel selectedtextModel = new TextModel();
    String mText;
    EditText editText;
    private MotionEvent lastEvent;
    float keyboardSelectX, keyboardSelectY = 0f;
    View view;
    TranslateAnimation anim;
    private boolean hideText = false;
    private RectF bounds = new RectF();
    private int movedIndex = 121;
    private Paint movedPaint = new Paint();
    private CanvasItemModel canvasItemModelMoved;
    Boolean isTextSelected = false;
    private RectF pathBounds;
    private boolean isMultiShapeSelected=false;

    public CanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mode = Mode.DRAW;
        this.drawer = Drawer.PEN;
        this.isDown = false;
        this.paintStyle = Style.STROKE;
        this.paintStrokeColor = -16777216;
        this.paintFillColor = -16777216;
        this.paintStrokeWidth = 10.0F;
        this.opacity = 255;
        this.blur = 0.0F;
        this.lineCap = Cap.ROUND;
        this.drawPathEffect = null;
        this.text = "";
        this.fontFamily = Typeface.DEFAULT;
        this.fontSize = 32.0F;
        this.textAlign = Align.RIGHT;
        this.textPaint = new Paint();
        this.textX = 0.0F;
        this.textY = 0.0F;
        this.startX = 0.0F;
        this.startY = 0.0F;
        this.startXTxt = 0.0F;
        this.startYTxt = 0.0F;
        this.controlX = 0.0F;
        this.controlY = 0.0F;
        this.setup();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mode = Mode.DRAW;
        this.drawer = Drawer.PEN;
        this.isDown = false;
        this.paintStyle = Style.STROKE;
        this.paintStrokeColor = -16777216;
        this.paintFillColor = -16777216;
        this.paintStrokeWidth = 10.0F;
        this.opacity = 255;
        this.blur = 0.0F;
        this.lineCap = Cap.ROUND;
        this.drawPathEffect = null;
        this.text = "";
        this.fontFamily = Typeface.DEFAULT;
        this.fontSize = 32.0F;
        this.textAlign = Align.RIGHT;
        this.textPaint = new Paint();
        this.textX = 0.0F;
        this.textY = 0.0F;
        this.startX = 0.0F;
        this.startY = 0.0F;
        this.startXTxt = 0.0F;
        this.startYTxt = 0.0F;
        this.controlX = 0.0F;
        this.controlY = 0.0F;
        this.setup();
    }

    public CanvasView(Context context) {
        super(context);
        this.mode = Mode.DRAW;
        this.drawer = Drawer.PEN;
        this.isDown = false;
        this.paintStyle = Style.STROKE;
        this.paintStrokeColor = -16777216;
        this.paintFillColor = -16777216;
        this.paintStrokeWidth = 10.0F;
        this.opacity = 255;
        this.blur = 0.0F;
        this.lineCap = Cap.ROUND;
        this.drawPathEffect = null;
        this.text = "";
        this.fontFamily = Typeface.DEFAULT;
        this.fontSize = 32.0F;
        this.textAlign = Align.RIGHT;
        this.textPaint = new Paint();
        this.textX = 0.0F;
        this.textY = 0.0F;
        this.startX = 0.0F;
        this.startY = 0.0F;
        this.startXTxt = 0.0F;
        this.startYTxt = 0.0F;
        this.controlX = 0.0F;
        this.controlY = 0.0F;
        this.setup();
    }

    public void setEditText(EditText editText1) {
        editText = editText1;
        editText.setVisibility(View.GONE);
    }

    public void setUpText(String s) {
        textPaint.setColor(paintStrokeColor);

        if (paintStrokeWidth == 10) {
            textPaint.setTextSize(50);
        } else if (paintStrokeWidth == 20) {
            textPaint.setTextSize(80);
        } else if (paintStrokeWidth == 30) {
            textPaint.setTextSize(110);
        }
        if (editText != null) {

            editText.setVisibility(View.GONE);
        }
        mText = s;

        List<ShapeModel> shapeModelList = new ArrayList<>();
        shapeModelList.add(new ShapeModel(new PathModel(), new TextModel(s, textPaint, keyboardSelectX, keyboardSelectY)));
        CanvasItemModel canvasItemModel = new CanvasItemModel("text", shapeModelList);
        this.pathLists.add(canvasItemModel);
        ++this.historyPointer;
        invalidate();
    }

    private void setup() {
        List<ShapeModel> shapeModelList = new ArrayList<>();
        shapeModelList.add(new ShapeModel(new PathModel(), new TextModel()));
        CanvasItemModel canvasItemModel = new CanvasItemModel("path", shapeModelList);
        this.pathLists.add(canvasItemModel);
        ++this.historyPointer;
    }

    private Paint createPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(this.paintStyle);
        paint.setStrokeWidth(this.paintStrokeWidth);
        paint.setStrokeCap(this.lineCap);
        paint.setStrokeJoin(Join.MITER);
        if (this.mode == Mode.TEXT) {
            paint.setTypeface(this.fontFamily);
            paint.setTextSize(this.fontSize);
            paint.setTextAlign(this.textAlign);
            paint.setStrokeWidth(0.0F);
        }

        if (this.mode == Mode.ERASER) {
            paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
            paint.setARGB(0, 0, 0, 0);
        } else {
            paint.setColor(this.paintStrokeColor);
            paint.setShadowLayer(this.blur, 0.0F, 0.0F, this.paintStrokeColor);
            paint.setAlpha(this.opacity);
            paint.setPathEffect(this.drawPathEffect);
        }

        return paint;
    }

    private Path createPath(MotionEvent event) {
        Path path = new Path();

        if (drawer == Drawer.ANGLE_DRAW) {
            if (isDrawingFirstLine) {
                x1 = event.getX();
                y1 = event.getY();
            } else if (isDrawingSecondLine) {
                x3 = event.getX();
                y3 = event.getY();
            }
            if (!lineDrawn) {
                this.startX = event.getX();
                this.startY = event.getY();
                firstStartX = event.getX();
                firstStartY = event.getY();
            }
        } else {
            this.startX = event.getX();
            this.startY = event.getY();
        }
        path.moveTo(this.startX, this.startY);
        return path;
    }

    private void updateHistory(Path path, String type) {
        List<ShapeModel> shapeModelList = new ArrayList<>();
        shapeModelList.add(new ShapeModel(new PathModel(path, this.createPaint()), new TextModel()));
        CanvasItemModel canvasItemModel = new CanvasItemModel(type, shapeModelList);

        if (this.historyPointer == this.pathLists.size()) {

            this.pathLists.add(canvasItemModel);
            ++this.historyPointer;
        } else {
            this.pathLists.set(this.historyPointer, canvasItemModel);
            ++this.historyPointer;
            int i = this.historyPointer;

            for (int size = this.pathLists.size(); i < size; ++i) {
                this.pathLists.remove(this.historyPointer);
            }
        }
    }


    private Path getCurrentPath() {

        return (Path) this.pathLists.get(this.historyPointer - 1).getListOfShapes().get(0).getPathModel().getPath();
    }

    private void drawText(Canvas canvas) {
        if (this.text.length() > 0) {
            this.textX = this.startX;
            this.textY = this.startY;
            textPaint.setColor(paintStrokeColor);
            if (paintStrokeWidth == 10) {
                textPaint.setTextSize(50);
            } else if (paintStrokeWidth == 20) {
                textPaint.setTextSize(80);
            } else if (paintStrokeWidth == 30) {
                textPaint.setTextSize(110);
            }
            int textLength = text.length();
            float textSpacing = 30f;
            // Iterate through each character of the text
            for (int j = 0; j < textLength; j++) {
                // Get the current character
                String character = String.valueOf(text.charAt(j));

                // Create a path for the character
                Path characterPath = new Path();
                characterPath.moveTo(x2 + 70 + j * textSpacing, y2 - 30);
                characterPath.lineTo(x2 + 70 + j * textSpacing + textSpacing, y2 - 30);
                // Draw the character path
                canvas.drawTextOnPath(character, characterPath, 0f, 0f, this.textPaint);
            }
        }
    }


    private void onActionDown(MotionEvent event) {
        switch (this.mode) {
            case DRAW:
            case ERASER:
                if (this.drawer == Drawer.ANGLE_DRAW) {
                    hideText = true;
                }
                if (this.drawer == Drawer.MOVE_SELECT) {

                    selectedPath = null;
                    isTextSelected = false;
                    selectedtextModel = null;
                    textXMove = 0.0f;
                    textYMove = 0.0f;

                }
                if (this.drawer != Drawer.QUADRATIC_BEZIER && this.drawer != Drawer.QUBIC_BEZIER) {
//                    this.updateHistory(this.createPath(event));
                    if (isDrawingSecondLine) {
                        this.updateHistory(this.createPath(event), "angle");
                    } else {
                        this.updateHistory(this.createPath(event), "path");
                    }
                    this.isDown = true;
                } else if (this.startX == 0.0F && this.startY == 0.0F) {
                    if (isDrawingSecondLine) {
                        this.updateHistory(this.createPath(event), "angle");
                    } else {
                        this.updateHistory(this.createPath(event), "path");
                    }
                } else {
                    this.controlX = event.getX();
                    this.controlY = event.getY();
                    this.isDown = true;
                }
                break;
            case TEXT:
                keyboardSelectX = event.getX();
                keyboardSelectY = event.getY() + 80;
                this.startX = event.getX();
                this.startY = event.getY();
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
                editText.setX(startX);
                editText.setY(startY);
                editText.setVisibility(View.VISIBLE);
                editText.requestFocus();
                lastEvent = event;

                break;


            case MOVE_SELECT:
                movedIndex = 121;
                movedPaint = new Paint();
                selectedPath = getTouchedPath((int) event.getX(), (int) event.getY());

                break;


            case MOVE_DELETE:
//                selectedPath = getTouchedPath((int) event.getX(), (int) event.getY());
                break;

        }

    }


    private Path getTouchedPath(final int eventX, final int eventY) {
        Path touched = null;
        selectedtextModel = null;
        isMultiShapeSelected=false;
        for (int i = 0; i < pathLists.size(); i++) {
            if (pathLists.get(i).getType().equals("text")) {

                TextModel textModel = pathLists.get(i).getListOfShapes().get(0).getTextModel();
                if (eventX >= textModel.getX() && eventX <= textModel.getY() + textModel.getPaint().measureText(text) &&
                        eventY >= textModel.getY() - textModel.getPaint().getTextSize() && eventY <= textModel.getY()) {
                    if (pathLists.get(i - 1).getType().equals("angle")) {
                        isMultiShapeSelected=true;
                        Path combinedPath = new Path();
                        combinedPath.addPath(pathLists.get(i - 2).getListOfShapes().get(0).getPathModel().getPath());
                        combinedPath.addPath(pathLists.get(i - 1).getListOfShapes().get(0).getPathModel().getPath());
                        combinedPath.addPath(pathLists.get(i).getListOfShapes().get(0).getPathModel().getPath());
                        pathBounds = new RectF();
                        combinedPath.computeBounds(pathBounds, true);
                        isTextSelected = false;
                        startX = eventX - pathBounds.left;
                        startY = eventY - pathBounds.top;
                        startXTxt = eventX - textModel.getX();
                        startYTxt = eventY - textModel.getY();
                        touched = combinedPath;
                        movedPaint = pathLists.get(i).getListOfShapes().get(0).getPathModel().getPaint();
                        movedIndex = i;
                        canvasItemModelMoved = pathLists.get(i);
                        break;
                    } else {
                        isTextSelected = true;
                        startX = eventX - textModel.getX();
                        startY = eventY - textModel.getY();
                        movedPaint = textModel.getPaint();
                        movedIndex = i;
                        selectedtextModel = textModel;
                        canvasItemModelMoved = pathLists.get(i);
                    }

                    break;
                }

            } else {
                Path path = pathLists.get(i).getListOfShapes().get(0).getPathModel().getPath();
                pathBounds = new RectF();
                path.computeBounds(pathBounds, true);
                if (pathBounds.contains((int) eventX, (int) eventY)) {
                    if (pathLists.get(i).getType().equals("angle")) {
                        isMultiShapeSelected=true;
                        TextModel textModel = pathLists.get(i+1).getListOfShapes().get(0).getTextModel();
                        Path combinedPath = new Path();
                        combinedPath.addPath(pathLists.get(i - 1).getListOfShapes().get(0).getPathModel().getPath());
                        combinedPath.addPath(pathLists.get(i).getListOfShapes().get(0).getPathModel().getPath());
                        combinedPath.addPath(pathLists.get(i+1).getListOfShapes().get(0).getPathModel().getPath());
                        pathBounds = new RectF();
                        combinedPath.computeBounds(pathBounds, true);
                        isTextSelected = false;
                        startX = eventX - pathBounds.left;
                        startY = eventY - pathBounds.top;
                        startXTxt = eventX - textModel.getX();
                        startYTxt = eventY - textModel.getY();
                        touched = combinedPath;
                        movedPaint = pathLists.get(i+1).getListOfShapes().get(0).getPathModel().getPaint();
                        movedIndex = i+1;
                        canvasItemModelMoved = pathLists.get(i);
                    }else if (i+1<pathLists.size()&&pathLists.get(i+1).getType().equals("angle")) {
                        isMultiShapeSelected=true;
                        TextModel textModel = pathLists.get(i+2).getListOfShapes().get(0).getTextModel();
                        Path combinedPath = new Path();
                        combinedPath.addPath(pathLists.get(i).getListOfShapes().get(0).getPathModel().getPath());
                        combinedPath.addPath(pathLists.get(i+1).getListOfShapes().get(0).getPathModel().getPath());
                        combinedPath.addPath(pathLists.get(i+2).getListOfShapes().get(0).getPathModel().getPath());
                        pathBounds = new RectF();
                        combinedPath.computeBounds(pathBounds, true);
                        isTextSelected = false;
                        startX = eventX - pathBounds.left;
                        startY = eventY - pathBounds.top;
                        startXTxt = eventX - textModel.getX();
                        startYTxt = eventY - textModel.getY();
                        touched = combinedPath;
                        movedPaint = pathLists.get(i+2).getListOfShapes().get(0).getPathModel().getPaint();
                        movedIndex = i+2;
                        canvasItemModelMoved = pathLists.get(i);
                    }else{
                        isTextSelected = false;
                        startX = eventX - pathBounds.left;
                        startY = eventY - pathBounds.top;
                        touched = path;
                        movedPaint = pathLists.get(i).getListOfShapes().get(0).getPathModel().getPaint();
                        movedIndex = i;
                        canvasItemModelMoved = pathLists.get(i);
                    }
                    break;
                }
            }
        }
        return touched;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mMeasuredRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    private void onActionMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (this.mode) {
            case DRAW:
            case ERASER:
                Path path;
                if (this.drawer != Drawer.QUADRATIC_BEZIER && this.drawer != Drawer.QUBIC_BEZIER) {
                    if (!this.isDown) {
                        return;
                    }

                    path = this.getCurrentPath();
                    switch (this.drawer) {
                        case PEN:
                            path.lineTo(x, y);
                            return;
                        case LINE:
                            path.reset();
                            path.moveTo(this.startX, this.startY);
                            path.lineTo(x, y);
                            return;
                        case ANGLE_DRAW:
                            x4 = event.getX();
                            y4 = event.getY();

                            path.reset();
                            path.moveTo(this.startX, this.startY);
                            path.lineTo(x, y);
                            if (lineDrawn) {
                                float slope1 = (y2 - y1) / (x2 - x1);
                                float slope2 = (y4 - y3) / (x4 - x3);

                                float angle = (float) Math.atan((slope2 - slope1) / (1 + slope1 * slope2));
                                angle = (float) Math.toDegrees(angle);
                                String angleText = String.format("%.2f", angle) + "°";
                                textPaint.setColor(Color.BLACK);
                                text = angleText;
                                drawText(canvas);
                            }
                            return;

                        case RECTANGLE:
                            path.reset();
                            float left = Math.min(this.startX, x);
                            float right = Math.max(this.startX, x);
                            float top = Math.min(this.startY, y);
                            float bottom = Math.max(this.startY, y);
                            path.addRect(left, top, right, bottom, Direction.CCW);
                            return;
                        case CIRCLE:
                            double distanceX = Math.abs((double) (this.startX - x));
                            double distanceY = Math.abs((double) (this.startX - y));
                            double radius = Math.sqrt(Math.pow(distanceX, 2.0D) + Math.pow(distanceY, 1.25D));
                            path.reset();
                            path.addCircle(this.startX, this.startY, (float) radius, Direction.CCW);
                            return;
                        case ELLIPSE:
                            RectF rect = new RectF(this.startX, this.startY, x, y);
                            path.reset();
                            path.addOval(rect, Direction.CCW);
                            return;

                    }
                } else {
                    if (!this.isDown) {
                        return;
                    }

                    path = this.getCurrentPath();
                    path.reset();
                    path.moveTo(this.startX, this.startY);
                    path.quadTo(this.controlX, this.controlY, x, y);
                }
                break;
            case TEXT:
                this.startX = x;
                this.startY = y;
                break;
            case MOVE_SELECT:
                if (selectedPath != null) {
                    // Update path coordinates
                    selectedPath.offset(x - startX - pathBounds.left,
                            y - startY - pathBounds.top);
                    pathBounds.offsetTo(x - startX, y - startY);
                    List<ShapeModel> ff = new ArrayList<ShapeModel>();
                    if (!isMultiShapeSelected){
                        ff.add(new ShapeModel(new PathModel(selectedPath, movedPaint), new TextModel()));
                        pathLists.set(movedIndex, new CanvasItemModel(canvasItemModelMoved.getType(), ff));
                    }else {
                        ff.add(new ShapeModel(new PathModel(selectedPath, pathLists.get(movedIndex-2).getListOfShapes().get(0).getPathModel().getPaint()), new TextModel()));
                        pathLists.set(movedIndex-2, new CanvasItemModel(pathLists.get(movedIndex-2).getType(), ff));
                        ff = new ArrayList<ShapeModel>();
                        ff.add(new ShapeModel(new PathModel(selectedPath, pathLists.get(movedIndex-1).getListOfShapes().get(0).getPathModel().getPaint()), new TextModel()));
                        pathLists.set(movedIndex-1, new CanvasItemModel(pathLists.get(movedIndex-1).getType(), ff));
                        textXMove = x - startXTxt;
                        textYMove = y - startYTxt;
                        ff = new ArrayList<ShapeModel>();
                        ff.add(new ShapeModel(new PathModel(), new TextModel(pathLists.get(movedIndex).getListOfShapes().get(0).getTextModel().getTitle(), pathLists.get(movedIndex).getListOfShapes().get(0).getTextModel().getPaint(), textXMove, textYMove)));
                        pathLists.set(movedIndex, new CanvasItemModel(pathLists.get(movedIndex).getType(), ff));
                    }
                } else if (isTextSelected) {
                    textXMove = x - startX;
                    textYMove = y - startY;
                    if (selectedtextModel != null) {
                        List<ShapeModel> ff = new ArrayList<ShapeModel>();
                        ff.add(new ShapeModel(new PathModel(), new TextModel(selectedtextModel.getTitle(), selectedtextModel.getPaint(), textXMove, textYMove)));
                        pathLists.set(movedIndex, new CanvasItemModel(canvasItemModelMoved.getType(), ff));
                    }

                }
                invalidate();
                break;
        }

    }

    private void onActionUp(MotionEvent event) {


        if (this.isDown) {
            this.startX = 0.0F;
            this.startY = 0.0F;
            this.isDown = false;
        }

        if (drawer == Drawer.ANGLE_DRAW) {

            if (isDrawingFirstLine) {
                x2 = event.getX();
                y2 = event.getY();
                isDrawingFirstLine = false;
                isDrawingSecondLine = true;
            } else if (isDrawingSecondLine) {
                x4 = event.getX();
                y4 = event.getY();
                isDrawingSecondLine = false;
                isDrawingFirstLine = true;
            }
            if (!lineDrawn) {
                lineDrawn = true;
                this.startX = event.getX();
                this.startY = event.getY();
            } else {
                lineDrawn = false;
                firstStartX = 0.0F;
                firstStartY = 0.0F;
                keyboardSelectX = x2 + 70;
                keyboardSelectY = y2 - 30;
                lastEvent = MotionEvent.obtain(
                        0, // down time in milliseconds
                        0, // event time in milliseconds
                        MotionEvent.ACTION_DOWN, // action type
                        x2 + 70, y2 - 30, // y-coordinate
                        0 // meta state
                );
                setUpText(text);
            }
            hideText = false;
            invalidate();
        }
        if (this.drawer == Drawer.MOVE_SELECT) {
//            if (selectedPath!=null){
//
//                List<ShapeModel>  ff=new ArrayList<ShapeModel>();
//                ff.add(new ShapeModel(new PathModel(selectedPath,movedPaint),new TextModel()));
//                pathLists.set(movedIndex,new CanvasItemModel(canvasItemModelMoved.getType(),ff));
//            }else if (isTextSelected){
//                List<ShapeModel>  ff=new ArrayList<ShapeModel>();
//                ff.add(new ShapeModel(new PathModel(),new TextModel(selectedtextModel.getTitle(),selectedtextModel.getPaint(),textX,textY)));
//                pathLists.set(movedIndex,new CanvasItemModel(canvasItemModelMoved.getType(),ff));
//
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (selectedPath != null) {
            Paint paint = new Paint();
            paint.setColor(Color.GRAY);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(20);

            Path path = selectedPath;
// add points to the path...

            RectF bounds = new RectF();
            path.computeBounds(bounds, true);

// add padding to the bounds
            float padding = 20; // in pixels
            bounds.left -= padding;
            bounds.top -= padding;
            bounds.right += padding;
            bounds.bottom += padding;

// create a DashPathEffect with the desired dot interval and offset
            float dotInterval = 10; // in pixels
            float dashOffset = 0;   // in pixels
            PathEffect dashEffect = new DashPathEffect(new float[]{dotInterval, dotInterval}, dashOffset);

// set the path effect and stroke width on the paint
            paint.setPathEffect(dashEffect);
            paint.setStrokeWidth(3);

// draw the rectangle and path
            canvas.drawRect(bounds, paint);
            canvas.drawPath(path, paint);

        }
        if (selectedtextModel != null) {
            Paint paint = new Paint();
            paint.setColor(Color.GRAY);
            paint.setTextSize(50);
            paint.setStyle(Style.STROKE);

            String text = selectedtextModel.getTitle();
            float x = textXMove; // left x-coordinate of the text
            float y = textYMove; // bottom y-coordinate of the text

// get the bounds of the text using the Paint object
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);

// add padding to the bounds
            float padding = 20; // in pixels
            bounds.left -= padding;
            bounds.top -= padding;
            bounds.right += padding;
            bounds.bottom += padding;

// create a DashPathEffect with the desired dot interval and offset
            float dotInterval = 10; // in pixels
            float dashOffset = 0;   // in pixels
            PathEffect dashEffect = new DashPathEffect(new float[]{dotInterval, dotInterval}, dashOffset);

// set the path effect and stroke width on the paint
            paint.setPathEffect(dashEffect);
            paint.setStrokeWidth(3);

// draw the rectangle around the text
            canvas.drawRect(bounds.left + x, y - bounds.bottom, bounds.right + x, y - bounds.top, paint);
            int textLength = text.length();
            float textSpacing = 30f;
            // Iterate through each character of the text
            for (int j = 0; j < textLength; j++) {
                // Get the current character
                String character = String.valueOf(text.charAt(j));

                // Create a path for the character
                Path characterPath = new Path();
                characterPath.moveTo(x + padding + j * textSpacing, y - padding);
                characterPath.lineTo(x + padding + j * textSpacing + textSpacing, y - padding);
                // Draw the character path
                canvas.drawTextOnPath(character, characterPath, 0f, 0f, paint);
            }

        }
        if (this.bitmap != null) {
            canvas.drawBitmap(this.bitmap, 0.0F, 0.0F, this.emptyPaint);
        }

        for (int i = 0; i < this.historyPointer; ++i) {
            if (this.pathLists.get(i).getType().equals("text")) {
                TextModel textModel = this.pathLists.get(i).getListOfShapes().get(0).getTextModel();
                // Draw the text as line paths with equal spacing
                String text = textModel.getTitle();
                int textLength = text.length();
                float textSpacing = 30f;
                // Iterate through each character of the text
                for (int j = 0; j < textLength; j++) {
                    // Get the current character
                    String character = String.valueOf(text.charAt(j));

                    // Create a path for the character
                    Path characterPath = new Path();
                    characterPath.moveTo(textModel.getX() + j * textSpacing, textModel.getY());
                    characterPath.lineTo(textModel.getX() + j * textSpacing + textSpacing, textModel.getY());
                    // Draw the character path
                    canvas.drawTextOnPath(character, characterPath, 0f, 0f, textModel.getPaint());
                }
            } else if (this.pathLists.get(i).getType().equals("path")) {

                Path path = this.pathLists.get(i).getListOfShapes().get(0).getPathModel().getPath();
                Paint paint = (Paint) this.pathLists.get(i).getListOfShapes().get(0).getPathModel().getPaint();
                canvas.drawPath(path, paint);
            }else if (this.pathLists.get(i).getType().equals("angle")) {

                Path path = this.pathLists.get(i).getListOfShapes().get(0).getPathModel().getPath();
                Paint paint = (Paint) this.pathLists.get(i).getListOfShapes().get(0).getPathModel().getPaint();
                canvas.drawPath(path, paint);
            }
        }
        if (hideText) {
            this.drawText(canvas);
        }
        this.canvas = canvas;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case 0:
                this.onActionDown(event);
                break;
            case 1:
                this.onActionUp(event);
                break;
            case 2:
                this.onActionMove(event);
        }


        this.invalidate();
        return true;
    }


    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Drawer getDrawer() {
        return this.drawer;
    }

    public void setDrawer(Drawer drawer) {
        this.drawer = drawer;
    }

    public boolean canUndo() {
        return this.historyPointer > 1;
    }

    public boolean canRedo() {
        return this.historyPointer < this.pathLists.size();
    }

    public boolean undo() {
        if (this.canUndo()) {
            --this.historyPointer;
            this.invalidate();
            return true;
        } else {
            return false;
        }
    }

    public boolean undoSpecial() {
        if (this.canUndo()) {
            if (selectedPath != null || selectedtextModel != null) {
                if (isMultiShapeSelected) {
                    --this.historyPointer;
                    --this.historyPointer;
                    --this.historyPointer;
                    try {
                        pathListsForSelectedDelete.add(pathLists.get(movedIndex));
                        pathListsForSelectedDelete.add(pathLists.get(movedIndex-1));
                        pathListsForSelectedDelete.add(pathLists.get(movedIndex-2));
                        pathLists.remove(movedIndex);
                        pathLists.remove(movedIndex-1);
                        pathLists.remove(movedIndex-2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    selectedPath = null;
                    isTextSelected = false;
                    selectedtextModel = null;
                    textXMove = 0.0f;
                    textYMove = 0.0f;
                    this.invalidate();
                }else{
                    --this.historyPointer;
                    try {
                        pathListsForSelectedDelete.add(pathLists.get(movedIndex));
                        pathLists.remove(movedIndex);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    selectedPath = null;
                    isTextSelected = false;
                    selectedtextModel = null;
                    textXMove = 0.0f;
                    textYMove = 0.0f;
                    this.invalidate();
                }

            }
            return true;
        } else {
            return false;
        }
    }

    public boolean redo() {
        if (this.canRedo()) {
            if (pathListsForSelectedDelete.size() != 0) {
                if (isMultiShapeSelected) {
                    pathLists.add(pathListsForSelectedDelete.get(pathListsForSelectedDelete.size() - 1));
                    pathLists.add(pathListsForSelectedDelete.get(pathListsForSelectedDelete.size() - 2));
                    pathLists.add(pathListsForSelectedDelete.get(pathListsForSelectedDelete.size() - 3));
                    pathListsForSelectedDelete.remove(pathListsForSelectedDelete.size() - 1);
                    pathListsForSelectedDelete.remove(pathListsForSelectedDelete.size() - 1);
                    pathListsForSelectedDelete.remove(pathListsForSelectedDelete.size() - 1);
                    ++this.historyPointer;
                    ++this.historyPointer;
                }else {
                    pathLists.add(pathListsForSelectedDelete.get(pathListsForSelectedDelete.size() - 1));
                    pathListsForSelectedDelete.remove(pathListsForSelectedDelete.size() - 1);
                }

            }
            ++this.historyPointer;
            this.invalidate();
            return true;
        } else {
            return false;
        }
    }

    public void clear() {
        Path path = new Path();
        path.moveTo(0.0F, 0.0F);
        path.addRect(0.0F, 0.0F, 1000.0F, 1000.0F, Direction.CCW);
        path.close();
        Paint paint = new Paint();
        paint.setColor(-1);
        paint.setStyle(Style.FILL);
        List<ShapeModel> shapeModelList = new ArrayList<>();
        shapeModelList.add(new ShapeModel(new PathModel(path, paint), new TextModel()));
        CanvasItemModel canvasItemModel = new CanvasItemModel("path", shapeModelList);
        if (this.historyPointer == this.pathLists.size()) {

            this.pathLists.add(canvasItemModel);
//            this.pathLists.add(path);
//            this.paintLists.add(paint);
            ++this.historyPointer;
        } else {

            this.pathLists.set(this.historyPointer, canvasItemModel);
//            this.paintLists.set(this.historyPointer, paint);
            ++this.historyPointer;
            int i = this.historyPointer;

            for (int size = this.pathLists.size(); i < size; ++i) {
                this.pathLists.remove(this.historyPointer);
            }
        }

        this.text = "";
        this.invalidate();
    }

    public int getBaseColor() {
        return this.baseColor;
    }

    public void setBaseColor(int color) {
        this.baseColor = color;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Style getPaintStyle() {
        return this.paintStyle;
    }

    public void setPaintStyle(Style style) {
        this.paintStyle = style;
    }

    public int getPaintStrokeColor() {
        return this.paintStrokeColor;
    }

    public void setPaintStrokeColor(int color) {
        this.paintStrokeColor = color;
    }

    public int getPaintFillColor() {
        return this.paintFillColor;
    }

    public void setPaintFillColor(int color) {
        this.paintFillColor = color;
    }

    public float getPaintStrokeWidth() {
        return this.paintStrokeWidth;
    }

    public void setPaintStrokeWidth(float width) {
        if (width >= 0.0F) {
            this.paintStrokeWidth = width;
        } else {
            this.paintStrokeWidth = 3.0F;
        }

    }

    public int getOpacity() {
        return this.opacity;
    }

    public void setOpacity(int opacity) {
        if (opacity >= 0 && opacity <= 255) {
            this.opacity = opacity;
        } else {
            this.opacity = 255;
        }

    }

    public float getBlur() {
        return this.blur;
    }

    public void setBlur(float blur) {
        if (blur >= 0.0F) {
            this.blur = blur;
        } else {
            this.blur = 0.0F;
        }

    }

    public Cap getLineCap() {
        return this.lineCap;
    }

    public void setLineCap(Cap cap) {
        this.lineCap = cap;
    }

    public PathEffect getDrawPathEffect() {
        return this.drawPathEffect;
    }

    public void setDrawPathEffect(PathEffect drawPathEffect) {
        this.drawPathEffect = drawPathEffect;
    }

    public float getFontSize() {
        return this.fontSize;
    }

    public void setFontSize(float size) {
        if (size >= 0.0F) {
            this.fontSize = size;
        } else {
            this.fontSize = 32.0F;
        }

    }

    public Typeface getFontFamily() {
        return this.fontFamily;
    }

    public void setFontFamily(Typeface face) {
        this.fontFamily = face;
    }

    public Bitmap getBitmap() {
        this.setDrawingCacheEnabled(false);
        this.setDrawingCacheEnabled(true);
        return Bitmap.createBitmap(this.getDrawingCache());
    }

    public Bitmap getScaleBitmap(int w, int h) {
        this.setDrawingCacheEnabled(false);
        this.setDrawingCacheEnabled(true);
        return Bitmap.createScaledBitmap(this.getDrawingCache(), w, h, true);
    }

    public void drawBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.invalidate();
    }

    public void drawBitmap(byte[] byteArray) {
        this.drawBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap, CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] getBitmapAsByteArray(CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.getBitmap().compress(format, quality, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] getBitmapAsByteArray() {
        return this.getBitmapAsByteArray(CompressFormat.PNG, 100);
    }

    public static enum Drawer {
        PEN,
        LINE,
        RECTANGLE,
        CIRCLE,
        ELLIPSE,
        QUADRATIC_BEZIER,
        QUBIC_BEZIER,
        ANGLE_DRAW,
        MOVE_SELECT,
        MOVE_DELETE;

        private Drawer() {
        }
    }

    public static enum Mode {
        DRAW,
        TEXT,
        ERASER, MOVE_SELECT, MOVE_DELETE;

        private Mode() {
        }
    }
}
