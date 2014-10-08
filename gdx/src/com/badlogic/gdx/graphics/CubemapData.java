
package com.badlogic.gdx.graphics;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;

/** Used by a {@link Cubemap} to load the pixel data. The Cubemap will request the CubemapData to prepare itself through
 * {@link #prepare()} and upload its data using {@link #consumeCubemapData()}. These are the first methods to be called by Cubemap.
 * After that the Cubemap will invoke the other methods to find out about the size of the image data, the format, whether the
 * CubemapData is able to manage the pixel data if the OpenGL ES context is lost.</p>
 * 
 * Before a call to either {@link #consumeCubemapData()}, Cubemap will bind the OpenGL ES texture.</p>
 * 
 * Look at {@link KTXTextureData} for example implementation of this interface.
 * @author Vincent Bousquet */
public interface CubemapData {

	/** @return whether the TextureData is prepared or not. */
	public boolean isPrepared ();

	/** Prepares the TextureData for a call to {@link #consumeCubemapData()}. This method can be called from a non OpenGL thread and
	 * should thus not interact with OpenGL. */
	public void prepare ();

	/** Uploads the pixel data for the 6 faces of the cube to the OpenGL ES texture. The caller must bind an OpenGL ES texture. A
	 * call to {@link #prepare()} must preceed a call to this method. Any internal data structures created in {@link #prepare()}
	 * should be disposed of here. */
	public void consumeCubemapData ();

	/** @return the width of the pixel data */
	public int getWidth ();

	/** @return the height of the pixel data */
	public int getHeight ();

	/** @return whether this implementation can cope with a EGL context loss. */
	public boolean isManaged ();

}
