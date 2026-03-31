package dev.alexco.minecraft.world.serialisation;

import com.badlogic.gdx.files.FileHandle;
import dev.alexco.minecraft.SharedConstants;
import dev.alexco.minecraft.Version;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.biome.Biome;
import dev.alexco.minecraft.world.entity.BlockItemEntity;
import dev.alexco.minecraft.world.entity.Cow;
import dev.alexco.minecraft.world.entity.Entity;
import dev.alexco.minecraft.world.entity.ItemEntity;
import dev.alexco.minecraft.world.entity.LivingEntity;
import dev.alexco.minecraft.world.entity.Pig;
import dev.alexco.minecraft.world.entity.Zombie;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.block.entity.BlockEntity;
import dev.alexco.minecraft.world.level.block.entity.BarrelBlockEntity;
import dev.alexco.minecraft.world.level.block.entity.FurnaceBlockEntity;
import dev.alexco.minecraft.world.level.block.state.StateDefinition;
import dev.alexco.minecraft.world.level.block.state.properties.Property;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkPos;
import dev.alexco.minecraft.world.level.chunk.ChunkStatus;
import dev.alexco.minecraft.world.level.item.BlockItem;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.minecraft.world.level.item.Items;
import dev.alexco.registry.ResourceLocation;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.alexco.minecraft.SharedConstants.CHUNK_HEIGHT;
import static dev.alexco.minecraft.SharedConstants.CHUNK_WIDTH;

public class ChunkFile {
    /**
     * Saves a generated chunk to disk as NBT.
     */
    public static void writeChunkToDisk(Chunk chunk, String worldFolderName) {
        if (chunk.getStatus().ordinal() < ChunkStatus.NOISE.ordinal()) {
            return;
        }

        CompoundTag chunkTag = serialiseChunk(chunk);
        FileHandle chunkFile = WorldSaveManager.getChunkFile(worldFolderName, chunk.getChunkPos().x);

        try {
            chunkFile.parent().mkdirs();
            NBTUtil.write(new NamedTag(null, chunkTag), chunkFile.file());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chunk data: " + e.getMessage(), e);
        }
    }

    /**
     * Serialises chunk data and in-chunk world objects into an NBT tag.
     */
    public static CompoundTag serialiseChunk(Chunk chunk) {
        CompoundTag tag = new CompoundTag();

        tag.putString("DataVersion", Version.DATA_VERSION);
        tag.putInt("x", chunk.getChunkPos().x);
        tag.putString("Status", chunk.getStatus().name());

        ListTag<CompoundTag> blocksTag = new ListTag<>(CompoundTag.class);
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                BlockState state = chunk.getBlockAt(x, y);
                BlockState bgState = chunk.getBackgroundBlockAt(x, y);
                if (!state.getBlock().equals(Blocks.AIR) || !bgState.getBlock().equals(Blocks.AIR)) {
                    CompoundTag blockTag = new CompoundTag();
                    blockTag.putInt("x", x);
                    blockTag.putInt("y", y);
                    blockTag.putString("block", Registry.BLOCK.getKey(state.getBlock()).toString());
                    blockTag.putString("bgBlock", Registry.BLOCK.getKey(bgState.getBlock()).toString());

                    CompoundTag propertiesTag = serialiseBlockProperties(state);
                    if (propertiesTag != null) {
                        blockTag.put("properties", propertiesTag);
                    }

                    CompoundTag bgPropertiesTag = serialiseBlockProperties(bgState);
                    if (bgPropertiesTag != null) {
                        blockTag.put("bgProperties", bgPropertiesTag);
                    }

                    blocksTag.add(blockTag);
                }
            }
        }
        tag.put("blocks", blocksTag);

        tag.putByteArray("skyLight", chunk.getChunkData().getSkyLightArray());
        tag.putByteArray("blockLight", chunk.getChunkData().getBlockLightArray());

        if (chunk.isBiomeCachePopulated()) {
            ListTag<CompoundTag> biomesTag = new ListTag<>(CompoundTag.class);
            for (int x = 0; x < CHUNK_WIDTH; x++) {
                Biome biome = chunk.getBiome(x);
                if (biome != null) {
                    CompoundTag biomeTag = new CompoundTag();
                    biomeTag.putInt("x", x);
                    biomeTag.putString("biome", biome.getName().toString());
                    biomesTag.add(biomeTag);
                }
            }
            tag.put("biomes", biomesTag);
        }

        if (chunk.isSurfaceCachePopulated()) {
            int[] surfaceY = new int[CHUNK_WIDTH];
            for (int x = 0; x < CHUNK_WIDTH; x++) {
                surfaceY[x] = chunk.getSurfaceY(x);
            }
            tag.putIntArray("surfaceY", surfaceY);
        }

        ListTag<CompoundTag> blockEntitiesTag = new ListTag<>(CompoundTag.class);
        World world = dev.alexco.minecraft.Minecraft.getInstance().getSession().getWorld();
        if (world != null) {
            int chunkX = chunk.getChunkPos().x;

            for (BlockEntity be : world.getBlockEntityManager().getAllBlockEntities()) {
                int beChunkX = World.getChunkX(be.getX());
                if (beChunkX == chunkX) {
                    CompoundTag beTag = new CompoundTag();
                    beTag.putString("type", be.getTypeId());
                    beTag.putInt("x", be.getX());
                    beTag.putInt("y", be.getY());
                    be.saveToNBT(beTag);
                    blockEntitiesTag.add(beTag);
                }
            }
        }
        tag.put("blockEntities", blockEntitiesTag);

        ListTag<CompoundTag> entitiesTag = new ListTag<>(CompoundTag.class);
        if (world != null) {
            int chunkX = chunk.getChunkPos().x;
            for (Entity entity : world.entities) {
                if (entity.removed) continue;
                int entityChunkX = World.getChunkX(entity.x);
                if (entityChunkX == chunkX) {
                    CompoundTag entityTag = serialiseEntity(entity);
                    if (entityTag != null) {
                        entitiesTag.add(entityTag);
                    }
                }
            }
        }
        tag.put("entities", entitiesTag);

        return tag;
    }

    /**
     * Converts a runtime entity into its saved NBT form.
     */
    private static CompoundTag serialiseEntity(Entity entity) {
        CompoundTag tag = new CompoundTag();

        tag.putString("id", entity.type.toString());
        tag.putString("uuid", entity.uuid);
        tag.putDouble("x", entity.x);
        tag.putDouble("y", entity.y);
        tag.putDouble("xd", entity.xd);
        tag.putDouble("yd", entity.yd);
        tag.putBoolean("onGround", entity.onGround);

        if (entity instanceof BlockItemEntity blockItemEntity) {
            tag.putString("item", Registry.ITEM.getKey(blockItemEntity.item).toString());
            tag.putString("block", Registry.BLOCK.getKey(blockItemEntity.item.getBlock()).toString());
            CompoundTag propsTag = serialiseBlockProperties(blockItemEntity.item.getBlockState());
            if (propsTag != null) {
                tag.put("blockProperties", propsTag);
            }
        } else if (entity instanceof ItemEntity itemEntity) {
            tag.putString("item", Registry.ITEM.getKey(itemEntity.item).toString());
        } else if (entity instanceof Cow) {
            tag.putString("mob_type", "cow");
            Cow cow = (Cow) entity;
            tag.putBoolean("baby", cow.isBaby());
            tag.putInt("grow_up_ticks", cow.getGrowUpTicks());
            tag.putInt("love_ticks", cow.getLoveTicks());
            tag.putInt("breed_cooldown_ticks", cow.getBreedCooldownTicks());
        } else if (entity instanceof Pig) {
            tag.putString("mob_type", "pig");
            Pig pig = (Pig) entity;
            tag.putBoolean("baby", pig.isBaby());
            tag.putInt("grow_up_ticks", pig.getGrowUpTicks());
            tag.putInt("love_ticks", pig.getLoveTicks());
            tag.putInt("breed_cooldown_ticks", pig.getBreedCooldownTicks());
        } else if (entity instanceof Zombie) {
            tag.putString("mob_type", "zombie");
            tag.putBoolean("spawner_spawned", ((Zombie) entity).isSpawnerSpawned());
            tag.putBoolean("baby", ((Zombie) entity).isBaby());
        }

        if (entity instanceof LivingEntity livingEntity) {
            tag.putFloat("health", livingEntity.getHealth());
        }

        return tag;
    }

    /**
     * Serialises a block state's property map into string values.
     */
    private static <T extends Comparable<T>> CompoundTag serialiseBlockProperties(BlockState state) {
        Map<Property<?>, Comparable<?>> values = state.getValues();
        if (values.isEmpty()) {
            return null;
        }

        CompoundTag propertiesTag = new CompoundTag();
        for (Map.Entry<Property<?>, Comparable<?>> entry : values.entrySet()) {
            serialiseProperty(propertiesTag, entry.getKey(), entry.getValue());
        }
        return propertiesTag;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> void serialiseProperty(CompoundTag tag, Property<T> property, Comparable<?> value) {
        tag.putString(property.getName(), property.getName((T) value));
    }

    public static boolean chunkExists(String worldFolderName, int chunkX) {
        return WorldSaveManager.getChunkFile(worldFolderName, chunkX).exists();
    }

    public static CompoundTag readChunkData(String worldFolderName, int chunkX) {
        FileHandle chunkFile = WorldSaveManager.getChunkFile(worldFolderName, chunkX);

        if (!chunkFile.exists()) {
            return null;
        }

        try {
            NamedTag namedTag = NBTUtil.read(chunkFile.file());
            return (CompoundTag) namedTag.getTag();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Loads chunk blocks, caches, block entities and entities from NBT.
     */
    public static void deserialiseChunk(Chunk chunk, CompoundTag tag, World world) {
        String statusName = tag.getString("Status");
        try {
            chunk.setStatus(ChunkStatus.valueOf(statusName));
        } catch (IllegalArgumentException e) {
            chunk.setStatus(ChunkStatus.FULL);
        }

        if (tag.containsKey("blocks")) {
            ListTag<CompoundTag> blocksTag = tag.getListTag("blocks").asCompoundTagList();
            for (CompoundTag blockTag : blocksTag) {
                int x = blockTag.getInt("x");
                int y = blockTag.getInt("y");
                String blockKey = blockTag.getString("block");
                String bgBlockKey = blockTag.getString("bgBlock");

                Block block = Registry.BLOCK.get(new ResourceLocation(blockKey));
                if (block != null) {
                    BlockState state = block.defaultBlockState();
                    if (blockTag.containsKey("properties")) {
                        state = deserialiseBlockProperties(state, blockTag.getCompoundTag("properties"));
                    }
                    chunk.setBlockAt(x, y, state, false);
                }

                Block bgBlock = Registry.BLOCK.get(new ResourceLocation(bgBlockKey));
                if (bgBlock != null) {
                    BlockState bgState = bgBlock.defaultBlockState();
                    if (blockTag.containsKey("bgProperties")) {
                        bgState = deserialiseBlockProperties(bgState, blockTag.getCompoundTag("bgProperties"));
                    }
                    chunk.setBackgroundBlockAt(x, y, bgState);
                }
            }
        }

        if (tag.containsKey("skyLight")) {
            byte[] skyLight = tag.getByteArray("skyLight");
            chunk.getChunkData().setSkyLightArray(skyLight);
        }

        if (tag.containsKey("blockLight")) {
            byte[] blockLight = tag.getByteArray("blockLight");
            chunk.getChunkData().setBlockLightArray(blockLight);
        }

        if (tag.containsKey("biomes")) {
            ListTag<CompoundTag> biomesTag = tag.getListTag("biomes").asCompoundTagList();
            for (CompoundTag biomeTag : biomesTag) {
                int x = biomeTag.getInt("x");
                String biomeKey = biomeTag.getString("biome");
                Biome biome = Registry.BIOME.get(new ResourceLocation(biomeKey));
                if (biome != null) {
                    chunk.setBiome(x, biome);
                }
            }
            chunk.markBiomeCachePopulated();
        }

        if (tag.containsKey("surfaceY")) {
            int[] surfaceY = tag.getIntArray("surfaceY");
            for (int x = 0; x < CHUNK_WIDTH && x < surfaceY.length; x++) {
                chunk.setSurfaceY(x, surfaceY[x]);
            }
            chunk.markSurfaceCachePopulated();
        }

        if (tag.containsKey("blockEntities")) {
            ListTag<CompoundTag> blockEntitiesTag = tag.getListTag("blockEntities").asCompoundTagList();
            for (CompoundTag beTag : blockEntitiesTag) {
                String type = beTag.getString("type");
                int beX = beTag.getInt("x");
                int beY = beTag.getInt("y");

                BlockEntity blockEntity = null;
                if (FurnaceBlockEntity.TYPE_ID.equals(type)) {
                    blockEntity = new FurnaceBlockEntity(beX, beY);
                } else if (BarrelBlockEntity.TYPE_ID.equals(type)) {
                    blockEntity = new BarrelBlockEntity(beX, beY);
                }

                if (blockEntity != null) {
                    try {
                        blockEntity.loadFromNBT(beTag);
                    } catch (Exception e) {
                        Logger.ERROR("[ChunkFile] Failed to deserialise block entity at (%d, %d): %s", beX, beY, e.getMessage());
                        Logger.ERROR("[ChunkFile] Creating fresh block entity instead");
                        if (FurnaceBlockEntity.TYPE_ID.equals(type)) {
                            blockEntity = new FurnaceBlockEntity(beX, beY);
                        } else if (BarrelBlockEntity.TYPE_ID.equals(type)) {
                            blockEntity = new BarrelBlockEntity(beX, beY);
                        }
                    }
                    world.getBlockEntityManager().addBlockEntity(beX, beY, blockEntity);
                }
            }
        }

        if (tag.containsKey("entities")) {
            ListTag<CompoundTag> entitiesTag = tag.getListTag("entities").asCompoundTagList();
            for (CompoundTag entityTag : entitiesTag) {
                try {
                    Entity entity = deserialiseEntity(entityTag, world);
                    if (entity != null) {
                        boolean alreadyExists = world.entities.stream()
                            .anyMatch(e -> e.uuid.equals(entity.uuid));
                        if (!alreadyExists) {
                            chunk.addEntity(entity);
                            world.entities.add(entity);
                        }
                    }
                } catch (Exception e) {
                    Logger.ERROR("[ChunkFile] Failed to deserialise entity: %s", e.getMessage());
                }
            }
        }
    }

    /**
     * Turns a loaded entity tag into a runtime entity instance.
     */
    private static Entity deserialiseEntity(CompoundTag tag, World world) {
        String id = tag.getString("id");
        if (id == null || id.isEmpty()) return null;

        ResourceLocation entityType = new ResourceLocation(id);
        String itemKey = tag.getString("item");
        double x = tag.getDouble("x");
        double y = tag.getDouble("y");
        String uuid = tag.containsKey("uuid") ? tag.getString("uuid") : java.util.UUID.randomUUID().toString();
        double xd = tag.getDouble("xd");
        double yd = tag.getDouble("yd");
        boolean onGround = tag.getBoolean("onGround");

        if (tag.containsKey("block")) {
            String blockKey = tag.getString("block");
            Block block = Registry.BLOCK.get(new ResourceLocation(blockKey));
            if (block == null) return null;

            BlockState blockState = block.defaultBlockState();
            if (tag.containsKey("blockProperties")) {
                blockState = deserialiseBlockProperties(blockState, tag.getCompoundTag("blockProperties"));
            }

            BlockItem blockItem = new BlockItem(blockState, new Item.Properties());
            BlockItemEntity blockItemEntity = new BlockItemEntity(blockItem, x, y);
            blockItemEntity.uuid = uuid;
            blockItemEntity.xd = xd;
            blockItemEntity.yd = yd;
            blockItemEntity.onGround = onGround;
            blockItemEntity.setPos(x, y);
            return blockItemEntity;
        }

        if (entityType.toString().equals("item_entity") || entityType.toString().equals("minecraft:item_entity")) {
            Item item = Registry.ITEM.get(new ResourceLocation(itemKey));
            if (item == null) return null;

            ItemEntity itemEntity = new ItemEntity(item, x, y);
            itemEntity.uuid = uuid;
            itemEntity.xd = xd;
            itemEntity.yd = yd;
            itemEntity.onGround = onGround;
            itemEntity.setPos(x, y);
            return itemEntity;
        }

        if (entityType.toString().equals("minecraft:cow") || entityType.toString().equals("cow")) {
            boolean baby = tag.containsKey("baby") && tag.getBoolean("baby");
            Cow cow = new Cow(x, y, baby);
            cow.uuid = uuid;
            cow.xd = xd;
            cow.yd = yd;
            cow.onGround = onGround;
            if (tag.containsKey("health")) {
                cow.setHealth(tag.getFloat("health"));
            }
            if (tag.containsKey("grow_up_ticks")) {
                cow.setGrowUpTicks(tag.getInt("grow_up_ticks"));
            }
            if (tag.containsKey("love_ticks")) {
                cow.setLoveTicks(tag.getInt("love_ticks"));
            }
            if (tag.containsKey("breed_cooldown_ticks")) {
                cow.setBreedCooldownTicks(tag.getInt("breed_cooldown_ticks"));
            }
            cow.setPos(x, y);
            return cow;
        }
        if (entityType.toString().equals("minecraft:pig") || entityType.toString().equals("pig")) {
            boolean baby = tag.containsKey("baby") && tag.getBoolean("baby");
            Pig pig = new Pig(x, y, baby);
            pig.uuid = uuid;
            pig.xd = xd;
            pig.yd = yd;
            pig.onGround = onGround;
            if (tag.containsKey("health")) {
                pig.setHealth(tag.getFloat("health"));
            }
            if (tag.containsKey("grow_up_ticks")) {
                pig.setGrowUpTicks(tag.getInt("grow_up_ticks"));
            }
            if (tag.containsKey("love_ticks")) {
                pig.setLoveTicks(tag.getInt("love_ticks"));
            }
            if (tag.containsKey("breed_cooldown_ticks")) {
                pig.setBreedCooldownTicks(tag.getInt("breed_cooldown_ticks"));
            }
            pig.setPos(x, y);
            return pig;
        }
        if (entityType.toString().equals("minecraft:zombie") || entityType.toString().equals("zombie")) {
            boolean spawnerSpawned = tag.containsKey("spawner_spawned") && tag.getBoolean("spawner_spawned");
            boolean baby = tag.containsKey("baby") && tag.getBoolean("baby");
            Zombie zombie = new Zombie(x, y, spawnerSpawned, baby);
            zombie.uuid = uuid;
            zombie.xd = xd;
            zombie.yd = yd;
            zombie.onGround = onGround;
            if (tag.containsKey("health")) {
                zombie.setHealth(tag.getFloat("health"));
            }
            zombie.setPos(x, y);
            return zombie;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    /**
     * Applies saved block-state properties back onto a default state.
     */
    private static BlockState deserialiseBlockProperties(BlockState state, CompoundTag propertiesTag) {
        Block block = state.getBlock();
        StateDefinition<Block, BlockState> stateDef = block.getStateDefinition();

        for (String propertyName : propertiesTag.keySet()) {
            Property<?> property = stateDef.getProperty(propertyName);
            if (property == null) continue;

            String valueStr = propertiesTag.getString(propertyName);
            Optional<?> valueOpt = property.getValue(valueStr);
            if (valueOpt.isPresent()) {
                state = state.setValue((Property) property, (Comparable) valueOpt.get());
            }
        }

        return state;
    }
}
