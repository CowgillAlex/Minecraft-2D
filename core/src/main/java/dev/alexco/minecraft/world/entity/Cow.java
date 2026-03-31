package dev.alexco.minecraft.world.entity;

import com.badlogic.gdx.math.MathUtils;

import dev.alexco.minecraft.sound.SoundSystem;
import dev.alexco.minecraft.world.entity.ai.CowPanicGoal;
import dev.alexco.minecraft.world.entity.ai.CowTemptGoal;
import dev.alexco.minecraft.world.entity.ai.WanderGoal;
import dev.alexco.registry.ResourceLocation;

public class Cow extends Mob {
    public static final ResourceLocation TYPE = new ResourceLocation("minecraft", "cow");
    private static final float ADULT_WIDTH = 0.95f;
    private static final float ADULT_HEIGHT = 0.85f;
    private static final float BABY_SCALE = 0.45f;
    private int panicTicks = 0;
    private float panicDirection = 0f;
    private boolean baby;
    private int growUpTicks;
    private int loveTicks = 0;
    private int breedCooldownTicks = 0;
    private int temptedTicks = 0;

    public Cow(double x, double y) {
        this(x, y, false);
    }

    public Cow(double x, double y, boolean baby) {
        super(TYPE, 10.0f);
        this.baby = baby;
        this.growUpTicks = baby ? 20 * 60 * 3 : 0;
        applyAgeScale();
        this.stepHeight = 0.0f;
        this.heightOffset = 0.0f;
        setMovementTuning(0.018f, 0.04f);
        setJumpVelocity(0.8f);
        setHealthRegenIntervalTicks(120);
        this.setPos(x, y);
        addGoal(new WanderGoal(7));
        addGoal(new CowPanicGoal());
        addGoal(new CowTemptGoal(12.0f));
    }

    public int getPanicTicks() {
        return panicTicks;
    }

    public float getPanicDirection() {
        return panicDirection;
    }

    public void decrementPanic() {
        panicTicks = Math.max(0, panicTicks - 1);
    }

    public void markTempted() {
        temptedTicks = 2;
    }

    public boolean isTempted() {
        return temptedTicks > 0;
    }

    public boolean isBaby() {
        return baby;
    }

    public int getGrowUpTicks() {
        return growUpTicks;
    }

    public int getLoveTicks() {
        return loveTicks;
    }

    public int getBreedCooldownTicks() {
        return breedCooldownTicks;
    }

    public void setGrowUpTicks(int ticks) {
        this.growUpTicks = Math.max(0, ticks);
        if (this.growUpTicks <= 0 && this.baby) {
            this.baby = false;
            applyAgeScale();
        }
    }

    public void setLoveTicks(int ticks) {
        this.loveTicks = Math.max(0, ticks);
    }

    public void setBreedCooldownTicks(int ticks) {
        this.breedCooldownTicks = Math.max(0, ticks);
    }

    public boolean canBreed() {
        return !baby && growUpTicks <= 0 && breedCooldownTicks <= 0;
    }

    public boolean feedForBreeding() {
        if (!canBreed()) {
            return false;
        }
        loveTicks = 20 * 15;
        return true;
    }

    public void onBreedComplete() {
        loveTicks = 0;
        breedCooldownTicks = 20 * 30;
    }

    /**
     * Ticks growth, breeding state and movement tuning.
     */
    @Override
    public void tick(float delta) {
        if (growUpTicks > 0) {
            growUpTicks--;
            if (growUpTicks == 0 && baby) {
                baby = false;
                applyAgeScale();
            }
        }
        if (loveTicks > 0) {
            loveTicks--;
        }
        if (breedCooldownTicks > 0) {
            breedCooldownTicks--;
        }
        if (temptedTicks > 0) {
            temptedTicks--;
        }

        if (panicTicks > 0) {
            setMovementTuning(0.045f, 0.09f);
        } else if (isTempted()) {
            setMovementTuning(0.028f, 0.06f);
        } else {
            setMovementTuning(0.018f, 0.04f);
        }

        super.tick(delta);

        if (amIinWater()) {
            this.yd += 0.04f;
            if (this.yd > 0.12f) {
                this.yd = 0.12f;
            }
        }
    }

    /**
     * Starts panic movement away from the damage source.
     */
    @Override
    protected void onHurt(Entity source, float amount) {
        if (source == null) {
            return;
        }
                    SoundSystem.playSound("mob.cow.hurt" + (MathUtils.random.nextInt(2) + 1));

        panicTicks = 100;
        panicDirection = (float) Math.signum(this.x - source.x);
        if (panicDirection == 0.0f) {
            panicDirection = this.randomFloat() < 0.5f ? -1.0f : 1.0f;
        }
    }
 @Override
    protected void onDeath(Entity source) {

        super.onDeath(source);
                    SoundSystem.playSound("mob.cow.hurt" + (MathUtils.random.nextInt(2) + 1));
    }

    /**
     * Applies adult or baby collision scale to the cow.
     */
    private void applyAgeScale() {
        float scale = baby ? BABY_SCALE : 1.0f;
        this.bbWidth = ADULT_WIDTH * scale;
        this.bbHeight = ADULT_HEIGHT * scale;
        if (this.bb != null) {
            this.setPos(this.x, this.y);
        }
    }

}
