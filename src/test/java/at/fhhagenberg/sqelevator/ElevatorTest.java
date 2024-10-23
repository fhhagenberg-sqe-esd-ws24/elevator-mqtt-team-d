package at.fhhagenberg.sqelevator;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

/**
 * This class tests the Elevator class.
 */
public class ElevatorTest {

    @Test
    public void testDirectionDefault() {
        Elevator elevator = new Elevator(5, 10, 100);

        assertEquals(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED, elevator.getDirection());
    }

    @Test
    public void testDirections() {
        Elevator elevator = new Elevator(5, 10, 100);

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
        Elevator elevator = new Elevator(5, 10, 100);

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
        Elevator elevator = new Elevator(5, 10, 100);

        assertEquals(0, elevator.getAcceleration());

        elevator.setAcceleration(10);
        assertEquals(10, elevator.getAcceleration());

        elevator.setAcceleration(-10);
        assertEquals(-10, elevator.getAcceleration());
    }

    @Test
    public void testElevatorButton() {
        Elevator elevator = new Elevator(2, 10, 100);

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
        Elevator elevator = new Elevator(5, 10, 100);

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
        Elevator elevator = new Elevator(5, 10, 100);

        RemoteException exception = assertThrows(RemoteException.class, () -> {
            elevator.setElevatorButton(true, -1);
        });
        assertEquals("Invalid parameter", exception.getMessage());

        exception = assertThrows(RemoteException.class, () -> {
            elevator.setElevatorButton(true, 6);
        });
        assertEquals("Invalid parameter", exception.getMessage());
    }
}
