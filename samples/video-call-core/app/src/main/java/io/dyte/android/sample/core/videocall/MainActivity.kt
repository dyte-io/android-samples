package io.dyte.android.sample.core.videocall

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import io.dyte.core.DyteMeetingBuilder
import io.dyte.core.DyteMobileClient
import io.dyte.core.listeners.DyteMeetingRoomEventsListener
import io.dyte.core.listeners.DyteParticipantEventsListener
import io.dyte.core.listeners.DyteParticipantUpdateListener
import io.dyte.core.listeners.DyteSelfEventsListener
import io.dyte.core.models.DyteJoinedMeetingParticipant
import io.dyte.core.models.DyteMeetingInfoV2
import io.dyte.core.models.DyteSelfParticipant

class MainActivity : AppCompatActivity() {
    private lateinit var localUserVideoContainer: FrameLayout
    private lateinit var remoteUserVideoContainer: FrameLayout
    private lateinit var callBottomBar: LinearLayout
    private lateinit var cameraToggleButton: Button
    private lateinit var endCallButton: Button
    private lateinit var progressBar: ProgressBar

    private var meeting: DyteMobileClient? = null
    private var remoteUser: DyteJoinedMeetingParticipant? = null

    private val meetingRoomEventsListener = object : DyteMeetingRoomEventsListener {
        override fun onMeetingInitCompleted() {
            Log.d(TAG, "onMeetingInitCompleted")
        }

        override fun onMeetingInitFailed(exception: Exception) {
            Log.d(TAG, "onMeetingInitFailed -> ${exception.message}")
        }

        override fun onMeetingInitStarted() {
            Log.d(TAG, "onMeetingInitStarted")
        }

        override fun onMeetingRoomJoinCompleted() {
            Log.d(TAG, "onMeetingRoomJoinCompleted")
            showVideoCallUI()
        }

        override fun onMeetingRoomJoinFailed(exception: Exception) {
            Log.d(TAG, "onMeetingRoomJoinFailed -> ${exception.message}")
        }

        override fun onMeetingRoomJoinStarted() {
            Log.d(TAG, "onMeetingRoomJoinStarted")
        }

        override fun onMeetingEnded() {
            Log.d(TAG, "onMeetingEnded")
            /*
            * Note: calling meeting.release is currently compulsory after getting onMeetingEnded callback.
            * */
            meeting?.release(
                onReleaseSuccess = { finish() },
                onReleaseFailed = { finish() }
            )
        }
    }

    private val localUserEventsListener = object : DyteSelfEventsListener {
        override fun onVideoUpdate(videoEnabled: Boolean) {
            meeting?.localUser?.let {
                refreshLocalUserVideo(it)
            }
            cameraToggleButton.isEnabled = true
        }
    }

    private val remoteUserUpdateListener = object : DyteParticipantUpdateListener {
        override fun onVideoUpdate(isEnabled: Boolean) {
            remoteUser?.let {
                refreshRemoteUserVideo(it)
            }
        }
    }

    private val participantEventsListener = object : DyteParticipantEventsListener {
        override fun onParticipantJoin(participant: DyteJoinedMeetingParticipant) {
            // Ignoring onParticipantJoin callback for localUser
            if (participant.id == meeting?.localUser?.id) {
                return
            }

            participant.addParticipantUpdateListener(remoteUserUpdateListener)
            remoteUser = participant
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initChildViews()
        initDyteClient()
    }

    override fun onDestroy() {
        remoteUser?.removeParticipantUpdateListener(remoteUserUpdateListener)
        meeting?.removeMeetingRoomEventsListener(meetingRoomEventsListener)
        meeting?.removeSelfEventsListener(localUserEventsListener)
        meeting?.removeParticipantEventsListener(participantEventsListener)
        super.onDestroy()
    }

    private fun initChildViews() {
        localUserVideoContainer = findViewById(R.id.framelayout_local_user_container)
        remoteUserVideoContainer = findViewById(R.id.framelayout_remote_user_container)
        progressBar = findViewById(R.id.progressbar_video_call)
        callBottomBar = findViewById(R.id.linearlayout_bottom_bar)

        cameraToggleButton = findViewById(R.id.button_camera_toggle)
        cameraToggleButton.isEnabled = false
        cameraToggleButton.setOnClickListener {
            meeting?.localUser?.let {
                onCameraToggleClicked(it)
            }
        }

        endCallButton = findViewById(R.id.button_end_call)
        endCallButton.isEnabled = false
        endCallButton.setOnClickListener {
            meeting?.let {
                onEndCallClicked(it)
            }
        }
    }


    private fun initDyteClient() {
        val meetingInfo = DyteMeetingInfoV2(
            authToken = AUTH_TOKEN,
            enableAudio = true,
            enableVideo = true
        )

        meeting = DyteMeetingBuilder.build(this)
        meeting?.addMeetingRoomEventsListener(meetingRoomEventsListener)
        meeting?.addSelfEventsListener(localUserEventsListener)
        meeting?.addParticipantEventsListener(participantEventsListener)
        meeting?.init(
            dyteMeetingInfo = meetingInfo,
            onInitSuccess = { meeting?.joinRoom() },
            onInitFailure = { error -> finish() }
        )
    }

    private fun showVideoCallUI() {
        meeting?.localUser?.let {
            refreshLocalUserVideo(it)
        }
        progressBar.isVisible = false
        callBottomBar.isVisible = true
        cameraToggleButton.isEnabled = true
        endCallButton.isEnabled = true
    }

    private fun refreshRemoteUserVideo(remoteUser: DyteJoinedMeetingParticipant) {
        if (remoteUser.videoEnabled) {
            remoteUserVideoContainer.removeAllViews()
            val videoView = remoteUser.getVideoView()
            (videoView?.parent as? ViewGroup)?.removeView(videoView)
            remoteUserVideoContainer.addView(
                videoView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
            videoView?.renderVideo()
            remoteUserVideoContainer.isVisible = true
        } else {
            remoteUserVideoContainer.removeAllViews()
            remoteUserVideoContainer.isVisible = false
        }
    }

    private fun refreshLocalUserVideo(localUser: DyteSelfParticipant) {
        if (localUser.videoEnabled) {
            localUserVideoContainer.removeAllViews()
            val videoView = localUser.getSelfPreview()
            // Setting ZOrderMediaOverlay since local user's video is placed on top of remote user's video
            videoView.setZOrderMediaOverlay(true)
            (videoView.parent as? ViewGroup)?.removeView(videoView)
            localUserVideoContainer.addView(
                videoView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
            videoView.renderVideo()
            localUserVideoContainer.isVisible = true
        } else {
            localUserVideoContainer.removeAllViews()
            localUserVideoContainer.isVisible = false
        }
    }

    private fun onCameraToggleClicked(localUser: DyteSelfParticipant) {
        cameraToggleButton.isEnabled = false
        if (!localUser.videoEnabled) {
            localUser.enableVideo()
        } else {
            localUser.disableVideo()
        }
    }

    private fun onEndCallClicked(meeting: DyteMobileClient) {
        endCallButton.isEnabled = false
        meeting.participants.kickAll()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val AUTH_TOKEN = "<paste_auth_token_here>"
    }
}