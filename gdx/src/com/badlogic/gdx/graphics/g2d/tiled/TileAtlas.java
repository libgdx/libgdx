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
package com.badlogic.gdx.graphics.g2d.tiled;

import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;

/**
 * Contains an atlas of tiles by tile id for use with {@link TileMapRenderer}
 * @author David Fraska
 */
public class TileAtlas implements Disposable {

    protected IntMap<TextureRegion> regionsMap = new IntMap<TextureRegion>();
    protected final HashSet<Texture> textures = new HashSet<Texture>(1);

    /**
     * Protected constructor to allow different implementations
     */
    protected TileAtlas() {
    }

    /**
     * Creates a TileAtlas for use with {@link TileMapRenderer}. Run the map through TiledMapPacker to create the files required.
     * @param map The tiled map
     * @param inputDir The directory containing all the files created by TiledMapPacker
     * */
    public TileAtlas(TiledMap map, FileHandle inputDir) {
        // TODO: Create a constructor that doesn't take a tmx map, 
        for (TileSet set : map.tileSets) {
            FileHandle packfile = getRelativeFileHandle(inputDir, removeExtension(set.imageName) + " packfile");
            TextureAtlas textureAtlas = new TextureAtlas(packfile, packfile.parent(), false);
            List<AtlasRegion> atlasRegions = textureAtlas.findRegions(removeExtension(removePath(set.imageName)));

            for (AtlasRegion reg : atlasRegions) {
                regionsMap.put(reg.index + set.firstgid, reg);
                if (!textures.contains(reg.getTexture())) {
                    textures.add(reg.getTexture());
                }
            }
        }
    }

    /**
     * Gets an {@link TextureRegion} for a tile id
     * @param id tile id
     * @return the {@link TextureRegion}
     */
    public TextureRegion getRegion(int id) {
        return regionsMap.get(id);
    }

    /**
     * Releases all resources associated with this TileAtlas instance. This releases all the textures backing all AtlasRegions,
     * which should no longer be used after calling dispose.
     */
    @Override
    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
        textures.clear();
    }

    private static String removeExtension(String s) {
        int extensionIndex = s.lastIndexOf(".");
        if (extensionIndex == -1) {
            return s;
        }

        return s.substring(0, extensionIndex);
    }

    private static String removePath(String s) {
        String temp;

        int index = s.lastIndexOf('\\');
        if (index != -1) {
            temp = s.substring(index + 1);
        } else {
            temp = s;
        }

        index = temp.lastIndexOf('/');
        if (index != -1) {
            return s.substring(index + 1);
        } else {
            return s;
        }
    }

    private static FileHandle getRelativeFileHandle(FileHandle path, String relativePath) {
        if (relativePath.trim().length() == 0) {
            return path;
        }

        FileHandle child = path;

        StringTokenizer tokenizer = new StringTokenizer(relativePath, "\\/");
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            if (token.equals("..")) {
                child = child.parent();
            } else {
                child = child.child(token);
            }
        }

        return child;
    }
}
