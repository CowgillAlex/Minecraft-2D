package dev.alexco.minecraft.world.level.block;

import com.badlogic.gdx.graphics.Color;

public enum Solidity {
    AIR(Color.GRAY),
    WATER(Color.BLUE),
    SCAFFOLD(Color.RED),
    SOLID(Color.CYAN);

    public final Color debugColor;

    private Solidity(Color color){
        this.debugColor = color;
    }
}
