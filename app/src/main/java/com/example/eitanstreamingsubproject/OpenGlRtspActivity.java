/*
 * Copyright (C) 2023 pedroSG94.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.eitanstreamingsubproject;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
//import com.example.rttma.R;
import com.pedro.encoder.input.gl.SpriteGestureController;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtsp.RtspCamera1;
import com.pedro.rtplibrary.view.OpenGlView;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;

import java.io.File;

public class OpenGlRtspActivity extends AppCompatActivity implements ConnectCheckerRtsp, View.OnClickListener,
        SurfaceHolder.Callback, View.OnTouchListener {
  private RtspCamera1 rtspCamera1;
  private Button button;
  private Button bRecord;

  private Button switchCamera;
  private EditText etUrl;
  private String currentDateAndTime = "";
  private File folder;
  private OpenGlView openGlView;
  private SpriteGestureController spriteGestureController = new SpriteGestureController();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_open_gl_rtsp);
    openGlView = findViewById(R.id.surfaceView);
    button = findViewById(R.id.b_start_stop);
    button.setOnClickListener(this);
    bRecord = findViewById(R.id.b_record);
    bRecord.setOnClickListener(this);
    etUrl = findViewById(R.id.et_rtp_url);
    etUrl.setHint("RTSP");
    etUrl.setText("");
    switchCamera = findViewById(R.id.switch_camera);
    switchCamera.setOnClickListener(this);
    rtspCamera1 = new RtspCamera1(openGlView, this);
    openGlView.getHolder().addCallback(this);
    openGlView.setOnTouchListener(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // I commented this line because I don't need this menu
    // getMenuInflater().inflate(R.menu.gl_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    //Stop listener for image, text, and gif stream objects.
    spriteGestureController.stopListener();
    return false;
  }

  @Override
  public void onConnectionStartedRtsp(String rtspUrl) {}

  @Override
  public void onConnectionSuccessRtsp() {
    runOnUiThread(() -> Toast.makeText(OpenGlRtspActivity.this, "Connection success", Toast.LENGTH_SHORT).show());
  }

  @Override
  public void onConnectionFailedRtsp(String reason) {
    Log.e("RTTMA", reason);
    runOnUiThread(() -> {
      Toast.makeText(OpenGlRtspActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT).show();
      rtspCamera1.stopStream();
      button.setText("Start");
    });
  }

  @Override
  public void onNewBitrateRtsp(long bitrate) {}

  @Override
  public void onDisconnectRtsp() {
    runOnUiThread(() -> Toast.makeText(OpenGlRtspActivity.this, "Disconnected", Toast.LENGTH_SHORT).show());
  }

  @Override
  public void onAuthErrorRtsp() {
    runOnUiThread(() -> Toast.makeText(OpenGlRtspActivity.this, "Auth error", Toast.LENGTH_SHORT).show());
  }

  @Override
  public void onAuthSuccessRtsp() {
    runOnUiThread(() -> Toast.makeText(OpenGlRtspActivity.this, "Auth success", Toast.LENGTH_SHORT).show());
  }

  @Override
  public void onClick(View view) {
    //b_start_stop
    if(view == button)
    {
      if (!rtspCamera1.isStreaming()) {
        if (rtspCamera1.isRecording() || (rtspCamera1.prepareAudio() && rtspCamera1.prepareVideo())) {
          button.setText("Stop");
          rtspCamera1.startStream(etUrl.getText().toString());
        } else {
          Toast.makeText(OpenGlRtspActivity.this, "Error preparing stream, This device can't do it",
                  Toast.LENGTH_SHORT).show();
        }
      } else {
        button.setText("Start");
        rtspCamera1.stopStream();
      }
    }
    //switch camera
    else if(view == switchCamera){
      try {
        rtspCamera1.switchCamera();
      } catch (CameraOpenException e) {
        Toast.makeText(OpenGlRtspActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
      }
    }

  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {}

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    rtspCamera1.startPreview();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    if (rtspCamera1.isStreaming()) {
      rtspCamera1.stopStream();
      button.setText("Start");
    }
    rtspCamera1.stopPreview();
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent) {
    if (spriteGestureController.spriteTouched(view, motionEvent)) {
      spriteGestureController.moveSprite(view, motionEvent);
      spriteGestureController.scaleSprite(motionEvent);
      return true;
    }
    return false;
  }
}
