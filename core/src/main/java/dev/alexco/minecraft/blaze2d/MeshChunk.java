package dev.alexco.minecraft.blaze2d;

import java.util.ArrayList;
import java.util.List;
import static dev.alexco.minecraft.SharedConstants.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector2;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.block.DoorBlock;
import dev.alexco.minecraft.world.level.block.OakSlabBlock;
import dev.alexco.minecraft.world.level.block.OakStairsBlock;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.util.Direction;
import dev.alexco.minecraft.world.level.chunk.Chunk;

public class MeshChunk {
    private float[] vertexBuffer; //216kb per 16*384 chunk
    private short[] indexBuffer;
    private int vertexCount;
    private int indexCount;
    private short vertIndex;
    public Mesh mesh;
    private Chunk chunkRef;
    private BlockTextureAtlas atlas;
    public int chunkPos;

    private static final int FLOATS_PER_VERTEX = 11;
    private static final float BACKGROUND_BLOCK_LIGHT_FACTOR = 0.25f;
    private final int maxVertices;
    private volatile boolean disposed;

    public MeshChunk(Chunk chunkRef, BlockTextureAtlas atlas) {
        this.chunkRef = chunkRef;
        this.chunkPos = chunkRef.getPos().x;
        this.atlas = atlas;

        int maxBlocks = CHUNK_WIDTH * CHUNK_HEIGHT;
        this.maxVertices = maxBlocks * 4;
        vertexBuffer = new float[maxVertices * FLOATS_PER_VERTEX];
        indexBuffer = new short[maxBlocks * 6];
        this.disposed = false;
    }

    /**
     * Reads sky and block light for one local block and packs them into one byte pair.
     */
    private int getPackedLight(int x, int y) {
        if (x < 0 || x >= CHUNK_WIDTH || y < 0 || y >= CHUNK_HEIGHT) {
            return (15 << 4) | 0;
        }
        int skyLight = Math.min(15, chunkRef.getSkyLightAt(x, y) & 0xFF);
        int blockLight = Math.min(15, chunkRef.getBlockLightAt(x, y) & 0xFF);
        return (skyLight << 4) | blockLight;
    }

    /**
     * Scales only block-light contribution while preserving sky-light.
     */
    private int scalePackedBlockLight(int packedLight, float factor) {
        int skyLight = (packedLight >> 4) & 0xF;
        int blockLight = packedLight & 0xF;
        int scaledBlock = 0;
        if (blockLight > 0) {
            scaledBlock = Math.max(1, Math.min(15, (int) Math.ceil(blockLight * factor)));
        }
        return (skyLight << 4) | scaledBlock;
    }

    /**
     * Rebuilds this chunk mesh from current block and background block state.
     */
    public synchronized void build() {
        if (disposed || chunkRef.isUnloaded()) {
            return;
        }
        if (vertexBuffer == null){


        int maxBlocks = CHUNK_WIDTH * CHUNK_HEIGHT;
        vertexBuffer = new float[maxVertices * FLOATS_PER_VERTEX];
        indexBuffer = new short[maxBlocks * 6];

        }
        vertexCount = 0;
        indexCount = 0;
        vertIndex = 0;
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                BlockState bgBlockState = chunkRef.getBackgroundBlockAt(x, y);
                BlockState blockState = chunkRef.getBlockAt(x, y);
                int packedLight = getPackedLight(x, y);
                int bgPackedLight = scalePackedBlockLight(packedLight, BACKGROUND_BLOCK_LIGHT_FACTOR);

                if (!bgBlockState.getBlock().equals(Blocks.AIR)) {
                    addBlockToBuffer(x, y, bgBlockState, Color.DARK_GRAY, bgPackedLight);
                }
                if (!blockState.getBlock().equals(Blocks.AIR)) {

                    if (BlockTags.LEAVES.contains(blockState.getBlock())) {
                        addBlockToBuffer(x, y, blockState, Color.GREEN, packedLight);
                        continue;
                    }
                    if (blockState.getBlock().equals(Blocks.WATER)) {
                        addBlockToBuffer(x, y, blockState, Color.CYAN, packedLight);
                        continue;
                    }

                    addBlockToBuffer(x, y, blockState, Color.WHITE, packedLight);
                }
            }
        }
        if (mesh != null) {
            try {
                mesh.dispose();
            } catch (IllegalArgumentException ignored) {
            }
            mesh = null;
        }

        int numVertices = vertexCount / FLOATS_PER_VERTEX;

        mesh = new Mesh(true, numVertices, indexBuffer.length,
                new VertexAttribute(Usage.Position, 3, "a_position"),
                new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"),
                new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"),
                new VertexAttribute(Usage.Generic, 2, "a_light"));
        mesh.setVertices(vertexBuffer, 0, vertexCount);
        mesh.setIndices(indexBuffer, 0, indexCount);
        // Logger.INFO("Built Mesh for chunk %d with %d/%d vertices (%d floats)",
        //         chunkRef.getPos().x, numVertices, maxVertices, vertexCount);
    }


    /**
     * Queues a block quad with the same tint on all corners.
     */
    private void addBlockToBuffer(float x, float y, BlockState blockState, Color color, int packedLight) {
        addBlockToBuffer(x, y, blockState, color, color, color, color, packedLight);
    }

    /**
     * Queues geometry for one block, delegating to special-shape builders when needed.
     */
    private void addBlockToBuffer(float x, float y, BlockState blockState,
            Color bottomLeft, Color bottomRight,
            Color topRight, Color topLeft, int packedLight) {
        Block block = blockState.getBlock();
        if (block instanceof DoorBlock) {
            addDoorToBuffer(x, y, blockState, bottomLeft, bottomRight, topRight, topLeft, packedLight);
            return;
        }
        if (block instanceof OakSlabBlock) {
            addSlabToBuffer(x, y, blockState, bottomLeft, bottomRight, topRight, topLeft, packedLight);
            return;
        }
        if (block instanceof OakStairsBlock) {
            addStairsToBuffer(x, y, blockState, bottomLeft, bottomRight, topRight, topLeft, packedLight);
            return;
        }

        Vector2 uv = atlas.getUV(blockState);
        float uvSize = atlas.getUVBlockSize();

        Direction facing = Direction.UP;
        if (blockState.hasProperty(BlockStateProperties.FACING)) {
            facing = blockState.getValue(BlockStateProperties.FACING);
        }

        Vector2[] uvCoords = new Vector2[4];
        uvCoords[0] = new Vector2(uv.x, uv.y + uvSize);
        uvCoords[1] = new Vector2(uv.x + uvSize, uv.y + uvSize);
        uvCoords[2] = new Vector2(uv.x + uvSize, uv.y);
        uvCoords[3] = new Vector2(uv.x, uv.y);

        uvCoords = rotateUVs(uvCoords, facing);

        addVertex(x, y, uvCoords[0].x, uvCoords[0].y, bottomLeft, packedLight);
        addVertex(x + 1f, y, uvCoords[1].x, uvCoords[1].y, bottomRight, packedLight);
        addVertex(x + 1f, y + 1f, uvCoords[2].x, uvCoords[2].y, topRight, packedLight);
        addVertex(x, y + 1f, uvCoords[3].x, uvCoords[3].y, topLeft, packedLight);

        addQuadIndices();
    }

    /**
     * Builds a slim or open door quad from block state properties.
     */
    private void addDoorToBuffer(float x, float y, BlockState blockState,
            Color bottomLeft, Color bottomRight, Color topRight, Color topLeft, int packedLight) {
        Vector2 uv = atlas.getUV(blockState);
        float uvSize = atlas.getUVBlockSize();
        boolean open = blockState.getValue(BlockStateProperties.OPEN);

        float minX = x;
        float maxX = open ? x + 1.0f : x + 0.125f;
        float minY = y;
        float maxY = y + 1.0f;

        float minU = uv.x;
        float maxU = open ? uv.x + uvSize : uv.x + (uvSize * 0.125f);
        float minV = uv.y;
        float maxV = uv.y + uvSize;

        addVertex(minX, minY, minU, maxV, bottomLeft, packedLight);
        addVertex(maxX, minY, maxU, maxV, bottomRight, packedLight);
        addVertex(maxX, maxY, maxU, minV, topRight, packedLight);
        addVertex(minX, maxY, minU, minV, topLeft, packedLight);
        addQuadIndices();
    }

    /**
     * Builds top or bottom slab geometry using half-height texture mapping.
     */
    private void addSlabToBuffer(float x, float y, BlockState blockState,
            Color bottomLeft, Color bottomRight, Color topRight, Color topLeft, int packedLight) {
        Vector2 uv = atlas.getUV(blockState);
        float uvSize = atlas.getUVBlockSize();
        boolean top = blockState.getValue(BlockStateProperties.TOP);

        float minX = x;
        float maxX = x + 1.0f;
        float minY = top ? y + 0.5f : y;
        float maxY = top ? y + 1.0f : y + 0.5f;

        float minU = uv.x;
        float maxU = uv.x + uvSize;
        float minV = top ? uv.y + (uvSize * 0.5f) : uv.y;
        float maxV = top ? uv.y + uvSize : uv.y + (uvSize * 0.5f);

        addVertex(minX, minY, minU, maxV, bottomLeft, packedLight);
        addVertex(maxX, minY, maxU, maxV, bottomRight, packedLight);
        addVertex(maxX, maxY, maxU, minV, topRight, packedLight);
        addVertex(minX, maxY, minU, minV, topLeft, packedLight);
        addQuadIndices();
    }

    /**
     * Builds stair geometry as two quads based on top/bottom and facing state.
     */
    private void addStairsToBuffer(float x, float y, BlockState blockState,
            Color bottomLeft, Color bottomRight, Color topRight, Color topLeft, int packedLight) {
        boolean top = blockState.getValue(BlockStateProperties.TOP);
        Direction facing = blockState.getValue(BlockStateProperties.FACING);
        Vector2 uv = atlas.getUV(blockState);
        float uvSize = atlas.getUVBlockSize();

        if (!top) {
            addQuadWithUv(
                x, y, x + 1.0f, y + 0.5f,
                uv.x, uv.y + (uvSize * 0.5f), uv.x + uvSize, uv.y + uvSize,
                bottomLeft, bottomRight, topRight, topLeft, packedLight
            );
            if (facing == Direction.LEFT) {
                addQuadWithUv(
                    x, y + 0.5f, x + 0.5f, y + 1.0f,
                    uv.x, uv.y, uv.x + (uvSize * 0.5f), uv.y + (uvSize * 0.5f),
                    bottomLeft, bottomRight, topRight, topLeft, packedLight
                );
            } else {
                addQuadWithUv(
                    x + 0.5f, y + 0.5f, x + 1.0f, y + 1.0f,
                    uv.x + (uvSize * 0.5f), uv.y, uv.x + uvSize, uv.y + (uvSize * 0.5f),
                    bottomLeft, bottomRight, topRight, topLeft, packedLight
                );
            }
            return;
        }

        addQuadWithUv(
            x, y + 0.5f, x + 1.0f, y + 1.0f,
            uv.x, uv.y, uv.x + uvSize, uv.y + (uvSize * 0.5f),
            bottomLeft, bottomRight, topRight, topLeft, packedLight
        );
        if (facing == Direction.LEFT) {
            addQuadWithUv(
                x, y, x + 0.5f, y + 0.5f,
                uv.x, uv.y + (uvSize * 0.5f), uv.x + (uvSize * 0.5f), uv.y + uvSize,
                bottomLeft, bottomRight, topRight, topLeft, packedLight
            );
        } else {
            addQuadWithUv(
                x + 0.5f, y, x + 1.0f, y + 0.5f,
                uv.x + (uvSize * 0.5f), uv.y + (uvSize * 0.5f), uv.x + uvSize, uv.y + uvSize,
                bottomLeft, bottomRight, topRight, topLeft, packedLight
            );
        }
    }

    /**
     * Adds one textured quad rectangle to the mesh buffers.
     */
    private void addQuadWithUv(float minX, float minY, float maxX, float maxY,
            float minU, float minV, float maxU, float maxV,
            Color bottomLeft, Color bottomRight, Color topRight, Color topLeft, int packedLight) {
        addVertex(minX, minY, minU, maxV, bottomLeft, packedLight);
        addVertex(maxX, minY, maxU, maxV, bottomRight, packedLight);
        addVertex(maxX, maxY, maxU, minV, topRight, packedLight);
        addVertex(minX, maxY, minU, minV, topLeft, packedLight);
        addQuadIndices();
    }

    /**
     * Rotates block-face uv coordinates to match the block facing direction.
     */
    private Vector2[] rotateUVs(Vector2[] uvCoords, Direction facing) {
        Vector2[] rotated = new Vector2[4];

        switch (facing) {
            case UP:

                return uvCoords;
            case RIGHT:

                rotated[0] = uvCoords[3]; // Top Left -> Bottom Left
                rotated[1] = uvCoords[0]; // Bottom Left -> Bottom Right
                rotated[2] = uvCoords[1]; // Bottom Right -> Top Right
                rotated[3] = uvCoords[2]; // Top Right -> Top Left
                return rotated;
            case DOWN:

                rotated[0] = uvCoords[2]; // Top Right -> Bottom Left
                rotated[1] = uvCoords[3]; // Top Left -> Bottom Right
                rotated[2] = uvCoords[0]; // Bottom Left -> Top Right
                rotated[3] = uvCoords[1]; // Bottom Right -> Top Left
                return rotated;
            case LEFT:

                rotated[0] = uvCoords[1]; // Bottom Right -> Bottom Left
                rotated[1] = uvCoords[2]; // Top Right -> Bottom Right
                rotated[2] = uvCoords[3]; // Top Left -> Top Right
                rotated[3] = uvCoords[0]; // Bottom Left -> Top Left
                return rotated;
            default:
                return uvCoords;
        }
    }

    /**
     * Appends one vertex with position, uv, tint and packed light channels.
     */
    private void addVertex(float x, float y, float u, float v, Color color, int packedLight) {
        int skyLight = (packedLight >> 4) & 0xF;
        int blockLight = packedLight & 0xF;

        vertexBuffer[vertexCount++] = x;
        vertexBuffer[vertexCount++] = y;
        vertexBuffer[vertexCount++] = 0f;
        vertexBuffer[vertexCount++] = u;
        vertexBuffer[vertexCount++] = v;
        vertexBuffer[vertexCount++] = color.r;
        vertexBuffer[vertexCount++] = color.g;
        vertexBuffer[vertexCount++] = color.b;
        vertexBuffer[vertexCount++] = color.a;
        vertexBuffer[vertexCount++] = skyLight / 15.0f;
        vertexBuffer[vertexCount++] = blockLight / 15.0f;
    }

    /**
     * Adds two triangles for the latest four queued vertices.
     */
    private void addQuadIndices() {
        indexBuffer[indexCount++] = vertIndex;
        indexBuffer[indexCount++] = (short) (vertIndex + 1);
        indexBuffer[indexCount++] = (short) (vertIndex + 2);
        indexBuffer[indexCount++] = (short) (vertIndex + 2);
        indexBuffer[indexCount++] = (short) (vertIndex + 3);
        indexBuffer[indexCount++] = vertIndex;
        vertIndex += 4;
    }

    /**
     * Releases gpu mesh and backing buffers for this chunk mesh.
     */
    public synchronized void dispose() {
        disposed = true;
        if (mesh != null) {
            try {
                mesh.dispose();
            } catch (IllegalArgumentException ignored) {
            }
            mesh = null;
        }
        vertexBuffer = null;
        indexBuffer = null;
    }
}
