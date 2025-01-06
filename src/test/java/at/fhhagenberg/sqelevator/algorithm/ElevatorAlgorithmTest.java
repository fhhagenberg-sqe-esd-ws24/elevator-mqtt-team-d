package at.fhhagenberg.sqelevator.algorithm;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.junit.jupiter.api.*;

import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ElevatorAlgorithmTest {
    @Container
    static final HiveMQContainer hivemqCe = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"));

    private Mqtt5AsyncClient publisher;
    private Mqtt5AsyncClient mqttClient;

    ElevatorAlgorithm client;

    private boolean connected = false;

    @BeforeAll
    public static void setUpAll() {
        hivemqCe.start();
    }

    @AfterAll
    public static void tearDownAll() {
        hivemqCe.stop();
    }

    @AfterEach
    void tearDown() {
        publisher.disconnect();
    }

    @BeforeEach
    public void setUp() throws Exception {

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

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        client = new ElevatorAlgorithm(mqttClient);

        publisher.connect().get(2, TimeUnit.SECONDS);
        publisher.publishWith()
                .topic("info/num_of_elevators")
                .payload("2".getBytes())
                .retain(true)
                .send();

        publisher.publishWith()
                .topic("info/floor_height")
                .payload("3".getBytes())
                .retain(true)
                .send();

        publisher.publishWith()
                .topic("info/num_of_floors")
                .payload("5".getBytes())
                .retain(true)
                .send();

        publisher.publishWith()
                .topic("elevator/0/capacity")
                .payload("5".getBytes())
                .retain(true)
                .send();

        publisher.publishWith()
                .topic("elevator/1/capacity")
                .payload("5".getBytes())
                .retain(true)
                .send();

        CountDownLatch latch = new CountDownLatch(1);

        subscriber.connect();
        subscriber.subscribeWith()
                .topicFilter("elevator_control/connection_status")
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    if(payload.equals("true")){
                        connected = true;
                        latch.countDown();
                    }
                })
                .send();
        client.run();
        latch.await();

        subscriber.disconnect();
    }

    @Test
    public void testConnectionStatus() throws Exception {
        assertTrue(connected);
    }

    @Test
    public void testUncommittedUpUncommitted() throws Exception {
        assertTrue(connected);

        final AtomicReference<Map<String, String>> expectedMessages = new AtomicReference<>(Map.of(
                "elevator_control/0/direction", "2"
        ));
        final AtomicReference<Map<String, String>> receivedMessages = new AtomicReference<>(new ConcurrentHashMap<>());
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                .topicFilter("elevator_control/+/direction")
                .applySubscription()
                .addSubscription()
                .topicFilter("elevator_control/+/target_floor")
                .applySubscription()
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    String topic = message.getTopic().toString();
                    if (expectedMessages.get().containsKey(topic) && payload.equals(expectedMessages.get().get(topic)) && !receivedMessages.get().containsKey(topic)) {
                        receivedMessages.get().put(topic, payload);
                        latch.get().countDown();
                    }
                })
                .send();

        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("1".getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "1"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/floor_requested/1/")
                .payload(String.valueOf(true).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "2"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(1));

        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("0".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/floor_requested/1")
                .payload(String.valueOf(false).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        subscriber.disconnect();
    }

    @Test
    public void testUncommittedUpUp() throws Exception {
        assertTrue(connected);

        final AtomicReference<Map<String, String>> expectedMessages = new AtomicReference<>(Map.of(
                "elevator_control/0/direction", "2"
        ));
        final AtomicReference<Map<String, String>> receivedMessages = new AtomicReference<>(new ConcurrentHashMap<>());
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                .topicFilter("elevator_control/+/direction")
                .applySubscription()
                .addSubscription()
                .topicFilter("elevator_control/+/target_floor")
                .applySubscription()
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    String topic = message.getTopic().toString();
                    if (expectedMessages.get().containsKey(topic) && payload.equals(expectedMessages.get().get(topic)) && !receivedMessages.get().containsKey(topic)) {
                        receivedMessages.get().put(topic, payload);
                        latch.get().countDown();
                    }
                })
                .send();

        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("1".getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "1"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/floor_requested/1/")
                .payload(String.valueOf(true).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "4"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("0".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/floor_requested/4")
                .payload(String.valueOf(true).getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/floor_requested/1")
                .payload(String.valueOf(false).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        subscriber.disconnect();
    }

    @Test
    public void testUncommittedUpDown() throws Exception {
        assertTrue(connected);

        final AtomicReference<Map<String, String>> expectedMessages = new AtomicReference<>(Map.of(
                "elevator_control/0/direction", "2"
        ));
        final AtomicReference<Map<String, String>> receivedMessages = new AtomicReference<>(new ConcurrentHashMap<>());
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                .topicFilter("elevator_control/+/direction")
                .applySubscription()
                .addSubscription()
                .topicFilter("elevator_control/+/target_floor")
                .applySubscription()
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    String topic = message.getTopic().toString();
                    if (expectedMessages.get().containsKey(topic) && payload.equals(expectedMessages.get().get(topic)) && !receivedMessages.get().containsKey(topic)) {
                        receivedMessages.get().put(topic, payload);
                        latch.get().countDown();
                    }
                })
                .send();

        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("1".getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "1"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/floor_requested/1/")
                .payload(String.valueOf(true).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "1",
                "elevator_control/0/target_floor", "0"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("0".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/floor_requested/0")
                .payload(String.valueOf(true).getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/floor_requested/1")
                .payload(String.valueOf(false).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        subscriber.disconnect();
    }

    @Test
    public void testUncommittedDownUncommitted() throws Exception {
        assertTrue(connected);

        final AtomicReference<Map<String, String>> expectedMessages = new AtomicReference<>(Map.of(
                "elevator_control/0/direction", "2"
        ));
        final AtomicReference<Map<String, String>> receivedMessages = new AtomicReference<>(new ConcurrentHashMap<>());
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                .topicFilter("elevator_control/+/direction")
                .applySubscription()
                .addSubscription()
                .topicFilter("elevator_control/+/target_floor")
                .applySubscription()
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    String topic = message.getTopic().toString();
                    if (expectedMessages.get().containsKey(topic) && payload.equals(expectedMessages.get().get(topic)) && !receivedMessages.get().containsKey(topic)) {
                        receivedMessages.get().put(topic, payload);
                        latch.get().countDown();
                    }
                })
                .send();

        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("1".getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "1",
                "elevator_control/0/target_floor", "0"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/floor_requested/0/")
                .payload(String.valueOf(true).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "2"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(1));

        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("0".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("0".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/floor_requested/0")
                .payload(String.valueOf(false).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        subscriber.disconnect();
    }

    @Test
    public void testUncommittedDownDown() throws Exception {
        assertTrue(connected);

        final AtomicReference<Map<String, String>> expectedMessages = new AtomicReference<>(Map.of(
                "elevator_control/0/direction", "2"
        ));
        final AtomicReference<Map<String, String>> receivedMessages = new AtomicReference<>(new ConcurrentHashMap<>());
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                .topicFilter("elevator_control/+/direction")
                .applySubscription()
                .addSubscription()
                .topicFilter("elevator_control/+/target_floor")
                .applySubscription()
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    String topic = message.getTopic().toString();
                    if (expectedMessages.get().containsKey(topic) && payload.equals(expectedMessages.get().get(topic)) && !receivedMessages.get().containsKey(topic)) {
                        receivedMessages.get().put(topic, payload);
                        latch.get().countDown();
                    }
                })
                .send();

        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("2".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("2".getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "1",
                "elevator_control/0/target_floor", "1"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/floor_requested/1/")
                .payload(String.valueOf(true).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "1",
                "elevator_control/0/target_floor", "0"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/floor_requested/0")
                .payload(String.valueOf(true).getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/floor_requested/1")
                .payload(String.valueOf(false).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        subscriber.disconnect();
    }

    @Test
    public void testUncommittedFloorUpFloorDownRequested() throws Exception {
        assertTrue(connected);

        final AtomicReference<Map<String, String>> expectedMessages = new AtomicReference<>(Map.of(
                "elevator_control/0/direction", "2"
        ));
        final AtomicReference<Map<String, String>> receivedMessages = new AtomicReference<>(new ConcurrentHashMap<>());
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                .topicFilter("elevator_control/+/direction")
                .applySubscription()
                .addSubscription()
                .topicFilter("elevator_control/+/target_floor")
                .applySubscription()
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    String topic = message.getTopic().toString();
                    if (expectedMessages.get().containsKey(topic) && payload.equals(expectedMessages.get().get(topic)) && !receivedMessages.get().containsKey(topic)) {
                        receivedMessages.get().put(topic, payload);
                        latch.get().countDown();
                    }
                })
                .send();

        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("3".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("3".getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "4"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("floor/4/button_down/")
                .payload(String.valueOf(true).getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/1/button_up/")
                .payload(String.valueOf(true).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "1",
                "elevator_control/0/target_floor", "1"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("4".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("4".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("0".getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/4/button_down")
                .payload(String.valueOf(false).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        subscriber.disconnect();
    }

    @Test
    public void testUncommittedFloorDownFloorUpRequested() throws Exception {
        assertTrue(connected);

        final AtomicReference<Map<String, String>> expectedMessages = new AtomicReference<>(Map.of(
                "elevator_control/0/direction", "2"
        ));
        final AtomicReference<Map<String, String>> receivedMessages = new AtomicReference<>(new ConcurrentHashMap<>());
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                .topicFilter("elevator_control/+/direction")
                .applySubscription()
                .addSubscription()
                .topicFilter("elevator_control/+/target_floor")
                .applySubscription()
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    String topic = message.getTopic().toString();
                    if (expectedMessages.get().containsKey(topic) && payload.equals(expectedMessages.get().get(topic)) && !receivedMessages.get().containsKey(topic)) {
                        receivedMessages.get().put(topic, payload);
                        latch.get().countDown();
                    }
                })
                .send();

        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("2".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("2".getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "1",
                "elevator_control/0/target_floor", "1"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("floor/4/button_down/")
                .payload(String.valueOf(true).getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/1/button_up/")
                .payload(String.valueOf(true).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "4"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/1/button_up")
                .payload(String.valueOf(false).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        subscriber.disconnect();
    }

    @Test
    public void testUncommittedFloorUpFloorUpRequested() throws Exception {
        assertTrue(connected);

        final AtomicReference<Map<String, String>> expectedMessages = new AtomicReference<>(Map.of(
                "elevator_control/0/direction", "2"
        ));
        final AtomicReference<Map<String, String>> receivedMessages = new AtomicReference<>(new ConcurrentHashMap<>());
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                .topicFilter("elevator_control/+/direction")
                .applySubscription()
                .addSubscription()
                .topicFilter("elevator_control/+/target_floor")
                .applySubscription()
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    String topic = message.getTopic().toString();
                    if (expectedMessages.get().containsKey(topic) && payload.equals(expectedMessages.get().get(topic)) && !receivedMessages.get().containsKey(topic)) {
                        receivedMessages.get().put(topic, payload);
                        latch.get().countDown();
                    }
                })
                .send();

        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("1".getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "2"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("floor/2/button_up/")
                .payload(String.valueOf(true).getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/3/button_up/")
                .payload(String.valueOf(true).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "3"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("2".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("2".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("0".getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/2/button_up")
                .payload(String.valueOf(false).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        subscriber.disconnect();
    }

    @Test
    public void testUncommittedFloorDownFloorDownRequested() throws Exception {
        assertTrue(connected);

        final AtomicReference<Map<String, String>> expectedMessages = new AtomicReference<>(Map.of(
                "elevator_control/0/direction", "2"
        ));
        final AtomicReference<Map<String, String>> receivedMessages = new AtomicReference<>(new ConcurrentHashMap<>());
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                .topicFilter("elevator_control/+/direction")
                .applySubscription()
                .addSubscription()
                .topicFilter("elevator_control/+/target_floor")
                .applySubscription()
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    String topic = message.getTopic().toString();
                    if (expectedMessages.get().containsKey(topic) && payload.equals(expectedMessages.get().get(topic)) && !receivedMessages.get().containsKey(topic)) {
                        receivedMessages.get().put(topic, payload);
                        latch.get().countDown();
                    }
                })
                .send();

        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("4".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("4".getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "1",
                "elevator_control/0/target_floor", "2"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("floor/2/button_down/")
                .payload(String.valueOf(true).getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/1/button_down/")
                .payload(String.valueOf(true).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "1",
                "elevator_control/0/target_floor", "1"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("2".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("2".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/2/button_down")
                .payload(String.valueOf(false).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        subscriber.disconnect();
    }

    @Test
    public void testUpFloorUpRequested() throws Exception {
        assertTrue(connected);

        final AtomicReference<Map<String, String>> expectedMessages = new AtomicReference<>(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "4"
        ));
        final AtomicReference<Map<String, String>> receivedMessages = new AtomicReference<>(new ConcurrentHashMap<>());
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(2));

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                .topicFilter("elevator_control/+/direction")
                .applySubscription()
                .addSubscription()
                .topicFilter("elevator_control/+/target_floor")
                .applySubscription()
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    String topic = message.getTopic().toString();
                    if (expectedMessages.get().containsKey(topic) && payload.equals(expectedMessages.get().get(topic)) && !receivedMessages.get().containsKey(topic)) {
                        receivedMessages.get().put(topic, payload);
                        latch.get().countDown();
                    }
                })
                .send();

        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("2".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("4".getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/4/button_down")
                .payload(String.valueOf(true).getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("0".getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "3"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("floor/3/button_up/")
                .payload(String.valueOf(true).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "4"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("3".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("3".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("0".getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/3/button_up")
                .payload(String.valueOf(false).getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("1".getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        subscriber.disconnect();
    }

    @Test
    public void testUpFloorUpRequestedFloorNotServiced() throws Exception {
        assertTrue(connected);

        final AtomicReference<Map<String, String>> expectedMessages = new AtomicReference<>(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "4"
        ));
        final AtomicReference<Map<String, String>> receivedMessages = new AtomicReference<>(new ConcurrentHashMap<>());
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(2));

        Mqtt5AsyncClient subscriber = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(hivemqCe.getHost())
                .serverPort(hivemqCe.getMqttPort())
                .buildAsync();

        subscriber.connect();
        subscriber.subscribeWith()
                .addSubscription()
                .topicFilter("elevator_control/+/direction")
                .applySubscription()
                .addSubscription()
                .topicFilter("elevator_control/+/target_floor")
                .applySubscription()
                .callback(message -> {
                    String payload = new String(message.getPayloadAsBytes());
                    String topic = message.getTopic().toString();
                    if (expectedMessages.get().containsKey(topic) && payload.equals(expectedMessages.get().get(topic)) && !receivedMessages.get().containsKey(topic)) {
                        receivedMessages.get().put(topic, payload);
                        latch.get().countDown();
                    }
                })
                .send();

        publisher.publishWith()
                .topic("elevator/0/door_status")
                .payload("2".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/direction")
                .payload("0".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/target_floor")
                .payload("4".getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/4/button_down")
                .payload(String.valueOf(true).getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("0".getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/floor_serviced/2")
                .payload(String.valueOf(false).getBytes())
                .send();
        publisher.publishWith()
                .topic("elevator/0/floor_serviced/0")
                .payload(String.valueOf(false).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        expectedMessages.set(Map.of(
                "elevator_control/0/direction", "0",
                "elevator_control/0/target_floor", "4"
        ));
        receivedMessages.set(new ConcurrentHashMap<>());
        latch.set(new CountDownLatch(2));

        publisher.publishWith()
                .topic("elevator/0/current_floor")
                .payload("1".getBytes())
                .send();
        publisher.publishWith()
                .topic("floor/2/button_up/")
                .payload(String.valueOf(true).getBytes())
                .send();

        latch.get().await();
        assertEquals(expectedMessages.get(), receivedMessages.get()); // Compare contents

        subscriber.disconnect();
    }
}
