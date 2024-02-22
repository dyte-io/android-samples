package io.dyte.sample.facetime

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import dyte.io.uikit.DyteNotificationConfig
import dyte.io.uikit.DyteUIKitBuilder
import dyte.io.uikit.DyteUIKitConfig
import dyte.io.uikit.DyteUIKitInfo
import dyte.io.uikit.DyteUiKitNotificationsConfig
import dyte.io.uikit.screens.DyteMeetingViewModel
import dyte.io.uikit.screens.groupcall.DyteGroupCallFragment
import dyte.io.uikit.screens.webinar.DyteWebinarFragment
import dyte.io.uikit.utils.ViewUtils.gone
import dyte.io.uikit.utils.ViewUtils.visible
import dyte.io.uikit.view.DyteErrorView
import io.dyte.core.models.DyteMeetingInfoV2

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: DyteMeetingViewModel

    private lateinit var buttonStartMeeting: Button
    private lateinit var loader: ProgressBar
    private lateinit var clDataContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonStartMeeting = findViewById(R.id.btn_start_meeting)
        loader = findViewById(R.id.progressbar_meeting_room)
        clDataContainer = findViewById(R.id.clDataContainer)

        val meetingInfo = DyteMeetingInfoV2(authToken = MeetingConfig.AUTH_TOKEN)
        DyteUIKitBuilder.build(DyteUIKitInfo(activity = this, dyteMeetingInfo = meetingInfo))

        viewModel = ViewModelProvider(this)[DyteMeetingViewModel::class.java]
        viewModel.stateLiveData.observe(this) {
            println("dytelog::MainActivity::onCreate::${it.javaClass.simpleName}")
            when (it) {
                DyteMeetingViewModel.DyteMeetingState.Loading -> {
                    showLoading()
                }
                DyteMeetingViewModel.DyteMeetingState.Setup -> {
                    // we dont wanna show setup screen here, hence joining meeting directly
//                    viewModel.meeting.participants.disableCache()
                    viewModel.meeting.joinRoom()
                }
                DyteMeetingViewModel.DyteMeetingState.GroupCall -> {
                    showMeeting()
                }
                is DyteMeetingViewModel.DyteMeetingState.Error -> {
                    showError(it.exception)
                }
                DyteMeetingViewModel.DyteMeetingState.Left -> {
                    finish()
                }
                DyteMeetingViewModel.DyteMeetingState.Removed -> {
                    showError(Exception("Removed from meeting!"))
                }
                else -> {
                    // todo : implement remaining states
                }
            }
        }

        buttonStartMeeting.setOnClickListener {
            viewModel.start()
        }
    }

    private fun showMeeting() {
        clDataContainer.gone()
        buttonStartMeeting.gone()
        loader.gone()
        clDataContainer.visible()
        clDataContainer.removeAllViews()

        val meetingView = MeetingView(this)
        clDataContainer.addView(meetingView)
        meetingView.activate(viewModel.meeting)
    }

    private fun showLoading() {
        clDataContainer.gone()
        buttonStartMeeting.gone()
        loader.visible()
    }

    private fun showError(exception: Exception) {
        clDataContainer.gone()
        loader.gone()
        buttonStartMeeting.gone()
        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
        finish()
    }
}