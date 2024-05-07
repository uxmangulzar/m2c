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

package com.fenris.motion2coach.view.activity.live;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.fenris.motion2coach.R;
import com.fenris.motion2coach.databinding.ActivityVisionLivePreviewBinding;
import com.fenris.motion2coach.model.PoseDataModel;
import com.fenris.motion2coach.view.activity.live.posedetector.PoseDetectorProcessor;
import com.fenris.motion2coach.view.activity.live.posedetector.PoseGraphic;
import com.fenris.motion2coach.view.activity.live.posedetector.segmenter.SegmenterProcessor;
import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@KeepName
public final class LivePreviewActivity extends AppCompatActivity
    implements OnItemSelectedListener, CompoundButton.OnCheckedChangeListener , PoseGraphic.MyCallback {
  private static final String OBJECT_DETECTION = "Object Detection";
  private static final String POSE_DETECTION = "Motion2Coach Live";
  private static final String SELFIE_SEGMENTATION = "Selfie Segmentation";
  private static final String TAG = "LivePreviewActivity";

  private CameraSource cameraSource = null;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;
  private String selectedModel = POSE_DETECTION;
  private ActivityVisionLivePreviewBinding viewBinding;
  private boolean camFacingFront=false;




  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");
    viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_vision_live_preview);
    Toast.makeText(
                    getApplicationContext(),
                    "Please wait few seconds to load data",
                    Toast.LENGTH_LONG)
            .show();

    preview = findViewById(R.id.preview_view);
    if (preview == null) {
      Log.d(TAG, "Preview is null");
    }
    graphicOverlay = findViewById(R.id.graphic_overlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }



    Spinner spinner = findViewById(R.id.spinner);

    List<String> options = new ArrayList<>();
    options.add(POSE_DETECTION);
    createCameraSource(selectedModel);


    viewBinding.switchCam.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        if (cameraSource != null) {
          if (camFacingFront) {
            cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            camFacingFront=false;
          } else {
            cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            camFacingFront=true;
          }
        }
        preview.stop();
        startCameraSource();
      }
    });



  }

  @Override
  public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    // An item was selected. You can retrieve the selected item using
    // parent.getItemAtPosition(pos)
    selectedModel = parent.getItemAtPosition(pos).toString();
    Log.d(TAG, "Selected model: " + selectedModel);
    preview.stop();
    createCameraSource(selectedModel);
    startCameraSource();
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing.
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Log.d(TAG, "Set facing");
    if (cameraSource != null) {
      if (isChecked) {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
      } else {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
      }
    }
    preview.stop();
    startCameraSource();
  }

  private void createCameraSource(String model) {
    // If there's no existing cameraSource, create one.
    if (cameraSource == null) {
      cameraSource = new CameraSource(this, graphicOverlay);
      cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);

    }

    try {
      switch (model) {


        case POSE_DETECTION:
          PoseDetectorOptionsBase poseDetectorOptions =
              PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
          Log.i(TAG, "Using Pose Detector with options " + poseDetectorOptions);
          boolean shouldShowInFrameLikelihood =
              PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
          boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
          boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
          boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);
          cameraSource.setMachineLearningFrameProcessor(
              new PoseDetectorProcessor(
                  this,
                  poseDetectorOptions,
                  shouldShowInFrameLikelihood,
                  visualizeZ,
                  rescaleZ,
                  runClassification,
                  /* isStreamMode = */ true
              ,this)
          );
          break;
        case SELFIE_SEGMENTATION:
          cameraSource.setMachineLearningFrameProcessor(new SegmenterProcessor(this));
          break;
        default:
          Log.e(TAG, "Unknown model: " + model);
      }
    } catch (RuntimeException e) {
      Log.e(TAG, "Can not create image processor: " + model, e);
      Toast.makeText(
              getApplicationContext(),
              "Can not create image processor: " + e.getMessage(),
              Toast.LENGTH_LONG)
          .show();
    }
  }

  /**
   * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() {
    if (cameraSource != null) {
      try {
        if (preview == null) {
          Log.d(TAG, "resume: Preview is null");
        }
        if (graphicOverlay == null) {

          Log.d(TAG, "resume: graphOverlay is null");
        }
        preview.start(cameraSource, graphicOverlay);
      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        cameraSource.release();
        cameraSource = null;
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    createCameraSource(selectedModel);
    startCameraSource();
  }

  /** Stops the camera. */
  @Override
  protected void onPause() {
    super.onPause();
    preview.stop();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
    }
  }

    public void onBackPressed(View view) {
        finish();
    }


  @Override
  public void updateMyText(PoseDataModel poseDataModel) {
    viewBinding.sTilt.setText(""+poseDataModel.getS_tilt());
    viewBinding.sBend.setText(""+poseDataModel.getS_bend());
    viewBinding.sTurn.setText(""+poseDataModel.getS_turn());
    viewBinding.pBend.setText(""+poseDataModel.getP_bend());
    viewBinding.pTilt.setText(""+poseDataModel.getP_tilt());
    viewBinding.pTurn.setText(""+poseDataModel.getP_turn());

  }
}
