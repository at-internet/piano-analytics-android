<div id="top"></div>

<br />
<div align="center">
    <h1 align="center">Piano Analytics SDK Android</h1>
</div>

<!-- ABOUT THE PROJECT -->
## About The Project


The Piano Analytics Android SDK allows you to collect audience measurement data for the [Piano Analytics](https://piano.io/product/analytics/) solution.
It can be used with Android Java applications.

This SDK makes the implementation of Piano Analytics as simple as possible, while keeping all the flexibility of the solution. By using this small library in your applications, and using [dedicated and documented methods](https://developers.atinternet-solutions.com/piano-analytics/), you will be able to send powerful events.

It also includes [Privacy tagging methods](https://developers.atinternet-solutions.com/piano-analytics/data-collection/privacy) that allow you a perfect management of your tagging depending on the regulation you refer to.


<!-- GETTING STARTED -->
## Getting Started

- Install our library on your project (see below), you have a few possibilities :
  - Using Maven (coming soon)
  - Cloning the GitHub project and adding the library directly
- Check the <a href="https://developers.atinternet-solutions.com/piano-analytics/"><strong>documentation</strong></a> for an overview of the functionalities and code examples

## Using Maven

Coming soon...

<p align="right">(<a href="#top">back to top</a>)</p>

## Cloning the GitHub project and adding the library directly

1. Clone the Android SDK from [here](https://github.com/at-internet/piano-analytics-android)
    * SSH: git@github.com:at-internet/piano-analytics-android.git
    * HTTPS: https://github.com/at-internet/piano-analytics-android.git

2. From your Android Studio
    * Go to **File** > **Project Structure** > **Modules**
    * Click on the plus sign **+** and go to **Import...**
    * Search for the cloned directory and select the **piano-analytics** folder
    * Click on **Finish**

3. After a sync of Gradle you might have an error like 'Build was configured to prefer settings repositories over project repositories'
    * Open your project-level **settings.gradle** file and replace/add the repositoriesMode as below
        * repositoriesMode.set(RepositoriesMode.**PREFER_SETTINGS**)
    * Resync Gradle

4. As a support to Huawei in our SDK, you might also have a warning on sync (error on build) talking about a library from com.huawei.hms
    * For Gradle plugin earlier than 7.0
        * Open your project-level **build.gradle** file
        * Add the Maven repository as below under **buildscript** > **repositories** and **allproject** > **repositories**
            * maven {url 'https://developer.huawei.com/repo/'}
    * For Gradle plugin 7.0
        * Open your project-level **build.gradle** file
        * Add the Maven repository as below under **buildscript** > **repositories**
            * maven {url 'https://developer.huawei.com/repo/'}
        * Open your project-level **settings.gradle** file
        * Add the Maven repository as below under **dependencyResolutionManagement** > **repositories**
            * maven {url 'https://developer.huawei.com/repo/'}
    * For Gradle plugin 7.1 or Later
        * Open your project-level **settings.gradle** file
        * Add the Maven repository as below under **pluginManagement** > **repositories** and **dependencyResolutionManagement** > **repositories**
            * maven {url 'https://developer.huawei.com/repo/'}


piano-analytics should now be recognized as a library

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- USAGE EXAMPLES -->
## Usage

1. Configure your site and collect domain in your application initialization
    ```java
    import com.piano.analytics.PianoAnalytics;
    import com.piano.analytics.Configuration;
    
    ...

   PianoAnalytics pa = PianoAnalytics.getInstance(getApplicationContext());

    ...
    
    pa.setConfiguration(new Configuration.Builder()
        .withCollectDomain("log.xiti.com")
        .withSite(123456789)
        .build()
    );
    ```

2. Send events
    ```java
   ...
    import com.piano.analytics.Event;
   ...

    pa.sendEvent(new Event("page.display", new HashMap<String, Object>(){{
        put("page", "page name"); // Event properties
        put("page_chapter1", "chapter 1");
        put("page_chapter2", "chapter 2");
        put("page_chapter3", "chapter 3");
    }}));
    ```

_For more examples, please refer to the [Documentation](https://developers.atinternet-solutions.com/piano-analytics/)_

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
