package com.example.androidcoresamplekotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import io.dyte.core.DyteAndroidClientBuilder
import io.dyte.core.listeners.DyteMeetingRoomEventsListener
import io.dyte.core.listeners.DyteParticipantEventsListener
import io.dyte.core.models.DyteJoinedMeetingParticipant
import io.dyte.core.models.DyteMeetingInfoV2

private const val TAG = "DyteMeeting"

class MainActivity : AppCompatActivity() {
  private lateinit var btnInitMeeting: Button
  private lateinit var btnJoinMeeting: Button
  private lateinit var btnLeaveMeeting: Button
  private lateinit var pbLoader: ProgressBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    btnInitMeeting = findViewById(R.id.btnInitMeeting)
    btnJoinMeeting = findViewById(R.id.btnJoinMeeting)
    btnLeaveMeeting = findViewById(R.id.btnLeaveMeeting)
    pbLoader = findViewById(R.id.pbLoader)

    btnInitMeeting.visibility = View.VISIBLE
    btnJoinMeeting.visibility = View.GONE
    btnLeaveMeeting.visibility = View.GONE
    pbLoader.visibility = View.GONE

    val meetingEventListener = object : DyteMeetingRoomEventsListener {
      override fun onMeetingInitStarted() {
        Log.v(TAG, "onMeetingInitStarted")
        btnInitMeeting.isEnabled = false
        pbLoader.visibility = View.VISIBLE
      }

      override fun onMeetingInitCompleted() {
        Log.v(TAG, "onMeetingInitCompleted")
        pbLoader.visibility = View.GONE
        btnInitMeeting.isEnabled = true
        btnInitMeeting.visibility = View.GONE
        btnJoinMeeting.visibility = View.VISIBLE
      }

      override fun onMeetingInitFailed(exception: Exception) {
        Log.v(TAG, "onMeetingInitFailed ${exception.message}")
      }

      override fun onMeetingRoomJoinStarted() {
        Log.v(TAG, "onMeetingRoomJoinStarted")
        btnJoinMeeting.isEnabled = false
        pbLoader.visibility = View.VISIBLE
      }

      override fun onMeetingRoomJoinCompleted() {
        Log.v(TAG, "onMeetingRoomJoinCompleted")
        pbLoader.visibility = View.GONE
        btnInitMeeting.isEnabled = true
        btnJoinMeeting.visibility = View.GONE
        btnLeaveMeeting.visibility = View.VISIBLE
      }

      override fun onMeetingRoomJoinFailed(exception: Exception) {
        Log.v(TAG, "onMeetingRoomJoinFailed ${exception.message}")
      }
    }

    val participantEventsListener = object : DyteParticipantEventsListener {
      override fun onActiveParticipantsChanged(active: List<DyteJoinedMeetingParticipant>) {
        super.onActiveParticipantsChanged(active)
        Log.v(TAG, "onActiveParticipantsChanged ${active.size}")
      }

      override fun onParticipantJoin(participant: DyteJoinedMeetingParticipant) {
        super.onParticipantJoin(participant)
        Log.v(TAG, "onParticipantJoin ${participant.name}")
      }

      override fun onParticipantLeave(participant: DyteJoinedMeetingParticipant) {
        super.onParticipantLeave(participant)
        Log.v(TAG, "onParticipantJoin ${participant.name}")
      }
    }

    val dyteMeetingInfo =
      DyteMeetingInfoV2(authToken = AUTH_TOKEN, enableAudio = false, enableVideo = false)
    val meeting = DyteAndroidClientBuilder.build(this)
    meeting.addMeetingRoomEventsListener(meetingEventListener)
    meeting.addParticipantEventsListener(participantEventsListener)

    val chat = meeting.chat
    val roomParticipants = meeting.participants
    val polls = meeting.polls
    val localUser = meeting.localUser

    btnInitMeeting.setOnClickListener {
      meeting.init(dyteMeetingInfo)
    }

    btnJoinMeeting.setOnClickListener {
      meeting.joinRoom()
    }

    btnLeaveMeeting.setOnClickListener {
      meeting.leaveRoom()
    }
  }
}