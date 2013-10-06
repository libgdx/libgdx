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

 package de.swagner.paxbritannica.help;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

public class ScreenshotSaver {

	public static void saveScreenshot(String baseName) throws IOException {
		if(Gdx.app.getType()==ApplicationType.Android){
			return;
		}

		byte[] screenshotPixels = ScreenUtils.getFrameBufferPixels(true);
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		DataBufferByte dataBuffer = new DataBufferByte(screenshotPixels, screenshotPixels.length);

		int[] offsets = { 0, 1, 2 };
		PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, 4, 4 * width, offsets);

		ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8 }, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

		WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));

		BufferedImage img = new BufferedImage(cm, raster, false, null);

		ImageIO.write(img, "png", File.createTempFile(baseName, ".png"));
	}

}