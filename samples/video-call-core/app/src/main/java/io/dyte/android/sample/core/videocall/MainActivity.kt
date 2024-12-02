package io.dyte.android.sample.core.videocall

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
import io.dyte.core.DyteMeetingBuilder
import io.dyte.core.DyteMobileClient
import io.dyte.core.errors.MeetingError
import io.dyte.core.listeners.DyteMeetingRoomEventsListener
import io.dyte.core.listeners.DyteParticipantUpdateListener
import io.dyte.core.listeners.DyteParticipantsEventListener
import io.dyte.core.listeners.DyteSelfEventsListener
import io.dyte.core.meta.SocketConnectionState
import io.dyte.core.meta.SocketState
import io.dyte.core.models.DyteMeetingInfoV2
import io.dyte.core.models.DyteSelfParticipant
import io.dyte.core.participants.DyteMeetingParticipant
import io.dyte.core.participants.DyteRemoteParticipant

class MainActivity : AppCompatActivity() {
    private lateinit var localUserVideoContainer: FrameLayout
    private lateinit var remoteUserVideoContainer: FrameLayout
    private lateinit var callBottomBar: LinearLayout
    private lateinit var cameraToggleButton: Button
    private lateinit var endCallButton: Button
    private lateinit var progressBar: ProgressBar

    private var meeting: DyteMobileClient? = null
    private var remoteUser: DyteRemoteParticipant? = null

    private val meetingRoomEventsListener = object : DyteMeetingRoomEventsListener {
        override fun onMeetingInitCompleted(meeting: DyteMobileClient) {
            Log.d(TAG, "onMeetingInitCompleted")
        }

        override fun onMeetingInitFailed(error: MeetingError) {
            Log.d(TAG, "onMeetingInitFailed -> ${error.message}")
        }

        override fun onMeetingInitStarted() {
            Log.d(TAG, "onMeetingInitStarted")
        }

        override fun onMeetingRoomJoinCompleted(meeting: DyteMobileClient) {
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
            Log.d(TAG, "onMeetingEnded")
            /*
            * Note: calling meeting.release is currently compulsory after getting onMeetingEnded callback.
            * */
            meeting?.release(
                onReleaseSuccess = { finish() },
                onReleaseFailed = { finish() }
            )
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
                applicationContext,
                "Reconnection failed, Please try again later",
                Toast.LENGTH_SHORT
            ).show()
            /*
            * Note: calling meeting.release is currently compulsory when closing the Activity after
            * reconnection failure.
            * */
            meeting?.release(
                onReleaseSuccess = { finish() },
                onReleaseFailed = { finish() }
            )
        }

        private fun onReconnectedToMeetingRoom() {
            Log.d(TAG, "onReconnectedToMeetingRoom")
            Toast.makeText(
                applicationContext,
                "Connection restored",
                Toast.LENGTH_SHORT
            ).show()
        }

        private fun onReconnectingToMeetingRoom() {
            Log.d(TAG, "onReconnectingToMeetingRoom")
            Toast.makeText(
                applicationContext,
                "Connection lost. Trying to reconnect...",
                Toast.LENGTH_SHORT
            ).show()
            showProgressBar()
            disableCallControls()
        }
    }

    private val localUserEventsListener = object : DyteSelfEventsListener {
        override fun onVideoUpdate(isEnabled: Boolean) {
            meeting?.localUser?.let {
                refreshLocalUserVideo(it)
            }
            cameraToggleButton.isEnabled = true
        }
    }

    private val remoteUserUpdateListener = object : DyteParticipantUpdateListener {
        override fun onVideoUpdate(participant: DyteMeetingParticipant, isEnabled: Boolean) {
            if (participant.id == remoteUser?.id) {
                refreshRemoteUserVideo(participant as DyteRemoteParticipant)
            }
        }
    }

    private val participantsEventListener = object : DyteParticipantsEventListener {
        override fun onParticipantJoin(participant: DyteRemoteParticipant) {
            Log.d(TAG, "onParticipantJoin, ${participant.name}")
        }

        override fun onParticipantLeave(participant: DyteRemoteParticipant) {
            Log.d(TAG, "onParticipantLeave, ${participant.name}")
        }

        override fun onActiveParticipantsChanged(active: List<DyteRemoteParticipant>) {
            Log.d(TAG, "onActiveParticipantsChanged, ${active.map { it.name }}")
            /*
            * Select the current remote user from active participants, ignoring any old instance
            * if the user reconnected after a network issue.
            * */
            val remoteUser =
                active.firstOrNull { it.id != this@MainActivity.remoteUser?.id }
                    ?: kotlin.run {
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
        setContentView(R.layout.activity_main)
        initChildViews()
        initDyteClient()
    }

    override fun onDestroy() {
        remoteUser?.removeParticipantUpdateListener(remoteUserUpdateListener)
        meeting?.removeMeetingRoomEventsListener(meetingRoomEventsListener)
        meeting?.removeSelfEventsListener(localUserEventsListener)
        meeting?.removeParticipantsEventListener(participantsEventListener)
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
        meeting?.addParticipantsEventListener(participantsEventListener)
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
        hideProgressBar()
        callBottomBar.isVisible = true
        enableCallControls()
    }

    private fun setUpRemoteUser(remoteUser: DyteRemoteParticipant) {
        if (this.remoteUser?.id == remoteUser.id) {
            return
        }

        // removing listener from old remote user instance (if any)
        this@MainActivity.remoteUser?.removeParticipantUpdateListener(remoteUserUpdateListener)

        remoteUser.addParticipantUpdateListener(remoteUserUpdateListener)
        this@MainActivity.remoteUser = remoteUser
        refreshRemoteUserVideo(remoteUser)
    }

    private fun refreshRemoteUserVideo(remoteUser: DyteRemoteParticipant) {
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
            val videoView = localUser.getSelfPreview() ?: kotlin.run {
                Log.d(TAG, "refreshLocalUserVideo, VideoView is null even when video is enabled")
                return
            }
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
            localUser.enableVideo { error ->
                if (error != null) {
                    Log.d(TAG, "enableVideo, ${error.message}")
                    cameraToggleButton.isEnabled = true
                }
            }
        } else {
            localUser.disableVideo { error ->
                if (error != null) {
                    Log.d(TAG, "disableVideo, ${error.message}")
                    cameraToggleButton.isEnabled = true
                }
            }
        }
    }

    private fun onEndCallClicked(meeting: DyteMobileClient) {
        endCallButton.isEnabled = false
        val error = meeting.participants.kickAll()
        if (error != null) {
            Log.d(TAG, "endCall, ${error.message}")
            endCallButton.isEnabled = true
        }
    }

    private fun enableCallControls() {
        cameraToggleButton.isEnabled = true
        endCallButton.isEnabled = true
    }

    private fun disableCallControls() {
        cameraToggleButton.isEnabled = false
        endCallButton.isEnabled = false
    }

    private fun showProgressBar() {
        progressBar.isVisible = true
    }

    private fun hideProgressBar() {
        progressBar.isVisible = false
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val AUTH_TOKEN = "<paste_auth_token_here>"
    }
}