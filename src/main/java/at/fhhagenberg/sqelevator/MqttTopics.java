package at.fhhagenberg.sqelevator;

/**
 * Class which represents the MQTT topics.
 */
public class MqttTopics {
    /** The topic for the elevator control. */
    public static final String ELEVATOR_CONTROL_TOPIC = "elevator_control";
    /** The subtopic for the connection status of the algorithm. */
    public static final String CONNECTION_STATUS_SUBTOPIC = "/connection_status";
    /** The topic for the elevator. */
    public static final String ELEVATOR_TOPIC = "elevator";
    /** The subtopic for the capacity. */
    public static final String CAPACITY_SUBTOPIC = "/capacity";
    /** The subtopic for the speed. */
    public static final String SPEED_SUBTOPIC = "/speed";
    /** The subtopic for the acceleration. */
    public static final String ACCELERATION_SUBTOPIC = "/acceleration";
    /** The subtopic for the direction. */
    public static final String DIRECTION_SUBTOPIC = "/direction";
    /** The subtopic for the door status. */
    public static final String DOOR_STATUS_SUBTOPIC = "/door_status";
    /** The subtopic for the current floor. */
    public static final String CURRENT_FLOOR_SUBTOPIC = "/current_floor";
    /** The subtopic for the target floor. */
    public static final String TARGET_FLOOR_SUBTOPIC = "/target_floor";
    /** The subtopic for the weight. */
    public static final String WEIGHT_SUBTOPIC = "/weight";
    /** The subtopic for the requested floor. */
    public static final String FLOOR_REQUESTED_SUBTOPIC = "/floor_requested";
    /** The subtopic for the serviced floor. */
    public static final String FLOOR_SERVICED_SUBTOPIC = "/floor_serviced";

    /** The topic for the info topic. */
    public static final String INFO_TOPIC = "info";
    /** The subtopic for the number of elevators. */
    public static final String NUM_OF_ELEVATORS_SUBTOPIC = "/num_of_elevators";
    /** The subtopic for the floor height. */
    public static final String FLOOR_HEIGHT_SUBTOPIC = "/floor_height";
    /** The subtopic for the number of floors. */
    public static final String NUM_OF_FLOORS_SUBTOPIC = "/num_of_floors";

    /** The topic for the floor. */
    public static final String FLOOR_TOPIC = "floor";
    /** The subtopic for the button up. */
    public static final String BUTTON_UP_SUBTOPIC = "/button_up";
    /** The subtopic for the button down. */
    public static final String BUTTON_DOWN_SUBTOPIC = "/button_down";


}
