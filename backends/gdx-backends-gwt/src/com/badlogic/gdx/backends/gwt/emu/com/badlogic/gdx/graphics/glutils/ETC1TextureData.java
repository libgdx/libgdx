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

package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ETC1TextureData implements TextureData {
	public ETC1TextureData (FileHandle file) {
		throw new GdxRuntimeException("ETC1TextureData not supported in GWT backend");
	}

	public ETC1TextureData (FileHandle file, boolean useMipMaps) {
		throw new GdxRuntimeException("ETC1TextureData not supported in GWT backend");
	}

	@Override
	public TextureDataType getType () {
		return null;
	}

	@Override
	public boolean isPrepared () {
		return false;
	}

	@Override
	public void prepare () {
	}

	@Override
	public Pixmap consumePixmap () {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean disposePixmap () {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void consumeCustomData (int target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getWidth () {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHeight () {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Format getFormat () {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean useMipMaps () {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isManaged () {
		// TODO Auto-generated method stub
		return false;
	}

}