package at.fhhagenberg.sqelevator.adapter;

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
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import sqelevator.IElevator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@Testcontainers
public class RMIDisconnectElevatorMqttAdapterTest {
    @Container
    static final HiveMQContainer hivemqCe = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"));

    @Mock
    IElevator plc;

    private Mqtt5AsyncClient publisher;
    private Mqtt5AsyncClient mqttClient;
    ElevatorMqttAdapter client;

    private final Logger logger = Logger.getLogger(ElevatorMqttAdapter.class.getName());

    @BeforeAll
    public static void setUpAll() { hivemqCe.start(); }

    @AfterAll
    public static void tearDownAll() { hivemqCe.stop(); }

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

        publisher.connect().get(10, TimeUnit.SECONDS);
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

    @Test
    public void testRemoteExceptionOnUpdateECS() throws Exception {
        when(plc.getElevatorButton(0, 1)).thenThrow(new RemoteException("RemoteException thrown!"));

        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        ConsoleHandler consoleHandler = new CaptureLoggingConsoleHandler(logStream);
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        client.run(250);
        await().atMost(15, TimeUnit.SECONDS).until(() -> logStream.toString().contains("Trying to reconnect to RMI..."));
        logger.removeHandler(consoleHandler);
    }

    @Test
    public void testRemoteExceptionOnSetDirection() throws Exception {
        when(plc.getElevatorButton(0, 1)).thenReturn(false);
        doThrow(new RemoteException("RemoteException thrown!")).when(plc).setCommittedDirection(0, 0);

        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        ConsoleHandler consoleHandler = new CaptureLoggingConsoleHandler(logStream);
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        client.run(250);
        publisher.publishWith()
                .topic("elevator_control/0/direction").retain(true)
                .payload("0".getBytes())
                .send();
        await().atMost(15, TimeUnit.SECONDS).until(() -> logStream.toString().contains("Trying to reconnect to RMI..."));
        logger.removeHandler(consoleHandler);
    }

    @Test
    public void testRemoteExceptionOnSetTarget() throws Exception {
        when(plc.getElevatorButton(0, 1)).thenReturn(false);
        doThrow(new RemoteException("RemoteException thrown!")).when(plc).setTarget(0, 0);

        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        ConsoleHandler consoleHandler = new CaptureLoggingConsoleHandler(logStream);
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        client.run(250);
        publisher.publishWith()
                .topic("elevator_control/0/target_floor").retain(true)
                .payload("0".getBytes())
                .send();
        await().atMost(15, TimeUnit.SECONDS).until(() -> logStream.toString().contains("Trying to reconnect to RMI..."));
        logger.removeHandler(consoleHandler);
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