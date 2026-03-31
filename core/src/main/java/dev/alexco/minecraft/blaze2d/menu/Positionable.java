package dev.alexco.minecraft.blaze2d.menu;


public interface Positionable {
    void setPosition(float x, float y);
    void setSize(float width, float height);
    float getX();
    float getY();
    float getWidth();
    float getHeight();
}
