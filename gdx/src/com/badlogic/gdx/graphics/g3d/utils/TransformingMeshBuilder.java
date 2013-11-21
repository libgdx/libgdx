package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Implementation of <code>TransformingMeshPartBuilder</code>
 * 
 * @author azazad
 *
 */
public class TransformingMeshBuilder extends MeshBuilder implements TransformingMeshPartBuilder {
	private final Matrix4 transform = new Matrix4();
	
	private final Vector3 tempPosTransformed = new Vector3();
	private final Vector3 tempNorTransformed = new Vector3();
	
	@Override
	public Matrix4 getTransform(Matrix4 out) {
		return out.set(this.transform);
	}

	@Override
	public void setTransform(Matrix4 transform) {
		this.transform.set(transform);
	}
	
	@Override
	public short vertex(Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
		tempPosTransformed.set(pos).mul(transform);
		tempNorTransformed.set(nor).rot(transform).nor();
		
		return super.vertex(tempPosTransformed, tempNorTransformed, col, uv);
	}

}
