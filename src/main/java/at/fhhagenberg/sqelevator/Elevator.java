package at.fhhagenberg.sqelevator;

import java.rmi.RemoteException;

public class Elevator {
    public enum ButtonStatus {
        Inactive,
        Down,
        Up
    }

    private final int mCapacity;
    private int mSpeed;
    private int mAcceleration;
    private final int mNumOfFloors;
    private int mDirection;
    private int mElevatorDoorStatus;
    private int mCurrentFloor;

    private ButtonStatus[] mButtonStatus;

    public Elevator(int numOfFloors, int capacity) {
        mCapacity = capacity;
        mNumOfFloors = numOfFloors;
        mButtonStatus = new ButtonStatus[numOfFloors];
        for(ButtonStatus buttonStatus : mButtonStatus) {
            buttonStatus = ButtonStatus.Inactive;
        }
    }

    public int getDirection() {
        return mDirection;
    }

    public void setDirection(int direction) {
        mDirection = direction;
    }

    public int getAcceleration() {
        return mAcceleration;
    }

    public void setAcceleration(int acceleration) {
        mAcceleration = acceleration;
    }

    public ButtonStatus getElevatorButton(int floor) throws RemoteException {
        if(floor >= mNumOfFloors) {
            throw new RemoteException("Invalid parameter");
        }

        return mButtonStatus[floor];
    }

    public void setElevatorButton(ButtonStatus buttonStatus, int floor) throws RemoteException {
        if(floor >= mNumOfFloors) {
            throw new RemoteException("Invalid parameter");
        }

        mButtonStatus[floor] = buttonStatus;
    }

    public int getElevatorDoorStatus() {
        return mElevatorDoorStatus;
    }

    public void setElevatorDoorStatus(int doorStatus) {
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
