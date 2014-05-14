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

	/** This method will unpack the given the {@link TextureAtlasData}. The outputDir is optional and if one isn't provided the
	 * output directory will be the location of the pack file.
	 * 
	 * @param atlasData
	 * @param outputDir
	 * @throws IOException */
	static public void process (TextureAtlasData atlasData, String outputDir) throws IOException {
		for (Region region : atlasData.getRegions()) {
			FileHandle textureFile = region.page.textureFile;
			BufferedImage src = ImageIO.read(textureFile.read());
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

			String outputDirTemp = outputDir != null && !outputDir.isEmpty() ? outputDir : textureFile.parent().path();
			ImageIO.write(subimage, "PNG", new FileOutputStream(outputDirTemp + File.separator + region.name + ".png"));
		}

	}

	/** This method will take a full path to the pack file that will be processed for unpacking. The outputDir is optional and if
	 * one isn't provided the output directory will be the location of the pack file.
	 * @param packNameFilePath
	 * @param outputDir
	 * @throws IOException */
	static public void process (String packNameFilePath, String outputDir) throws IOException {
		FileHandle inputFileHandle = new FileHandle(packNameFilePath);
		File inputFile = inputFileHandle.file();

		TextureAtlasData atlasData = new TextureAtlasData(inputFileHandle, inputFileHandle.parent(), false);
		process(atlasData, outputDir);
	}

	/** Main method for running the {@link TextureUnpacker}. The outputDir is optional and if one isn't provided the output
	 * directory will be the location of the pack file.
	 * @param args [packFileName] [outputDir]
	 * @throws Exception */
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
