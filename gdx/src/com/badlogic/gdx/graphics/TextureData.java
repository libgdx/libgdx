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

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.ETC1TextureData;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.glutils.MipMapGenerator;

/** Loads image data for a texture. Used with {@link Texture}, this allows custom image loading for managed textures. If the OpenGL
 * context is lost, the TextureData will be asked to load again when the context is restored. The TextureData doesn't necessary
 * need to keep the image data in memory between loads. 
 */

/**
 * Used by a {@link Texture} to load the pixel data. A TextureData can either return a {@link Pixmap} or upload the pixel
 * data itself. It signals it's type via {@link #getType()} to the Texture that's using it. The Texture will then either
 * invoke {@link #getPixmap()} or {@link #uploadCompressedData()}. These are the first methods to be called by Texture.
 * After that the Texture will invoke the other methods to find out about the size of the image data, the format, whether
 * mipmaps should be generated and whether the TextureData is able to manage the pixel data if the OpenGL ES context is 
 * lost.</p>
 * 
 * In case the TextureData implementation has the type {@link TextureDataType#Compressed}, the implementatio has to
 * generate the mipmaps itself if necessary. See {@link MipMapGenerator}.</p>
 * 
 * Before a call to either {@link #getPixmap()} or {@link #uploadCompressedData()}, Texture will bind the OpenGL ES texture.</p>
 * 
 * Look at {@link FileTextureData} and {@link ETC1TextureData} for example implementations of this interface.
 * @author mzechner
 *
 */
public interface TextureData {
	/**
	 * The type of this {@link TextureData}. 
	 * @author mzechner
	 *
	 */
	public enum TextureDataType {
		Pixmap,
		Compressed
	}

	/**
	 * @return the {@link TextureDataType}
	 */
	public TextureDataType getType ();

	/**
	 * Returns the {@link Pixmap} for upload by Texture. Called
	 * before any other method.
	 * @return the pixmap.
	 */
	public Pixmap getPixmap ();

	/**
	 * @return whether Texture should dispose the Pixmap returned by {@link #getPixmap()}
	 */
	public boolean disposePixmap ();

	/**
	 * Uploads the pixel data to the OpenGL ES texture. The Texture calling
	 * this method will bind the OpenGL ES texture. Called before any other method.
	 */
	public void uploadCompressedData ();

	/**
	 * @return the width of the pixel data
	 */
	public int getWidth ();

	/**
	 * @return the height of the pixel data
	 */
	public int getHeight ();

	/**
	 * @return the {@link Format} of the pixel data
	 */
	public Format getFormat ();

	/**
	 * @return whether to generate mipmaps or not.
	 */
	public boolean useMipMaps ();

	/**
	 * @return whether this implementation can cope with a EGL context loss.
	 */
	public boolean isManaged ();
}
