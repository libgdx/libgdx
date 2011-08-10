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

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap.Format;


/**
 * Loads image data for a texture. Used with
 * {@link Texture}, this allows custom
 * image loading for managed textures. If the OpenGL context is lost, the TextureData will be asked to load again when the context
 * is restored. The TextureData doesn't necessary need to keep the image data in memory between loads.
 */
public interface TextureData {
	public enum TextureDataType {
		Pixmap,
		Compressed
	}

	public TextureDataType getType();
	
	public Pixmap getPixmap();
	public boolean disposePixmap();
	
	public void uploadCompressedData();
	
	public int getWidth();
	public int getHeight();
	public Format getFormat();
	public boolean useMipMaps();
	public boolean isManaged();
}
