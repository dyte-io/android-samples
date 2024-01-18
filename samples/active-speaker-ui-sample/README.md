<!-- PROJECT LOGO -->
<p align="center">
  <h2 align="center">Active Speaker UI Sample</h3>

  <p align="center">
    This sample showcases how you can build a UI to display only the active speaker while screenshare in a Webinar meeting with Dyte's Android UI Kit!
    <br />
    <a href="https://docs.dyte.io"><strong>Explore the docs »</strong></a>
    <br />
  </p>
</p>

<!-- GETTING STARTED -->

## Getting Started

1. To run the sample app, start by cloning this repo:

    ```sh
    git clone https://github.com/dyte-io/android-samples.git
    ```

2. Open the `active-speaker-ui-sample` project in Android Studio.

3. Paste the `authToken` of a participant with Webinar Preset on [line#29]() of the MainActivity

    ```kt
    const val AUTH_TOKEN = <PASTE_AUTH_TOKEN_HERE>
    ```

4. Click the build/run button to run the app in an emulator/physical device

<!-- Implementation Overview -->

## Implementation Overview

This section provides a concise overview of how the whole UI is implemented in the sample code.

- [MainActivity](#mainactivity)
- [Set-up screen](#set-up-screen)
- [Webinar screen](#webinar-screen)
- [Chat Screen](#chat-screen)
- [Polls screen](#poll-screen)
- [Settings dialog](#settings-dialog)
- [Raise Hand button](#raise-hand-button)
- [Join stage confirmation dialog](#join-stage-confirmation-dialog)
- [Leave Webinar dialog](#leave-webinar-dialog)

#### MainActivity
- The `MainActivity` is implemented using APIs from the Dyte's Android UI Kit and hosts multiple screens, handles navigation.
- It observes the `meetingState` from the UI Kit to show the appropriate screen dynamically. The behaviour can be easily modified in the code if you want to have custom navigation.

#### Set-up screen
- The sample code utilises the pre-built `DyteSetupFragment` from the UI Kit to display the Set-up screen

#### Webinar screen
*Portrait Orientation:*
- The sample code utilises the prebuilt `DyteWebinarFragment` from the Dyte UIKit

*Landscape Orientation:*
- This is a custom Webinar screen implemented to display only the active speaker during screenshare. The landscape layout file available [here](), demonstrates how the UI components from Dyte's Android UIKit can be combined with your custom UI elements
- The Stage section is a cutom view that shows active speaker, screenshares, and plugins
- The Vertical ControlBar uses the `DyteControlBarView` and `DyteControlBarButtons`. Also, the `DyteControlBarButton` is extended to display the unread count dot on the Chat and Polls toggle buttons.

#### Chat screen
- The sample code utilises the prebuilt `DyteChatFragment` from the UI Kit
- In portrait mode, it is displayed full screen, and in landscape mode, it appears beside the Stage section

#### Polls screen
- The sample code utilises the prebuilt `DytePollsFragment` from the UI Kit
- In portrait mode, it is displayed full screen, and in landscape mode, it appears beside the Stage section

#### Settings dialog
- This is a custom dialog implemented using UI components from the UI Kit

#### Raise Hand button
- This is a custom `DyteControlBarButton` that calls the appropriate APIs from the Core SDK according to the permissions and `stageStatus` of the particiant. The code is available [here]() for reference and modification.

#### Join stage confirmation dialog
- This is a custom dialog implemented using the UI components from the UI Kit
- Utilises `DyteParticipantTileView` to display video preview and `DyteMicToggleButton`, `DyteCameraToggleButton` to let the participant toggle mic-camera before joining stage. It also call the `DyteStage.join()` API from Dyte's Android-Core SDK
- You can refer the code [here]()

#### Leave Webinar dialog
- This is a custom dialog which utilises `DyteMobileClient.leaveRoom()` API from Dyte's Android-Core SDK