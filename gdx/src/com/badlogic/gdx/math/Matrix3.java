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
	public float[] vals = new float[9];
	private float[] tmp = new float[9];

	public Matrix3 () {
		idt();
	}

	/** Sets this matrix to the identity matrix
	 * @return this matrix */
	public Matrix3 idt () {
		this.vals[0] = 1;
		this.vals[1] = 0;
		this.vals[2] = 0;

		this.vals[3] = 0;
		this.vals[4] = 1;
		this.vals[5] = 0;

		this.vals[6] = 0;
		this.vals[7] = 0;
		this.vals[8] = 1;

		return this;
	}

	/** Multiplies this matrix with the other matrix in the order this * m.
	 * @return this matrix */
	public Matrix3 mul (Matrix3 m) {
		float v00 = vals[0] * m.vals[0] + vals[3] * m.vals[1] + vals[6] * m.vals[2];
		float v01 = vals[0] * m.vals[3] + vals[3] * m.vals[4] + vals[6] * m.vals[5];
		float v02 = vals[0] * m.vals[6] + vals[3] * m.vals[7] + vals[6] * m.vals[8];

		float v10 = vals[1] * m.vals[0] + vals[4] * m.vals[1] + vals[7] * m.vals[2];
		float v11 = vals[1] * m.vals[3] + vals[4] * m.vals[4] + vals[7] * m.vals[5];
		float v12 = vals[1] * m.vals[6] + vals[4] * m.vals[7] + vals[7] * m.vals[8];

		float v20 = vals[2] * m.vals[0] + vals[5] * m.vals[1] + vals[8] * m.vals[2];
		float v21 = vals[2] * m.vals[3] + vals[5] * m.vals[4] + vals[8] * m.vals[5];
		float v22 = vals[2] * m.vals[6] + vals[5] * m.vals[7] + vals[8] * m.vals[8];

		vals[0] = v00;
		vals[1] = v10;
		vals[2] = v20;
		vals[3] = v01;
		vals[4] = v11;
		vals[5] = v21;
		vals[6] = v02;
		vals[7] = v12;
		vals[8] = v22;

		return this;
	}

	/** Sets this matrix to a rotation matrix that will rotate any vector in counter clockwise order around the z-axis.
	 * @param angle the angle in degrees.
	 * @return this matrix */
	public Matrix3 setToRotation (float angle) {
		angle = DEGREE_TO_RAD * angle;
		float cos = (float)Math.cos(angle);
		float sin = (float)Math.sin(angle);

		this.vals[0] = cos;
		this.vals[1] = sin;
		this.vals[2] = 0;

		this.vals[3] = -sin;
		this.vals[4] = cos;
		this.vals[5] = 0;

		this.vals[6] = 0;
		this.vals[7] = 0;
		this.vals[8] = 1;

		return this;
	}

	/** Sets this matrix to a translation matrix.
	 * @param x the translation in x
	 * @param y the translation in y
	 * @return this matrix */
	public Matrix3 setToTranslation (float x, float y) {
		this.vals[0] = 1;
		this.vals[1] = 0;
		this.vals[2] = 0;

		this.vals[3] = 0;
		this.vals[4] = 1;
		this.vals[5] = 0;

		this.vals[6] = x;
		this.vals[7] = y;
		this.vals[8] = 1;

		return this;
	}

	/** Sets this matrix to a scaling matrix
	 * 
	 * @param scaleX the scale in x
	 * @param scaleY the scale in y
	 * @return this matrix */
	public Matrix3 setToScaling (float scaleX, float scaleY) {
		this.vals[0] = scaleX;
		this.vals[1] = 0;
		this.vals[2] = 0;

		this.vals[3] = 0;
		this.vals[4] = scaleY;
		this.vals[5] = 0;

		this.vals[6] = 0;
		this.vals[7] = 0;
		this.vals[8] = 1;

		return this;
	}

	public String toString () {
		return "[" + vals[0] + "|" + vals[3] + "|" + vals[6] + "]\n" + "[" + vals[1] + "|" + vals[4] + "|" + vals[7] + "]\n" + "["
			+ vals[2] + "|" + vals[5] + "|" + vals[8] + "]";
	}

	/** @return the determinant of this matrix */
	public float det () {
		return vals[0] * vals[4] * vals[8] + vals[3] * vals[7] * vals[2] + vals[6] * vals[1] * vals[5] - vals[0] * vals[7]
			* vals[5] - vals[3] * vals[1] * vals[8] - vals[6] * vals[4] * vals[2];
	}

	/** Inverts this matrix given that the determinant is != 0
	 * @return this matrix */
	public Matrix3 inv () {
		float det = det();
		if (det == 0) throw new GdxRuntimeException("Can't invert a singular matrix");

		float inv_det = 1.0f / det;
		float tmp[] = {0, 0, 0, 0, 0, 0, 0, 0, 0};

		tmp[0] = vals[4] * vals[8] - vals[5] * vals[7];
		tmp[1] = vals[2] * vals[7] - vals[1] * vals[8];
		tmp[2] = vals[1] * vals[5] - vals[2] * vals[4];
		tmp[3] = vals[5] * vals[6] - vals[3] * vals[8];
		tmp[4] = vals[0] * vals[8] - vals[2] * vals[6];
		tmp[5] = vals[2] * vals[3] - vals[0] * vals[5];
		tmp[6] = vals[3] * vals[7] - vals[4] * vals[6];
		tmp[7] = vals[1] * vals[6] - vals[0] * vals[7];
		tmp[8] = vals[0] * vals[4] - vals[1] * vals[3];

		vals[0] = inv_det * tmp[0];
		vals[1] = inv_det * tmp[1];
		vals[2] = inv_det * tmp[2];
		vals[3] = inv_det * tmp[3];
		vals[4] = inv_det * tmp[4];
		vals[5] = inv_det * tmp[5];
		vals[6] = inv_det * tmp[6];
		vals[7] = inv_det * tmp[7];
		vals[8] = inv_det * tmp[8];

		return this;
	}

	public Matrix3 set (Matrix3 mat) {
		vals[0] = mat.vals[0];
		vals[1] = mat.vals[1];
		vals[2] = mat.vals[2];
		vals[3] = mat.vals[3];
		vals[4] = mat.vals[4];
		vals[5] = mat.vals[5];
		vals[6] = mat.vals[6];
		vals[7] = mat.vals[7];
		vals[8] = mat.vals[8];
		return this;
	}

	/** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
	 * @param vector The translation vector
	 * @return This matrix for chaining */
	public Matrix3 trn (Vector3 vector) {
		vals[6] += vector.x;
		vals[7] += vector.y;
		return this;
	}

	/** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
	 * @param x The x-component of the translation vector
	 * @param y The y-component of the translation vector
	 * @return This matrix for chaining */
	public Matrix3 trn (float x, float y) {
		vals[6] += x;
		vals[7] += y;
		return this;
	}
	/**
	 * Postmultiplies this matrix by a translation matrix. Postmultiplication is
	 * also used by OpenGL ES' glTranslate/glRotate/glScale
	 * @param x
	 * @param y
	 * @return this matrix for chaining
	 */
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
		mul(vals, tmp);
		return this;
	}
	
	
	/**
	 * Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is
	 * also used by OpenGL ES' glTranslate/glRotate/glScale
	 * @param angle the angle in degrees
	 * @return this matrix for chaining
	 */
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
		mul(vals, tmp);
		return this;
	}

	/**
	 * Postmultiplies this matrix with a scale matrix. Postmultiplication is
	 * also used by OpenGL ES' glTranslate/glRotate/glScale.
	 * @param scaleX
	 * @param scaleY
	 * @return this matrix for chaining
	 */
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
		mul(vals, tmp);
		return this;
	}

	public float[] getValues () {
		return vals;
	}
	
	private static void mul(float[] mata, float[] matb) {
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
