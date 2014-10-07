
package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class KTXCubemap extends GLTexture {

	private final KTXTextureData data;

	public KTXCubemap (KTXTextureData data) {
		super(GL20.GL_TEXTURE_CUBE_MAP);
		minFilter = TextureFilter.Nearest;
		magFilter = TextureFilter.Nearest;
		uWrap = TextureWrap.ClampToEdge;
		vWrap = TextureWrap.ClampToEdge;
		this.data = data;
		load();
	}

	@Override
	public int getWidth () {
		if (data.getWidth() == -1) throw new GdxRuntimeException("getWidth called before preparing the texture");
		return data.getWidth();
	}

	@Override
	public int getHeight () {
		if (data.getHeight() == -1) throw new GdxRuntimeException("getHeight called before preparing the texture");
		return data.getHeight();
	}

	@Override
	public int getDepth () {
		return 0;
	}

	@Override
	public boolean isManaged () {
		return true;
	}

	@Override
	protected void reload () {
		glHandle = createGLHandle();
		load();
	}

	public void load () {
		if (!data.isPrepared()) data.prepare();
		bind();
		uploadImageData(glTarget, data);
		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
		Gdx.gl.glBindTexture(glTarget, 0);
	}

}
