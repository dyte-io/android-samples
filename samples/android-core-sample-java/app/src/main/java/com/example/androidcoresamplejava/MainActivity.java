package com.example.androidcoresamplejava;

import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import io.dyte.core.DyteMeetingBuilder;
import io.dyte.core.DyteMobileClient;
import io.dyte.core.listeners.DyteMeetingRoomEventsListener;
import io.dyte.core.models.ActiveTabType;
import io.dyte.core.models.DyteMeetingInfoV2;

public class MainActivity extends AppCompatActivity {
  private String TAG = "MainActivity";

  private Button btnInit, btnJoin, btnLeave;
  private ProgressBar pbLoader;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    btnInit = findViewById(R.id.btnInitMeeting);
    btnJoin = findViewById(R.id.btnJoinMeeting);
    btnLeave = findViewById(R.id.btnLeaveMeeting);
    pbLoader = findViewById(R.id.pbLoader);

    btnJoin.setVisibility(View.GONE);
    btnLeave.setVisibility(View.GONE);

    DyteMobileClient mobileClient = DyteMeetingBuilder.INSTANCE.build(this);
    DyteMeetingRoomEventsListener meetingEventListener = new DyteMeetingRoomEventsListener() {
      @Override public void onReconnectingToMeetingRoom() {

      }

      @Override public void onReconnectedToMeetingRoom() {

      }

      @Override public void onMeetingRoomReconnectionFailed() {

      }

      @Override public void onMeetingRoomDisconnected() {

      }

      @Override public void onMeetingRoomConnectionFailed() {

      }

      @Override public void onMeetingRoomLeaveStarted() {
        Log.v(TAG, "onMeetingRoomLeaveStarted");
      }

      @Override public void onMeetingRoomLeaveCompleted() {
        Log.v(TAG, "onMeetingRoomLeaveCompleted");
      }

      @Override public void onMeetingRoomJoinStarted() {
        Log.v(TAG, "onMeetingRoomJoinStarted");
        btnJoin.setEnabled(false);
        pbLoader.setVisibility(View.VISIBLE);
      }

      @Override public void onMeetingRoomJoinFailed(@NonNull Exception e) {
        Log.v(TAG, "onMeetingRoomJoinFailed");
      }

      @Override public void onMeetingRoomJoinCompleted() {
        Log.v(TAG, "onMeetingRoomJoinCompleted");
        pbLoader.setVisibility(View.GONE);
        btnJoin.setVisibility(View.GONE);
        btnLeave.setVisibility(View.VISIBLE);
      }

      @Override public void onMeetingInitStarted() {
        Log.v(TAG, "onMeetingInitStarted");
        btnInit.setEnabled(false);
        pbLoader.setVisibility(View.VISIBLE);
      }

      @Override public void onMeetingInitFailed(@NonNull Exception e) {
        Log.v(TAG, "onMeetingInitFailed " + e.getMessage());
      }

      @Override public void onMeetingInitCompleted() {
        Log.v(TAG, "onMeetingInitCompleted");
        pbLoader.setVisibility(View.GONE);
        btnInit.setVisibility(View.GONE);
        btnJoin.setVisibility(View.VISIBLE);
      }

      @Override public void onDisconnectedFromMeetingRoom() {

      }

      @Override public void onConnectingToMeetingRoom() {

      }

      @Override public void onConnectedToMeetingRoom() {

      }

      @Override
      public void onActiveTabUpdate(@NonNull String s, @NonNull ActiveTabType activeTabType) {

      }
    };

    mobileClient.addMeetingRoomEventsListener(meetingEventListener);

    btnInit.setOnClickListener(view -> {
      Log.v(TAG, "doing init meeting");
      DyteMeetingInfoV2 dyteMeetingInfoV2 = new DyteMeetingInfoV2(
          Constants.AUTH_TOKEN,
          true, true, Constants.BASE_URL);
      mobileClient.init(dyteMeetingInfoV2);
    });

    btnJoin.setOnClickListener(view -> {
      Log.v(TAG, "doing join meeting");
      mobileClient.joinRoom();
    });
  }
}