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

package com.badlogic.gdx.tests.gles3;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.tests.gles3.PixelFormatES3.GLInternalFormat;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** An alternative to Libgdx's Pixmap.Format. TextureFormatES3 is used primarily during construction of specific GenericTexture
 * instances (those that require newer pixel formats). An instance of this class is used privately by GenericTexture to keep track
 * of the state of the loaded texture (though not its contents).
 * @author Mattijs Driel */
public class TextureFormatES3 {

	// general
	public int glTarget = GL20.GL_TEXTURE_2D;

	// storage
	public PixelFormatES3 pixelFormat = new PixelFormatES3(GLInternalFormat.GL_RGBA);

	// dimensions
	public int width = 1; // at least 1 pixel
	public int height = 0;
	public int depth = 0;

	// texture parameters
	public final TextureParameters params = new TextureParameters();

	public static class TextureParameters {
		public int minFilter = GL20.GL_NEAREST;
		public int magFilter = GL20.GL_NEAREST;
		public int wrapS = GL20.GL_REPEAT;
		public int wrapT = GL20.GL_REPEAT;

		public void copyFrom (TextureParameters source) {
			magFilter = source.magFilter;
			minFilter = source.minFilter;
			wrapS = source.wrapS;
			wrapT = source.wrapT;
		}
	}

	public TextureFormatES3 copy () {
		TextureFormatES3 copy = new TextureFormatES3();
		copy.glTarget = glTarget;
		copy.pixelFormat = pixelFormat.copy();
		copy.width = width;
		copy.height = height;
		copy.depth = depth;
		copy.params.copyFrom(params);
		return copy;
	}
}
