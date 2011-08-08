package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;

public class FileTextureData implements TextureData {
	final FileHandle file;
	int width = 0;
	int height = 0;
	Format format;
	Pixmap pixmap;
	boolean useMipMaps;
	
	public FileTextureData(FileHandle file, Pixmap preloadedPixmap, Format format, boolean useMipMaps) {
		this.file = file;
		this.pixmap = preloadedPixmap;
		this.format = format;
		this.useMipMaps = useMipMaps;
		if(pixmap != null) {
			width = pixmap.getWidth();
			height = pixmap.getHeight();
			if(format == null) this.format = pixmap.getFormat();
		}
	}
	
	@Override
	public Pixmap getPixmap () {
		if(pixmap != null) {
			Pixmap tmp = pixmap;
			this.pixmap = null;
			return tmp;
		} else {			
			Pixmap pixmap = new Pixmap(file);
			width = pixmap.getWidth();
			height = pixmap.getHeight();
			if(format == null) format = pixmap.getFormat();
			return pixmap;
		}
	}

	@Override
	public boolean disposePixmap () {
		return true;
	}

	@Override
	public int getWidth () {
		return width;
	}

	@Override
	public int getHeight () {
		return height;
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
		return true;
	}
	
	public FileHandle getFileHandle() {
		return file;
	}
}
