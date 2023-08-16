# Piano Analytics SDK for Android

## v3.3.2
* Added `ReportUrlProvider` to `Configuration` for case "switching between several site ids for one app at runtime"

## v3.3.1
* Fixed setting properties `av_previous_position`, `av_position` and `av_duration`
* Added `PianoAnalytics.EventProcessorCallback` as a replacement for previous `OnWorkListener`

## v3.3.0
* Changed package and artifact group: `io.piano.analytics` -> `io.piano.android.analytics`
* Rewritten to Kotlin 1.8
* Increased minSdkVersion from 16 to 21
* Added compatibility with Android 14
See migration steps [here](https://github.com/at-internet/piano-analytics-android/tree/main#migration-from-320-and-older-to-330)
