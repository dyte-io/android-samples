package io.dyte.androiduikitsamplekotlin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import dyte.io.uikit.DyteUIKitBuilder
import dyte.io.uikit.DyteUIKitInfo
import io.dyte.core.listeners.DyteMeetingRoomEventsListener
import io.dyte.core.models.DyteMeetingInfoV2
import io.dyte.core.observability.DyteLogger
import io.dyte.virtual_background.VirtualBackground
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
  private lateinit var btnStartMeeting: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    btnStartMeeting = findViewById(R.id.btnStartMeeting)
    btnStartMeeting.setOnClickListener {
      startDyteMeeting(Constants.AUTH_TOKEN, Constants.BASE_URL)
    }
  }

  private fun startDyteMeeting(authToken: String, baseUrl: String) {
    val meetingInfo = DyteMeetingInfoV2(
      authToken = authToken,
      baseUrl = baseUrl
    )
    val dyteUIKitInfo = DyteUIKitInfo(
      activity = this,
      dyteMeetingInfo = meetingInfo
    )
    val dyteUIKit = DyteUIKitBuilder.build(dyteUIKitInfo)
    dyteUIKit.meeting.addMeetingRoomEventsListener(object : DyteMeetingRoomEventsListener {
      override fun onMeetingRoomJoinCompleted() {
        super.onMeetingRoomJoinCompleted()
        println("MainActivity::onMeetingRoomJoinCompleted::")
        val byteArrayOutputStream = ByteArrayOutputStream()
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
        val imageString: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        dyteUIKit.meeting.localUser.addVideoMiddleware(VirtualBackground(imageString))
      }
    })
    dyteUIKit.startMeeting()
  }
}