package dev.alexco.minecraft.blaze2d.menu;

public interface GridElement extends Positionable {
    void render();
    void update();
    void setEnabled(boolean enabled);
    boolean isEnabled();
}
