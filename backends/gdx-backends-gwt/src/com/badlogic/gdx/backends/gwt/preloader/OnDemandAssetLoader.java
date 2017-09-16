package com.badlogic.gdx.backends.gwt.preloader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.OndemandAssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;
import com.google.gwt.dom.client.ImageElement;

public class OnDemandAssetLoader implements Preloader {

    private final AssetDownloader loader;
    private final PreloadedAssetManager preloadedAssetManager = new PreloadedAssetManager();

    private final String baseUrl;
    private final boolean crossDomain;

    public OnDemandAssetLoader(
            String baseUrl, boolean crossDomain) {
        this.baseUrl = baseUrl;
        this.crossDomain = crossDomain;
        this.loader = crossDomain ? AssetDownloader.crossDomain() : AssetDownloader.standard();
    }

    @Override
    public void preload(final PreloaderCallback callback) {

        callback.update(new PreloaderState(new Array<Asset>()) {
            @Override
            public boolean hasEnded() {
                return true;
            }
        });
    }

    @Override
    public PreloadedAssetManager getPreloadedAssetManager() {
        return preloadedAssetManager;
    }

    public void load(AssetDescriptor assetDescriptor, OndemandAssetManager.RetrievalListener retrievalListener) {
        Gdx.app.debug("LOADING", "Downloading from " + (baseUrl + assetDescriptor.fileName));

        if (assetDescriptor.type == Texture.class) {
            loadImage(assetDescriptor, retrievalListener);
        }
        if (assetDescriptor.type == TextureAtlas.class) {
            loadAtlas(assetDescriptor, retrievalListener);
        }
        if (assetDescriptor.type == BitmapFont.class) {
            loadBitmapFont(assetDescriptor, retrievalListener);
        }
        if (assetDescriptor.type == Music.class) {
            loadMusic(assetDescriptor, retrievalListener);
        }

        if (assetDescriptor.type == TiledMap.class) {
            loadTileMap(assetDescriptor, retrievalListener);
        }

        if (assetDescriptor.type == Sound.class) {
            loadMusic(assetDescriptor, retrievalListener);
        }
    }

    private void loadMusic(final AssetDescriptor assetDescriptor, final OndemandAssetManager.RetrievalListener retrievalListener) {
        load(assetDescriptor, AssetFilter.AssetType.Audio, "", retrievalListener);
    }

    private void loadAtlas(final AssetDescriptor assetDescriptor, final OndemandAssetManager.RetrievalListener retrievalListener) {
        load(assetDescriptor, AssetFilter.AssetType.Text, "text/plain", retrievalListener);
    }

    private void loadBitmapFont(final AssetDescriptor assetDescriptor, final OndemandAssetManager.RetrievalListener retrievalListener) {
        load(assetDescriptor, AssetFilter.AssetType.Text, "application/unknown", retrievalListener);
    }

    private void loadImage(final AssetDescriptor assetDescriptor, OndemandAssetManager.RetrievalListener retrievalListener) {
        String assetFileUrl = assetDescriptor.fileName;
        String mimeType = assetFileUrl.endsWith("png") ? "image/png" : "image/jpeg";
        load(assetDescriptor, AssetFilter.AssetType.Image, mimeType, retrievalListener);
    }

    private void loadTileMap(final AssetDescriptor assetDescriptor, final OndemandAssetManager.RetrievalListener retrievalListener) {
        load(assetDescriptor, AssetFilter.AssetType.Text, "text/plain", retrievalListener);
    }

    private void load(final AssetDescriptor assetDescriptor, final AssetFilter.AssetType assetType, String mimeType, final OndemandAssetManager.RetrievalListener retrievalListener) {
        load(baseUrl, assetDescriptor, assetType, mimeType, retrievalListener);
    }

    public void load(final String baseUrl, final AssetDescriptor assetDescriptor, final AssetFilter.AssetType assetType, String mimeType, final OndemandAssetManager.RetrievalListener retrievalListener) {
        loader.load(baseUrl + assetDescriptor.fileName, assetType, mimeType, new AssetDownloader.AssetLoaderListener<Object>() {
            @Override
            public void onProgress(double amount) {
                retrievalListener.onProgress(assetDescriptor.fileName, assetDescriptor.type, (float) amount);
            }

            @Override
            public void onFailure() {
                retrievalListener.onFailure(assetDescriptor.fileName, assetDescriptor.type);
            }

            @Override
            public void onSuccess(Object result) {
                switch (assetType) {
                    case Text:
                        preloadedAssetManager.texts.put(assetDescriptor.fileName, (String) result);
                        break;
                    case Image: {
                        ImageElement imageElement = (ImageElement) result;
                        if (crossDomain) {
                            Gdx.app.debug("LOADING", "SETTING CROSS DOMAIN");
                            imageElement.setAttribute("crossOrigin", AssetDownloader.CROSS_DOMAIN);
                        }
                        preloadedAssetManager.images.put(assetDescriptor.fileName, (ImageElement) result);
                    }
                    break;
                    case Binary:
                        preloadedAssetManager.binaries.put(assetDescriptor.fileName, (Blob) result);
                        break;
                    case Audio:
                        preloadedAssetManager.audio.put(assetDescriptor.fileName, null);
                        break;
                    case Directory:
                        preloadedAssetManager.directories.put(assetDescriptor.fileName, null);
                        break;
                }
                retrievalListener.onSuccess(assetDescriptor.fileName, assetDescriptor.type);
            }
        });
    }

    public void loadAbsoluteCrossDomain(final AssetDescriptor assetDescriptor, String mimeType, final OndemandAssetManager.RetrievalListener retrievalListener) {
        loader.loadImage(assetDescriptor.fileName, mimeType, AssetDownloader.CROSS_DOMAIN, new AssetDownloader.AssetLoaderListener<ImageElement>() {
            @Override
            public void onProgress(double amount) {
                retrievalListener.onProgress(assetDescriptor.fileName, assetDescriptor.type, (float) amount);
            }

            @Override
            public void onFailure() {
                retrievalListener.onFailure(assetDescriptor.fileName, assetDescriptor.type);
            }

            @Override
            public void onSuccess(ImageElement result) {
                preloadedAssetManager.images.put(assetDescriptor.fileName, result);
                retrievalListener.onSuccess(assetDescriptor.fileName, assetDescriptor.type);
            }
        });
    }

}
