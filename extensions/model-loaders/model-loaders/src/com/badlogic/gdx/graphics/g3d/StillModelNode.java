
package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class StillModelNode implements StillModelInstance {
	static final private float[] vec3 = {0, 0, 0};

	final public Vector3 origin = new Vector3();
	final public Vector3 transformedPosition = new Vector3();

	final public Matrix4 matrix = new Matrix4();
	public Material[] materials;
	public float radius;

	public StillModelNode () {
		this(null);
	}

	public StillModelNode (Material[] materials) {
		this.materials = materials;
	}

	@Override
	public Matrix4 getTransform () {
		return matrix;
	}

	@Override
	public Vector3 getSortCenter () {
		vec3[0] = origin.x;
		vec3[1] = origin.y;
		vec3[2] = origin.z;
		Matrix4.mulVec(matrix.val, vec3);
		transformedPosition.x = vec3[0];
		transformedPosition.y = vec3[1];
		transformedPosition.z = vec3[2];
		return transformedPosition;
	}

	@Override
	public Material[] getMaterials () {
		return materials;
	}

	@Override
	public float getBoundingSphereRadius () {
		return radius;
	}

	public StillModelNode copy () {
		final StillModelNode copy = new StillModelNode();
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
		return copy;

	}

}
