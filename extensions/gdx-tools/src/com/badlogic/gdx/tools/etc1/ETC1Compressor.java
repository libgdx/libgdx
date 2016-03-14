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

package com.badlogic.gdx.tools.etc1;

import java.io.File;
import java.io.OutputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.glutils.ETC1;
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class ETC1Compressor {
	private static final Color FUXIA = new Color(1, 0, 1, 1);

	public static void process (String inputDirectory, String outputDirectory, boolean recursive, boolean flatten)
		throws Exception {
		GdxNativesLoader.load();
		ETC1FileProcessor processor = new ETC1FileProcessor();
		processor.setRecursive(recursive);
		processor.setFlattenOutput(flatten);
		processor.process(new File(inputDirectory), new File(outputDirectory));
	}

	public static void main (String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("ETC1Compressor <input-dir> <output-dir>");
			System.exit(-1);
		}
		ETC1Compressor.process(args[0], args[1], true, false);
	}

	public static void compress (String inputFilePath, String outputFilePath) throws Exception {
		ETC1CompressorParams params = new ETC1CompressorParams();
		FileHandle inputFile = new FileHandle(inputFilePath);
		FileHandle outputFile = new FileHandle(outputFilePath);
		System.out.println("Processing " + inputFile);
		GdxNativesLoader.load();

		Pixmap pixmap = new Pixmap(inputFile);
		params.setInputPixmap(pixmap);

		OutputStream fileStream = outputFile.write(false);
		params.setOutputStream(fileStream);

		ETC1Compressor.compress(params);
		pixmap.dispose();

		System.out.println("Writing " + outputFile);
		fileStream.close();

	}

	public static void compress (ETC1CompressorParams params) throws Exception {
		final Pixmap pixmap = params.getInputPixmap();
		Color transparentColor = params.getTransparentColor();
		OutputStream outputStream = params.getOutputStream();
		if (transparentColor == null) {
			transparentColor = FUXIA;
		}
		
		Pixmap tmp = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGB888);
		tmp.setColor(transparentColor);
		tmp.fill();
		tmp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
		ETC1Data pkm = ETC1.encodeImagePKM(tmp);
		tmp.dispose();
		
		pkm.write(outputStream);
		outputStream.flush();
		pkm.dispose();
	}

	public static void deCompress (String etc1File, String restoredPngFile) {
		GdxNativesLoader.load();
		FileHandle inputFile = new FileHandle(etc1File);
		FileHandle outputFile = new FileHandle(restoredPngFile);
		System.out.println("Restoring " + inputFile);
		ETC1Data etc1Data = new ETC1Data(inputFile);
		Pixmap etc1Pixmap = ETC1.decodeImage(etc1Data, Format.RGB888);
		PixmapIO.writePNG(outputFile, etc1Pixmap);

	}

}
