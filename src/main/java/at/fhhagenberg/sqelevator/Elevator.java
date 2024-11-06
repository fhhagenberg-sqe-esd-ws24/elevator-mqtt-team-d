package at.fhhagenberg.sqelevator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Elevator {
    private final int mCapacity;
    private final int mNumOfFloors;
    private int mSpeed = 0;
    private int mAcceleration = 0;
    private int mDirection = IElevator.ELEVATOR_DIRECTION_UNCOMMITTED;
    private int mElevatorDoorStatus = IElevator.ELEVATOR_DOORS_CLOSED;
    private int mCurrentFloor = 0;
    private int mTargetFloor = 0;
    private int mCurrentWeight = 0;
    private final boolean[] mButtonStatus;
    private final boolean[] mFloorService;

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

    public int getDirection() {
        return mDirection;
    }

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

    public int getAcceleration() {
        return mAcceleration;
    }

    public boolean setAcceleration(int acceleration) {
        if (acceleration != mAcceleration) {
            mAcceleration = acceleration;
            return true;
        }
        return false;
    }

    public boolean getElevatorButton(int floor) {
        if(floor < 0 || floor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        return mButtonStatus[floor];
    }

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

    public int getElevatorDoorStatus() {
        return mElevatorDoorStatus;
    }

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

    public int getCurrentFloor() {
        return mCurrentFloor;
    }

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

    public int getTargetFloor() {
        return mTargetFloor;
    }

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

    public boolean getFloorService(int floor) {
        if(floor < 0 || floor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        return mFloorService[floor];
    }

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

    public int getWeight() {
        return mCurrentWeight;
    }

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

    public int getSpeed() {
        return mSpeed;
    }

    public boolean setSpeed(int speed) {
        if (speed != mSpeed) {
            mSpeed = speed;
            return true;
        }
        return false;
    }

    public int getCapacity() {
        return mCapacity;
    }
}
