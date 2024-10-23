package at.fhhagenberg.sqelevator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.rmi.RemoteException;

public class ElevatorControlSystemTest {

    private ElevatorControlSystem system;

    @BeforeEach
    public void setup() {
        // Initialize the ElevatorControlSystem with 3 elevators and 5 floors
        int nrOfElevators = 3;
        int[] elevatorCapacities = {10, 12, 8}; // Capacity for each elevator
        int[] emptyElevatorWeights = {1000, 1100, 900}; // Initial weight for each elevator
        int nrOfFloors = 5;
        int floorHeight = 10; // Assume each floor has a height of 10 meters

        try {
            system = new ElevatorControlSystem(nrOfElevators, elevatorCapacities, emptyElevatorWeights, nrOfFloors, floorHeight);
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetElevatorNum() {
        try {
            assertEquals(3, system.getElevatorNum());
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetFloorNum() {
        try {
            assertEquals(5, system.getFloorNum());
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetFloorHeight() {
        try {
            assertEquals(10, system.getFloorHeight());
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testSetAndGetCommittedDirection() {
        try {
            system.setCommittedDirection(0, IElevator.ELEVATOR_DIRECTION_UP);
            assertEquals(IElevator.ELEVATOR_DIRECTION_UP, system.getCommittedDirection(0));

            system.setCommittedDirection(1, IElevator.ELEVATOR_DIRECTION_DOWN);
            assertEquals(IElevator.ELEVATOR_DIRECTION_DOWN, system.getCommittedDirection(1));
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetAndSetElevatorSpeed() {
        try {
            system.setTarget(0, 2);
            system.setCommittedDirection(0, IElevator.ELEVATOR_DIRECTION_UP);

            assertEquals(0, system.getElevatorSpeed(0));  // Initially the speed is 0
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testElevatorFloorServices() {
        try {
            assertFalse(system.getServicesFloors(0, 2));
            system.setServicesFloors(0, 2, true);
            assertTrue(system.getServicesFloors(0, 2));
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetAndSetElevatorButton() {
        try {
            assertFalse(system.getElevatorButton(0, 2));
            system.setServicesFloors(0, 2, true); // Simulate pressing the button
            assertFalse(system.getElevatorButton(0, 2));
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetElevatorDoorStatus() {
        try {
            assertEquals(IElevator.ELEVATOR_DOORS_CLOSED, system.getElevatorDoorStatus(0));
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetElevatorWeight() {
        try {
            // Initially, the elevator's weight should be 0 (no passengers).
            assertEquals(0, system.getElevatorWeight(0));
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetElevatorCapacity() {
        try {
            // Initially, the elevator's weight should be 0 (no passengers).
            assertEquals(10, system.getElevatorCapacity(0));
            assertEquals(12, system.getElevatorCapacity(1));
            assertEquals(8, system.getElevatorCapacity(2));
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetElevatorFloor(){
        try {
            assertEquals(0, system.getElevatorFloor(0));
            assertEquals(0, system.getElevatorFloor(1));
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetElevatorPosition() {
        try {
            assertEquals(0, system.getElevatorPosition(0));
            assertEquals(0, system.getElevatorPosition(1));
        } catch (RemoteException e) {
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testFloorButtonUpAndDown() {
        // Initially, no floor buttons should be pressed.
        try{
            assertFalse(system.getFloorButtonUp(2));
            assertFalse(system.getFloorButtonDown(2));
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testSetAndGetTargetFloor() {
        try {
            system.setTarget(0, 2);
            assertEquals(2, system.getTarget(0));

            system.setTarget(1, 3);
            assertEquals(3, system.getTarget(1));
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    @Test
    public void testClockTick() {
        try {
            long start = System.currentTimeMillis();
            long clockTick = system.getClockTick();
            long current = System.currentTimeMillis();

            assertTrue(clockTick >= 0);
            assertTrue(current - start >= 0);
        } catch (RemoteException e) {
            // Handle the exception as needed (e.g., print it, fail the test, etc.)
            fail("RemoteException thrown: " + e.getMessage());
        }
    }

    // tests for exceptions
    @Test
    public void testInvalidElevatorNumberThrowsException() {
        assertThrows(RemoteException.class, () -> system.getCommittedDirection(5));
        assertThrows(RemoteException.class, () -> system.getCommittedDirection(-1));

        assertThrows(RemoteException.class, () -> system.getElevatorAccel(6));
        assertThrows(RemoteException.class, () -> system.getElevatorAccel(-1));
    }

    @Test
    public void testInvalidFloorThrowsException() {
        assertThrows(RemoteException.class, () -> system.getFloorButtonDown(6));
        assertThrows(RemoteException.class, () -> system.getFloorButtonDown(-1));

        assertThrows(RemoteException.class, () -> system.getFloorButtonUp(6));
        assertThrows(RemoteException.class, () -> system.getFloorButtonUp(-1));
    }

    @Test
    public void testInvalidGetAndSetTargetThrowsException() {
        assertThrows(RemoteException.class, () -> system.setTarget(6, 0));
        assertThrows(RemoteException.class, () -> system.setTarget(-1, 0));

        assertThrows(RemoteException.class, () -> system.setTarget(1, -37));
        assertThrows(RemoteException.class, () -> system.setTarget(1, -37));

        assertThrows(RemoteException.class, () -> system.getTarget(6));
        assertThrows(RemoteException.class, () -> system.getTarget(-1));
    }

    @Test
    public void testInvalidGetAndSetServiceFloorsThrowsException() {
        assertThrows(RemoteException.class, () -> system.setServicesFloors(6, 2, true));
        assertThrows(RemoteException.class, () -> system.setServicesFloors(-1, 0, false));

        assertThrows(RemoteException.class, () -> system.setServicesFloors(1, 9, false));
        assertThrows(RemoteException.class, () -> system.setServicesFloors(0, -5, false));

        assertThrows(RemoteException.class, () -> system.getServicesFloors(6, 0));
        assertThrows(RemoteException.class, () -> system.getServicesFloors(-1, 0));

        assertThrows(RemoteException.class, () -> system.getServicesFloors(0, 7));
        assertThrows(RemoteException.class, () -> system.getServicesFloors(0, -7));
    }

    @Test
    public void testInvalidGetAndSetCommittedDirectionThrowsException() {
        assertThrows(RemoteException.class, () -> system.setCommittedDirection(6, 2));
        assertThrows(RemoteException.class, () -> system.setCommittedDirection(-1, 0));

        assertThrows(RemoteException.class, () -> system.setCommittedDirection(1, 9));
        assertThrows(RemoteException.class, () -> system.setCommittedDirection(0, -5));

        assertThrows(RemoteException.class, () -> system.getCommittedDirection(6));
        assertThrows(RemoteException.class, () -> system.getCommittedDirection(-1));
    }

    @Test
    public void testInvalidGetElevatorCapacityThrowsException() {
        assertThrows(RemoteException.class, () -> system.getElevatorCapacity(6));
        assertThrows(RemoteException.class, () -> system.getElevatorCapacity(-1));
    }

    @Test
    public void testInvalidGetElevatorHeightThrowsException() {
        assertThrows(RemoteException.class, () -> system.getElevatorWeight(6));
        assertThrows(RemoteException.class, () -> system.getElevatorWeight(-1));
    }

    @Test
    public void testInvalidGetElevatorSpeedThrowsException() {
        assertThrows(RemoteException.class, () -> system.getElevatorSpeed(6));
        assertThrows(RemoteException.class, () -> system.getElevatorSpeed(-1));
    }

    @Test
    public void testInvalidGetElevatorPositionThrowsException() {
        assertThrows(RemoteException.class, () -> system.getElevatorPosition(6));
        assertThrows(RemoteException.class, () -> system.getElevatorPosition(-1));
    }

    @Test
    public void testInvalidGetElevatorFloorThrowsException() {
        assertThrows(RemoteException.class, () -> system.getElevatorFloor(6));
        assertThrows(RemoteException.class, () -> system.getElevatorFloor(-1));
    }

    @Test
    public void testInvalidGetElevatorDoorStatusThrowsException() {
        assertThrows(RemoteException.class, () -> system.getElevatorDoorStatus(6));
        assertThrows(RemoteException.class, () -> system.getElevatorDoorStatus(-1));
    }

    @Test
    public void testInvalidGetElevatorButtonStatusThrowsException() {
        assertThrows(RemoteException.class, () -> system.getElevatorButton(6, 0));
        assertThrows(RemoteException.class, () -> system.getElevatorButton(-1, 0));

        assertThrows(RemoteException.class, () -> system.getElevatorButton(0, 7));
        assertThrows(RemoteException.class, () -> system.getElevatorButton(0, -1));
    }

}