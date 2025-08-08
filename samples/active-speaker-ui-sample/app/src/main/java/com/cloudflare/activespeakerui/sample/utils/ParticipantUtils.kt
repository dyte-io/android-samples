package com.cloudflare.activespeakerui.sample.utils

import com.cloudflare.realtimekit.media.MediaPermission
import com.cloudflare.realtimekit.self.RtkSelfParticipant

internal object ParticipantUtils {
  fun RtkSelfParticipant.canJoinStage(): Boolean {
    val videoProductionPermission = permissions.media.video.permission
    val audioProductionPermission = permissions.media.audioPermission

    return videoProductionPermission == MediaPermission.ALLOWED ||
        audioProductionPermission == MediaPermission.ALLOWED
  }

  fun RtkSelfParticipant.canRequestToJoinStage(): Boolean {
    val videoProductionPermission = permissions.media.video.permission
    val audioProductionPermission = permissions.media.audioPermission

    return videoProductionPermission == MediaPermission.CAN_REQUEST ||
        audioProductionPermission == MediaPermission.CAN_REQUEST
  }
}
