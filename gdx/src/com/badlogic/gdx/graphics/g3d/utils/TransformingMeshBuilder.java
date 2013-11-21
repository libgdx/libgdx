/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Implementation of <code>TransformingMeshPartBuilder</code> based on <code>MeshBuilder</code>.
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
