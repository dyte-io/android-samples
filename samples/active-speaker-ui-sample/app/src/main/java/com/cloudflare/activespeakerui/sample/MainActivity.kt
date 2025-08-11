package com.cloudflare.activespeakerui.sample

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cloudflare.activespeakerui.sample.utils.DialogUtils.setWidthToScreenPercentage
import com.cloudflare.activespeakerui.sample.utils.ViewUtils.getOrientation
import com.cloudflare.activespeakerui.sample.views.LeaveMeetingDialog
import com.cloudflare.realtimekit.models.RtkMeetingInfo
import com.cloudflare.realtimekit.ui.RealtimeKitNotificationConfig
import com.cloudflare.realtimekit.ui.RealtimeKitUIBuilder
import com.cloudflare.realtimekit.ui.RealtimeKitUIConfig
import com.cloudflare.realtimekit.ui.RealtimeKitUIInfo
import com.cloudflare.realtimekit.ui.RealtimeKitUINotificationsConfig
import com.cloudflare.realtimekit.ui.screens.RtkMeetingViewModel
import com.cloudflare.realtimekit.ui.screens.setup.RtkSetupFragment
import com.cloudflare.realtimekit.ui.screens.webinar.RtkWebinarFragment
import com.cloudflare.realtimekit.ui.utils.Utils.showToast
import com.cloudflare.realtimekit.ui.view.RtkErrorView
import com.cloudflare.realtimekit.ui.view.RtkLoaderView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
  private lateinit var container: FrameLayout

  private lateinit var viewModel: RtkMeetingViewModel

  private lateinit var meetingRoomProgressBar: ProgressBar

  private val onBackPressedCallback =
    object : OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        this@MainActivity.handleOnBackPressed()
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val meetingInfo =
      RtkMeetingInfo(authToken = MeetingConfig.AUTH_TOKEN, baseDomain = MeetingConfig.BASE_URL)

    RealtimeKitUIBuilder.build(
      RealtimeKitUIInfo(
        activity = this,
        rtkMeetingInfo = meetingInfo,
        uiKitConfig =
          RealtimeKitUIConfig(
            RealtimeKitUINotificationsConfig(
              participantJoinConfig = RealtimeKitNotificationConfig(false),
              participantLeaveConfig = RealtimeKitNotificationConfig(false),
            )
          ),
      )
    )

    container = findViewById(R.id.clContainer)
    meetingRoomProgressBar = findViewById(R.id.progressbar_meeting_room)

    viewModel = ViewModelProvider(this)[RtkMeetingViewModel::class.java]
    viewModel.stateLiveData.observe(this) { meetingState ->
      when (meetingState) {
        is RtkMeetingViewModel.RtkMeetingState.Error -> {
          showError(meetingState.errorMessage)
        }

        RtkMeetingViewModel.RtkMeetingState.Loading -> {
          showLoading()
        }

        RtkMeetingViewModel.RtkMeetingState.Setup -> {
          setChatLimits()
          showSetupScreen()
        }

        RtkMeetingViewModel.RtkMeetingState.Removed,
        RtkMeetingViewModel.RtkMeetingState.Ended -> {
          /*
           * Note: calling meeting.release is currently compulsory when closing this Activity,
           * except when leaving the meeting by tapping the leave button or via the leave dialog.
           * */
          viewModel.meeting.release(onSuccess = { finish() }, onFailure = { finish() })
        }

        RtkMeetingViewModel.RtkMeetingState.Left -> {
          finish()
        }

        RtkMeetingViewModel.RtkMeetingState.Webinar -> {
          showWebinarFragment()
        }

        else -> {
          println("MainActivity::onCreate::cant process state ${meetingState.javaClass.simpleName}")
        }
      }
    }

    lifecycleScope.launch {
      viewModel.meetingNotificationLiveData.collectLatest { showToast(container, it) }
    }

    viewModel.meetingRoomProcessing.observe(this) { processing ->
      meetingRoomProgressBar.isVisible = processing
    }

    onBackPressedDispatcher.addCallback(onBackPressedCallback)

    showLoading()
    viewModel.start()
  }

  private fun showError(e: String) {
    Log.d(TAG, "showError::$e")
    val errorView = RtkErrorView(this)
    container.removeAllViews()
    container.addView(errorView)
    errorView.refresh(e) {
      viewModel.meeting.release(onSuccess = { finish() }, onFailure = { finish() })
    }
  }

  private fun showLoading() {
    Log.d(TAG, "showLoading")
    val loaderView = RtkLoaderView(this)
    container.removeAllViews()
    container.addView(loaderView)
  }

  private fun showSetupScreen() {
    Log.d(TAG, "showSetupScreen")
    // Avoiding addition of DyteSetupFragment again on Activity recreation
    if (supportFragmentManager.fragments.lastOrNull() is RtkSetupFragment) {
      return
    }

    container.removeAllViews()
    supportFragmentManager.beginTransaction().replace(R.id.clContainer, RtkSetupFragment()).commit()
  }

  private fun showWebinarFragment() {
    Log.d(TAG, "showWebinarFragment")

    container.removeAllViews()
    if (getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
      Log.d(TAG, "showWebinarFragment::dyte")
      supportFragmentManager
        .beginTransaction()
        .replace(R.id.clContainer, RtkWebinarFragment())
        .commit()
    } else {
      Log.d(TAG, "showWebinarFragment::activespeaker")
      supportFragmentManager
        .beginTransaction()
        .replace(R.id.clContainer, ActiveSpeakerWebinarFragment())
        .commit()
    }
  }

  private fun handleOnBackPressed() {
    when (viewModel.stateLiveData.value) {
      RtkMeetingViewModel.RtkMeetingState.Webinar -> {
        showLeaveClassDialog()
      }

      RtkMeetingViewModel.RtkMeetingState.Setup -> {
        /*
         * Client can also implement their custom logic here.
         * Note: calling meeting.release is currently compulsory when closing this Activity,
         * except when leaving the meeting by tapping the leave button or via the leave dialog.
         * */
        viewModel.meeting.release(onSuccess = { finish() }, onFailure = { finish() })
      }

      else -> {
        /*
         * Note: calling meeting.release is currently compulsory when closing this Activity,
         * except when leaving the meeting by tapping the leave button or via the leave dialog.
         * */
        viewModel.meeting.release(onSuccess = { finish() }, onFailure = { finish() })
      }
    }
  }

  private fun showLeaveClassDialog() {
    val leaveClassDialog =
      LeaveMeetingDialog(
        this,
        meeting = viewModel.meeting,
        designTokens = RealtimeKitUIBuilder.realtimeKitUI.designTokens,
      )
    leaveClassDialog.show()

    if (getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
      leaveClassDialog.setWidthToScreenPercentage(0.90f)
    } else {
      leaveClassDialog.setWidthToScreenPercentage(0.60f)
    }
  }

  private fun setChatLimits() {
    val meeting = viewModel.meeting
    meeting.chat.setCharacterLimit(MeetingConfig.CHAT_CHARACTER_LIMIT)
    meeting.chat.setMessageRateLimit(
      maxMessages = MeetingConfig.CHAT_MAX_MESSAGES,
      intervalInSeconds = MeetingConfig.CHAT_MESSAGE_INTERVAL_SECONDS,
    )
  }

  companion object {
    private const val TAG = "MainActivity"
  }
}
