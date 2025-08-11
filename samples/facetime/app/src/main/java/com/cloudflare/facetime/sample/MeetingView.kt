package com.cloudflare.facetime.sample

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.cloudflare.realtimekit.RealtimeKitClient
import com.cloudflare.realtimekit.participants.RtkParticipantsEventListener
import com.cloudflare.realtimekit.participants.RtkRemoteParticipant
import com.cloudflare.realtimekit.ui.utils.ViewUtils.gone
import com.cloudflare.realtimekit.ui.utils.ViewUtils.visible
import com.cloudflare.realtimekit.ui.view.participanttile.RtkParticipantTileView

class MeetingView : ConstraintLayout {
  private val primaryTile: ConstraintLayout by lazy { findViewById(R.id.primaryParticipant) }
  private val secondaryTile: ConstraintLayout by lazy { findViewById(R.id.secondaryParticipant) }
  private val controlBarView: ControlBarView by lazy { findViewById(R.id.control_bar) }

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
    inflate(context, R.layout.meeting_view, this)
  }

  fun activate(rtkClient: RealtimeKitClient, supportFragmentManager: FragmentManager) {
    controlBarView.activate(rtkClient, supportFragmentManager)
    rtkClient.addParticipantsEventListener(
      object : RtkParticipantsEventListener {
        override fun onActiveParticipantsChanged(active: List<RtkRemoteParticipant>) {
          super.onActiveParticipantsChanged(active)
          val self = rtkClient.localUser
          val selfVideoView = self.getVideoView()
          selfVideoView?.stopVideoRender()
          val other = active.find { it.id != self.id }
          if (other == null) {
            secondaryTile.gone()
            val tile = RtkParticipantTileView(context)
            primaryTile.removeAllViews()
            primaryTile.addView(tile)
            val params = tile.layoutParams
            params.height = primaryTile.height
            params.width = primaryTile.width
            tile.layoutParams = params
            tile.activateForSelfPreview(self)
          } else {
            val tile = RtkParticipantTileView(context)
            primaryTile.removeAllViews()
            primaryTile.addView(tile)
            val params = tile.layoutParams
            params.height = primaryTile.height
            params.width = primaryTile.width
            tile.layoutParams = params
            tile.activate(other)
            secondaryTile.visible()
            val selfTile = RtkParticipantTileView(context)
            selfTile.setZOrderMediaOverlay()
            secondaryTile.removeAllViews()
            secondaryTile.addView(selfTile)
            val sParams = selfTile.layoutParams
            sParams.height = secondaryTile.height
            sParams.width = secondaryTile.width
            selfTile.layoutParams = sParams
            selfTile.activateForSelfPreview(self)
          }
        }
      }
    )
  }
}
