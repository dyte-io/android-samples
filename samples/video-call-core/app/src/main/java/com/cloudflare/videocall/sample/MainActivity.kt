package com.cloudflare.videocall.sample

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.cloudflare.realtimekit.RealtimeKitClient
import com.cloudflare.realtimekit.RealtimeKitMeetingBuilder
import com.cloudflare.realtimekit.RtkMeetingParticipant
import com.cloudflare.realtimekit.RtkMeetingRoomEventListener
import com.cloudflare.realtimekit.errors.MeetingError
import com.cloudflare.realtimekit.meta.SocketConnectionState
import com.cloudflare.realtimekit.meta.SocketState
import com.cloudflare.realtimekit.models.RtkMeetingInfo
import com.cloudflare.realtimekit.participants.RtkParticipantUpdateListener
import com.cloudflare.realtimekit.participants.RtkParticipantsEventListener
import com.cloudflare.realtimekit.participants.RtkRemoteParticipant
import com.cloudflare.realtimekit.self.RtkSelfEventListener
import com.cloudflare.realtimekit.self.RtkSelfParticipant
import com.cloudflare.videocall.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private var _binding: ActivityMainBinding? = null
  private val binding get() = _binding!!

  private var meeting: RealtimeKitClient? = null
  private var remoteUser: RtkRemoteParticipant? = null

  private val meetingRoomEventsListener = object : RtkMeetingRoomEventListener {
    override fun onMeetingInitCompleted(meeting: RealtimeKitClient) {
      Log.d(TAG, "onMeetingInitCompleted")
    }

    override fun onMeetingInitFailed(error: MeetingError) {
      Log.d(TAG, "onMeetingInitFailed -> ${error.message}")
    }

    override fun onMeetingInitStarted() {
      Log.d(TAG, "onMeetingInitStarted")
    }

    override fun onMeetingRoomJoinCompleted(meeting: RealtimeKitClient) {
      Log.d(TAG, "onMeetingRoomJoinCompleted")
      showVideoCallUI()
    }

    override fun onMeetingRoomJoinFailed(error: MeetingError) {
      Log.d(TAG, "onMeetingRoomJoinFailed -> ${error.message}")
    }

    override fun onMeetingRoomJoinStarted() {
      Log.d(TAG, "onMeetingRoomJoinStarted")
    }

    override fun onMeetingEnded() {
      Log.d(TAG, "onMeetingEnded")/*
         * Note: calling meeting.release is currently compulsory after getting onMeetingEnded callback.
         * */
      meeting?.release(onSuccess = { finish() }, onFailure = { finish() })
    }

    override fun onSocketConnectionUpdate(newState: SocketConnectionState) {
      Log.d(TAG, "onSocketConnectionUpdate:$newState")
      if (newState.socketState == SocketState.CONNECTED && newState.reconnected) {
        onReconnectedToMeetingRoom()
      } else if (newState.socketState == SocketState.RECONNECTING && newState.reconnectionAttempt == 0) {
        onReconnectingToMeetingRoom()
      } else if (newState.isReconnectionFailure) {
        onMeetingRoomReconnectionFailed()
      }
    }

    private fun onMeetingRoomReconnectionFailed() {
      Log.d(TAG, "onMeetingRoomReconnectionFailed")
      Toast.makeText(
        applicationContext, "Reconnection failed, Please try again later", Toast.LENGTH_SHORT
      ).show()/*
         * Note: calling meeting.release is currently compulsory when closing the Activity after
         * reconnection failure.
         * */
      meeting?.release(onSuccess = { finish() }, onFailure = { finish() })
    }

    private fun onReconnectedToMeetingRoom() {
      Log.d(TAG, "onReconnectedToMeetingRoom")
      Toast.makeText(applicationContext, "Connection restored", Toast.LENGTH_SHORT).show()
    }

    private fun onReconnectingToMeetingRoom() {
      Log.d(TAG, "onReconnectingToMeetingRoom")
      Toast.makeText(
        applicationContext, "Connection lost. Trying to reconnect...", Toast.LENGTH_SHORT
      ).show()
      showProgressBar()
      disableCallControls()
    }
  }

  private val localUserEventsListener = object : RtkSelfEventListener {
    override fun onVideoUpdate(isEnabled: Boolean) {
      meeting?.localUser?.let { refreshLocalUserVideo(it) }
      binding.buttonCameraToggle.isEnabled = true
    }
  }

  private val remoteUserUpdateListener = object : RtkParticipantUpdateListener {
    override fun onVideoUpdate(participant: RtkMeetingParticipant, isEnabled: Boolean) {
      if (participant.id == remoteUser?.id) {
        refreshRemoteUserVideo(participant as RtkRemoteParticipant)
      }
    }
  }

  private val participantsEventListener = object : RtkParticipantsEventListener {
    override fun onParticipantJoin(participant: RtkRemoteParticipant) {
      Log.d(TAG, "onParticipantJoin, ${participant.name}")
    }

    override fun onParticipantLeave(participant: RtkRemoteParticipant) {
      Log.d(TAG, "onParticipantLeave, ${participant.name}")
    }

    override fun onActiveParticipantsChanged(active: List<RtkRemoteParticipant>) {
      Log.d(TAG, "onActiveParticipantsChanged, ${active.map { it.name }}")/*
         * Select the current remote user from active participants, ignoring any old instance
         * if the user reconnected after a network issue.
         * */
      val remoteUser = active.firstOrNull { it.id != this@MainActivity.remoteUser?.id } ?: run {
        Log.d(
          TAG,
          "onActiveParticipantsChanged, remote user is either already rendered or hasn't joined the call yet"
        )
        return
      }
      setUpRemoteUser(remoteUser)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setupUi()
    initMeetingClient()
  }

  override fun onDestroy() {
    remoteUser?.removeParticipantUpdateListener(remoteUserUpdateListener)
    meeting?.removeMeetingRoomEventListener(meetingRoomEventsListener)
    meeting?.removeSelfEventListener(localUserEventsListener)
    meeting?.removeParticipantsEventListener(participantsEventListener)
    _binding = null
    super.onDestroy()
  }

  private fun setupUi() {
    binding.buttonCameraToggle.isEnabled = false
    binding.buttonCameraToggle.setOnClickListener {
      meeting?.localUser?.let {
        onCameraToggleClicked(
          it
        )
      }
    }
    binding.buttonEndCall.isEnabled = false
    binding.buttonEndCall.setOnClickListener { meeting?.let { onEndCallClicked(it) } }
  }

  private fun initMeetingClient() {
    val meetingInfo = RtkMeetingInfo(authToken = AUTH_TOKEN, enableAudio = true, enableVideo = true)

    meeting = RealtimeKitMeetingBuilder.build(this)
    meeting?.let {
      it.addMeetingRoomEventListener(meetingRoomEventsListener)
      it.addSelfEventListener(localUserEventsListener)
      it.addParticipantsEventListener(participantsEventListener)
      it.init(meetingInfo, onSuccess = { it.joinRoom({}, {}) }) { finish() }
    }
  }

  private fun showVideoCallUI() {
    meeting?.localUser?.let { refreshLocalUserVideo(it) }
    hideProgressBar()
    binding.linearlayoutBottomBar.isVisible = true
    enableCallControls()
  }

  private fun setUpRemoteUser(remoteUser: RtkRemoteParticipant) {
    if (this.remoteUser?.id == remoteUser.id) {
      return
    }

    // removing listener from old remote user instance (if any)
    this@MainActivity.remoteUser?.removeParticipantUpdateListener(remoteUserUpdateListener)

    remoteUser.addParticipantUpdateListener(remoteUserUpdateListener)
    this@MainActivity.remoteUser = remoteUser
    refreshRemoteUserVideo(remoteUser)
  }

  private fun refreshRemoteUserVideo(remoteUser: RtkRemoteParticipant) {
    if (remoteUser.videoEnabled) {
      binding.framelayoutRemoteUserContainer.removeAllViews()
      val videoView = remoteUser.getVideoView()
      (videoView?.parent as? ViewGroup)?.removeView(videoView)
      binding.framelayoutRemoteUserContainer.addView(
        videoView, FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.WRAP_CONTENT,
          FrameLayout.LayoutParams.WRAP_CONTENT,
          Gravity.CENTER
        )
      )
      videoView?.renderVideo()
      binding.framelayoutRemoteUserContainer.isVisible = true
    } else {
      binding.framelayoutRemoteUserContainer.removeAllViews()
      binding.framelayoutRemoteUserContainer.isVisible = false
    }
  }

  private fun refreshLocalUserVideo(localUser: RtkSelfParticipant) {
    if (localUser.videoEnabled) {
      binding.framelayoutLocalUserContainer.removeAllViews()
      val videoView = localUser.getSelfPreview() ?: run {
        Log.d(TAG, "refreshLocalUserVideo, VideoView is null even when video is enabled")
        return
      }
      // Setting ZOrderMediaOverlay since local user's video is placed on top of remote user's video
      videoView.setZOrderMediaOverlay(true)
      (videoView.parent as? ViewGroup)?.removeView(videoView)
      binding.framelayoutLocalUserContainer.addView(
        videoView, FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.WRAP_CONTENT,
          FrameLayout.LayoutParams.WRAP_CONTENT,
          Gravity.CENTER
        )
      )
      videoView.renderVideo()
      binding.framelayoutLocalUserContainer.isVisible = true
    } else {
      binding.framelayoutLocalUserContainer.removeAllViews()
      binding.framelayoutLocalUserContainer.isVisible = false
    }
  }

  private fun onCameraToggleClicked(localUser: RtkSelfParticipant) {
    binding.buttonCameraToggle.isEnabled = false
    if (!localUser.videoEnabled) {
      localUser.enableVideo { error ->
        if (error != null) {
          Log.d(TAG, "enableVideo, ${error.message}")
          binding.buttonCameraToggle.isEnabled = true
        }
      }
    } else {
      localUser.disableVideo { error ->
        if (error != null) {
          Log.d(TAG, "disableVideo, ${error.message}")
          binding.buttonCameraToggle.isEnabled = true
        }
      }
    }
  }

  private fun onEndCallClicked(meeting: RealtimeKitClient) {
    binding.buttonEndCall.isEnabled = false
    val error = meeting.participants.kickAll()
    if (error != null) {
      Log.d(TAG, "endCall, ${error.message}")
      binding.buttonEndCall.isEnabled = true
    }
  }

  private fun enableCallControls() {
    binding.buttonCameraToggle.isEnabled = true
    binding.buttonEndCall.isEnabled = true
  }

  private fun disableCallControls() {
    binding.buttonCameraToggle.isEnabled = false
    binding.buttonEndCall.isEnabled = false
  }

  private fun showProgressBar() {
    binding.progressbarVideoCall.isVisible = true
  }

  private fun hideProgressBar() {
    binding.progressbarVideoCall.isVisible = false
  }

  companion object {
    private const val TAG = "MainActivity"
    private const val AUTH_TOKEN = "<paste_auth_token_here>"
  }
}
