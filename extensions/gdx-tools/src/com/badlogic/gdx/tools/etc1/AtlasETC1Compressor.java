
package com.badlogic.gdx.tools.etc1;

import java.io.File;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class AtlasETC1Compressor {

	public static AtlasETC1CompressorSettings newCompressionSettings () {
		return new AtlasETC1CompressorSettings();
	}

	public static AtlasETC1CompressionResult compress (AtlasETC1CompressorSettings settings) throws Exception {

		String atlas_file_path_string = checkNull("atlas_file_path_string", settings.getAtlasFilePathString());
		log();
		log("compressing atlas to ETC1", atlas_file_path_string);
		GdxNativesLoader.load();

		AtlasETC1CompressionResult result = new AtlasETC1CompressionResult();

		FileHandle atlas_file = new FileHandle(new File(atlas_file_path_string));
		result.setAtlasFilePath(atlas_file.path());

		FileHandle atlas_folder = atlas_file.parent();

		TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(atlas_file, atlas_folder, false);

		Array<Page> pages = data.getPages();

		String atlas_data = atlas_file.readString();

		ETC1Compressor.process(atlas_folder.path(), atlas_folder.path(), false, true);

		for (int i = 0; i < pages.size; i++) {
			Page page_i = pages.get(i);
			FileHandle page_file = page_i.textureFile;

			String old_page_file_name = page_file.name();
			String page_file_name = page_file.nameWithoutExtension();
			String new_page_file_name = page_file_name + ".etc1";

			atlas_data = atlas_data.replaceAll(old_page_file_name, new_page_file_name);

			FileHandle compressed_page_file = page_file.parent().child(new_page_file_name);
			log("page " + i, page_file);
			log(" to", compressed_page_file);

			result.addCompressedTextureNames(old_page_file_name, new_page_file_name);

			page_file.delete();

		}

		atlas_file.writeString(atlas_data, false);

		return result;
	}

	private static <T> T checkNull (String parameter_name, T parameter_value) {
		if (parameter_value != null) {
			return parameter_value;
		}
		reportError("Parameter <" + parameter_name + "> is null");
		return null;
	}

	private static void reportError (String err_message) {
		throw new Error(err_message);
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
