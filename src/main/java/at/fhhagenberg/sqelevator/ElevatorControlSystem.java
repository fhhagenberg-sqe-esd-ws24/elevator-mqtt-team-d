package at.fhhagenberg.sqelevator;

import java.rmi.RemoteException;

public class ElevatorControlSystem implements IElevator {
    private final Elevator[] mElevators;
    private final Floor[] mFloors;
    private final int mFloorHeight;

    private final long mStartTimestamp;

    /**
     * CTor which instantiates all members.
     * @param mElevators
     * @param mFloors
     * @param mFloorHeight
     */
    public ElevatorControlSystem(int nrOfElevators, int[] elevatorCapacities, int nrOfFloors, int floorHeight) throws
            RemoteException {
        // Check if number of elevators is equal to elevator capacities length
        if (nrOfElevators != elevatorCapacities.length) {
            throw new RemoteException("Mismatch between number of elevators and their capacities!");
        }

        mElevators = new Elevator[nrOfElevators];
        mFloors = new Floor[nrOfFloors];
        mFloorHeight = floorHeight;

        // Instantiate each elevator
        for (int i = 0; i < mElevators.length; ++i) {
            mElevators[i] = new Elevator(mFloors.length, elevatorCapacities[i]);
        }

        mStartTimestamp = System.currentTimeMillis();
    }

    private boolean checkValidElevatorNumber(int elevatorNumber) {
        // Check valid elevator
        return (elevatorNumber <= 0 || elevatorNumber >= mElevators.length);
    }

    private boolean checkValidFloor(int floor) {
        // Check valid floor
        return (floor < 0 || floor >= mFloors.length);
    }

    @Override
    public int getCommittedDirection(int elevatorNumber) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        return mElevators[elevatorNumber - 1].getDirection();
    }

    @Override
    public int getElevatorAccel(int elevatorNumber) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        return mElevators[elevatorNumber - 1].getAcceleration();
    }

    @Override
    public boolean getElevatorButton(int elevatorNumber, int floor) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        // Check valid floor
        if (!checkValidFloor(floor)) {
            throw new RemoteException("Floor " + floor + " does not exist!");
        }

        return mElevators[elevatorNumber - 1].getElevatorButton(floor);
    }

    @Override
    public int getElevatorDoorStatus(int elevatorNumber) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        return mElevators[elevatorNumber - 1].getElevatorDoorStatus();
    }

    @Override
    public int getElevatorFloor(int elevatorNumber) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        return mElevators[elevatorNumber - 1].getCurrentFloor();
    }

    @Override
    public int getElevatorNum() throws RemoteException {
        return mElevators.length;
    }

    @Override
    public int getElevatorPosition(int elevatorNumber) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        return mElevators[elevatorNumber - 1].getCurrentFloor() * mFloorHeight;
    }

    @Override
    public int getElevatorSpeed(int elevatorNumber) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        return mElevators[elevatorNumber - 1].getSpeed();
    }

    @Override
    public int getElevatorWeight(int elevatorNumber) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        return 0;
    }

    @Override
    public int getElevatorCapacity(int elevatorNumber) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        return mElevators[elevatorNumber - 1].getCapacity();
    }

    @Override
    public boolean getFloorButtonDown(int floor) throws RemoteException {
        // Check valid floor
        if (!checkValidFloor(floor)) {
            throw new RemoteException("Floor " + floor + " does not exist!");
        }

        return mFloors[floor].getButtonDownPressed();
    }

    @Override
    public boolean getFloorButtonUp(int floor) throws RemoteException {
        // Check valid floor
        if (!checkValidFloor(floor)) {
            throw new RemoteException("Floor " + floor + " does not exist!");
        }

        return mFloors[floor].getButtonUpPressed();
    }

    @Override
    public int getFloorHeight() throws RemoteException {
        return mFloorHeight;
    }

    @Override
    public int getFloorNum() throws RemoteException {
        return mFloors.length;
    }

    @Override
    public boolean getServicesFloors(int elevatorNumber, int floor) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        // Check valid floor
        if (!checkValidFloor(floor)) {
            throw new RemoteException("Floor " + floor + " does not exist!");
        }

        return false;
    }

    @Override
    public int getTarget(int elevatorNumber) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        return 0;
    }

    @Override
    public void setCommittedDirection(int elevatorNumber, int direction) throws RemoteException {
        // Check valid elevator
        if (!checkValidElevatorNumber(elevatorNumber)) {
            throw new RemoteException("Elevator " + elevatorNumber + " does not exist!");
        }

        mElevators[elevatorNumber - 1].setDirection(direction);
    }

    @Override
    public void setServicesFloors(int elevatorNumber, int floor, boolean service) throws RemoteException {

    }

    @Override
    public void setTarget(int elevatorNumber, int target) throws RemoteException {

    }

    @Override
    public long getClockTick() throws RemoteException {
        return System.currentTimeMillis() - mStartTimestamp;
    }
}
