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

package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;

public class TextureParameter implements AssetLoaderParameters<Texture> {
	/** the format of the final Texture. Uses the source images format if null **/
	public Format format = null;
	/** whether to generate mipmaps **/
	public boolean genMipMaps = false;
	/** The texture to put the {@link TextureData} in, optional. **/
	public Texture texture = null;
	/** TextureData for textures created on the fly, optional. When set, all format and genMipMaps are ignored */
	public TextureData textureData = null;
}
