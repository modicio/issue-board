# issue board (case-study)

## About

This application is a minimal domain specific web starter written in Scala to demonstrate the modicio framework. For more detailled background information check the modicio documentation and wiki.

The main idea of this project is to provide a runnable and minimal domain oriented application to demonstrate the ESI evolution concept. Therefore, the issue board provides a model view including a model editor for modicio deep-models and the actual issue board to create issues of modelled types. Between model updates, no downtime, data-schema or code-changes are required.

> :bulb: Note: No verificators are implemented in this case-study. This includes checks of rules and attribute values and association multiplicities. Example 1: a nonesensical link-rule input is not rejected but accepted and ignored. Example 2: An attribute is modelled as obligatory number but any string input, also empty strings, are accepted.

Most functionality regarding modicio is further explained and extracted as part of our [getting-started guide](https://github.com/modicio/modicio/wiki/Getting-Started)

## Release Notes

This case-study is not guaranteed to be compatible with the nwest modicio version. A compiled modicio version is included in this repo.

## Getting Started

### Start from CI Build

The CI pipeline of this repository creates a dist package with a runnable bash script. You can download the package by clicking on the latest CI run. 
* Unpack the ZIP
* Place the resources folder of this repos inside the bin folder
* Execute the issue-board bash file inside bin on your terminal. 

After some time, the web-app is available on localhost:9000 (or different port, check the terminal output).

> :zap: Windows Powershell may not work, please use a true unix bash, for example GitBash or Ubuntu.

A running java setup is required to run this application, the dist requires OpenJDK 11, a JRE may be sufficent.

> :ambulance: If the last CI build is older then some Github defaults, the build artifacts may be unavailable. In this case please use the custom build explained below.

###  Custom Build

* Clone this repository
* Install SBT (Scala build system) and a **JDK 11**
* Go to the root directory where this README and the ``build.sbt`` is located.
* Execute ``sbt dist`` and check the terminal output where the generated JAR package is located
* Alternatively, execute ``sbt run`` directly to start the app without generating a JAR
