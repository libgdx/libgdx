/*******************************************************************************
 * Licensed under the Unlicense license (the "License");
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this file, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * You may obtain a copy of the License at
 * 
 *   http://unlicense.org
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.

 ******************************************************************************/

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
