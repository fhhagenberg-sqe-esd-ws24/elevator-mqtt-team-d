package at.fhhagenberg.sqelevator.adapter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sqelevator.IElevator;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

/**
 * Test class for the elevator mqtt adapter with faulty mqtt broker
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class FaultyBrokerElevatorMqttAdapterTest {
    /** The adapter to be tested */
    private ElevatorMqttAdapter adapter;

    /** The MQTT client for the adapter (mocked in this case) */
    @Mock
    private Mqtt5AsyncClient mockMqttClient;

    /** The PLC mock */
    @Mock
    private IElevator plc;

    /** The test thread */
    private Thread testThread;

    /** The logger for the test */
    private final Logger logger = Logger.getLogger(ElevatorMqttAdapter.class.getName());

    /**
     * Set up the test environment for each test
     */
    @BeforeEach
    void setUp() throws Exception {
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

        adapter = new ElevatorMqttAdapter(plc, mockMqttClient);
    }

    /**
     * Reset the test environment for each test
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
     * Test case which tests faulty mqtt broker
     * @throws Exception if adapter encounters an error
     */
    @Test
    void testRunWithFaultyBrokerConnection() throws Exception {
        when(mockMqttClient.connect()).thenThrow(new RuntimeException("Connection failed")).thenReturn(null);

        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        ConsoleHandler consoleHandler = new CaptureLoggingConsoleHandler(logStream);
        consoleHandler.setLevel(Level.ALL); // Capture all levels of logs
        logger.addHandler(consoleHandler);

        testThread = new Thread(() -> {
            try {
                adapter.run(500);
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
         * @param logRecord  description of the log event. A null record is
         *                 silently ignored and is not published
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
         * @throws SecurityException if a security manager exists and if the caller does not have LoggingPermission("control").
         */
        @Override
        public void close() throws SecurityException {
            // no-op
        }
    }
}