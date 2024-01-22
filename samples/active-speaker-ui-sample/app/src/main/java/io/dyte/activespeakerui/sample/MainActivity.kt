package io.dyte.activespeakerui.sample

import android.content.res.Configuration
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dyte.io.uikit.DyteUIKitBuilder
import dyte.io.uikit.DyteUIKitInfo
import dyte.io.uikit.screens.DyteMeetingViewModel
import dyte.io.uikit.screens.setup.DyteSetupFragment
import dyte.io.uikit.screens.webinar.DyteWebinarFragment
import dyte.io.uikit.utils.Utils.showToast
import dyte.io.uikit.view.DyteErrorView
import dyte.io.uikit.view.DyteJoinStageDialog
import dyte.io.uikit.view.DyteLoaderView
import io.dyte.core.models.DyteMeetingInfoV2
import io.dyte.core.observability.DyteLogger
import io.dyte.activespeakerui.sample.utils.ViewUtils.getOrientation
import io.dyte.activespeakerui.sample.utils.DialogUtils.setWidthToScreenPercentage
import io.dyte.activespeakerui.sample.views.LeaveMeetingDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

const val AUTH_TOKEN = "<PASTE_AUTH_TOKEN_HERE>"

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

        val meetingInfo = DyteMeetingInfoV2(authToken = AUTH_TOKEN)
        DyteUIKitBuilder.build(DyteUIKitInfo(this, meetingInfo))

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
                    finish()
                }

                DyteMeetingViewModel.DyteMeetingState.Left -> {
                    finish()
                }

                DyteMeetingViewModel.DyteMeetingState.Webinar -> {
                    showWebinarFragment()
                }

                else -> {
                    DyteLogger.info("MainActivity::onCreate::cant process state ${meetingState.javaClass.simpleName}")
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

        viewModel.selfStageLiveData.observe(this) { shouldShowDialog ->
            if (shouldShowDialog) {
                val stageView = DyteJoinStageDialog(this)
                stageView.show()
                stageView.activate(viewModel.meeting)
            }
        }

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        showLoading()
        viewModel.start()
    }

    private fun showError(e: Exception) {
        DyteLogger.info("MainActivity::showError::")
        val errorView = DyteErrorView(this)
        container.removeAllViews()
        container.addView(errorView)
        errorView.refresh(e) {
            finish()
        }
    }

    private fun showLoading() {
        DyteLogger.info("MainActivity::showLoading::")
        val loaderView = DyteLoaderView(this)
        container.removeAllViews()
        container.addView(loaderView)
    }

    private fun showSetupScreen() {
        DyteLogger.info("MainActivity::showSetupScreen::")
        container.removeAllViews()
        supportFragmentManager.beginTransaction()
            .add(R.id.clContainer, DyteSetupFragment())
            .commit()
    }

    private fun showWebinarFragment() {
        DyteLogger.info("MainActivity::showWebinarFragment::")
        container.removeAllViews()
        if (getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            DyteLogger.info("MainActivity::showWebinarFragment::dyte")
            supportFragmentManager.beginTransaction()
                .add(R.id.clContainer, DyteWebinarFragment())
                .commit()
        } else {
            DyteLogger.info("MainActivity::showWebinarFragment::activespeaker")
            supportFragmentManager.beginTransaction()
                .add(R.id.clContainer, ActiveSpeakerWebinarFragment())
                .commit()
        }
    }

    private fun handleOnBackPressed() {
        when (viewModel.stateLiveData.value) {
            DyteMeetingViewModel.DyteMeetingState.Webinar -> {
                showLeaveClassDialog()
            }

            DyteMeetingViewModel.DyteMeetingState.Setup -> {
                // TODO: Implement custom logic if needed
            }

            else -> {
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
}