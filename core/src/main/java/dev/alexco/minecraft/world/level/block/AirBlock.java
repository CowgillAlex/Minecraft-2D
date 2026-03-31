package dev.alexco.minecraft.world.level.block;

public class AirBlock extends Block {


    public AirBlock(Properties properties) {
        super(properties);

    }
    @Override
    public boolean isAir() {
        return true;
    }
}
