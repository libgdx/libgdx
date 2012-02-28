/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g2d.tiled;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;

/** Loads a Tiled Map from a tmx file
 * @author David Fraska */
public class TiledLoader {

	/** Loads a <code>TiledMap</code> from a <code>String</code>.
	 * @param tmxData The tmx file's content. */
	public static TiledMap createMap (String tmxData) {
		return createMap(null, tmxData);
	}

	/** Loads a Tiled Map from a tmx file
	 * @param tmxFile the map's tmx file */
	public static TiledMap createMap (FileHandle tmxFile) {
		return createMap(tmxFile, null);
	}

	/** Loads a TiledMap from a tmx file.
	 * @param tmxFile The tmx file. NULL to force load from <code>tmxData</code>.
	 * @param tmxData The tmx file's content. NULL to force load from <code>tmxFile</code>. */
	private static TiledMap createMap (FileHandle tmxFile, String tmxData) {
		final TiledMap map;

		map = new TiledMap();
		map.tmxFile = tmxFile;

		try {
			XmlReader xmlReader = new XmlReader() {

				Stack<String> currBranch = new Stack<String>();

				boolean awaitingData = false;
				TiledLayer currLayer;
				int currLayerWidth = 0, currLayerHeight = 0;
				TileSet currTileSet;
				TiledObjectGroup currObjectGroup;
				TiledObject currObject;
				int currTile;

				class Polyline {
					String name;
					String points;

					public Polyline( String name ) {
						this.name = name;
					}

					public Polyline() {
					}
				}

				Polyline polyline,polygon;

				class Property {
					String parentType, name, value;
				}

				Property currProperty;

				String encoding, dataString, compression;
				byte[] data;

				int dataCounter = 0, row, col;

				@Override
				protected void open (String name) {
					currBranch.push(name);

					if ("layer".equals(name)) {
						currLayer = new TiledLayer();
						return;
					}

					if ("tileset".equals(name)) {
						currTileSet = new TileSet();
						return;
					}

					if ("data".equals(name)) {
						dataString = ""; // clear the string for new data
						awaitingData = true;
						return;
					}

					if ("objectgroup".equals(name)) {
						currObjectGroup = new TiledObjectGroup();
						return;
					}

					if ("object".equals(name)) {
						currObject = new TiledObject();
						return;
					}

					if ("property".equals(name)) {
						currProperty = new Property();
						currProperty.parentType = currBranch.get(currBranch.size() - 3);
						return;
					}

					if( "polyline".equals( name ) ) {
						polyline = new Polyline("polyline");
						return;
					}

					if( "polygon".equals( name ) ) {
						polygon = new Polyline("polygon");
						return;
					}
				}

				@Override
				protected void attribute (String name, String value) {
					String element = currBranch.peek();

					if ("layer".equals(element)) {
						if ("width".equals(name)) {
							currLayerWidth = Integer.parseInt(value);
						} else if ("height".equals(name)) {
							currLayerHeight = Integer.parseInt(value);
						}

						if (currLayerWidth != 0 && currLayerHeight != 0) {
							currLayer.tiles = new int[currLayerHeight][currLayerWidth];
						}
						if ("name".equals(name)) {
							currLayer.name = value;
						}
						return;
					}

					if ("tileset".equals(element)) {
						if ("firstgid".equals(name)) {
							currTileSet.firstgid = Integer.parseInt(value);
							return;
						}
						if ("tilewidth".equals(name)) {
							currTileSet.tileWidth = Integer.parseInt(value);
							return;
						}
						if ("tileheight".equals(name)) {
							currTileSet.tileHeight = Integer.parseInt(value);
							return;
						}
						if ("name".equals(name)) {
							currTileSet.name = value;
							return;
						}
						if ("spacing".equals(name)) {
							currTileSet.spacing = Integer.parseInt(value);
							return;
						}
						if ("margin".equals(name)) {
							currTileSet.margin = Integer.parseInt(value);
							return;
						}
						return;
					}

					if ("image".equals(element)) {
						if ("source".equals(name)) {
							currTileSet.imageName = value;
							return;
						}
						return;
					}

					if ("data".equals(element)) {
						if ("encoding".equals(name)) {
							encoding = value;
							return;
						}
						if ("compression".equals(name)) {
							compression = value;
							return;
						}
						return;
					}

					if ("objectgroup".equals(element)) {
						if ("name".equals(name)) {
							currObjectGroup.name = value;
							return;
						}
						if ("height".equals(name)) {
							currObjectGroup.height = Integer.parseInt(value);
							return;
						}
						if ("width".equals(name)) {
							currObjectGroup.width = Integer.parseInt(value);
							return;
						}
						return;
					}

					if ("object".equals(element)) {
						if ("name".equals(name)) {
							currObject.name = value;
							return;
						}
						if ("type".equals(name)) {
							currObject.type = value;
							return;
						}
						if ("x".equals(name)) {
							currObject.x = Integer.parseInt(value);
							return;
						}
						if ("y".equals(name)) {
							currObject.y = Integer.parseInt(value);
							return;
						}
						if ("width".equals(name)) {
							currObject.width = Integer.parseInt(value);
							return;
						}
						if ("height".equals(name)) {
							currObject.height = Integer.parseInt(value);
							return;
						}
						if ("gid".equals(name)) {
							currObject.gid = Integer.parseInt(value);
							return;
						}
						return;
					}

					if ("map".equals(element)) {
						if ("orientation".equals(name)) {
							map.orientation = value;
							return;
						}
						if ("width".equals(name)) {
							map.width = Integer.parseInt(value);
							return;
						}
						if ("height".equals(name)) {
							map.height = Integer.parseInt(value);
							return;
						}
						if ("tilewidth".equals(name)) {
							map.tileWidth = Integer.parseInt(value);
							return;
						}
						if ("tileheight".equals(name)) {
							map.tileHeight = Integer.parseInt(value);
							return;
						}
						return;
					}

					if ("tile".equals(element)) {
						if (awaitingData) { // Actually getting tile data
							if ("gid".equals(name)) {
								col = dataCounter % currLayerWidth;
								row = dataCounter / currLayerWidth;
								if (row < currLayerHeight) {
									currLayer.tiles[row][col] = Integer.parseInt(value);
								} else {
									Gdx.app.log("TiledLoader", "Warning: extra XML gid values ignored! Your map is likely corrupt!");
								}
								dataCounter++;
							}
						} else { // Not getting tile data, must be a tile Id (for properties)
							if ("id".equals(name)) {
								currTile = Integer.parseInt(value);
							}
						}
						return;
					}

					if ("property".equals(element)) {
						if ("name".equals(name)) {
							currProperty.name = value;
							return;
						}
						if ("value".equals(name)) {
							currProperty.value = value;
							return;
						}
						return;
					}

					if( "polyline".equals( element ) ) {
						if( "points".equals( name ) ) {
							polyline.points = value;
							return;
						}
						return;
					}

					if( "polygon".equals( element ) ) {
						if( "points".equals( name ) ) {
							polygon.points = value;
							return;
						}
						return;
					}
				}

				@Override
				protected void text (String text) {
					if (awaitingData) {
						dataString = dataString.concat(text);
					}
				}

				@Override
				protected void close () {
					String element = currBranch.pop();

					if ("layer".equals(element)) {
						map.layers.add(currLayer);
						currLayer = null;
						return;
					}

					if ("tileset".equals(element)) {
						map.tileSets.add(currTileSet);
						currTileSet = null;
						return;
					}

					if ("object".equals(element)) {
						currObjectGroup.objects.add(currObject);
						currObject = null;
						return;
					}

					if ("objectgroup".equals(element)) {
						map.objectGroups.add(currObjectGroup);
						currObjectGroup = null;
						return;
					}

					if ("property".equals(element)) {
						putProperty(currProperty);
						currProperty = null;
						return;
					}

					if( "polyline".equals( element ) ) {
						putPolyLine( polyline );
						polyline = null;
						return;
					}

					if( "polygon".equals( element ) ) {
						putPolyLine( polygon );
						polygon = null;
						return;
					}

					if ("data".equals(element)) {

						// decode and uncompress the data
						if ("base64".equals(encoding)) {
							if (dataString == null | "".equals(dataString.trim())) return;

							data = Base64Coder.decode(dataString.trim());

							if ("gzip".equals(compression)) {
								unGZip();
							} else if ("zlib".equals(compression)) {
								unZlib();
							} else if (compression == null) {
								arrangeData();
							}

						} else if ("csv".equals(encoding) && compression == null) {
							fromCSV();

						} else if (encoding == null && compression == null) {
							// startElement() handles most of this
							dataCounter = 0;// reset counter in case another layer comes through
						} else {
							throw new GdxRuntimeException("Unsupported encoding and/or compression format");
						}

						awaitingData = false;
						return;
					}

					if ("property".equals(element)) {
						putProperty(currProperty);
						currProperty = null;
					}
				}

				private void putPolyLine( Polyline polyLine ) {
					if( polyLine == null ) {
						return;
					}

					if( "polyline".equals( polyLine.name ) ) {
						currObject.polyline = polyLine.points;
						return;
					}

					if( "polygon".equals( polyLine.name ) ) {
						currObject.polygon = polyLine.points;
						return;
					}

					return;
				}

				private void putProperty (Property property) {
					if ("tile".equals(property.parentType)) {
						map.setTileProperty(currTile + currTileSet.firstgid, property.name, property.value);
						return;
					}

					if ("map".equals(property.parentType)) {
						map.properties.put(property.name, property.value);
						return;
					}

					if ("layer".equals(property.parentType)) {
						currLayer.properties.put(property.name, property.value);
						return;
					}

					if ("objectgroup".equals(property.parentType)) {
						currObjectGroup.properties.put(property.name, property.value);
						return;
					}

					if ("object".equals(property.parentType)) {
						currObject.properties.put(property.name, property.value);
						return;
					}
				}

				private void fromCSV () {
					StringTokenizer st = new StringTokenizer(dataString.trim(), ",");
					for (int row = 0; row < currLayerHeight; row++) {
						for (int col = 0; col < currLayerWidth; col++) {
							currLayer.tiles[row][col] = Integer.parseInt(st.nextToken().trim());
						}
					}
				}

				private void arrangeData () {
					int byteCounter = 0;
					for (int row = 0; row < currLayerHeight; row++) {
						for (int col = 0; col < currLayerWidth; col++) {
							currLayer.tiles[row][col] = unsignedByteToInt(data[byteCounter++])
								| unsignedByteToInt(data[byteCounter++]) << 8 | unsignedByteToInt(data[byteCounter++]) << 16
								| unsignedByteToInt(data[byteCounter++]) << 24;
						}
					}
				}

				private void unZlib () {
					Inflater zlib = new Inflater();
					byte[] readTemp = new byte[4];

					zlib.setInput(data, 0, data.length);

					for (int row = 0; row < currLayerHeight; row++) {
						for (int col = 0; col < currLayerWidth; col++) {
							try {
								zlib.inflate(readTemp, 0, 4);
								currLayer.tiles[row][col] = unsignedByteToInt(readTemp[0]) | unsignedByteToInt(readTemp[1]) << 8
									| unsignedByteToInt(readTemp[2]) << 16 | unsignedByteToInt(readTemp[3]) << 24;
							} catch (DataFormatException e) {
								throw new GdxRuntimeException("Error Reading TMX Layer Data.", e);
							}
						}
					}
				}

				private void unGZip () {
					GZIPInputStream GZIS = null;
					try {
						GZIS = new GZIPInputStream(new ByteArrayInputStream(data), data.length);
					} catch (IOException e) {
						throw new GdxRuntimeException("Error Reading TMX Layer Data - IOException: " + e.getMessage());
					}

					// Read the GZIS data into an array, 4 bytes = 1 GID
					byte[] readTemp = new byte[4];
					for (int row = 0; row < currLayerHeight; row++) {
						for (int col = 0; col < currLayerWidth; col++) {
							try {
								GZIS.read(readTemp, 0, 4);
								currLayer.tiles[row][col] = unsignedByteToInt(readTemp[0]) | unsignedByteToInt(readTemp[1]) << 8
									| unsignedByteToInt(readTemp[2]) << 16 | unsignedByteToInt(readTemp[3]) << 24;
							} catch (IOException e) {
								throw new GdxRuntimeException("Error Reading TMX Layer Data.", e);
							}
						}
					}
				}
			};
			// Is it a file?
			if (tmxFile != null) {
				xmlReader.parse(tmxFile);
			} else {
				xmlReader.parse(tmxData);
			}
		} catch (IOException e) {
			throw new GdxRuntimeException("Error Parsing TMX file", e);
		}

		return map;
	}

	static int unsignedByteToInt (byte b) {
		return (int)b & 0xFF;
	}
}
