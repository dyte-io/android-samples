package io.dyte.sample.facetime

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import dyte.io.uikit.view.controlbarbuttons.DyteCameraToggleButton
import dyte.io.uikit.view.controlbarbuttons.DyteLeaveButton
import dyte.io.uikit.view.controlbarbuttons.DyteMicToggleButton
import io.dyte.core.DyteMobileClient

class ControlBarView: ConstraintLayout {
    private lateinit var cameraToggle: DyteCameraToggleButton
    private lateinit var micToggle: DyteMicToggleButton
    private lateinit var leaveCall: DyteLeaveButton

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
    }

    fun activate(dyteMobileClient: DyteMobileClient) {
        cameraToggle.activate(dyteMobileClient)
        micToggle.activate(dyteMobileClient)
        leaveCall.activate(dyteMobileClient)
    }
}