package dev.alexco.minecraft.input;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.World;

public class InputHandler extends InputAdapter {
    private static final Set<Integer> keyboardCurrentlyPressed = new HashSet<>();
    private static final Set<Integer> keyboardJustPressed = new HashSet<>();
    private static final Set<Integer> mouseCurrentlyPressed = new HashSet<>();
    private static final Set<Integer> mouseJustPressed = new HashSet<>();
    private static int scrollDeltaY = 0;
    public static int mouseHeldDownFor = 0;
    public InputHandler() {
        Gdx.input.setInputProcessor(this);
    }

    /**
     * Captures the latest vertical scroll delta for one-tick consumption.
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        scrollDeltaY = (int) amountY; // store vertical scroll delta
        return true; // mark event as handled
    }

    /**
     * Returns and clears the stored scroll amount from the most recent scroll event.
     */
    public static int getScrollDeltaY() {
        int delta = scrollDeltaY;
        scrollDeltaY = 0; // reset after reading
        return delta;
    }

    /**
     * Applies per-tick debug zoom key handling and lower bound clamping.
     */
    public void tick() {
        World world = Minecraft.getInstance().getWorld();

        if (Gdx.input.isKeyPressed(Keys.EQUALS)) {
            world.worldData.blockSize += 0.5f;
        }
        if (Gdx.input.isKeyPressed(Keys.MINUS)) {
            world.worldData.blockSize -= 0.5f;
        }
        if (world.worldData.blockSize <= 8f) {
            world.worldData.blockSize = 8f;
        }

    }

    /**
     * Refreshes pressed/just-pressed keyboard and mouse state for this frame.
     */
    public static void update() {
        // update keyboard
        keyboardJustPressed.clear();
        mouseJustPressed.clear();
        for (int key = 0; key < 256; key++) {
            boolean isPressed = Gdx.input.isKeyPressed(key);

            if (isPressed && !keyboardCurrentlyPressed.contains(key)) {
                keyboardJustPressed.add(key);
            }

            if (isPressed) {
                keyboardCurrentlyPressed.add(key);
            } else {
                keyboardCurrentlyPressed.remove(key);
            }
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            mouseHeldDownFor++;
        } else{
            mouseHeldDownFor=0;
        }
        // update mouse
        for (int button = 0; button <= 7; button++) {
            boolean isPressed = Gdx.input.isButtonPressed(button);

            if (isPressed && !mouseCurrentlyPressed.contains(button)) {
                mouseJustPressed.add(button);
            }
            if (isPressed) {
                mouseCurrentlyPressed.add(button);
            } else {
                mouseCurrentlyPressed.remove(button);
            }
        }
    }

    /**
     * Returns true if the key was just pressed this tick.
     */
    public static boolean isKeyJustPressed(int key) {
        return keyboardJustPressed.contains(key);
    }

    /**
     * Returns true if the key is currently held down.
     */
    public static boolean isKeyDown(int key) {
        return keyboardCurrentlyPressed.contains(key);
    }

    /**
     * Returns true if the mouse button was just pressed this tick.
     */
    public static boolean isButtonJustPressed(int button) {
        return mouseJustPressed.contains(button);
    }

    /**
     * Returns true if the mouse button is currently held down.
     */
    public static boolean isButtonDown(int button) {
        return mouseCurrentlyPressed.contains(button);
    }

}
