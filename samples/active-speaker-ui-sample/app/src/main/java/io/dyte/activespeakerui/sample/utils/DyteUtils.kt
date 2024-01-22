package io.dyte.activespeakerui.sample.utils

import io.dyte.core.models.DyteMediaPermission
import io.dyte.core.models.DyteSelfParticipant

internal object DyteUtils {
  fun DyteSelfParticipant.canJoinStage(): Boolean {
    val videoProductionPermission = permissions.media.video.permission
    val audioProductionPermission = permissions.media.audioPermission

    return (videoProductionPermission == DyteMediaPermission.ALLOWED ||
      audioProductionPermission == DyteMediaPermission.ALLOWED)
  }

  fun DyteSelfParticipant.canRequestToJoinStage(): Boolean {
    val videoProductionPermission = permissions.media.video.permission
    val audioProductionPermission = permissions.media.audioPermission

    return (videoProductionPermission == DyteMediaPermission.CAN_REQUEST ||
      audioProductionPermission == DyteMediaPermission.CAN_REQUEST)
  }
}