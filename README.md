# Elevator Project
## SQE3-18ILV
## Team D: Damianschitz, Oberndorfer, Reinberger  
### v1.0  

-------------------
[![Java CI with Maven](https://github.com/fhhagenberg-sqe-esd-ws24/elevator-mqtt-team-d/actions/workflows/maven.yml/badge.svg)](https://github.com/fhhagenberg-sqe-esd-ws24/elevator-mqtt-team-d/actions/workflows/maven.yml)

<b>HOW TO RUN:</b>
* Download the release archive.   
  It contains: 
  * JAR
  * Batch file
  * Properties file

* External dependencies (modifiable in the properties file):
  * MQTT Broker (e.g. Mosquitto)
  * Elevator RMI Interface

* Execute the batch file:
  * `./elevator-teamd.bat` (Windows)
  * The batch file starts both the MQTT adapter and the algorithm.
  * If RMI is not available on startup, the adapter will shut down. => Restart via batch file.
  * If MQTT Broker is not available on startup, both the adapter and the algorithm will try to reconnect every 5 seconds.
  * If RMI disconnects during runtime, the adapter will try to reconnect every 5 seconds.
-------------------
<b>TEST CONCEPT:</b>
* Simple data classes: White Box Unit Tests
  * Elevator
  * Floor
  * MqttTopics
  * ElevatorState
* Complex classes: Black Box Integration Tests
  * ElevatorControlSystem (Mocks for RMI) 
  * ElevatorMqttAdapter (using Testcontainers for MQTT & Mocks for RMI)
  * ElevatorAlgorithm (using Testcontainers for MQTT)
All of these tests are executed automatically in the CI pipeline.

* How to run tests:
  * `mvn test` (from the project root directory) 
  * External dependencies: 
    * Java 17
    * Maven 4.0
    * Docker (for Testcontainers)

