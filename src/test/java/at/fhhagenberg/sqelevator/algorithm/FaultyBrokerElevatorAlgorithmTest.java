package at.fhhagenberg.sqelevator.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class FaultyBrokerElevatorAlgorithmTest {
    private ElevatorAlgorithm elevatorAlgorithm;

    @Mock
    private Mqtt5AsyncClient mockMqttClient;

    @BeforeEach
    void setUp() {
        when(mockMqttClient.connect()).thenThrow(new RuntimeException("Connection failed")).thenReturn(null);
        elevatorAlgorithm = new ElevatorAlgorithm(mockMqttClient);
    }

    @Test
    void testRunWithFaultyBrokerConnection() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        Thread testThread = new Thread(() -> {
            try {
                elevatorAlgorithm.run();
            }
            catch (Exception e) {
                // do nothing, suppress InterruptedException warning during Thread.sleep()
            }
        });
        testThread.start();
        Thread.sleep(6000);

        assertTrue(baos.toString().contains("Failed to connect to broker. Retrying in 5 seconds..."));
        verify(mockMqttClient, times(2)).connect();

        testThread.interrupt();
        testThread.join();
    }
}
