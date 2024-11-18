package at.fhhagenberg.sqelevator;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import io.vavr.control.Either;

import java.rmi.Naming;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ElevatorMqttAdapter {
    private final IElevator mPLC;
    private final ElevatorControlSystem mControlSystem;
    private final Mqtt5AsyncClient mMqttClient;

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

            // Connect to MQTT broker
            CompletableFuture<Mqtt5ConnAck> connAckFuture = mqttClient.connect();
            connAckFuture.whenComplete((connAck, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    System.out.println("Connected to MQTT broker");
                }
            });

            ElevatorMqttAdapter client = new ElevatorMqttAdapter(plc, mqttClient);

            // Initialize elevators and floors
            client.mControlSystem.initializeElevatorsViaPLC();
            client.pollPLC(interval);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pollPLC(int interval) {
        // Poll plc every interval
        while (true) {
            mControlSystem.updateDataViaPLC();
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
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
