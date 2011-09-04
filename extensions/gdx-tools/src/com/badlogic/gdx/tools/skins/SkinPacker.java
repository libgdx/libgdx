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
package com.badlogic.gdx.tools.skins;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;

import javax.imageio.ImageIO;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.tools.FileProcessor;
import com.badlogic.gdx.tools.imagepacker.TexturePacker;
import com.badlogic.gdx.tools.imagepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class SkinPacker {
	static public void process (final File inputDir, final File packedDir, final File skinFile) throws Exception {
		Texture.setEnforcePotImages(false);

		new FileHandle(packedDir).deleteDirectory();

		final ObjectMap<String, int[]> nameToSplits = new ObjectMap();

		Settings settings = new Settings();
		settings.alias = true;
		settings.defaultFilterMag = TextureFilter.Linear;
		settings.defaultFilterMin = TextureFilter.Linear;
		settings.padding = 2;
		settings.pot = false;
		settings.duplicatePadding = false;
		settings.stripWhitespace = false;
		final TexturePacker texturePacker = new TexturePacker(settings);

		FileProcessor regionProcessor = new FileProcessor() {
			protected void processFile (InputFile inputFile) throws Exception {
				texturePacker.addImage(ImageIO.read(inputFile.inputFile), inputFile.outputFile.getName());
			}
		};
		regionProcessor.setRecursive(false);
		regionProcessor.setInputFilter(new FilenameFilter() {
			public boolean accept (File dir, String name) {
				return name.endsWith(".png") && !name.endsWith(".9.png");
			}
		});
		regionProcessor.setOutputSuffix("");
		regionProcessor.process(inputDir, inputDir);

		FileProcessor ninePatchProcessor = new FileProcessor() {
			protected void processFile (InputFile inputFile) throws Exception {
				BufferedImage image = ImageIO.read(inputFile.inputFile);
				String name = inputFile.outputFile.getName();
				name = name.substring(0, name.length() - 2);
				image = getSplits(image, name);
				texturePacker.addImage(image, name);
			}

			private BufferedImage getSplits (BufferedImage image, String name) {
				WritableRaster raster = image.getRaster();
				int[] rgba = new int[4];

				int startX = -1;
				for (int x = 1; x < raster.getWidth() - 1; x++) {
					raster.getPixel(x, 0, rgba);
					if (rgba[3] == 0) continue;
					if (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0)
						throw new RuntimeException("Unknown pixel:" + x + ",0: " + name);
					startX = x;
					break;
				}
				if (startX == -1) throw new RuntimeException("Missing marker pixels in first row of pixels: " + name);
				int endX;
				for (endX = startX; endX < raster.getWidth() - 1; endX++) {
					raster.getPixel(endX, 0, rgba);
					if (rgba[3] == 0) break;
					if (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0)
						throw new RuntimeException("Unknown pixel " + endX + ",0: " + name);
				}
				for (int x = endX + 1; x < raster.getWidth() - 1; x++) {
					raster.getPixel(x, 0, rgba);
					if (rgba[3] != 0) throw new RuntimeException("Unknown pixel " + x + ",0: " + name);
				}

				int startY = -1;
				for (int y = 1; y < raster.getHeight() - 1; y++) {
					raster.getPixel(0, y, rgba);
					if (rgba[3] == 0) continue;
					if (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0)
						throw new RuntimeException("Unknown pixel: 0," + y + ": " + name);
					startY = y;
					break;
				}
				if (startY == -1) throw new RuntimeException("Missing marker pixels in first column of pixels: " + name);
				int endY;
				for (endY = startY; endY < raster.getHeight() - 1; endY++) {
					raster.getPixel(0, endY, rgba);
					if (rgba[3] == 0) break;
					if (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0)
						throw new RuntimeException("Unknown pixel 0," + endY + ": " + name);
				}
				for (int y = endY + 1; y < raster.getHeight() - 1; y++) {
					raster.getPixel(0, y, rgba);
					if (rgba[3] != 0) throw new RuntimeException("Unknown pixel 0," + y + ": " + name);
				}

				int[] splits = new int[4];
				splits[0] = startX - 1;
				splits[1] = endX - 1;
				splits[2] = startY - 1;
				splits[3] = endY - 1;
				nameToSplits.put(name, splits);

				BufferedImage newImage = new BufferedImage(raster.getWidth() - 2, raster.getHeight() - 2,
					BufferedImage.TYPE_4BYTE_ABGR);
				newImage.getGraphics().drawImage(image, 0, 0, newImage.getWidth(), newImage.getHeight(), 1, 1, raster.getWidth() - 1,
					raster.getHeight() - 1, null);
				return newImage;
			}
		};
		ninePatchProcessor.setRecursive(false);
		ninePatchProcessor.addInputSuffix(".9.png");
		ninePatchProcessor.setOutputSuffix("");
		ninePatchProcessor.process(inputDir, inputDir);

		texturePacker.process(packedDir, new File(packedDir, "pack"), "skin");

		new LwjglApplication(new ApplicationListener() {
			public void create () {
				Skin skin = new Skin();
				TextureAtlasData atlas = new TextureAtlasData(new FileHandle(new File(packedDir, "pack")), new FileHandle(packedDir),
					true);
				Texture texture = new Texture(1, 1, Format.Alpha);
				for (Region region : atlas.getRegions()) {
					int[] split = nameToSplits.get(region.name);
					TextureRegion textureRegion = new TextureRegion(texture, region.left, region.top, region.width, region.height);
					if (split == null) {
						skin.addResource(region.name, textureRegion);
					} else {
						skin.addResource(region.name, new NinePatch(textureRegion, split[0], region.width - split[1], split[2],
							region.height - split[3]));
					}
				}
				FileHandle newSkinFile = new FileHandle(new File(inputDir, "skin"));
				skin.save(newSkinFile);

				Json json = new Json();
				ObjectMap oldSkin = json.fromJson(ObjectMap.class, new FileHandle(skinFile));
				ObjectMap newSkin = json.fromJson(ObjectMap.class, newSkinFile);
				newSkin.put("styles", oldSkin.get("styles"));
				Writer writer = newSkinFile.writer(false);
				try {
					writer.write(json.prettyPrint(newSkin, true));
					writer.close();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}

				System.exit(0);
			}

			public void resume () {
			}

			public void resize (int width, int height) {
			}

			public void render () {
			}

			public void pause () {
			}

			public void dispose () {
			}
		}, "SkinPacker", 1, 1, false);
	}

	static public void main (String[] args) throws Exception {
		File inputDir = new File("C:/Users/Nate/Desktop/skin");
		File packedDir = new File("C:/Users/Nate/Desktop/skin/packed");
		File skinFile = new File("C:/Users/Nate/Desktop/skin/old-skin.json");
		// skinFile = null;
		process(inputDir, packedDir, skinFile);
	}
}
