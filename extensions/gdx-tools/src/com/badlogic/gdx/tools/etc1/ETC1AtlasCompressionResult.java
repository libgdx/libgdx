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

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;

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

	private Color transparentColor;

	public void setAtlasFilePath (String path) {
		atlas = path;
	}

	public void setTransparentColor (Color transparentColor) {
		this.transparentColor = transparentColor;
	}

	public void addCompressedTextureNames (String oldPageFileName, String newPageFileName) {
		TextureFileRenaming renaming = new TextureFileRenaming(oldPageFileName, newPageFileName);
		textures.add(renaming);
	}

	public void print () {
		log("AtlasETC1CompressionResult[" + textures.size() + "]");
		log(" atlas file", atlas);
		log(" transparent color", transparentColor);
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
