package com.badlogic.gdx.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

public class OndemandAssetManager {

    private final AssetManager assetManager;
    private final Retriever retriever;

    private final ObjectMap<String, Class> toRetrieve = new OrderedMap<String, Class>();
    private final ObjectMap<String, Class> retrieved = new OrderedMap<String, Class>();
    private final ObjectMap<String, Class> toLoadIntoMemory = new OrderedMap<String, Class>();

    private final ObjectMap<String, Float> progress = new ObjectMap<String, Float>();
    private final ObjectMap<String, ObjectMap<String, Class>> dependencyTree = new OrderedMap<String, ObjectMap<String, Class>>();

    public OndemandAssetManager(AssetManager assetManager, Retriever retriever) {
        this.assetManager = assetManager;
        this.retriever = retriever;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public void clear() {
        toRetrieve.clear();
        retrieved.clear();
        progress.clear();
        dependencyTree.clear();
    }

    public void load(String fileName, final Class type) {
        Gdx.app.debug("OAM", fileName + " isInMemory " + assetManager.isLoaded(fileName, type));
        if (!assetManager.isLoaded(fileName, type) && !isRequestedForRetrieval(fileName) && !isRetrieved(fileName)) {
            retrieveDependencies(fileName, type);
            retrieve(fileName, type);
        }
    }

    private void retrieveDependencies(String fileName, Class type) {
        if (type == TextureAtlas.class || type == BitmapFont.class) {
            String textureName = fileName.split("\\.")[0] + ".png";
            Gdx.app.debug("OAM", "Retrieving dependency " + textureName + " of " + fileName + " parent type " + type);
            ObjectMap<String, Class> dependency = dependencyTree.get(fileName, new ObjectMap<String, Class>());
            dependency.put(textureName, Texture.class);
            dependencyTree.put(fileName, dependency);

            retrieve(textureName, Texture.class);
        }
    }

    private void retrieve(final String fileName, final Class type) {
        toRetrieve.put(fileName, type);
        retriever.retrieve(fileName, type, new RetrievalListener() {
            @Override
            public void onProgress(String fileName, Class type, float progress) {
                OndemandAssetManager.this.progress.put(fileName, progress);
            }

            @Override
            public void onFailure(String fileName, Class type) {
                Gdx.app.error("OAM", "Error loading: " + fileName);
            }

            @Override
            public void onSuccess(String fileName, Class type) {
                Gdx.app.debug("OAM", fileName + " retrieved: " + type);
                retrieved.put(fileName, type);
                toLoadIntoMemory.put(fileName, type);
            }
        });
    }

    private boolean isRequestedForRetrieval(String fileName) {
        return toRetrieve.containsKey(fileName);
    }

    private boolean isRetrieved(String fileName) {
        return retrieved.containsKey(fileName);
    }

    public boolean isComplete() {
        float inMemory = 0;
        for (ObjectMap.Entry<String, Class> entry : toRetrieve.entries()) {
            if (assetManager.isLoaded(entry.key, entry.value)) {
                inMemory++;
            }
        }
        return inMemory == toRetrieve.size;
    }

    public float progress() {
        float rtn = 0;

        for (Float f : progress.values()) {
            rtn += ((1 / (float) toRetrieve.size) * f);
        }

        rtn *= 0.9f;

        float inMemory = 0;
        for (ObjectMap.Entry<String, Class> entry : toRetrieve.entries()) {
            if (assetManager.isLoaded(entry.key, entry.value)) {
                inMemory++;
            }
        }

        rtn += ((inMemory / (float) toRetrieve.size) * 0.1f);

        return rtn;
    }

    public float update() {
        assetManager.update();
        checkAndLoadedRetrievedAssets();
        return progress();
    }

    private void checkAndLoadedRetrievedAssets() {
        if (toLoadIntoMemory.size > 0) {
            for (ObjectMap.Entries<String, Class> entries = toLoadIntoMemory.entries(); entries.hasNext(); ) {
                ObjectMap.Entry<String, Class> toLoad = entries.next();
                String name = toLoad.key;
                Gdx.app.debug("OAM", name + " attempting memory load");
                if (!dependencyTree.containsKey(name)) {
                    Gdx.app.debug("OAM", name + " added to manager for memory load");
                    assetManager.load(toLoad.key, toLoad.value);
                    entries.remove();
                    break;
                } else {
                    Gdx.app.debug("OAM", name + " has dependencies");
                    boolean dependenciesLoaded = false;
                    ObjectMap<String, Class> dependencies = dependencyTree.get(name);
                    for (String dependency : dependencies.keys()) {
                        Gdx.app.debug("OAM", name + " has dependency " + dependency);
                        dependenciesLoaded |= retrieved.containsKey(dependency);
                    }
                    if (dependenciesLoaded) {
                        Gdx.app.debug("OAM", toLoad.key + " attempting memory load - dependency");
                        assetManager.load(toLoad.key, toLoad.value);
                        entries.remove();
                    }
                }
            }
        }
    }

    public interface Retriever {
        void retrieve(String fileName, Class type, RetrievalListener listener);
    }

    public interface RetrievalListener {

        void onProgress(String fileName, Class type, float progress);

        void onFailure(String fileName, Class type);

        void onSuccess(String fileName, Class type);
    }

}
