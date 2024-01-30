package io.dyte.activespeakerui.sample.views

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.fragment.app.FragmentManager
import dyte.io.uikit.DyteUIKitBuilder
import dyte.io.uikit.screens.chat.DyteChatFragment
import dyte.io.uikit.screens.polls.DytePollsFragment
import dyte.io.uikit.utils.ViewUtils.visible
import dyte.io.uikit.view.dytegrid.DyteGridViewV2
import dyte.io.uikit.view.participanttile.DyteParticipantTileView
import io.dyte.activespeakerui.sample.R
import io.dyte.activespeakerui.sample.utils.ViewUtils.gone
import io.dyte.core.controllers.StageStatus
import io.dyte.core.listeners.DyteParticipantEventsListener
import io.dyte.core.models.DyteJoinedMeetingParticipant
import io.dyte.core.observability.DyteLogger

class MeetingView : ConstraintLayout {
    private lateinit var dptvFloting: DyteParticipantTileView
    private lateinit var clLeftbar: ConstraintLayout
    private lateinit var dgvGrid: DyteGridViewV2

    var chatPanelOpen = false
    var pollsPanelOpen = false

    private val meeting by lazy {
        DyteUIKitBuilder.meeting
    }

    private val pinnedUserEventListener = object : DyteParticipantEventsListener {
        override fun onParticipantPinned(participant: DyteJoinedMeetingParticipant) {
            super.onParticipantPinned(participant)
            DyteLogger.info("MeetingView::onParticipantPinned::")
            refreshGrid()
        }

        override fun onParticipantUnpinned(participant: DyteJoinedMeetingParticipant) {
            super.onParticipantUnpinned(participant)
            DyteLogger.info("MeetingView::onParticipantUnpinned::")
            refreshGrid()
        }

        override fun onActiveSpeakerChanged(participant: DyteJoinedMeetingParticipant) {
            super.onActiveSpeakerChanged(participant)
            DyteLogger.info("MeetingView::onActiveSpeakerChanged::")
            refreshGrid()
        }

        override fun onNoActiveSpeaker() {
            super.onNoActiveSpeaker()
            DyteLogger.info("MeetingView::onNoActiveSpeaker::")
//            refreshGrid()
        }

        override fun onScreenSharesUpdated() {
            super.onScreenSharesUpdated()
            DyteLogger.info("MeetingView::onScreenSharesUpdated::")
            refreshGrid()
        }
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(
        context,
        attrs,
        defStyleAttr
    ) {
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

        meeting.addParticipantEventsListener(pinnedUserEventListener)

        clLeftbar.gone()

        refreshGrid()
    }

    fun toggleChatWindow(supportFragmentManager: FragmentManager) {
        if (chatPanelOpen.not() || clLeftbar.isGone) {
            chatPanelOpen = true
            val chatFragment = DyteChatFragment()
            clLeftbar.visible()
            supportFragmentManager.beginTransaction()
                .replace(R.id.clLeftBar, chatFragment, "chat")
                .commit()
            clLeftbar.post {
                chatFragment.hideHeader()
            }
        } else {
            chatPanelOpen = false
            supportFragmentManager.findFragmentByTag("chat")?.let {
                supportFragmentManager.beginTransaction()
                    .remove(it)
                    .commit()
            }
            clLeftbar.gone()
        }
    }

    fun togglePollsWindow(supportFragmentManager: FragmentManager) {
        if (pollsPanelOpen.not() || clLeftbar.isGone) {
            pollsPanelOpen = true
            val pollsFragment = DytePollsFragment()
            clLeftbar.visible()
            supportFragmentManager.beginTransaction()
                .replace(R.id.clLeftBar, pollsFragment, "polls")
                .commit()
            clLeftbar.post {
                pollsFragment.hideHeader()
            }
        } else {
            pollsPanelOpen = false
            supportFragmentManager.findFragmentByTag("polls")?.let {
                supportFragmentManager.beginTransaction()
                    .remove(it)
                    .commit()
            }
            clLeftbar.gone()
        }
    }

    private fun refreshGrid() {
        DyteLogger.info("MeetingView::refreshGrid::")
        val pinnedPeer = meeting.participants.pinned
        val screenShares = meeting.participants.screenShares
        val plugins = meeting.plugins.active

        if (pinnedPeer == null) {
            if (screenShares.isNotEmpty() || plugins.isNotEmpty()) {
                DyteLogger.info("MeetingView::refreshGrid::custom with no pinned")
                dgvGrid.visible()
                dgvGrid.refresh()
                refreshFloatingPeer()
            } else {
                DyteLogger.info("MeetingView::refreshGrid::standard with no pinned")
                dgvGrid.visible()
                dptvFloting.gone()
                dgvGrid.refresh()
            }
        } else {
            if (screenShares.isNotEmpty() || plugins.isNotEmpty()) {
                DyteLogger.info("MeetingView::refreshGrid::custom with pinned")
                dgvGrid.visible()
                dgvGrid.refresh()
                refreshFloatingPeer()
            } else {
                DyteLogger.info("MeetingView::refreshGrid::standard with pinned")
                dgvGrid.visible()
                dptvFloting.gone()
                dgvGrid.refresh()
            }
        }
    }

    private fun refreshFloatingPeer() {
        val pinnedPeer = meeting.participants.pinned
        val activeSpeaker = meeting.participants.activeSpeaker
        val self = meeting.localUser
        if (pinnedPeer != null) {
            DyteLogger.info("MeetingView::refreshFloatingPeer::showing pinned")
            dptvFloting.visible()
            dptvFloting.activate(pinnedPeer)
        } else if(activeSpeaker != null) {
            DyteLogger.info("MeetingView::refreshFloatingPeer::showing active")
            dptvFloting.visible()
            dptvFloting.activate(activeSpeaker)
        } else if(self.stageStatus == StageStatus.ON_STAGE) {
            DyteLogger.info("MeetingView::refreshFloatingPeer::showing self")
            dptvFloting.visible()
            dptvFloting.activate(self)
        } else {
            DyteLogger.info("MeetingView::refreshFloatingPeer::none in floating")
            dptvFloting.gone()
        }
    }
}