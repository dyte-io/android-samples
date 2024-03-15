package io.dyte.android.sample.uikitjava;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dyte.io.uikit.DyteUIKit;
import dyte.io.uikit.DyteUIKitBuilder;
import dyte.io.uikit.DyteUIKitInfo;
import io.dyte.core.models.DyteMeetingInfoV2;

public class MainActivity extends AppCompatActivity {
    private Button mStartMeetingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartMeetingButton = findViewById(R.id.btn_start_meeting);
        mStartMeetingButton.setOnClickListener(v -> {
            startDyteMeeting();
        });
    }

    private void startDyteMeeting() {
        DyteMeetingInfoV2 meetingInfo = new DyteMeetingInfoV2(
                MeetingConfig.AUTH_TOKEN,
                true,
                true,
                MeetingConfig.BASE_URL
        );
        DyteUIKitInfo dyteUIKitInfo = new DyteUIKitInfo(this, meetingInfo);
        DyteUIKit dyteUIKit = DyteUIKitBuilder.build(dyteUIKitInfo);
        dyteUIKit.startMeeting();
    }
}