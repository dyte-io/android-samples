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
import com.cloudflare.realtimekit.ui.view.RtkMeetingTitleView
import com.cloudflare.realtimekit.ui.view.controlbarbuttons.RtkControlBarButton
import com.cloudflare.realtimekit.ui.view.controlbars.RtkControlBarView

class ActiveSpeakerWebinarFragment : Fragment() {
  private val meetingTitleView: RtkMeetingTitleView by lazy {
    view?.findViewById(R.id.meeting_title_view)!!
  }
  private val controlBarView: RtkControlBarView by lazy {
    view?.findViewById(R.id.meeting_controls)!!
  }
  private val chatToggleButton: NotifyingControlBarButton by lazy {
    view?.findViewById(R.id.button_chat_toggle)!!
  }
  private val pollsToggleButton: NotifyingControlBarButton by lazy {
    view?.findViewById(R.id.button_polls_toggle)!!
  }
  private val raiseHandButton: RaiseHandButton by lazy {
    view?.findViewById(R.id.button_raise_hand)!!
  }
  private val settingsButton: RtkControlBarButton by lazy {
    view?.findViewById(R.id.button_settings)!!
  }
  private val meetingView: MeetingView by lazy { view?.findViewById(R.id.meeting_view)!! }

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

    chatToggleButton.setOnClickListener { onChatToggleButtonClicked() }
    pollsToggleButton.setOnClickListener { onPollsToggleButtonClicked() }
    raiseHandButton.joinStageClickListener = { showJoinStageConfirmationDialog() }
    settingsButton.setOnClickListener { onSettingsButtonClicked() }
    controlBarView.applyDesignTokens(RealtimeKitUIBuilder.realtimeKitUI.designTokens)

    activateUI(meeting)
  }

  override fun onDestroyView() {
    meeting.removeStageEventListener(stageInvitationListener)
    meeting.removeChatEventListener(readChatListener)
    meeting.removePollsEventListener(readPollsListener)
    super.onDestroyView()
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
    val settingsFragment = RtkSettingsFragment()
    settingsFragment.show(childFragmentManager, ActiveSpeakerWebinarFragment::class.java.simpleName)
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
