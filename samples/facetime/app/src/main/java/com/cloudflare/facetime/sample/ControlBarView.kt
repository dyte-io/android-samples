package com.cloudflare.facetime.sample

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.cloudflare.realtimekit.RealtimeKitClient
import com.cloudflare.realtimekit.ui.screens.chat.RtkChatBottomSheet
import com.cloudflare.realtimekit.ui.screens.participant.RtkParticipantsFragment
import com.cloudflare.realtimekit.ui.view.controlbarbuttons.RtkCameraToggleButton
import com.cloudflare.realtimekit.ui.view.controlbarbuttons.RtkControlBarButton
import com.cloudflare.realtimekit.ui.view.controlbarbuttons.RtkLeaveButton
import com.cloudflare.realtimekit.ui.view.controlbarbuttons.RtkMicToggleButton

class ControlBarView : ConstraintLayout {
  private val cameraToggle: RtkCameraToggleButton by lazy { findViewById(R.id.rtk_camera_toggle) }
  private val micToggle: RtkMicToggleButton by lazy { findViewById(R.id.rtk_mic_toggle) }
  private val leaveCall: RtkLeaveButton by lazy { findViewById(R.id.rtk_leave) }
  private val chatButton: RtkControlBarButton by lazy { findViewById(R.id.rtk_chat) }
  private val participantButton: RtkControlBarButton by lazy { findViewById(R.id.rtk_participant) }

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
    inflate(context, R.layout.control_bar, this)
  }

  fun activate(rtkClient: RealtimeKitClient, fragmentManager: FragmentManager) {
    cameraToggle.activate(rtkClient)
    micToggle.activate(rtkClient)
    leaveCall.activate(rtkClient)

    chatButton.setOnClickListener {
      val chatFragment = RtkChatBottomSheet()
      chatFragment.show(fragmentManager, TAG)
    }

    participantButton.setOnClickListener {
      val participantFragment = RtkParticipantsFragment()
      participantFragment.show(fragmentManager, TAG)
    }
  }

  companion object {
    const val TAG = "ControlBarView"
  }
}
