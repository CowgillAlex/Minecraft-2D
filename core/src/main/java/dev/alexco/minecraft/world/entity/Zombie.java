package dev.alexco.minecraft.world.entity;

import com.badlogic.gdx.math.MathUtils;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.RenderableEntity;
import dev.alexco.minecraft.sound.SoundLoader;
import dev.alexco.minecraft.sound.SoundSystem;
import dev.alexco.minecraft.world.entity.ai.FollowPlayerGoal;
import dev.alexco.minecraft.world.entity.ai.WanderGoal;
import dev.alexco.registry.ResourceLocation;

public class Zombie extends Mob {
    public static final ResourceLocation TYPE = new ResourceLocation("minecraft", "zombie");
    private static final float ADULT_WIDTH = 0.8f;
    private static final float ADULT_HEIGHT = 1.8f;
    private static final float BABY_SCALE = 0.55f;
    private int attackCooldown = 0;
    private int burnTickCounter = 0;
    private final boolean spawnerSpawned;
    private boolean baby;

    public Zombie(double x, double y) {
        this(x, y, false, false);
    }

    public Zombie(double x, double y, boolean spawnerSpawned) {
        this(x, y, spawnerSpawned, false);
    }

    public Zombie(double x, double y, boolean spawnerSpawned, boolean baby) {
        super(TYPE, 20.0f);
        this.spawnerSpawned = spawnerSpawned;
        this.baby = baby;
        applyAgeScale();
        this.stepHeight = 0.0f;
        this.heightOffset = 0.0f;
        setMovementTuning(0.022f, 0.05f);
        setJumpVelocity(0.9f);
        setHealthRegenIntervalTicks(220);
        this.setPos(x, y);
        addGoal(new WanderGoal(5));
        addGoal(new FollowPlayerGoal(16.0f));
    }

    @Override
    public boolean isHostile() {
        return true;
    }

    public boolean isSpawnerSpawned() {
        return spawnerSpawned;
    }

    public boolean isBaby() {
        return baby;
    }

    /**
     * Ticks zombie AI, sunburn and melee attack cooldown.
     */
    @Override
    public void tick(float delta) {
        super.tick(delta);

        tickSunburn();

        if (attackCooldown > 0) {
            attackCooldown--;
        }
        Player player = Minecraft.getInstance().getPlayer();
        if (player == null || attackCooldown > 0) {
            return;
        }

        double dx = player.x - this.x;
        double dy = player.y - this.y;
        double distSq = dx * dx + dy * dy;
        if (distSq <= 1.6 * 1.6) {
            float kb = (float) Math.signum(dx) * 0.22f;
            if (kb == 0.0f) {
                kb = this.randomFloat() < 0.5f ? -0.22f : 0.22f;
            }
            player.xd += kb;
            player.yd += 0.08f;
            attackCooldown = 40;
                SoundSystem.playSound("damage.hit"+ (MathUtils.random.nextInt(2) + 1));


        }
    }

    /**
     * Applies adult or baby collision scale to the zombie.
     */
    private void applyAgeScale() {
        float scale = baby ? BABY_SCALE : 1.0f;
        this.bbWidth = ADULT_WIDTH * scale;
        this.bbHeight = ADULT_HEIGHT * scale;
    }

    /**
     * Applies daytime burn damage when exposed to full skylight.
     */
    private void tickSunburn() {
        long gameTime = Minecraft.getInstance().getSession().getTimer().totalTicks % 24000;
        if (gameTime < 5000 || gameTime > 18000) {
            burnTickCounter = 0;
            return;
        }

        int skyLight = RenderableEntity.getSkyLight(this);
        if (skyLight < 15) {
            burnTickCounter = 0;
            return;
        }

        burnTickCounter++;
        if (burnTickCounter >= 20) {
            burnTickCounter = 0;
            hurt(2.0f);
            //2 of them
            SoundSystem.playSound("mob.zombie.hurt"+ (1 + (int)(Math.random() * 2)));
        }

    }
    @Override
    protected void onHurt(Entity source, float amount) {
        super.onHurt(source, amount);
        SoundSystem.playSound("mob.zombie.hurt"+ (1 + (int)(Math.random() * 2)));

    }
    @Override
    protected void onDeath(Entity source) {
        super.onDeath(source);
        SoundSystem.playSound("mob.zombie.death");

    }
}
