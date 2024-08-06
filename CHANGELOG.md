# Piano Analytics SDK for Android

## v3.4.1
* Fixed bug with prefs for new `PianoConsents` mode

## v3.4.0
* Updated to Kotlin 1.9
* Added support for `PianoConsents`
* Added `PrivacyMode.CUSTOM`, that will be used if set `ConsentMode.CUSTOM`
* Deprecated `PrivacyModesStorage`, use `PianoConsents` instead
* Added switching to main thread when required
* Removed requirement for `READ_PHONE_STATE` permission for Android 6-9
* Updated dependencies:
    - Kotlin [1.8.22 -> 1.9.23]
    - com.squareup.moshi:moshi [1.15.0 -> 1.15.1]
      https://github.com/square/moshi/
    - androidx.lifecycle:lifecycle-process [2.6.2 -> 2.7.0]
      https://developer.android.com/jetpack/androidx/releases/lifecycle#2.7.0

## v3.3.5
* Decreased default offline storage lifetime for events
* Fixed bug with cyclic read/save current privacy mode after its expiration

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
