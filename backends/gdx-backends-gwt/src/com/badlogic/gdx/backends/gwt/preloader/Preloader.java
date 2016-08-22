/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.gwt.preloader;

import com.badlogic.gdx.utils.Array;

public interface Preloader {

    void preload (PreloaderCallback callback);

    PreloadedAssetManager getPreloadedAssetManager();

    interface PreloaderCallback {

        void update(PreloaderState state);

        void error(String file);

    }

    class PreloaderState {

        public PreloaderState(Array<DefaultPreloader.Asset> assets) {
            this.assets = assets;
        }

        public long getDownloadedSize() {
            long size = 0;
            for (int i = 0; i < assets.size; i++) {
                DefaultPreloader.Asset asset = assets.get(i);
                size += (asset.succeed || asset.failed) ? asset.size : Math.min(asset.size, asset.loaded);
            }
            return size;
        }

        public long getTotalSize() {
            long size = 0;
            for (int i = 0; i < assets.size; i++) {
                DefaultPreloader.Asset asset = assets.get(i);
                size += asset.size;
            }
            return size;
        }

        public float getProgress() {
            long total = getTotalSize();
            return total == 0 ? 1 : (getDownloadedSize() / (float) total);
        }

        public boolean hasEnded() {
            return getDownloadedSize() == getTotalSize();
        }

        public final Array<DefaultPreloader.Asset> assets;

    }

    class Asset {
        public Asset (String url, AssetFilter.AssetType type, long size, String mimeType) {
            this.url = url;
            this.type = type;
            this.size = size;
            this.mimeType = mimeType;
        }

        public boolean succeed;
        public boolean failed;
        public long loaded;
        public final String url;
        public final AssetFilter.AssetType type;
        public final long size;
        public final String mimeType;
    }
}
