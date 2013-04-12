# Hoiio SMS Notification for Jenkins

This is Jenkins plugin that sends SMS notification whenever there's a failed build. The plugin is powered by [Hoiio API][homepage]

Hoiio API is set of telephony API that integrate telephony services - phone calls, conference, IVR (Interactive Voice Responses), Fax and SMS - into your services and website easily.

Read the documentation at http://hoiio.readthedocs.org/

## Installation

You can find the plugin in Jenkins' plugin site. Plugin's name is "sms-notification"

## Get Hoiio Developer Account

Visit [Hoiio Developer][homepage] site to get a Hoiio Developer account.

## Get AppID and Access Token

Login to Hoiio Developer's portal, create an app. Then, click on "View Keys" to retrieve the appID and accessToken

## Setup

Go to Manage Jenkins -> Configure System, navigate to "Hoiio Credentials" and fill in the appId and accessToken of your Hoiio Developer account

## Usage

To setup SMS notification for a specific Jenkins job, go to Configuration of the job.
Then add a Post-build task named "SMS Notification", fill in the mobile numbers you want to retrieve the SMS with the following format:+6591234567
Multiple numbers are supported by adding a comma "," between the numbers.


## Hoiio API

Visit [our API site][documentation] to learn more about our powerful communication API.


[homepage]:http://developer.hoiio.com/
[documentation]:http://developer.hoiio.com/docs/index.html
[sms_query_status]:http://developer.hoiio.com/docs/sms_status.html
