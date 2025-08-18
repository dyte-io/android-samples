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
          refreshVideoViews(rtkClient, active)
        }
      }
    )

    refreshVideoViews(rtkClient, rtkClient.participants.active)
  }

  private fun refreshVideoViews(
    rtkClient: RealtimeKitClient,
    activeParticipants: List<RtkRemoteParticipant>,
  ) {
    val self = rtkClient.localUser
    val selfVideoView = self.getVideoView()
    selfVideoView?.stopVideoRender()

    val remoteParticipant = activeParticipants.find { it.id != self.id }

    // Configure primary tile
    setupVideoTile(primaryTile, context) { tile -> tile.activate(remoteParticipant ?: self) }

    // Configure secondary tile (only shown when remote participant exists)
    secondaryTile.apply {
      if (remoteParticipant != null) {
        visible()
        setupVideoTile(this, context) { tile ->
          tile.setZOrderMediaOverlay()
          tile.activate(self)
        }
      } else {
        gone()
      }
    }
  }

  private fun setupVideoTile(
    container: ConstraintLayout,
    context: Context,
    configureTile: (RtkParticipantTileView) -> Unit,
  ) {
    val tile = RtkParticipantTileView(context)
    container.removeAllViews()
    container.addView(tile)

    // Use MATCH_PARENT instead of container dimensions which might be 0 during initial layout
    val params = tile.layoutParams as ConstraintLayout.LayoutParams
    params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
    params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
    tile.layoutParams = params

    configureTile(tile)
  }
}
