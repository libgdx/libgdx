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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class ETC1AtlasCompressor {

	public static ETC1AtlasCompressorSettings newCompressionSettings () {
		return new ETC1AtlasCompressorSettings();
	}

	public static ETC1AtlasCompressionResult compress (ETC1AtlasCompressorSettings settings) throws Exception {

		String atlasFilePathString = checkNull("atlas_file_path_string", settings.getAtlasFilePathString());

		log("compressing atlas to ETC1", atlasFilePathString);
		GdxNativesLoader.load();

		ETC1AtlasCompressionResult result = new ETC1AtlasCompressionResult();

		FileHandle atlasFile = new FileHandle(new File(atlasFilePathString));
		result.setAtlasFilePath(atlasFile.path());

		FileHandle atlasFolder = atlasFile.parent();

		TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(atlasFile, atlasFolder, false);

		Array<Page> pages = data.getPages();

		String atlasData = atlasFile.readString();

		ETC1Compressor.process(atlasFolder.path(), atlasFolder.path(), false, true, settings.getTransparentColor());

		for (int i = 0; i < pages.size; i++) {
			Page page_i = pages.get(i);
			FileHandle pageFile = page_i.textureFile;

			String oldPageFileName = pageFile.name();
			String pageFileName = pageFile.nameWithoutExtension();
			String newPageFileName = pageFileName + ".etc1";

			atlasData = atlasData.replaceAll(oldPageFileName, newPageFileName);

			FileHandle compressedPageFile = pageFile.parent().child(newPageFileName);
			log("  page " + i, pageFile);
			log("   to", compressedPageFile);

			result.addCompressedTextureNames(oldPageFileName, newPageFileName);

			pageFile.delete();

		}

		atlasFile.writeString(atlasData, false);

		return result;
	}

	private static <T> T checkNull (String parameterName, T parameterValue) {
		if (parameterValue != null) {
			return parameterValue;
		}
		throw new NullPointerException("Parameter <" + parameterName + "> is null");
	}

	public static void log (String tag, Object message) {
		log(tag + " > " + message);
	}

	public static void log (Object message) {
		System.out.println(message + "");
	}

	public static void log () {
		System.out.println();
	}

	public static void main (String[] args) throws Exception {
		String atlasFilePath = null;
		String outputDir = null;
		switch (args.length) {

		case 1:
			atlasFilePath = args[0];
			break;
		default:
			log("Usage: %inputAtlasFile%");
			System.exit(0);
		}

		ETC1AtlasCompressorSettings settings = ETC1AtlasCompressor.newCompressionSettings();
		settings.setAtlasFilePathString(atlasFilePath);
		ETC1AtlasCompressionResult compressionResult = ETC1AtlasCompressor.compress(settings);
		log();
		compressionResult.print();

	}

}
