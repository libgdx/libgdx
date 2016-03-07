
package com.badlogic.gdx.tools.etc1;

import java.util.ArrayList;

public class AtlasETC1CompressionResult {

	static class TextureFileRenaming {

		private String old_page_file_name;
		private String new_page_file_name;

		public TextureFileRenaming (String old_page_file_name, String new_page_file_name) {
			this.old_page_file_name = old_page_file_name;
			this.new_page_file_name = new_page_file_name;
		}

		@Override
		public String toString () {
			return "" + old_page_file_name + " :-> " + new_page_file_name + "";
		}

	}

	private String atlas;
	private ArrayList<TextureFileRenaming> textures = new ArrayList<TextureFileRenaming>();

	public void setAtlasFilePath (String path) {
		atlas = path;
	}

	public void addCompressedTextureNames (String old_page_file_name, String new_page_file_name) {
		TextureFileRenaming renaming = new TextureFileRenaming(old_page_file_name, new_page_file_name);
		textures.add(renaming);
	}

	public void print () {
		log("AtlasETC1CompressionResult[" + textures.size() + "]");
		log(" atlas file", atlas);
		for (int i = 0; i < textures.size(); i++) {
			TextureFileRenaming renaming = textures.get(i);
			log("   " + i, renaming);
		}

	}

	private void log (String tag, Object message) {
		AtlasETC1Compressor.log(tag, message);
	}

	private void log (Object message) {
		AtlasETC1Compressor.log(message);
	}

	public String getAtlasPath () {
		return this.atlas;
	}

}
