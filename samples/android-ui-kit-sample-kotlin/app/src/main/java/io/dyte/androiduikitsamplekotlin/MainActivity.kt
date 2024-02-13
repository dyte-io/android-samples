package io.dyte.androiduikitsamplekotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import dyte.io.uikit.DyteUIKitBuilder
import dyte.io.uikit.DyteUIKitInfo
import io.dyte.core.models.DyteMeetingInfoV2

class MainActivity : AppCompatActivity() {
  private lateinit var btnStartMeeting: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    btnStartMeeting = findViewById(R.id.btnStartMeeting)
    btnStartMeeting.setOnClickListener {
      startDyteMeeting(MeetingConfig.AUTH_TOKEN)
    }
  }

  private fun startDyteMeeting(authToken: String) {
    val meetingInfo = DyteMeetingInfoV2(
      authToken = authToken
    )
    val dyteUIKitInfo = DyteUIKitInfo(
      activity = this,
      dyteMeetingInfo = meetingInfo
    )
    val dyteUIKit = DyteUIKitBuilder.build(dyteUIKitInfo)
    dyteUIKit.startMeeting()
  }
}