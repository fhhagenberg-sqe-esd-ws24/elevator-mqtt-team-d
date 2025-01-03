package at.fhhagenberg.sqelevator.algorithm;

import at.fhhagenberg.sqelevator.Elevator;
import at.fhhagenberg.sqelevator.Floor;
import sqelevator.IElevator;
import at.fhhagenberg.sqelevator.MqttTopics;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ElevatorAlgorithm {
    private static Mqtt5AsyncClient mMqttClient;

    private int mNrOfElevators = 0;
    private int mNrOfFloors = 0;
    private int mFloorHeight = 0;
    private final Map<Integer, Integer> mMaxPassengers = new HashMap<Integer, Integer>();

    private ElevatorState mElevatorState;
    private final TreeSet<Integer> mFloorRequestsToBeServiced = new TreeSet<>();

    public ElevatorAlgorithm(Mqtt5AsyncClient mqttClient) {
        mMqttClient = mqttClient;
    }

    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.load(ElevatorAlgorithm.class.getResourceAsStream("/elevator.properties"));

            // Fetch properties
            String mqttUrl = properties.getProperty("mqtt.url");
            int mqttPort = Integer.parseInt(properties.getProperty("mqtt.port"));

            Mqtt5AsyncClient mqttClient = Mqtt5Client.builder()
                    .identifier(UUID.randomUUID().toString())
                    .serverHost(mqttUrl)
                    .serverPort(mqttPort)
                    .buildAsync();

            ElevatorAlgorithm algorithm = new ElevatorAlgorithm(mqttClient);
            algorithm.run();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws Exception {
        // check broker connection
        while(!connectToBroker()) {
            System.out.println("Failed to connect to broker. Retrying in 5 seconds...");
            Thread.sleep(5000);
        }

        // subscribe to retained topics
        subscribeToRetainedTopics();

        // wait until static information has been received
        while(mNrOfElevators == 0 || mNrOfFloors == 0 || mFloorHeight == 0 || mMaxPassengers.size() != mNrOfElevators) {
            Thread.sleep(500);
        }

        // initialize elevator state
        mElevatorState = new ElevatorState(mNrOfElevators, mNrOfFloors, mFloorHeight,
                mMaxPassengers.values().stream().mapToInt(Integer::intValue).toArray());

        // subscribe to topics
        subscribeToTopics();

        // set connection status to true to signal availability
        publishConnectionStatus();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                publishConnectionStatus();
                resolveElevatorRequests();
            }
        }, 0, 100);
    }

    private boolean connectToBroker() {
        CompletableFuture<Mqtt5ConnAck> connAckFuture = mMqttClient.connect();

        try {
            connAckFuture.get(10, TimeUnit.SECONDS);
            if (mMqttClient.getState().isConnected()){
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false;
        }


        return false;
    }

    private void subscribeToRetainedTopics() {
        // Subscribe to info topic
        mMqttClient.subscribeWith()
                .addSubscription()
                .topicFilter(MqttTopics.INFO_TOPIC + "/#")
                .applySubscription()
                .addSubscription()
                .topicFilter(MqttTopics.ELEVATOR_TOPIC + "/+" + MqttTopics.CAPACITY_SUBTOPIC)
                .applySubscription()
                .callback(this::retainedMessagesMqttCallback)
                .send();
    }

    private void subscribeToTopics() {
        // Subscribe to elevator and floor topics
        mMqttClient.subscribeWith()
                .addSubscription()
                    .topicFilter(MqttTopics.ELEVATOR_TOPIC + "/#")
                    .applySubscription()
                .addSubscription()
                    .topicFilter(MqttTopics.FLOOR_TOPIC + "/#")
                    .applySubscription()
                .callback(this::mqttCallback)
                .send();
    }

    private void retainedMessagesMqttCallback(Mqtt5Publish publish) {
        String topic = publish.getTopic().toString();
        String[] parts = topic.split("/");

        if (parts.length == 2) {
            switch (("/" + parts[1])) {
                case MqttTopics.NUM_OF_ELEVATORS_SUBTOPIC ->
                        mNrOfElevators = Integer.parseInt(new String(publish.getPayloadAsBytes()));
                case MqttTopics.NUM_OF_FLOORS_SUBTOPIC ->
                        mNrOfFloors = Integer.parseInt(new String(publish.getPayloadAsBytes()));
                case MqttTopics.FLOOR_HEIGHT_SUBTOPIC ->
                        mFloorHeight = Integer.parseInt(new String(publish.getPayloadAsBytes()));
                default -> System.out.println("Unknown subtopic in subscribeToTopics: " + topic);
            }
        }
        else if (parts.length == 3) {
            if ((("/" + parts[2]).equals(MqttTopics.CAPACITY_SUBTOPIC))) {
                mMaxPassengers.put(Integer.parseInt(parts[1]), Integer.parseInt(new String(publish.getPayloadAsBytes())));
            }
            else {
                System.out.println("Unknown subtopic in subscribeToTopics: " + topic);
            }
        }
    }

    private void mqttCallback(Mqtt5Publish publish) {
        String topic = publish.getTopic().toString();
        String[] parts = topic.split("/");

        switch (parts[0]) {
            case MqttTopics.ELEVATOR_TOPIC -> {
                int elevatorNumber = Integer.parseInt(parts[1]);
                switch ("/" + parts[2]) {
                    case MqttTopics.SPEED_SUBTOPIC ->
                        mElevatorState.getElevators()[elevatorNumber].setSpeed(Integer.parseInt(new String(publish.getPayloadAsBytes())));

                    case MqttTopics.ACCELERATION_SUBTOPIC ->
                        mElevatorState.getElevators()[elevatorNumber].setAcceleration(Integer.parseInt(new String(publish.getPayloadAsBytes())));

                    case MqttTopics.DIRECTION_SUBTOPIC ->
                        mElevatorState.getElevators()[elevatorNumber].setDirection(Integer.parseInt(new String(publish.getPayloadAsBytes())));

                    case MqttTopics.DOOR_STATUS_SUBTOPIC ->
                        mElevatorState.getElevators()[elevatorNumber].setElevatorDoorStatus(Integer.parseInt(new String(publish.getPayloadAsBytes())));

                    case MqttTopics.CURRENT_FLOOR_SUBTOPIC ->
                        mElevatorState.getElevators()[elevatorNumber].setCurrentFloor(Integer.parseInt(new String(publish.getPayloadAsBytes())));

                    case MqttTopics.TARGET_FLOOR_SUBTOPIC ->
                        mElevatorState.getElevators()[elevatorNumber].setTargetFloor(Integer.parseInt(new String(publish.getPayloadAsBytes())));

                    case MqttTopics.WEIGHT_SUBTOPIC ->
                        mElevatorState.getElevators()[elevatorNumber].setWeight(Integer.parseInt(new String(publish.getPayloadAsBytes())));

                    case MqttTopics.FLOOR_REQUESTED_SUBTOPIC -> {
                        int floorNumber = Integer.parseInt(parts[3]);
                        mElevatorState.getElevators()[elevatorNumber].setElevatorButton(Boolean.parseBoolean(new String(publish.getPayloadAsBytes())), floorNumber);
                    }

                    case MqttTopics.FLOOR_SERVICED_SUBTOPIC -> {
                        int floorNumber = Integer.parseInt(parts[3]);
                        if (floorNumber != 0) {
                            mElevatorState.getElevators()[elevatorNumber].setFloorService(Boolean.parseBoolean(new String(publish.getPayloadAsBytes())), floorNumber);
                        }
                    }

                    case MqttTopics.CAPACITY_SUBTOPIC -> {}

                    default -> System.out.println("Unknown elevator subtopic: " + parts[2]);
                }
            }

            case MqttTopics.FLOOR_TOPIC -> {
                int floorNumber = Integer.parseInt(parts[1]);
                switch ("/" + parts[2]) {
                    case MqttTopics.BUTTON_UP_SUBTOPIC ->
                        mElevatorState.getFloors()[floorNumber].setButtonUpPressed(Boolean.parseBoolean(new String(publish.getPayloadAsBytes())));

                    case MqttTopics.BUTTON_DOWN_SUBTOPIC ->
                        mElevatorState.getFloors()[floorNumber].setButtonDownPressed(Boolean.parseBoolean(new String(publish.getPayloadAsBytes())));

                    default -> System.out.println("Unknown floor subtopic: " + parts[2]);
                }
            }

            default -> System.out.println("Unknown topic in mqttCallback: " + topic);
        }
    }

    private void publishConnectionStatus() {
        mMqttClient.publishWith()
                .topic(MqttTopics.ELEVATOR_CONTROL_TOPIC + MqttTopics.CONNECTION_STATUS_SUBTOPIC)
                .payload(String.valueOf(true).getBytes())
                .send();
    }

    private void resolveElevatorRequests() {
        // Iterate through elevators and check if control command needs to be sent
        var elevators = mElevatorState.getElevators();
        var floors = mElevatorState.getFloors();

        for (int i = 0; i < elevators.length; i++) {
            // Check if target location reached
            if (elevators[i].getCurrentFloor() == elevators[i].getTargetFloor()) {
                if (elevators[i].getElevatorDoorStatus() != IElevator.ELEVATOR_DOORS_OPEN) {
                    continue;
                }

                checkElevatorRequests(elevators[i], i, floors);
            }
            // otherwise -> check if request can be handled
            else {
                if(elevators[i].getElevatorDoorStatus() == IElevator.ELEVATOR_DOORS_CLOSED) {
                    checkElevatorRequests(elevators[i], i, floors);
                }
            }
        }
        mFloorRequestsToBeServiced.clear();
    }

    private void checkElevatorRequests(Elevator elevator, int elevatorNum, Floor[] floors) {
        // Check if there is another request in current direction
        switch (elevator.getDirection()) {
            case IElevator.ELEVATOR_DIRECTION_UP:
                handleUpwardRequest(elevator, elevatorNum, floors);
                break;

            case IElevator.ELEVATOR_DIRECTION_DOWN:
                handleDownwardRequest(elevator, elevatorNum, floors);
                break;

            case IElevator.ELEVATOR_DIRECTION_UNCOMMITTED:
                handleUncommittedRequest(elevator, elevatorNum, floors);
                break;
        }
    }

    private void handleUpwardRequest(Elevator elevator, int elevatorNum, Floor[] floors) {
        int requestedFloor = findNextRequestedFloor(elevator, floors, true);

        if (requestedFloor > elevator.getCurrentFloor()) {
            sendElevatorTargetFloor(elevator, elevatorNum, requestedFloor);
            sendElevatorDirection(elevator, elevatorNum, IElevator.ELEVATOR_DIRECTION_UP);
        } else {
            requestedFloor = findNextRequestedFloor(elevator, floors, false);

            if(requestedFloor < elevator.getCurrentFloor()) {
                sendElevatorTargetFloor(elevator, elevatorNum, requestedFloor);
                sendElevatorDirection(elevator, elevatorNum, IElevator.ELEVATOR_DIRECTION_DOWN);
            } else {
                sendElevatorDirection(elevator, elevatorNum, IElevator.ELEVATOR_DIRECTION_UNCOMMITTED);
            }
        }
    }

    private void handleDownwardRequest(Elevator elevator, int elevatorNum, Floor[] floors) {
        int requestedFloor = findNextRequestedFloor(elevator, floors, false);

        if (requestedFloor < elevator.getCurrentFloor()) {
            sendElevatorTargetFloor(elevator, elevatorNum, requestedFloor);
            sendElevatorDirection(elevator, elevatorNum, IElevator.ELEVATOR_DIRECTION_DOWN);
        } else {
            requestedFloor = findNextRequestedFloor(elevator, floors, true);

            if(requestedFloor > elevator.getCurrentFloor()) {
                sendElevatorTargetFloor(elevator, elevatorNum, requestedFloor);
                sendElevatorDirection(elevator, elevatorNum, IElevator.ELEVATOR_DIRECTION_UP);
            } else {
                sendElevatorDirection(elevator, elevatorNum, IElevator.ELEVATOR_DIRECTION_UNCOMMITTED);
            }
        }
    }

    private void handleUncommittedRequest(Elevator elevator, int elevatorNum, Floor[] floors) {
        int requestedFloorUp = findNextRequestedFloor(elevator, floors, true);
        int requestedFloorDown = findNextRequestedFloor(elevator, floors, false);

        if (requestedFloorUp == elevator.getCurrentFloor() && requestedFloorDown == elevator.getCurrentFloor()) {
            sendElevatorDirection(elevator, elevatorNum, IElevator.ELEVATOR_DIRECTION_UNCOMMITTED);
            return;
        }

        int distanceToUp = Math.abs(elevator.getCurrentFloor() - requestedFloorUp);
        int distanceToDown = Math.abs(elevator.getCurrentFloor() - requestedFloorDown);

        if (distanceToUp == 0) {
            sendElevatorTargetFloor(elevator, elevatorNum, requestedFloorDown);
            sendElevatorDirection(elevator, elevatorNum, IElevator.ELEVATOR_DIRECTION_DOWN);
            return;
        }
        if (distanceToDown == 0) {
            sendElevatorTargetFloor(elevator, elevatorNum, requestedFloorUp);
            sendElevatorDirection(elevator, elevatorNum, IElevator.ELEVATOR_DIRECTION_UP);
            return;
        }

        // Send command to nearest target
        if (distanceToUp <= distanceToDown) {
            sendElevatorTargetFloor(elevator, elevatorNum, requestedFloorUp);
            sendElevatorDirection(elevator, elevatorNum, IElevator.ELEVATOR_DIRECTION_UP);
        } else {
            sendElevatorTargetFloor(elevator, elevatorNum, requestedFloorDown);
            sendElevatorDirection(elevator, elevatorNum, IElevator.ELEVATOR_DIRECTION_DOWN);
        }
    }

    private int findNextRequestedFloor(Elevator elevator, Floor[] floors, boolean movingUp) {
        int requestedFloor = elevator.getCurrentFloor();
        for (int i = (movingUp ? requestedFloor + 1 : requestedFloor - 1);
             movingUp ? i < floors.length : i >= 0;
             i = (movingUp ? i + 1 : i - 1)) {

            // Check requested floors from within elevator
            if (elevator.getElevatorButton(i) && elevator.getFloorService(i)) {
                requestedFloor = i;
                break;
            }

            // Check elevator requests from floor
            if ((movingUp ? floors[i].getButtonUpPressed() : floors[i].getButtonDownPressed()) &&
                    elevator.getFloorService(i) &&
                    !mFloorRequestsToBeServiced.contains(i)) {
                requestedFloor = i;
                mFloorRequestsToBeServiced.add(i);
                break;
            }
        }

        // if no new requested floor found, also check direction switch
        if (requestedFloor == elevator.getCurrentFloor()) {
            for (int i = (movingUp ? floors.length - 1 : 0);
                 movingUp ? i > elevator.getCurrentFloor() : i < elevator.getCurrentFloor();
                 i = (movingUp ? i - 1 : i + 1)) {

                // check elevator requests from floor
                if ((movingUp ? floors[i].getButtonDownPressed() : floors[i].getButtonUpPressed()) &&
                elevator.getFloorService(i) &&
                        !mFloorRequestsToBeServiced.contains(i)) {
                    requestedFloor = i;
                    mFloorRequestsToBeServiced.add(i);
                    break;
                }
            }
        }

        return requestedFloor;
    }

    private void sendElevatorTargetFloor(Elevator elevator, int elevatorNumber, int targetFloor) {
        mMqttClient.publishWith()
                .topic(MqttTopics.ELEVATOR_CONTROL_TOPIC + "/" + elevatorNumber + MqttTopics.TARGET_FLOOR_SUBTOPIC)
                .payload(String.valueOf(targetFloor).getBytes()).send();
    }

    private void sendElevatorDirection(Elevator elevator, int elevatorNumber, int direction) {
        mMqttClient.publishWith()
                .topic(MqttTopics.ELEVATOR_CONTROL_TOPIC + "/" + elevatorNumber + MqttTopics.DIRECTION_SUBTOPIC)
                .payload(String.valueOf(direction).getBytes()).send();
    }
}
