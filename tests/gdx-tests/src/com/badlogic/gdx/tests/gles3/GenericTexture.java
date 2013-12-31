
package com.badlogic.gdx.tests.gles3;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;

public class GenericTexture {

	private int textureName;
	private IntBuffer ib = BufferUtils.newIntBuffer(1);
	
	public GenericTexture (int width, int height, int sizedInternalFormat, int baseInternalFormat, int filter) {
		Gdx.gl30.glGenTextures(1, ib);
		textureName = ib.get(0);
		
		bind();
		
		Gdx.gl30.glTexParameterf(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, filter);
		Gdx.gl30.glTexParameterf(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, filter);
		
		Gdx.gl30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, sizedInternalFormat, width, height, 0, baseInternalFormat, GL30.GL_UNSIGNED_BYTE, null);

	}
	
	public void bind()
	{
		Gdx.gl30.glBindTexture(GL30.GL_TEXTURE_2D, textureName);
	}
	
	public void bindToFBO(int attachment)
	{
		bind(); // <- not sure if necessary
		Gdx.gl30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, GL30.GL_TEXTURE_2D, textureName, 0);
	}

}
