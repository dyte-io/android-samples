package io.dyte.activespeakerui.sample

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
import dyte.io.uikit.DyteNotificationConfig
import dyte.io.uikit.DyteUIKitBuilder
import dyte.io.uikit.DyteUIKitConfig
import dyte.io.uikit.DyteUIKitInfo
import dyte.io.uikit.DyteUiKitNotificationsConfig
import dyte.io.uikit.screens.DyteMeetingViewModel
import dyte.io.uikit.screens.setup.DyteSetupFragment
import dyte.io.uikit.screens.webinar.DyteWebinarFragment
import dyte.io.uikit.utils.Utils.showToast
import dyte.io.uikit.view.DyteErrorView
import dyte.io.uikit.view.DyteLoaderView
import io.dyte.activespeakerui.sample.utils.DialogUtils.setWidthToScreenPercentage
import io.dyte.activespeakerui.sample.utils.ViewUtils.getOrientation
import io.dyte.activespeakerui.sample.views.LeaveMeetingDialog
import io.dyte.core.models.DyteMeetingInfoV2
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var container: FrameLayout

    private lateinit var viewModel: DyteMeetingViewModel

    private lateinit var meetingRoomProgressBar: ProgressBar

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            this@MainActivity.handleOnBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val meetingInfo = DyteMeetingInfoV2(authToken = MeetingConfig.AUTH_TOKEN, baseUrl = MeetingConfig.BASE_URL)
        DyteUIKitBuilder.build(
            DyteUIKitInfo(
                activity = this,
                dyteMeetingInfo = meetingInfo,
                uiKitConfig = DyteUIKitConfig(
                    DyteUiKitNotificationsConfig(
                        participantJoinConfig = DyteNotificationConfig(false),
                        participantLeaveConfig = DyteNotificationConfig(false)
                    )
                )
            )
        )

        container = findViewById(R.id.clContainer)
        meetingRoomProgressBar = findViewById(R.id.progressbar_meeting_room)

        viewModel = ViewModelProvider(this)[DyteMeetingViewModel::class.java]
        viewModel.stateLiveData.observe(this) { meetingState ->
            when (meetingState) {
                is DyteMeetingViewModel.DyteMeetingState.Error -> {
                    showError(meetingState.exception)
                }

                DyteMeetingViewModel.DyteMeetingState.Loading -> {
                    showLoading()
                }

                DyteMeetingViewModel.DyteMeetingState.Setup -> {
                    showSetupScreen()
                }

                DyteMeetingViewModel.DyteMeetingState.Removed -> {
                    /*
                    * Note: calling meeting.release is currently compulsory when closing this Activity,
                    * except when leaving the meeting by tapping the leave button or via the leave dialog.
                    * */
                    viewModel.meeting.release(
                        onReleaseSuccess = { finish() },
                        onReleaseFailed = { finish() }
                    )
                }

                DyteMeetingViewModel.DyteMeetingState.Left -> {
                    finish()
                }

                DyteMeetingViewModel.DyteMeetingState.Webinar -> {
                    showWebinarFragment()
                }

                else -> {
                    println("MainActivity::onCreate::cant process state ${meetingState.javaClass.simpleName}")
                }
            }
        }

        lifecycleScope.launch {
            viewModel.meetingNotificationLiveData
                .collectLatest {
                    showToast(container, it)
                }
        }

        viewModel.meetingRoomProcessing.observe(this) { processing ->
            meetingRoomProgressBar.isVisible = processing
        }

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        showLoading()
        viewModel.start()
    }

    private fun showError(e: Exception) {
        Log.d(TAG, "showError::${e.message}")
        val errorView = DyteErrorView(this)
        container.removeAllViews()
        container.addView(errorView)
        errorView.refresh(e) {
            finish()
        }
    }

    private fun showLoading() {
        Log.d(TAG, "showLoading")
        val loaderView = DyteLoaderView(this)
        container.removeAllViews()
        container.addView(loaderView)
    }

    private fun showSetupScreen() {
        Log.d(TAG, "showSetupScreen")
        // Avoiding addition of DyteSetupFragment again on Activity recreation
        if (supportFragmentManager.fragments.lastOrNull() is DyteSetupFragment) {
            return
        }

        container.removeAllViews()
        supportFragmentManager.beginTransaction()
            .replace(R.id.clContainer, DyteSetupFragment())
            .commit()
    }

    private fun showWebinarFragment() {
        Log.d(TAG, "showWebinarFragment")

        container.removeAllViews()
        if (getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "showWebinarFragment::dyte")
            supportFragmentManager.beginTransaction()
                .replace(R.id.clContainer, DyteWebinarFragment())
                .commit()
        } else {
            Log.d(TAG, "showWebinarFragment::activespeaker")
            supportFragmentManager.beginTransaction()
                .replace(R.id.clContainer, ActiveSpeakerWebinarFragment())
                .commit()
        }
    }

    private fun handleOnBackPressed() {
        when (viewModel.stateLiveData.value) {
            DyteMeetingViewModel.DyteMeetingState.Webinar -> {
                showLeaveClassDialog()
            }

            DyteMeetingViewModel.DyteMeetingState.Setup -> {
                /*
                * Client can also implement their custom logic here.
                * Note: calling meeting.release is currently compulsory when closing this Activity,
                * except when leaving the meeting by tapping the leave button or via the leave dialog.
                * */
                viewModel.meeting.release(
                    onReleaseSuccess = { finish() },
                    onReleaseFailed = { finish() }
                )
            }

            else -> {
                /*
                * Note: calling meeting.release is currently compulsory when closing this Activity,
                * except when leaving the meeting by tapping the leave button or via the leave dialog.
                * */
                viewModel.meeting.release(
                    onReleaseSuccess = { finish() },
                    onReleaseFailed = { finish() }
                )
            }
        }
    }

    private fun showLeaveClassDialog() {
        val leaveClassDialog = LeaveMeetingDialog(
            this,
            meeting = viewModel.meeting,
            designTokens = DyteUIKitBuilder.dyteUIKit.designTokens
        )
        leaveClassDialog.show()

        if (getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            leaveClassDialog.setWidthToScreenPercentage(0.90f)
        } else {
            leaveClassDialog.setWidthToScreenPercentage(0.60f)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}