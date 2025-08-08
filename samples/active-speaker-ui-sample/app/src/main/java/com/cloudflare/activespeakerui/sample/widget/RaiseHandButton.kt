package com.cloudflare.activespeakerui.sample.widget

import android.content.Context
import android.util.AttributeSet
import com.cloudflare.activespeakerui.sample.R
import com.cloudflare.activespeakerui.sample.utils.ParticipantUtils.canJoinStage
import com.cloudflare.activespeakerui.sample.utils.ParticipantUtils.canRequestToJoinStage
import com.cloudflare.activespeakerui.sample.widget.RaiseHandButtonState.AllowedToJoin
import com.cloudflare.activespeakerui.sample.widget.RaiseHandButtonState.CanRequestToJoin
import com.cloudflare.activespeakerui.sample.widget.RaiseHandButtonState.CancellingRequest
import com.cloudflare.activespeakerui.sample.widget.RaiseHandButtonState.JoiningStage
import com.cloudflare.activespeakerui.sample.widget.RaiseHandButtonState.LeavingStage
import com.cloudflare.activespeakerui.sample.widget.RaiseHandButtonState.NotAllowedToJoin
import com.cloudflare.activespeakerui.sample.widget.RaiseHandButtonState.OnStage
import com.cloudflare.activespeakerui.sample.widget.RaiseHandButtonState.RequestedToJoin
import com.cloudflare.activespeakerui.sample.widget.RaiseHandButtonState.RequestingToJoin
import com.cloudflare.realtimekit.RealtimeKitClient
import com.cloudflare.realtimekit.self.RtkSelfParticipant
import com.cloudflare.realtimekit.stage.RtkStageEventListener
import com.cloudflare.realtimekit.stage.StageStatus
import com.cloudflare.realtimekit.ui.view.RtkJoinStageDialog
import com.cloudflare.realtimekit.ui.view.controlbarbuttons.RtkControlBarButton

class RaiseHandButton : RtkControlBarButton {
  private var meeting: RealtimeKitClient? = null

  override val defaultIconResId: Int
    get() = R.drawable.ic_join_stage_24

  override val defaultLabelResId: Int
    get() = R.string.join_stage_label

  private var currentState: RaiseHandButtonState = NotAllowedToJoin

  private val selfStageStatusListener =
      object : RtkStageEventListener {
        override fun onStageStatusUpdated(oldStatus: StageStatus, newStatus: StageStatus) {
          super.onStageStatusUpdated(oldStatus, newStatus)
          if (oldStatus == newStatus) return

          meeting?.let {
            val nextState = currentState.getNext(it.localUser, newStatus)
            updateState(nextState)
          }
        }
      }

  var joinStageClickListener: () -> Unit = {}

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

  constructor(
      context: Context,
      attrs: AttributeSet?,
      defStyleAttr: Int
  ) : super(context, attrs, defStyleAttr)

  fun activate(meeting: RealtimeKitClient) {
    // Removing listeners as a precaution
    removeMeetingListeners(meeting)

    val initialState = getButtonStateForCurrentStageStatus(meeting)
    if (initialState == NotAllowedToJoin) {
      updateState(initialState)
      return
    }

    this.meeting = meeting
    meeting.addStageEventListener(selfStageStatusListener)
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
        StageStatus.OFF_STAGE -> {
          if (it.localUser.canJoinStage()) {
            performJoinStage(it)
          } else if (it.localUser.canRequestToJoinStage()) {
            performRequestToJoinStage(it)
          } else {
            // do nothing
          }
        }

        StageStatus.REQUESTED_TO_JOIN_STAGE -> {
          performCancelJoinStageRequest(it)
        }

        StageStatus.ACCEPTED_TO_JOIN_STAGE -> {
          updateState(JoiningStage)
          joinStageClickListener.invoke()
        }

        StageStatus.ON_STAGE -> {
          performLeaveStage(it)
        }
      }
    }
  }

  private fun performJoinStage(meeting: RealtimeKitClient) {
    val previousButtonState = currentState
    updateState(JoiningStage)
    try {
      meeting.stage.join()
    } catch (e: Exception) {
      updateState(previousButtonState)
    }
  }

  private fun performRequestToJoinStage(meeting: RealtimeKitClient) {
    updateState(RequestingToJoin)
    try {
      meeting.stage.requestAccess()
    } catch (e: Exception) {
      updateState(CanRequestToJoin)
    }
  }

  private fun showJoinStageConfirmation(meeting: RealtimeKitClient) {
    val dyteJoinStage = RtkJoinStageDialog(context)
    // dyteJoinWebinarStageConfirmationDialog.setOnShowListener {
    //   dyteJoinWebinarStageConfirmationDialog.applyDesignTokens(designTokens)
    // }

    // Roll-back to previous state i.e. AllowedToJoin when user cancels the dialog
    dyteJoinStage.setOnCancelListener { updateState(AllowedToJoin) }
    dyteJoinStage.show()
    dyteJoinStage.activate(meeting)
  }

  private fun performCancelJoinStageRequest(meeting: RealtimeKitClient) {
    updateState(CancellingRequest)
    try {
      meeting.stage.cancelRequestAccess()
    } catch (e: Exception) {
      updateState(RequestedToJoin)
    }
  }

  private fun performLeaveStage(meeting: RealtimeKitClient) {
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
        labelTextView.text = context.getText(R.string.join_stage_label)
      }

      CanRequestToJoin -> {
        setProcessingState(false)
        iconImageView.setImageResource(R.drawable.ic_raise_hand_32)
        labelTextView.text = context.getText(R.string.can_request_to_join_label)
      }

      RequestingToJoin -> {
        setProcessingState(true)
        labelTextView.text = context.getText(R.string.requesting_to_join_label)
      }

      RequestedToJoin -> {
        setProcessingState(false)
        iconImageView.setImageResource(R.drawable.ic_cancel_request_24)
        labelTextView.text = context.getText(R.string.requested_to_join_label)
      }

      CancellingRequest -> {
        setProcessingState(false)
        labelTextView.text = context.getText(R.string.cancelling_join_request_label)
      }

      AllowedToJoin -> {
        setProcessingState(false)
        iconImageView.setImageResource(R.drawable.ic_join_stage_24)
        labelTextView.text = context.getText(R.string.join_stage_label)
      }

      JoiningStage -> {
        setProcessingState(false)
        labelTextView.text = context.getText(R.string.joining_stage_label)
      }

      OnStage -> {
        setProcessingState(false)
        iconImageView.setImageResource(R.drawable.ic_leave_stage_24)
        labelTextView.text = context.getText(R.string.leaving_stage_label)
      }

      LeavingStage -> {
        setProcessingState(true)
        labelTextView.text = context.getText(R.string.leaving_stage_label)
      }
    }

    currentState = state
  }

  private fun removeMeetingListeners(meeting: RealtimeKitClient) {
    meeting.removeStageEventListener(selfStageStatusListener)
  }

  private fun getButtonStateForCurrentStageStatus(
      meeting: RealtimeKitClient
  ): RaiseHandButtonState {
    return when (meeting.stage.stageStatus) {
      StageStatus.OFF_STAGE -> {
        getButtonStateForOffStageSelfParticipant(meeting.localUser)
      }

      StageStatus.REQUESTED_TO_JOIN_STAGE -> {
        RequestedToJoin
      }

      StageStatus.ACCEPTED_TO_JOIN_STAGE -> {
        AllowedToJoin
      }

      StageStatus.ON_STAGE -> {
        OnStage
      }
    }
  }

  companion object {
    private fun RaiseHandButtonState.getNext(
        localUser: RtkSelfParticipant,
        stageStatus: StageStatus
    ): RaiseHandButtonState {
      return when (this) {
        AllowedToJoin -> {
          if (stageStatus == StageStatus.ON_STAGE) {
            OnStage
          } else if (stageStatus == StageStatus.OFF_STAGE) {
            getButtonStateForOffStageSelfParticipant(localUser)
          } else {
            JoiningStage
          }
        }

        CanRequestToJoin -> {
          if (stageStatus == StageStatus.ACCEPTED_TO_JOIN_STAGE) {
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
          if (stageStatus == StageStatus.ACCEPTED_TO_JOIN_STAGE) {
            AllowedToJoin
          } else {
            getButtonStateForOffStageSelfParticipant(localUser)
          }
        }

        NotAllowedToJoin -> {
          if (stageStatus == StageStatus.ACCEPTED_TO_JOIN_STAGE) {
            AllowedToJoin
          } else {
            NotAllowedToJoin
          }
        }

        OnStage -> {
          if (stageStatus == StageStatus.ACCEPTED_TO_JOIN_STAGE) {
            AllowedToJoin
          } else if (stageStatus == StageStatus.OFF_STAGE) {
            getButtonStateForOffStageSelfParticipant(localUser)
          } else {
            LeavingStage
          }
        }

        RequestedToJoin -> {
          if (stageStatus == StageStatus.ACCEPTED_TO_JOIN_STAGE) {
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
        localUser: RtkSelfParticipant
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
