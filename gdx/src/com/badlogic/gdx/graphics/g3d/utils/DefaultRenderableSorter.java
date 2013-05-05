package com.badlogic.gdx.graphics.g3d.utils;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class DefaultRenderableSorter implements RenderableSorter, Comparator<Renderable> {
	private Camera camera;
	private final Vector3 tmpV1 = new Vector3();
	private final Vector3 tmpV2 = new Vector3();
	
	@Override
	public void sort (final Camera camera, final Array<Renderable> renderables) {
		this.camera = camera;
		renderables.sort(this);
	}
	
	@Override
	public int compare (final Renderable o1, final Renderable o2) {
		final boolean b1 = o1.material.has(BlendingAttribute.Type);
		final boolean b2 = o2.material.has(BlendingAttribute.Type);
		if (b1 != b2) 
			return b1 ? 1 : -1;
		// FIXME implement better sorting algorithm
		// final boolean same = o1.shader == o2.shader && o1.mesh == o2.mesh && (o1.lights == null) == (o2.lights == null) && 
			// o1.material.equals(o2.material);
		o1.modelTransform.getTranslation(tmpV1);
		o2.modelTransform.getTranslation(tmpV2);
		final float dst = camera.position.dst2(tmpV1) - camera.position.dst2(tmpV2);
		return dst < 0f ? -1 : (dst > 0f ? 1 : 0);
	}
}
