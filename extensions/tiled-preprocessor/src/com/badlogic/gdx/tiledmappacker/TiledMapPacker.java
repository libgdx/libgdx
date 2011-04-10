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
import com.badlogic.gdx.graphics.g2d.tiled.TileSet;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLayer;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.imagepacker.TexturePacker;
import com.badlogic.gdx.imagepacker.TexturePacker.Settings;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Packs a Tiled Map, adding some properties to improve the speed of the {@link TiledMapRenderer}. Also runs the texture packer on
 * the tiles for use with a {@link TileAtlas}
 * @author David Fraska
 */
public class TiledMapPacker {

	private TexturePacker packer;
	private TiledMap map;
	private int tileCount = 0;

	private File outputDir;
	private FileHandle tmxFileHandle;
	private FileHandle imageDirHandle;

	private ArrayList<Integer> blendedTiles = new ArrayList<Integer>();

	/**
	 * Packs a Tiled Map, adding some properties to improve the speed of the {@link TiledMapRenderer}. Also runs the texture packer
	 * on the tiles for use with a {@link TileAtlas}
	 * @param tmxFile the map's tmx file
	 * @param imageDir the directory containing tile set images
	 * @param outputDir the directory to output a fully processed map to
	 * @param settings the settings used in the TexturePacker
	 * */
	public void processMap (File tmxFile, File imageDir, File outputDir, Settings settings) throws IOException {
		this.outputDir = outputDir;

		tmxFileHandle = Gdx.files.absolute(tmxFile.getAbsolutePath());
		imageDirHandle = Gdx.files.absolute(imageDir.getAbsolutePath());

		new File(outputDir, tmxFileHandle.name()).delete();
		if (outputDir.exists()) {
			String prefix = tmxFileHandle.nameWithoutExtension();
			for (File file : outputDir.listFiles())
				if (file.getName().startsWith(prefix)) file.delete();
		}

		map = TiledLoader.createMap(tmxFileHandle);

		packMap(map, settings);
	}

	private void packMap (TiledMap map, Settings settings) throws IOException {
		packer = new TexturePacker(settings);

		BufferedImage tile;
		Vector2 tileLocation;
		TileSetLayout packerTileSet;
		Graphics g;

		ArrayList<Integer> tilesOnMap = new ArrayList<Integer>();

		// Loop through all tiles on map
		for (TiledLayer layer : map.layers) {
			for (int row = 0; row < layer.height; row++) {
				for (int col = 0; col < layer.width; col++) {
					if (layer.tile[row][col] != 0) {
						tileCount++;
						if (!tilesOnMap.contains(layer.tile[row][col])) {
							tilesOnMap.add(layer.tile[row][col]);
						}
					}
				}
			}
		}

		for (int i = 0; i < tilesOnMap.size(); i++) {
			//FIXME: this is kind of brute force. We don't know when a tileSet is going to change, so we just assume
			//it changes every tile. Should probably keep track of how many tiles are in the tileSet and use that.
			packerTileSet = getTileSetLayout(tilesOnMap.get(i)); 
			tileLocation = packerTileSet.getLocation(tilesOnMap.get(i));
			tile = new BufferedImage(packerTileSet.tileSet.tileWidth, packerTileSet.tileSet.tileHeight,
				BufferedImage.TYPE_4BYTE_ABGR);

			g = tile.createGraphics();
			g.drawImage(packerTileSet.image, 0, 0, packerTileSet.tileSet.tileWidth, packerTileSet.tileSet.tileHeight,
				(int)tileLocation.x, (int)tileLocation.y, (int)tileLocation.x + packerTileSet.tileSet.tileWidth, (int)tileLocation.y
					+ packerTileSet.tileSet.tileHeight, null);

			if (isBlended(tile)) setBlended(tilesOnMap.get(i));

			packer.addImage(tile, map.tmxFile.nameWithoutExtension() + "_" + tilesOnMap.get(i));
		}

		packer.process(outputDir, new File(outputDir, map.tmxFile.nameWithoutExtension() + " packfile"),
			tmxFileHandle.nameWithoutExtension());
		writeUpdatedTMX();
	}

	private void setBlended (int tileNum) {
		blendedTiles.add(tileNum);
		// System.out.println("TileNum " + tileNum + " is blended");
	}

	private void writeUpdatedTMX () throws IOException {
		Document doc;
		DocumentBuilder docBuilder;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

		try {
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(tmxFileHandle.read());
			
			Node map = doc.getFirstChild();
			while(map.getNodeType() != Node.ELEMENT_NODE || map.getNodeName() != "map"){
				if((map = map.getNextSibling()) == null){
					throw new GdxRuntimeException("Couldn't find map node!");
				}
			}
			
			setProperty(doc, map, "blended tiles", toCSV(blendedTiles));
			setProperty(doc, map, "tile count", String.valueOf(tileCount));

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

	private TileSetLayout getTileSetLayout (int tileNum) throws IOException {
		int firstgid = 0;
		int lastgid;

		for (TileSet set : map.tileSets) {
			TileSetLayout layout = new TileSetLayout(set, imageDirHandle);
			firstgid = set.firstgid;
			lastgid = firstgid + layout.numTiles - 1;
			if (tileNum >= firstgid && tileNum <= lastgid) {
				return layout;
			}
		}

		return null;
	}

	public static void main (String[] args) {
		File tmxFile, baseDir, outputDir;

		Settings settings = new Settings();
		settings.padding = 2;
		settings.duplicatePadding = true;

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

		if (args.length != 3) {
			System.out.println("Usage: TMXFILE BASEDIR OUTPUTDIR");
			return;
		}

		tmxFile = new File(args[0]);
		baseDir = new File(args[1]);
		outputDir = new File(args[2]);

		if (!baseDir.exists()) {
			throw new RuntimeException("Base directory does not exist");
		}
		if (!tmxFile.exists()) {
			throw new RuntimeException("TMX file does not exist");
		}

		try {
			packer.processMap(tmxFile, baseDir, outputDir, settings);
		} catch (IOException e) {
			throw new RuntimeException("Error processing map: " + e.getMessage());
		}

		System.exit(0);
	}
}
