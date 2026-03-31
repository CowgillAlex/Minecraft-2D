package dev.alexco.minecraft.world.entity.spawn;

public final class SpawnLightUtil {
    private SpawnLightUtil() {
    }

    /**
     * Returns day-night sky intensity in range [0, 1].
     */
    public static float skyIntensityAtTick(long ticks) {
        long dayTick = Math.floorMod(ticks, 24000L);
        if (dayTick < 2000L) {
            return 0.1f + 0.4f * (dayTick / 2000f);
        }
        if (dayTick < 6000L) {
            return 0.5f + 0.5f * ((dayTick - 2000L) / 4000f);
        }
        if (dayTick < 12000L) {
            return 1.0f;
        }
        if (dayTick < 14000L) {
            return 1.0f - 0.3f * ((dayTick - 12000L) / 2000f);
        }
        if (dayTick < 18000L) {
            return 0.7f - 0.6f * ((dayTick - 14000L) / 4000f);
        }
        return 0.1f;
    }

    /**
     * Converts raw sky light into time-adjusted effective sky light.
     */
    public static int effectiveSkyLight(int rawSkyLight, long ticks) {
        int clamped = Math.max(0, Math.min(15, rawSkyLight));
        int scaled = (int) Math.floor(clamped * skyIntensityAtTick(ticks) + 1.0e-6f);
        return Math.max(0, scaled - 1);
    }
}
