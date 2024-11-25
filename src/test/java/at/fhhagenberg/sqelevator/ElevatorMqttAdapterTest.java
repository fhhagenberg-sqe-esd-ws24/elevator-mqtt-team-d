package at.fhhagenberg.sqelevator;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ElevatorMqttAdapterTest {
    @Container
    final HiveMQContainer hivemqCe = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"));

    @Test
    public void test_mqtt() throws Exception {
        hivemqCe.start();
        var publisher = Mqtt5Client.builder()
                .serverPort(hivemqCe.getMqttPort())
                .serverHost(hivemqCe.getHost())
                .identifier("publisher")
                .buildBlocking();

        publisher.connect();

        var subscriber = Mqtt5Client.builder()
                .serverPort(hivemqCe.getMqttPort())
                .serverHost(hivemqCe.getHost())
                .identifier("subscriber")
                .buildBlocking();

        var publishes = subscriber.publishes(MqttGlobalPublishFilter.ALL);
        subscriber.connect();
        subscriber.subscribeWith().topicFilter("topic/test").send();

        publisher.publishWith()
                .topic("topic/test")
                .payload("Hello World!".getBytes()).send();

        var receive = publishes.receive();

        assertNotNull(receive); // 4
        assertEquals("Hello World!", new String(receive.getPayloadAsBytes()));
        publisher.disconnect();
        subscriber.disconnect();
        hivemqCe.stop();
    }
}
