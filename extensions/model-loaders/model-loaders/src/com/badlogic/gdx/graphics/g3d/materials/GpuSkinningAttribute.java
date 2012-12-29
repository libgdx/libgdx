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

package com.badlogic.gdx.graphics.g3d.materials;

import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.model.skeleton.Skeleton;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Pool;

public class GpuSkinningAttribute extends MaterialAttribute {
	private static final int maxNumBones = 32;
	private final static String bonesName = "bones";
	
	private Skeleton skeleton;
	private final FloatBuffer matrixArray;	
	private final float[] tmpValues;
	private final Matrix4 tempMatrix = new Matrix4();
	private Matrix4 modelMatrix = new Matrix4();

	protected GpuSkinningAttribute () {
		final int size = maxNumBones*16;
		tmpValues = new float[size];
		matrixArray = BufferUtils.newFloatBuffer(size);
		
	}

	public GpuSkinningAttribute (Skeleton skeleton) {
		//hack to get to pass through BONES_NUM
		super(" BONES_NUM "+skeleton.combinedMatrices.size+"\n#define gpuSkinning");
		this.skeleton = skeleton;
		
		final int size = maxNumBones*16;
		tmpValues = new float[size];
		matrixArray = BufferUtils.newFloatBuffer(size);
	}
	
	// Call this to set the model matrix before binding this attribute.
	public void setModelMatrix(Matrix4 matrix){
		modelMatrix.set(matrix); 
	}

	@Override
	public void bind () {
	}

	@Override
	public void bind (ShaderProgram program) {
		//program.setUniformMatrix(bonesName, skeleton.combinedMatrices);

		//TODO: the following code is only needed because there is no way to set an array of matrix in ShaderProgram
		GL20 gl = Gdx.graphics.getGL20();
		int length = skeleton.combinedMatrices.size;
		for(int i=0;i<length;i++){
			Matrix4 matrix = skeleton.combinedMatrices.get(i);
			tempMatrix.set(modelMatrix);
			tempMatrix.mul(matrix);
			System.arraycopy(tempMatrix.val, 0, tmpValues, i*16, 16);
			program.setUniformMatrix(bonesName,matrix);
		}
		
		int location = program.getUniformLocation(bonesName);

		this.matrixArray.clear();
		BufferUtils.copy(tmpValues, this.matrixArray, length*16, 0);
		gl.glUniformMatrix4fv(location, length, false, this.matrixArray);
	}

	@Override
	public MaterialAttribute copy () {
		return new GpuSkinningAttribute(skeleton);
	}

	@Override
	public void set (MaterialAttribute attr) {
		GpuSkinningAttribute gpuAttr = (GpuSkinningAttribute)attr;
		name = gpuAttr.name;
		skeleton = gpuAttr.skeleton;
		modelMatrix.idt();
	}

	private final static Pool<GpuSkinningAttribute> pool = new Pool<GpuSkinningAttribute>() {
		@Override
		protected GpuSkinningAttribute newObject () {
			return new GpuSkinningAttribute();
		}
	};

	@Override
	public MaterialAttribute pooledCopy () {
		GpuSkinningAttribute attr = pool.obtain();
		attr.set(this);
		return attr;
	}

	@Override
	public void free () {
		if (isPooled) pool.free(this);
	}
}
