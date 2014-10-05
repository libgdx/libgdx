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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;

public class MatrixJNITest extends GdxTest {
	@Override
	public void create () {
		Matrix4 mat1 = new Matrix4();
		Matrix4 mat2 = new Matrix4();
		Matrix4 mat3 = new Matrix4();
		Vector3 vec = new Vector3(1, 2, 3);
		float[] fvec = {1, 2, 3};
		float[] fvecs = {1, 2, 3, 0, 0, 1, 2, 3, 0, 0, 1, 2, 3, 0, 0};

		mat1.setToRotation(0, 1, 0, 45);
		mat2.setToRotation(1, 0, 0, 45);

		vec.mul(mat1);
		Matrix4.mulVec(mat1.val, fvec);
		Matrix4.mulVec(mat1.val, fvecs, 0, 3, 5);
		check(vec, fvec);
		check(vec, fvecs, 3, 5);

		vec.prj(mat1);
		Matrix4.prj(mat1.val, fvec);
		Matrix4.prj(mat1.val, fvecs, 0, 3, 5);
		check(vec, fvec);
		check(vec, fvecs, 3, 5);

		vec.rot(mat1);
		Matrix4.rot(mat1.val, fvec);
		Matrix4.rot(mat1.val, fvecs, 0, 3, 5);
		check(vec, fvec);
		check(vec, fvecs, 3, 5);

		if (mat1.det() != Matrix4.det(mat1.val)) throw new GdxRuntimeException("det doesn't work");

		mat2.set(mat1);
		mat1.inv();
		Matrix4.inv(mat2.val);
		check(mat1, mat2);

		mat3.set(mat1);
		mat1.mul(mat2);
		Matrix4.mul(mat3.val, mat2.val);
		check(mat1, mat3);

		bench();
		
		System.out.println("All tests passed.");
	}

	private void bench () {
		Matrix4 mata = new Matrix4();
		Matrix4 matb = new Matrix4();

		long start = TimeUtils.nanoTime();
		for (int i = 0; i < 1000000; i++) {
			mata.mul(matb);
		}
		Gdx.app.log("MatrixJNITest", "java matrix * matrix took: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		start = TimeUtils.nanoTime();
		for (int i = 0; i < 1000000; i++) {
			Matrix4.mul(mata.val, matb.val);
		}
		Gdx.app.log("MatrixJNITest", "jni matrix * matrix took: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		Vector3 vec = new Vector3();
		start = TimeUtils.nanoTime();
		for (int i = 0; i < 500000; i++) {
			vec.mul(mata);
		}
		Gdx.app.log("MatrixJNITest", "java vecs * matrix took: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		float[] fvec = new float[3];
		start = TimeUtils.nanoTime();
		for (int i = 0; i < 500000; i++) {
			Matrix4.mulVec(mata.val, fvec);
		}
		Gdx.app.log("MatrixJNITest", "jni vecs * matrix took: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		float[] fvecs = new float[3 * 500000];
		start = TimeUtils.nanoTime();
		Matrix4.mulVec(mata.val, fvecs, 0, 500000, 3);
		Gdx.app.log("MatrixJNITest", "jni bulk vecs * matrix took: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		start = TimeUtils.nanoTime();
		for (int i = 0; i < 1000000; i++) {
			mata.inv();
		}
		Gdx.app.log("MatrixJNITest", "java inv(matrix): " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		start = TimeUtils.nanoTime();
		for (int i = 0; i < 1000000; i++) {
			Matrix4.inv(mata.val);
		}
		Gdx.app.log("MatrixJNITest", "jni inv(matrix): " + (TimeUtils.nanoTime() - start) / 1000000000.0f);
	}

	private void check (Vector3 vec, float[] fvec) {
		if (vec.x != fvec[0] || vec.y != fvec[1] || vec.z != fvec[2]) throw new GdxRuntimeException("vectors are not equal");
	}

	private void check (Vector3 vec, float[] fvec, int numVecs, int stride) {
		int offset = 0;
		for (int i = 0; i < numVecs; i++) {
			if (vec.x != fvec[0] || vec.y != fvec[1] || vec.z != fvec[2]) throw new GdxRuntimeException("vectors are not equal");
		}
	}

	private void check (Matrix4 mat1, Matrix4 mat2) {
		for (int i = 0; i < 16; i++) {
			if (mat1.val[i] != mat2.val[i]) throw new GdxRuntimeException("matrices not equal");
		}
	}
}
