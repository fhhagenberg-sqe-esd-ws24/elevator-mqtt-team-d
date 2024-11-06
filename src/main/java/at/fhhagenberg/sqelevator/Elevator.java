package at.fhhagenberg.sqelevator;

import java.rmi.RemoteException;
import java.util.Arrays;

public class Elevator {
    private final int mCapacity;
    private int mSpeed = 0;
    private int mAcceleration = 0;
    private final int mNumOfFloors;
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

    public void setDirection(int direction) {
        if(direction < IElevator.ELEVATOR_DIRECTION_UP || direction > IElevator.ELEVATOR_DIRECTION_UNCOMMITTED) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        mDirection = direction;
    }

    public int getAcceleration() {
        return mAcceleration;
    }

    public void setAcceleration(int acceleration) {
        mAcceleration = acceleration;
    }

    public boolean getElevatorButton(int floor) {
        if(floor < 0 || floor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        return mButtonStatus[floor];
    }

    public void setElevatorButton(boolean buttonStatus, int floor) {
        if(floor < 0 || floor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        mButtonStatus[floor] = buttonStatus;
    }

    public int getElevatorDoorStatus() {
        return mElevatorDoorStatus;
    }

    public void setElevatorDoorStatus(int doorStatus) {
        if(doorStatus < IElevator.ELEVATOR_DOORS_OPEN || doorStatus > IElevator.ELEVATOR_DOORS_CLOSING) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        mElevatorDoorStatus = doorStatus;
    }

    public int getCurrentFloor() {
        return mCurrentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        if(currentFloor < 0 || currentFloor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        mCurrentFloor = currentFloor;
    }

    public int getTargetFloor() {
        return mTargetFloor;
    }

    public void setTargetFloor(int targetFloor) {
        if(targetFloor < 0 || targetFloor >= mNumOfFloors){
            throw new IllegalArgumentException("Invalid parameter");
        }

        mTargetFloor = targetFloor;
    }

    public boolean getFloorService(int floor) {
        if(floor < 0 || floor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        return mFloorService[floor];
    }

    public void setFloorService(boolean service, int floor) {
        if(floor < 1 || floor >= mNumOfFloors) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        mFloorService[floor] = service;
    }

    public int getWeight() {
        return mCurrentWeight;
    }

    public void setWeight(int weight) {
        if(weight < 0) {
            throw new IllegalArgumentException("Invalid parameter");
        }

        mCurrentWeight = weight;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
    }

    public int getCapacity() {
        return mCapacity;
    }
}
