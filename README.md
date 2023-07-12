<div id="top"></div>

<br />
<div align="center">
    <h1 align="center">Piano Analytics SDK Android</h1>
</div>

![GitHub](https://img.shields.io/github/license/at-internet/piano-analytics-android)
![Maven Central](https://img.shields.io/maven-central/v/io.piano.android/analytics)
![GitHub Workflow Status (branch)](https://img.shields.io/github/actions/workflow/status/at-internet/piano-analytics-android/build.yml?branch=master)

<!-- ABOUT THE PROJECT -->
## About The Project


The Piano Analytics Android SDK allows you to collect audience measurement data for the [Piano Analytics](https://piano.io/product/analytics/) solution.
It can be used with Android Java applications.

This SDK makes the implementation of Piano Analytics as simple as possible, while keeping all the flexibility of the solution. By using this small library in your applications, and using [dedicated and documented methods](https://developers.atinternet-solutions.com/piano-analytics/), you will be able to send powerful events.

It also includes [Privacy tagging methods](https://developers.atinternet-solutions.com/piano-analytics/data-collection/privacy) that allow you a perfect management of your tagging depending on the regulation you refer to.


<!-- GETTING STARTED -->
## Getting Started
1. Add SDK dependency in your app script:
```kotlin
dependencies {
    ...
    implementation("io.piano.android:analytics:VERSION")
}
```
2. Add required Google/Huawei ID libraries, if you want to use it as Visitor ID
```kotlin
dependencies {
    ...
    // for GOOGLE_ADVERTISING_ID or ADVERTISING_ID
    implementation("com.google.android.gms:play-services-ads-identifier:GOOGLE_VERSION")
    // for HUAWEI_OPEN_ADVERTISING_ID or ADVERTISING_ID
    implementation("com.huawei.hms:hms-ads-identifier:HUAWEIVERSION")
}
```
3. Add Huawei libraries repository (only if you've added Huawei ID library at the previous step)
```kotlin
    repositories {
        ...
        maven("https://developer.huawei.com/repo/")
    }
```

Check the <a href="https://developers.atinternet-solutions.com/piano-analytics/"><strong>documentation</strong></a> for an overview of the functionalities and code examples

<p align="right">(<a href="#top">back to top</a>)</p>


<!-- USAGE EXAMPLES -->
## Usage

### Configure SDK at your application initialization
```kotlin
val configuration = Configuration.Builder(
    collectDomain = "logs.xiti.com",
    site = 123456789,
    // you can set other properties here 
).defaultPrivacyMode(...)
    // or set properties via builder methods
    .build()

PianoAnalytics.init(applicationContext, configuration)
```

### Sending events
```kotlin
PianoAnalytics.getInstance().sendEvents(
    Event.Builder(Event.CLICK_NAVIGATION)
        .properties(
            Property(PropertyName.CLICK, "value"),
            // you can add other properties here
        )
        .build(),
    // you can add other events here
    Event.Builder("some_event_name")
        .properties(
            Property(PropertyName("some_property_name"), "value"),
        )
        .build()
)
```
Some event names are predefined in `Event` class, some property names are predefined in `PropertyName` class also

### Working with privacy modes
```kotlin
val privacyModesStorage = PianoAnalytics.getInstance().privacyModesStorage
// create new privacy mode
val myCustomPrivacyMode = PrivacyMode(
    visitorMode = "name",
    visitorConsent = true
)
// forbid all storage features for new mode
myCustomPrivacyMode.forbiddenStorageFeatures += PrivacyStorageFeature.ALL
// register the new privacy mode
privacyModesStorage.allModes += myCustomPrivacyMode
// set current privacy mode (should be registered before, if custom)
privacyModesStorage.currentMode = myCustomPrivacyMode
```

### Working with custom properties
```kotlin
val contextPropertiesStorage = PianoAnalytics.getInstance().contextPropertiesStorage
// add new context property
contextPropertiesStorage.add(
    ContextProperty(
        properties = setOf(
            Property(...),
            Property(...)
        ),
        persistent = false,
        eventNames = listOf(Event.CLICK_NAVIGATION, Event.CLICK_ACTION)
    )
)
// remove some property added before
contextPropertiesStorage.deleteByKey(PropertyName.CLICK)
```

### Working with user
```kotlin
val userStorage = PianoAnalytics.getInstance().userStorage
// check that user was loaded from storage
if (userStorage.userRecognized) {
    ...
}
// set current user
userStorage.currentUser = User("id", shouldBeStored = false)
```

### Working with AV Insights
```kotlin
val heartbeat = SparseLongArray()
...
val bufferHeartbeat = SparseLongArray()
...
val mediaHelper = PianoAnalytics.getInstance()
    .mediaHelper("contentId")
    .setHeartbeat(heartbeat)
    .setBufferHeartbeat(bufferHeartbeat)
// set playback speed
mediaHelper.playbackSpeed = 2.5
// post play event
mediaHelper.play(
    position,
    Property(...)
)
// post share event
mediaHelper.share()
```

_For more examples, please refer to the [Documentation](https://developers.atinternet-solutions.com/piano-analytics/)_

<p align="right">(<a href="#top">back to top</a>)</p>

## Migration from 3.2.0 and older to 3.3.0+

1. Update all imports from `io.piano.analytics` to `io.piano.android.analytics`
2. Replace all `withXXXX(...)` calls with `XXXX(...)` for `Configuration.Builder`
3. Replace `pa.setConfiguration(configuration)` with `PianoAnalytics.init(applicationContext, configuration)`. Note: configuration can be set only once, at initialization.
4. Replace `Event("some_event_name", new HashMap<String, Object>(){{ ... }})` with `Event.Builder("some_event_name").properties( ... ).build()`
5. Replace `pa.setProperty(...)` with `contextPropertiesStorage.add(ContextProperty(...))`, where `contextPropertiesStorage` is `PianoAnalytics.getInstance().contextPropertiesStorage`
6. Replace `pa.privacySetMode(...)` with `PianoAnalytics.getInstance().privacyModesStorage.currentMode = ...`
7. Replace `Media(pa)` with `PianoAnalytics.getInstance().mediaHelper("av_content_id_value")`. Note: `MediaHelper` instance is linked to `contentId` and adds it as `av_content_id` property automatically

You can find full list of changes [here](https://developers.atinternet-solutions.com/piano-analytics/data-collection/sdks/android-kotlin#migration-from--330)

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- CONTRIBUTING -->
## Contributing

Please do not hesitate to contribute by using this github project, we will look at any merge request or issue. 
Note that we will always close merge request when accepting (or refusing) it as any modification has to be done from our side exclusively (so we will be the ones to implement your merge request if we consider it useful).
Also, it is possible that issues and requests from GitHub may take longer for us to process as we have dedicated support tools for our customers. So we suggest that you use GitHub tools for technical purposes only :)



<!-- LICENSE -->
## License

Distributed under the MIT License.

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- CONTACT -->
## Contact

AtInternet a Piano Company - support@atinternet.com

<p align="right">(<a href="#top">back to top</a>)</p>
