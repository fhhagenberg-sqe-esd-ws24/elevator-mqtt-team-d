package at.fhhagenberg.sqelevator;

import at.fhhagenberg.sqelevator.adapter.*;
import at.fhhagenberg.sqelevator.algorithm.*;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for the elevator system
 */
@Suite
@SelectClasses({ ElevatorTest.class, MqttTopicsTest.class, FloorTest.class,
    ElevatorControlSystemTest.class, ElevatorMqttAdapterTest.class,
    ElevatorStateTest.class, ElevatorAlgorithmTest.class,
    FaultyBrokerElevatorAlgorithmTest.class, FaultyBrokerElevatorMqttAdapterTest.class,
    RMIDisconnectElevatorMqttAdapterTest.class})
public class ElevatorTestSuite {

}
