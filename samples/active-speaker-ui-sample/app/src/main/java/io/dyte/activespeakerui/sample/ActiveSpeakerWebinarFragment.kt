package io.dyte.activespeakerui.sample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dyte.io.uikit.DyteUIKitBuilder
import dyte.io.uikit.token.DyteDesignTokens
import dyte.io.uikit.view.DyteMeetingTitleView
import dyte.io.uikit.view.controlbarbuttons.DyteControlBarButton
import dyte.io.uikit.view.controlbars.DyteControlBarView
import io.dyte.activespeakerui.sample.views.JoinStageConfirmationDialog
import io.dyte.activespeakerui.sample.views.MeetingView
import io.dyte.activespeakerui.sample.views.NotifyingControlBarButton
import io.dyte.activespeakerui.sample.widget.RaiseHandButton
import io.dyte.core.DyteMobileClient
import io.dyte.core.controllers.DyteStageStatus
import io.dyte.core.feat.DyteChatMessage
import io.dyte.core.feat.DytePollMessage
import io.dyte.core.listeners.DyteChatEventsListener
import io.dyte.core.listeners.DytePollEventsListener
import io.dyte.core.listeners.DyteStageEventListener

class ActiveSpeakerWebinarFragment : Fragment() {
    private lateinit var meetingTitleView: DyteMeetingTitleView
    private lateinit var controlBarView: DyteControlBarView
    private lateinit var chatToggleButton: NotifyingControlBarButton
    private lateinit var pollsToggleButton: NotifyingControlBarButton
    private lateinit var raiseHandButton: RaiseHandButton
    private lateinit var settingsButton: DyteControlBarButton
    private lateinit var meetingView: MeetingView

    private val meeting by lazy {
        DyteUIKitBuilder.dyteUIKit.meeting
    }

    private val stageInvitationListener = object : DyteStageEventListener {
        override fun onStageStatusUpdated(stageStatus: DyteStageStatus) {
            super.onStageStatusUpdated(stageStatus)
            if (stageStatus == DyteStageStatus.ACCEPTED_TO_JOIN_STAGE) {
                showJoinStageConfirmationDialog()
            }
        }
    }

    private val readChatListener = object : DyteChatEventsListener {
        private var readChatCount = 0

        override fun onChatUpdates(messages: List<DyteChatMessage>) {
            super.onChatUpdates(messages)
            if (!meetingView.chatPanelOpen) {
                val unreadChatCount = messages.size - readChatCount
                chatToggleButton.notificationCount = unreadChatCount
            } else {
                readChatCount = messages.size
            }
        }
    }

    private val readPollsListener = object : DytePollEventsListener {
        private var readPollsCount = 0

        override fun onPollUpdates(pollMessages: List<DytePollMessage>) {
            super.onPollUpdates(pollMessages)
            if (!meetingView.pollsPanelOpen) {
                val unreadPollsCount = pollMessages.size - readPollsCount
                pollsToggleButton.notificationCount = unreadPollsCount
            } else {
                readPollsCount = pollMessages.size
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_active_speaker_webinar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHeaderView(view)
        setUpControlBarView(view)
        applyDesignTokens(DyteUIKitBuilder.dyteUIKit.designTokens)
        activateUI(meeting)
    }

    override fun onDestroyView() {
        meeting.removeStageEventsListener(stageInvitationListener)
        meeting.removeChatEventsListener(readChatListener)
        meeting.removePollEventsListener(readPollsListener)
        super.onDestroyView()
    }

    private fun setUpHeaderView(rootView: View) {
        meetingTitleView = rootView.findViewById(R.id.meeting_title_view)
    }

    private fun setUpControlBarView(rootView: View) {
        controlBarView = rootView.findViewById(R.id.meeting_controls)
        chatToggleButton = rootView.findViewById(R.id.button_chat_toggle)
        pollsToggleButton = rootView.findViewById(R.id.button_polls_toggle)
        raiseHandButton = rootView.findViewById(R.id.button_raise_hand)
        settingsButton = rootView.findViewById(R.id.button_settings)
        meetingView = rootView.findViewById(R.id.meeting_view)

        chatToggleButton.setOnClickListener {
            onChatToggleButtonClicked()
        }

        pollsToggleButton.setOnClickListener {
            onPollsToggleButtonClicked()
        }

        raiseHandButton.joinStageClickListener = {
            showJoinStageConfirmationDialog()
        }

        settingsButton.setOnClickListener {
            onSettingsButtonClicked()
        }
    }

    private fun applyDesignTokens(designTokens: DyteDesignTokens) {
        controlBarView.applyDesignTokens(designTokens)
    }

    private fun activateUI(meeting: DyteMobileClient) {
        meetingTitleView.activate(meeting)
        raiseHandButton.activate(meeting)
        chatToggleButton.notificationCount = meeting.chat.messages.size
        pollsToggleButton.notificationCount = meeting.polls.polls.size
        meeting.addStageEventsListener(stageInvitationListener)
        meeting.addChatEventsListener(readChatListener)
        meeting.addPollEventsListener(readPollsListener)
    }

    private fun onChatToggleButtonClicked() {
        Log.d(TAG, "ChatToggleButtonClicked")
        meetingView.toggleChatWindow(childFragmentManager)
        chatToggleButton.notificationCount = 0
    }

    private fun onPollsToggleButtonClicked() {
        Log.d(TAG, "PollsToggleButtonClicked")
        meetingView.togglePollsWindow(childFragmentManager)
        pollsToggleButton.notificationCount = 0
    }

    // TODO: Implement Raise hand feature
    private fun onRaiseHandButtonClicked() {
        Log.d(TAG, "RaiseHandButtonClicked")
        showJoinStageConfirmationDialog()
    }

    // TODO: Open Settings dialog
    private fun onSettingsButtonClicked() {
        Log.d(TAG, "SettingsButtonClicked")
    }

    private fun showJoinStageConfirmationDialog() {
        val joinStageConfirmationDialog = JoinStageConfirmationDialog(
            requireContext(),
            DyteUIKitBuilder.dyteUIKit.designTokens
        )
        joinStageConfirmationDialog.show()
        joinStageConfirmationDialog.activate(meeting)
    }

    companion object {
        private const val TAG = "ActiveSpeakerFragment"
    }
}