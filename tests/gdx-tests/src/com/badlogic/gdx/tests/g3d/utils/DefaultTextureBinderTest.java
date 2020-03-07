package com.badlogic.gdx.tests.g3d.utils;


import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.IntIntMap;

public class DefaultTextureBinderTest extends GdxTest {

	private static final int numTextures = 64;
	private DefaultTextureBinder binderLRU;
	private Array<Texture> textures;
	private DefaultTextureBinder binderRR;
	private IntIntMap map;

	@Override
	public void create () {
		binderLRU = new DefaultTextureBinder(DefaultTextureBinder.LRU, 0, 4);
		binderRR = new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN, 0, 4);
		textures = new Array<Texture>();
		map = new IntIntMap();
		for(int i=0 ; i < numTextures ; i++){
			textures.add(new Texture(16, 16, Format.RGBA8888));
			map.put(textures.peek().getTextureObjectHandle(), i);
		}
	}
	
	@Override
	public void dispose () {
		for(Texture texture : textures){
			texture.dispose();
		}
	}
	
	@Override
	public void render () {
		
		// Test LRU
		binderLRU.begin();
		binderLRU.bind(textures.get(0));
		binderLRU.bind(textures.get(1));
		binderLRU.bind(textures.get(2));
		binderLRU.bind(textures.get(2));
		assertBindReuseCounts(3, 1);
		assertBinding(0, 1, 2);
		
		binderLRU.bind(textures.get(3));
		assertBindReuseCounts(1, 0);
		assertBinding(0, 1, 2, 3);
		
		binderLRU.bind(textures.get(1));
		assertBindReuseCounts(0, 1);
		assertBinding(0, 1, 2, 3);
		
		binderLRU.bind(textures.get(5));
		binderLRU.bind(textures.get(2));
		assertBindReuseCounts(1, 1);
		assertBinding(5, 1, 2, 3);
		
		binderLRU.end();
		
		printBindings(4);
	}
	private void assertBinding (int ...b) {
		for(int i=0 ; i<b.length ; i++){
			Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + i);
			IntBuffer buffer = BufferUtils.newIntBuffer(16);
			Gdx.gl.glGetIntegerv(GL20.GL_TEXTURE_BINDING_2D, buffer);
			int tex = buffer.get(0);
			int textureID = map.get(tex, -1);
			if(textureID != b[i]){
				System.err.println("UNIT " + i + " texture " + textureID + " expected " + b[i]);
			}
		}
	}
	private void printBindings (int n) {
		for(int i=0 ; i<n ; i++){
			Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + i);
			IntBuffer buffer = BufferUtils.newIntBuffer(16);
			Gdx.gl.glGetIntegerv(GL20.GL_TEXTURE_BINDING_2D, buffer);
			int tex = buffer.get(0);
			int textureID = map.get(tex, -1);
			System.out.println("UNIT " + i + " texture " + textureID);
		}
	}
	private void assertBindReuseCounts (int expectedBC, int expectedRC) {
		if(expectedBC != binderLRU.getBindCount()) System.err.println("bad bind count: " + binderLRU.getBindCount() + " expected" + expectedBC);
		if(expectedRC != binderLRU.getReuseCount()) System.err.println("bad resuse count: " + binderLRU.getReuseCount() + " expected" + expectedRC);
		binderLRU.resetCounts();
	}

}
