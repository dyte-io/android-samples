# Dyte Android Samples

## Contents
1. [Introduction](#introduction)
2. [About This Repository](#about-this-repository)
3. [Usage](#usage)
4. [Trying out a sample](#trying-out-a-sample)

## Introduction

Create immersive live experiences with everything you need - audio/video conferencing, live streaming, and chat - all packed into a single SDK.


## About This Repository
This repository consists of all the different ways in which you can use Dyte's
Android SDK and other packages to its full extent to get the best live
audio/video experience.

## Samples

Here are the list of available samples at the moment.

1. [Android Core Sample in Kotlin](samples/android-core-sample-kotlin)
2. [Android Core Sample in Java](samples/android-core-sample-java)
3. [Android UI Kit sample with default meetings UI](samples/android-ui-kit-sample-kotlin)
4. [Android UI Kit sample with custom meetings UI](samples/active-speaker-ui-sample)

## Usage

To use these samples you would need to do the following steps:

First, you'll need to create a meeting and add a participant to that meeting.

You can do so by going to https://docs.dyte.io/api?v=v2 and run the APIs in the
API runner itself so you can quickly get started.

Make sure you've created your Dyte account at https://dev.dyte.io and have your
`Organization ID` and `API Key` ready to use from the
[API Keys section](https://dev.dyte.io/apikeys).

1. Go to
   [Create Meeting API](https://docs.dyte.io/api/?v=v2#/operations/create_meeting)
   and add your credentials and run the API with your request body, note the
   `id` you receive in resonse, this is the meeting id.
2. Go to
   [Add Participant API](https://docs.dyte.io/api/?v=v2#/operations/add_participant)
   and add a participant to the meeting with the `meetingId` you received in
   previous API call.

Once you're done, you'll get an `authToken`, which you can use in a sample as
explained below.

## Trying out a sample

Here are steps to try out the samples:

1. Clone the repo:

```sh
git clone git@github.com:dyte-io/android-samples.git
```

2. Open the project in your `Android Studio` and paste your `authToken` in `MeetingConfig` file and run the app