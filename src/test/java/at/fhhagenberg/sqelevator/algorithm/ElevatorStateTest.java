package at.fhhagenberg.sqelevator.algorithm;

import at.fhhagenberg.sqelevator.Elevator;
import at.fhhagenberg.sqelevator.Floor;
import at.fhhagenberg.sqelevator.algorithm.ElevatorState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ElevatorStateTest {
    @Test
    public void ctorElevatorStateInvalidTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ElevatorState(1, 1, 1, new int[] {1, 2});
        });
    }

    @Test
    public void ctorElevatorStateValidTest() {
        ElevatorState elevatorState = new ElevatorState(2, 4, 1, new int[] {1, 2});

        Elevator[] elevators = elevatorState.getElevators();
        assertEquals(2, elevators.length);

        Floor[] floors = elevatorState.getFloors();
        assertEquals(4, floors.length);
    }
}
