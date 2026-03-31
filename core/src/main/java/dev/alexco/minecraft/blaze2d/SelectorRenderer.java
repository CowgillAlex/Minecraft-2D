package dev.alexco.minecraft.blaze2d;

import static dev.alexco.minecraft.SharedConstants.CHUNK_HEIGHT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.input.InputHandler;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.item.ToolItem;

public class SelectorRenderer extends RenderableLifecycle {
    public static int worldMouseX;
    public static int worldMouseY;

    @Override
    public void create() {
        super.create("Selector", Color.GRAY);
    }

    /**
     * Updates hovered world block coordinates and renders selector/break-stage overlays.
     */
    @Override
    public void render() {
        if (Minecraft.getInstance().isScreenOpen()) return;

        double blockSize = Minecraft.getInstance().getWorld().worldData.blockSize;
        double cX = Minecraft.getInstance().getWorld().worldData.cameraX;
        double cY = Minecraft.getInstance().getWorld().worldData.cameraY;

        double mouseX = Gdx.input.getX();
        double mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        double worldX = mouseX + cX;
        double worldY = mouseY + cY;

        // Calculate BEFORE the bounds check
        worldMouseX = (int) Math.floor(worldX / blockSize);
        worldMouseY = (int) Math.floor(worldY / blockSize);

        // NOW check bounds with FRESH values
        if (worldMouseY < 0 || worldMouseY > CHUNK_HEIGHT) return;

        double xToRender = Math.floor(worldX / blockSize) * blockSize - cX;
        double yToRender = Math.floor(worldY / blockSize) * blockSize - cY;
        float brightness = RenderableEntity.getLightBrightnessAt(worldMouseX, worldMouseY);

        spriteBatch.begin();
        if (InputHandler.isButtonDown(Buttons.LEFT) && !Player.blockStateUnderPlayerHand.getBlock().equals(Blocks.AIR)) {
            // ?for how logn have we held the mouse
            int len = InputHandler.mouseHeldDownFor;
            boolean canHarvest = ToolItem.canHarvest(Player.getPlayer().blockInHand, Player.blockStateUnderPlayerHand.getBlock());
            float toolSpeed = ToolItem.getMiningSpeed(Player.getPlayer().blockInHand, Player.blockStateUnderPlayerHand.getBlock());
            double outOf = Player.blockStateUnderPlayerHand.getBlock().calculateMiningTime(
                    canHarvest,
                    toolSpeed,
                    0,
                    false,
                    0,
                    false,
                    0,
                    false,
                    false,
                    Player.getPlayer().onGround
            );
            if (outOf <= 0) {
                outOf = 1;
            }
            float progress = (float) len / (float) outOf;
            float scaled = progress * 10;
            float stage = (float) Math.floor(scaled);
            int clamped = (int) Math.clamp(stage, 0, 9);

            String destrNum = clamped + ""; // between 0 and 9
            if (! BlockTags.UNSELECTABLE.contains(Player.blockStateUnderPlayerHand.getBlock())) {
                if (SelectorRenderer.worldMouseY<0 || SelectorRenderer.worldMouseY>CHUNK_HEIGHT||(Minecraft.getInstance().isScreenOpen())){
                }else{
                    spriteBatch.setColor(brightness, brightness, brightness, 1.0f);
                    spriteBatch.draw(
                        Minecraft.getInstance().textureManager
                        .get("textures/destroy/destroy_stage_" + destrNum + ".png"),
                        (float) xToRender, (float) yToRender,
                        (float) blockSize, (float) blockSize);
                    }
                }

        }
        spriteBatch.setColor(brightness, brightness, brightness, 1.0f);
        spriteBatch.draw(
                Minecraft.getInstance().textureManager.get("textures/block/block_selector.png"),
                (float) xToRender, (float) yToRender,
                (float) blockSize, (float) blockSize);
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.end();
    }

}
