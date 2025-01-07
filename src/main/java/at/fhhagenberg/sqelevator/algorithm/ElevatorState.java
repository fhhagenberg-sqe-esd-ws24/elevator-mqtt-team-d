package at.fhhagenberg.sqelevator.algorithm;

import at.fhhagenberg.sqelevator.Elevator;
import at.fhhagenberg.sqelevator.Floor;

/**
 * Class which represents the state of the elevators and floors.
 */
public class ElevatorState {

    /**< The elevators. */
    private Elevator[] mElevators = null;
    /**< The floors. */
    private Floor[] mFloors = null;
    /**< The height of all floors. */
    private final int mFloorHeight;

    /**
     * CTor which instantiates all members.
     * @param nrOfElevators The number of elevators.
     * @param nrOfFloors The number of floors.
     * @param floorHeight The height of all floors.
     * @param elevatorCapacities The capacities of the elevators.
     * @throws IllegalArgumentException If the number of elevators does not match the number of elevator capacities.
     */
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

    /**
     * Returns the elevators.
     * @return The elevators.
     */
    public Elevator[] getElevators() {
        return mElevators;
    }

    /**
     * Returns the floors.
     * @return The floors.
     */
    public Floor[] getFloors() {
        return mFloors;
    }
}
