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
	private DefaultTextureBinder binderW;
	private DefaultTextureBinder binderRR;
	private int maxUnits;
	private IntIntMap map;

	@Override
	public void create () {
		binderLRU = new DefaultTextureBinder(DefaultTextureBinder.LRU, 0, 4);
		binderW = new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 0, 4);
		binderRR = new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN, 0, 4);
		textures = new Array<Texture>();
		map = new IntIntMap();
		for(int i=0 ; i < numTextures ; i++){
			textures.add( new Texture(new Pixmap(16, 16, Format.RGBA8888)));
			map.put(textures.peek().getTextureObjectHandle(), i);
		}
		
		IntBuffer buffer = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_IMAGE_UNITS, buffer);
		maxUnits = buffer.get(0);
		// XXX
		maxUnits = 4;
		
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
		
		// Test Weighted
		binderW.begin();
		
		// mesh part 1 with 3 textures (drawn 10 times)
		for(int i=0 ; i<10 ; i++){
			binderW.bind(textures.get(0));
			binderW.bind(textures.get(1));
			binderW.bind(textures.get(2));
		}
		
		// mesh part 2 with 3 others textures
		binderW.bind(textures.get(3));
		printBindings(4);
		
		binderW.bind(textures.get(4)); 
		printBindings(4); // FIXME here texture 3 get stolen!
		
		binderW.end();
		
		
		for(int i=0 ; i<maxUnits ; i++){
			Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + i);
			IntBuffer buffer = BufferUtils.newIntBuffer(16);
			Gdx.gl.glGetIntegerv(GL20.GL_TEXTURE_BINDING_2D, buffer);
			int tex = buffer.get(0);
			int textureID = map.get(tex, 0);
			System.out.println("UNIT " + i + " texture " + textureID);
		}
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
