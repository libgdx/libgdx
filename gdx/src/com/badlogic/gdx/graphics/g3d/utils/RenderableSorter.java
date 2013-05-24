package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Array;

/**
 * Responsible for sorting {@link Renderable} lists by whatever criteria (material, distance to camera, etc.)
 * @author badlogic
 *
 */
public interface RenderableSorter {
	/**
	 * Sorst the array of {@link Renderable} instances based on some criteria,
	 * e.g. material, distance to camera etc.
	 * @param renderables the array of renderables to be sorted
	 */
	public void sort(Camera camera, Array<Renderable> renderables);
}
