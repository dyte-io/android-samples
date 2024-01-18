package io.dyte.activespeakerui.sample.widget

import android.content.Context
import android.util.AttributeSet
import dyte.io.uikit.view.DyteJoinStageDialog
import dyte.io.uikit.view.controlbarbuttons.DyteControlBarButton
import io.dyte.activespeakerui.sample.R
import io.dyte.activespeakerui.sample.utils.DyteUtils.canJoinStage
import io.dyte.activespeakerui.sample.utils.DyteUtils.canRequestToJoinStage
import io.dyte.activespeakerui.sample.widget.RaiseHandButtonState.AllowedToJoin
import io.dyte.activespeakerui.sample.widget.RaiseHandButtonState.CanRequestToJoin
import io.dyte.activespeakerui.sample.widget.RaiseHandButtonState.CancellingRequest
import io.dyte.activespeakerui.sample.widget.RaiseHandButtonState.JoiningStage
import io.dyte.activespeakerui.sample.widget.RaiseHandButtonState.LeavingStage
import io.dyte.activespeakerui.sample.widget.RaiseHandButtonState.NotAllowedToJoin
import io.dyte.activespeakerui.sample.widget.RaiseHandButtonState.OnStage
import io.dyte.activespeakerui.sample.widget.RaiseHandButtonState.RequestedToJoin
import io.dyte.activespeakerui.sample.widget.RaiseHandButtonState.RequestingToJoin
import io.dyte.core.DyteMobileClient
import io.dyte.core.controllers.DyteStageStatus
import io.dyte.core.listeners.DyteStageEventListener
import io.dyte.core.models.DyteSelfParticipant

class RaiseHandButton : DyteControlBarButton {
  private var meeting: DyteMobileClient? = null

  override val defaultIconResId: Int
    get() = R.drawable.ic_join_stage_24

  override val defaultLabelResId: Int
    get() = R.string.join_stage_label

  private var currentState: RaiseHandButtonState = NotAllowedToJoin
  private var previousStageStatus: DyteStageStatus = DyteStageStatus.OFF_STAGE

  private val selfStageStatusListener = object : DyteStageEventListener {
    override fun onStageStatusUpdated(stageStatus: DyteStageStatus) {
      super.onStageStatusUpdated(stageStatus)
      if (previousStageStatus == stageStatus) {
        return
      }

      meeting?.let {
        previousStageStatus = stageStatus
        val nextState = currentState.getNext(it.localUser, stageStatus)
        updateState(nextState)
      }
    }
  }

  var joinStageClickListener: () -> Unit = {}

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  fun activate(meeting: DyteMobileClient) {
    // Removing listeners as a precaution
    removeMeetingListeners(meeting)

    val initialState = getButtonStateForCurrentStageStatus(meeting)
    previousStageStatus = meeting.stage.stageStatus

    if (initialState == NotAllowedToJoin) {
      updateState(initialState)
      return
    }

    this.meeting = meeting
    meeting.addStageEventsListener(selfStageStatusListener)
    setOnClickListener { onClicked() }
    updateState(initialState)
  }

  fun deactivate() {
    super.setOnClickListener(null)
    meeting?.let {
      removeMeetingListeners(it)
      meeting = null
    }
  }

  override fun onDetachedFromWindow() {
    deactivate()
    super.onDetachedFromWindow()
  }

  fun refresh() {
    meeting?.let {
      val buttonState = getButtonStateForCurrentStageStatus(it)
      updateState(buttonState)
    }
  }

  private fun onClicked() {
    meeting?.let {
      when (it.stage.stageStatus) {
        DyteStageStatus.OFF_STAGE, DyteStageStatus.REJECTED_TO_JOIN_STAGE -> {
          if (it.localUser.canJoinStage()) {
            performJoinStage(it)
          } else if (it.localUser.canRequestToJoinStage()) {
            performRequestToJoinStage(it)
          } else {
            // do nothing
          }
        }

        DyteStageStatus.REQUESTED_TO_JOIN_STAGE -> {
          performCancelJoinStageRequest(it)
        }

        DyteStageStatus.ACCEPTED_TO_JOIN_STAGE -> {
          updateState(JoiningStage)
          joinStageClickListener.invoke()
        }

        DyteStageStatus.ON_STAGE -> {
          performLeaveStage(it)
        }
      }
    }
  }

  private fun performJoinStage(meeting: DyteMobileClient) {
    val previousButtonState = currentState
    updateState(JoiningStage)
    try {
      meeting.stage.join()
    } catch (e: Exception) {
      updateState(previousButtonState)
    }
  }

  private fun performRequestToJoinStage(meeting: DyteMobileClient) {
    updateState(RequestingToJoin)
    try {
      meeting.stage.requestAccess()
    } catch (e: Exception) {
      updateState(CanRequestToJoin)
    }
  }

  private fun showJoinStageConfirmation(meeting: DyteMobileClient) {
    val dyteJoinStage =
      DyteJoinStageDialog(context)
    // dyteJoinWebinarStageConfirmationDialog.setOnShowListener {
    //   dyteJoinWebinarStageConfirmationDialog.applyDesignTokens(designTokens)
    // }

    // Roll-back to previous state i.e. AllowedToJoin when user cancels the dialog
    dyteJoinStage.setOnCancelListener {
      updateState(AllowedToJoin)
    }
    dyteJoinStage.show()
    dyteJoinStage.activate(meeting)
  }

  private fun performCancelJoinStageRequest(meeting: DyteMobileClient) {
    updateState(CancellingRequest)
    try {
      meeting.stage.cancelRequestAccess()
    } catch (e: Exception) {
      updateState(RequestedToJoin)
    }
  }

  private fun performLeaveStage(meeting: DyteMobileClient) {
    updateState(LeavingStage)
    try {
      meeting.stage.leave()
    } catch (e: Exception) {
      updateState(OnStage)
    }
  }

  private fun updateState(state: RaiseHandButtonState) {
    when (state) {
      NotAllowedToJoin -> {
        setProcessingState(false)
        isEnabled = false
        setIconDrawable(null)
        labelTextView.text =
          context.getText(R.string.join_stage_label)
      }

      CanRequestToJoin -> {
        setProcessingState(false)
        iconImageView.setImageResource(R.drawable.ic_raise_hand_32)
        labelTextView.text =
          context.getText(R.string.can_request_to_join_label)
      }

      RequestingToJoin -> {
        setProcessingState(true)
        labelTextView.text =
          context.getText(R.string.requesting_to_join_label)
      }

      RequestedToJoin -> {
        setProcessingState(false)
        iconImageView.setImageResource(R.drawable.ic_cancel_request_24)
        labelTextView.text =
          context.getText(R.string.requested_to_join_label)
      }

      CancellingRequest -> {
        setProcessingState(false)
        labelTextView.text =
          context.getText(R.string.cancelling_join_request_label)
      }

      AllowedToJoin -> {
        setProcessingState(false)
        iconImageView.setImageResource(R.drawable.ic_join_stage_24)
        labelTextView.text =
          context.getText(R.string.join_stage_label)
      }

      JoiningStage -> {
        setProcessingState(false)
        labelTextView.text =
          context.getText(R.string.joining_stage_label)
      }

      OnStage -> {
        setProcessingState(false)
        iconImageView.setImageResource(R.drawable.ic_leave_stage_24)
        labelTextView.text =
          context.getText(R.string.leaving_stage_label)
      }

      LeavingStage -> {
        setProcessingState(true)
        labelTextView.text =
          context.getText(R.string.leaving_stage_label)
      }
    }

    currentState = state
  }

  private fun removeMeetingListeners(meeting: DyteMobileClient) {
    meeting.removeStageEventsListener(selfStageStatusListener)
  }

  private fun getButtonStateForCurrentStageStatus(meeting: DyteMobileClient): RaiseHandButtonState {
    return when (meeting.stage.stageStatus) {
      DyteStageStatus.OFF_STAGE, DyteStageStatus.REJECTED_TO_JOIN_STAGE -> {
        getButtonStateForOffStageSelfParticipant(meeting.localUser)
      }

      DyteStageStatus.REQUESTED_TO_JOIN_STAGE -> {
        RequestedToJoin
      }

      DyteStageStatus.ACCEPTED_TO_JOIN_STAGE -> {
        AllowedToJoin
      }

      DyteStageStatus.ON_STAGE -> {
        OnStage
      }
    }
  }

  companion object {
    private fun RaiseHandButtonState.getNext(
      localUser: DyteSelfParticipant,
      stageStatus: DyteStageStatus
    ): RaiseHandButtonState {
      return when (this) {
        AllowedToJoin -> {
          if (stageStatus == DyteStageStatus.ON_STAGE) {
            OnStage
          } else if (stageStatus == DyteStageStatus.OFF_STAGE) {
            getButtonStateForOffStageSelfParticipant(localUser)
          } else {
            JoiningStage
          }
        }

        CanRequestToJoin -> {
          if (stageStatus == DyteStageStatus.ACCEPTED_TO_JOIN_STAGE) {
            AllowedToJoin
          } else {
            RequestingToJoin
          }
        }

        CancellingRequest -> {
          CanRequestToJoin
        }

        JoiningStage -> {
          OnStage
        }

        LeavingStage -> {
          if (stageStatus == DyteStageStatus.ACCEPTED_TO_JOIN_STAGE) {
            AllowedToJoin
          } else {
            getButtonStateForOffStageSelfParticipant(localUser)
          }
        }

        NotAllowedToJoin -> {
          if (stageStatus == DyteStageStatus.ACCEPTED_TO_JOIN_STAGE) {
            AllowedToJoin
          } else {
            NotAllowedToJoin
          }
        }

        OnStage -> {
          if (stageStatus == DyteStageStatus.ACCEPTED_TO_JOIN_STAGE) {
            AllowedToJoin
          } else if (stageStatus == DyteStageStatus.OFF_STAGE) {
            getButtonStateForOffStageSelfParticipant(localUser)
          } else {
            LeavingStage
          }
        }

        RequestedToJoin -> {
          if (stageStatus == DyteStageStatus.ACCEPTED_TO_JOIN_STAGE) {
            AllowedToJoin
          } else {
            CanRequestToJoin
          }
        }

        RequestingToJoin -> {
          RequestedToJoin
        }
      }
    }

    private fun getButtonStateForOffStageSelfParticipant(
      localUser: DyteSelfParticipant
    ): RaiseHandButtonState {
      return if (localUser.canJoinStage()) {
        AllowedToJoin
      } else if (localUser.canRequestToJoinStage()) {
        CanRequestToJoin
      } else {
        NotAllowedToJoin
      }
    }
  }
}

sealed class RaiseHandButtonState {
  data object NotAllowedToJoin : RaiseHandButtonState()

  data object CanRequestToJoin : RaiseHandButtonState()

  data object RequestingToJoin : RaiseHandButtonState()

  data object RequestedToJoin : RaiseHandButtonState()

  data object CancellingRequest : RaiseHandButtonState()

  data object AllowedToJoin : RaiseHandButtonState()

  data object JoiningStage : RaiseHandButtonState()

  data object OnStage : RaiseHandButtonState()

  data object LeavingStage : RaiseHandButtonState()
}