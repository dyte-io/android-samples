package io.dyte.sample.facetime

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import dyte.io.uikit.utils.ViewUtils.gone
import dyte.io.uikit.utils.ViewUtils.visible
import dyte.io.uikit.view.participanttile.DyteParticipantTileView
import io.dyte.core.DyteMobileClient
import io.dyte.core.listeners.DyteParticipantEventsListener
import io.dyte.core.models.DyteJoinedMeetingParticipant

class MeetingView : ConstraintLayout {
    private lateinit var primaryTile: ConstraintLayout
    private lateinit var secondaryTile: ConstraintLayout
    private lateinit var controlBarView: ControlBarView

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
        inflate(context, R.layout.meeting_view, this)

        primaryTile = findViewById(R.id.primaryParticipant)
        secondaryTile = findViewById(R.id.secondaryParticipant)
        controlBarView = findViewById(R.id.control_bar)
    }

    fun activate(mobileClient: DyteMobileClient) {
        controlBarView.activate(mobileClient)

        mobileClient.addParticipantEventsListener(object : DyteParticipantEventsListener {
            override fun onActiveParticipantsChanged(active: List<DyteJoinedMeetingParticipant>) {
                super.onActiveParticipantsChanged(active)
                println("dytelog::MeetingView::onActiveParticipantsChanged::")
                val self = mobileClient.localUser
                val selfVideoView = self.getVideoView()
                selfVideoView?.stopVideoRender()
                val other = active.find { it.id != self.id }
                if (other == null) {
                    secondaryTile.gone()
                    val tile = DyteParticipantTileView(context)
                    primaryTile.removeAllViews()
                    primaryTile.addView(tile)
                    val params = tile.layoutParams
                    params.height = primaryTile.height
                    params.width = primaryTile.width
                    println("dytelog::MeetingView::onActiveParticipantsChanged::params::${params.width}::${params.height}")
                    tile.layoutParams = params
                    tile.activate(self)
                } else {
                    val tile = DyteParticipantTileView(context)
                    primaryTile.removeAllViews()
                    primaryTile.addView(tile)
                    val params = tile.layoutParams
                    params.height = primaryTile.height
                    params.width = primaryTile.width
                    println("dytelog::MeetingView::onActiveParticipantsChanged::params::${params.width}::${params.height}")
                    tile.layoutParams = params
                    tile.activate(other)

                    val selfTile = DyteParticipantTileView(context)
                    selfTile.setZOrderMediaOverlay()
                    secondaryTile.removeAllViews()
                    secondaryTile.addView(selfTile)
                    val sParams = selfTile.layoutParams
                    sParams.height = secondaryTile.height
                    sParams.width = secondaryTile.width
                    println("dytelog::MeetingView::onActiveParticipantsChanged::sParams::${sParams.width}::${sParams.height}")
                    selfTile.layoutParams = sParams
                    selfTile.activate(self)
                }
            }
        })
    }
}