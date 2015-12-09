![art-icon](http://bamb.at/hdpi.png) ART - Alert Response Teams
======

## What is this project about?

Lets just assume that you are in need of medical help. You will call 144, right? 

But unfortunatelly the next emergency team is not nearby you and it can take up to half an hour till someone else can help you.

This is still a major problem in our actual emergency call chain, especially in less populated areas.

Due to this problem, we have created an app which allows all medical educated staff to be notified if someone needs help nearby.

## How does this work?

Each medically educated staff should download and install this app on his/her android device, register with a relative unique name and set their actual status to online. 

After that the App sends the actual position of this staff (if the person is in an active status) to an management server periodically where a dispatcher can see your position on a map. 

After this dispatcher gets a call he can send you a push notification with the most important informations about the actual incident.

Actual informations are:

* What is the main problem?
* Where is the place? (link to Google Maps)

PICTURE OF TASK

## Why has this project been made?

Project 'ART' was built during an bachelor degree course called "Mobile App Development" in the bachelor course 'ITM13'.

## General structure

* Folder 'android' contains the whole android app. (Created with Android Studio 1.4)
* Folder 'webserver' contains the management website. 

## Installation

### Android

<div style="text-align:center">
Use the APK in the 'android/APK' folder to install the app

or

Clone the whole project into Android Studio and deploy it from there.
</div>


* Requirements: Android 4.1+ (API Level 16)
* IMPORTANT: The Webserver-URL is actually hard coded written in the project. If you use your own Webserver instead of the default one (http://kerbtech.diphda.uberspace.de/art/) please change this in the project. 

### Webserver
<div style="text-align:center">
Use the default webserver (http://kerbtech.diphda.uberspace.de/art/)
</div>

* Usernmae: moappdev

* Password: leitstellenfruechtchen

<div style="text-align:center">
Otherwise copy everything from the 'webserver' folder to your desired webserver direction.

Start Server with './bin/pushserver.js'

NOTE AGAIN THAT THE WEBSERVER URL IS HARD CODED IN THE ANDROID PROJECT

</div>

* Requirements: NodeJS, MongoDB

* Use this repo to initialize MongoDB for the GCM Push Server:
  * https://github.com/Smile-SA/node-pushserver

## Repository Visualization
In case you want to get a visual overview of the repository take a look at the video below.

[![Alt video player](http://bamb.at/player.png)](http://bamb.at/art_visualization.webm)
