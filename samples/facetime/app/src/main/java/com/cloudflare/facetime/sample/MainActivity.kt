package com.cloudflare.facetime.sample

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cloudflare.realtimekit.models.RtkMeetingInfo
import com.cloudflare.realtimekit.ui.RealtimeKitUIBuilder
import com.cloudflare.realtimekit.ui.RealtimeKitUIInfo
import com.cloudflare.realtimekit.ui.screens.RtkMeetingViewModel
import com.cloudflare.realtimekit.ui.utils.ViewUtils.gone
import com.cloudflare.realtimekit.ui.utils.ViewUtils.visible

class MainActivity : AppCompatActivity() {
  private val viewModel: RtkMeetingViewModel by lazy {
    ViewModelProvider(this)[RtkMeetingViewModel::class.java]
  }

  private val buttonStartMeeting: Button by lazy { findViewById(R.id.btn_start_meeting) }
  private val loader: ProgressBar by lazy { findViewById(R.id.progressbar_meeting_room) }
  private val clDataContainer: FrameLayout by lazy { findViewById(R.id.clDataContainer) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val meetingInfo =
      RtkMeetingInfo(baseDomain = MeetingConfig.BASE_DOMAIN, authToken = MeetingConfig.AUTH_TOKEN)
    RealtimeKitUIBuilder.build(RealtimeKitUIInfo(activity = this, rtkMeetingInfo = meetingInfo))

    viewModel.stateLiveData.observe(this) {
      when (it) {
        RtkMeetingViewModel.RtkMeetingState.Loading -> {
          showLoading()
        }
        RtkMeetingViewModel.RtkMeetingState.Setup -> {
          viewModel.meeting.joinRoom(onSuccess = {}, onFailure = {})
        }
        RtkMeetingViewModel.RtkMeetingState.GroupCall -> {
          showMeeting()
        }
        is RtkMeetingViewModel.RtkMeetingState.Error -> {
          showError(it.errorMessage)
        }
        RtkMeetingViewModel.RtkMeetingState.Left -> {
          finish()
        }
        RtkMeetingViewModel.RtkMeetingState.Removed -> {
          showError("Removed from meeting!")
        }
        else -> {
          // todo : implement remaining states
        }
      }
    }

    buttonStartMeeting.setOnClickListener { viewModel.start() }
  }

  private fun showMeeting() {
    clDataContainer.gone()
    buttonStartMeeting.gone()
    loader.gone()
    clDataContainer.visible()
    clDataContainer.removeAllViews()

    val meetingView = MeetingView(this)
    clDataContainer.addView(meetingView)
    meetingView.activate(viewModel.meeting, supportFragmentManager)
  }

  private fun showLoading() {
    clDataContainer.gone()
    buttonStartMeeting.gone()
    loader.visible()
  }

  private fun showError(exception: String) {
    clDataContainer.gone()
    loader.gone()
    buttonStartMeeting.gone()
    Toast.makeText(this, exception, Toast.LENGTH_LONG).show()
    finish()
  }
}
