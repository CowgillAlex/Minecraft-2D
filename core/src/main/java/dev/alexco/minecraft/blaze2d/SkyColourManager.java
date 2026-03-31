package dev.alexco.minecraft.blaze2d;

import com.badlogic.gdx.graphics.Color;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.biome.Biome;

public class SkyColourManager {
      private static final Color NIGHT_COLOR = new Color(0.05f, 0.05f, 0.1f, 1f);
    private static final Color DAWN_DUSK_COLOR = new Color(0.95f, 0.55f, 0.35f, 1f);
    private static final Color DAY_COLOR = new Color(0.6f, 0.8f, 1f, 1f);
    private static final Color SUNSET_COLOR = new Color(0.9f, 0.4f, 0.2f, 1f);


      private static final Color tempColor = new Color();


            /**
             * Computes base sky colour from biome tint and current day-night phase.
             */
      public static Color getSkyColor(float multiplier, Biome biome) {
        float ticks = (float) Math.floor(Minecraft.getInstance().getTotalTicks()) % 24000;

        if (biome == null) {
            return DAY_COLOR;
        }
        if (ticks < 6000) {
            if (ticks < 2000) {
                return lerpColors(NIGHT_COLOR, DAWN_DUSK_COLOR, ticks / 2000f);
            } else {
                return lerpColors(DAWN_DUSK_COLOR, biome.getSkyColour(), (ticks - 2000) / 4000f);
            }
        }

        else if (ticks < 12000) {
            return biome.getSkyColour().cpy();
        }

        else if (ticks < 18000) {
            if (ticks < 14000) {
                // day to sunset
                return lerpColors(biome.getSkyColour(), SUNSET_COLOR, (ticks - 12000) / 2000f);
            } else {
                // sunset to night
                return lerpColors(SUNSET_COLOR, NIGHT_COLOR, (ticks - 14000) / 4000f);
            }
        }

        else {
            return NIGHT_COLOR.cpy();
        }
    }


            /**
             * Linearly blends two colours into a reusable temporary colour instance.
             */
      private static Color lerpColors(Color c1, Color c2, float alpha) {
        alpha = Math.max(0, Math.min(1, alpha));
        tempColor.set(
                c1.r + (c2.r - c1.r) * alpha,
                c1.g + (c2.g - c1.g) * alpha,
                c1.b + (c2.b - c1.b) * alpha,
                1f);
        return tempColor;
    }
}
