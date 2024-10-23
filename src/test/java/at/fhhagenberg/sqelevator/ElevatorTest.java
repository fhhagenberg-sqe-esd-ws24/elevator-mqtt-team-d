package at.fhhagenberg.sqelevator;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class tests the Elevator class.
 */
public class ElevatorTest {

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

    @Test
    public void testDirectionDefault() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED, elevator.getDirection());
    }

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

    @Test
    public void testElevatorDoorStatusDefault() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(IElevator.ELEVATOR_DOORS_CLOSED, elevator.getElevatorDoorStatus());
    }

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

    @Test
    public void testCurrentFloorDefault() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(0, elevator.getCurrentFloor());
    }

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

    @Test
    public void testTargetFloorDefault () {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(2, 10, 100);
        });

        assertEquals(0, elevator.getCurrentFloor());
    }

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

    @Test
    public void testServiceFloorDefault () {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(2, 10, 100);
        });

        assertDoesNotThrow(() -> assertFalse(elevator.getFloorService(0)));
        assertDoesNotThrow(() -> assertFalse(elevator.getFloorService(1)));
    }

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

    @Test
    public void testWeightDefault() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(0, elevator.getWeight());
    }

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

    @Test
    public void testCapacity() {
        Elevator elevator = assertDoesNotThrow(() -> {
            return new Elevator(5, 10, 100);
        });

        assertEquals(10, elevator.getCapacity());
    }
}
