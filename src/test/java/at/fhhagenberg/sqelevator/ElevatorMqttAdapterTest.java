package at.fhhagenberg.sqelevator;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ElevatorMqttAdapterTest {
    @Container
    final static HiveMQContainer hivemqCe = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"));

    @Mock
    IElevator plc;

    private Mqtt5AsyncClient publisher;
    private Mqtt5AsyncClient mqttClient;
    ElevatorMqttAdapter client;

    @BeforeAll
    public static void setUpAll() {
        hivemqCe.start();
    }

    @AfterAll
    public static void tearDownAll() {
        hivemqCe.stop();
    }

    @BeforeEach
    public void setUp() throws Exception {
        // set up MqttClient

        mqttClient = Mqtt5Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost(hivemqCe.getHost())
            .serverPort(hivemqCe.getMqttPort())
            .buildAsync();

        publisher = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        publisher.connect().get(1, TimeUnit.SECONDS);
        publisher.publishWith()
                .topic("elevator_control/connection_status").retain(true)
                .payload(String.valueOf(true).getBytes()).send();

        when(plc.getElevatorNum()).thenReturn(1);
        when(plc.getElevatorFloor(0)).thenReturn(1);
        when(plc.getElevatorAccel(0)).thenReturn(15);
        when(plc.getElevatorDoorStatus(0)).thenReturn(2);
        when(plc.getElevatorSpeed(0)).thenReturn(5);
        when(plc.getElevatorWeight(0)).thenReturn(10);
        when(plc.getElevatorCapacity(0)).thenReturn(5);
        when(plc.getElevatorButton(0, 1)).thenReturn(true);
        when(plc.getElevatorButton(0, 0)).thenReturn(false);
        when(plc.getElevatorButton(0, 2)).thenReturn(false);
        when(plc.getElevatorButton(0, 3)).thenReturn(false);
        when(plc.getElevatorButton(0, 4)).thenReturn(false);

        when(plc.getFloorButtonDown(0)).thenReturn(false);
        when(plc.getFloorButtonDown(1)).thenReturn(true);
        when(plc.getFloorButtonDown(2)).thenReturn(false);
        when(plc.getFloorButtonDown(3)).thenReturn(false);
        when(plc.getFloorButtonDown(4)).thenReturn(false);
        when(plc.getFloorButtonUp(0)).thenReturn(false);
        when(plc.getFloorButtonUp(1)).thenReturn(false);
        when(plc.getFloorButtonUp(2)).thenReturn(false);
        when(plc.getFloorButtonUp(3)).thenReturn(false);
        when(plc.getFloorButtonUp(4)).thenReturn(false);
        when(plc.getFloorNum()).thenReturn(5);
        when(plc.getFloorHeight()).thenReturn(3);
        when(plc.getServicesFloors(0, 0)).thenReturn(false);
        when(plc.getServicesFloors(0, 1)).thenReturn(true);
        when(plc.getServicesFloors(0, 2)).thenReturn(false);
        when(plc.getServicesFloors(0, 3)).thenReturn(false);
        when(plc.getServicesFloors(0, 4)).thenReturn(false);

        when(plc.getTarget(0)).thenReturn(4);
        when(plc.getCommittedDirection(0)).thenReturn(1);

        client = new ElevatorMqttAdapter(plc, mqttClient);
    }

    @AfterEach
    void tearDown() {
        publisher.disconnect();
    }

    @Test
    public void testRetainedTopics() throws Exception {
        Map<String, String> expectedMessages = Map.of(
                "info/num_of_elevators", "1",
                "info/floor_height", "3",
                "info/num_of_floors", "5",
                "elevator/0/capacity", "5"
        );

        ConcurrentHashMap<String, String> receivedMessages = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(expectedMessages.size());

        var subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                    .topicFilter("info/#")
                    .applySubscription()
                .addSubscription()
                    .topicFilter("elevator/+/capacity")
                    .applySubscription()
                .callback(publish -> {
                    String payload = new String(publish.getPayloadAsBytes());
                    if (expectedMessages.containsKey(publish.getTopic().toString()) && payload.equals(expectedMessages.get(publish.getTopic().toString()))) {
                        receivedMessages.put(publish.getTopic().toString(), payload);
                        latch.countDown();
                    }
                })
                .send();

        client.run(250);

        // Wait for all messages to be received or timeout after 5 seconds
        assertEquals(expectedMessages, receivedMessages, "Received messages do not match expected messages.");

        subscriber.disconnect();
    }
}
