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
	public void consumeCompressedData (int target) {
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
