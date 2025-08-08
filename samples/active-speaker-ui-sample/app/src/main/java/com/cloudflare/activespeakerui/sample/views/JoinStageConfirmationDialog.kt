package com.cloudflare.activespeakerui.sample.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import androidx.core.graphics.drawable.toDrawable
import com.cloudflare.activespeakerui.sample.R
import com.cloudflare.activespeakerui.sample.utils.DialogUtils.setWidthToScreenPercentage
import com.cloudflare.activespeakerui.sample.utils.ViewUtils.getOrientation
import com.cloudflare.realtimekit.RealtimeKitClient
import com.cloudflare.realtimekit.stage.RtkStageEventListener
import com.cloudflare.realtimekit.stage.StageStatus
import com.cloudflare.realtimekit.ui.token.RtkDesignTokens
import com.cloudflare.realtimekit.ui.view.button.RtkButton
import com.cloudflare.realtimekit.ui.view.controlbarbuttons.RtkCameraToggleButton
import com.cloudflare.realtimekit.ui.view.controlbarbuttons.RtkMicToggleButton
import com.cloudflare.realtimekit.ui.view.participanttile.RtkParticipantTileView

// NOTE: This can also be implemented using a DialogFragment if the codebase already uses Fragments.
class JoinStageConfirmationDialog(
    context: Context,
    private val designTokens: RtkDesignTokens? = null
) : AppCompatDialog(context) {
  private var participantPreviewTile: RtkParticipantTileView? = null
  private var cameraToggleButton: RtkCameraToggleButton? = null
  private var micToggleButton: RtkMicToggleButton? = null
  private var confirmButton: RtkButton? = null
  private var cancelButton: RtkButton? = null

  private var meeting: RealtimeKitClient? = null

  private var stageStatusUpdateListener =
      object : RtkStageEventListener {
        override fun onStageStatusUpdated(oldStatus: StageStatus, newStatus: StageStatus) {
          super.onStageStatusUpdated(oldStatus, newStatus)

          val shouldDismissDialog =
              oldStatus == StageStatus.ACCEPTED_TO_JOIN_STAGE && newStatus == StageStatus.OFF_STAGE

          if (shouldDismissDialog) dismiss()
        }
      }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.dialog_join_stage_confirmation)

    participantPreviewTile = findViewById(R.id.participanttileview_join_stage_confirmation)
    cameraToggleButton = findViewById(R.id.cameratoggle_join_stage_confirmation)
    micToggleButton = findViewById(R.id.mictoggle_join_stage_confirmation)
    confirmButton = findViewById(R.id.button_join_stage_confirmation_confirm)
    cancelButton = findViewById(R.id.button_join_stage_confirmation_cancel)

    cancelButton?.setOnClickListener { onCancelButtonClicked() }

    window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    designTokens?.let { applyDesignTokens(it) }
  }

  override fun onStart() {
    super.onStart()
    if (context.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
      setWidthToScreenPercentage(0.90f)
    } else {
      setWidthToScreenPercentage(0.60f)
    }
  }

  override fun onDetachedFromWindow() {
    meeting?.removeStageEventListener(stageStatusUpdateListener)
    meeting = null
    super.onDetachedFromWindow()
  }

  fun activate(meeting: RealtimeKitClient) {
    this.meeting = meeting
    participantPreviewTile?.activateForSelfPreview(meeting.localUser)
    micToggleButton?.activate(meeting)
    cameraToggleButton?.activate(meeting)
    this.meeting?.addStageEventListener(stageStatusUpdateListener)
    confirmButton?.setOnClickListener { onConfirmButtonClicked() }
  }

  private fun onConfirmButtonClicked() {
    meeting?.let {
      if (it.stage.stageStatus == StageStatus.ACCEPTED_TO_JOIN_STAGE) {
        confirmButton?.text = "Joining stage…"
        try {
          it.stage.join()
        } catch (e: Exception) {
          // no-op
        }
        confirmButton?.text = "Raise Hand"
      }
    }
    dismiss()
  }

  private fun onCancelButtonClicked() {
    meeting?.let {
      if (it.stage.stageStatus == StageStatus.ACCEPTED_TO_JOIN_STAGE) {
        try {
          it.stage.leave()
        } catch (e: Exception) {
          // no-op
        }
      }
    }
    cancel()
  }

  private fun applyDesignTokens(designTokens: RtkDesignTokens) {
    participantPreviewTile?.applyDesignTokens(designTokens)
    cameraToggleButton?.applyDesignTokens(designTokens)
    micToggleButton?.applyDesignTokens(designTokens)
    confirmButton?.applyDesignTokens(designTokens)
    cancelButton?.applyDesignTokens(designTokens)
  }
}
