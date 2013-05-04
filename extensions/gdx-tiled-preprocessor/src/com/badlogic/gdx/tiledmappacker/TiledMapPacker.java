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

package com.badlogic.gdx.tiledmappacker;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2.Settings;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectMap;

/** Packs a Tiled Map, adding some properties to improve the speed of the {@link TileMapRenderer}. Also runs the texture packer on
 * the tiles for use with a {@link TileAtlas}
 * @author David Fraska
 * @author Manuel Bua */
public class TiledMapPacker {

	private TexturePacker2 packer;
	private TiledMap map;

	private ArrayList<Integer> blendedTiles = new ArrayList<Integer>();
	private TmxMapLoader mapLoader = new TmxMapLoader();
	private TiledMapPackerSettings settings;

	// a map tracking tileids usage for any given tileset, across multiple maps
	private HashMap<String, IntArray> tilesetUsedIds = new HashMap<String, IntArray>();

	static private class TmxFilter implements FilenameFilter {

		public TmxFilter () {
		}

		@Override
		public boolean accept (File dir, String name) {
			if (name.endsWith(".tmx")) return true;

			return false;
		}

	}

	public TiledMapPacker () {
		this(new TiledMapPackerSettings());
	}

	public TiledMapPacker (TiledMapPackerSettings settings) {
		this.settings = settings;
	}

	/** Typically, you should run the {@link TiledMapPacker#main(String[])} method instead of this method. Packs a directory of
	 * Tiled Maps, adding properties to improve the speed of the {@link TileMapRenderer}. Also runs the texture packer on the tile
	 * sets for use with a {@link TileAtlas}
	 * @param inputDir the input directory containing the tmx files (and tile sets, relative to the path listed in the tmx file)
	 * @param outputDir The output directory for the tmx files, should be empty before running. WARNING: Use caution if you have a
	 *           "../" in the path of your tile sets! The output for these tile sets will be relative to the output directory. For
	 *           example, if your output directory is "C:\mydir\maps" and you have a tileset with the path "../tileset.png", the
	 *           tileset will be output to "C:\mydir\" and the maps will be in "C:\mydir\maps".
	 * @param settings the settings used in the TexturePacker */
	public void processMap (File inputDir, File outputDir, Settings settings) throws IOException {
		FileHandle inputDirHandle = new FileHandle(inputDir.getAbsolutePath());
		File[] files = inputDir.listFiles(new TmxFilter());
		ObjectMap<String, TiledMapTileSet> tilesetsToPack = new ObjectMap<String, TiledMapTileSet>();

		for (File file : files) {
			map = mapLoader.load(file.getAbsolutePath());

			// if enabled, build a list of used tileids for the tileset used by this map
			if (this.settings.stripUnusedTiles) {
				int mapWidth = map.getProperties().get("width", Integer.class);
				int mapHeight = map.getProperties().get("height", Integer.class);
				int numlayers = map.getLayers().getCount();
				int bucketSize = mapWidth * mapHeight * numlayers;

				Iterator<MapLayer> it = map.getLayers().iterator();
				while (it.hasNext()) {
					MapLayer layer = it.next();

					// some layers can be plain MapLayer instances (ie. object groups), just ignore them
					if (layer instanceof TiledMapTileLayer) {
						TiledMapTileLayer tlayer = (TiledMapTileLayer)layer;

						for (int y = 0; y < mapHeight; ++y) {
							for (int x = 0; x < mapWidth; ++x) {
								if (tlayer.getCell(x, y) != null) {
									int tileid = tlayer.getCell(x, y).getTile().getId() & ~0xE0000000;
									String tilesetName = tilesetNameFromTileId(map, tileid);
									IntArray usedIds = getUsedIdsBucket(tilesetName, bucketSize);
									usedIds.add(tileid);

									// track this tileset to be packed if not already tracked
									if (!tilesetsToPack.containsKey(tilesetName)) {
										tilesetsToPack.put(tilesetName, map.getTileSets().getTileSet(tilesetName));
									}
								}
							}
						}
					}
				}
			} else {
				for (TiledMapTileSet tileset : map.getTileSets()) {
					String tilesetName = tileset.getName();
					if (!tilesetsToPack.containsKey(tilesetName)) {
						tilesetsToPack.put(tilesetName, map.getTileSets().getTileSet(tilesetName));
					}
				}
			}

			FileHandle tmxFile = new FileHandle(file.getAbsolutePath());
			writeUpdatedTMX(map, outputDir, tmxFile);
		}

		for (TiledMapTileSet tileset : tilesetsToPack.values()) {
			packTileSet(tileset, this.settings.stripUnusedTiles ? tilesetUsedIds.get(tileset.getName()) : null, inputDirHandle,
				outputDir, settings);
		}
	}

	/** Returns the tileset name associated with the specified tile id
	 * @param tileid
	 * @return a tileset name */
	private String tilesetNameFromTileId (TiledMap map, int tileid) {
		String name = "";
		if (tileid == 0) {
			return "";
		}

		for (TiledMapTileSet tileset : map.getTileSets()) {
			int firstgid = tileset.getProperties().get("firstgid", -1, Integer.class);
			if (firstgid == -1) continue; // skip this tileset
			if (tileid >= firstgid) {
				name = tileset.getName();
			} else {
				return name;
			}
		}

		return name;
	}

	/** Returns the usedIds bucket for the given tileset name, if it doesn't exist one will be created with the specified size
	 * @param tilesetName
	 * @param size
	 * @return a bucket */
	private IntArray getUsedIdsBucket (String tilesetName, int size) {
		if (tilesetUsedIds.containsKey(tilesetName)) {
			return tilesetUsedIds.get(tilesetName);
		}

		IntArray bucket = new IntArray(size);
		tilesetUsedIds.put(tilesetName, bucket);
		return bucket;
	}

	private void packTileSet (TiledMapTileSet set, IntArray usedIds, FileHandle inputDirHandle, File outputDir, Settings settings)
		throws IOException {
		BufferedImage tile;
		Vector2 tileLocation;
		TileSetLayout packerTileSet;
		Graphics g;

		packer = new TexturePacker2(settings);

		int tileWidth = set.getProperties().get("tilewidth", Integer.class);
		int tileHeight = set.getProperties().get("tileheight", Integer.class);
		int firstgid = set.getProperties().get("firstgid", Integer.class);
		String imageName = set.getProperties().get("imagesource", String.class);

		TileSetLayout layout = new TileSetLayout(firstgid, set, inputDirHandle);

		for (int gid = layout.firstgid, i = 0; i < layout.numTiles; gid++, i++) {
			if (usedIds != null && !usedIds.contains(gid)) {
				System.out.println("Stripped Id: " + gid);
				continue;
			}

			tileLocation = layout.getLocation(gid);
			tile = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_4BYTE_ABGR);

			g = tile.createGraphics();
			g.drawImage(layout.image, 0, 0, tileWidth, tileHeight, (int)tileLocation.x, (int)tileLocation.y, (int)tileLocation.x
				+ tileWidth, (int)tileLocation.y + tileHeight, null);

			if (isBlended(tile)) setBlended(gid);

			packer.addImage(tile, removeExtension(removePath(imageName)) + "_" + i);
		}

		File outputFile = getRelativeFile(outputDir, removeExtension(imageName) + " packfile");
		outputFile.getParentFile().mkdirs();
		packer.pack(outputFile.getParentFile(), removeExtension(removePath(imageName)));
	}

	private static String removeExtension (String s) {
		int extensionIndex = s.lastIndexOf(".");
		if (extensionIndex == -1) return s;

		return s.substring(0, extensionIndex);
	}

	private static String removePath (String s) {
		String temp;

		int index = s.lastIndexOf('\\');
		if (index != -1)
			temp = s.substring(index + 1);
		else
			temp = s;

		index = temp.lastIndexOf('/');
		if (index != -1)
			return s.substring(index + 1);
		else
			return s;
	}

	private static File getRelativeFile (File path, String relativePath) {
		if (relativePath.trim().length() == 0) return path;

		File child = path;

		StringTokenizer tokenizer = new StringTokenizer(relativePath, "\\/");
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			if (token.equals(".."))
				child = child.getParentFile();
			else {
				child = new File(child, token);
			}
		}

		return child;
	}

	private void setBlended (int tileNum) {
		blendedTiles.add(tileNum);
	}

	private void writeUpdatedTMX (TiledMap tiledMap, File outputDir, FileHandle tmxFileHandle) throws IOException {
		Document doc;
		DocumentBuilder docBuilder;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

		try {
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(tmxFileHandle.read());

			Node map = doc.getFirstChild();
			while (map.getNodeType() != Node.ELEMENT_NODE || map.getNodeName() != "map") {
				if ((map = map.getNextSibling()) == null) {
					throw new GdxRuntimeException("Couldn't find map node!");
				}
			}

			setProperty(doc, map, "blended tiles", toCSV(blendedTiles));

			// set used tileset atlases
			for (TiledMapTileSet tileset : tiledMap.getTileSets()) {
				String atlasName = removeExtension(tileset.getProperties().get("imagesource", String.class))+".atlas";
				setProperty(doc, map, "atlas_" + tileset.getName(), atlasName);
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			outputDir.mkdirs();
			StreamResult result = new StreamResult(new File(outputDir, tmxFileHandle.name()));
			transformer.transform(source, result);

		} catch (ParserConfigurationException e) {
			throw new RuntimeException("ParserConfigurationException: " + e.getMessage());
		} catch (SAXException e) {
			throw new RuntimeException("SAXException: " + e.getMessage());
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException("TransformerConfigurationException: " + e.getMessage());
		} catch (TransformerException e) {
			throw new RuntimeException("TransformerException: " + e.getMessage());
		}
	}

	private static void setProperty (Document doc, Node parent, String name, String value) {
		Node properties = getFirstChildNodeByName(parent, "properties");
		Node property = getFirstChildByNameAttrValue(properties, "property", "name", name);

		NamedNodeMap attributes = property.getAttributes();
		Node valueNode = attributes.getNamedItem("value");
		if (valueNode == null) {
			valueNode = doc.createAttribute("value");
			valueNode.setNodeValue(value);
			attributes.setNamedItem(valueNode);
		} else {
			valueNode.setNodeValue(value);
		}
	}

	private static String toCSV (ArrayList<Integer> values) {
		String temp = "";
		for (int i = 0; i < values.size() - 1; i++) {
			temp += values.get(i) + ",";
		}
		if (values.size() > 0) temp += values.get(values.size() - 1);
		return temp;
	}

	/** If the child node doesn't exist, it is created. */
	private static Node getFirstChildNodeByName (Node parent, String child) {
		NodeList childNodes = parent.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			if (childNodes.item(i).getNodeName().equals(child)) {
				return childNodes.item(i);
			}
		}

		Node newNode = parent.getOwnerDocument().createElement(child);

		if (childNodes.item(0) != null)
			return parent.insertBefore(newNode, childNodes.item(0));
		else
			return parent.appendChild(newNode);
	}

	/** If the child node or attribute doesn't exist, it is created. Usage example: Node property =
	 * getFirstChildByAttrValue(properties, "property", "name", "blended tiles"); */
	private static Node getFirstChildByNameAttrValue (Node node, String childName, String attr, String value) {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			if (childNodes.item(i).getNodeName().equals(childName)) {
				NamedNodeMap attributes = childNodes.item(i).getAttributes();
				Node attribute = attributes.getNamedItem(attr);
				if (attribute.getNodeValue().equals(value)) return childNodes.item(i);
			}
		}

		Node newNode = node.getOwnerDocument().createElement(childName);
		NamedNodeMap attributes = newNode.getAttributes();

		Attr nodeAttr = node.getOwnerDocument().createAttribute(attr);
		nodeAttr.setNodeValue(value);
		attributes.setNamedItem(nodeAttr);

		if (childNodes.item(0) != null) {
			return node.insertBefore(newNode, childNodes.item(0));
		} else {
			return node.appendChild(newNode);
		}
	}

	private static boolean isBlended (BufferedImage tile) {
		int[] rgbArray = new int[tile.getWidth() * tile.getHeight()];
		tile.getRGB(0, 0, tile.getWidth(), tile.getHeight(), rgbArray, 0, tile.getWidth());
		for (int i = 0; i < tile.getWidth() * tile.getHeight(); i++) {
			if (((rgbArray[i] >> 24) & 0xff) != 255) {
				return true;
			}
		}
		return false;
	}

	/** Processes a directory of Tile Maps, compressing each tile set contained in any map once.
	 * @param args args[0]: the input directory containing the tmx files (and tile sets, relative to the path listed in the tmx
	 *           file). args[1]: The output directory for the tmx files, should be empty before running. WARNING: Use caution if
	 *           you have a "../" in the path of your tile sets! The output for these tile sets will be relative to the output
	 *           directory. For example, if your output directory is "C:\mydir\output" and you have a tileset with the path
	 *           "../tileset.png", the tileset will be output to "C:\mydir\" and the maps will be in "C:\mydir\output". args[2]:
	 *           --strip-unused (optional, include to let the TiledMapPacker remove tiles which are not used. */
	static File inputDir;
	static File outputDir;

	public static void main (String[] args) {
		final Settings texturePackerSettings = new Settings();
		texturePackerSettings.paddingX = 2;
		texturePackerSettings.paddingY = 2;
		texturePackerSettings.duplicatePadding = true;

		final TiledMapPackerSettings packerSettings = new TiledMapPackerSettings();

		switch (args.length) {
		case 3: {
			inputDir = new File(args[0]);
			outputDir = new File(args[1]);
			if ("--strip-unused".equals(args[2])) {
				packerSettings.stripUnusedTiles = true;
			}
			break;
		}
		case 2: {
			inputDir = new File(args[0]);
			outputDir = new File(args[1]);
			break;
		}
		case 1: {
			inputDir = new File(args[0]);
			outputDir = new File(inputDir, "output/");
			break;
		}
		default: {
			System.out.println("Usage: INPUTDIR [OUTPUTDIR] [--strip-unused]");
			System.exit(0);
		}
		}

		new LwjglApplication(new ApplicationListener() {

			@Override
			public void resume () {
			}

			@Override
			public void resize (int width, int height) {
			}

			@Override
			public void render () {
			}

			@Override
			public void pause () {
			}

			@Override
			public void dispose () {
			}

			@Override
			public void create () {
				TiledMapPacker packer = new TiledMapPacker(packerSettings);

				if (!inputDir.exists()) {
					throw new RuntimeException("Input directory does not exist");
				}

				try {
					packer.processMap(inputDir, outputDir, texturePackerSettings);
				} catch (IOException e) {
					throw new RuntimeException("Error processing map: " + e.getMessage());
				}

				Gdx.app.exit();
			}
		}, "TiledMapPacker", 100, 50, true);
	}

	public static class TiledMapPackerSettings {
		public boolean stripUnusedTiles = false;
	}
}
