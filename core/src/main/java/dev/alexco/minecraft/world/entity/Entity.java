package dev.alexco.minecraft.world.entity;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.special.SolidityAABB;
import dev.alexco.minecraft.phys.AABB;
import dev.alexco.minecraft.phys.AABBPool;
import dev.alexco.minecraft.sound.SoundSystem;
import dev.alexco.minecraft.world.level.block.Solidity;
import dev.alexco.registry.ResourceLocation;

public class Entity {
    public String uuid;
    public ResourceLocation type;
    public double xo;
    public double yo;
    public double x;
    public double y;
    public double yd;
    public double xd;
    public double yRot;
    public AABB bb;
    public boolean onGround;
    public boolean removed;
    public float heightOffset;
    protected float bbWidth;
    public float bbHeight;
    public float stepHeight;
    public boolean noPhysics = false;
    private double distanceTravelledSinceLastStep = 0;

    public Entity(ResourceLocation type) {
        this.type = type;
        this.uuid = java.util.UUID.randomUUID().toString();
        resetPos();
    }

    protected void resetPos() {
    }

    protected void setSize(float w, float h) {
        this.bbWidth = w;
        this.bbHeight = h;
    }

    /**
     * Sets world position and rebuilds this entity's bounding box.
     */
    public void setPos(double x, double y) {
        this.x = x;
        this.y = y;
        double w = this.bbWidth;
        double h = this.bbHeight;

        this.bb = AABBPool.AABBpool.get(x - w / 2.0, y - this.heightOffset, x + w / 2.0, y - this.heightOffset + h);
    }

    public void turn(float xo) {
        this.yRot = (float) (this.yRot + xo * 0.15D);
    }

    /**
     * Advances previous-position tracking and step sound timing.
     */
    public void tick(float delta) {
        // Calculate distance travelled and play step sound if threshold reached
        if (this.onGround) {
            double dx = this.x - this.xo;
            double distanceTravelled = Math.sqrt(dx * dx);
            this.distanceTravelledSinceLastStep += distanceTravelled;

            // Play step sound every ~1 block of horizontal distance
            if (this.distanceTravelledSinceLastStep >= 1.0) {
                playStepSound();
                this.distanceTravelledSinceLastStep = 0;
            }
        }

        this.xo = this.x;
        this.yo = this.y;
    }

    /**
     * Returns true when this entity's box intersects water collision boxes.
     */
    public boolean amIinWater() {
        if (this.bb == null) {
            setPos(this.x, this.y);
        }
        List<SolidityAABB> cubes = Minecraft.getInstance().getWorld().getCubes(this.bb);
        boolean foundWater = false;

        for (SolidityAABB aabb : cubes) {
            if (!foundWater && aabb.solidity == Solidity.WATER) {
                if (aabb.boundingbox.intersects(this.bb)) {
                    foundWater = true;
                }
            }
            AABBPool.AABBpool.release(aabb.boundingbox);
        }
        return foundWater;
    }

    /**
     * Applies movement with collision clipping and step-up support.
     */
    public void move(double xa, double ya) {
        if (this.bb == null) {
            setPos(this.x, this.y);
        }
        if (noPhysics) {
            this.bb.move(0.0F, ya);

            this.bb.move(xa, 0.0F);
            this.x += xa;
            this.y += ya;
            return;
        }
        double xaOrg = xa;
        double yaOrg = ya;

        List<SolidityAABB> aABBs = Minecraft.getInstance().getWorld().getCubes(this.bb.expand(xa, ya));
        // we need to get rid of the ones that are not particularly solid
        aABBs = aABBs.stream()
                .filter(aabb -> aabb.solidity == Solidity.SOLID || aabb.solidity == Solidity.SCAFFOLD)
                .toList();
        int i;
        for (i = 0; i < aABBs.size(); i++) {
            ya = aABBs.get(i).boundingbox.clipYCollide(this.bb, ya);
        }
        this.bb.move(0.0F, ya);
        double originalXa = xa;
        for (i = 0; i < aABBs.size(); i++) {
            xa = aABBs.get(i).boundingbox.clipXCollide(this.bb, xa);
        }

        if (xa != originalXa && this.onGround && Math.abs(originalXa) > 1e-6) {
            double stepUpAmount = tryStepUp(originalXa, aABBs);
            if (stepUpAmount > 0) {
                this.bb.move(0.0F, stepUpAmount);
                xa = originalXa;
            }
        }

        this.bb.move(xa, 0.0F);

        this.onGround = false;
        double groundEpsilon = 0.02;
        if (yaOrg < 0) {
            AABB groundCheck = this.bb.expand(0, -groundEpsilon);
            List<SolidityAABB> groundBoxes = Minecraft.getInstance().getWorld().getCubes(groundCheck).stream()
                    .filter(aabb -> aabb.solidity == Solidity.SOLID || aabb.solidity == Solidity.SCAFFOLD)
                    .toList();

            for (SolidityAABB solid : groundBoxes) {
                if (solid.boundingbox.intersects(groundCheck)) {
                    this.onGround = true;

                    break;
                }
            }

            for (SolidityAABB aabb : groundBoxes) {
                AABBPool.AABBpool.release(aabb.boundingbox);
            }
            AABBPool.AABBpool.release(groundCheck);
        }
        if (xaOrg != xa) {
            this.xd = 0.0F;
        }
        if (yaOrg != ya) {
            this.yd = 0.0F;
        }

        this.x = (this.bb.x0 + this.bb.x1) / 2.0F;
        this.y = this.bb.y0 + this.heightOffset;

        for (SolidityAABB aabb : aABBs) {
            AABBPool.AABBpool.release(aabb.boundingbox);
        }
    }

    /**
     * Attempts to step up over a low obstacle during horizontal movement.
     */
    private double tryStepUp(double xa, List<SolidityAABB> aABBs) {
        // Create a temporary bounding box for testing step-up
        AABB testBB = AABBPool.AABBpool.get(this.bb.x0, this.bb.y0, this.bb.x1, this.bb.y1);

        try {
            // Test step heights from small increment up to maximum step height
            double testStep = 0.1; // Start with small step
            while (testStep <= this.stepHeight) {
                // Move test box up by test step amount
                testBB.set(this.bb.x0, this.bb.y0 + testStep, this.bb.x1, this.bb.y1 + testStep);

                // Check if we can move horizontally at this height
                double testXa = xa;
                boolean canMoveHorizontally = true;

                for (SolidityAABB aabb : aABBs) {
                    double clippedXa = aabb.boundingbox.clipXCollide(testBB, testXa);
                    if (Math.abs(clippedXa - testXa) > 1e-6) {
                        canMoveHorizontally = false;
                        break;
                    }
                }

                if (canMoveHorizontally) {
                    // Move the test box horizontally
                    testBB.move(xa, 0.0F);

                    // Check if there's ground to land on at this new position
                    // We need to check if we can move down from the stepped-up position
                    AABB landingTestBB = AABBPool.AABBpool.get(testBB.x0, testBB.y0 - testStep,
                            testBB.x1, testBB.y1 - testStep);
                    try {
                        boolean hasGroundSupport = false;

                        // Get collision boxes for the landing position
                        List<SolidityAABB> landingAABBs = Minecraft.getInstance().getWorld()
                                .getCubes(landingTestBB.expand(0, -0.1));
                        try {
                            for (SolidityAABB landingAABB : landingAABBs) {
                                if (landingAABB.boundingbox.intersects(landingTestBB)) {
                                    hasGroundSupport = true;
                                    break;
                                }
                            }
                        } finally {
                            for (SolidityAABB aabb : landingAABBs) {
                                AABBPool.AABBpool.release(aabb.boundingbox);
                            }
                        }

                        // If we have ground support or we're not stepping too high, allow the step
                        if (hasGroundSupport || testStep <= 0.1) {
                            return testStep;
                        }
                    } finally {
                        AABBPool.AABBpool.release(landingTestBB);

                    }
                }

                testStep += 0.1; // Increment step test height
            }

            return 0; // No valid step-up found
        } finally {
            AABBPool.AABBpool.release(testBB);

        }
    }

    /**
     * Converts local movement intent into velocity using current yaw.
     */
    public void moveRelative(float xa, float ya, float speed) {
        float dist = xa * xa + ya * ya;

        if (dist < 1e-7F) // Avoid issues with very small numbers
            return;
        dist = speed / (float) Math.sqrt(dist);

        xa *= dist;
        ya *= dist;

        float sin = (float) Math.sin(this.yRot * Math.PI / 180.0D);
        float cos = (float) Math.cos(this.yRot * Math.PI / 180.0D);

        this.xd += xa * cos - ya * sin;
        this.yd += ya * cos + xa * sin;
    }

    /**
     * Releases pooled collision resources for this entity.
     */
    public void destroy() {
        if (this.bb != null) {
            AABBPool.AABBpool.release(this.bb);
            this.bb = null;
        }
    }

    /**
     * Plays a random stone step sound variant.
     */
    public void playStepSound() {
        SoundSystem.playSound("minecraft:step.stone" + (MathUtils.random.nextInt(5) + 1));
    }

}
