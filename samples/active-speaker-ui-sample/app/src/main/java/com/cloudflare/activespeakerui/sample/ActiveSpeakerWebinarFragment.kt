package com.cloudflare.activespeakerui.sample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cloudflare.activespeakerui.sample.views.JoinStageConfirmationDialog
import com.cloudflare.activespeakerui.sample.views.MeetingView
import com.cloudflare.activespeakerui.sample.views.NotifyingControlBarButton
import com.cloudflare.activespeakerui.sample.widget.RaiseHandButton
import com.cloudflare.realtimekit.RealtimeKitClient
import com.cloudflare.realtimekit.chat.ChatMessage
import com.cloudflare.realtimekit.chat.RtkChatEventListener
import com.cloudflare.realtimekit.polls.Poll
import com.cloudflare.realtimekit.polls.RtkPollsEventListener
import com.cloudflare.realtimekit.stage.RtkStageEventListener
import com.cloudflare.realtimekit.stage.StageStatus
import com.cloudflare.realtimekit.ui.RealtimeKitUIBuilder
import com.cloudflare.realtimekit.ui.screens.settings.RtkSettingsFragment
import com.cloudflare.realtimekit.ui.token.RtkDesignTokens
import com.cloudflare.realtimekit.ui.view.RtkMeetingTitleView
import com.cloudflare.realtimekit.ui.view.controlbarbuttons.RtkControlBarButton
import com.cloudflare.realtimekit.ui.view.controlbars.RtkControlBarView

class ActiveSpeakerWebinarFragment : Fragment() {
  private lateinit var meetingTitleView: RtkMeetingTitleView
  private lateinit var controlBarView: RtkControlBarView
  private lateinit var chatToggleButton: NotifyingControlBarButton
  private lateinit var pollsToggleButton: NotifyingControlBarButton
  private lateinit var raiseHandButton: RaiseHandButton
  private lateinit var settingsButton: RtkControlBarButton
  private lateinit var meetingView: MeetingView

  private val meeting by lazy { RealtimeKitUIBuilder.realtimeKitUI.meeting }

  private val stageInvitationListener =
    object : RtkStageEventListener {
      override fun onStageStatusUpdated(oldStatus: StageStatus, newStatus: StageStatus) {
        super.onStageStatusUpdated(oldStatus, newStatus)
        if (newStatus == StageStatus.ACCEPTED_TO_JOIN_STAGE) {
          showJoinStageConfirmationDialog()
        }
      }
    }

  private val readChatListener =
    object : RtkChatEventListener {
      private var readChatCount = 0

      override fun onChatUpdates(messages: List<ChatMessage>) {
        super.onChatUpdates(messages)
        if (!meetingView.chatPanelOpen) {
          val unreadChatCount = messages.size - readChatCount
          chatToggleButton.notificationCount = unreadChatCount
        } else {
          readChatCount = messages.size
        }
      }
    }

  private val readPollsListener =
    object : RtkPollsEventListener {
      private var readPollsCount = 0

      override fun onPollUpdates(pollItems: List<Poll>) {
        super.onPollUpdates(pollItems)
        if (!meetingView.pollsPanelOpen) {
          val unreadPollsCount = pollItems.size - readPollsCount
          pollsToggleButton.notificationCount = unreadPollsCount
        } else {
          readPollsCount = pollItems.size
        }
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.fragment_active_speaker_webinar, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setUpHeaderView(view)
    setUpControlBarView(view)
    applyDesignTokens(RealtimeKitUIBuilder.realtimeKitUI.designTokens)
    activateUI(meeting)
  }

  override fun onDestroyView() {
    meeting.removeStageEventListener(stageInvitationListener)
    meeting.removeChatEventListener(readChatListener)
    meeting.removePollsEventListener(readPollsListener)
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

    chatToggleButton.setOnClickListener { onChatToggleButtonClicked() }

    pollsToggleButton.setOnClickListener { onPollsToggleButtonClicked() }

    raiseHandButton.joinStageClickListener = { showJoinStageConfirmationDialog() }

    settingsButton.setOnClickListener { onSettingsButtonClicked() }
  }

  private fun applyDesignTokens(designTokens: RtkDesignTokens) {
    controlBarView.applyDesignTokens(designTokens)
  }

  private fun activateUI(meeting: RealtimeKitClient) {
    meetingTitleView.activate(meeting)
    raiseHandButton.activate(meeting)
    chatToggleButton.notificationCount = meeting.chat.messages.size
    pollsToggleButton.notificationCount = meeting.polls.items.size
    meeting.addStageEventListener(stageInvitationListener)
    meeting.addChatEventListener(readChatListener)
    meeting.addPollsEventListener(readPollsListener)
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

  private fun onSettingsButtonClicked() {
    Log.d(TAG, "SettingsButtonClicked")
    val dyteSettingsFragment = RtkSettingsFragment()
    dyteSettingsFragment.show(
      childFragmentManager,
      ActiveSpeakerWebinarFragment::class.java.simpleName,
    )
  }

  private fun showJoinStageConfirmationDialog() {
    val joinStageConfirmationDialog =
      JoinStageConfirmationDialog(requireContext(), RealtimeKitUIBuilder.realtimeKitUI.designTokens)
    joinStageConfirmationDialog.show()
    joinStageConfirmationDialog.activate(meeting)
  }

  companion object {
    private const val TAG = "ActiveSpeakerFragment"
  }
}
