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

package com.badlogic.gdx.tools.texturepacker;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;

/** @author Nathan Sweet, Michael Bazos */
public class TextureUnpacker {
	/** Will unpack the given pack file name into individual files where the output directory is specified.
	 * @param packFileName
	 * @param outputDir
	 * @throws IOException */
	static private void process (String packFileName, String outputDir) throws IOException {
		FileHandle inputFileHandle = new FileHandle(packFileName);
		File inputFile = inputFileHandle.file();

		if (outputDir == null) {
			File outputFile = new File(inputFile.getParentFile(), "output");
			if (!outputFile.exists()) {
				outputFile.mkdir();
			}
			outputDir = outputFile.getAbsolutePath();
		}

		TextureAtlasData atlasData = new TextureAtlasData(inputFileHandle, inputFileHandle.parent(), false);

		for (Region region : atlasData.getRegions()) {
			BufferedImage src = ImageIO.read(region.page.textureFile.read());
			BufferedImage subimage = null;

			System.out.println(String.format("processing image for %s x[%s] y[%s] w[%s] h[%s], rotate[%s]", region.name,
				region.left, region.top, region.width, region.height, region.rotate));

			if (region.rotate) {
				BufferedImage unRotatedImage = src.getSubimage(region.left, region.top, region.height, region.width);
				double rotationRequired = Math.toRadians(90.0);
				double locationX = unRotatedImage.getWidth() / 2;
				double locationY = unRotatedImage.getHeight() / 2;
				AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
				subimage = op.filter(unRotatedImage, subimage);

			} else {
				subimage = src.getSubimage(region.left, region.top, region.width, region.height);
			}

			ImageIO.write(subimage, "PNG", new FileOutputStream(outputDir + File.separator + region.name + ".png"));
		}

	}

	static public void main (String[] args) throws Exception {
		String packFileName = null, outputDir = null;

		switch (args.length) {
		case 2:
			outputDir = args[1];
		case 1:
			packFileName = args[0];
			break;
		default:
			System.out.println("Usage: [packFileName] [outputDir]");
			System.exit(0);
		}

		process(packFileName, outputDir);
	}
}
