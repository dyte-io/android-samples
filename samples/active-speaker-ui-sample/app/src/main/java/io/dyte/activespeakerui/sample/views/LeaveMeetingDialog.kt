package io.dyte.activespeakerui.sample.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import dyte.io.uikit.token.DyteDesignTokens
import dyte.io.uikit.view.button.DyteButton
import io.dyte.activespeakerui.sample.R
import io.dyte.core.DyteMobileClient

/*
* NOTE: This can also be implemented using a DialogFragment if the codebase already uses Fragments.
* */
class LeaveMeetingDialog(
  context: Context,
  private val meeting: DyteMobileClient,
  private val designTokens: DyteDesignTokens? = null
) : AppCompatDialog(context) {
  private var confirmButton: DyteButton? = null
  private var cancelButton: DyteButton? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.dialog_leave_meeting_confirmation)

    confirmButton = findViewById(R.id.button_leave_meeting_confirm)
    cancelButton = findViewById(R.id.button_leave_meeting_cancel)

    confirmButton?.setOnClickListener {
      onConfirmButtonClicked()
    }

    cancelButton?.setOnClickListener {
      onCancelButtonClicked()
    }

    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    designTokens?.let { applyDesignTokens(it) }
  }

  private fun onConfirmButtonClicked() {
    meeting.leaveRoom()
    dismiss()
  }

  private fun onCancelButtonClicked() {
    cancel()
  }

  private fun applyDesignTokens(designTokens: DyteDesignTokens) {
    confirmButton?.applyDesignTokens(designTokens)
    cancelButton?.applyDesignTokens(designTokens)
  }
}