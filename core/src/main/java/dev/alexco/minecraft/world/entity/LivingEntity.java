package dev.alexco.minecraft.world.entity;

import dev.alexco.registry.ResourceLocation;

public class LivingEntity extends Entity {
    protected float maxHealth;
    protected float health;
    protected int hurtTicks = 0;
    protected int maxAirTicks = 300;
    protected int airTicks = 300;
    protected int submergedTicks = 0;
    protected int regenTickCounter = 0;
    protected int healthRegenIntervalTicks = 200;

    public LivingEntity(ResourceLocation type, float maxHealth) {
        super(type);
        this.maxHealth = Math.max(1.0f, maxHealth);
        this.health = this.maxHealth;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setHealth(float health) {
        this.health = Math.max(0.0f, Math.min(maxHealth, health));
        if (this.health <= 0.0f) {
            this.removed = true;
        }
    }

    public void hurt(float amount) {
        hurt(amount, null);
    }

    /**
     * Applies damage, invokes hooks and handles death.
     */
    public void hurt(float amount, Entity source) {
        if (amount <= 0.0f) {
            return;
        }
        this.hurtTicks = 8;
        onHurt(source, amount);
        float newHealth = this.health - amount;
        if (newHealth <= 0.0f) {
            this.health = 0.0f;
            onDeath(source);
            this.removed = true;
            return;
        }
        setHealth(newHealth);
    }

    public void heal(float amount) {
        if (amount <= 0.0f) {
            return;
        }
        setHealth(this.health + amount);
    }

    public int getHurtTicks() {
        return hurtTicks;
    }

    public int getMaxAirTicks() {
        return maxAirTicks;
    }

    public int getAirTicks() {
        return airTicks;
    }

    public int getSubmergedTicks() {
        return submergedTicks;
    }

    public int getHealthRegenIntervalTicks() {
        return healthRegenIntervalTicks;
    }

    protected void setHealthRegenIntervalTicks(int ticks) {
        this.healthRegenIntervalTicks = Math.max(1, ticks);
    }

    /**
     * Updates hurt, breathing and passive health regeneration timers.
     */
    protected void tickLivingState() {
        if (hurtTicks > 0) {
            hurtTicks--;
        }

        boolean inWater = amIinWater();
        if (inWater) {
            submergedTicks++;
            if (airTicks > 0) {
                airTicks--;
            }
        } else {
            submergedTicks = 0;
            airTicks = Math.min(maxAirTicks, airTicks + 2);
        }

        int regenInterval = Math.max(1, this.healthRegenIntervalTicks);
        if (health >= maxHealth || health <= 0.0f) {
            regenTickCounter = 0;
            return;
        }
        regenTickCounter++;
        if (regenTickCounter >= regenInterval) {
            regenTickCounter = 0;
            heal(1.0f);
        }
    }

    protected void onHurt(Entity source, float amount) {
    }

    protected void onDeath(Entity source) {
    }
}
