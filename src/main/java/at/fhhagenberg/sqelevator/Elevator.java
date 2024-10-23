package at.fhhagenberg.sqelevator;

import java.rmi.RemoteException;

public class Elevator {
    /** State variable for elevator doors open.	 */
    public final static int ELEVATOR_DOORS_OPEN = 1;
    /** State variable for elevator doors closed. */
    public final static int ELEVATOR_DOORS_CLOSED = 2;
    /** State variable for elevator doors opening. */
    public final static int ELEVATOR_DOORS_OPENING = 3;
    /** State variable for elevator doors closing. */
    public final static int ELEVATOR_DOORS_CLOSING = 4;

    /** State variable for elevator status when going up */
    public final static int ELEVATOR_DIRECTION_UP = 0;
    /** State variable for elevator status when going down. */
    public final static int ELEVATOR_DIRECTION_DOWN = 1;
    /** State variables for elevator status stopped and uncommitted. */
    public final static int ELEVATOR_DIRECTION_UNCOMMITTED = 2;

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
        if(direction < ELEVATOR_DIRECTION_UP || direction > ELEVATOR_DIRECTION_UNCOMMITTED) {
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
        if(doorStatus < ELEVATOR_DOORS_OPEN || doorStatus >ELEVATOR_DOORS_CLOSING) {
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
