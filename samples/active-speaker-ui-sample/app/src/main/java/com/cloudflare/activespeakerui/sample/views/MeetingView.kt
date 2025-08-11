package com.cloudflare.activespeakerui.sample.views

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.fragment.app.FragmentManager
import com.cloudflare.activespeakerui.sample.R
import com.cloudflare.activespeakerui.sample.utils.ViewUtils.gone
import com.cloudflare.activespeakerui.sample.utils.ViewUtils.visible
import com.cloudflare.realtimekit.participants.RtkParticipantsEventListener
import com.cloudflare.realtimekit.participants.RtkRemoteParticipant
import com.cloudflare.realtimekit.plugins.RtkPlugin
import com.cloudflare.realtimekit.plugins.RtkPluginsEventListener
import com.cloudflare.realtimekit.stage.StageStatus
import com.cloudflare.realtimekit.ui.RealtimeKitUIBuilder
import com.cloudflare.realtimekit.ui.screens.chat.RtkChatFragment
import com.cloudflare.realtimekit.ui.screens.polls.RtkPollsFragment
import com.cloudflare.realtimekit.ui.view.grid.RtkGridView
import com.cloudflare.realtimekit.ui.view.participanttile.RtkParticipantTileView

class MeetingView : ConstraintLayout {
  private lateinit var dptvFloting: RtkParticipantTileView
  private lateinit var clLeftbar: ConstraintLayout
  private lateinit var dgvGrid: RtkGridView

  var chatPanelOpen = false
  var pollsPanelOpen = false

  private val meeting by lazy { RealtimeKitUIBuilder.meeting }

  private val pinnedUserEventListener =
    object : RtkParticipantsEventListener {
      override fun onParticipantPinned(participant: RtkRemoteParticipant) {
        super.onParticipantPinned(participant)
        refreshGrid()
      }

      override fun onParticipantUnpinned(participant: RtkRemoteParticipant) {
        super.onParticipantUnpinned(participant)
        refreshGrid()
      }

      override fun onActiveSpeakerChanged(participant: RtkRemoteParticipant?) {
        super.onActiveSpeakerChanged(participant)
        refreshGrid()
      }

      override fun onScreenShareUpdate(participant: RtkRemoteParticipant, isEnabled: Boolean) {
        super.onScreenShareUpdate(participant, isEnabled)
        refreshGrid()
        dgvGrid.refresh(true)
      }
    }

  private val pluginEventsListener =
    object : RtkPluginsEventListener {
      override fun onPluginActivated(plugin: RtkPlugin) {
        super.onPluginActivated(plugin)
        refreshGrid()
      }

      override fun onPluginDeactivated(plugin: RtkPlugin) {
        super.onPluginDeactivated(plugin)
        refreshGrid()
        dgvGrid.refresh(true)
      }
    }

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    init(context)
  }

  constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
  ) : super(context, attrs, defStyleAttr) {
    init(context)
  }

  private fun init(context: Context) {
    inflate(context, R.layout.view_meeting, this)

    dptvFloting = findViewById(R.id.dptvFloting)
    dptvFloting.setZOrderMediaOverlay()
    clLeftbar = findViewById(R.id.clLeftBar)
    dgvGrid = findViewById(R.id.dgvGrid)

    dgvGrid.enableFocusMode()
    dgvGrid.activate(meeting)

    meeting.addParticipantsEventListener(pinnedUserEventListener)
    meeting.addPluginsEventListener(pluginEventsListener)

    clLeftbar.gone()

    refreshGrid()
  }

  fun toggleChatWindow(supportFragmentManager: FragmentManager) {
    if (chatPanelOpen.not() || clLeftbar.isGone) {
      chatPanelOpen = true
      val chatFragment = RtkChatFragment()
      clLeftbar.visible()
      supportFragmentManager
        .beginTransaction()
        .replace(R.id.clLeftBar, chatFragment, "chat")
        .commit()
      clLeftbar.post { chatFragment.hideHeader() }
    } else {
      chatPanelOpen = false
      supportFragmentManager.findFragmentByTag("chat")?.let {
        supportFragmentManager.beginTransaction().remove(it).commit()
      }
      clLeftbar.gone()
    }
    clLeftbar.post { dgvGrid.refresh(true) }
  }

  fun togglePollsWindow(supportFragmentManager: FragmentManager) {
    if (pollsPanelOpen.not() || clLeftbar.isGone) {
      pollsPanelOpen = true
      val pollsFragment = RtkPollsFragment()
      clLeftbar.visible()
      supportFragmentManager
        .beginTransaction()
        .replace(R.id.clLeftBar, pollsFragment, "polls")
        .commit()
      clLeftbar.post { pollsFragment.hideHeader() }
    } else {
      pollsPanelOpen = false
      supportFragmentManager.findFragmentByTag("polls")?.let {
        supportFragmentManager.beginTransaction().remove(it).commit()
      }
      clLeftbar.gone()
    }
    clLeftbar.post { dgvGrid.refresh(true) }
  }

  private fun refreshGrid() {
    val pinnedPeer = meeting.participants.pinned
    val screenShares = meeting.participants.screenShares
    val plugins = meeting.plugins.active

    if (pinnedPeer == null) {
      if (screenShares.isNotEmpty() || plugins.isNotEmpty()) {
        dgvGrid.visible()
        dgvGrid.refresh()
        refreshFloatingPeer()
      } else {
        dgvGrid.visible()
        dptvFloting.gone()
        dgvGrid.refresh()
      }
    } else {
      if (screenShares.isNotEmpty() || plugins.isNotEmpty()) {
        dgvGrid.visible()
        dgvGrid.refresh()
        refreshFloatingPeer()
      } else {
        dgvGrid.visible()
        dptvFloting.gone()
        dgvGrid.refresh()
      }
    }
  }

  private fun refreshFloatingPeer() {
    val presenter = meeting.participants.active.firstOrNull { it.presetName == "webinar_presenter" }
    val pinnedPeer = meeting.participants.pinned
    val activeSpeaker = meeting.participants.activeSpeaker
    val self = meeting.localUser

    if (pinnedPeer != null) {
      dptvFloting.visible()
      dptvFloting.activate(pinnedPeer)
    } else if (activeSpeaker != null) {
      dptvFloting.visible()
      dptvFloting.activate(activeSpeaker)
    } else if (presenter != null) {
      dptvFloting.visible()
      dptvFloting.activate(presenter)
    } else if (self.stageStatus == StageStatus.ON_STAGE) {
      dptvFloting.visible()
      dptvFloting.activateForSelfPreview(self)
    } else {
      dptvFloting.gone()
    }
  }
}
