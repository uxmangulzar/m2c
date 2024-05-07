package com.fenris.motion2coach.view.activity;

import android.Manifest;
import android.app.ProgressDialog;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.fenris.motion2coach.R;
import com.google.android.material.snackbar.Snackbar;


import java.util.ArrayList;
import java.util.List;


public class DemoActivity extends AppCompatActivity {


    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private VideoView videoView;
    private Uri selectedVideoUri;
    private int stopPosition;
    private ScrollView mainlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        final TextView uploadVideo = findViewById(R.id.uploadVideo);
        TextView decreaseSpeed = findViewById(R.id.decreaseSpeed);
        videoView = findViewById(R.id.videoView);
        mainlayout = findViewById(R.id.mainlayout);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);

        uploadVideo.setOnClickListener(v -> getPermission());

        decreaseSpeed.setOnClickListener(v -> {
            if (selectedVideoUri != null) {
            } else
                Snackbar.make(mainlayout, "Please upload a video", 4000).show();
        });

    }

    private void getPermission() {
        String[] params = null;
        String writeExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;

        int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, writeExternalStorage);
        int hasReadExternalStoragePermission = ActivityCompat.checkSelfPermission(this, readExternalStorage);
        List<String> permissions = new ArrayList<>();

        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(writeExternalStorage);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(readExternalStorage);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[0]);
        }
        if (params != null && params.length > 0) {
            ActivityCompat.requestPermissions(DemoActivity.this,
                    params,
                    100);
        } else
            uploadVideo();
    }


    /**
     * Handling response for permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    uploadVideo();
                }
            }
            break;
            case 200: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }


        }
    }

    /**
     * Opening gallery for uploading video
     */
    private void uploadVideo() {
        try {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPosition = videoView.getCurrentPosition(); //stopPosition is an int
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.seekTo(stopPosition);
        videoView.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();
                videoView.setVideoURI(selectedVideoUri);
                videoView.start();


                videoView.setOnPreparedListener(mp -> {
                    // TODO Auto-generated method stub
//                    duration = mp.getDuration() / 1000;
//                    tvLeft.setText("00:00:00");
//
//                    tvRight.setText(getTime(mp.getDuration() / 1000));
//                    mp.setLooping(true);



                });

            }
        }
    }


}
