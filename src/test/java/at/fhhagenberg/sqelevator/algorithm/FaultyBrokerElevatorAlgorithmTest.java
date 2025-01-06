package at.fhhagenberg.sqelevator.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.awaitility.Awaitility.await;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class FaultyBrokerElevatorAlgorithmTest {
    private ElevatorAlgorithm elevatorAlgorithm;

    @Mock
    private Mqtt5AsyncClient mockMqttClient;

    private static final Logger logger = Logger.getLogger(ElevatorAlgorithm.class.getName());

    @BeforeEach
    void setUp() {
        when(mockMqttClient.connect()).thenThrow(new RuntimeException("Connection failed")).thenReturn(null);
        elevatorAlgorithm = new ElevatorAlgorithm(mockMqttClient);
    }

    @Test
    void testRunWithFaultyBrokerConnection() throws Exception {
        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        ConsoleHandler consoleHandler = new CaptureLoggingConsoleHandler(logStream);
        consoleHandler.setLevel(Level.ALL); // Capture all levels of logs
        logger.addHandler(consoleHandler);

        Thread testThread = new Thread(() -> {
            try {
                elevatorAlgorithm.run();
            }
            catch (Exception e) {
                // do nothing, suppress InterruptedException warning during Thread.sleep()
            }
        });
        testThread.start();
        await().atMost(6, TimeUnit.SECONDS).until(() -> logStream.toString().contains("Failed to connect to broker. Retrying in 5 seconds..."));
        verify(mockMqttClient, times(1)).connect();

        testThread.interrupt();
        testThread.join();
    }

    private static class CaptureLoggingConsoleHandler extends ConsoleHandler {
        private final ByteArrayOutputStream byteArrayOutputStream;

        public CaptureLoggingConsoleHandler(ByteArrayOutputStream byteArrayOutputStream) {
            this.byteArrayOutputStream = byteArrayOutputStream;
        }

        @Override
        public void publish(java.util.logging.LogRecord logRecord) {
            try {
                byteArrayOutputStream.write(getFormatter().format(logRecord).getBytes());
            } catch (IOException e) {
                // no-op
            }
        }

        @Override
        public void flush() {
            // no-op
        }

        @Override
        public void close() throws SecurityException {
            // no-op
        }
    }
}
