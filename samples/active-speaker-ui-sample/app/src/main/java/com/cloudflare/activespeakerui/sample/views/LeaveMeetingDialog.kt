package com.cloudflare.activespeakerui.sample.views

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import androidx.core.graphics.drawable.toDrawable
import com.cloudflare.activespeakerui.sample.R
import com.cloudflare.realtimekit.RealtimeKitClient
import com.cloudflare.realtimekit.ui.token.RtkDesignTokens
import com.cloudflare.realtimekit.ui.view.button.RtkButton

/*
 * NOTE: This can also be implemented using a DialogFragment if the codebase already uses Fragments.
 * */
class LeaveMeetingDialog(
  context: Context,
  private val meeting: RealtimeKitClient,
  private val designTokens: RtkDesignTokens? = null,
) : AppCompatDialog(context) {
  private val confirmButton: RtkButton by lazy { findViewById(R.id.button_leave_meeting_confirm)!! }
  private val cancelButton: RtkButton by lazy { findViewById(R.id.button_leave_meeting_cancel)!! }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.dialog_leave_meeting_confirmation)

    confirmButton.setOnClickListener { onConfirmButtonClicked() }
    cancelButton.setOnClickListener { onCancelButtonClicked() }

    window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    designTokens?.let { applyDesignTokens(it) }
  }

  private fun onConfirmButtonClicked() {
    meeting.leaveRoom(onSuccess = {}, onFailure = {})
    dismiss()
  }

  private fun onCancelButtonClicked() {
    cancel()
  }

  private fun applyDesignTokens(designTokens: RtkDesignTokens) {
    confirmButton.applyDesignTokens(designTokens)
    cancelButton.applyDesignTokens(designTokens)
  }
}
