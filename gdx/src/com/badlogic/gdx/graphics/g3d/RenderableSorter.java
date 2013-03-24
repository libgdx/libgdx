package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.utils.Array;

/**
 * Responsible for sorting {@link RenderInstance} lists by whatever criteria (material, distance to camera, etc.)
 * @author badlogic
 *
 */
public interface RenderableSorter {
	/**
	 * Sorst the array of {@link Renderable} instances based on some criteria,
	 * e.g. material, distance to camera etc.
	 * @param renderables the array of renderables to be sorted
	 */
	public void sort(Array<Renderable> renderables);
}
