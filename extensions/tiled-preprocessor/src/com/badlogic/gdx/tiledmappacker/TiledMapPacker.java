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
package com.badlogic.gdx.tiledmappacker;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

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
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
import com.badlogic.gdx.graphics.g2d.tiled.TileMapRenderer;
import com.badlogic.gdx.graphics.g2d.tiled.TileSet;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLayer;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.imagepacker.TexturePacker;
import com.badlogic.gdx.imagepacker.TexturePacker.Settings;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Packs a Tiled Map, adding some properties to improve the speed of the {@link TileMapRenderer}. Also runs the texture packer on
 * the tiles for use with a {@link TileAtlas}
 * @author David Fraska
 */
public class TiledMapPacker {

	private TexturePacker packer;
	private TiledMap map;

	private File outputDir;
	private ArrayList<String> processedTileSets = new ArrayList<String>();

	private ArrayList<Integer> blendedTiles = new ArrayList<Integer>();

	static private class TmxFilter implements FilenameFilter {

		public TmxFilter () {
		}

		@Override public boolean accept (File dir, String name) {
			if (name.endsWith(".tmx")) return true;

			return false;
		}

	}

	/**
	 * Typically, you should run the {@link TiledMapPacker#main(String[])} method instead of this method.
	 * Packs a directory of Tiled Maps, adding properties to improve the speed of the {@link TileMapRenderer}. Also runs the
	 * texture packer on the tile sets for use with a {@link TileAtlas}
	 * @param inputDir the directory containing tile set images and tmx files
	 * @param outputDir the directory to output a fully processed map to
	 * @param settings the settings used in the TexturePacker
	 * */
	public void processMap (File inputDir, File outputDir, Settings settings) throws IOException {
		this.outputDir = outputDir;

		FileHandle inputDirHandle = Gdx.files.absolute(inputDir.getAbsolutePath());
		File[] files = inputDir.listFiles(new TmxFilter());

		for (File file : files) {
			map = TiledLoader.createMap(Gdx.files.absolute(file.getAbsolutePath()));

			for (TileSet set : map.tileSets) {
				if (!processedTileSets.contains(set.imageName)) {
					processedTileSets.add(set.imageName);
					packTileSet(set, inputDirHandle, settings);
				}
			}

			writeUpdatedTMX(map.tmxFile);
		}
	}

	private void packTileSet (TileSet set, FileHandle inputDirHandle, Settings settings) throws IOException {
		BufferedImage tile;
		Vector2 tileLocation;
		TileSetLayout packerTileSet;
		Graphics g;

		packer = new TexturePacker(settings);

		TileSetLayout layout = new TileSetLayout(set, inputDirHandle);

		for (int gid = layout.firstgid, i = 0; i < layout.numTiles; gid++, i++) {
			tileLocation = layout.getLocation(gid);
			tile = new BufferedImage(layout.tileWidth, layout.tileHeight, BufferedImage.TYPE_4BYTE_ABGR);

			g = tile.createGraphics();
			g.drawImage(layout.image, 0, 0, layout.tileWidth, layout.tileHeight, (int)tileLocation.x, (int)tileLocation.y,
				(int)tileLocation.x + layout.tileWidth, (int)tileLocation.y + layout.tileHeight, null);

			if (isBlended(tile)) setBlended(gid);

			packer.addImage(tile, removeExtension(set.imageName) + "_" + i);
		}

		packer
			.process(outputDir, new File(outputDir, removeExtension(set.imageName) + " packfile"), removeExtension(set.imageName));
	}

	private static String removeExtension (String s) {

		String separator = System.getProperty("file.separator");
		String filename;

		// Remove the path up to the filename.
		int lastSeparatorIndex = s.lastIndexOf(separator);
		if (lastSeparatorIndex == -1) {
			filename = s;
		} else {
			filename = s.substring(lastSeparatorIndex + 1);
		}

		// Remove the extension.
		int extensionIndex = filename.lastIndexOf(".");
		if (extensionIndex == -1) return filename;

		return filename.substring(0, extensionIndex);
	}

	private void setBlended (int tileNum) {
		blendedTiles.add(tileNum);
	}

	private void writeUpdatedTMX (FileHandle tmxFileHandle) throws IOException {
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

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
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

	/**
	 * If the child node or attribute doesn't exist, it is created. Usage example: Node property =
	 * getFirstChildByAttrValue(properties, "property", "name", "blended tiles");
	 */
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

	private TileSetLayout getTileSetLayout (int tileNum, FileHandle inputDirHandle) throws IOException {
		int firstgid = 0;
		int lastgid;

		for (TileSet set : map.tileSets) {
			TileSetLayout layout = new TileSetLayout(set, inputDirHandle);
			firstgid = set.firstgid;
			lastgid = firstgid + layout.numTiles - 1;
			if (tileNum >= firstgid && tileNum <= lastgid) {
				return layout;
			}
		}

		return null;
	}

	/**
	 * Processes a 
	 * @param args args[0]: the input directory containing tmx files and tile set images.
	 * 				args[1]: The output directory, should be empty before running.
	 */
	public static void main (String[] args) {
		File tmxFile, inputDir, outputDir;

		Settings settings = new Settings();
		settings.padding = 2;
		settings.duplicatePadding = true;
		settings.incremental = true;
		settings.alias = true;

		// Create a new JoglApplication so that Gdx stuff works properly
		new JoglApplication(new ApplicationListener() {
			@Override public void create () {
			}

			@Override public void dispose () {
			}

			@Override public void pause () {
			}

			@Override public void render () {
			}

			@Override public void resize (int width, int height) {
			}

			@Override public void resume () {
			}
		}, "", 0, 0, false);

		TiledMapPacker packer = new TiledMapPacker();

		if (args.length != 2) {
			System.out.println("Usage: INPUTDIR OUTPUTDIR");
			System.exit(0);
		}

		inputDir = new File(args[0]);
		outputDir = new File(args[1]);

		if (!inputDir.exists()) {
			throw new RuntimeException("Input directory does not exist");
		}

		try {
			packer.processMap(inputDir, outputDir, settings);
		} catch (IOException e) {
			throw new RuntimeException("Error processing map: " + e.getMessage());
		}

		System.exit(0);
	}
}
