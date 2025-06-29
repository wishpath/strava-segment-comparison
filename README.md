## Narrative
Compare athlete's Strava segment results.

## What app does
* Downloads athlete's starred Strava segments.
* Filters them.
* Counts how close they are to home point
* Scores athletes best performance score depending on:
  * Distance, time and gradient.
* Presents calculations in the console.

## Settings
* Build and run: Intellij
* Gradle: wrapper (8.5)
* Gradle JVM: 21
* Java Home: 22

## Set personal variables
* In \src\main\java\org\sa\config\Props.java
* Get location from google maps (right click on the map).
  * example: HOME_LATITUDE = 50.99999999
* Get Strava authentication variables from: https://www.strava.com/settings/api
  * example: STRAVA_CLIENT_ID = "99999999"