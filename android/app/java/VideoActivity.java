package com.nexpecto.univaq;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {
    private String videoPath;
    private MediaController mediaController;
    private int videoPosition;
    private static ProgressDialog progressDialog;

    VideoView myVideoView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_fullscreen);
        Intent i = getIntent();
        if(i != null){
            Bundle extras = i.getExtras();
            if (extras != null) {
                String videoUri = extras.getString("VIDEO_URL");
                if (videoUri != null) {
                    videoPath = videoUri;
                } else {
                    String resourceName = extras.getString("VIDEO_RESOURCE_NAME");
                    int resourceId =  getResources().getIdentifier(resourceName, "raw", getPackageName());
                    videoPath =  "android.resource://" + getPackageName() + "/" + resourceId;
                }
                videoPosition = extras.getInt("VIDEO_POSITION");
            }
            myVideoView = findViewById(R.id.videoView);
            progressDialog = ProgressDialog.show(VideoActivity.this, "", "Caricamento...", true);
            progressDialog.setCancelable(true);
            PlayVideo();
        }
        else{
            Toast.makeText(VideoActivity.this, "VideoURL not found", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void PlayVideo() {
        try {
//            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            // Set status bar color
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);

            mediaController = new MediaController(VideoActivity.this);
            mediaController.setAnchorView(myVideoView);

            Uri video = Uri.parse(videoPath);
            myVideoView.setVideoURI(video);
            myVideoView.setMediaController(mediaController);
            myVideoView.requestFocus();
            myVideoView.setKeepScreenOn(true);
            myVideoView.seekTo(videoPosition * 1000);
            mediaController.setPrevNextListeners(v -> {
                mediaController.show();
            }, v -> finish());
            myVideoView.setOnPreparedListener(mp -> {
                progressDialog.dismiss();
                myVideoView.start();
            });
            myVideoView.setOnCompletionListener(mp -> finishProgress(true));
            myVideoView.setOnTouchListener((v, event) -> {
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    if (mediaController.isShowing()) {
                        mediaController.hide();
                    } else {
                        mediaController.show();
                    }
                    return true;
                }
                return true;
            });


        } catch (Exception e) {
            progressDialog.dismiss();
            System.out.println("Video Play Error :" + e.toString());
            finishProgress();
        }

    }

    protected void finishProgress() {
        this.finishProgress(false);
    }

    // Called instead of finish() to always send back the progress.
    protected void finishProgress(Boolean isEnd) {
        Intent resultIntent = new Intent(Intent.ACTION_PICK);
        int position = myVideoView.getCurrentPosition();
        if (isEnd) {
            position = 0;
        }
        resultIntent.putExtra("VIDEO_POSITION", position);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    // Pass the progress back on the user pressing the back button.
    public void onBackPressed(){
        finishProgress();
    }

//    public void onClick(View v) {
//        switch(v.getId()) {
//            case R.id.closeBtn:
//                finishProgress();
//                break;
//        }
//    }
}
