package dev.alexco.minecraft.blaze2d;
import static dev.alexco.minecraft.SharedConstants.CHUNK_WIDTH;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkStatus;

public class MeshChunkRenderer extends RenderableLifecycle {
    ShaderProgram shader;
    Texture tex;

    @Override
    public void create() {
        super.create("ChunkRenderer", Color.LIME);
        ShaderProgram.pedantic = false;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Minecraft.getInstance().textureManager.forceLoadTexture("textures.png");
        tex = Minecraft.getInstance().textureManager.get("textures.png");
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        String vertexShader = "attribute vec3 a_position;\n" +
                "attribute vec2 a_texCoord0;\n" +
                "attribute vec4 a_color;\n" +
                "attribute vec2 a_light;\n" +
                "uniform vec2 u_cameraOffset;\n" +
                "uniform vec2 u_screenSize;\n" +
                "uniform float u_zoom;\n" +
                "uniform vec2 u_chunkOffset;\n" +
                "varying vec2 v_texCoord;\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_light;\n" +
                "\n" +
                "void main() {\n" +
                "    v_texCoord = a_texCoord0;\n" +
                "    v_color = a_color;\n" +
                "    v_light = a_light;\n" +
                "    vec2 blockPos = a_position.xy * 16.0;\n" +
                "    vec2 worldPos = (blockPos * u_zoom) + u_chunkOffset;\n" +
                "    vec2 pos = worldPos - u_cameraOffset;\n" +
                "    vec2 screenPos = (pos / u_screenSize) * 2.0 - 1.0;\n" +
                "    gl_Position = vec4(screenPos, 0.0, 1.0);\n" +
                "}";

        String fragmentShader = "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "\n" +
                "varying vec2 v_texCoord;\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_light;\n" +
                "uniform sampler2D u_texture;\n" +
                "uniform float u_skyIntensity;\n" +
                "uniform float u_forceBlockLight;\n" +
                "\n" +
                "void main() {\n" +
                "    vec4 texColor = texture2D(u_texture, v_texCoord);\n" +
                "    if (texColor.a < 0.01) discard;\n" +
                "    \n" +
                "    float skyLight = v_light.x;\n" +
                "    float blockLight = u_forceBlockLight > 0.5 ? 1.0 : v_light.y;\n" +
                "    float brightness = max(blockLight, skyLight * u_skyIntensity);\n" +
                "    \n" +
                "    gl_FragColor = vec4(texColor.rgb * v_color.rgb * brightness, texColor.a);\n" +
                "}\n";

        shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) {
            Gdx.app.error("Shader", "Compilation failed:\n" + shader.getLog());
        }
    }

    @Override
    public void render() {
        shader.bind();

        float cameraX = (float) Minecraft.getInstance().getWorld().worldData.cameraX;
        float cameraY = (float) Minecraft.getInstance().getWorld().worldData.cameraY;
        float zoom = (Minecraft.getInstance().getWorld().worldData.blockSize) / 16f;

        shader.setUniformi("u_texture", 0);
        shader.setUniformf("u_cameraOffset", cameraX, cameraY);
        shader.setUniformf("u_screenSize", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shader.setUniformf("u_zoom", zoom);
        shader.setUniformf("u_skyIntensity", getSkyIntensity());
        shader.setUniformf("u_forceBlockLight", Gdx.input.isKeyPressed(Keys.G) ? 1.0f : 0.0f);

        tex.bind();

        for (Chunk chunk : Minecraft.getInstance().getWorld().worldChunks.values()) {
            if (!chunk.amIInRenderDistance()) {
                continue;
            }
            if (!chunk.getStatus().isAtLeast(ChunkStatus.LIGHT)) {
                continue;
            }
            MeshChunk chunkMesh = chunk.chunkMesh;
            float chunkWorldX = chunkMesh.chunkPos * CHUNK_WIDTH * 16.0f * zoom;
            shader.setUniformf("u_chunkOffset", chunkWorldX, 0f);

            if (chunkMesh.mesh == null) {
                continue;
            }
            chunkMesh.mesh.render(shader, GL20.GL_TRIANGLES);
        }
    }

    /**
     * Returns day-night sky multiplier used to modulate sky light in the chunk shader.
     */
    private float getSkyIntensity() {
        long ticks = Minecraft.getInstance().getTotalTicks() % 24000;
        if (Gdx.input.isKeyPressed(Keys.G)){
            return 1f;//gamma oveeride for testing
        }
        if (ticks < 2000) {
            return 0.1f + 0.4f * (ticks / 2000f);
        } else if (ticks < 6000) {
            return 0.5f + 0.5f * ((ticks - 2000) / 4000f);
        } else if (ticks < 12000) {
            return 1.0f;
        } else if (ticks < 14000) {
            return 1.0f - 0.3f * ((ticks - 12000) / 2000f);
        } else if (ticks < 18000) {
            return 0.7f - 0.6f * ((ticks - 14000) / 4000f);
        } else {
            return 0.1f;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        shader.dispose();
    }
}
