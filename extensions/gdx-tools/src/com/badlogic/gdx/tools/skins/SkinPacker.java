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
import com.badlogic.gdx.utils.ObjectMap;

public class SkinPacker {
	public static void main (String[] args) throws Exception {
		final File inputDir = new File("C:/Users/Nate/Desktop/skin");
		final File packedDir = new File("C:/Users/Nate/Desktop/skin/packed");

		new FileHandle(packedDir).deleteDirectory();

		final ObjectMap<String, int[]> nameToSplits = new ObjectMap();

		Settings settings = new Settings();
		settings.alias = true;
		settings.defaultFilterMag = TextureFilter.Linear;
		settings.defaultFilterMin = TextureFilter.Linear;
		settings.padding = 2;
		settings.pot = false;
		settings.stripWhitespace = false;
		final TexturePacker texturePacker = new TexturePacker(settings);
		FileProcessor processor = new FileProcessor() {
			protected void processFile (InputFile inputFile) throws Exception {
				BufferedImage image = ImageIO.read(inputFile.inputFile);
				image = getSplits(image, inputFile.outputFile.getName());
				texturePacker.addImage(image, inputFile.outputFile.getName());
			}

			private BufferedImage getSplits (BufferedImage image, String name) {
				System.out.println(name);
				int countX = 0, countY = 0;
				int[] splits = new int[4];
				int[] rgba = new int[4];
				WritableRaster raster = image.getRaster();
				for (int x = 0; x < raster.getWidth(); x++) {
					raster.getPixel(x, 0, rgba);
					if (rgba[0] == 255 && rgba[1] == 0 && rgba[2] == 255 && rgba[3] != 0) {
						countX++;
						if (countX > 2) return image;
						splits[countX - 1] = x - 1;
						continue;
					}
					if ((rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0) && rgba[3] != 0) return image;
				}
				if (countX != 2) return image;
				for (int y = 0; y < raster.getHeight(); y++) {
					raster.getPixel(0, y, rgba);
					if (rgba[0] == 255 && rgba[1] == 0 && rgba[2] == 255 && rgba[3] != 0) {
						countY++;
						if (countY > 2) return image;
						splits[countY - 1 + 2] = y - 1;
						continue;
					}
					if ((rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0) && rgba[3] != 0) return image;
				}
				if (countY != 2) return image;
				nameToSplits.put(name, splits);
				BufferedImage newImage = new BufferedImage(raster.getWidth() - 1, raster.getHeight() - 1,
					BufferedImage.TYPE_4BYTE_ABGR);
				Graphics g = newImage.getGraphics();
				g.drawImage(image, 0, 0, newImage.getWidth(), newImage.getHeight(), 1, 1, newImage.getWidth(), newImage.getHeight(),
					null);
				return newImage;
			}
		};
		processor.setRecursive(false);
		processor.addInputSuffix(".png");
		processor.setOutputSuffix("");
		processor.process(inputDir, inputDir);

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
				skin.save(new FileHandle(new File(inputDir, "skin")));
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
}