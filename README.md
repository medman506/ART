![art-icon](http://bamb.at/hdpi.png) ART - Alert Response Teams Redone - ReImplemented
======

## What is this project about?

Lets just assume that you are in need of medical help. You will call 144, 112 or 911, right? 

It seems like the next emergency team takes for ages to reach you. It may be caused because not every member of the team has a pager. 

This is still a major problem in our actual emergency call chain, especially in smaller emergency stations.

Due to this problem, we have created an app which allows all medical educated staff to be notified if there are new missions.

## How does this work?

Personnel can log into this app and will be alerted of new missions via PUSH NOTIFICATIONS.
The dispatcher can use a web interface to send notifications to whole teams.

Actual informations are:

* What is the main problem?
* Where is the place? (link to Google Maps)

## Why has this project been made?

Project 'ART' was built during an bachelor degree course called "Mobile App Development" in the bachelor course 'ITM13'.

This is a Fork of this Project which is implemented as part of a Bachelors Thesis. Based on the research of the thesis, this project is rewritten to suite the needs of actual users even better. 

## General structure

* Folder 'android' contains the whole android app. (Created with Android Studio 1.4)
* Folder 'webserver' contains the management website. 

## Installation

### Android

Use the APK in the 'android/APK' folder to install the app

or

Clone the whole project into Android Studio and deploy it from there.

* Requirements: Android 4.1+ (API Level 16)
* IMPORTANT: The Webserver-URL is actually hard coded written in the project. If you use your own Webserver instead of the default one (http://kerbtech.diphda.uberspace.de/art/) please change this in the project. 

### Webserver

* The Webserver is based on the Node Pushserver from Smile SA, which is licensed under the MIT License. 
  * https://github.com/Smile-SA/node-pushserver
* It is altered to fit the needs of the current application

The Webserver offers a basic protection. 


To use the server, copy everything from the 'webserver' folder to your desired webserver direction.

Start Server with './bin/pushserver.js'

NOTE AGAIN THAT THE WEBSERVER URL IS HARD CODED IN THE ANDROID PROJECT

* Requirements: NodeJS, MongoDB
