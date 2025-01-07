package at.fhhagenberg.sqelevator.algorithm;

import org.junit.jupiter.api.AfterEach;
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

/**
 * Test class for the elevator algorithm with faulty mqtt broker
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class FaultyBrokerElevatorAlgorithmTest {
    /** The algorithm to be tested */
    private ElevatorAlgorithm elevatorAlgorithm;

    /** The MQTT client for the algorithm (mocked in this case) */
    @Mock
    private Mqtt5AsyncClient mockMqttClient;

    /** The test thread */
    private Thread testThread;

    /** The logger for the test */
    private final Logger logger = Logger.getLogger(ElevatorAlgorithm.class.getName());

    /**
     * Set up the test environment for each test
     */
    @BeforeEach
    void setUp() {
        when(mockMqttClient.connect()).thenThrow(new RuntimeException("Connection failed")).thenReturn(null);
        elevatorAlgorithm = new ElevatorAlgorithm(mockMqttClient);
    }

    /**
     * Tear down the test environment after each test
     * @throws Exception if thread join fails
     */
    @AfterEach
    void tearDown() throws Exception {
        if (testThread != null) {
            testThread.interrupt();
            testThread.join();
        }
        // Clean up logging after each test
        for (java.util.logging.Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
    }

    /**
     * Test case which tests the run method with a faulty broker connection
     * @throws Exception if algorithm run fails
     */
    @Test
    void testRunWithFaultyBrokerConnection() throws Exception {
        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        ConsoleHandler consoleHandler = new CaptureLoggingConsoleHandler(logStream);
        consoleHandler.setLevel(Level.ALL); // Capture all levels of logs
        logger.addHandler(consoleHandler);

        testThread = new Thread(() -> {
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

    /**
     * Console Handler for capturing logs
     */
    private static class CaptureLoggingConsoleHandler extends ConsoleHandler {
        /** The byte array output stream */
        private final ByteArrayOutputStream byteArrayOutputStream;

        /**
         * Constructor for the console handler
         * @param byteArrayOutputStream the byte array output stream
         */
        public CaptureLoggingConsoleHandler(ByteArrayOutputStream byteArrayOutputStream) {
            this.byteArrayOutputStream = byteArrayOutputStream;
        }

        /**
         * Publish the log record
         * @param logRecord the log record to publish
         */
        @Override
        public void publish(java.util.logging.LogRecord logRecord) {
            try {
                byteArrayOutputStream.write(getFormatter().format(logRecord).getBytes());
            } catch (IOException e) {
                // no-op
            }
        }

        /**
         * Flush the console handler
         */
        @Override
        public void flush() {
            // no-op
        }

        /**
         * Close the console handler
         * @throws SecurityException if closing the handler fails (in this case never)
         */
        @Override
        public void close() throws SecurityException {
            // no-op
        }
    }
}
