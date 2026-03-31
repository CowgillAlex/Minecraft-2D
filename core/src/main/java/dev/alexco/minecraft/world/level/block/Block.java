package dev.alexco.minecraft.world.level.block;

import java.util.Objects;

import javax.annotation.Nullable;

import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.blaze2d.special.SolidityAABB;
import dev.alexco.minecraft.phys.AABB;
import dev.alexco.minecraft.phys.AABBPool;
import dev.alexco.registry.ResourceLocation;
import dev.alexco.registry.IdMapper;
import dev.alexco.minecraft.util.Util;
import dev.alexco.minecraft.world.level.block.state.StateDefinition;
import dev.alexco.minecraft.world.level.material.Material;
import dev.alexco.minecraft.world.level.material.MaterialColor;
import java.util.List;

public class Block {
    public static final IdMapper<BlockState> BLOCK_STATE_REGISTRY = new IdMapper<BlockState>();

    protected final float destroySpeed;
    protected final float explosionResistance;
    protected final boolean hasCollision;
    protected final StateDefinition<Block, BlockState> stateDefinition;
    protected final Material material;
    protected final MaterialColor materialColor;
    protected final Solidity solidity;
    protected final boolean changesStateForBackground;
    protected final int lightEmission;
    protected final int lightFilter;
    private BlockState defaultBlockState;
    private BlockState defaultBackgroundBlockState;
    @Nullable
    private String descriptionId;
    @Nullable
    private ResourceLocation drops;

    public Block(Properties properties) {
        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<Block, BlockState>(this);
        this.createBlockStateDefinition(builder);
        this.destroySpeed = properties.destroyTime;
        this.explosionResistance = properties.explosionResistance;
        this.hasCollision = properties.hasCollision;
        this.material = properties.material;
        this.materialColor = properties.materialColor;
        this.drops = properties.drops;
        this.solidity = properties.solidity;
        this.stateDefinition = builder.create(BlockState::new);
        this.changesStateForBackground = properties.changesStateForBackground;
        this.lightEmission = properties.lightEmission;
        this.lightFilter = properties.lightFilter;
        this.registerDefaultState(this.stateDefinition.any());
        this.defaultBackgroundBlockState = properties.backgroundBlockState != null
                ? properties.backgroundBlockState
                : this.defaultBlockState;

    }

    public String getDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("block", Registry.BLOCK.getKey(this));
        }
        return this.descriptionId;
    }

    public final BlockState defaultBlockState() {
        return this.defaultBlockState;
    }

    public final BlockState defaultBackgroundBlockState() {
        return this.defaultBackgroundBlockState;
    }

    @Deprecated
    public Material getMaterial(BlockState blockState) {
        return this.material;
    }

    protected final void registerDefaultState(BlockState blockState) {
        this.defaultBlockState = blockState;
    }

    public Solidity getSolidity() {
        return solidity;
    }
    public boolean doIChangeStateForBg(){
        return changesStateForBackground;
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }

    public StateDefinition<Block, BlockState> getStateDefinition() {
        return this.stateDefinition;
    }

    /**
     * Resolves the block loot table location, caching the result.
     */
    public ResourceLocation getLootTable() {
        if (this.drops == null) {
            ResourceLocation resourceLocation = Registry.BLOCK.getKey(this);
            this.drops = new ResourceLocation(resourceLocation.getNamespace(), "blocks/" + resourceLocation.getPath());
        }
        return this.drops;
    }

    public void setCollisionShape(BlockState blockState, int x, int y, AABB aabb) {
        aabb.set(x, y, x + 1.0, y + 1.0);
    }

    /**
     * Adds this block's collision shape to the output list.
     */
    public void addCollisionBoxes(BlockState blockState, int x, int y, List<SolidityAABB> out) {
        AABB aabb = AABBPool.AABBpool.get(0, 0, 0, 0);
        setCollisionShape(blockState, x, y, aabb);
        if (aabb.x1 > aabb.x0 && aabb.y1 > aabb.y0) {
            out.add(SolidityAABB.create(aabb, getSolidity()));
        } else {
            AABBPool.AABBpool.release(aabb);
        }
    }

    public boolean isAir() {
        return false;
    }

    public int getLightEmission() {
        return lightEmission;
    }

    public int getLightEmission(BlockState blockState) {
        return lightEmission;
    }

    public int getLightFilter() {
        return lightFilter;
    }

    public ResourceLocation getKey() {
        return Registry.BLOCK.getKey(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Block) {
            Block object = (Block) obj;
            if (object == this) {
                return true;
            }
            if (object.descriptionId != null && this.descriptionId != null
                    && object.descriptionId.equals(this.descriptionId)) {
                return true;
            }
            if (object.getKey() != null && this.getKey() != null
                    && object.getKey().equals(this.getKey())) {
                return true;
            }
            if (object.defaultBlockState != null && this.defaultBlockState != null
                    && object.defaultBlockState.equals(this.defaultBlockState)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes mining time in ticks from tool and status modifiers.
     */
    public double calculateMiningTime(boolean canHarvest, float toolSpeed, int efficiencyLevel, boolean hasHaste,
            int hasteLevel, boolean hasMiningFatigue, int fatigueLevel, boolean isInWater, boolean hasAquaAffinity,
            boolean onGround) {
        float speed = toolSpeed;
        if (speed > 1.0f && efficiencyLevel > 0) {
            speed += (efficiencyLevel * efficiencyLevel + 1);
        }
        if (hasHaste) {
            speed *= 1.0f + (hasteLevel + 1) * 0.2f;
        }
        if (hasMiningFatigue) {
            float fatigueMultiplier;
            switch (fatigueLevel) {
                case 0 -> fatigueMultiplier = 0.3f;
                case 1 -> fatigueMultiplier = 0.09f;
                case 2 -> fatigueMultiplier = 0.0027f;
                default -> fatigueMultiplier = 0.00081f;
            }
            speed *= fatigueMultiplier;
        }
        if (isInWater && !hasAquaAffinity) {
            speed /= 5.0f;
        }

        if (!onGround) {
            speed /= 5.0f;
        }
        if (destroySpeed < 0.0f) {
            return -1; //we cannot break the block
        }
        float damagePerTick = speed / destroySpeed;
        damagePerTick /= (canHarvest ? 30.0f : 100.0f);
        if (damagePerTick >= 1.0f) {
            return 0; //insta mine
        }
        return Math.ceil(1.0f / damagePerTick);
    }

    public static class Properties {
        private float destroyTime;
        private float explosionResistance;
        private boolean hasCollision = true;
        private Material material;
        private MaterialColor materialColor;
        private ResourceLocation drops;
        private BlockState backgroundBlockState;
        private Solidity solidity = Solidity.SOLID;;
        private boolean changesStateForBackground = true;
        private int lightEmission = 0;
        private int lightFilter = 3;

        private Properties(Material material, MaterialColor materialColor) {
            this.material = material;
            this.materialColor = materialColor;
        }

        protected Properties noDrops() {
            this.drops = null;// BuiltInLootTables.EMPTY;
            return this;
        }

        public Properties dropsLike(Block block) {
            this.drops = block.getLootTable();
            return this;
        }

        public static Properties of(Material material) {
            return Properties.of(material, material.getColor());
        }

        public static Properties of(Material material, MaterialColor materialColor) {
            return new Properties(material, materialColor);
        }

        public static Properties copy(Block block) {
            Properties properties = new Properties(block.material, block.materialColor);
            properties.material = block.material;
            properties.destroyTime = block.destroySpeed;
            properties.materialColor = block.materialColor;
            properties.lightFilter = block.lightFilter;
            return properties;
        }

        public Properties noCollission() {
            this.hasCollision = false;
            return this;
        }

        public Properties solidity(Solidity solidness) {
            this.solidity = solidness;
            return this;
        }

        public Properties strength(float hardness, float blast_resistance) {
            this.destroyTime = hardness;
            this.explosionResistance = Math.max(0.0f, blast_resistance);
            return this;
        }

        protected Properties instabreak() {
            return this.strength(0.0f);
        }

        public Properties backgroundBlockState(BlockState background) {

            this.backgroundBlockState = background;
            return this;
        }
        public Properties dontChangeBgStateOnBreak()
        {
            this.changesStateForBackground = false;
            return this;
        }
        protected Properties strength(float f) {
            this.strength(f, f);
            return this;
        }

        public Properties lightEmission(int level) {
            this.lightEmission = Math.max(0, Math.min(15, level));
            return this;
        }

        public Properties filtersBy(int value) {
            this.lightFilter = Math.max(0, Math.min(15, value));
            return this;
        }

    }
}
