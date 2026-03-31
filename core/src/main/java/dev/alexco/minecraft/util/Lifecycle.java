package dev.alexco.minecraft.util;

public interface Lifecycle {
    public void create();
    public default void render(){};
    public default void resize(int width, int height){};
    public void destroy();
}
