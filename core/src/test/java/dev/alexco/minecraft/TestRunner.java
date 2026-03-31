package dev.alexco.minecraft;

import java.util.ArrayList;
import java.util.List;

public class TestRunner {
    private final List<TestCase> tests = new ArrayList<>();
    private int passed = 0;
    private int failed = 0;


    /**
     * Register a test case to be run
     */
    public void addTest(TestCase test) {
        tests.add(test);
    }

 public void runAll() {
        System.out.println("=== Running Tests ===\n");

        long totalStartTime = System.nanoTime();

        for (TestCase test : tests) {
            long testStartTime = System.nanoTime();

            try {
                test.run();
                long testDuration = System.nanoTime() - testStartTime;
                passed++;
                System.out.println("✓PASS: " + test.getName() +
                    " (" + formatDuration(testDuration) + ")");
            } catch (AssertionError e) {
                long testDuration = System.nanoTime() - testStartTime;
                failed++;
                System.out.println("✗ FAIL: " + test.getName() +
                    " (" + formatDuration(testDuration) + ")");
                System.out.println("  Reason: " + e.getMessage());
            } catch (Exception e) {
                long testDuration = System.nanoTime() - testStartTime;
                failed++;
                System.out.println("✗ ERROR: " + test.getName() +
                    " (" + formatDuration(testDuration) + ")");
                System.out.println("  Exception: " + e.getMessage());
            }
        }

        long totalDuration = System.nanoTime() - totalStartTime;

        printSummary(totalDuration);
    }
    private void printSummary(long durationNanos) {
        System.out.println("\n=== Test Summary ===");
        System.out.println("Total:  " + (passed + failed));
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Success Rate: " +
            String.format("%.1f%%", (passed * 100.0) / (passed + failed)));
        System.out.println("Duration: " + formatDuration(durationNanos));
    }

    /**
     * Format duration in appropriate units based on magnitude
     */
    private String formatDuration(long nanos) {
        if (nanos < 1_000) {
            return nanos + " ns";
        } else if (nanos < 1_000_000) {
            return String.format("%.2f ns", nanos / 1_000.0);
        } else if (nanos < 1_000_000_000) {
            return String.format("%.2f ms", nanos / 1_000_000.0);
        } else if (nanos < 60_000_000_000L) {
            return String.format("%.2f s", nanos / 1_000_000_000.0);
        } else if (nanos < 3_600_000_000_000L) {
            long seconds = nanos / 1_000_000_000L;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%d:%02d", minutes, seconds);
        } else {
            long seconds = nanos / 1_000_000_000L;
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            seconds = seconds % 60;
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
    }
}

