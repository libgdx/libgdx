
package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.materials.Material;

public class AnimatedModelNode extends StillModelNode implements AnimatedModelInstance {
	public String animation;
	public float time;
	public boolean looping;

	@Override
	public String getAnimation () {
		return animation;
	}

	@Override
	public float getAnimationTime () {
		return time;
	}

	@Override
	public boolean isLooping () {
		return looping;
	}

	public AnimatedModelNode copy () {
		final AnimatedModelNode copy = new AnimatedModelNode();
		if (materials != null) {
			final int len = materials.length;
			Material[] mats = new Material[len];
			for (int i = 0; i < len; i++) {
				mats[i] = materials[i].copy();
			}
			copy.materials = mats;
		}
		copy.matrix.set(matrix.val);
		copy.origin.set(origin);
		copy.radius = radius;
		copy.transformedPosition.set(transformedPosition);
		copy.animation = animation;
		copy.time = time;
		copy.looping = looping;
		return copy;
	}

}
