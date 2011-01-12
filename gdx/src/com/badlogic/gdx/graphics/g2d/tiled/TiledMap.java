/*
 * Copyright 2010 David Fraska (dfraska@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.graphics.g2d.tiled;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.files.FileHandle;

/**
 * Contains information from a Tiled Map. Use {@link TiledLoader#createMap(FileHandle)} to fill it.
 * @author David Fraska
 * */
public class TiledMap {
	public ArrayList<TiledLayer> layers = new ArrayList<TiledLayer>(4);
	public ArrayList<TiledObjectGroup> objectGroups = new ArrayList<TiledObjectGroup>(1);
	public ArrayList<TileSet> tileSets = new ArrayList<TileSet>(5);
	/** Stores the map properties with a key of the property name. */
	public HashMap<String, String> properties = new HashMap<String, String>(2);
	private ArrayList<TileProperty> tileProperties = new ArrayList<TileProperty>(0);

	public FileHandle tmxFile;
	public String orientation;
	public int width, height, tileWidth, tileHeight;

	/**
	 * Sets a tile's property. Typically only called by {@link TiledLoader#createMap(FileHandle)}
	 * @param id The tile's id
	 * @param name The property name
	 * @param value The property value
	 * */
	public void setTileProperty (int id, String name, String value) {
		for (TileProperty tp : tileProperties) {
			if (tp.id == id) {
				tp.propertyMap.put(name, value);
				return;
			}
		}

		// no TileProperty found with the correct id, add a new one
		TileProperty tempProperty = new TileProperty();
		tempProperty.id = id;
		tempProperty.propertyMap.put(name, value);
		tileProperties.add(tempProperty);
	}

	/**
	 * Gets a tile's property.
	 * @param id The tile's id
	 * @param name The property name
	 * @return The property value
	 * */
	public String getTileProperty (int id, String name) {
		for (TileProperty tp : tileProperties) {
			if (tp.id == id) {
				return tp.propertyMap.get(name);
			}
		}

		return null;
	}

	class TileProperty {
		int id;
		HashMap<String, String> propertyMap = new HashMap<String, String>();
	}
}
