package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class MipMapGenerator {
	private static boolean useHWMipMap = true;

	static public void setUseHardwareMipMap(boolean useHWMipMap) {
		MipMapGenerator.useHWMipMap = useHWMipMap;
	}
	
	/**
	 * Sets the image data of the {@link Texture} based on the {@link Pixmap}. The texture
	 * must be bound for this to work. If <code>disposePixmap</code> is true, the pixmap
	 * will be disposed at the end of the method.
	 * @param pixmap the Pixmap
	 * @param texture the Texture
	 * @param disposePixmap whether to dispose the Pixmap after upload
	 */
	public static void generateMipMap(Pixmap pixmap, Texture texture, boolean disposePixmap) {
		if(!useHWMipMap) {
			generateMipMapCPU(pixmap, texture, disposePixmap);
			return;
		}
		
		if(Gdx.app.getType() == ApplicationType.Android) {
			if(Gdx.graphics.isGL20Available())
				generateMipMapGLES20(pixmap, disposePixmap);
			else
				generateMipMapCPU(pixmap, texture, disposePixmap);
		} else {
			generateMipMapDesktop(pixmap, texture, disposePixmap);
		}
	}
	
	private static void generateMipMapGLES20(Pixmap pixmap, boolean disposePixmap) {
		Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_2D);
		if(disposePixmap) pixmap.dispose();
	}
	
	private static void generateMipMapDesktop(Pixmap pixmap, Texture texture, boolean disposePixmap) {
		if(Gdx.graphics.isGL20Available() &&
			(Gdx.graphics.supportsExtension("GL_ARB_framebuffer_object") ||
			Gdx.graphics.supportsExtension("GL_EXT_framebuffer_object"))) {
			Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
			Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_2D);
			if(disposePixmap) pixmap.dispose();
		} else if(Gdx.graphics.supportsExtension("GL_SGIS_generate_mipmap")) {
			if((Gdx.gl20==null) && texture.getWidth() != texture.getHeight())
				throw new GdxRuntimeException("texture width and height must be square when using mipmapping in OpenGL ES 1.x");
			Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GLCommon.GL_GENERATE_MIPMAP, GL10.GL_TRUE);
			Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
			if(disposePixmap) pixmap.dispose();
		} else {
			generateMipMapCPU(pixmap, texture, disposePixmap);
		}				
	}
	
	private static void generateMipMapCPU(Pixmap pixmap, Texture texture, boolean disposePixmap) {
		Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
		if((Gdx.gl20==null) && texture.getWidth() != texture.getHeight())
			throw new GdxRuntimeException("texture width and height must be square when using mipmapping in OpenGL ES 1.x");
		int width = pixmap.getWidth() / 2;
		int height = pixmap.getHeight() / 2;
		int level = 1;
		while(width > 0  && height > 0) {
			Pixmap tmp = new Pixmap(width, height, pixmap.getFormat());		
			tmp.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, width, height);
			if(level > 1 || disposePixmap)
				pixmap.dispose();				
			pixmap = tmp;
			
			Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, level, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());							
							
			width = pixmap.getWidth() / 2;
			height = pixmap.getHeight() / 2;
			level++;
		}
		pixmap.dispose();
	}
}
