# Piano Analytics SDK for Android

## v3.3.5-SNAPSHOT
* Decreased default offline storage lifetime for events

## v3.3.4
* Added limit for event storage
* Updated dependencies:
    - com.squareup.okhttp3:okhttp [4.11.0 -> 4.12.0]
      https://square.github.io/okhttp/

## v3.3.3
* Added `track` function to `MediaHelper` for tracking custom media events.
* Changed behavior: `extraProps` in `MediaHelper` are added to all events, not only to "heartbeat" events.
* Fixed bug with clearing storage at setting `PrivacyMode` with only `PrivacyStorageFeature.ALL` allowed.
* Fixed bug with incorrect default values for `LIFECYCLE` properties

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
