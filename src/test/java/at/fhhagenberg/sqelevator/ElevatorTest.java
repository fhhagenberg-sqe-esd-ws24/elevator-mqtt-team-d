package at.fhhagenberg.sqelevator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

/**
 * This class tests the Elevator class.
 */
public class ElevatorTest {

    @Test
    public void testDirectionDefault() {
        Elevator elevator = new Elevator(5, 10, 100);

        Assertions.assertEquals(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED, elevator.getDirection());
    }

    @Test
    public void testDirections() {
        Elevator elevator = new Elevator(5, 10, 100);

        Assertions.assertDoesNotThrow(() -> {
            elevator.setDirection(IElevator.ELEVATOR_DIRECTION_UP);
        });
        Assertions.assertEquals(IElevator.ELEVATOR_DIRECTION_UP, elevator.getDirection());

        Assertions.assertDoesNotThrow(() -> {
            elevator.setDirection(IElevator.ELEVATOR_DIRECTION_DOWN);
        });
        Assertions.assertEquals(IElevator.ELEVATOR_DIRECTION_DOWN, elevator.getDirection());

        Assertions.assertDoesNotThrow(() -> {
            elevator.setDirection(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED);
        });
        Assertions.assertEquals(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED, elevator.getDirection());
    }

    @Test
    public void testDirectionThrows() {
        Elevator elevator = new Elevator(5, 10, 100);

        RemoteException exception = Assertions.assertThrows(RemoteException.class, () -> {
            elevator.setDirection(-1);
        });
        Assertions.assertEquals(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED, elevator.getDirection());
        Assertions.assertEquals("Invalid parameter", exception.getMessage());

        exception = Assertions.assertThrows(RemoteException.class, () -> {
            elevator.setDirection(3);
        });
        Assertions.assertEquals(IElevator.ELEVATOR_DIRECTION_UNCOMMITTED, elevator.getDirection());
        Assertions.assertEquals("Invalid parameter", exception.getMessage());
    }

    @Test
    public void testAcceleration() {
        Elevator elevator = new Elevator(5, 10, 100);

        Assertions.assertEquals(0, elevator.getAcceleration());

        elevator.setAcceleration(10);
        Assertions.assertEquals(10, elevator.getAcceleration());

        elevator.setAcceleration(-10);
        Assertions.assertEquals(-10, elevator.getAcceleration());
    }
}
