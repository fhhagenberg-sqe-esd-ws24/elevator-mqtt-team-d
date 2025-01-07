package at.fhhagenberg.sqelevator.adapter;

import org.testcontainers.junit.jupiter.Testcontainers;
import sqelevator.IElevator;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the elevator mqtt adapter
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@Testcontainers
public class ElevatorMqttAdapterTest {
    /** The HiveMQ container */
    @Container
    static final HiveMQContainer hivemqCe = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"));

    /** The PLC mock */
    @Mock
    IElevator plc;

    /** The MQTT publisher */
    private Mqtt5AsyncClient publisher;
    /** The adapter to be tested */
    ElevatorMqttAdapter client;

    /**
     * Set up the test environment in general
     */
    @BeforeAll
    public static void setUpAll() {
        hivemqCe.start();
    }

    /**
     * Tear down the test environment
     */
    @AfterAll
    public static void tearDownAll() {
        hivemqCe.stop();
    }

    /**
     * Set up the test environment for each test
     */
    @BeforeEach
    public void setUp() throws Exception {
        // set up MqttClient
        Mqtt5AsyncClient mqttClient = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        publisher = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        publisher.connect().get(5, TimeUnit.SECONDS);
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
        when(plc.getServicesFloors(0, 0)).thenReturn(true);
        when(plc.getServicesFloors(0, 1)).thenReturn(true);
        when(plc.getServicesFloors(0, 2)).thenReturn(false);
        when(plc.getServicesFloors(0, 3)).thenReturn(false);
        when(plc.getServicesFloors(0, 4)).thenReturn(false);

        when(plc.getTarget(0)).thenReturn(4);
        when(plc.getCommittedDirection(0)).thenReturn(1);

        client = new ElevatorMqttAdapter(plc, mqttClient);
    }

    /**
     * Reset the test environment for each test
     */
    @AfterEach
    void tearDown() {
        publisher.disconnect();
    }

    /**
     * Test if the connection status is retained
     * @throws Exception if adapter encounters an error
     */
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

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
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
        latch.await();

        // Wait for all messages to be received or timeout after 5 seconds
        assertEquals(expectedMessages, receivedMessages, "Received messages do not match expected messages.");
        subscriber.disconnect();
    }

    /**
     * Test if the initial values are sent correctly
     * @throws Exception if adapter encounters an error
     */
    @Test
    void testInitialValues() throws Exception {
        Map<String, String> expectedMessages = Map.ofEntries(
            Map.entry("elevator/0/current_floor", "1"),
            Map.entry("elevator/0/acceleration", "15"),
            Map.entry("elevator/0/door_status", "2"),
            Map.entry("elevator/0/speed", "5"),
            Map.entry("elevator/0/weight", "10"),
            Map.entry("elevator/0/floor_requested/1", "true"),
            Map.entry("elevator/0/floor_requested/0", "false"),
            Map.entry("elevator/0/floor_requested/2", "false"),
            Map.entry("elevator/0/floor_requested/3", "false"),
            Map.entry("elevator/0/floor_requested/4", "false"),
            Map.entry("floor/0/button_up", "false"),
            Map.entry("floor/1/button_up", "false"),
            Map.entry("floor/2/button_up", "false"),
            Map.entry("floor/3/button_up", "false"),
            Map.entry("floor/4/button_up", "false"),
            Map.entry("floor/0/button_down", "false"),
            Map.entry("floor/1/button_down", "true"),
            Map.entry("floor/2/button_down", "false"),
            Map.entry("floor/3/button_down", "false"),
            Map.entry("floor/4/button_down", "false"),
            Map.entry("elevator/0/floor_serviced/0", "true"),
            Map.entry("elevator/0/floor_serviced/1", "true"),
            Map.entry("elevator/0/floor_serviced/2", "false"),
            Map.entry("elevator/0/floor_serviced/3", "false"),
            Map.entry("elevator/0/floor_serviced/4", "false"),
            Map.entry("elevator/0/target_floor", "4"),
            Map.entry("elevator/0/direction", "1")
        );

        ConcurrentHashMap<String, String> receivedMessages = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(expectedMessages.size());

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
            .addSubscription()
            .topicFilter("elevator/#")
            .applySubscription()
            .addSubscription()
            .topicFilter("floor/#")
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

        latch.await();
        // Wait for all messages to be received or timeout after 5 seconds
        assertEquals(expectedMessages, receivedMessages, "Received messages do not match expected messages.");
        subscriber.disconnect();
    }
}
