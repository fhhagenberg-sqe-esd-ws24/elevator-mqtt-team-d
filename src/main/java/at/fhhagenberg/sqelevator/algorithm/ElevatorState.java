package at.fhhagenberg.sqelevator.algorithm;

import at.fhhagenberg.sqelevator.Elevator;
import at.fhhagenberg.sqelevator.Floor;

public class ElevatorState {

    /**< The elevators. */
    private Elevator[] mElevators = null;
    /**< The floors. */
    private Floor[] mFloors = null;
    /**< The height of all floors. */
    private final int mFloorHeight;

    public ElevatorState(int nrOfElevators, int nrOfFloors, int floorHeight, int[] elevatorCapacities) throws IllegalArgumentException {
        if (elevatorCapacities.length != nrOfElevators) {
            throw new IllegalArgumentException("The number of elevators must be equal to the amount of elevator capacities");
        }

        mFloorHeight = floorHeight;

        // Set up elevators
        mElevators = new Elevator[nrOfElevators];
        for (int i = 0; i < nrOfElevators; ++i) {
            mElevators[i] = new Elevator(nrOfFloors, elevatorCapacities[i]);
        }

        // Set up floors
        mFloors = new Floor[nrOfFloors];
        for (int i = 0; i < nrOfFloors; ++i) {
            mFloors[i] = new Floor();
        }
    }

    public Elevator[] getElevators() {
        return mElevators;
    }

    public Floor[] getFloors() {
        return mFloors;
    }
}
