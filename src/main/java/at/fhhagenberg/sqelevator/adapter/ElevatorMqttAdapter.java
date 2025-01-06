package at.fhhagenberg.sqelevator.adapter;

import sqelevator.IElevator;
import at.fhhagenberg.sqelevator.MqttTopics;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.vavr.control.Either;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElevatorMqttAdapter {
    private final IElevator mPLC;
    private ElevatorControlSystem mControlSystem;
    private final Mqtt5AsyncClient mMqttClient;
    private boolean mConnectionStatus = false;
    private long mConnectionStatusTimestamp = 0;

    private static final Logger logger = Logger.getLogger(ElevatorMqttAdapter.class.getName());

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
            logger.log(Level.SEVERE, "Configuration Error: {}", e.getMessage());
            System.exit(1);
        }
    }

    public void run(int interval) throws Exception {
        // Initialize elevators and floors
        mControlSystem.initializeElevatorsViaPLC();

        // check broker connection
        while (!connectToBroker()) {
            logger.info("Failed to connect to broker. Retrying in 5 seconds...");
            Thread.sleep(5000);
        }

        // retained messages
        publishRetainedMessages();

        // subscribe to topics
        subscribeToTopics();

        while (!mConnectionStatus) {
            Thread.sleep(500);
        }

        // Create a scheduled executor to handle periodic tasks
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Schedule the task to poll the PLC at the specified interval
        scheduler.scheduleAtFixedRate(new Runnable() {
            boolean initial = true;

            @Override
            public void run() {
                if (Thread.currentThread().isInterrupted()) {
                    scheduler.shutdown();
                    return;
                }

                if (mConnectionStatus && (System.currentTimeMillis() - mConnectionStatusTimestamp < 2000)) {
                    pollPLC(initial);
                    initial = false;  // Set initial to false after the first poll
                } else {
                    mConnectionStatus = false;
                }
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    private void pollPLC(boolean initial) {
        try {
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
        catch (Exception e) {
            reconnectToRMI();
        }
    }

    private boolean connectToBroker() {
        try {
            CompletableFuture<Mqtt5ConnAck> connAckFuture = mMqttClient.connect();
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

    private void publishRetainedMessages() {
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
                    .topic(MqttTopics.ELEVATOR_TOPIC + "/" + i + MqttTopics.CAPACITY_SUBTOPIC).retain(true)
                    .payload(String.valueOf(mControlSystem.getElevators()[i].getCapacity()).getBytes()).send();
        }
    }

    private void subscribeToTopics() {
        mMqttClient.subscribeWith()
                .topicFilter(MqttTopics.ELEVATOR_CONTROL_TOPIC + "/#")
                .callback(this::mqttCallback)
                .send();
    }

    private void mqttCallback(Mqtt5Publish publish) {
        String topic = publish.getTopic().toString();
        String[] parts = topic.split("/");

        if (parts.length == 2) {
            if(("/" + parts[1]).equals(MqttTopics.CONNECTION_STATUS_SUBTOPIC)) {
                mConnectionStatus = (Boolean.parseBoolean(new String(publish.getPayloadAsBytes())));
                mConnectionStatusTimestamp = System.currentTimeMillis();
            }
            else {
                logger.log(Level.WARNING, "Unknown subtopic in subscribeToTopics: {}", topic);
            }

            return;
        }

        if(parts.length != 3) {
            logger.log(Level.WARNING, "Invalid topic: {}", topic);
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
                    logger.log(Level.WARNING, "Unknown subtopic in subscribeToTopics: {}", topic);
                    break;
            }
        } catch (RemoteException e) {
            reconnectToRMI();
        }
    }

    private void reconnectToRMI() {
        String plcUrl = "";
        logger.info("Trying to reconnect to RMI...");
        try {
            // Read from property file
            Properties properties = new Properties();
            properties.load(ElevatorMqttAdapter.class.getResourceAsStream("/elevator.properties"));

            plcUrl = properties.getProperty("plc.url");
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Could not load elevator.properties: {}", e.getMessage());
            System.exit(1);
        }

        while (true) {
            try {
                // Attempt to reconnect to RMI
                IElevator plc = (IElevator) Naming.lookup(plcUrl);
                mControlSystem = new ElevatorControlSystem(plc);
                mControlSystem.initializeElevatorsViaPLC();
                publishRetainedMessages();
                logger.info("Reconnected to RMI successfully.");
                break; // Exit the loop once reconnected
            } catch (Exception e) {
                logger.warning("Failed to reconnect to RMI! ");
                try {
                    // Wait before retrying
                    Thread.sleep(5000); // 5 seconds
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    logger.severe("Reconnection wait interrupted.");
                    break; // Exit loop on interruption
                }
            }
        }
    }
}
