package at.fhhagenberg.sqelevator;

import java.util.Set;
import java.util.HashSet;
import java.rmi.RemoteException;

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

    /**< The set of topics which need to be updated. */
    private final Set<String> mUpdateTopics;

    /**
     * CTor which instantiates all members.
     *
     * @param plc plc The PLC object to communicate with.
     */
    public ElevatorControlSystem(IElevator plc) {
        mPLC = plc;
        mUpdateTopics = new HashSet<String>();
    }

    /**
     * Initializes the elevators and floors via the PLC. Only called once at startup.
     */
    public void initializeElevatorsViaPLC() {
        try {
            // Fetch data from PLC
            int numOfElevators = mPLC.getElevatorNum();
            int numOfFloors = mPLC.getFloorNum();

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
     * Returns the set of topics which need to be updated.
     * @return The set of topics.
     */
    public Set<String> getUpdateTopics() {
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
                mUpdateTopics.add(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.DIRECTION_SUBTOPIC));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.DIRECTION_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setAcceleration(mPLC.getElevatorAccel(elevatorNumber))) {
                mUpdateTopics.add(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.ACCELERATION_SUBTOPIC));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.ACCELERATION_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setSpeed(mPLC.getElevatorSpeed(elevatorNumber))) {
                mUpdateTopics.add(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.SPEED_SUBTOPIC));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.SPEED_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setElevatorDoorStatus(mPLC.getElevatorDoorStatus(elevatorNumber))) {
                mUpdateTopics.add(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.DOOR_STATUS_SUBTOPIC));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.DOOR_STATUS_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setCurrentFloor(mPLC.getElevatorFloor(elevatorNumber))) {
                mUpdateTopics.add(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.CURRENT_FLOOR_SUBTOPIC));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.CURRENT_FLOOR_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setTargetFloor(mPLC.getTarget(elevatorNumber))) {
                mUpdateTopics.add(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.TARGET_FLOOR_SUBTOPIC));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.TARGET_FLOOR_SUBTOPIC));
            }

            if (mElevators[elevatorNumber].setWeight(mPLC.getElevatorWeight(elevatorNumber))) {
                mUpdateTopics.add(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.WEIGHT_SUBTOPIC));
            }
            else {
                mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.WEIGHT_SUBTOPIC));
            }

            for (int i = 0; i < mFloors.length; ++i) {
                if (mElevators[elevatorNumber].setElevatorButton(mPLC.getElevatorButton(elevatorNumber, i), i)) {
                    mUpdateTopics.add(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.FLOOR_REQUESTED_SUBTOPIC, i));
                }
                else {
                    mUpdateTopics.remove(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.FLOOR_REQUESTED_SUBTOPIC, i));
                }

                if (mElevators[elevatorNumber].setFloorService(mPLC.getServicesFloors(elevatorNumber, i), i)) {
                    mUpdateTopics.add(formatElevatorUpdateTopic(elevatorNumber, MqttTopics.FLOOR_SERVICED_SUBTOPIC, i));
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
     * Updates the floor data.
     * @param floorNumber The floor number.
     */
    private void updateFloor(int floorNumber) {
        assert (floorNumber < mFloors.length && floorNumber >= 0);
        try {
            if (mFloors[floorNumber].setButtonUpPressed(mPLC.getFloorButtonUp(floorNumber))) {
                mUpdateTopics.add(formatFloorUpdateTopic(floorNumber, MqttTopics.BUTTON_UP_SUBTOPIC));
            }
            else {
                mUpdateTopics.remove(formatFloorUpdateTopic(floorNumber, MqttTopics.BUTTON_UP_SUBTOPIC));
            }

            if (mFloors[floorNumber].setButtonDownPressed(mPLC.getFloorButtonDown(floorNumber))) {
                mUpdateTopics.add(formatFloorUpdateTopic(floorNumber, MqttTopics.BUTTON_DOWN_SUBTOPIC));
            }
            else {
                mUpdateTopics.remove(formatFloorUpdateTopic(floorNumber, MqttTopics.BUTTON_DOWN_SUBTOPIC));
            }
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
        return MqttTopics.ELEVATOR_TOPIC + "/" + elevatorNumber + "/" + subtopic;
    }

    /**
     * Formats the topic for an elevator update.
     * @param elevatorNumber The elevator number.
     * @param subtopic The subtopic.
     * @param floor The floor number.
     * @return The formatted topic.
     */
    private String formatElevatorUpdateTopic(int elevatorNumber, String subtopic, int floor) {
        return MqttTopics.ELEVATOR_TOPIC + "/" + elevatorNumber + "/" + subtopic + "/" + floor;
    }

    /**
     * Formats the topic for a floor update.
     * @param floorNumber The floor number.
     * @param subtopic The subtopic.
     * @return The formatted topic.
     */
    private String formatFloorUpdateTopic(int floorNumber, String subtopic) {
        return MqttTopics.FLOOR_TOPIC + "/" + floorNumber + "/" + subtopic;
    }
}
