![readme-header](readme-header.png)

# BoT-Gateway-Service
  Edge Gateway Module service for Finn Banking of Things to accept remote requests, process the requests and route to proper Finn device within the local network. This module is designed to be run on edge system and the service gets exposed to outside world either through Ngrok tool or router port forwarding mechanism. The default port is 3001 on which the service gets started to be accessed by remote clients.

## Prerequisites
- Hardware Devices
  - Raspberry Pi Zero / 3 / 4
  - Any IoT Device on which Java 1.8 is supported
  - Linux / Windows / Mac OS System
- Software Packages
  - JDK 1.8
  - Raspian / Linux / Windows / Mac Operating System
  - [Ngrok](https://ngrok.com/) tool
  - Maven build tool

## Installation Instructions
  - Get the repository: `git clone git@github.com:BankingofThings/BoT-Gateway-Service.git`
  - Go to BoT-Gateway-Service directory:  `cd BoT-Gateway-Service`
  - Update the Finn device's IP Address and defined action IDs in the source file `src/main/java/com/finn/bot/edge/controller/WebhookServiceController.java`
  - Build the code: `mvn clean package -DskipTests`
  - Install [Ngrok](https://ngrok.com/) tool on the gateway device
  
## Execution Instructions
  - Go to BoT-Gateway-Service/target:  `cd BoT-Gateway-Service/target`
  - Execute the built jar:  `java -jar BoT-Gateway-Service.jar`
  - Execute Ngrok tool:  `ngrok http 3001`
  - Make a note of URLs output from [Ngrok](https://ngrok.com/) tool execution
  
## Supported End Points
  - End point to retrieve QrCode for the device - `/qrcode`
  - End point to post as webhook in to remote client/service - `/webhook`

## Contributing
Any improvement to the FINN SDK are very much welcome! Our software is open-source and we believe your input can help create a lively community and the best version of FINN. We’ve already implemented much of the feedback given to us by community members and will continue to do so. Join them by contributing to the SDK or by contributing to the documentation.

# Community

## Slack
Slack is our main feedback channel for the SDK and documentation. Join our [Slack channel](https://ing-bankingofthings.slack.com/join/shared_invite/enQtNDEyODg3MDE1NDg4LWJhNGFiOTFhZmVlNGQwMTM4ZjQzNmZmZDk5ZGZiNjNlZTVjZjNmYjE0Y2MxZjU5MWQxNmY5MTgzYzAxNmFiNGU) and be part of the FINN community.<br/>

## Meetups
We also organize meetups, e.g. demo or hands-on workshops. Keep an eye on our meetup group for any events coming up soon. Here you will be able to see the FINN software in action and meet the team.<br/>
[Meetup/Amsterdam-ING-Banking-of-Things](meetup.com/Amsterdam-ING-Banking-of-Things/).
 
# About FINN
After winning the ING Innovation Bootcamp in 2017, FINN is now part of the ING Accelerator Program. Our aim is to become the new Internet of Things (IoT) payment standard that enables service-led business models. FINN offers safe, autonomous transactions for smart devices.
We believe our software offers tremendous business opportunities. However, at heart, we are enthusiasts. Every member of our team has a passion for innovation. That’s why we love working on FINN.
[makethingsfinn.com](makethingsfinn.com)
