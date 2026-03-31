package dev.alexco.minecraft;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents the current state of the loading process. This is used to
 * communicate between the loading thread and the main thread, allowing the
 * loading screen to display the current step and detail of the loading process.
 */
public class LoadingState {
    private final AtomicReference<String> currentStep = new AtomicReference<>("Initialising...");
    private final AtomicReference<String> currentDetail = new AtomicReference<>("");
    private final AtomicBoolean complete = new AtomicBoolean(false);

    public void setStep(String step) {
        currentStep.set(step);
    }

    public void setDetail(String detail) {
        currentDetail.set(detail);
    }

    public void setStep(String step, String detail) {

        currentStep.set(step);
        currentDetail.set(detail);
    }

    public String getStep() {
        return currentStep.get();
    }

    public String getDetail() {
        return currentDetail.get();
    }

    public void markComplete() {
        complete.set(true);
    }

    public boolean isComplete() {
        return complete.get();
    }

    public void reset() {
        currentStep.set("Initialising...");
        currentDetail.set("");
        complete.set(false);
    }
}
