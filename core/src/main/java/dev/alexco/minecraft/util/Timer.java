package dev.alexco.minecraft.util;

public class Timer {
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final long MAX_NANOS_PER_UPDATE = NANOS_PER_SECOND;
    private static final int MAX_TICKS_PER_UPDATE = 100;
    private static final int SKIP_THRESHOLD = 60;

    private final float ticksPerSecond;

    private long lastTimeNano;
    private float tickAccumulator = 0.0F;

    private boolean paused = false;
    private long pausedAtNano = 0L;
    private long totalPausedTime = 0L;

    public int elapsedTicks;
    public float partialTickProgress;
    public float timeScale = 1.0F;
    public float fps = 0.0F;

    public long totalTicks = 0L;

    private int tickCounter = 0;
    private long lastTpsUpdateTime = System.nanoTime();
    private float currentTps = 0.0F;

    public Timer(float ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
        this.lastTimeNano = System.nanoTime();
        this.lastTpsUpdateTime = this.lastTimeNano;
    }


    public Timer(float ticksPerSecond, long startTick) {
        this.ticksPerSecond = ticksPerSecond;
        this.totalTicks = startTick;
        this.lastTimeNano = System.nanoTime();
        this.lastTpsUpdateTime = this.lastTimeNano;
    }

    /**
     * Pauses tick progression while preserving timing state.
     */

    public void pause() {
        if (!paused) {
            paused = true;
            pausedAtNano = System.nanoTime();
        }
    }

    /**
     * Resumes ticking and accounts for elapsed paused time.
     */

    public void resume() {
        if (paused) {
            long currentTime = System.nanoTime();
            totalPausedTime += (currentTime - pausedAtNano);
            lastTimeNano = currentTime;
            lastTpsUpdateTime = currentTime;
            paused = false;
            pausedAtNano = 0L;
        }
    }


    public boolean isPaused() {
        return paused;
    }

    /**
     * Resets timer accumulators and sets an explicit world start tick.
     */
    public void setStartTick(long tick) {
        this.totalTicks = tick;
        this.tickAccumulator = 0.0F;
        this.elapsedTicks = 0;
        this.partialTickProgress = 0.0F;
        this.tickCounter = 0;
        this.totalPausedTime = 0L;

        long currentTime = System.nanoTime();
        this.lastTimeNano = currentTime;
        this.lastTpsUpdateTime = currentTime;

        if (paused) {
            this.pausedAtNano = currentTime;
        }
    }

    public long getTotalPausedTimeNanos() {
        long totalPaused = totalPausedTime;
        if (paused) {
            totalPaused += (System.nanoTime() - pausedAtNano);
        }
        return totalPaused;
    }


    public float getTotalPausedTimeSeconds() {
        return getTotalPausedTimeNanos() / (float) NANOS_PER_SECOND;
    }

    /**
     * Advances timing state and computes elapsed simulation ticks.
     */
    public void advanceTime() {

        if (paused) {
            elapsedTicks = 0;
            partialTickProgress = tickAccumulator;
            fps = 0.0F;
            return;
        }

        long currentTimeNano = System.nanoTime();
        long deltaNano = currentTimeNano - lastTimeNano;
        lastTimeNano = currentTimeNano;

        if (deltaNano > MAX_NANOS_PER_UPDATE) {
            deltaNano = MAX_NANOS_PER_UPDATE;
        }

        this.fps = (float) NANOS_PER_SECOND / deltaNano;

        float deltaSeconds = deltaNano / (float) NANOS_PER_SECOND;
        tickAccumulator += deltaSeconds * timeScale * ticksPerSecond;

        elapsedTicks = (int) tickAccumulator;

        if (elapsedTicks > SKIP_THRESHOLD) {
            Logger.INFO("Game behind by %s ticks, skipping to present", elapsedTicks);
            elapsedTicks = 1;
            tickAccumulator = 0.0F;
        } else if (elapsedTicks > MAX_TICKS_PER_UPDATE) {
            elapsedTicks = MAX_TICKS_PER_UPDATE;
        }

        totalTicks += elapsedTicks;
        tickAccumulator -= elapsedTicks;
        partialTickProgress = tickAccumulator;

        tickCounter += elapsedTicks;
        if (currentTimeNano - lastTpsUpdateTime >= NANOS_PER_SECOND) {
            currentTps = tickCounter * (NANOS_PER_SECOND / (float) (currentTimeNano - lastTpsUpdateTime));
            tickCounter = 0;
            lastTpsUpdateTime = currentTimeNano;
        }
    }

    public float getWorldAgeAsSeconds() {
        return totalTicks / ticksPerSecond;
    }

    public int getTicksPerSecond() {
        return Math.round(currentTps);
    }
}
