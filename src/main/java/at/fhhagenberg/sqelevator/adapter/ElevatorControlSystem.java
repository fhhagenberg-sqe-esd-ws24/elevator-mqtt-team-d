package at.fhhagenberg.sqelevator.adapter;

import java.util.HashMap;
import java.rmi.RemoteException;
import java.util.Map;

import at.fhhagenberg.sqelevator.Elevator;
import at.fhhagenberg.sqelevator.Floor;
import sqelevator.IElevator;
import at.fhhagenberg.sqelevator.MqttTopics;
import io.vavr.control.Either;

/**
 * Class which represents the elevator control system.
 */
public class ElevatorControlSystem {
    /**< The PLC object to communicate with. */
    private final IElevator mPLC;

    /**< The elevators. */
    private Elevator[] mElevators = null;
    /**< The floors. */
    private Floor[] mFloors = null;

    private int mFloorHeight;

    /**< The set of topics which need to be updated. */
    private final HashMap<String, Either<Integer, Boolean>> mUpdateTopics;

    /**
     * CTor which instantiates all members.
     *
     * @param plc plc The PLC object to communicate with.
     */
    public ElevatorControlSystem(IElevator plc) {
        mPLC = plc;
        mUpdateTopics = new HashMap<>();
    }

    /**
     * Initializes the elevators and floors via the PLC. Only called once at startup.
     */
    public void initializeElevatorsViaPLC() {
        try {
            // Fetch data from PLC
            int numOfElevators = mPLC.getElevatorNum();
            int numOfFloors = mPLC.getFloorNum();
            mFloorHeight = mPLC.getFloorHeight();

            // Set up elevators
            mElevators = new Elevator[numOfElevators];
            for (int i = 0; i < numOfElevators; ++i) {
                mElevators[i] = new Elevator(numOfFloors, mPLC.getElevatorCapacity(i));
            }

            // Set up floors
            mFloors = new Floor[numOfFloors];
            for (int i = 0; i < numOfFloors; ++i) {
                mFloors[i] = new Floor();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the number of floors.
     * @return The number of floors.
     */
    public int getFloorHeight() {
        return mFloorHeight;
    }

    /**
     * Updates the data via the PLC. Gets called periodically.
     */
    public void updateDataViaPLC() {
        if (mElevators == null) {
            return;
        }

        for (int i = 0; i < mElevators.length; ++i) {
            updateElevator(i);
        }

        for (int i = 0; i < mFloors.length; ++i) {
            updateFloor(i);
        }
    }

    /**
     *
     */
    public void initialUpdateDataViaPLC() {
        if (mElevators == null) {
            return;
        }

        for (int i = 0; i < mElevators.length; ++i) {
            initialUpdateElevator(i);
        }

        for (int i = 0; i < mFloors.length; ++i) {
            initialUpdateFloor(i);
        }
    }

    /**
     * Returns the set of topics which need to be updated.
     * @return The set of topics.
     */
    public Map<String, Either<Integer, Boolean>> getUpdateTopics() {
        return mUpdateTopics;
    }

    /**
     * Updates the elevator data.
     * @param elevatorNumber The elevator number.
     */
    private void updateElevator(int elevatorNumber) {
        assert (elevatorNumber < mElevators.length && elevatorNumber >= 0);
        try {
            if (mElevators[elevatorNumber].setDirection(mPLC.getCommittedDirection(elevatorNumber))) {
                mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.DIRECTION_SUBTOPIC), Either.left(mPLC.getCommittedDirection(elevatorNumber)));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.DIRECTION_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setAcceleration(mPLC.getElevatorAccel(elevatorNumber))) {
                mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.ACCELERATION_SUBTOPIC), Either.left(mPLC.getElevatorAccel(elevatorNumber)));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.ACCELERATION_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setSpeed(mPLC.getElevatorSpeed(elevatorNumber))) {
                mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.SPEED_SUBTOPIC), Either.left(mPLC.getElevatorSpeed(elevatorNumber)));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.SPEED_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setElevatorDoorStatus(mPLC.getElevatorDoorStatus(elevatorNumber))) {
                mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.DOOR_STATUS_SUBTOPIC), Either.left(mPLC.getElevatorDoorStatus(elevatorNumber)));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.DOOR_STATUS_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setCurrentFloor(mPLC.getElevatorFloor(elevatorNumber))) {
                mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.CURRENT_FLOOR_SUBTOPIC), Either.left(mPLC.getElevatorFloor(elevatorNumber)));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.CURRENT_FLOOR_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setTargetFloor(mPLC.getTarget(elevatorNumber))) {
                mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.TARGET_FLOOR_SUBTOPIC), Either.left(mPLC.getTarget(elevatorNumber)));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.TARGET_FLOOR_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setWeight(mPLC.getElevatorWeight(elevatorNumber))) {
                mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.WEIGHT_SUBTOPIC), Either.left(mPLC.getElevatorWeight(elevatorNumber)));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.WEIGHT_SUBTOPIC));
            }

            for (int i = 0; i < mFloors.length; ++i) {
                if (mElevators[elevatorNumber].setElevatorButton(mPLC.getElevatorButton(elevatorNumber, i), i)) {
                    mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.FLOOR_REQUESTED_SUBTOPIC, i), Either.right(mPLC.getElevatorButton(elevatorNumber, i)));
                }
                else {
                    mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.FLOOR_REQUESTED_SUBTOPIC, i));
                }

                if(i > 0) {
                    if (mElevators[elevatorNumber].setFloorService(mPLC.getServicesFloors(elevatorNumber, i), i)) {
                        mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.FLOOR_SERVICED_SUBTOPIC, i), Either.right(mPLC.getServicesFloors(elevatorNumber, i)));
                    } else {
                        mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.FLOOR_SERVICED_SUBTOPIC, i));
                    }
                }
                else {
                    mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.FLOOR_SERVICED_SUBTOPIC, i));
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initial pdates the elevator data.
     * @param elevatorNumber The elevator number.
     */
    private void initialUpdateElevator(int elevatorNumber) {
        assert (elevatorNumber < mElevators.length && elevatorNumber >= 0);
        try {
            mElevators[elevatorNumber].setDirection(mPLC.getCommittedDirection(elevatorNumber));
            mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.DIRECTION_SUBTOPIC), Either.left(mPLC.getCommittedDirection(elevatorNumber)));

            mElevators[elevatorNumber].setAcceleration(mPLC.getElevatorAccel(elevatorNumber));
            mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.ACCELERATION_SUBTOPIC), Either.left(mPLC.getElevatorAccel(elevatorNumber)));

            mElevators[elevatorNumber].setSpeed(mPLC.getElevatorSpeed(elevatorNumber));
            mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.SPEED_SUBTOPIC), Either.left(mPLC.getElevatorSpeed(elevatorNumber)));

            mElevators[elevatorNumber].setElevatorDoorStatus(mPLC.getElevatorDoorStatus(elevatorNumber));
            mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.DOOR_STATUS_SUBTOPIC), Either.left(mPLC.getElevatorDoorStatus(elevatorNumber)));


            mElevators[elevatorNumber].setCurrentFloor(mPLC.getElevatorFloor(elevatorNumber));
            mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.CURRENT_FLOOR_SUBTOPIC), Either.left(mPLC.getElevatorFloor(elevatorNumber)));

            mElevators[elevatorNumber].setTargetFloor(mPLC.getTarget(elevatorNumber));
            mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.TARGET_FLOOR_SUBTOPIC), Either.left(mPLC.getTarget(elevatorNumber)));


            mElevators[elevatorNumber].setWeight(mPLC.getElevatorWeight(elevatorNumber));
            mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.WEIGHT_SUBTOPIC), Either.left(mPLC.getElevatorWeight(elevatorNumber)));


            for (int i = 0; i < mFloors.length; ++i) {
                mElevators[elevatorNumber].setElevatorButton(mPLC.getElevatorButton(elevatorNumber, i), i);
                mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.FLOOR_REQUESTED_SUBTOPIC, i), Either.right(mPLC.getElevatorButton(elevatorNumber, i)));

                if(i > 0) {
                    mElevators[elevatorNumber].setFloorService(mPLC.getServicesFloors(elevatorNumber, i), i);
                }
                mUpdateTopics.put(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.FLOOR_SERVICED_SUBTOPIC, i), Either.right(mPLC.getServicesFloors(elevatorNumber, i)));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the floor data.
     * @param floorNumber The floor number.
     */
    private void updateFloor(int floorNumber) {
        assert (floorNumber < mFloors.length && floorNumber >= 0);
        try {
            if (mFloors[floorNumber].setButtonUpPressed(mPLC.getFloorButtonUp(floorNumber))) {
                mUpdateTopics.put(formatFloorUpdateTopic(floorNumber, MqttTopics.BUTTON_UP_SUBTOPIC), Either.right(mPLC.getFloorButtonUp(floorNumber)));
            }
            else {
                mUpdateTopics.remove(formatFloorUpdateTopic(floorNumber, MqttTopics.BUTTON_UP_SUBTOPIC));
            }

            if (mFloors[floorNumber].setButtonDownPressed(mPLC.getFloorButtonDown(floorNumber))) {
                mUpdateTopics.put(formatFloorUpdateTopic(floorNumber, MqttTopics.BUTTON_DOWN_SUBTOPIC), Either.right(mPLC.getFloorButtonDown(floorNumber)));
            }
            else {
                mUpdateTopics.remove(formatFloorUpdateTopic(floorNumber, MqttTopics.BUTTON_DOWN_SUBTOPIC));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initial updates the floor data.
     * @param floorNumber The floor number.
     */
    private void initialUpdateFloor(int floorNumber) {
        assert (floorNumber < mFloors.length && floorNumber >= 0);
        try {
            mFloors[floorNumber].setButtonUpPressed(mPLC.getFloorButtonUp(floorNumber));
            mUpdateTopics.put(formatFloorUpdateTopic(floorNumber, MqttTopics.BUTTON_UP_SUBTOPIC), Either.right(mPLC.getFloorButtonUp(floorNumber)));

            mFloors[floorNumber].setButtonDownPressed(mPLC.getFloorButtonDown(floorNumber));
            mUpdateTopics.put(formatFloorUpdateTopic(floorNumber, MqttTopics.BUTTON_DOWN_SUBTOPIC), Either.right(mPLC.getFloorButtonDown(floorNumber)));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Formats the topic for an elevator update.
     * @param elevatorNumber The elevator number.
     * @param subtopic The subtopic.
     * @return The formatted mqtt topic.
     */
    private String formatElevatorUpdateTopic(int elevatorNumber, String subtopic) {
        return MqttTopics.ELEVATOR_TOPIC + "/" + elevatorNumber + subtopic;
    }

    /**
     * Formats the topic for an elevator update.
     * @param elevatorNumber The elevator number.
     * @param subtopic The subtopic.
     * @param floor The floor number.
     * @return The formatted topic.
     */
    private String formatElevatorUpdateTopic(int elevatorNumber, String subtopic, int floor) {
        return MqttTopics.ELEVATOR_TOPIC + "/" + elevatorNumber + subtopic + "/" + floor;
    }

    /**
     * Formats the topic for a floor update.
     * @param floorNumber The floor number.
     * @param subtopic The subtopic.
     * @return The formatted topic.
     */
    private String formatFloorUpdateTopic(int floorNumber, String subtopic) {
        return MqttTopics.FLOOR_TOPIC + "/" + floorNumber + subtopic;
    }

    /**
     * Returns the elevators. For testing purposes only.
     * @return The elevators.
     */
    public Elevator[] getElevators() {
        return mElevators;
    }

    /**
     * Returns the floors. For testing purposes only.
     * @return The floors.
     */
    public Floor[] getFloors() {
        return mFloors;
    }



}
