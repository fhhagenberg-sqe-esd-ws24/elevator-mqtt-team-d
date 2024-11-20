package at.fhhagenberg.sqelevator;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.vavr.control.Either;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ElevatorMqttAdapter {
    private final IElevator mPLC;
    private final ElevatorControlSystem mControlSystem;
    private static Mqtt5AsyncClient mMqttClient;
    private boolean mConnectionStatus = false;

    public ElevatorMqttAdapter(IElevator plc, Mqtt5AsyncClient mqttClient) {
        mPLC = plc;
        mControlSystem = new ElevatorControlSystem(plc);
        mMqttClient = mqttClient;
    }

    public static void main(String[] args){
        try {
            // Read from property file
            Properties properties = new Properties();
            properties.load(ElevatorMqttAdapter.class.getResourceAsStream("/elevator.properties"));

            // Fetch properties
            String plcUrl = properties.getProperty("plc.url");
            String mqttUrl = properties.getProperty("mqtt.url");
            int mqttPort = Integer.parseInt(properties.getProperty("mqtt.port"));
            int interval = Integer.parseInt(properties.getProperty("interval"));

            // Set up RMI and MQTT client
            IElevator plc = (IElevator) Naming.lookup(plcUrl);
            Mqtt5AsyncClient mqttClient = Mqtt5Client.builder()
                    .identifier(UUID.randomUUID().toString())
                    .serverHost(mqttUrl)
                    .serverPort(mqttPort)
                    .buildAsync();

            ElevatorMqttAdapter client = new ElevatorMqttAdapter(plc, mqttClient);

            client.run(interval);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void run(int interval) throws InterruptedException, RemoteException {
        // Initialize elevators and floors
        mControlSystem.initializeElevatorsViaPLC();

        // check broker connection
        while(connectToBroker() == false) {
            System.out.println("Failed to connect to broker. Retrying in 5 seconds...");
            Thread.sleep(5000);
        }

        // retained messages
        publishRetainedMessages();

        // subscribe to topics
        subscribeToTopics();

        while(!mConnectionStatus) {}

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            boolean initial = true;
            @Override
            public void run() {
                pollPLC(initial);
                initial = false;
            }
        }, 0, interval);
    }

    private void pollPLC(boolean initial) {
        if(initial){
            mControlSystem.initialUpdateDataViaPLC();
        }
        else {
            mControlSystem.updateDataViaPLC();
        }

        var topicsToPublish = mControlSystem.getUpdateTopics();

        // Publish to MQTT
        for (var entry : topicsToPublish.entrySet()) {
            String topic = entry.getKey();
            Either<Integer, Boolean> value = entry.getValue();

            if (value.isLeft()) {
                int intValue = value.getLeft();
                mMqttClient.publishWith().topic(topic).payload(String.valueOf(intValue).getBytes()).send();
            } else {
                boolean boolValue = value.get();
                mMqttClient.publishWith().topic(topic).payload(String.valueOf(boolValue).getBytes()).send();
            }
        }
    }

    private boolean connectToBroker() {
        CompletableFuture<Mqtt5ConnAck> connAckFuture = mMqttClient.connect();

        try {
            connAckFuture.get(10, TimeUnit.SECONDS);
            if (mMqttClient.getState().isConnected()){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void publishRetainedMessages() throws RemoteException {
        mMqttClient.publishWith()
                .topic(MqttTopics.INFO_TOPIC + MqttTopics.NUM_OF_ELEVATORS_SUBTOPIC).retain(true)
                .payload(String.valueOf(mControlSystem.getElevators().length).getBytes()).send();
        mMqttClient.publishWith()
                .topic(MqttTopics.INFO_TOPIC + MqttTopics.NUM_OF_FLOORS_SUBTOPIC).retain(true)
                .payload(String.valueOf(mControlSystem.getFloors().length).getBytes()).send();
        mMqttClient.publishWith()
                .topic(MqttTopics.INFO_TOPIC + MqttTopics.FLOOR_HEIGHT_SUBTOPIC).retain(true)
                .payload(String.valueOf(mControlSystem.getFloorHeight()).getBytes()).send();

        for (int i = 0; i < mControlSystem.getElevators().length; i++) {
            mMqttClient.publishWith()
                    .topic(MqttTopics.ELEVATOR_TOPIC + i + MqttTopics.CAPACITY_SUBTOPIC).retain(true)
                    .payload(String.valueOf(mControlSystem.getElevators()[i].getCapacity()).getBytes()).send();
        }
    }

    private void subscribeToTopics() {
        mMqttClient.subscribeWith()
                .topicFilter(MqttTopics.ELEVATOR_CONTROL_TOPIC + "/#")
                .callback(publish -> mqttCallback(publish))
                .send();
    }

    private void mqttCallback(Mqtt5Publish publish) {
        String topic = publish.getTopic().toString();
        String[] parts = topic.split("/");

        if (parts.length == 2) {
            if(("/" + parts[1]) == MqttTopics.CONNECTION_STATUS_SUBTOPIC) {
                mConnectionStatus = (Boolean.parseBoolean(new String(publish.getPayloadAsBytes())) ? true : false);
            }
            else {
                System.out.println("Unknown subtopic in subscribeToTopics: " + topic);
            }

            return;
        }

        if(parts.length != 3) {
            System.out.println("Invalid topic: " + topic);
            return;
        }

        int elevatorNumber = Integer.parseInt(parts[1]);
        String subtopic = "/" + parts[2];

        try {
            switch (subtopic) {
                case MqttTopics.TARGET_FLOOR_SUBTOPIC:
                    mPLC.setTarget(elevatorNumber, Integer.parseInt(new String(publish.getPayloadAsBytes())));
                    break;
                case MqttTopics.DIRECTION_SUBTOPIC:
                    mPLC.setCommittedDirection(elevatorNumber, Integer.parseInt(new String(publish.getPayloadAsBytes())));
                    break;
                default:
                    System.out.println("Unknown subtopic in subscribeToTopics: " + topic);
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
