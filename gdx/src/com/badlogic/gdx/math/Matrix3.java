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

package com.badlogic.gdx.math;

import java.io.Serializable;

import com.badlogic.gdx.utils.GdxRuntimeException;

/** A 3x3 column major matrix for 2D transforms.
 * 
 * @author mzechner */
public class Matrix3 implements Serializable {
	private static final long serialVersionUID = 7907569533774959788L;
	private final static float DEGREE_TO_RAD = (float)Math.PI / 180;
	public static final int M00 = 0;
	public static final int M01 = 3;
	public static final int M02 = 6;
	public static final int M10 = 1;
	public static final int M11 = 4;
	public static final int M12 = 7;
	public static final int M20 = 2;
	public static final int M21 = 5;
	public static final int M22 = 8;
	public float[] val = new float[9];
	private float[] tmp = new float[9];

	public Matrix3 () {
		idt();
	}

	/** Sets this matrix to the identity matrix
	 * @return this matrix */
	public Matrix3 idt () {
		this.val[0] = 1;
		this.val[1] = 0;
		this.val[2] = 0;

		this.val[3] = 0;
		this.val[4] = 1;
		this.val[5] = 0;

		this.val[6] = 0;
		this.val[7] = 0;
		this.val[8] = 1;

		return this;
	}

	/** Multiplies this matrix with the other matrix in the order this * m.
	 * @return this matrix */
	public Matrix3 mul (Matrix3 m) {
		float v00 = val[0] * m.val[0] + val[3] * m.val[1] + val[6] * m.val[2];
		float v01 = val[0] * m.val[3] + val[3] * m.val[4] + val[6] * m.val[5];
		float v02 = val[0] * m.val[6] + val[3] * m.val[7] + val[6] * m.val[8];

		float v10 = val[1] * m.val[0] + val[4] * m.val[1] + val[7] * m.val[2];
		float v11 = val[1] * m.val[3] + val[4] * m.val[4] + val[7] * m.val[5];
		float v12 = val[1] * m.val[6] + val[4] * m.val[7] + val[7] * m.val[8];

		float v20 = val[2] * m.val[0] + val[5] * m.val[1] + val[8] * m.val[2];
		float v21 = val[2] * m.val[3] + val[5] * m.val[4] + val[8] * m.val[5];
		float v22 = val[2] * m.val[6] + val[5] * m.val[7] + val[8] * m.val[8];

		val[0] = v00;
		val[1] = v10;
		val[2] = v20;
		val[3] = v01;
		val[4] = v11;
		val[5] = v21;
		val[6] = v02;
		val[7] = v12;
		val[8] = v22;

		return this;
	}

	/** Sets this matrix to a rotation matrix that will rotate any vector in counter clockwise order around the z-axis.
	 * @param angle the angle in degrees.
	 * @return this matrix */
	public Matrix3 setToRotation (float angle) {
		angle = DEGREE_TO_RAD * angle;
		float cos = (float)Math.cos(angle);
		float sin = (float)Math.sin(angle);

		this.val[0] = cos;
		this.val[1] = sin;
		this.val[2] = 0;

		this.val[3] = -sin;
		this.val[4] = cos;
		this.val[5] = 0;

		this.val[6] = 0;
		this.val[7] = 0;
		this.val[8] = 1;

		return this;
	}

	/** Sets this matrix to a translation matrix.
	 * @param x the translation in x
	 * @param y the translation in y
	 * @return this matrix */
	public Matrix3 setToTranslation (float x, float y) {
		this.val[0] = 1;
		this.val[1] = 0;
		this.val[2] = 0;

		this.val[3] = 0;
		this.val[4] = 1;
		this.val[5] = 0;

		this.val[6] = x;
		this.val[7] = y;
		this.val[8] = 1;

		return this;
	}

	/** Sets this matrix to a scaling matrix
	 * 
	 * @param scaleX the scale in x
	 * @param scaleY the scale in y
	 * @return this matrix */
	public Matrix3 setToScaling (float scaleX, float scaleY) {
		this.val[0] = scaleX;
		this.val[1] = 0;
		this.val[2] = 0;

		this.val[3] = 0;
		this.val[4] = scaleY;
		this.val[5] = 0;

		this.val[6] = 0;
		this.val[7] = 0;
		this.val[8] = 1;

		return this;
	}

	public String toString () {
		return "[" + val[0] + "|" + val[3] + "|" + val[6] + "]\n" + "[" + val[1] + "|" + val[4] + "|" + val[7] + "]\n" + "["
			+ val[2] + "|" + val[5] + "|" + val[8] + "]";
	}

	/** @return the determinant of this matrix */
	public float det () {
		return val[0] * val[4] * val[8] + val[3] * val[7] * val[2] + val[6] * val[1] * val[5] - val[0] * val[7]
			* val[5] - val[3] * val[1] * val[8] - val[6] * val[4] * val[2];
	}

	/** Inverts this matrix given that the determinant is != 0
	 * @return this matrix */
	public Matrix3 inv () {
		float det = det();
		if (det == 0) throw new GdxRuntimeException("Can't invert a singular matrix");

		float inv_det = 1.0f / det;

		tmp[0] = val[4] * val[8] - val[5] * val[7];
		tmp[1] = val[2] * val[7] - val[1] * val[8];
		tmp[2] = val[1] * val[5] - val[2] * val[4];
		tmp[3] = val[5] * val[6] - val[3] * val[8];
		tmp[4] = val[0] * val[8] - val[2] * val[6];
		tmp[5] = val[2] * val[3] - val[0] * val[5];
		tmp[6] = val[3] * val[7] - val[4] * val[6];
		tmp[7] = val[1] * val[6] - val[0] * val[7];
		tmp[8] = val[0] * val[4] - val[1] * val[3];

		val[0] = inv_det * tmp[0];
		val[1] = inv_det * tmp[1];
		val[2] = inv_det * tmp[2];
		val[3] = inv_det * tmp[3];
		val[4] = inv_det * tmp[4];
		val[5] = inv_det * tmp[5];
		val[6] = inv_det * tmp[6];
		val[7] = inv_det * tmp[7];
		val[8] = inv_det * tmp[8];

		return this;
	}

	public Matrix3 set (Matrix3 mat) {
		val[0] = mat.val[0];
		val[1] = mat.val[1];
		val[2] = mat.val[2];
		val[3] = mat.val[3];
		val[4] = mat.val[4];
		val[5] = mat.val[5];
		val[6] = mat.val[6];
		val[7] = mat.val[7];
		val[8] = mat.val[8];
		return this;
	}
	
	public Matrix3 set(Matrix4 mat) {
		val[0] = mat.val[0];
		val[1] = mat.val[1];
		val[2] = mat.val[2];
		val[3] = mat.val[4];
		val[4] = mat.val[5];
		val[5] = mat.val[6];
		val[6] = mat.val[8];
		val[7] = mat.val[9];
		val[8] = mat.val[10];
		return this;
	}

	/** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
	 * @param vector The translation vector
	 * @return This matrix for chaining */
	public Matrix3 trn (Vector3 vector) {
		val[6] += vector.x;
		val[7] += vector.y;
		return this;
	}

	/** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
	 * @param x The x-component of the translation vector
	 * @param y The y-component of the translation vector
	 * @return This matrix for chaining */
	public Matrix3 trn (float x, float y) {
		val[6] += x;
		val[7] += y;
		return this;
	}

	/** Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES'
	 * glTranslate/glRotate/glScale
	 * @param x
	 * @param y
	 * @return this matrix for chaining */
	public Matrix3 translate (float x, float y) {
		tmp[0] = 1;
		tmp[1] = 0;
		tmp[2] = 0;

		tmp[3] = 0;
		tmp[4] = 1;
		tmp[5] = 0;

		tmp[6] = x;
		tmp[7] = y;
		tmp[8] = 1;
		mul(val, tmp);
		return this;
	}

	/** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES'
	 * glTranslate/glRotate/glScale
	 * @param angle the angle in degrees
	 * @return this matrix for chaining */
	public Matrix3 rotate (float angle) {
		if (angle == 0) return this;
		angle = DEGREE_TO_RAD * angle;
		float cos = (float)Math.cos(angle);
		float sin = (float)Math.sin(angle);

		tmp[0] = cos;
		tmp[1] = sin;
		tmp[2] = 0;

		tmp[3] = -sin;
		tmp[4] = cos;
		tmp[5] = 0;

		tmp[6] = 0;
		tmp[7] = 0;
		tmp[8] = 1;
		mul(val, tmp);
		return this;
	}

	/** Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' glTranslate/glRotate/glScale.
	 * @param scaleX
	 * @param scaleY
	 * @return this matrix for chaining */
	public Matrix3 scale (float scaleX, float scaleY) {
		tmp[0] = scaleX;
		tmp[1] = 0;
		tmp[2] = 0;

		tmp[3] = 0;
		tmp[4] = scaleY;
		tmp[5] = 0;

		tmp[6] = 0;
		tmp[7] = 0;
		tmp[8] = 1;
		mul(val, tmp);
		return this;
	}

	public float[] getValues () {
		return val;
	}
	
	public Matrix3 scl (Vector3 scale) {
		val[M00] *= scale.x;
		val[M11] *= scale.y;
		return this;
	}
	
	public Matrix3 scl (float scale) {
		val[M00] *= scale;
		val[M11] *= scale;
		return this;
	}

	private static void mul (float[] mata, float[] matb) {
		float v00 = mata[0] * matb[0] + mata[3] * matb[1] + mata[6] * matb[2];
		float v01 = mata[0] * matb[3] + mata[3] * matb[4] + mata[6] * matb[5];
		float v02 = mata[0] * matb[6] + mata[3] * matb[7] + mata[6] * matb[8];

		float v10 = mata[1] * matb[0] + mata[4] * matb[1] + mata[7] * matb[2];
		float v11 = mata[1] * matb[3] + mata[4] * matb[4] + mata[7] * matb[5];
		float v12 = mata[1] * matb[6] + mata[4] * matb[7] + mata[7] * matb[8];

		float v20 = mata[2] * matb[0] + mata[5] * matb[1] + mata[8] * matb[2];
		float v21 = mata[2] * matb[3] + mata[5] * matb[4] + mata[8] * matb[5];
		float v22 = mata[2] * matb[6] + mata[5] * matb[7] + mata[8] * matb[8];

		mata[0] = v00;
		mata[1] = v10;
		mata[2] = v20;
		mata[3] = v01;
		mata[4] = v11;
		mata[5] = v21;
		mata[6] = v02;
		mata[7] = v12;
		mata[8] = v22;
	}
}
