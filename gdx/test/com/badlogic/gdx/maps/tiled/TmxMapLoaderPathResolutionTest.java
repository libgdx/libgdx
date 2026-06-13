
package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/** Headless regression tests for TMX/TSX tileset path resolution.
 *
 * <p>
 * Verifies that per-tile image paths declared inside a .tsx file are resolved relative to the .tsx file, not the .tmx map file
 * (regression for GitHub #7818). */
public class TmxMapLoaderPathResolutionTest {

	@Rule public TemporaryFolder tmp = new TemporaryFolder();

	// --- helpers ---

	/** ImageResolver that records every path it is queried for. Returns null textures (callers must ensure addStaticTiledMapTile
	 * is a no-op). */
	static class RecordingImageResolver implements ImageResolver {
		final List<String> requestedPaths = new ArrayList<>();

		@Override
		public TextureRegion getImage (String name) {
			requestedPaths.add(name);
			return null; // no GPU in unit tests
		}
	}

	/** TmxMapLoader that skips actual tile construction so null TextureRegions are safe, and skips property loading that requires
	 * Gdx.app. */
	static class HeadlessTmxMapLoader extends TmxMapLoader {
		@Override
		protected void addStaticTiledMapTile (TiledMapTileSet tileSet, TextureRegion textureRegion, int tileId, float offsetX,
			float offsetY) {
			// no-op: avoids NPE on null TextureRegion in headless tests
			tileSet.putTile(tileId, new StaticTiledMapTile((TextureRegion)null));
		}

		@Override
		protected void addTileProperties (TiledMapTile tile, com.badlogic.gdx.utils.XmlReader.Element tileElement) {
			// no-op: avoids Gdx.app.log() call in loadMapPropertiesClassDefaults
		}
	}

	// --- tests ---

	/** External TSX with collection-of-images tiles in a sub-directory.
	 *
	 * <pre>
	 *   maps/
	 *     test.tmx          ← references ../ts/sprites.tsx
	 *   ts/
	 *     sprites.tsx       ← tile image source="../img/tile.png"
	 *   ts/img/
	 *     tile.png          ← correct resolved location
	 * </pre>
	 *
	 * Before the fix, addStaticTiles resolved images relative to maps/ (wrong). After the fix, it resolves them relative to ts/
	 * (correct). */
	@Test
	public void externalTsxTileImagesResolvedRelativeToTsxFile () throws Exception {
		// set up directory layout
		File mapsDir = tmp.newFolder("maps");
		File tsDir = tmp.newFolder("ts");
		File tsImgDir = new File(tsDir, "img");
		tsImgDir.mkdirs();

		// write sprites.tsx (collection of images: one tile with image source relative to ts/)
		File tsxFile = new File(tsDir, "sprites.tsx");
		try (PrintWriter w = new PrintWriter(tsxFile)) {
			w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			w.println("<tileset name=\"sprites\" tilewidth=\"16\" tileheight=\"16\" tilecount=\"1\">");
			w.println("  <tile id=\"0\">");
			w.println("    <image source=\"img/tile.png\" width=\"16\" height=\"16\"/>");
			w.println("  </tile>");
			w.println("</tileset>");
		}

		// write test.tmx referencing ../ts/sprites.tsx
		File tmxFile = new File(mapsDir, "test.tmx");
		try (PrintWriter w = new PrintWriter(tmxFile)) {
			w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			w.println("<map version=\"1.10\" orientation=\"orthogonal\" renderorder=\"right-down\"");
			w.println("     width=\"1\" height=\"1\" tilewidth=\"16\" tileheight=\"16\" infinite=\"0\"");
			w.println("     nextlayerid=\"2\" nextobjectid=\"1\">");
			w.println("  <tileset firstgid=\"1\" source=\"../ts/sprites.tsx\"/>");
			w.println("  <layer id=\"1\" name=\"Tile Layer 1\" width=\"1\" height=\"1\">");
			w.println("    <data encoding=\"csv\">0</data>");
			w.println("  </layer>");
			w.println("</map>");
		}

		// also create the expected image file so FileHandle.exists() passes if needed
		new File(tsImgDir, "tile.png").createNewFile();

		FileHandle tmxHandle = new FileHandle(tmxFile);
		RecordingImageResolver resolver = new RecordingImageResolver();
		HeadlessTmxMapLoader loader = new HeadlessTmxMapLoader();

		// parse TMX, extract the tileset reference element, and load it
		loader.root = loader.xml.parse(tmxHandle);
		com.badlogic.gdx.utils.XmlReader.Element tilesetRef = loader.root.getChildByName("tileset");
		loader.loadTileSet(tilesetRef, tmxHandle, resolver);

		// the resolver must have been called exactly once
		assertEquals("Expected exactly one tile image to be resolved", 1, resolver.requestedPaths.size());

		String resolvedPath = resolver.requestedPaths.get(0).replace(File.separatorChar, '/');
		String expectedSuffix = "ts/img/tile.png";

		assertTrue("Image path should be resolved relative to the .tsx file (ts/img/tile.png), " + "but was: " + resolvedPath,
			resolvedPath.endsWith(expectedSuffix));

		// explicitly verify it was NOT resolved relative to the .tmx file
		assertFalse("Image path must NOT be resolved relative to the .tmx file (maps/img/tile.png)",
			resolvedPath.endsWith("maps/img/tile.png"));
	}

	/** Embedded tileset (no external .tsx) — image path stays relative to the .tmx file. This ensures the fallback path is not
	 * broken by the fix. */
	@Test
	public void embeddedTilesetImagesResolvedRelativeToTmxFile () throws Exception {
		File mapsDir = tmp.newFolder("maps2");
		File mapsImgDir = new File(mapsDir, "img");
		mapsImgDir.mkdirs();

		File tmxFile = new File(mapsDir, "embedded.tmx");
		try (PrintWriter w = new PrintWriter(tmxFile)) {
			w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			w.println("<map version=\"1.10\" orientation=\"orthogonal\" renderorder=\"right-down\"");
			w.println("     width=\"1\" height=\"1\" tilewidth=\"16\" tileheight=\"16\" infinite=\"0\"");
			w.println("     nextlayerid=\"2\" nextobjectid=\"1\">");
			w.println("  <tileset firstgid=\"1\" name=\"embedded\" tilewidth=\"16\" tileheight=\"16\" tilecount=\"1\">");
			w.println("    <tile id=\"0\">");
			w.println("      <image source=\"img/tile.png\" width=\"16\" height=\"16\"/>");
			w.println("    </tile>");
			w.println("  </tileset>");
			w.println("  <layer id=\"1\" name=\"Tile Layer 1\" width=\"1\" height=\"1\">");
			w.println("    <data encoding=\"csv\">0</data>");
			w.println("  </layer>");
			w.println("</map>");
		}

		new File(mapsImgDir, "tile.png").createNewFile();

		FileHandle tmxHandle = new FileHandle(tmxFile);
		RecordingImageResolver resolver = new RecordingImageResolver();
		HeadlessTmxMapLoader loader = new HeadlessTmxMapLoader();

		loader.root = loader.xml.parse(tmxHandle);
		com.badlogic.gdx.utils.XmlReader.Element tilesetRef = loader.root.getChildByName("tileset");
		loader.loadTileSet(tilesetRef, tmxHandle, resolver);

		assertEquals("Expected exactly one tile image to be resolved", 1, resolver.requestedPaths.size());

		String resolvedPath = resolver.requestedPaths.get(0).replace(File.separatorChar, '/');
		String expectedSuffix = "maps2/img/tile.png";

		assertTrue("Embedded tileset image should be resolved relative to the .tmx file, " + "but was: " + resolvedPath,
			resolvedPath.endsWith(expectedSuffix));
	}
}
