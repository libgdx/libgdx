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
import com.badlogic.gdx.graphics.glutils.FileTextureArrayData;

/** Used by a {@link TextureArray} to load the pixel data. The TextureArray will request the TextureArrayData to prepare itself through
 * {@link #prepare()} and upload its data using {@link #consumeTextureArrayData()}. These are the first methods to be called by TextureArray.
 * After that the TextureArray will invoke the other methods to find out about the size of the image data, the format, whether the
 * TextureArrayData is able to manage the pixel data if the OpenGL ES context is lost.</p>
 *
 * Before a call to either {@link #consumeTextureArrayData()}, TextureArray will bind the OpenGL ES texture.</p>
 *
 * Look at {@link FileTextureArrayData} for example implementation of this interface.
 * @author Tomski */
public interface TextureArrayData {

	/** @return whether the TextureArrayData is prepared or not. */
	public boolean isPrepared ();

	/** Prepares the TextureArrayData for a call to {@link #consumeTextureArrayData()}. This method can be called from a non OpenGL thread and
	 * should thus not interact with OpenGL. */
	public void prepare ();

	/** Uploads the pixel data of the TextureArray layers of the TextureArray to the OpenGL ES texture. The caller must bind an OpenGL ES texture. A
	 * call to {@link #prepare()} must preceed a call to this method. Any internal data structures created in {@link #prepare()}
	 * should be disposed of here. */
	public void consumeTextureArrayData ();

	/** @return the width of this TextureArray */
	public int getWidth ();

	/** @return the height of this TextureArray */
	public int getHeight ();

	/** @return the layer count of this TextureArray */
	public int getDepth ();

	/** @return whether this implementation can cope with a EGL context loss. */
	public boolean isManaged ();

	/** @return the internal format of this TextureArray */
	public int getInternalFormat ();

	/** @return the GL type of this TextureArray*/
	public int getGLType ();

	/** Provides static method to instantiate the right implementation.
	 * @author Tomski */
	public static class Factory {

		public static TextureArrayData loadFromFiles (Pixmap.Format format, boolean useMipMaps, FileHandle... files) {
			return new FileTextureArrayData(format, useMipMaps, files);
		}

	}

}
