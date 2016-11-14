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

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Texture;

/** Responsible for binding textures, may implement a strategy to avoid binding a texture unnecessarily. A TextureBinder may decide
 * to which texture unit it binds a texture.
 * @author badlogic, Xoppa */
public interface TextureBinder {
	/** Prepares the binder for operation, must be matched with a call to {@link #end()}. */
	public void begin ();

	/** Disables all used texture units and unbinds textures. Resets the counts. */
	public void end ();

	/** Binds the texture to an available unit and applies the filters in the descriptor.
	 * @param textureDescriptor the {@link TextureDescriptor}
	 * @return the unit the texture was bound to */
	public int bind (TextureDescriptor textureDescriptor);

	/** Binds the texture to an available unit.
	 * @param texture the {@link Texture}
	 * @return the unit the texture was bound to */
	public int bind (GLTexture texture);

	/** @return the number of binds actually executed since the last call to {@link #resetCounts()} */
	public int getBindCount ();

	/** @return the number of binds that could be avoided by reuse */
	public int getReuseCount ();

	/** Resets the bind/reuse counts */
	public void resetCounts ();
}
