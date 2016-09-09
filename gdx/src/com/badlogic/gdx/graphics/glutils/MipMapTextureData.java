package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** This class will load each contained TextureData to the chosen mipmap level.
 *  All the mipmap levels must be defined and cannot be null. */
public class MipMapTextureData implements TextureData{
	TextureData[] mips;
	
	/** @param mipMapData must be != null and its length must be >= 1 */
	public MipMapTextureData(TextureData... mipMapData){
		mips = new TextureData[mipMapData.length];
		System.arraycopy(mipMapData, 0, mips, 0, mipMapData.length);
	}
	
	@Override
	public TextureDataType getType () {
		return TextureDataType.Custom;
	}

	@Override
	public boolean isPrepared () {
		return true;
	}

	@Override
	public void prepare () {}

	@Override
	public Pixmap consumePixmap () {
		throw new GdxRuntimeException("It's compressed, use the compressed method");
	}

	@Override
	public boolean disposePixmap () {
		return false;
	}

	@Override
	public void consumeCustomData (int target) {
		for(int i=0; i < mips.length; ++i){
			GLTexture.uploadImageData(target, mips[i], i);
		}
	}

	@Override
	public int getWidth () {
		return mips[0].getWidth();
	}

	@Override
	public int getHeight () {
		return mips[0].getHeight();
	}

	@Override
	public Format getFormat () {
		return mips[0].getFormat();
	}

	@Override
	public boolean useMipMaps () {
		return false;
	}

	@Override
	public boolean isManaged () {
		return true;
	}
}