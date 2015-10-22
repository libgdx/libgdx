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
import java.util.HashMap;
import java.util.Iterator;

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
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;

/** Given one or more TMX tilemaps, packs all tileset resources used across the maps, or the resources used per map, into a single,
 * or multiple (one per map), {@link TextureAtlas} and produces a new TMX file to be loaded with an AtlasTiledMapLoader loader.
 * Optionally, it can keep track of unused tiles and omit them from the generated atlas, reducing the resource size.
 * 
 * The original TMX map file will be parsed by using the {@link TmxMapLoader} loader, thus access to a valid OpenGL context is
 * <b>required</b>, that's why an LwjglApplication is created by this preprocessor.
 * 
 * The new TMX map file will contains a new property, namely "atlas", whose value will enable the AtlasTiledMapLoader to correctly
 * read the associated TextureAtlas representing the tileset.
 * 
 * @author David Fraska and others (initial implementation, tell me who you are!)
 * @author Manuel Bua */
public class TiledMapPacker {
	private TexturePacker packer;
	private TiledMap map;

	private TmxMapLoader mapLoader = new TmxMapLoader(new AbsoluteFileHandleResolver());
	private TiledMapPackerSettings settings;

	private static final String TilesetsOutputDir = "tileset";
	static String AtlasOutputName = "packed";

	private HashMap<String, IntArray> tilesetUsedIds = new HashMap<String, IntArray>();
	private ObjectMap<String, TiledMapTileSet> tilesetsToPack;

	static File inputDir;
	static File outputDir;
	private FileHandle currentDir;

	private static class TmxFilter implements FilenameFilter {
		public TmxFilter () {
		}

		@Override
		public boolean accept (File dir, String name) {
			return (name.endsWith(".tmx"));
		}
	}

	private static class DirFilter implements FilenameFilter {
		public DirFilter () {
		}

		@Override
		public boolean accept (File f, String s) {
			return (new File(f, s).isDirectory());
		}
	}

	/** Constructs a new preprocessor by using the default packing settings */
	public TiledMapPacker () {
		this(new TiledMapPackerSettings());
	}

	/** Constructs a new preprocessor by using the specified packing settings */
	public TiledMapPacker (TiledMapPackerSettings settings) {
		this.settings = settings;
	}

	/** You can either run the {@link TiledMapPacker#main(String[])} method or reference this class in your own project and call
	 * this method. If working with libGDX sources, you can also run this file to create a run configuration then export it as a
	 * Runnable Jar. To run from a nightly build:
	 * 
	 * <code> <br><br>
	 * Linux / OS X <br>
	   java -cp gdx.jar:gdx-natives.jar:gdx-backend-lwjgl.jar:gdx-backend-lwjgl-natives.jar:gdx-tiled-preprocessor.jar:extensions/gdx-tools/gdx-tools.jar
	    com.badlogic.gdx.tiledmappacker.TiledMapPacker inputDir [outputDir]  [--strip-unused] [--combine-tilesets] [-v]
	 * <br><br>
	 * 
	 * Windows <br>
	   java -cp gdx.jar;gdx-natives.jar;gdx-backend-lwjgl.jar;gdx-backend-lwjgl-natives.jar;gdx-tiled-preprocessor.jar;extensions/gdx-tools/gdx-tools.jar
	    com.badlogic.gdx.tiledmappacker.TiledMapPacker inputDir [outputDir]  [--strip-unused] [--combine-tilesets] [-v]
	 * <br><br> </code>
	 * 
	 * Keep in mind that this preprocessor will need to load the maps by using the {@link TmxMapLoader} loader and this in turn
	 * will need a valid OpenGL context to work.
	 * 
	 * Process a directory containing TMX map files representing Tiled maps and produce multiple, or a single, TextureAtlas as well
	 * as new processed TMX map files, correctly referencing the generated {@link TextureAtlas} by using the "atlas" custom map
	 * property. */
	public void processInputDir (Settings texturePackerSettings) throws IOException {
		FileHandle inputDirHandle = new FileHandle(inputDir.getCanonicalPath());
		File[] mapFilesInCurrentDir = inputDir.listFiles(new TmxFilter());
		tilesetsToPack = new ObjectMap<String, TiledMapTileSet>();

		// Processes the maps inside inputDir
		for (File mapFile : mapFilesInCurrentDir) {
			processSingleMap(mapFile, inputDirHandle, texturePackerSettings);
		}

		processSubdirectories(inputDirHandle, texturePackerSettings);

		boolean combineTilesets = this.settings.combineTilesets;
		if (combineTilesets == true) {
			packTilesets(inputDirHandle, texturePackerSettings);
		}
	}

	/** Looks for subdirectories inside parentHandle, processes maps in subdirectory, repeat.
	 * @param currentDir The directory to look for maps and other directories
	 * @throws IOException */
	private void processSubdirectories (FileHandle currentDir, Settings texturePackerSettings) throws IOException {
		File parentPath = new File(currentDir.path());
		File[] directories = parentPath.listFiles(new DirFilter());

		for (File directory : directories) {
			currentDir = new FileHandle(directory.getCanonicalPath());
			File[] mapFilesInCurrentDir = directory.listFiles(new TmxFilter());

			for (File mapFile : mapFilesInCurrentDir) {
				processSingleMap(mapFile, currentDir, texturePackerSettings);
			}

			processSubdirectories(currentDir, texturePackerSettings);
		}
	}

	private void processSingleMap (File mapFile, FileHandle dirHandle, Settings texturePackerSettings) throws IOException {
		boolean combineTilesets = this.settings.combineTilesets;
		if (combineTilesets == false) {
			tilesetUsedIds = new HashMap<String, IntArray>();
			tilesetsToPack = new ObjectMap<String, TiledMapTileSet>();
		}

		map = mapLoader.load(mapFile.getCanonicalPath());

		// if enabled, build a list of used tileids for the tileset used by this map
		boolean stripUnusedTiles = this.settings.stripUnusedTiles;
		if (stripUnusedTiles) {
			stripUnusedTiles();
		} else {
			for (TiledMapTileSet tileset : map.getTileSets()) {
				String tilesetName = tileset.getName();
				if (!tilesetsToPack.containsKey(tilesetName)) {
					tilesetsToPack.put(tilesetName, tileset);
				}
			}
		}

		if (combineTilesets == false) {
			FileHandle tmpHandle = new FileHandle(mapFile.getName());
			this.settings.atlasOutputName = tmpHandle.nameWithoutExtension();

			packTilesets(dirHandle, texturePackerSettings);
		}

		FileHandle tmxFile = new FileHandle(mapFile.getCanonicalPath());
		writeUpdatedTMX(map, tmxFile);
	}

	private void stripUnusedTiles () {
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
							TiledMapTile tile = tlayer.getCell(x, y).getTile();
							if (tile instanceof AnimatedTiledMapTile) {
								AnimatedTiledMapTile aTile = (AnimatedTiledMapTile)tile;
								for (StaticTiledMapTile t : aTile.getFrameTiles()) {
									addTile(t, bucketSize);
								}
							}
							// Adds non-animated tiles and the base animated tile
							addTile(tile, bucketSize);
						}
					}
				}
			}
		}
	}

	private void addTile (TiledMapTile tile, int bucketSize) {
		int tileid = tile.getId() & ~0xE0000000;
		String tilesetName = tilesetNameFromTileId(map, tileid);
		IntArray usedIds = getUsedIdsBucket(tilesetName, bucketSize);
		usedIds.add(tileid);

		// track this tileset to be packed if not already tracked
		if (!tilesetsToPack.containsKey(tilesetName)) {
			tilesetsToPack.put(tilesetName, map.getTileSets().getTileSet(tilesetName));
		}
	}

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

	/** Returns the usedIds bucket for the given tileset name. If it doesn't exist one will be created with the specified size if
	 * its > 0, else null will be returned.
	 * 
	 * @param size The size to use to create a new bucket if it doesn't exist, else specify 0 or lower to return null instead
	 * @return a bucket */
	private IntArray getUsedIdsBucket (String tilesetName, int size) {
		if (tilesetUsedIds.containsKey(tilesetName)) {
			return tilesetUsedIds.get(tilesetName);
		}

		if (size <= 0) {
			return null;
		}

		IntArray bucket = new IntArray(size);
		tilesetUsedIds.put(tilesetName, bucket);
		return bucket;
	}

	/** Traverse the specified tilesets, optionally lookup the used ids and pass every tile image to the {@link TexturePacker},
	 * optionally ignoring unused tile ids */
	private void packTilesets (FileHandle inputDirHandle, Settings texturePackerSettings) throws IOException {
		BufferedImage tile;
		Vector2 tileLocation;
		Graphics g;

		packer = new TexturePacker(texturePackerSettings);

		for (TiledMapTileSet set : tilesetsToPack.values()) {
			String tilesetName = set.getName();
			System.out.println("Processing tileset " + tilesetName);

			IntArray usedIds = this.settings.stripUnusedTiles ? getUsedIdsBucket(tilesetName, -1) : null;

			int tileWidth = set.getProperties().get("tilewidth", Integer.class);
			int tileHeight = set.getProperties().get("tileheight", Integer.class);
			int firstgid = set.getProperties().get("firstgid", Integer.class);
			String imageName = set.getProperties().get("imagesource", String.class);

			TileSetLayout layout = new TileSetLayout(firstgid, set, inputDirHandle);

			for (int gid = layout.firstgid, i = 0; i < layout.numTiles; gid++, i++) {
				boolean verbose = this.settings.verbose;

				if (usedIds != null && !usedIds.contains(gid)) {
					if (verbose) {
						System.out.println("Stripped id #" + gid + " from tileset \"" + tilesetName + "\"");
					}
					continue;
				}

				tileLocation = layout.getLocation(gid);
				tile = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_4BYTE_ABGR);

				g = tile.createGraphics();
				g.drawImage(layout.image, 0, 0, tileWidth, tileHeight, (int)tileLocation.x, (int)tileLocation.y, (int)tileLocation.x
					+ tileWidth, (int)tileLocation.y + tileHeight, null);

				if (verbose) {
					System.out.println("Adding " + tileWidth + "x" + tileHeight + " (" + (int)tileLocation.x + ", "
						+ (int)tileLocation.y + ")");
				}
				// AtlasTmxMapLoader expects every tileset's index to begin at zero for the first tile in every tileset.
				// so the region's adjusted gid is (gid - layout.firstgid). firstgid will be added back in AtlasTmxMapLoader on load
				int adjustedGid = gid - layout.firstgid;
				final String separator = "_";
				String regionName = tilesetName + separator + adjustedGid;

				packer.addImage(tile, regionName);
			}
		}
		String tilesetOutputDir = outputDir.toString() + "/" + this.settings.tilesetOutputDirectory;
		File relativeTilesetOutputDir = new File(tilesetOutputDir);
		File outputDirTilesets = new File(relativeTilesetOutputDir.getCanonicalPath());

		outputDirTilesets.mkdirs();
		packer.pack(outputDirTilesets, this.settings.atlasOutputName + ".atlas");
	}

	private void writeUpdatedTMX (TiledMap tiledMap, FileHandle tmxFileHandle) throws IOException {
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

			setProperty(doc, map, "atlas", settings.tilesetOutputDirectory + "/" + settings.atlasOutputName + ".atlas");

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
	 * getFirstChildByAttrValue(properties, "property", "name"); */
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

	/** Processes a directory of Tile Maps, compressing each tile set contained in any map once.
	 * 
	 * @param args args[0]: the input directory containing the tmx files (and tile sets, relative to the path listed in the tmx
	 *           file). args[1]: The output directory for the tmx files, should be empty before running. args[2-4] options */
	public static void main (String[] args) {
		final Settings texturePackerSettings = new Settings();
		texturePackerSettings.paddingX = 2;
		texturePackerSettings.paddingY = 2;
		texturePackerSettings.edgePadding = true;
		texturePackerSettings.duplicatePadding = true;
		texturePackerSettings.bleed = true;
		texturePackerSettings.alias = true;
		texturePackerSettings.useIndexes = true;

		final TiledMapPackerSettings packerSettings = new TiledMapPackerSettings();

		if (args.length == 0) {
			printUsage();
			System.exit(0);
		} else if (args.length == 1) {
			inputDir = new File(args[0]);
			outputDir = new File(inputDir, "../output/");
		} else if (args.length == 2) {
			inputDir = new File(args[0]);
			outputDir = new File(args[1]);
		} else {
			inputDir = new File(args[0]);
			outputDir = new File(args[1]);
			processExtraArgs(args, packerSettings);
		}

		TiledMapPacker packer = new TiledMapPacker(packerSettings);
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.forceExit = false;
		config.width = 100;
		config.height = 50;
		config.title = "TiledMapPacker";
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
					System.out.println(inputDir.getAbsolutePath());
					throw new RuntimeException("Input directory does not exist: " + inputDir);
				}

				try {
					packer.processInputDir(texturePackerSettings);
				} catch (IOException e) {
					throw new RuntimeException("Error processing map: " + e.getMessage());
				}
				System.out.println("Finished processing.");
				Gdx.app.exit();
			}
		}, config);
	}

	private static void processExtraArgs (String[] args, TiledMapPackerSettings packerSettings) {
		String stripUnused = "--strip-unused";
		String combineTilesets = "--combine-tilesets";
		String verbose = "-v";

		int length = args.length - 2;
		String[] argsNotDir = new String[length];
		System.arraycopy(args, 2, argsNotDir, 0, length);

		for (String string : argsNotDir) {
			if (stripUnused.equals(string)) {
				packerSettings.stripUnusedTiles = true;

			} else if (combineTilesets.equals(string)) {
				packerSettings.combineTilesets = true;

			} else if (verbose.equals(string)) {
				packerSettings.verbose = true;

			} else {
				System.out.println("\nOption \"" + string + "\" not recognized.\n");
				printUsage();
				System.exit(0);
			}
		}
	}

	private static void printUsage () {
		System.out.println("Usage: INPUTDIR [OUTPUTDIR] [--strip-unused] [--combine-tilesets] [-v]");
		System.out.println("Processes a directory of Tiled .tmx maps. Unable to process maps with XML");
		System.out.println("tile layer format.");
		System.out.println("  --strip-unused             omits all tiles that are not used. Speeds up");
		System.out.println("                             the processing. Smaller tilesets.");
		System.out.println("  --combine-tilesets         instead of creating a tileset for each map,");
		System.out.println("                             this combines the tilesets into some kind");
		System.out.println("                             of monster tileset. Has problems with tileset");
		System.out.println("                             location. Has problems with nested folders.");
		System.out.println("                             Not recommended.");
		System.out.println("  -v                         outputs which tiles are stripped and included");
		System.out.println();
	}

	public static class TiledMapPackerSettings {
		public boolean stripUnusedTiles = false;
		public boolean combineTilesets = false;
		public boolean verbose = false;
		public String tilesetOutputDirectory = TilesetsOutputDir;
		public String atlasOutputName = AtlasOutputName;
	}
}
