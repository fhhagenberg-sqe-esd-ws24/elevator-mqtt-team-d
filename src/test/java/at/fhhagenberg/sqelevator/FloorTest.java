package at.fhhagenberg.sqelevator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the floor class
 */
public class FloorTest {
    private Floor floor;

    /**
     * Function which is executed before each unit test.
     */
    @BeforeEach
    public void setUp() {
        floor = new Floor();
    }

    /**
     * Test case which tests initial status of button down.
     */
    @Test
    public void testGetButtonDownPressedInitial() {
        assertFalse(floor.getButtonDownPressed());
    }

    /**
     * Test case which tests initial status of button up.
     */
    @Test
    public void testGetButtonUpPressedInitial() {
        assertFalse(floor.getButtonUpPressed());
    }

    /**
     * Test case which tests both possibilities of setting the button down status.
     */
    @Test
    public void testSetButtonDownPressed() {
        floor.setButtonDownPressed(true);
        assertTrue(floor.getButtonDownPressed());
        floor.setButtonDownPressed(false);
        assertFalse(floor.getButtonDownPressed());
    }

    /**
     * Test case which tests both possibilities of setting the button up status.
     */
    @Test
    public void testSetButtonUpPressed() {
        floor.setButtonUpPressed(true);
        assertTrue(floor.getButtonUpPressed());
        floor.setButtonUpPressed(false);
        assertFalse(floor.getButtonUpPressed());
    }

    /**
     * Test case which tests if setting button down status influences the button up status.
     */
    @Test
    public void testSetButtonDownPressedAffectEachOther() {
        floor.setButtonDownPressed(true);
        assertFalse(floor.getButtonUpPressed());
        floor.setButtonUpPressed(true);
        floor.setButtonDownPressed(false);
        assertTrue(floor.getButtonUpPressed());
    }

    /**
     * Test case which tests if setting button up status influences the button down status.
     */
    @Test
    public void testSetButtonUpPressedAffectEachOther() {
        floor.setButtonUpPressed(true);
        assertFalse(floor.getButtonDownPressed());
        floor.setButtonDownPressed(true);
        floor.setButtonUpPressed(false);
        assertTrue(floor.getButtonDownPressed());
    }
}
