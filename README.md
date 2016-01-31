![art-icon](http://bamb.at/hdpi.png) ART - Alert Response Teams Redone - ReImplemented
======

## What is this project about?

Lets just assume that you are in need of medical help. You will call 144, 112 or 911, right? 

It seems like the next emergency team takes for ages to reach you. The reason for this might be, that each team member has to be alertet seperately, which takes a lot of time.  

This is still a major problem in our actual emergency call chain, especially in smaller emergency stations.

Due to this problem, we have created an app which allows emergency staff to be notified if there are new missions.

## How does this work?

* Personnel can log into this app and will be alerted of new missions via PUSH NOTIFICATIONS.
* The App does not even have to be running in foreground. 
* The user gets notified by sound, vibration and the LED, even if the phone is muted.
* The dispatcher can use a web interface to send notifications to whole teams.

Actual informations are:

* What is the main problem?
* What is its priority?
* Where is the place? (including a link to Google Maps)

## Why has this project been made?

Project 'ART' was built during an bachelor degree course called "Mobile App Development" in the bachelor course 'ITM13'.

This is a fork of ART which was enhanced by conducting user interviews and the usage of Guidelines for Notification Systems found in the corresponding Bachelor Thesis.

## General structure

* Folder 'android' contains the whole android app. (Created with Android Studio 1.4)
* Folder 'webserver' contains the management website. 

## Installation

See install.txt

### Android

Use the APK in the 'android/APK' folder to install the app

or

Clone the whole project into Android Studio and deploy it from there.

* Requirements: Android 4.1+ (API Level 16)
* IMPORTANT:The webserver adress as well as api keys and several credentials have to be adjusted prior to using this app. 

### Webserver

* The Webserver is based on the Node Pushserver from Smile SA, which is licensed under the MIT License. 
  * https://github.com/Smile-SA/node-pushserver
* It is altered to fit the needs of the current application

The Webserver offers a basic protection. 

To use the server, copy everything from the 'webserver' folder to your desired webserver direction.

Start Server with './bin/pushserver.js -c <path to your config.json>'

NOTE AGAIN THAT THE WEBSERVER AND GCM ID HAVE TO BE ADJUSTED IN ANDROID PROJECT

* Requirements: NodeJS, MongoDB
