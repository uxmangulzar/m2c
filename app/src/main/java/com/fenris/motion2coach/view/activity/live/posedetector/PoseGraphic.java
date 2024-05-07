/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fenris.motion2coach.view.activity.live.posedetector;

import static com.google.common.primitives.Floats.max;
import static com.google.common.primitives.Floats.min;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.fenris.motion2coach.model.PoseDataModel;
import com.fenris.motion2coach.view.activity.live.GraphicOverlay;
import com.google.common.primitives.Ints;
import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

/** Draw the detected pose in preview. */
public class PoseGraphic extends GraphicOverlay.Graphic {

  private static final float DOT_RADIUS = 20.0f;
  private static final float IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f;
  private static final float STROKE_WIDTH = 10;
  private static final float POSE_CLASSIFICATION_TEXT_SIZE = 90.0f;

  private PoseLandmark poseLandmark12;
  private final Pose pose;
  private final boolean showInFrameLikelihood;
  private final boolean visualizeZ;
  private final boolean rescaleZForVisualization;
  private float zMin = Float.MAX_VALUE;
  private float zMax = Float.MIN_VALUE;

  private final List<String> poseClassification;
  private final Paint classificationTextPaint;
  private final Paint leftPaint;
  private final Paint rightPaint;
  private final Paint whitePaint;
  private final Paint motion2coachPaint;
  private final Paint fenrisPaint;
  private final Paint newfenrisPaint;
  private final Paint ovalPaint;
  private final Paint fenrisText;
  MyCallback myCallback = null;
  Context context;



  PoseGraphic(
          GraphicOverlay overlay,
          Pose pose,
          boolean showInFrameLikelihood,
          boolean visualizeZ,
          boolean rescaleZForVisualization,
          List<String> poseClassification,
          MyCallback callback1,
          Context context) {
    super(overlay);
    this.pose = pose;
    this.context = context;
    myCallback = callback1;
    this.showInFrameLikelihood = showInFrameLikelihood;
    this.visualizeZ = visualizeZ;
    this.rescaleZForVisualization = rescaleZForVisualization;

    this.poseClassification = poseClassification;
    classificationTextPaint = new Paint();
    classificationTextPaint.setColor(Color.WHITE);
    classificationTextPaint.setTextSize(POSE_CLASSIFICATION_TEXT_SIZE);
    classificationTextPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK);

    whitePaint = new Paint();
    whitePaint.setStrokeWidth(STROKE_WIDTH);
    whitePaint.setColor(Color.WHITE);
    whitePaint.setTextSize(IN_FRAME_LIKELIHOOD_TEXT_SIZE);
    leftPaint = new Paint();
    leftPaint.setStrokeWidth(STROKE_WIDTH);
    leftPaint.setColor(Color.GREEN);
    rightPaint = new Paint();
    rightPaint.setStrokeWidth(STROKE_WIDTH);
    rightPaint.setColor(Color.YELLOW);


    // According to Motion2Coach
    motion2coachPaint = new Paint();
    motion2coachPaint.setStrokeWidth(STROKE_WIDTH);
    motion2coachPaint.setColor(Color.RED);

    // According to Motion2Coach
    fenrisPaint = new Paint();
    fenrisPaint.setStrokeWidth(STROKE_WIDTH);
    fenrisPaint.setColor(Color.BLUE);

    newfenrisPaint = new Paint();
    newfenrisPaint.setStrokeWidth(STROKE_WIDTH);
    newfenrisPaint.setColor(Color.BLACK);

    ovalPaint = new Paint();
    ovalPaint.setStrokeWidth(5);
    ovalPaint.setStyle(Paint.Style.STROKE);
    ovalPaint.setColor(Color.BLUE);

    // According to Motion2Coach
    fenrisText = new Paint();
    fenrisText.setStrokeWidth(30);
    fenrisText.setTextSize(50);
    fenrisText.setColor(Color.RED);


  }

  public interface MyCallback {
    // Declaration of the template function for the interface
    public void updateMyText(PoseDataModel poseDataModel);
  }

  @Override
  public void draw(Canvas canvas) {
    List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
    if (landmarks.isEmpty()) {
      return;
    }

    // Draw pose classification text.
    float classificationX = POSE_CLASSIFICATION_TEXT_SIZE * 0.5f;
    for (int i = 0; i < poseClassification.size(); i++) {
      float classificationY = (canvas.getHeight() - POSE_CLASSIFICATION_TEXT_SIZE * 1.5f
          * (poseClassification.size() - i));
      canvas.drawText(
          poseClassification.get(i),
          classificationX,
          classificationY,
              motion2coachPaint);
    }

//    // Draw all the points
//    for (PoseLandmark landmark : landmarks) {
//      drawPoint(canvas, landmark, fenrisPaint);
//      if (visualizeZ && rescaleZForVisualization) {
//        zMin = min(zMin, landmark.getPosition3D().getZ());
//        zMax = max(zMax, landmark.getPosition3D().getZ());
//      }
//    }

    PoseLandmark nose = pose.getPoseLandmark(PoseLandmark.NOSE);
    PoseLandmark lefyEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER);
    PoseLandmark lefyEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE);
    PoseLandmark leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER);
    PoseLandmark rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER);
    PoseLandmark rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE);
    PoseLandmark rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER);
    PoseLandmark leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR);
    PoseLandmark rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR);
    PoseLandmark leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH);
    PoseLandmark rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH);

    PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
    PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
    PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
    PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
    PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
    PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
    PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
    PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
    PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
    PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
    PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
    PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

    PoseLandmark leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY);
    PoseLandmark rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY);
    PoseLandmark leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX);
    PoseLandmark rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX);
    PoseLandmark leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB);
    PoseLandmark rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB);
    PoseLandmark leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL);
    PoseLandmark rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL);
    PoseLandmark leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
    PoseLandmark rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX);









//    // Face

    drawPoint(canvas, nose, fenrisPaint);
//    drawLine(canvas, nose, lefyEyeInner, whitePaint);
//    drawLine(canvas, lefyEyeInner, lefyEye, whitePaint);
//    drawLine(canvas, lefyEye, leftEyeOuter, whitePaint);
//    drawLine(canvas, leftEyeOuter, leftEar, whitePaint);
//    drawLine(canvas, nose, rightEyeInner, whitePaint);
//    drawLine(canvas, rightEyeInner, rightEye, whitePaint);
//    drawLine(canvas, rightEye, rightEyeOuter, whitePaint);
//    drawLine(canvas, rightEyeOuter, rightEar, whitePaint);
//    drawLine(canvas, leftMouth, rightMouth, whitePaint);
//
//    drawLine(canvas, leftShoulder, rightShoulder, whitePaint);
//    drawLine(canvas, leftHip, rightHip, whitePaint);
//
    // Left body

    drawPoint(canvas, leftShoulder, fenrisPaint);
    drawPoint(canvas, leftElbow, fenrisPaint);
    drawPoint(canvas, leftWrist, fenrisPaint);
    drawPoint(canvas, leftHip, fenrisPaint);
    drawPoint(canvas, leftAnkle, fenrisPaint);
    drawPoint(canvas, leftKnee, fenrisPaint);
    drawPoint(canvas, leftHeel, fenrisPaint);
    drawPoint(canvas, leftFootIndex, fenrisPaint);


    drawLine(canvas, leftShoulder, leftElbow, motion2coachPaint);
    drawLine(canvas, leftElbow, leftWrist, motion2coachPaint);
  //  drawLine(canvas, leftShoulder, leftHip, leftPaint);
    drawLine(canvas, leftHip, leftKnee, motion2coachPaint);
    drawLine(canvas, leftKnee, leftAnkle, motion2coachPaint);
   // drawLine(canvas, leftWrist, leftThumb, leftPaint);
    drawLine(canvas, leftWrist, leftPinky, motion2coachPaint);
    //drawLine(canvas, leftWrist, leftIndex, leftPaint);
    //drawLine(canvas, leftIndex, leftPinky, leftPaint);
    drawLine(canvas, leftAnkle, leftHeel, motion2coachPaint);
    drawLine(canvas, leftHeel, leftFootIndex, motion2coachPaint);
    drawLine(canvas, leftAnkle, leftFootIndex, motion2coachPaint);
//
    // Right body

    drawPoint(canvas, rightShoulder, fenrisPaint);
    drawPoint(canvas, rightElbow, fenrisPaint);
    drawPoint(canvas, rightWrist, fenrisPaint);
    drawPoint(canvas, rightHip, fenrisPaint);
    drawPoint(canvas, rightAnkle, fenrisPaint);
    drawPoint(canvas, rightKnee, fenrisPaint);
    drawPoint(canvas, rightHeel, fenrisPaint);
    drawPoint(canvas, rightFootIndex, fenrisPaint);

    drawLine(canvas, rightShoulder, rightElbow, motion2coachPaint);
    drawLine(canvas, rightElbow, rightWrist, motion2coachPaint);
    //drawLine(canvas, rightShoulder, rightHip, rightPaint);
    drawLine(canvas, rightHip, rightKnee, motion2coachPaint);
    drawLine(canvas, rightKnee, rightAnkle, motion2coachPaint);
    //drawLine(canvas, rightWrist, rightThumb, rightPaint);
    drawLine(canvas, rightWrist, rightPinky, motion2coachPaint);
    //drawLine(canvas, rightWrist, rightIndex, rightPaint);
    //drawLine(canvas, rightIndex, rightPinky, rightPaint);
     drawLine(canvas, rightAnkle, rightHeel, motion2coachPaint);
     drawLine(canvas, rightHeel, rightFootIndex, motion2coachPaint);
     drawLine(canvas, rightAnkle, rightFootIndex, motion2coachPaint);

    double pelvisTurn = Math.toDegrees((atan2(leftHip.getPosition3D().getX() - rightHip.getPosition3D().getX(), leftHip.getPosition3D().getZ() - rightHip.getPosition3D().getZ()) ));
    double shoulderTurn = Math.toDegrees((atan2(leftShoulder.getPosition3D().getX() - rightShoulder.getPosition3D().getX(), leftShoulder.getPosition3D().getZ() - rightShoulder.getPosition3D().getZ()) ));

    double pelvisTilt = Math.toDegrees((atan2(leftHip.getPosition3D().getX() - rightHip.getPosition3D().getX(), leftHip.getPosition3D().getY() - rightHip.getPosition3D().getY()) ));
    double shoulderTilt = Math.toDegrees((atan2(leftShoulder.getPosition3D().getX() - rightShoulder.getPosition3D().getX(), leftShoulder.getPosition3D().getY() - rightShoulder.getPosition3D().getY())));

    PoseDataModel poseDataModel = new PoseDataModel(Math.round(90 - shoulderTurn)+ "°", Math.round(90 - shoulderTilt) + "°",  "0°", Math.round(90 - pelvisTurn) + "°", Math.round(90 - pelvisTilt) + "°",   "0°"); myCallback.updateMyText(poseDataModel);
   PointF3D shoulderMidPoint = new PointF3D() {
     @Override
     public float getX() {
       return (leftShoulder.getPosition3D().getX() + rightShoulder.getPosition3D().getX())/2;
     }

     @Override
     public float getY() {
       return (leftShoulder.getPosition3D().getY() + rightShoulder.getPosition3D().getY())/2;
     }

     @Override
     public float getZ() {
       return (leftShoulder.getPosition3D().getZ() + rightShoulder.getPosition3D().getZ())/2;
     }
   };


    PointF3D hipMidPoint = new PointF3D() {
      @Override
      public float getX() {
        return (leftHip.getPosition3D().getX() + rightHip.getPosition3D().getX())/2;
      }

      @Override
      public float getY() {
        return (leftHip.getPosition3D().getY() + rightHip.getPosition3D().getY())/2;
      }

      @Override
      public float getZ() {
        return (leftHip.getPosition3D().getZ() + rightHip.getPosition3D().getZ())/2;
      }
    };
    PointF3D ankleMidPoint = new PointF3D() {
          @Override
          public float getX() {
            return (leftAnkle.getPosition3D().getX() + rightAnkle.getPosition3D().getX())/2;
          }

          @Override
          public float getY() {
            return (leftAnkle.getPosition3D().getY() + rightAnkle.getPosition3D().getY())/2;
          }

          @Override
          public float getZ() {
            return (leftAnkle.getPosition3D().getZ() + rightAnkle.getPosition3D().getZ())/2;
          }
        };


    drawPoint3DFenris(canvas, hipMidPoint, shoulderMidPoint, motion2coachPaint);;
    drawLineFenris(canvas, nose, shoulderMidPoint, motion2coachPaint);

//    drawArrow(motion2coachPaint,canvas,hipMidPoint.getX(),hipMidPoint.getY(),hipMidPoint.getX(),hipMidPoint.getY());
//    drawArrow(canvas,ankleMidPoint,hipMidPoint,-1.0,motion2coachPaint);

    //Blue Lines
    //drawLineFenrisBlue(canvas, shoulderMidPoint, 250, fenrisPaint);
    //drawLineFenrisBlue(canvas, hipMidPoint, 250, fenrisPaint);


    drawOval(canvas, leftHip.getPosition3D(), rightHip.getPosition3D(), ovalPaint);
    drawOvalAndArrow(canvas,rightHip.getPosition3D());
    //According to Motion2Coach
    drawLine(canvas, rightShoulder, leftShoulder, motion2coachPaint);
//    drawLinePoints(canvas, ankleMidPoint, hipMidPoint, newfenrisPaint);
//    drawArrowHead(canvas, ankleMidPoint, hipMidPoint);


    drawLine(canvas, rightHip, leftHip, motion2coachPaint);


    // Draw inFrameLikelihood for all points
//    if (showInFrameLikelihood) {
//      for (PoseLandmark landmark : landmarks) {
//        canvas.drawText(
//            String.format(Locale.US, "%.2f", landmark.getInFrameLikelihood()),
//            translateX(landmark.getPosition().x),
//            translateY(landmark.getPosition().y),
//                motion2coachPaint);
//      }
//    }
  }

    void drawPoint(Canvas canvas, PoseLandmark landmark, Paint paint) {
    PointF3D point = landmark.getPosition3D();
    maybeUpdatePaintColor(paint, canvas, point.getZ());
    canvas.drawCircle(translateX(point.getX()), translateY(point.getY()), DOT_RADIUS, paint);
  }

  public void drawCurvedArrow(Canvas canvas, Float x,Float y, PoseLandmark endLandmark, Paint paint) {


    PointF3D end = endLandmark.getPosition3D();

    final Path path = new Path();
    int midX            = (int) (x + ((end.getX() - x) / 2));
    int midY            = (int) (y + ((end.getY() - y) / 2));
    float xDiff         = midX - x;
    float yDiff         = midY - y;
    double angle        = (atan2(yDiff, xDiff) * (180 / PI)) - 90;
    double angleRadians = Math.toRadians(angle);
    float pointX        = (float) (midX + 10 * cos(angleRadians));
    float pointY        = (float) (midY + 10 * sin(angleRadians));

    path.moveTo(x, y);
    path.cubicTo(x,y,pointX, pointY, end.getX(), end.getY());
    canvas.drawPath(path, paint);


  }

  void drawLine(Canvas canvas, PoseLandmark startLandmark, PoseLandmark endLandmark, Paint paint) {
    PointF3D start = startLandmark.getPosition3D();
    PointF3D end = endLandmark.getPosition3D();

    // Gets average z for the current body line
    float avgZInImagePixel = (start.getZ() + end.getZ()) / 2;
    maybeUpdatePaintColor(paint, canvas, avgZInImagePixel);

    canvas.drawLine(
          translateX(start.getX()),
          translateY(start.getY()),
          translateX(end.getX()),
          translateY(end.getY()),
          paint);
  }
  private void drawArrowHead(Canvas canvas, PointF3D tip, PointF3D tail)
  {
    double dy = tip.getY() - tail.getY();
    double dx = tip.getX() - tail.getX();
    double theta = Math.atan2(dy, dx);
    int tempX = (int) tip.getX(),tempY = (int) tip.getY();
    //make arrow touch the circle
    if(tip.getX()>tail.getX() && tip.getY()==tail.getY())
    {
      tempX = (int) (tip.getX()-10);
    }
    else if(tip.getX()<tail.getX() && tip.getY()==tail.getY())
    {
      tempX = (int) (tip.getX()+10);
    }
    else if(tip.getY()>tail.getY() && tip.getX()==tail.getX())
    {
      tempY = (int) (tip.getY()-10);
    }
    else if(tip.getY()<tail.getY() && tip.getX()==tail.getX())
    {
      tempY = (int) (tip.getY()+10);
    }
    else if(tip.getX()>tail.getX() || tip.getX()<tail.getX())
    {
      int rCosTheta = (int) ((10)*Math.cos(theta)) ;
      int xx = (int) (tip.getX() - rCosTheta);
      int yy = (int) ((xx-tip.getX())*(dy/dx) + tip.getY());
      tempX = xx;
      tempY = yy;
    }


    double x, y, rho = theta + Math.PI;
    for(int j = 0; j < 2; j++)
    {
      x = tempX - 10 * Math.cos(rho);
      y = tempY - 10  * Math.sin(rho);

      canvas.drawLine(tempX,tempY,(int)x,(int)y,this.newfenrisPaint);
      rho = theta - Math.PI;
    }
  }
  void drawLinePoints(Canvas canvas, PointF3D start,  PointF3D end, Paint paint) {


    // Gets average z for the current body line
    float avgZInImagePixel = (start.getZ() + end.getZ()) / 2;
    maybeUpdatePaintColor(paint, canvas, avgZInImagePixel);

    canvas.drawLine(
          translateX(start.getX()),
          translateY(start.getY()),
          translateX(end.getX()),
          translateY(end.getY()+10),
          paint);
  }


  void drawOval(Canvas canvas, PointF3D start, PointF3D end, Paint paint) {


    // Gets average z for the current body line
    float avgZInImagePixel = (start.getZ() + end.getZ()) / 2;
    maybeUpdatePaintColor(paint, canvas, avgZInImagePixel);

    float radius = 20;
    final RectF oval = new RectF();
    oval.set(translateX(start.getX() + 6),
            translateY(start.getY() + 8),
            translateX(end.getX() - 6),
            translateY(end.getY()) -78);
    Path myPath = new Path();
    myPath.arcTo(oval, 0, -(float) 90, true);
    canvas.drawOval(
            oval,
            paint);
  }
  private void drawOvalAndArrow(Canvas canvas, PointF3D start){


    Paint circlePaint = new Paint();
    circlePaint.setStyle(Paint.Style.STROKE);
    circlePaint.setAntiAlias(true);
    circlePaint.setStrokeWidth(2);
    circlePaint.setColor(Color.CYAN);

    float centerWidth =translateX(start.getX()); //get center x of display
    float centerHeight = translateY(start.getY()-13); //get center y of display
    float circleRadius = 20; //set radius
    float circleDistance = 30; //set distance between both circles

    //draw circles
    canvas.drawCircle(centerWidth, centerHeight, circleRadius, circlePaint);


    //to draw an arrow, just lines needed, so style is only STROKE
    circlePaint.setStyle(Paint.Style.STROKE);
    circlePaint.setColor(Color.CYAN);

    //create a path to draw on
    Path arrowPath = new Path();

    //create an invisible oval. the oval is for "behind the scenes" ,to set the path´
    //area. Imagine this is an egg behind your circles. the circles are in the middle of this egg
    final RectF arrowOval = new RectF();
    arrowOval.set(centerWidth,
            centerHeight-80,
            centerWidth + circleDistance,
            centerHeight+80);

    //add the oval to path
    arrowPath.addArc(arrowOval,-180,-90);

    //draw path on canvas


    //draw arrowhead on path start

    //same as above on path end

    //draw the path
    canvas.drawPath(arrowPath,circlePaint);

  }

  void drawPoint3DFenris(Canvas canvas, PointF3D startLandmark, PointF3D endLandmark, Paint paint) {
    PointF3D start = startLandmark;
    PointF3D end = endLandmark;

    // Gets average z for the current body line
    float avgZInImagePixel = (start.getZ() + end.getZ()) / 2;
    maybeUpdatePaintColor(paint, canvas, avgZInImagePixel);

    canvas.drawLine(
            translateX(start.getX()),
            translateY(start.getY()),
            translateX(end.getX()),
            translateY(end.getY()),
            paint);
  }
  private void drawArrow(Paint paint, Canvas canvas, float from_x, float from_y, float to_x, float to_y)
  {
    float angle,anglerad, radius, lineangle;

    //values to change for other appearance *CHANGE THESE FOR OTHER SIZE ARROWHEADS*
    radius=20;
    angle=15f;

    //some angle calculations
    anglerad= (float) (PI*angle/180.0f);
    lineangle= (float) (atan2(to_y-from_y,to_x-from_x));

    //tha line
    canvas.drawLine(from_x,from_y,to_x,to_y,paint);

    //tha triangle
    Path path = new Path();
    path.setFillType(Path.FillType.EVEN_ODD);
    path.moveTo(to_x, to_y);
    path.lineTo((float)(to_x-radius*cos(lineangle - (anglerad / 2.0))),
            (float)(to_y-radius*sin(lineangle - (anglerad / 2.0))));
    path.lineTo((float)(to_x-radius*cos(lineangle + (anglerad / 2.0))),
            (float)(to_y-radius*sin(lineangle + (anglerad / 2.0))));
    path.close();

    canvas.drawPath(path, paint);
  }
  void drawLineFenris(Canvas canvas, PoseLandmark startLandmark, PointF3D endLandmark, Paint paint) {
    PointF3D start = startLandmark.getPosition3D();
    PointF3D end = endLandmark;

    // Gets average z for the current body line
    float avgZInImagePixel = (start.getZ() + end.getZ()) / 2;
    maybeUpdatePaintColor(paint, canvas, avgZInImagePixel);

    canvas.drawLine(
            translateX(start.getX()),
            translateY(start.getY()),
            translateX(end.getX()),
            translateY(end.getY()),
            paint);
  }



  void drawLineFenrisBlue(Canvas canvas, PointF3D startLandmark, float translation, Paint paint) {
    PointF3D start = startLandmark;
    float end = translation;

    // Gets average z for the current body line
    float avgZInImagePixel = (start.getZ() + start.getZ()) / 2;
    maybeUpdatePaintColor(paint, canvas, avgZInImagePixel);

    canvas.drawLine(
            translateX(start.getX() - end),
            translateY(start.getY()),
            translateX(start.getX() + end ),
            translateY(start.getY()),
            paint);
  }



  private void maybeUpdatePaintColor(Paint paint, Canvas canvas, float zInImagePixel) {
    if (!visualizeZ) {
      return;
    }

    // When visualizeZ is true, sets up the paint to different colors based on z values.
    // Gets the range of z value.
    float zLowerBoundInScreenPixel;
    float zUpperBoundInScreenPixel;

    if (rescaleZForVisualization) {
      zLowerBoundInScreenPixel = min(-0.001f, scale(zMin));
      zUpperBoundInScreenPixel = max(0.001f, scale(zMax));
    } else {
      // By default, assume the range of z value in screen pixel is [-canvasWidth, canvasWidth].
      float defaultRangeFactor = 1f;
      zLowerBoundInScreenPixel = -defaultRangeFactor * canvas.getWidth();
      zUpperBoundInScreenPixel = defaultRangeFactor * canvas.getWidth();
    }

    float zInScreenPixel = scale(zInImagePixel);

    if (zInScreenPixel < 0) {
      // Sets up the paint to draw the body line in red if it is in front of the z origin.
      // Maps values within [zLowerBoundInScreenPixel, 0) to [255, 0) and use it to control the
      // color. The larger the value is, the more red it will be.
      int v = (int) (zInScreenPixel / zLowerBoundInScreenPixel * 255);
      v = Ints.constrainToRange(v, 0, 255);
    //  paint.setARGB(255, 255, 255 - v, 255 - v);
    } else {
      // Sets up the paint to draw the body line in blue if it is behind the z origin.
      // Maps values within [0, zUpperBoundInScreenPixel] to [0, 255] and use it to control the
      // color. The larger the value is, the more blue it will be.
      int v = (int) (zInScreenPixel / zUpperBoundInScreenPixel * 255);
      v = Ints.constrainToRange(v, 0, 255);
     // paint.setARGB(255, 255 - v, 255 - v, 255);
    }
  }





}
