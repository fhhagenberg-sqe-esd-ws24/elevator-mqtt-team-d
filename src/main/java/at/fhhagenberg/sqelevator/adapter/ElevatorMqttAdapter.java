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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adapter class for the elevator system that connects to the MQTT broker
 */
public class ElevatorMqttAdapter {
    /** The PLC */
    private IElevator mPLC;
    /** The control system */
    private ElevatorControlSystem mControlSystem;
    /** The MQTT client */
    private final Mqtt5AsyncClient mMqttClient;
    /** The connection status of the algorithm */
    private boolean mConnectionStatus = false;
    /** The connection status of the RMI */
    private boolean mRmiIsConnected = false;
    /** The timestamp of the connection status */
    private long mConnectionStatusTimestamp = 0;

    /** The logger */
    private static final Logger logger = Logger.getLogger(ElevatorMqttAdapter.class.getName());

    /**
     * Constructor
     * @param plc The PLC
     * @param mqttClient The MQTT client
     */
    public ElevatorMqttAdapter(IElevator plc, Mqtt5AsyncClient mqttClient) {
        mPLC = plc;
        mControlSystem = new ElevatorControlSystem(plc);
        mMqttClient = mqttClient;
    }

    /**
     * Main method
     * @param args The arguments
     */
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
            logger.log(Level.SEVERE, "Configuration Error: {0}", e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Run method
     * @param interval The polling interval
     * @throws Exception if run fails
     */
    public void run(int interval) throws Exception {
        // if run method is called -> RMI connected
        mRmiIsConnected = true;

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

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            boolean initial = true;
            @Override
            public void run() {
                if (mRmiIsConnected && mConnectionStatus && (System.currentTimeMillis() - mConnectionStatusTimestamp < 500)) {
                    pollPLC(initial);
                    initial = false;
                }
                else {
                    mConnectionStatus = false;
                }
            }
        }, 0, interval);
    }

    /**
     * Poll the PLC
     * @param initial If the poll is initial
     */
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
            // check if mqtt callback already caught rmi exception
            // prevent duplicate reconnect
            if (mRmiIsConnected) {
                mRmiIsConnected = false;
                reconnectToRMI();
            }
        }
    }

    /**
     * Connect to the mqtt broker
     * @return True if the connection was successful, false otherwise
     */
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

    /**
     * Publish retained messages
     */
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

    /**
     * Subscribe to elevator control topics
     */
    private void subscribeToTopics() {
        mMqttClient.subscribeWith()
                .topicFilter(MqttTopics.ELEVATOR_CONTROL_TOPIC + "/#")
                .callback(this::mqttCallback)
                .send();
    }

    /**
     * Unsubscribe from elevator control topics
     */
    private void unsubscribeFromTopics() {
        mMqttClient.unsubscribeWith()
                .topicFilter(MqttTopics.ELEVATOR_CONTROL_TOPIC + "/#")
                .send();
    }

    /**
     * Callback for MQTT elevator control messages
     * @param publish the mqtt message (topic + payload)
     */
    private void mqttCallback(Mqtt5Publish publish) {
        String topic = publish.getTopic().toString();
        String[] parts = topic.split("/");

        if (parts.length == 2) {
            if(("/" + parts[1]).equals(MqttTopics.CONNECTION_STATUS_SUBTOPIC)) {
                mConnectionStatus = (Boolean.parseBoolean(new String(publish.getPayloadAsBytes())));
                mConnectionStatusTimestamp = System.currentTimeMillis();
            }
            else {
                logger.log(Level.WARNING, "Unknown subtopic in subscribeToTopics: {0}", topic);
            }

            return;
        }

        if(parts.length != 3) {
            logger.log(Level.WARNING, "Invalid topic: {0}", topic);
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
                    logger.log(Level.WARNING, "Unknown subtopic in subscribeToTopics: {0}", topic);
                    break;
            }
        } catch (RemoteException e) {
            // check if timer task already caught remote exception
            // prevent duplicate reconnect
            if (mRmiIsConnected) {
                mRmiIsConnected = false;
                reconnectToRMI();
            }
        }
    }

    /**
     * Reconnect to RMI
     */
    private void reconnectToRMI() {
        // unsubscribe from incoming mqtt messages
        unsubscribeFromTopics();
        String plcUrl = "";
        logger.info("Trying to reconnect to RMI...");
        try {
            // Read from property file
            Properties properties = new Properties();
            properties.load(ElevatorMqttAdapter.class.getResourceAsStream("/elevator.properties"));

            plcUrl = properties.getProperty("plc.url");
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Could not load elevator.properties: {0}", e.getMessage());
            System.exit(1);
        }

        while (true) {
            try {
                // Attempt to reconnect to RMI
                mPLC = (IElevator) Naming.lookup(plcUrl);
                mRmiIsConnected = true;
                mControlSystem = new ElevatorControlSystem(mPLC);
                mControlSystem.initializeElevatorsViaPLC();
                publishRetainedMessages();
                subscribeToTopics();
                pollPLC(true);
                logger.info("Reconnected to RMI successfully.");
                break; // Exit the loop once reconnected
            } catch (Exception e) {
                logger.warning("Failed to reconnect to RMI! ");
                try {
                    // Wait before retrying
                    Thread.sleep(5000);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    logger.severe("Reconnection wait interrupted.");
                    break; // Exit loop on interruption
                }
            }
        }
    }
}
