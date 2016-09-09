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

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Affine2Test extends GdxTest {

	static public final float TOLERANCE = 0.005f;

	@Override
	public void create () {
		Vector2 trn = new Vector2(30, 50);
		float rot = 35;
		float cos = (float)Math.cos(MathUtils.degreesToRadians * rot);
		float sin = (float)Math.sin(MathUtils.degreesToRadians * rot);
		Vector2 scl = new Vector2(0.42f, 1.19f);
		Vector2 shear = new Vector2(0.35f, 0.71f);

		Matrix3 mat1 = new Matrix3();
		Matrix3 mat2 = new Matrix3();
		Affine2 afn1 = new Affine2();
		Affine2 afn2 = new Affine2();

		// check setter - identity
		checkEqual(mat1, new float[] {1, 0, 0, 0, 1, 0, 0, 0, 1});
		checkEqual(mat1, mat2.idt());
		checkEqual(mat1, afn1);
		checkEqual(mat1, afn1.idt());

		// check setter - translation
		mat1.setToTranslation(trn);
		checkEqual(mat1, new float[] {1, 0, 0, 0, 1, 0, trn.x, trn.y, 1});
		afn1.setToTranslation(trn);
		checkEqual(mat1, afn1);

		// check setter - scale
		mat1.setToScaling(scl);
		checkEqual(mat1, new float[] {scl.x, 0, 0, 0, scl.y, 0, 0, 0, 1});
		afn1.setToScaling(scl);
		checkEqual(mat1, afn1);

		// check setter - rotation
		mat1.setToRotation(rot);
		checkEqual(mat1, new float[] {cos, sin, 0, -sin, cos, 0, 0, 0, 1});
		afn1.setToRotation(rot);
		checkEqual(mat1, afn1);
		mat1.setToRotationRad(MathUtils.degreesToRadians * rot);
		checkEqual(mat1, afn1);
		afn1.setToRotationRad(MathUtils.degreesToRadians * rot);
		checkEqual(mat1, afn1);

		// check setter - shearing
		afn1.setToShearing(shear);
		checkEqual(mat1.set(afn1), new float[] {1, shear.y, 0, shear.x, 1, 0, 0, 0, 1});

		// check setter - translation x rotation x scale
		afn1.setToTrnRotScl(trn, rot, scl);
		afn2.setToTrnRotRadScl(trn, MathUtils.degreesToRadians * rot, scl);
		checkEqual(afn1, afn2);
		afn2.setToTranslation(trn).rotate(rot).scale(scl);
		checkEqual(afn1, afn2);

		// check setter - translation x scale
		afn1.setToTrnRotScl(trn, 0, scl);
		afn2.setToTrnScl(trn, scl);
		checkEqual(afn1, afn2);

		// check post-multiplication
		mat1.idt().scale(scl).rotate(rot).translate(trn).mul(mat2.set(afn2.setToShearing(shear)));
		afn1.idt().scale(scl).rotate(rot).translate(trn).shear(shear);
		checkEqual(mat1, afn1);
		afn1.idt().mul(afn2.setToScaling(scl)).mul(afn2.setToRotation(rot)).mul(afn2.setToTranslation(trn))
			.mul(afn2.setToShearing(shear));
		checkEqual(mat1, afn1);

		// check pre-multiplication
		afn1.idt().preShear(shear).preTranslate(trn).preRotate(rot).preScale(scl);
		checkEqual(mat1, afn1);
		afn1.idt().preMul(afn2.setToShearing(shear)).preMul(afn2.setToTranslation(trn)).preMul(afn2.setToRotation(rot))
			.preMul(afn2.setToScaling(scl));
		checkEqual(mat1, afn1);
		mat1.set(afn2.setToShearing(shear)).trn(trn).mulLeft(mat2.setToRotation(rot)).mulLeft(mat2.setToScaling(scl));
		checkEqual(mat1, afn1);

		// check determinant and inverse
		checkEqual(mat1.det(), afn1.det());
		check(afn1.det() == (afn1.m00 * afn1.m11 - afn1.m01 * afn1.m10));
		mat1.inv();
		afn2.set(afn1).inv();
		checkEqual(mat1, afn2);
		checkEqual(afn1.det(), 1 / afn2.det());

		// check for exception when trying to invert singular matrices
		boolean didThrow = false;
		afn1.setToShearing(1, 1);
		try {
			afn1.inv();
		} catch (GdxRuntimeException e) {
			didThrow = true;
		}
		check(didThrow);

		System.out.println("All tests passed.");
	}

	private static void check (boolean condition) {
		if (!condition) throw new GdxRuntimeException("");
	}

	private static void check (boolean condition, String msg) {
		if (!condition) throw new GdxRuntimeException(msg);
	}

	private static void checkEqual (Matrix3 matrix, Affine2 affine) {
		checkEqual(matrix, new Matrix3().set(affine));
	}

	private static void checkEqual (Affine2 a, Affine2 b) {
		checkEqual(new Matrix3().set(a), new Matrix3().set(b));
	}

	private static void checkEqual (Matrix3 a, Matrix3 b) {
		for (int i = 0; i < 9; i++)
			check(MathUtils.isEqual(a.val[i], b.val[i], TOLERANCE), "matrices are not equal");
	}

	private static void checkEqual (Matrix3 matrix, float[] vals) {
		for (int i = 0; i < 9; i++)
			check(MathUtils.isEqual(matrix.val[i], vals[i], TOLERANCE), "matrices are not equal");
	}

	private static void checkEqual (float a, float b) {
		check(MathUtils.isEqual(a, b, TOLERANCE));
	}
}
