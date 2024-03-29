# issue-board (case-study)

## About

This application is a minimal domain specific web starter written in Scala to demonstrate the modicio framework. For more detailled background information check the modicio documentation and wiki.

The main idea of this project is to provide a runnable and minimal domain oriented application to demonstrate the ESI evolution concept. Therefore, the issue board provides a model view including a model editor for modicio deep-models and the actual issue board to create issues of modelled types. Between model updates, no downtime, data-schema or code-changes are required.

> :bulb: Note: No verificators are implemented in this case-study. This includes checks of rules and attribute values and association multiplicities. Example 1: a nonesensical link-rule input is not rejected but accepted and ignored. Example 2: An attribute is modelled as obligatory number but any string input, also empty strings, are accepted.

Most functionality regarding modicio is further explained and extracted as part of our [getting-started guide](https://github.com/modicio/modicio/wiki/Getting-Started)

## Release Notes

This case-study is not guaranteed to be compatible with the newest modicio version. A compiled modicio version is included in this repo.

* This project contains the following [releases](https://github.com/modicio/issue-board/releases):
  * **v 0.1.0 Basic Case Study** - the initial implementation to demonstrate the feasibility of the modicio framework.
  * **v 0.2.0 Feature DSL Case Study** - extended case study to demonstrate the feasibility of a feature-request DSL at runtime.

## Getting Started

### modicio

This project contains a static build of the [modicio framework](https://github.com/modicio/modicio). Please check the modicio documentation and wiki for more Information.

### Feature DSL

This project contains a static build of the [featurelang project](https://github.com/modicio/feature-dsl-tools) to compile feature requests at runtime. Please check the respective documentation.

### Start from CI Build

The CI pipeline of this repository creates a dist package with a runnable bash script. You can download the package by clicking on the latest CI run. 
* Unpack the ZIP
* Install a **JDK 11** and a **JDK 17**
* **Place the resources folder of this repository inside the bin folder**
* Make sure that JDK 11 is active by checking ``java --version``. Otherwise, update your JAVA_HOME.
* Open the ``javaconf.txt`` file inside ``resources/featurecompiler`` and enter the path to the java 17 executable.
* Execute the issue-board bash file inside bin on your terminal. 

After some time, the web-app is available on localhost:9000 (or different port, check the terminal output).

> :zap: Windows Powershell may not work, please use a true unix bash, for example GitBash or Ubuntu.

A running java setup is required to run this application, the dist requires OpenJDK 11, a JRE may be sufficent.

> :ambulance: If the last CI build is older then some Github defaults, the build artifacts may be unavailable. In this case please use the custom build explained below.

###  Custom Build

* Clone this repository
* Install SBT (Scala build system) and a **JDK 11** as well as a **JDK 17**
* Make sure that JDK 11 is active by checking ``java --version``. Otherwise, update your JAVA_HOME.
* Go to the root directory where this README and the ``build.sbt`` is located.
* Execute ``sbt dist`` and check the terminal output where the universal package is located
  * If zipped, unzip the dist package
  * **Place the resources folder of this repository inside the bin folder**
  * Open the ``javaconf.txt`` file inside ``resources/featurecompiler`` and enter the path to the java 17 executable.
* Alternatively, execute ``sbt run`` directly to start the app without generating a JAR (Set the java 17 path as stated in the step above)

---

**Attributions**
* [Gear icons created by Freepik - Flaticon](https://www.flaticon.com/free-icons/gear)
* [Tick icons created by Roundicons - Flaticon](https://www.flaticon.com/free-icons/tick)
