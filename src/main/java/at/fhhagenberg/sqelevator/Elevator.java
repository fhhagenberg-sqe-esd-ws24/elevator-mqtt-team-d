package at.fhhagenberg.sqelevator;

import java.rmi.RemoteException;

public class Elevator {
    private final int mCapacity;
    private int mSpeed = 0;
    private int mAcceleration = 0;
    private final int mNumOfFloors;
    private int mDirection = IElevator.ELEVATOR_DIRECTION_UNCOMMITTED;
    private int mElevatorDoorStatus = IElevator.ELEVATOR_DOORS_CLOSED;
    private int mCurrentFloor = 0;
    private int mTargetFloor = 0;
    private final int mInitWeight;
    private int mCurrentWeight;
    private boolean[] mButtonStatus;
    private boolean[] mFloorService;

    public Elevator(int numOfFloors, int capacity, int initWeight) {
        mCapacity = capacity;
        mNumOfFloors = numOfFloors;
        mInitWeight = initWeight;
        mCurrentWeight = initWeight;
        mButtonStatus = new boolean[numOfFloors];
        mFloorService = new boolean[numOfFloors];
        for(boolean floorService : mFloorService) {
            floorService = true;
        }
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
        if(floor < 0 || floor >= mNumOfFloors) {
            throw new RemoteException("Invalid parameter");
        }

        return mButtonStatus[floor];
    }

    public void setElevatorButton(boolean buttonStatus, int floor) throws RemoteException {
        if(floor < 0 || floor >= mNumOfFloors) {
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
        if(currentFloor < 0 || currentFloor >= mNumOfFloors) {
            throw new RemoteException("Invalid parameter");
        }

        mCurrentFloor = currentFloor;
    }

    public int getTargetFloor() {
        return mTargetFloor;
    }

    public void setTargetFloor(int targetFloor) throws RemoteException {
        if(targetFloor < 0 || targetFloor >= mNumOfFloors){
            throw new RemoteException("Invalid parameter");
        }

        mTargetFloor = targetFloor;
    }

    public boolean getFloorService(int floor) throws RemoteException{
        if(floor < 0 || floor >= mNumOfFloors) {
            throw new RemoteException("Invalid parameter");
        }

        return mFloorService[floor];
    }

    public void setFloorService(boolean service, int floor) throws  RemoteException {
        if(floor < 1 || floor >= mNumOfFloors) {
            throw new RemoteException("Invalid parameter");
        }

        mFloorService[floor] = service;
    }

    public int getWeight() {
        return mCurrentWeight - mInitWeight;
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
