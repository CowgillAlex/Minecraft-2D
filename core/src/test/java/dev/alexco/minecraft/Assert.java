package dev.alexco.minecraft;

import java.util.Objects;

public class Assert {
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, "Expected true but was false");
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertFalse(boolean condition) {
        assertFalse(condition, "Expected false but was true");
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message +
                " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    public static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, "Values not equal");
    }

    public static void assertNotEquals(Object unexpected, Object actual, String message) {
        if (Objects.equals(unexpected, actual)) {
            throw new AssertionError(message +
                " - Both values were: " + actual);
        }
    }

    public static void assertNotEquals(Object unexpected, Object actual) {
        assertNotEquals(unexpected, actual, "Values should not be equal");
    }

    public static void assertNull(Object obj, String message) {
        if (obj != null) {
            throw new AssertionError(message + " - Expected null but was: " + obj);
        }
    }

    public static void assertNull(Object obj) {
        assertNull(obj, "Expected null");
    }

    public static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }

    public static void assertNotNull(Object obj) {
        assertNotNull(obj, "Expected non-null value");
    }

    public static void fail(String message) {
        throw new AssertionError(message);
    }

    public static void success() {
        // Explicitly mark test as successful - does nothing but makes intent clear
    }

    public static void success(String message) {
        // Explicitly mark test as successful with a message (for clarity in test code)
    }
}
