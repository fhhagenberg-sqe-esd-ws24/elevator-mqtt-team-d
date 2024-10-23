package at.fhhagenberg.sqelevator;

import java.rmi.RemoteException;

public class Elevator {
    private final int mCapacity;
    private int mSpeed;
    private int mAcceleration;
    private final int mNumOfFloors;
    private int mDirection;
    private int mElevatorDoorStatus;
    private int mCurrentFloor;
    private boolean[] mButtonStatus;

    public Elevator(int numOfFloors, int capacity) {
        mCapacity = capacity;
        mNumOfFloors = numOfFloors;
        mButtonStatus = new boolean[numOfFloors];
    }

    public int getDirection() {
        return mDirection;
    }

    public void setDirection(int direction) throws RemoteException {
        if(direction < IElevator.ELEVATOR_DIRECTION_UP || direction > IElevator.ELEVATOR_DIRECTION_UNCOMMITTED) {
            throw new RemoteException("Invalid parameter");
        }

        mDirection = direction;
    }

    public int getAcceleration() {
        return mAcceleration;
    }

    public void setAcceleration(int acceleration) {
        mAcceleration = acceleration;
    }

    public boolean getElevatorButton(int floor) throws RemoteException {
        if(floor >= mNumOfFloors) {
            throw new RemoteException("Invalid parameter");
        }

        return mButtonStatus[floor];
    }

    public void setElevatorButton(boolean buttonStatus, int floor) throws RemoteException {
        if(floor >= mNumOfFloors) {
            throw new RemoteException("Invalid parameter");
        }

        mButtonStatus[floor] = buttonStatus;
    }

    public int getElevatorDoorStatus() {
        return mElevatorDoorStatus;
    }

    public void setElevatorDoorStatus(int doorStatus) throws RemoteException {
        if(doorStatus < IElevator.ELEVATOR_DOORS_OPEN || doorStatus > IElevator.ELEVATOR_DOORS_CLOSING) {
            throw new RemoteException("Invalid parameter");
        }

        mElevatorDoorStatus = doorStatus;
    }

    public int getCurrentFloor() {
        return mCurrentFloor;
    }

    public void setCurrentFloor(int currentFloor) throws RemoteException {
        if(currentFloor >= mNumOfFloors)
            throw new RemoteException("Invalid parameter");

        mCurrentFloor = currentFloor;
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
