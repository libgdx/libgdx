package com.badlogic.gdx.graphics.g3d.utils;

import java.util.Comparator;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.utils.Array;

public class DefaultRenderableSorter implements RenderableSorter, Comparator<Renderable> {
	@Override
	public void sort (Array<Renderable> renderables) {
		renderables.sort(this);
	}
	
	@Override
	public int compare (final Renderable o1, final Renderable o2) {
		final boolean b1 = o1.material.has(BlendingAttribute.Type);
		final boolean b2 = o2.material.has(BlendingAttribute.Type);
		if (b1 != b2) 
			return b1 ? 1 : -1;
		final int result = (o1.shader == o2.shader) ? 0 : o1.shader.compareTo(o2.shader);
		return result;
	}
}
