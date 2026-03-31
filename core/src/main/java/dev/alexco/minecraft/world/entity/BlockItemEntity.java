package dev.alexco.minecraft.world.entity;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.level.item.BlockItem;

public class BlockItemEntity extends ItemEntity {
        public BlockItem item;

   public BlockItemEntity(BlockItem item, double initX, double initY) {
    super(item, initX, initY);
        this.bbWidth = 0.65f;
        this.bbHeight = 0.65f;
        this.setPos(initX, initY);
        this.item = item;
        this.xd = (Math.random()-0.5f)*0.1f;
        this.yd = (Math.random())*0.2f;

    }

    /**
     * Ticks dropped block-item motion with gravity and fluid handling.
     */
    @Override
    public void tick(float delta) {
        this.xo = x;
        this.yo = y;

        if (Minecraft.getInstance().getWorld() == null ||
            Minecraft.getInstance().getWorld().getChunkIfExists(World.getChunkX(this.x)) == null) {
            return;
        }
           float xa = 0.0F;
        float ya = 0.0F;
        moveRelative(xa, ya, 0.02f);

        this.yd *= 0.91F;

        move(this.xd, this.yd);
        if (this.onGround) {
            // this.yd += 1;
            ya = 0;
        }
        this.xd *= 0.91F;
        if (!noPhysics) {

            if (amIinWater()) {

                this.yd -= 0.02f;
                //stop themfrom going too fast
                if (this.yd < -0.1f) {
                    this.yd = -0.1f;
                }
            } else {
                this.yd -= 0.157f;
            }
        }
    }
}
