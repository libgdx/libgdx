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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.ETC1TextureData;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;
import com.badlogic.gdx.graphics.glutils.MipMapGenerator;

/** Used by a {@link Texture} to load the pixel data. A TextureData can either return a {@link Pixmap} or upload the pixel data
 * itself. It signals it's type via {@link #getType()} to the Texture that's using it. The Texture will then either invoke
 * {@link #consumePixmap()} or {@link #consumeCustomData(int)}. These are the first methods to be called by Texture. After that
 * the Texture will invoke the other methods to find out about the size of the image data, the format, whether mipmaps should be
 * generated and whether the TextureData is able to manage the pixel data if the OpenGL ES context is lost.</p>
 * 
 * In case the TextureData implementation has the type {@link TextureDataType#Custom}, the implementation has to generate the
 * mipmaps itself if necessary. See {@link MipMapGenerator}.</p>
 * 
 * Before a call to either {@link #consumePixmap()} or {@link #consumeCustomData(int)}, Texture will bind the OpenGL ES
 * texture.</p>
 * 
 * Look at {@link FileTextureData} and {@link ETC1TextureData} for example implementations of this interface.
 * @author mzechner */
public interface TextureData {
	/** The type of this {@link TextureData}.
	 * @author mzechner */
	public enum TextureDataType {
		Pixmap, Custom
	}

	/** @return the {@link TextureDataType} */
	public TextureDataType getType ();

	/** @return whether the TextureData is prepared or not. */
	public boolean isPrepared ();

	/** Prepares the TextureData for a call to {@link #consumePixmap()} or {@link #consumeCustomData(int)}. This method can be
	 * called from a non OpenGL thread and should thus not interact with OpenGL. */
	public void prepare ();

	/** Returns the {@link Pixmap} for upload by Texture. A call to {@link #prepare()} must precede a call to this method. Any
	 * internal data structures created in {@link #prepare()} should be disposed of here.
	 * 
	 * @return the pixmap. */
	public Pixmap consumePixmap ();

	/** @return whether the caller of {@link #consumePixmap()} should dispose the Pixmap returned by {@link #consumePixmap()} */
	public boolean disposePixmap ();

	/** Uploads the pixel data to the OpenGL ES texture. The caller must bind an OpenGL ES texture. A call to {@link #prepare()}
	 * must preceed a call to this method. Any internal data structures created in {@link #prepare()} should be disposed of here. */
	public void consumeCustomData (int target);

	/** @return the width of the pixel data */
	public int getWidth ();

	/** @return the height of the pixel data */
	public int getHeight ();

	/** @return the {@link Format} of the pixel data */
	public Format getFormat ();

	/** @return whether to generate mipmaps or not. */
	public boolean useMipMaps ();

	/** @return whether this implementation can cope with a EGL context loss. */
	public boolean isManaged ();

	/** Provides static method to instantiate the right implementation (Pixmap, ETC1, KTX).
	 * @author Vincent Bousquet */
	public static class Factory {

		public static TextureData loadFromFile (FileHandle file, boolean useMipMaps) {
			return loadFromFile(file, null, useMipMaps);
		}

		public static TextureData loadFromFile (FileHandle file, Format format, boolean useMipMaps) {
			if (file == null) return null;
			if (file.name().endsWith(".cim")) return new FileTextureData(file, PixmapIO.readCIM(file), format, useMipMaps);
			if (file.name().endsWith(".etc1")) return new ETC1TextureData(file, useMipMaps);
			if (file.name().endsWith(".ktx") || file.name().endsWith(".zktx")) return new KTXTextureData(file, useMipMaps);
			return new FileTextureData(file, new Pixmap(file), format, useMipMaps);
		}

	}

}
