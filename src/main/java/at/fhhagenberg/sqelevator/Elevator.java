package at.fhhagenberg.sqelevator;

import java.util.Arrays;

/**
 * Class which represents an elevator.
 */
public class Elevator {
    /**< The capacity of the elevator. */
    private final int mCapacity;
    /**< The number of floors. */
    private final int mNumOfFloors;
    /**< The speed of the elevator. */
    private int mSpeed = 0;
    /**< The acceleration of the elevator. */
    private int mAcceleration = 0;
    /**< The direction of the elevator. */
    private int mDirection = IElevator.ELEVATOR_DIRECTION_UNCOMMITTED;
    /**< The status of the elevator buttons. */
    private int mElevatorDoorStatus = IElevator.ELEVATOR_DOORS_CLOSED;
    /**< The current floor of the elevator. */
    private int mCurrentFloor = 0;
    /**< The target floor of the elevator. */
    private int mTargetFloor = 0;
    /**< The current weight of the elevator. */
    private int mCurrentWeight = 0;
    /**< The status of the elevator buttons. */
    private final boolean[] mButtonStatus;
    /**< The status of the floor service. */
    private final boolean[] mFloorService;

    /**
     * CTor which instantiates all members.
     *
     * @param numOfFloors The number of floors.
     * @param capacity The capacity of the elevator.
     */
    public Elevator(int numOfFloors, int capacity) {
        if(numOfFloors < 0)
            throw new IllegalArgumentException("Invalid constructor parameter");

        if(capacity < 0)
            throw new IllegalArgumentException("Invalid constructor parameter");

        mCapacity = capacity;
        mNumOfFloors = numOfFloors;
        mButtonStatus = new boolean[numOfFloors];
        mFloorService = new boolean[numOfFloors];
        Arrays.fill(mButtonStatus, false);
        Arrays.fill(mFloorService, true);
    }

    /**
     * Returns the direction of the elevator.
     * @return The direction of the elevator.
     */
    public int getDirection() {
        return mDirection;
    }

    /**
     * Sets the direction of the elevator.
     * @param direction The direction of the elevator.
     * @return True if the direction has changed, false otherwise.
     */
    public boolean setDirection(int direction) {
        if(direction < IElevator.ELEVATOR_DIRECTION_UP || direction > IElevator.ELEVATOR_DIRECTION_UNCOMMITTED) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        if (direction != mDirection) {
            mDirection = direction;
            return true;
        }
        return false;
    }

    /**
     * Returns the acceleration of the elevator.
     * @return The acceleration of the elevator.
     */
    public int getAcceleration() {
        return mAcceleration;
    }

    /**
     * Sets the acceleration of the elevator.
     * @param acceleration The acceleration of the elevator.
     * @return True if the acceleration has changed, false otherwise.
     */
    public boolean setAcceleration(int acceleration) {
        if (acceleration != mAcceleration) {
            mAcceleration = acceleration;
            return true;
        }
        return false;
    }

    /**
     * Returns the status of the elevator button.
     * @param floor The floor.
     * @return The status of the specified elevator button.
     */
    public boolean getElevatorButton(int floor) {
        if(floor < 0 || floor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        return mButtonStatus[floor];
    }

    /**
     * Sets the status of the elevator button.
     * @param buttonStatus The status of the elevator button.
     * @param floor The floor.
     * @return True if the status has changed, false otherwise.
     */
    public boolean setElevatorButton(boolean buttonStatus, int floor) {
        if(floor < 0 || floor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        if (buttonStatus != mButtonStatus[floor]) {
            mButtonStatus[floor] = buttonStatus;
            return true;
        }
        return false;
    }

    /**
     * Returns the status of the elevator door.
     * @return The status of the elevator door.
     */
    public int getElevatorDoorStatus() {
        return mElevatorDoorStatus;
    }

    /**
     * Sets the status of the elevator door.
     * @param doorStatus The status of the elevator door.
     * @return True if the status has changed, false otherwise.
     */
    public boolean setElevatorDoorStatus(int doorStatus) {
        if(doorStatus < IElevator.ELEVATOR_DOORS_OPEN || doorStatus > IElevator.ELEVATOR_DOORS_CLOSING) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        if (doorStatus != mElevatorDoorStatus) {
            mElevatorDoorStatus = doorStatus;
            return true;
        }
        return false;
    }

    /**
     * Returns the current floor of the elevator.
     * @return The current floor of the elevator.
     */
    public int getCurrentFloor() {
        return mCurrentFloor;
    }

    /**
     * Sets the current floor of the elevator.
     * @param currentFloor The current floor of the elevator.
     * @return True if the current floor has changed, false otherwise.
     */
    public boolean setCurrentFloor(int currentFloor) {
        if(currentFloor < 0 || currentFloor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        if (currentFloor != mCurrentFloor) {
            mCurrentFloor = currentFloor;
            return true;
        }
        return false;
    }

    /**
     * Returns the target floor of the elevator.
     * @return The target floor of the elevator.
     */
    public int getTargetFloor() {
        return mTargetFloor;
    }

    /**
     * Sets the target floor of the elevator.
     * @param targetFloor The target floor of the elevator.
     * @return True if the target floor has changed, false otherwise.
     */
    public boolean setTargetFloor(int targetFloor) {
        if(targetFloor < 0 || targetFloor >= mNumOfFloors){
            throw new IllegalArgumentException("Invalid parameter");
        }

        if (targetFloor != mTargetFloor) {
            mTargetFloor = targetFloor;
            return true;
        }
        return false;
    }

    /**
     * Returns the status of the floor service.
     * @param floor The floor.
     * @return The status of the floor service.
     */
    public boolean getFloorService(int floor) {
        if(floor < 0 || floor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        return mFloorService[floor];
    }

    /**
     * Sets the status of the floor service.
     * @param service The status of the floor service.
     * @param floor The floor.
     * @return True if the status has changed, false otherwise.
     */
    public boolean setFloorService(boolean service, int floor) {
        if(floor < 1 || floor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        if (service != mFloorService[floor]) {
            mFloorService[floor] = service;
            return true;
        }
        return false;
    }

    /**
     * Returns the weight of the elevator.
     * @return The weight of the elevator.
     */
    public int getWeight() {
        return mCurrentWeight;
    }

    /**
     * Sets the weight of the elevator.
     * @param weight The weight of the elevator.
     * @return True if the weight has changed, false otherwise.
     */
    public boolean setWeight(int weight) {
        if(weight < 0) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        if (weight != mCurrentWeight) {
            mCurrentWeight = weight;
            return true;
        }
        return false;
    }

    /**
     * Returns the speed of the elevator.
     * @return The speed of the elevator.
     */
    public int getSpeed() {
        return mSpeed;
    }

    /**
     * Sets the speed of the elevator.
     * @param speed The speed of the elevator.
     * @return True if the speed has changed, false otherwise.
     */
    public boolean setSpeed(int speed) {
        if (speed != mSpeed) {
            mSpeed = speed;
            return true;
        }
        return false;
    }

    /**
     * Returns the capacity of the elevator.
     * @return The capacity of the elevator.
     */
    public int getCapacity() {
        return mCapacity;
    }
}
