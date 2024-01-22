package io.dyte.activespeakerui.sample.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import dyte.io.uikit.token.DyteDesignTokens
import dyte.io.uikit.view.button.DyteButton
import dyte.io.uikit.view.controlbarbuttons.DyteCameraToggleButton
import dyte.io.uikit.view.controlbarbuttons.DyteMicToggleButton
import dyte.io.uikit.view.participanttile.DyteParticipantTileView
import io.dyte.activespeakerui.sample.R
import io.dyte.activespeakerui.sample.utils.DialogUtils.setWidthToScreenPercentage
import io.dyte.activespeakerui.sample.utils.ViewUtils.getOrientation
import io.dyte.core.DyteMobileClient
import io.dyte.core.controllers.DyteStageStatus
import io.dyte.core.controllers.StageStatus
import io.dyte.core.listeners.DyteStageEventListener

/*
* NOTE: This can also be implemented using a DialogFragment if the codebase already uses Fragments.
* */
class JoinStageConfirmationDialog(
  context: Context,
  private val designTokens: DyteDesignTokens? = null
) : AppCompatDialog(context) {
  private var participantPreviewTile: DyteParticipantTileView? = null
  private var cameraToggleButton: DyteCameraToggleButton? = null
  private var micToggleButton: DyteMicToggleButton? = null
  private var confirmButton: DyteButton? = null
  private var cancelButton: DyteButton? = null

  private var meeting: DyteMobileClient? = null

  private var stageStatusUpdateListener = object : DyteStageEventListener {
    private var previousStageStatus: DyteStageStatus? = null

    override fun onStageStatusUpdated(stageStatus: DyteStageStatus) {
      super.onStageStatusUpdated(stageStatus)

      val shouldDismissDialog = previousStageStatus == DyteStageStatus.ACCEPTED_TO_JOIN_STAGE
        && (stageStatus == DyteStageStatus.OFF_STAGE || stageStatus == DyteStageStatus.REJECTED_TO_JOIN_STAGE)

      if (shouldDismissDialog) {
        dismiss()
      } else {
        previousStageStatus = stageStatus
      }
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

    cancelButton?.setOnClickListener {
      onCancelButtonClicked()
    }

    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
    meeting?.removeStageEventsListener(stageStatusUpdateListener)
    meeting = null
    super.onDetachedFromWindow()
  }

  fun activate(meeting: DyteMobileClient) {
    this.meeting = meeting
    participantPreviewTile?.activateForSelfPreview(meeting.localUser)
    micToggleButton?.activate(meeting)
    cameraToggleButton?.activate(meeting)
    this.meeting?.addStageEventsListener(stageStatusUpdateListener)
    confirmButton?.setOnClickListener {
      onConfirmButtonClicked()
    }
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

  private fun applyDesignTokens(designTokens: DyteDesignTokens) {
    participantPreviewTile?.applyDesignTokens(designTokens)
    cameraToggleButton?.applyDesignTokens(designTokens)
    micToggleButton?.applyDesignTokens(designTokens)
    confirmButton?.applyDesignTokens(designTokens)
    cancelButton?.applyDesignTokens(designTokens)
  }
}