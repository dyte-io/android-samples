package io.dyte.sample.facetime

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import dyte.io.uikit.screens.chat.DyteChatBottomSheet
import dyte.io.uikit.screens.participant.DyteParticipantsFragment
import dyte.io.uikit.view.controlbarbuttons.DyteCameraToggleButton
import dyte.io.uikit.view.controlbarbuttons.DyteControlBarButton
import dyte.io.uikit.view.controlbarbuttons.DyteLeaveButton
import dyte.io.uikit.view.controlbarbuttons.DyteMicToggleButton
import io.dyte.core.DyteMobileClient

class ControlBarView: ConstraintLayout {
    private lateinit var cameraToggle: DyteCameraToggleButton
    private lateinit var micToggle: DyteMicToggleButton
    private lateinit var leaveCall: DyteLeaveButton
    private lateinit var chatButton: DyteControlBarButton
    private lateinit var participantButton: DyteControlBarButton

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
        inflate(context, R.layout.control_bar, this)

        cameraToggle = findViewById(R.id.dyte_camera_toggle)
        micToggle = findViewById(R.id.dyte_mic_toggle)
        leaveCall = findViewById(R.id.dyte_leave)
        chatButton = findViewById(R.id.dyte_chat)
        participantButton = findViewById(R.id.dyte_participant)
    }

    fun activate(dyteMobileClient: DyteMobileClient, fragmentManager: FragmentManager) {
        cameraToggle.activate(dyteMobileClient)
        micToggle.activate(dyteMobileClient)
        leaveCall.activate(dyteMobileClient)

        chatButton.setOnClickListener {
            val chatFragment = DyteChatBottomSheet()
            chatFragment.show(fragmentManager, "ControlBar")
        }

        participantButton.setOnClickListener {
            val participantFragment = DyteParticipantsFragment()
            participantFragment.show(fragmentManager, "ControlBar")
        }
    }
}