package at.fhhagenberg.sqelevator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the mqtt topics utility class
 */
public class MqttTopicsTest {
    /**
     * Test case which tests if instantiating class is illegal.
     */
    @Test
    public void testIllegalInstantiation() {
        Constructor<?>[] constructors = MqttTopics.class.getDeclaredConstructors();
        assertEquals(1, constructors.length, "Expected only one constructor");

        Constructor<?> constructor = constructors[0];
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");

        constructor.setAccessible(true); // Allow access to invoke the constructor

        InvocationTargetException thrown = assertThrows(
                InvocationTargetException.class,
                constructor::newInstance,
                "Expected InvocationTargetException when invoking private constructor"
        );

        assertEquals(IllegalStateException.class, thrown.getCause().getClass());
        assertEquals("Utility class", thrown.getCause().getMessage());
    }
}
