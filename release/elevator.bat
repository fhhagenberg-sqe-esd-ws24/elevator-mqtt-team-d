@ECHO OFF
SETLOCAL

ECHO Launching Elevator MQTT Adapter...
START /B java -jar ./mqtt-elevator-teamd.jar 

ECHO Launching Elevator Algorithm...
START /B java -cp ./mqtt-elevator-teamd.jar at.fhhagenberg.sqelevator.algorithm.ElevatorAlgorithm

ENDLOCAL