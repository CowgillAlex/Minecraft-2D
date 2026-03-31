package dev.alexco.minecraft.blaze2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Logger;

import dev.alexco.minecraft.util.Lifecycle;

public class TextureManager implements Lifecycle {
    private AssetManager assetManager;
    public static String lastLoaded = "";

    @Override
    public void create() {
        assetManager = new AssetManager();
        configureLogger();
        loadAssets();
    }

    /**
     * Hooks the asset manager logger to expose the last loaded texture path.
     */
    private void configureLogger() {
        assetManager.setLogger(new Logger("", Logger.INFO) {
            @Override
            public void info(String message) {
                String text = message.replaceAll(", com.badlogic.gdx.graphics.Texture", "");
                lastLoaded = text;
            }
        });
    }

    /**
     * Forces synchronous loading for one texture path.
     */
    public void forceLoadTexture(String texPath){
        assetManager.load(texPath, Texture.class);
        assetManager.finishLoadingAsset(texPath);
        if (assetManager.get(texPath, Texture.class)!=null){
            dev.alexco.minecraft.util.Logger.INFO("Successfully force loaded %s", texPath);
        } else{

            dev.alexco.minecraft.util.Logger.ERROR("Failed to force load %s. Perhaps it doesn't exist?", texPath);
        }
    }
    public AssetManager getAssetManager() {
        return assetManager;
    }

    /**
     * Queues all png assets listed in assets.txt for asynchronous loading.
     */
    public void loadAssets() {
        FileHandle assetHandle = Gdx.files.internal("assets.txt");
        String text = assetHandle.readString();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.contains(".png")) {
                if (!line.contains(".png.mcmeta")) {
                    assetManager.load(line, Texture.class);
                }
            }
        }
    }

    public boolean update(int time) {
        return assetManager.update(time);
    }
    public boolean update() {
        return assetManager.update();
    }

    public float getProgress() {
        return assetManager.getProgress();
    }

    /**
     * Returns a texture by path, falling back to missingno when lookup fails.
     */
    public Texture get(String fileName) {
       try {

           return assetManager.get(fileName);
       } catch (Exception e){
        return assetManager.get("missingno.png");
       }
    }

    public String getLastLoaded() {
        return lastLoaded;
    }

    @Override
    public void destroy() {
        assetManager.dispose();
    }


}
