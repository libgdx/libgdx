package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.math.Matrix4;

/**
 * A <code>MeshPartBuilder</code> that applies a transformation to each output vertex before saving it to the internal array.
 * Positions are left-multiplied by the transformation matrix, while normals are rotated and renormalized.
 * 
 * @author azazad
 *
 */
public interface TransformingMeshPartBuilder extends MeshPartBuilder {
	/**
	 * Gets the current transformation matrix.
	 */
	public Matrix4 getTransform(Matrix4 out);
	
	/**
	 * Sets the current transformation matrix.
	 */
	public void setTransform(Matrix4 transform);
}
