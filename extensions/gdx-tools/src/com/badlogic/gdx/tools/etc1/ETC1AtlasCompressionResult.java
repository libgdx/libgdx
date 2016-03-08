
package com.badlogic.gdx.tools.etc1;

import java.util.ArrayList;

public class ETC1AtlasCompressionResult {

	static class TextureFileRenaming {

		private String oldPageFileName;
		private String newPageFileName;

		public TextureFileRenaming (String oldPageFileName, String newPageFileName) {
			this.oldPageFileName = oldPageFileName;
			this.newPageFileName = newPageFileName;
		}

		@Override
		public String toString () {
			return oldPageFileName + " :-> " + newPageFileName;
		}

	}

	private String atlas;
	private ArrayList<TextureFileRenaming> textures = new ArrayList<TextureFileRenaming>();

	public void setAtlasFilePath (String path) {
		atlas = path;
	}

	public void addCompressedTextureNames (String oldPageFileName, String newPageFileName) {
		TextureFileRenaming renaming = new TextureFileRenaming(oldPageFileName, newPageFileName);
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
		ETC1AtlasCompressor.log(tag, message);
	}

	private void log (Object message) {
		ETC1AtlasCompressor.log(message);
	}

	public String getAtlasPath () {
		return this.atlas;
	}

}
