package dev.alexco.minecraft;

public abstract class TestCase {
    private final String name;

    public TestCase(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Override to set up test state before execution
     */
    protected void setup() throws Exception {
        // Optional: override in subclass
    }

    /**
     * Override to define the test logic
     */
    protected abstract void test() throws Exception;

    /**
     * Override to clean up after test execution
     */
    protected void teardown() throws Exception {
        // Optional: override in subclass
    }

    /**
     * Execute the complete test lifecycle
     */
    final void run() throws Exception {
        try {
            setup();
            test();
        } finally {
            teardown();
        }
    }
}
