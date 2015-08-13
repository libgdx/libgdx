package com.badlogic.gdx.graphics.g3d.shadow.allocation;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Scene;

/**
 * The behavior of the FixedShadowMapAllocator is naive.
 * It separates the texture in several parts and for each lights
 * increments the region
 * @author realitix
 */
public class FixedShadowMapAllocator implements ShadowMapAllocator {
	public static final int QUALITY_MIN = 1024;
	public static final int QUALITY_MED = 2048;
	public static final int QUALITY_MAX = 4096;

	public static final int NB_MAP_MIN = 4;
	public static final int NB_MAP_MED = 16;
	public static final int NB_MAP_MAX = 32;

	protected final int size;
	protected final int nbMap;
	protected int currentMap;
	protected Scene scene;
	protected AllocatorResult result = new AllocatorResult();

	public FixedShadowMapAllocator(int size, int nbMap, Scene scene) {
		this.size = size;
		this.nbMap = nbMap;
		this.scene = scene;
	}

	@Override
	public int getSize() {
		return size;
	}

	public int getNbMap() {
		return nbMap;
	}

	@Override
	public void begin () {
		currentMap = 0;
	}

	@Override
	public void end () {
	}

	@Override
	public AllocatorResult nextResult (Camera camera) {
		int nbOnLine = (int)Math.round(Math.sqrt(nbMap));
		int i = currentMap % nbOnLine;
		int j = currentMap / nbOnLine;
		int sizeMap = size / nbOnLine;

		result.x = i*sizeMap;
		result.y = j*sizeMap;
		result.width = sizeMap;
		result.height = sizeMap;

		currentMap += 1;

		return result;
	}
}
