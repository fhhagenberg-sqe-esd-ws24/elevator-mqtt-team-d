package at.fhhagenberg.sqelevator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Set;

import static at.fhhagenberg.sqelevator.IElevator.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ElevatorControlSystemTest {
    @Mock
    IElevator plcMock;

    ElevatorControlSystem ecs;

    /**
     * Set up the test environment.
     */
    @BeforeEach
    public void setUp() {
        Mockito.reset(plcMock);
        ecs = new ElevatorControlSystem(plcMock);
    }

    /**
     * Test the initialization of the elevators via the PLC.
     * @throws Exception if PLC call fails
     */
    @Test
    public void testInitializeElevatorsViaPLC() throws Exception{
        when(plcMock.getElevatorNum()).thenReturn(2);
        when(plcMock.getFloorNum()).thenReturn(3);
        when(plcMock.getElevatorCapacity(0)).thenReturn(4);
        when(plcMock.getElevatorCapacity(1)).thenReturn(5);

        ecs.initializeElevatorsViaPLC();

        Elevator[] elevators = ecs.getElevators();
        Floor[] floors = ecs.getFloors();

        assertEquals(2, elevators.length);
        assertEquals(3, floors.length);
        assertEquals(4, elevators[0].getCapacity());
        assertEquals(5, elevators[1].getCapacity());

        verify(plcMock, times(1)).getElevatorNum();
        verify(plcMock, times(1)).getFloorNum();
        verify(plcMock, times(1)).getElevatorCapacity(0);
        verify(plcMock, times(1)).getElevatorCapacity(1);
    }

    /**
     * Test the update of the data via the PLC.
     * @throws Exception if PLC call fails
     */
    @Test
    public void testUpdateDataViaPLC() throws Exception {
        when(plcMock.getCommittedDirection(0)).thenReturn(ELEVATOR_DIRECTION_UP);
        when(plcMock.getElevatorAccel(0)).thenReturn(30);
        when(plcMock.getElevatorButton(0, 0)).thenReturn(true);
        when(plcMock.getElevatorDoorStatus(0)).thenReturn(ELEVATOR_DOORS_OPEN);
        when(plcMock.getElevatorFloor(0)).thenReturn(0);
        when(plcMock.getElevatorSpeed(0)).thenReturn(10);
        when(plcMock.getElevatorWeight(0)).thenReturn(100);
        when(plcMock.getElevatorCapacity(0)).thenReturn(5);
        when(plcMock.getElevatorNum()).thenReturn(1);
        when(plcMock.getFloorButtonDown(0)).thenReturn(false);
        when(plcMock.getFloorButtonDown(1)).thenReturn(false);
        when(plcMock.getFloorButtonDown(2)).thenReturn(false);
        when(plcMock.getFloorButtonDown(3)).thenReturn(true);
        when(plcMock.getFloorButtonUp(0)).thenReturn(false);
        when(plcMock.getFloorButtonUp(1)).thenReturn(false);
        when(plcMock.getFloorButtonUp(2)).thenReturn(false);
        when(plcMock.getFloorButtonUp(3)).thenReturn(false);
        when(plcMock.getFloorNum()).thenReturn(4);
        when(plcMock.getServicesFloors(0, 1)).thenReturn(false);
        when(plcMock.getServicesFloors(0, 2)).thenReturn(false);
        when(plcMock.getServicesFloors(0, 3)).thenReturn(true);
        when(plcMock.getTarget(0)).thenReturn(3);

        ecs.initializeElevatorsViaPLC();

        ecs.updateDataViaPLC();

        Elevator[] elevators = ecs.getElevators();
        Floor[] floors = ecs.getFloors();

        assertEquals(elevators[0].getDirection(), ELEVATOR_DIRECTION_UP);
        assertTrue(elevators[0].getElevatorButton(0));
        assertEquals(elevators[0].getElevatorDoorStatus(), ELEVATOR_DOORS_OPEN);
        assertEquals(elevators[0].getCurrentFloor(), 0);
        assertTrue(elevators[0].getFloorService(3));
        assertEquals(elevators[0].getSpeed(), 10);
        assertEquals(elevators[0].getWeight(), 100);
        assertEquals(elevators[0].getCapacity(), 5);
        assertEquals(elevators[0].getTargetFloor(), 3);
        assertEquals(elevators[0].getAcceleration(), 30);

        assertFalse(floors[0].getButtonDownPressed());
        assertFalse(floors[1].getButtonDownPressed());
        assertFalse(floors[2].getButtonDownPressed());
        assertTrue(floors[3].getButtonDownPressed());
        assertFalse(floors[0].getButtonUpPressed());
        assertFalse(floors[1].getButtonUpPressed());
        assertFalse(floors[2].getButtonUpPressed());
        assertFalse(floors[3].getButtonUpPressed());

        verify(plcMock, times(1)).getCommittedDirection(0);
        verify(plcMock, times(1)).getElevatorAccel(0);
        verify(plcMock, times(1)).getElevatorButton(0, 0);
        verify(plcMock, times(1)).getElevatorButton(0, 1);
        verify(plcMock, times(1)).getElevatorButton(0, 2);
        verify(plcMock, times(1)).getElevatorButton(0, 3);
        verify(plcMock, times(1)).getElevatorDoorStatus(0);
        verify(plcMock, times(1)).getElevatorFloor(0);
        verify(plcMock, times(1)).getElevatorSpeed(0);
        verify(plcMock, times(1)).getElevatorWeight(0);
        verify(plcMock, times(1)).getElevatorCapacity(0);
        verify(plcMock, times(1)).getElevatorNum();
        verify(plcMock, times(1)).getFloorButtonDown(0);
        verify(plcMock, times(1)).getFloorButtonDown(1);
        verify(plcMock, times(1)).getFloorButtonDown(2);
        verify(plcMock, times(1)).getFloorButtonDown(3);
        verify(plcMock, times(1)).getFloorButtonUp(0);
        verify(plcMock, times(1)).getFloorButtonUp(1);
        verify(plcMock, times(1)).getFloorButtonUp(2);
        verify(plcMock, times(1)).getFloorButtonUp(3);
        verify(plcMock, times(1)).getFloorNum();
        verify(plcMock, times(1)).getServicesFloors(0, 1);
        verify(plcMock, times(1)).getServicesFloors(0, 2);
        verify(plcMock, times(1)).getServicesFloors(0, 3);
        verify(plcMock, times(1)).getTarget(0);
    }

    @Test
    public void testGetUpdateTopics() throws Exception{
        when(plcMock.getCommittedDirection(0)).thenReturn(ELEVATOR_DIRECTION_UP);
        when(plcMock.getElevatorAccel(0)).thenReturn(30);
        when(plcMock.getElevatorButton(0, 0)).thenReturn(true);
        when(plcMock.getElevatorDoorStatus(0)).thenReturn(ELEVATOR_DOORS_OPEN);
        when(plcMock.getElevatorFloor(0)).thenReturn(0);
        when(plcMock.getElevatorSpeed(0)).thenReturn(10);
        when(plcMock.getElevatorWeight(0)).thenReturn(100);
        when(plcMock.getElevatorCapacity(0)).thenReturn(5);
        when(plcMock.getElevatorNum()).thenReturn(1);
        when(plcMock.getFloorButtonDown(0)).thenReturn(false);
        when(plcMock.getFloorButtonUp(0)).thenReturn(false);
        when(plcMock.getFloorNum()).thenReturn(1);
        when(plcMock.getTarget(0)).thenReturn(0);

        ecs.initializeElevatorsViaPLC();

        ecs.updateDataViaPLC();

        Set<String> s = ecs.getUpdateTopics();

        assertTrue(s.contains("elevator/0/direction"));
        assertTrue(s.contains("elevator/0/acceleration"));
        assertTrue(s.contains("elevator/0/speed"));
        assertTrue(s.contains("elevator/0/door_status"));
        assertTrue(s.contains("elevator/0/weight"));
        assertTrue(s.contains("elevator/0/floor_requested/0"));
        assertEquals(6, s.size());

        verify(plcMock, times(1)).getCommittedDirection(0);
        verify(plcMock, times(1)).getElevatorAccel(0);
        verify(plcMock, times(1)).getElevatorButton(0, 0);
        verify(plcMock, times(1)).getElevatorDoorStatus(0);
        verify(plcMock, times(1)).getElevatorFloor(0);
        verify(plcMock, times(1)).getElevatorSpeed(0);
        verify(plcMock, times(1)).getElevatorWeight(0);
        verify(plcMock, times(1)).getElevatorCapacity(0);
        verify(plcMock, times(1)).getElevatorNum();
        verify(plcMock, times(1)).getFloorNum();
        verify(plcMock, times(1)).getTarget(0);
    }

    @Test
    public void testMultipleUpdates() throws Exception {
        when(plcMock.getCommittedDirection(0)).thenReturn(ELEVATOR_DIRECTION_UP);
        when(plcMock.getElevatorAccel(0)).thenReturn(30);
        when(plcMock.getElevatorButton(0, 0)).thenReturn(true);
        when(plcMock.getElevatorDoorStatus(0)).thenReturn(ELEVATOR_DOORS_OPEN);
        when(plcMock.getElevatorFloor(0)).thenReturn(0);
        when(plcMock.getElevatorSpeed(0)).thenReturn(10);
        when(plcMock.getElevatorWeight(0)).thenReturn(100);
        when(plcMock.getElevatorCapacity(0)).thenReturn(5);
        when(plcMock.getElevatorNum()).thenReturn(1);
        when(plcMock.getFloorButtonDown(0)).thenReturn(false);
        when(plcMock.getFloorButtonDown(1)).thenReturn(false);
        when(plcMock.getFloorButtonDown(2)).thenReturn(false);
        when(plcMock.getFloorButtonDown(3)).thenReturn(true);
        when(plcMock.getFloorButtonUp(0)).thenReturn(false);
        when(plcMock.getFloorButtonUp(1)).thenReturn(false);
        when(plcMock.getFloorButtonUp(2)).thenReturn(false);
        when(plcMock.getFloorButtonUp(3)).thenReturn(false);
        when(plcMock.getFloorNum()).thenReturn(4);
        when(plcMock.getServicesFloors(0, 1)).thenReturn(false);
        when(plcMock.getServicesFloors(0, 2)).thenReturn(false);
        when(plcMock.getServicesFloors(0, 3)).thenReturn(true);
        when(plcMock.getTarget(0)).thenReturn(3);

        ecs.initializeElevatorsViaPLC();

        ecs.updateDataViaPLC();

        Elevator[] elevators = ecs.getElevators();
        Floor[] floors = ecs.getFloors();

        assertEquals(elevators[0].getDirection(), ELEVATOR_DIRECTION_UP);
        assertTrue(elevators[0].getElevatorButton(0));
        assertEquals(elevators[0].getElevatorDoorStatus(), ELEVATOR_DOORS_OPEN);
        assertEquals(elevators[0].getCurrentFloor(), 0);
        assertTrue(elevators[0].getFloorService(3));
        assertEquals(elevators[0].getSpeed(), 10);
        assertEquals(elevators[0].getWeight(), 100);
        assertEquals(elevators[0].getCapacity(), 5);
        assertEquals(elevators[0].getTargetFloor(), 3);
        assertEquals(elevators[0].getAcceleration(), 30);

        assertFalse(floors[0].getButtonDownPressed());
        assertFalse(floors[1].getButtonDownPressed());
        assertFalse(floors[2].getButtonDownPressed());
        assertTrue(floors[3].getButtonDownPressed());
        assertFalse(floors[0].getButtonUpPressed());
        assertFalse(floors[1].getButtonUpPressed());
        assertFalse(floors[2].getButtonUpPressed());
        assertFalse(floors[3].getButtonUpPressed());

        when(plcMock.getFloorButtonDown(2)).thenReturn(true);
        when(plcMock.getFloorButtonDown(3)).thenReturn(false);
        when(plcMock.getFloorButtonUp(0)).thenReturn(true);
        when(plcMock.getServicesFloors(0, 2)).thenReturn(true);
        when(plcMock.getServicesFloors(0, 3)).thenReturn(false);

        ecs.updateDataViaPLC();

        elevators = ecs.getElevators();
        floors = ecs.getFloors();

        assertEquals(elevators[0].getDirection(), ELEVATOR_DIRECTION_UP);
        assertTrue(elevators[0].getElevatorButton(0));
        assertEquals(elevators[0].getElevatorDoorStatus(), ELEVATOR_DOORS_OPEN);
        assertEquals(elevators[0].getCurrentFloor(), 0);
        assertFalse(elevators[0].getFloorService(3));
        assertTrue(elevators[0].getFloorService(0));
        assertTrue(elevators[0].getFloorService(2));
        assertEquals(elevators[0].getSpeed(), 10);
        assertEquals(elevators[0].getWeight(), 100);
        assertEquals(elevators[0].getCapacity(), 5);
        assertEquals(elevators[0].getTargetFloor(), 3);
        assertEquals(elevators[0].getAcceleration(), 30);

        assertFalse(floors[0].getButtonDownPressed());
        assertFalse(floors[1].getButtonDownPressed());
        assertTrue(floors[2].getButtonDownPressed());
        assertFalse(floors[3].getButtonDownPressed());
        assertTrue(floors[0].getButtonUpPressed());
        assertFalse(floors[1].getButtonUpPressed());
        assertFalse(floors[2].getButtonUpPressed());
        assertFalse(floors[3].getButtonUpPressed());


        verify(plcMock, times(2)).getCommittedDirection(0);
        verify(plcMock, times(2)).getElevatorAccel(0);
        verify(plcMock, times(2)).getElevatorButton(0, 0);
        verify(plcMock, times(2)).getElevatorButton(0, 1);
        verify(plcMock, times(2)).getElevatorButton(0, 2);
        verify(plcMock, times(2)).getElevatorButton(0, 3);
        verify(plcMock, times(2)).getElevatorDoorStatus(0);
        verify(plcMock, times(2)).getElevatorFloor(0);
        verify(plcMock, times(2)).getElevatorSpeed(0);
        verify(plcMock, times(2)).getElevatorWeight(0);
        verify(plcMock, times(1)).getElevatorCapacity(0);
        verify(plcMock, times(1)).getElevatorNum();
        verify(plcMock, times(2)).getFloorButtonDown(0);
        verify(plcMock, times(2)).getFloorButtonDown(1);
        verify(plcMock, times(2)).getFloorButtonDown(2);
        verify(plcMock, times(2)).getFloorButtonDown(3);
        verify(plcMock, times(2)).getFloorButtonUp(0);
        verify(plcMock, times(2)).getFloorButtonUp(1);
        verify(plcMock, times(2)).getFloorButtonUp(2);
        verify(plcMock, times(2)).getFloorButtonUp(3);
        verify(plcMock, times(1)).getFloorNum();
        verify(plcMock, times(2)).getServicesFloors(0, 1);
        verify(plcMock, times(2)).getServicesFloors(0, 2);
        verify(plcMock, times(2)).getServicesFloors(0, 3);
        verify(plcMock, times(2)).getTarget(0);
    }

}