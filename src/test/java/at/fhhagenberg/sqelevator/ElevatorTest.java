package at.fhhagenberg.sqelevator;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class tests the Elevator class.
 */
public class ElevatorTest {

    /**
     * Tests whether the constructor throws exceptions.
     */
    @Test
    public void testConstructorThrow() {
        RemoteException exception = assertThrows(RemoteException.class, () -> {
            new Elevator(-10, 1, 1);
        });
        assertEquals("Invalid constructor parameter", exception.getMessage());

        exception = assertThrows(RemoteException.class, () -> {
            new Elevator(10, -10, 1);
        });
        assertEquals("Invalid constructor parameter", exception.getMessage());

        exception = assertThrows(RemoteException.class, () -> {
            new Elevator(10, 1, -10);
        });
        assertEquals("Invalid constructor parameter", exception.getMessage());
    }

    /**
     * Tests default value of direction.
     */
    @Test
    public void testDirectionDefault() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED, elevator.getDirection());
    }

    /**
     * Tests setter and getter of direction.
     */
    @Test
    public void testDirections() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertDoesNotThrow(() -> {
            elevator.setDirection(IElevator.ELEVATOR_DIRECTION_UP);
        });
        assertEquals(IElevator.ELEVATOR_DIRECTION_UP, elevator.getDirection());

        assertDoesNotThrow(() -> {
            elevator.setDirection(IElevator.ELEVATOR_DIRECTION_DOWN);
        });
        assertEquals(IElevator.ELEVATOR_DIRECTION_DOWN, elevator.getDirection());

        assertDoesNotThrow(() -> {
            elevator.setDirection(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED);
        });
        assertEquals(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED, elevator.getDirection());
    }

    /**
     * Tests whether the setter methods of direction throws exceptions.
     */
    @Test
    public void testDirectionThrows() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        RemoteException exception = assertThrows(RemoteException.class, () -> {
            elevator.setDirection(-1);
        });
        assertEquals(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED, elevator.getDirection());
        assertEquals("Invalid parameter", exception.getMessage());

        exception = assertThrows(RemoteException.class, () -> {
            elevator.setDirection(3);
        });
        assertEquals(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED, elevator.getDirection());
        assertEquals("Invalid parameter", exception.getMessage());
    }

    /**
     * Tests acceleration method.
     */
    @Test
    public void testAcceleration() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(0, elevator.getAcceleration());

        elevator.setAcceleration(10);
        assertEquals(10, elevator.getAcceleration());

        elevator.setAcceleration(-10);
        assertEquals(-10, elevator.getAcceleration());
    }

    /**
     * Tests getter and setter of elevator buttons.
     */
    @Test
    public void testElevatorButton() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(2, 10, 100);
        });

        assertDoesNotThrow(() -> assertEquals(false, elevator.getElevatorButton(0)));
        assertDoesNotThrow(() -> assertEquals(false, elevator.getElevatorButton(1)));

        assertDoesNotThrow(() -> elevator.setElevatorButton(true, 0));
        assertDoesNotThrow(() -> assertEquals(true, elevator.getElevatorButton(0)));

        assertDoesNotThrow(() -> elevator.setElevatorButton(true, 1));
        assertDoesNotThrow(() -> assertEquals(true, elevator.getElevatorButton(1)));

        assertDoesNotThrow(() -> elevator.setElevatorButton(false, 0));
        assertDoesNotThrow(() -> assertEquals(false, elevator.getElevatorButton(0)));

        assertDoesNotThrow(() -> elevator.setElevatorButton(false, 1));
        assertDoesNotThrow(() -> assertEquals(false, elevator.getElevatorButton(1)));
    }

    /**
     * Tests whether getElevatorButton throw exceptions.
     */
    @Test
    public void testGetElevatorButtonThrow() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        RemoteException exception = assertThrows(RemoteException.class, () -> {
            elevator.getElevatorButton(-1);
        });
        assertEquals("Invalid parameter", exception.getMessage());

        exception = assertThrows(RemoteException.class, () -> {
            elevator.getElevatorButton(6);
        });
        assertEquals("Invalid parameter", exception.getMessage());
    }

    /**
     * Tests whether setElevatorButton throw exceptions.
     */
    @Test
    public void testSetElevatorButtonThrow() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        RemoteException exception = assertThrows(RemoteException.class, () -> {
            elevator.setElevatorButton(true, -1);
        });
        assertEquals("Invalid parameter", exception.getMessage());

        exception = assertThrows(RemoteException.class, () -> {
            elevator.setElevatorButton(true, 6);
        });
        assertEquals("Invalid parameter", exception.getMessage());
    }

    /**
     * Tests default value of elevator door status.
     */
    @Test
    public void testElevatorDoorStatusDefault() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(IElevator.ELEVATOR_DOORS_CLOSED, elevator.getElevatorDoorStatus());
    }

    /**
     * Tests setter and getter methods of elevator door status.
     */
    @Test
    public void testElevatorDoorStatus() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertDoesNotThrow(() -> elevator.setElevatorDoorStatus(IElevator.ELEVATOR_DOORS_OPEN));
        assertEquals(IElevator.ELEVATOR_DOORS_OPEN, elevator.getElevatorDoorStatus());

        assertDoesNotThrow(() -> elevator.setElevatorDoorStatus(IElevator.ELEVATOR_DOORS_CLOSED));
        assertEquals(IElevator.ELEVATOR_DOORS_CLOSED, elevator.getElevatorDoorStatus());

        assertDoesNotThrow(() -> elevator.setElevatorDoorStatus(IElevator.ELEVATOR_DOORS_OPENING));
        assertEquals(IElevator.ELEVATOR_DOORS_OPENING, elevator.getElevatorDoorStatus());

        assertDoesNotThrow(() -> elevator.setElevatorDoorStatus(IElevator.ELEVATOR_DOORS_CLOSING));
        assertEquals(IElevator.ELEVATOR_DOORS_CLOSING, elevator.getElevatorDoorStatus());
    }

    /**
     * Tests whether the setter methods of elevator door status throws exceptions.
     */
    @Test
    public void testElevatorDoorStatusThrow() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        RemoteException exception = assertThrows(RemoteException.class, () -> {
            elevator.setElevatorDoorStatus(0);
        });
        assertEquals(IElevator.ELEVATOR_DOORS_CLOSED, elevator.getElevatorDoorStatus());
        assertEquals("Invalid parameter", exception.getMessage());

        exception = assertThrows(RemoteException.class, () -> {
            elevator.setElevatorDoorStatus(5);
        });
        assertEquals(IElevator.ELEVATOR_DOORS_CLOSED, elevator.getElevatorDoorStatus());
        assertEquals("Invalid parameter", exception.getMessage());
    }

    /**
     * Tests the default value of current floor.
     */
    @Test
    public void testCurrentFloorDefault() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(0, elevator.getCurrentFloor());
    }

    /**
     * Tests the setter and getter methods of current floor.
     */
    @Test
    public void testCurrentFloor () {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(2, 10, 100);
        });

        assertDoesNotThrow(() -> elevator.setCurrentFloor(0));
        assertEquals(0, elevator.getCurrentFloor());

        assertDoesNotThrow(() -> elevator.setCurrentFloor(1));
        assertEquals(1, elevator.getCurrentFloor());
    }

    /**
     * Tests whether the setter method of current floor throws exceptions.
     */
    @Test
    public void testCurrentFloorThrow () {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(2, 10, 100);
        });

        RemoteException exception = assertThrows(RemoteException.class, () -> elevator.setCurrentFloor(-1));
        assertEquals(0, elevator.getCurrentFloor());
        assertEquals("Invalid parameter", exception.getMessage());

        exception = assertThrows(RemoteException.class, () -> elevator.setCurrentFloor(3));
        assertEquals(0, elevator.getCurrentFloor());
        assertEquals("Invalid parameter", exception.getMessage());
    }

    /**
     * Test the default value of target floor.
     */
    @Test
    public void testTargetFloorDefault () {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(2, 10, 100);
        });

        assertEquals(0, elevator.getCurrentFloor());
    }

    /**
     * Tests the setter and getter methods of target floor.
     */
    @Test
    public void testTargetFloor () {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(2, 10, 100);
        });

        assertDoesNotThrow(() -> elevator.setTargetFloor(0));
        assertEquals(0, elevator.getTargetFloor());

        assertDoesNotThrow(() -> elevator.setTargetFloor(1));
        assertEquals(1, elevator.getTargetFloor());
    }

    /**
     * Tests whether the setter methods of direction throws exceptions.
     */
    @Test
    public void testTargetFloorThrow () {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(2, 10, 100);
        });

        RemoteException exception = assertThrows(RemoteException.class, () -> elevator.setTargetFloor(-1));
        assertEquals(0, elevator.getTargetFloor());
        assertEquals("Invalid parameter", exception.getMessage());

        exception = assertThrows(RemoteException.class, () -> elevator.setTargetFloor(3));
        assertEquals(0, elevator.getTargetFloor());
        assertEquals("Invalid parameter", exception.getMessage());
    }

    /**
     * Tests the default value of floor service.
     */
    @Test
    public void testServiceFloorDefault () {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(2, 10, 100);
        });

        assertDoesNotThrow(() -> assertFalse(elevator.getFloorService(0)));
        assertDoesNotThrow(() -> assertFalse(elevator.getFloorService(1)));
    }

    /**
     * Tests the getter and setter methods of floor service.
     */
    @Test
    public void testServiceFloor () {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(2, 10, 100);
        });

        assertDoesNotThrow(() -> elevator.setFloorService(true, 1));
        assertDoesNotThrow(() -> assertTrue(elevator.getFloorService(1)));

        assertDoesNotThrow(() -> elevator.setFloorService(false, 1));
        assertDoesNotThrow(() -> assertFalse(elevator.getFloorService(1)));
    }

    /**
     * Tests whether the setter methods of service floor throws exceptions.
     */
    @Test
    public void testServiceFloorThrow () {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(2, 10, 100);
        });

        RemoteException exception = assertThrows(RemoteException.class, () -> elevator.setFloorService(true, -1));
        assertEquals("Invalid parameter", exception.getMessage());

        exception = assertThrows(RemoteException.class, () -> elevator.setFloorService(true, 3));
        assertEquals("Invalid parameter", exception.getMessage());
    }

    /**
     * Tests the getter method of weight.
     */
    @Test
    public void testWeightDefault() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(0, elevator.getWeight());
    }

    /**
     * Tests the default, getter and setter methods of speed.
     */
    @Test
    public void testSpeed() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(0, elevator.getSpeed());

        elevator.setSpeed(10);
        assertEquals(10, elevator.getSpeed());

        elevator.setSpeed(-10);
        assertEquals(-10, elevator.getSpeed());
    }

    /**
     * Tests the getCapcity method.
     */
    @Test
    public void testCapacity() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(10, elevator.getCapacity());
    }
}
