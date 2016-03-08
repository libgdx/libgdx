
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
		log();
		log("compressing atlas to ETC1", atlasFilePathString);
		GdxNativesLoader.load();

		ETC1AtlasCompressionResult result = new ETC1AtlasCompressionResult();

		FileHandle atlasFile = new FileHandle(new File(atlasFilePathString));
		result.setAtlasFilePath(atlasFile.path());

		FileHandle atlasFolder = atlasFile.parent();

		TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(atlasFile, atlasFolder, false);

		Array<Page> pages = data.getPages();

		String atlasData = atlasFile.readString();

		ETC1Compressor.process(atlasFolder.path(), atlasFolder.path(), false, true);

		for (int i = 0; i < pages.size; i++) {
			Page page_i = pages.get(i);
			FileHandle pageFile = page_i.textureFile;

			String oldPageFileName = pageFile.name();
			String pageFileName = pageFile.nameWithoutExtension();
			String newPageFileName = pageFileName + ".etc1";

			atlasData = atlasData.replaceAll(oldPageFileName, newPageFileName);

			FileHandle compressedPageFile = pageFile.parent().child(newPageFileName);
			log("page " + i, pageFile);
			log(" to", compressedPageFile);

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

}
