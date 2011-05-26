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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Loads a Tiled Map from a tmx file
 * @author David Fraska
 * */
public class TiledLoader extends DefaultHandler {

	// define states
	private static final int INIT = 0;
	private static final int DATA = 1;
	private static final int DONE = 2;

	/**
	 * Loads a Tiled Map from a tmx file
	 * @param tmxFile the map's tmx file
	 * */
	public static TiledMap createMap (FileHandle tmxFile) {

		final TiledMap map;

		map = new TiledMap();
		map.tmxFile = tmxFile;

		SAXParser parser = null;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(new InputSource(tmxFile.read()), new DefaultHandler() {

				int state = INIT;;
				TiledLayer currentLayer;
				TileSet currentTileSet;
				TiledObjectGroup currentObjectGroup;
				TiledObject currentObject;
				int currentTile;

				Stack<String> currentBranch = new Stack<String>();

				int firstgid, tileWidth, tileHeight, margin, spacing;
				String tileSetName;
				String encoding, dataString, compression;
				byte[] data;

				int dataCounter = 0, row, col;

				@Override public void startElement (String uri, String name, String qName, Attributes attr) {										
					if("".equals(qName)){
						currentBranch.push(name);
					} else{
						currentBranch.push(qName);						
					}
					
					try {

						if ("layer".equals(qName) | "layer".equals(name)) {
							String layerName = attr.getValue("name");
							int layerWidth = Integer.parseInt(attr.getValue("width"));
							int layerHeight = Integer.parseInt(attr.getValue("height"));
							currentLayer = new TiledLayer(layerName, layerWidth, layerHeight);
							return;
						}

						if ("data".equals(qName) | "data".equals(name)) {
							encoding = attr.getValue("encoding");
							compression = attr.getValue("compression");
							dataString = ""; // clear the string for new data
							state = DATA;
							return;
						}

						if ("tileset".equals(qName) | "tileset".equals(name)) {
							firstgid = Integer.parseInt(attr.getValue("firstgid"));
							tileWidth = Integer.parseInt(attr.getValue("tilewidth"));
							tileHeight = Integer.parseInt(attr.getValue("tileheight"));
							tileSetName = attr.getValue("name");
							spacing = parseIntWithDefault(attr.getValue("spacing"), 0);
							margin = parseIntWithDefault(attr.getValue("margin"), 0);
							return;
						}

						if ("objectgroup".equals(qName) | "objectgroup".equals(name)) {
							currentObjectGroup = new TiledObjectGroup();
							currentObjectGroup.name = attr.getValue("name");
							currentObjectGroup.height = Integer.parseInt(attr.getValue("height"));
							currentObjectGroup.width = Integer.parseInt(attr.getValue("width"));
							return;
						}

						if ("object".equals(qName) | "object".equals(name)) {
							currentObject = new TiledObject();
							currentObject.name = attr.getValue("name");
							currentObject.type = attr.getValue("type");
							currentObject.x = Integer.parseInt(attr.getValue("x"));
							currentObject.y = Integer.parseInt(attr.getValue("y"));
							currentObject.width = parseIntWithDefault(attr.getValue("width"), 0);
							currentObject.height = parseIntWithDefault(attr.getValue("height"), 0);
							currentObject.gid = parseIntWithDefault(attr.getValue("gid"), 0);
							return;
						}

						if ("image".equals(qName) | "image".equals(name)) {
							currentTileSet = new TileSet();
							currentTileSet.imageName = attr.getValue("source");
							currentTileSet.name = tileSetName;
							currentTileSet.tileWidth = tileWidth;
							currentTileSet.tileHeight = tileHeight;
							currentTileSet.firstgid = firstgid;
							currentTileSet.spacing = spacing;
							currentTileSet.margin = margin;
							return;
						}

						if ("map".equals(qName) | "map".equals(name)) {
							map.orientation = attr.getValue("orientation");
							map.width = Integer.parseInt(attr.getValue("width"));
							map.height = Integer.parseInt(attr.getValue("height"));
							map.tileWidth = Integer.parseInt(attr.getValue("tilewidth"));
							map.tileHeight = Integer.parseInt(attr.getValue("tileheight"));
							return;
						}

						if ("tile".equals(qName) | "tile".equals(name)) {
							switch (state) {
							case INIT:
								currentTile = Integer.parseInt(attr.getValue("id"));
								break;
							case DATA:
								col = dataCounter % currentLayer.width;
								row = dataCounter / currentLayer.width;
								currentLayer.tiles[row][col] = Integer.parseInt(attr.getValue("gid"));
								dataCounter++;
								break;
							}

							return;
						}

						if ("property".equals(qName) | "property".equals(name)) {
							String parentType = currentBranch.get(currentBranch.size() - 3);
							putProperty(parentType, attr.getValue("name"), attr.getValue("value"));
							return;
						}
					} catch (NumberFormatException e) {
						throw new GdxRuntimeException("Required attribute missing from TMX file! Property for " + qName + " missing.");
						// Note: Required integer attributes are parsed with "Integer.parseInt()" directly
						// Non-required integer attributes are parsed with parseIntWithDefault()
					}
				}

				@Override public void startDocument () {

				}

				private void putProperty (String parentType, String name, String value) {
					if ("tile".equals(parentType)) {
						map.setTileProperty(currentTile + currentTileSet.firstgid, name, value);
						return;
					}

					if ("map".equals(parentType)) {
						map.properties.put(name, value);
						return;
					}

					if ("layer".equals(parentType)) {
						currentLayer.properties.put(name, value);
						return;
					}

					if ("objectgroup".equals(parentType)) {
						currentObjectGroup.properties.put(name, value);
						return;
					}

					if ("object".equals(parentType)) {
						currentObject.properties.put(name, value);
						return;
					}
				}

				// No checking is done to make sure that an element has actually started.
				// Currently this may cause strange results if the XML file is malformed
				@Override public void endElement (String uri, String name, String qName) {
					currentBranch.pop();

					if ("data".equals(qName) | "data".equals(name)) {
						if (dataString == null | "".equals(dataString)) return;

						// decode and uncompress the data
						if ("base64".equals(encoding)) {
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

						state = INIT;
						return;
					}

					if ("layer".equals(qName) | "layer".equals(name)) {
						map.layers.add(currentLayer);
						currentLayer = null;
						return;
					}

					if ("tileset".equals(qName) | "tileset".equals(name)) {
						map.tileSets.add(currentTileSet);
						currentTileSet = null;
						return;
					}

					if ("objectgroup".equals(qName) | "objectgroup".equals(name)) {
						map.objectGroups.add(currentObjectGroup);
						currentObjectGroup = null;
						return;
					}

					if ("object".equals(qName) | "object".equals(name)) {
						currentObjectGroup.objects.add(currentObject);
						currentObject = null;
						return;
					}
				}

				private void fromCSV () {
					StringTokenizer st = new StringTokenizer(dataString.trim(), ",");
					for (int row = 0; row < currentLayer.height; row++) {
						for (int col = 0; col < currentLayer.width; col++) {
							currentLayer.tiles[row][col] = Integer.parseInt(st.nextToken().trim());
						}
					}
				}

				private void arrangeData () {
					int byteCounter = 0;
					for (int row = 0; row < currentLayer.height; row++) {
						for (int col = 0; col < currentLayer.width; col++) {
							currentLayer.tiles[row][col] = unsignedByteToInt(data[byteCounter++])
								| unsignedByteToInt(data[byteCounter++]) << 8 | unsignedByteToInt(data[byteCounter++]) << 16
								| unsignedByteToInt(data[byteCounter++]) << 24;
						}
					}
				}

				private void unZlib () {
					Inflater zlib = new Inflater();
					byte[] readTemp = new byte[4];

					zlib.setInput(data, 0, data.length);

					for (int row = 0; row < currentLayer.height; row++) {
						for (int col = 0; col < currentLayer.width; col++) {
							try {
								zlib.inflate(readTemp, 0, 4);
								currentLayer.tiles[row][col] = unsignedByteToInt(readTemp[0]) | unsignedByteToInt(readTemp[1]) << 8
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
					for (int row = 0; row < currentLayer.height; row++) {
						for (int col = 0; col < currentLayer.width; col++) {
							try {
								GZIS.read(readTemp, 0, 4);
								currentLayer.tiles[row][col] = unsignedByteToInt(readTemp[0]) | unsignedByteToInt(readTemp[1]) << 8
									| unsignedByteToInt(readTemp[2]) << 16 | unsignedByteToInt(readTemp[3]) << 24;
							} catch (IOException e) {
								throw new GdxRuntimeException("Error Reading TMX Layer Data.", e);
							}
						}
					}
				}

				@Override public void endDocument () {
					state = DONE;
				}

				@Override public void characters (char ch[], int start, int length) {
					switch (state) {
					case DATA:
						dataString = dataString.concat(String.copyValueOf(ch, start, length));
						break;
					default:
						break;
					}
				}
			});
		} catch (ParserConfigurationException e) {
			throw new GdxRuntimeException("Error Parsing TMX file.", e);
		} catch (SAXException e) {
			throw new GdxRuntimeException("Error Parsing TMX file.", e);
		} catch (IOException e) {
			throw new GdxRuntimeException("Error Parsing TMX file.", e);
		}

		return map;
	}

	static int unsignedByteToInt (byte b) {
		return (int)b & 0xFF;
	}

	static int parseIntWithDefault (String string, int defaultValue) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}
