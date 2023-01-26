# modicio-insights

:bulb: **A demonstration video is provided [here](https://videocampus.sachsen.de/m/3edea95717a07f74373ac0bdab1f29938e6374fa262a938afa55818b19d84d071c53fa20f61bf52acac6ea4530bc21c4fdeeb1b6c8814c6af38deacd54130e5d)**.

## About

This application is a minimal web starter written in Scala to demonstrate the modicio framework. This project was initially contributed as *codi-native* and renamed in a later release. For more detailled background information check the modicio documentation and wiki.

The main idea of this project is to provide a runnable and minimal application to demonstrate the modicio framework functionality. Therefore, modicio-insights provides a model view including a model editor for modicio deep-models and an user view to simulate an application frontend to use those models at runtime. Between model updates, no downtime, data-scheme or code-changes are required.

> :bulb: Note: No verificators are implemented in this demo-application. This includes checks of rules and attribute values and association multiplicities. Example 1: a nonesensical link-rule input is not rejected but accepted and ignored. Example 2: An attribute is modelled as obligatory number but any string input, also empty strings, are accepted.

Most functionality regarding modicio is further explained and extracted as part of our [getting-started guide](https://github.com/modicio/modicio/wiki/Getting-Started)

## Release Notes

This case-study application contains releases to follow the revision of modicio-native:

* [0.1a - experiment](https://github.com/modicio/modicio-insights/releases/tag/0.1a) contains the version (called *codi-native*) used to execute the experiment/user-study of Karl Kegel's master thesis.
* [0.1.2](https://github.com/modicio/modicio-insights/releases/tag/0.1.2)
  * modicio 1.2.0 integration ([modicio release](https://github.com/modicio/modicio/releases/tag/0.1.2))
   
Newer commits which are not yet part of a release may fix minor bugs or enhance documentation. We recommend to use the latest commit for exploration.

## Getting Started

### Start from CI Build

The CI pipeline of this repository creates a dist package with a runnable bash script. You can download the package by clicking on the latest CI run. 
* Unpack the ZIP
* Place the resources folder of this repos inside the bin folder
* Execute the modicio-native bash file inside bin on your terminal. 

After some time, the web-app is available on localhost:9000 (or different port, check the terminal output).

> :zap: Windows Powershell may not work, please use a true unix bash, for example GitBash or Ubuntu.

A running java setup is required to run this application, the dist requires OpenJDK 11 or newer, a JRE may be sufficent.

> :ambulance: If the last CI build is older then some Github defaults, the build artifacts may be unavailable. In this case please use the custom build explained below.

###  Custom Build

* Clone this repository
* Install SBT (Scala build system) and a **JDK 11**
* Go to the root directory where this README and the ``build.sbt`` is located.
* Execute ``sbt dist`` and check the terminal output where the generated JAR package is located
* Alternatively, execute ``sbt run`` directly to start the app without generating a JAR
