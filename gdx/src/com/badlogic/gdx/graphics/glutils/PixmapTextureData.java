
package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class PixmapTextureData implements TextureData {
	final Pixmap pixmap;
	final Format format;
	final boolean useMipMaps;
	final boolean disposePixmap;

	public PixmapTextureData (Pixmap pixmap, Format format, boolean useMipMaps, boolean disposePixmap) {
		this.pixmap = pixmap;
		this.format = format == null ? pixmap.getFormat() : format;
		this.useMipMaps = useMipMaps;
		this.disposePixmap = disposePixmap;
	}

	@Override
	public boolean disposePixmap () {
		return disposePixmap;
	}

	@Override
	public Pixmap getPixmap () {
		return pixmap;
	}

	@Override
	public int getWidth () {
		return pixmap.getWidth();
	}

	@Override
	public int getHeight () {
		return pixmap.getHeight();
	}

	@Override
	public Format getFormat () {
		return format;
	}

	@Override
	public boolean useMipMaps () {
		return useMipMaps;
	}

	@Override
	public boolean isManaged () {
		return false;
	}

	@Override
	public TextureDataType getType () {
		return TextureDataType.Pixmap;
	}

	@Override
	public void uploadCompressedData () {
		throw new GdxRuntimeException("This TextureData implementation does not upload data itself");
	}
}
