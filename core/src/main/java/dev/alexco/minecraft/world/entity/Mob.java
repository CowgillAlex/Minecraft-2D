package dev.alexco.minecraft.world.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.loot.LootTableManager;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.BlockItemEntity;
import dev.alexco.minecraft.world.entity.ItemEntity;
import dev.alexco.minecraft.world.entity.ai.AIGoal;
import dev.alexco.minecraft.world.level.item.BlockItem;
import dev.alexco.minecraft.world.level.item.Items;
import dev.alexco.registry.ResourceLocation;

public class Mob extends LivingEntity {
    protected final Random random = new Random();
    private final List<AIGoal> goals = new ArrayList<>();
    private float moveIntentX = 0.0f;
    private boolean jumpRequested = false;
    private int jumpCooldownTicks = 0;
    protected float moveAcceleration = 0.03f;
    protected float maxHorizontalSpeed = 0.06f;
    protected float jumpVelocity = 0.86f;

    public Mob(ResourceLocation type, float maxHealth) {
        super(type, maxHealth);
    }

    protected void addGoal(AIGoal goal) {
        goals.add(goal);
    }

    public void setMoveIntent(float moveIntentX) {
        this.moveIntentX = Math.max(-1.0f, Math.min(1.0f, moveIntentX));
    }

    public void requestJump() {
        this.jumpRequested = true;
    }

    public int randomInt(int bound) {
        return random.nextInt(bound);
    }

    public float randomFloat() {
        return random.nextFloat();
    }

    public boolean isHostile() {
        return false;
    }

    protected void setMovementTuning(float acceleration, float maxSpeed) {
        this.moveAcceleration = Math.max(0.005f, acceleration);
        this.maxHorizontalSpeed = Math.max(0.02f, maxSpeed);
    }

    protected void setJumpVelocity(float jumpVelocity) {
        this.jumpVelocity = Math.max(0.3f, jumpVelocity);
    }

    protected boolean softenCrowdSeparation() {
        return false;
    }

    protected ResourceLocation getLootTable() {
        return new ResourceLocation("minecraft", "entities/" + this.type.getPath());
    }

    /**
     * Ticks AI goals, movement, gravity and crowd separation.
     */
    @Override
    public void tick(float delta) {
        this.xo = this.x;
        this.yo = this.y;
        tickLivingState();

        World world = Minecraft.getInstance().getWorld();
        if (world == null || world.getChunkIfExists(World.getChunkX(this.x)) == null) {
            return;
        }

        this.moveIntentX = 0.0f;
        this.jumpRequested = false;
        if (jumpCooldownTicks > 0) {
            jumpCooldownTicks--;
        }
        for (AIGoal goal : goals) {
            goal.tick(this, world);
        }

        this.xd += this.moveIntentX * this.moveAcceleration;
        this.xd = Math.max(-this.maxHorizontalSpeed, Math.min(this.maxHorizontalSpeed, this.xd));

        // If we want to move but are barely translating, attempt a jump to clear blockers.
        if (this.onGround && Math.abs(this.moveIntentX) > 0.2f && Math.abs(this.xd) < 0.01f && jumpCooldownTicks <= 0) {
            this.jumpRequested = true;
        }

        if (this.onGround && this.jumpRequested && jumpCooldownTicks <= 0) {
            this.yd = Math.max(this.yd, this.jumpVelocity);
            this.xd += this.moveIntentX * 0.02f; // carry forward while jumping so they clear ledges
            this.xd = Math.max(-this.maxHorizontalSpeed * 1.2f, Math.min(this.maxHorizontalSpeed * 1.2f, this.xd));
            jumpCooldownTicks = 8;
        }

        if (!noPhysics) {
            if (amIinWater()) {
                this.yd -= 0.02f;
                if (this.yd < -0.1f) {
                    this.yd = -0.1f;
                }
            } else {
                this.yd -= 0.11f;
            }
        }
        this.yd *= 0.91f;

        move(this.xd, this.yd);
        applyEntitySeparation(world);

        this.xd *= this.onGround ? 0.7f : 0.91f;
    }

    /**
     * Applies horizontal separation from nearby living entities.
     */
    private void applyEntitySeparation(World world) {
        separateFromEntity(Minecraft.getInstance().getPlayer());
        for (Entity other : world.entities) {
            if (other == this || other.removed) {
                continue;
            }
            if (!(other instanceof LivingEntity)) {
                continue;
            }
            separateFromEntity(other);
        }
    }

    /**
     * Pushes this mob away when overlapping another entity.
     */
    private void separateFromEntity(Entity other) {
        if (other == null || other.bb == null || this.bb == null) {
            return;
        }
        boolean yOverlap = this.bb.y1 > other.bb.y0 && this.bb.y0 < other.bb.y1;
        if (!yOverlap) {
            return;
        }

        double dx = this.x - other.x;
        double separationFactor = (this.softenCrowdSeparation() || (other instanceof Mob mob && mob.softenCrowdSeparation()))
            ? 1.1
            : 1.3;
        double minDist = (this.bbWidth + other.bbWidth) * 0.5 * separationFactor;
        double absDx = Math.abs(dx);
        if (absDx >= minDist) {
            return;
        }

        double overlap = minDist - absDx;
        double dir = dx == 0.0 ? (this.random.nextBoolean() ? 1.0 : -1.0) : Math.signum(dx);
        double push = Math.min(0.09, overlap * 0.8);
        if (push <= 0.0) {
            return;
        }
        move(dir * push, 0.0);
    }

    /**
     * Spawns loot entities for this mob's loot table on death.
     */
    @Override
    protected void onDeath(Entity source) {
        ResourceLocation lootTable = getLootTable();
        if (lootTable == null) {
            return;
        }
        List<ItemStack> drops = LootTableManager.getDropsFromTable(lootTable, null, Items.AIR);
        World world = Minecraft.getInstance().getWorld();
        if (world == null) {
            return;
        }
        for (ItemStack drop : drops) {
            for (int i = 0; i < drop.amount; i++) {
                if (drop.item instanceof BlockItem blockItem) {
                    world.entities.add(new BlockItemEntity(blockItem, this.x, this.y));
                } else {
                    world.entities.add(new ItemEntity(drop.item, this.x, this.y));
                }
            }
        }
    }
}
