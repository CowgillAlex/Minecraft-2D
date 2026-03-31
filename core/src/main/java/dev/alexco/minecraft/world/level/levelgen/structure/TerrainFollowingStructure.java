package dev.alexco.minecraft.world.level.levelgen.structure;

import dev.alexco.minecraft.world.level.block.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension to Structure that supports terrain-following and chaining
 */
public class TerrainFollowingStructure extends Structure {
    private final List<ConnectionPoint> connectionPoints;
    private final Map<Integer, TerrainLayer> terrainLayers; // Y offset -> layer definition

    public TerrainFollowingStructure(String name, int width, int height) {
        super(name, width, height);
        this.connectionPoints = new ArrayList<>();
        this.terrainLayers = new HashMap<>();
    }

    /**
     * Add a connection point where other structures can attach
     */
    public TerrainFollowingStructure addConnectionPoint(int relX, int relY, ConnectionType type) {
        connectionPoints.add(new ConnectionPoint(relX, relY, type));
        return this;
    }

    /**
     * Define a layer that follows terrain at a specific Y offset
     * offset = 0 means surface level, -1 means one block below surface, etc.
     */
    public TerrainFollowingStructure addTerrainLayer(int yOffset, TerrainLayer layer) {
        terrainLayers.put(yOffset, layer);
        return this;
    }

    public List<ConnectionPoint> getConnectionPoints() {
        return connectionPoints;
    }

    public Map<Integer, TerrainLayer> getTerrainLayers() {
        return terrainLayers;
    }

    public boolean hasTerrainLayers() {
        return !terrainLayers.isEmpty();
    }

    /**
     * Connection point on a structure
     */
    public static class ConnectionPoint {
        public final int relX, relY;
        public final ConnectionType type;

        public ConnectionPoint(int relX, int relY, ConnectionType type) {
            this.relX = relX;
            this.relY = relY;
            this.type = type;
        }
    }

    /**
     * Types of connections
     */
    public enum ConnectionType {
        ROAD_HORIZONTAL,    // Connects left/right
        ROAD_VERTICAL,      // Connects forward/back (not used in 2D)
        DOOR_ENTRANCE,      // Building entrance
        DOOR_EXIT,          // Building exit
        TUNNEL_ENTRANCE,    // Goes underground
        BRIDGE_START,       // Starts a bridge
        BRIDGE_END          // Ends a bridge
    }

    /**
     * A layer of blocks that follows terrain height
     */
    public static class TerrainLayer {
        private final Map<Integer, BlockState> blocks; // X offset -> block state
        private final boolean replaceExisting;

        public TerrainLayer(boolean replaceExisting) {
            this.blocks = new HashMap<>();
            this.replaceExisting = replaceExisting;
        }

        /**
         * Add a block at X offset in this layer
         */
        public TerrainLayer addBlock(int xOffset, BlockState state) {
            blocks.put(xOffset, state);
            return this;
        }

        /**
         * Add blocks in a range
         */
        public TerrainLayer addBlocks(int startX, int endX, BlockState state) {
            for (int x = startX; x <= endX; x++) {
                blocks.put(x, state);
            }
            return this;
        }

        public Map<Integer, BlockState> getBlocks() {
            return blocks;
        }

        public boolean shouldReplaceExisting() {
            return replaceExisting;
        }
    }
}
