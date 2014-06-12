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

package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Disposable;

/** Renders points, lines, rectangles, filled rectangles and boxes.</p>
 * 
 * This class works with OpenGL ES 1.x and 2.0. In its base configuration a 2D orthographic projection with the origin in the
 * lower left corner is used. Units are given in screen pixels.</p>
 * 
 * To change the projection properties use the {@link #setProjectionMatrix(Matrix4)} method. Usually the {@link Camera#combined}
 * matrix is set via this method. If the screen orientation or resolution changes, the projection matrix might have to be adapted
 * as well.</p>
 * 
 * Shapes are rendered in batches to increase performance. The standard use-pattern looks as follows:
 * 
 * <pre>
 * {@code
 * camera.update();
 * shapeRenderer.setProjectionMatrix(camera.combined);
 * 
 * shapeRenderer.begin(ShapeType.Line);
 * shapeRenderer.setColor(1, 1, 0, 1);
 * shapeRenderer.line(x, y, x2, y2);
 * shapeRenderer.rect(x, y, width, height);
 * shapeRenderer.circle(x, y, radius);
 * shapeRenderer.end();
 * 
 * shapeRenderer.begin(ShapeType.Filled);
 * shapeRenderer.setColor(0, 1, 0, 1);
 * shapeRenderer.rect(x, y, width, height);
 * shapeRenderer.circle(x, y, radius);
 * shapeRenderer.end();
 * }
 * </pre>
 * 
 * The class has a second matrix called the transformation matrix which is used to rotate, scale and translate shapes in a more
 * flexible manner. This mechanism works much like matrix operations in OpenGL ES 1.x. The following example shows how to rotate a
 * rectangle around its center using the z-axis as the rotation axis and placing it's center at (20, 12, 2):
 * 
 * <pre>
 * shapeRenderer.begin(ShapeType.Line);
 * shapeRenderer.identity();
 * shapeRenderer.translate(20, 12, 2);
 * shapeRenderer.rotate(0, 0, 1, 90);
 * shapeRenderer.rect(-width / 2, -height / 2, width, height);
 * shapeRenderer.end();
 * </pre>
 * 
 * Matrix operations all use postmultiplication and work just like glTranslate, glScale and glRotate. The last transformation
 * specified will be the first that is applied to a shape (rotate then translate in the above example).
 * 
 * The projection and transformation matrices are a state of the ShapeRenderer, just like the color and will be applied to all
 * shapes until they are changed.
 * 
 * @author mzechner
 * @author stbachmann
 * @author Nathan Sweet */
public class ShapeRenderer implements Disposable {
	/** Shape types to be used with {@link #begin(ShapeType)}.
	 * @author mzechner, stbachmann */
	public enum ShapeType {
		Point(GL20.GL_POINTS), Line(GL20.GL_LINES), Filled(GL20.GL_TRIANGLES);

		private final int glType;

		ShapeType (int glType) {
			this.glType = glType;
		}

		public int getGlType () {
			return glType;
		}
	}

	ImmediateModeRenderer renderer;
	boolean matrixDirty = false;
	Matrix4 projView = new Matrix4();
	Matrix4 transform = new Matrix4();
	Matrix4 combined = new Matrix4();
	Vector2 tmp = new Vector2();
	Color color = new Color(1, 1, 1, 1);
	ShapeType currType = null;

	public ShapeRenderer () {
		this(5000);
	}

	public ShapeRenderer (int maxVertices) {
		renderer = new ImmediateModeRenderer20(maxVertices, false, true, 0);
		projView.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		matrixDirty = true;
	}

	/** Sets the {@link Color} to be used by shapes. */
	public void setColor (Color color) {
		this.color.set(color);
	}

	/** Sets the {@link Color} to be used by shapes. */
	public void setColor (float r, float g, float b, float a) {
		this.color.set(r, g, b, a);
	}

	public Color getColor () {
		return color;
	}

	/** Sets the projection matrix to be used for rendering. Usually this will be set to {@link Camera#combined}.
	 * @param matrix */
	public void setProjectionMatrix (Matrix4 matrix) {
		projView.set(matrix);
		matrixDirty = true;
	}

	/** If the matrix is modified, {@link #updateMatrices()} must be called. */
	public Matrix4 getProjectionMatrix () {
		return projView;
	}

	public void updateMatrices () {
		matrixDirty = true;
	}

	public void setTransformMatrix (Matrix4 matrix) {
		transform.set(matrix);
		matrixDirty = true;
	}

	/** If the matrix is modified, {@link #updateMatrices()} must be called. */
	public Matrix4 getTransformMatrix () {
		return transform;
	}

	/** Sets the transformation matrix to identity. */
	public void identity () {
		transform.idt();
		matrixDirty = true;
	}

	/** Multiplies the current transformation matrix by a translation matrix. */
	public void translate (float x, float y, float z) {
		transform.translate(x, y, z);
		matrixDirty = true;
	}

	/** Multiplies the current transformation matrix by a rotation matrix.
	 * @param angle angle in degrees */
	public void rotate (float axisX, float axisY, float axisZ, float angle) {
		transform.rotate(axisX, axisY, axisZ, angle);
		matrixDirty = true;
	}

	/** Multiplies the current transformation matrix by a scale matrix. */
	public void scale (float scaleX, float scaleY, float scaleZ) {
		transform.scale(scaleX, scaleY, scaleZ);
		matrixDirty = true;
	}

	/** Starts a new batch of shapes. All shapes within the batch have to have the type specified. E.g. if {@link ShapeType#Point}
	 * is specified, only call #point().
	 * 
	 * The call to this method must be paired with a call to {@link #end()}.
	 * 
	 * In case OpenGL ES 1.x is used, the projection and modelview matrix will be modified.
	 * @param type the {@link ShapeType}. */
	public void begin (ShapeType type) {
		if (currType != null) throw new GdxRuntimeException("Call end() before beginning a new shape batch");
		currType = type;
		if (matrixDirty) {
			combined.set(projView);
			Matrix4.mul(combined.val, transform.val);
			matrixDirty = false;
		}
		renderer.begin(combined, currType.getGlType());
	}

	/** Draws a point. The {@link ShapeType} passed to begin has to be {@link ShapeType#Point}.
	 * @param x
	 * @param y
	 * @param z */
	public void point (float x, float y, float z) {
		if (currType != ShapeType.Point) throw new GdxRuntimeException("Must call begin(ShapeType.Point)");
		checkDirty();
		checkFlush(1);
		renderer.color(color);
		renderer.vertex(x, y, z);
	}

	/** Draws a line. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}. */
	public final void line (float x, float y, float z, float x2, float y2, float z2) {
		line(x, y, z, x2, y2, z2, color, color);
	}

	/** Draws a line. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}. Lazy method that "just" calls the
	 * "other" method and unpacks the Vector3 for you */
	public final void line (Vector3 v0, Vector3 v1) {
		line(v0.x, v0.y, v0.z, v1.x, v1.y, v1.z, color, color);
	}

	/** Draws a line in the x/y plane. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}. */
	public final void line (float x, float y, float x2, float y2) {
		line(x, y, 0.0f, x2, y2, 0.0f, color, color);
	}

	/** Draws a line. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}. Lazy method that "just" calls the
	 * "other" method and unpacks the Vector2 for you */
	public final void line (Vector2 v0, Vector2 v1) {
		line(v0.x, v0.y, 0.0f, v1.x, v1.y, 0.0f, color, color);
	}

	/** Draws a line in the x/y plane. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}. The line is drawn
	 * with 2 colors interpolated between start & end point.
	 * @param c1 Color at start of the line
	 * @param c2 Color at end of the line */
	public final void line (float x, float y, float x2, float y2, Color c1, Color c2) {
		line(x, y, 0.0f, x2, y2, 0.0f, c1, c2);
	}

	/** Draws a line. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}. The line is drawn with 2 colors
	 * interpolated between start & end point.
	 * @param c1 Color at start of the line
	 * @param c2 Color at end of the line */
	public void line (float x, float y, float z, float x2, float y2, float z2, Color c1, Color c2) {
		if (currType != ShapeType.Line) throw new GdxRuntimeException("Must call begin(ShapeType.Line)");
		checkDirty();
		checkFlush(2);
		renderer.color(c1.r, c1.g, c1.b, c1.a);
		renderer.vertex(x, y, z);
		renderer.color(c2.r, c2.g, c2.b, c2.a);
		renderer.vertex(x2, y2, z2);
	}

	public void curve (float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2, int segments) {
		if (currType != ShapeType.Line) throw new GdxRuntimeException("Must call begin(ShapeType.Line)");
		checkDirty();
		checkFlush(segments * 2 + 2);

		// Algorithm from: http://www.antigrain.com/research/bezier_interpolation/index.html#PAGE_BEZIER_INTERPOLATION
		float subdiv_step = 1f / segments;
		float subdiv_step2 = subdiv_step * subdiv_step;
		float subdiv_step3 = subdiv_step * subdiv_step * subdiv_step;

		float pre1 = 3 * subdiv_step;
		float pre2 = 3 * subdiv_step2;
		float pre4 = 6 * subdiv_step2;
		float pre5 = 6 * subdiv_step3;

		float tmp1x = x1 - cx1 * 2 + cx2;
		float tmp1y = y1 - cy1 * 2 + cy2;

		float tmp2x = (cx1 - cx2) * 3 - x1 + x2;
		float tmp2y = (cy1 - cy2) * 3 - y1 + y2;

		float fx = x1;
		float fy = y1;

		float dfx = (cx1 - x1) * pre1 + tmp1x * pre2 + tmp2x * subdiv_step3;
		float dfy = (cy1 - y1) * pre1 + tmp1y * pre2 + tmp2y * subdiv_step3;

		float ddfx = tmp1x * pre4 + tmp2x * pre5;
		float ddfy = tmp1y * pre4 + tmp2y * pre5;

		float dddfx = tmp2x * pre5;
		float dddfy = tmp2y * pre5;

		while (segments-- > 0) {
			renderer.color(color);
			renderer.vertex(fx, fy, 0);
			fx += dfx;
			fy += dfy;
			dfx += ddfx;
			dfy += ddfy;
			ddfx += dddfx;
			ddfy += dddfy;
			renderer.color(color);
			renderer.vertex(fx, fy, 0);
		}
		renderer.color(color);
		renderer.vertex(fx, fy, 0);
		renderer.color(color);
		renderer.vertex(x2, y2, 0);
	}

	/** Draws a triangle in x/y plane. The {@link ShapeType} passed to begin has to be {@link ShapeType#Filled} or
	 * {@link ShapeType#Line}.
	 * @param x1 x of first point
	 * @param y1 y of first point
	 * @param x2 x of second point
	 * @param y2 y of second point
	 * @param x3 x of third point
	 * @param y3 y of third point */
	public void triangle (float x1, float y1, float x2, float y2, float x3, float y3) {
		if (currType != ShapeType.Filled && currType != ShapeType.Line)
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");
		checkDirty();
		checkFlush(6);
		if (currType == ShapeType.Line) {
			renderer.color(color);
			renderer.vertex(x1, y1, 0);
			renderer.color(color);
			renderer.vertex(x2, y2, 0);

			renderer.color(color);
			renderer.vertex(x2, y2, 0);
			renderer.color(color);
			renderer.vertex(x3, y3, 0);

			renderer.color(color);
			renderer.vertex(x3, y3, 0);
			renderer.color(color);
			renderer.vertex(x1, y1, 0);
		} else {
			renderer.color(color);
			renderer.vertex(x1, y1, 0);
			renderer.color(color);
			renderer.vertex(x2, y2, 0);
			renderer.color(color);
			renderer.vertex(x3, y3, 0);
		}
	}

	/** Draws a triangle in x/y plane with coloured corners. The {@link ShapeType} passed to begin has to be
	 * {@link ShapeType#Filled} or {@link ShapeType#Line}.
	 * @param x1 x of first point
	 * @param y1 y of first point
	 * @param x2 x of second point
	 * @param y2 y of second point
	 * @param x3 x of third point
	 * @param y3 y of third point
	 * @param col1 color of the point defined by x1 and y1
	 * @param col2 color of the point defined by x2 and y2
	 * @param col3 color of the point defined by x3 and y3 */
	public void triangle (float x1, float y1, float x2, float y2, float x3, float y3, Color col1, Color col2, Color col3) {
		if (currType != ShapeType.Filled && currType != ShapeType.Line)
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");
		checkDirty();
		checkFlush(6);
		if (currType == ShapeType.Line) {
			renderer.color(col1.r, col1.g, col1.b, col1.a);
			renderer.vertex(x1, y1, 0);
			renderer.color(col2.r, col2.g, col2.b, col2.a);
			renderer.vertex(x2, y2, 0);

			renderer.color(col2.r, col2.g, col2.b, col2.a);
			renderer.vertex(x2, y2, 0);
			renderer.color(col3.r, col3.g, col3.b, col3.a);
			renderer.vertex(x3, y3, 0);

			renderer.color(col3.r, col3.g, col3.b, col3.a);
			renderer.vertex(x3, y3, 0);
			renderer.color(col1.r, col1.g, col1.b, col1.a);
			renderer.vertex(x1, y1, 0);
		} else {
			renderer.color(col1.r, col1.g, col1.b, col1.a);
			renderer.vertex(x1, y1, 0);
			renderer.color(col2.r, col2.g, col2.b, col2.a);
			renderer.vertex(x2, y2, 0);
			renderer.color(col3.r, col3.g, col3.b, col3.a);
			renderer.vertex(x3, y3, 0);
		}
	}

	/** Draws a rectangle in the x/y plane. The x and y coordinate specify the bottom left corner of the rectangle. The
	 * {@link ShapeType} passed to begin has to be {@link ShapeType#Filled} or {@link ShapeType#Line}. */
	public void rect (float x, float y, float width, float height) {
		if (currType != ShapeType.Filled && currType != ShapeType.Line)
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");

		checkDirty();
		checkFlush(8);

		if (currType == ShapeType.Line) {
			renderer.color(color);
			renderer.vertex(x, y, 0);
			renderer.color(color);
			renderer.vertex(x + width, y, 0);

			renderer.color(color);
			renderer.vertex(x + width, y, 0);
			renderer.color(color);
			renderer.vertex(x + width, y + height, 0);

			renderer.color(color);
			renderer.vertex(x + width, y + height, 0);
			renderer.color(color);
			renderer.vertex(x, y + height, 0);

			renderer.color(color);
			renderer.vertex(x, y + height, 0);
			renderer.color(color);
			renderer.vertex(x, y, 0);
		} else {
			renderer.color(color);
			renderer.vertex(x, y, 0);
			renderer.color(color);
			renderer.vertex(x + width, y, 0);
			renderer.color(color);
			renderer.vertex(x + width, y + height, 0);

			renderer.color(color);
			renderer.vertex(x + width, y + height, 0);
			renderer.color(color);
			renderer.vertex(x, y + height, 0);
			renderer.color(color);
			renderer.vertex(x, y, 0);
		}
	}

	/** Draws a rectangle in the x/y plane. The x and y coordinate specify the bottom left corner of the rectangle. The
	 * {@link ShapeType} passed to begin has to be {@link ShapeType#Filled} or {@link ShapeType#Line}.
	 * @param col1 The color at (x, y)
	 * @param col2 The color at (x + width, y)
	 * @param col3 The color at (x + width, y + height)
	 * @param col4 The color at (x, y + height) */
	public void rect (float x, float y, float width, float height, Color col1, Color col2, Color col3, Color col4) {
		if (currType != ShapeType.Filled && currType != ShapeType.Line)
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");

		checkDirty();
		checkFlush(8);

		if (currType == ShapeType.Line) {
			renderer.color(col1.r, col1.g, col1.b, col1.a);
			renderer.vertex(x, y, 0);
			renderer.color(col2.r, col2.g, col2.b, col2.a);
			renderer.vertex(x + width, y, 0);

			renderer.color(col2.r, col2.g, col2.b, col2.a);
			renderer.vertex(x + width, y, 0);
			renderer.color(col3.r, col3.g, col3.b, col3.a);
			renderer.vertex(x + width, y + height, 0);

			renderer.color(col3.r, col3.g, col3.b, col3.a);
			renderer.vertex(x + width, y + height, 0);
			renderer.color(col4.r, col4.g, col4.b, col4.a);
			renderer.vertex(x, y + height, 0);

			renderer.color(col4.r, col4.g, col4.b, col4.a);
			renderer.vertex(x, y + height, 0);
			renderer.color(col1.r, col1.g, col1.b, col1.a);
			renderer.vertex(x, y, 0);
		} else {
			renderer.color(col1.r, col1.g, col1.b, col1.a);
			renderer.vertex(x, y, 0);
			renderer.color(col2.r, col2.g, col2.b, col2.a);
			renderer.vertex(x + width, y, 0);
			renderer.color(col3.r, col3.g, col3.b, col3.a);
			renderer.vertex(x + width, y + height, 0);

			renderer.color(col3.r, col3.g, col3.b, col3.a);
			renderer.vertex(x + width, y + height, 0);
			renderer.color(col4.r, col4.g, col4.b, col4.a);
			renderer.vertex(x, y + height, 0);
			renderer.color(col1.r, col1.g, col1.b, col1.a);
			renderer.vertex(x, y, 0);
		}
	}

	/** Draws a rectangle in the x/y plane. The x and y coordinate specify the bottom left corner of the rectangle. The originX and
	 * originY specify the point about which to rotate the rectangle. The rotation is in degrees. The {@link ShapeType} passed to
	 * begin has to be {@link ShapeType#Filled} or {@link ShapeType#Line}. */
	public void rect (float x, float y, float width, float height, float originX, float originY, float rotation) {
		rect(x, y, width, height, originX, originY, rotation, color, color, color, color);
	}

	/** Draws a rectangle in the x/y plane. The x and y coordinate specify the bottom left corner of the rectangle. The originX and
	 * originY specify the point about which to rotate the rectangle. The rotation is in degrees. The {@link ShapeType} passed to
	 * begin has to be {@link ShapeType#Filled} or {@link ShapeType#Line}.
	 * @param col1 The color at (x, y)
	 * @param col2 The color at (x + width, y)
	 * @param col3 The color at (x + width, y + height)
	 * @param col4 The color at (x, y + height) */
	public void rect (float x, float y, float width, float height, float originX, float originY, float rotation, Color col1,
		Color col2, Color col3, Color col4) {
		if (currType != ShapeType.Filled && currType != ShapeType.Line)
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");

		checkDirty();
		checkFlush(8);

		float r = (float)Math.toRadians(rotation);
		float c = (float)Math.cos(r);
		float s = (float)Math.sin(r);

		float x1, y1, x2, y2, x3, y3, x4, y4;

		x1 = x + c * (0 - originX) + -s * (0 - originY) + originX;
		y1 = y + s * (0 - originX) + c * (0 - originY) + originY;

		x2 = x + c * (width - originX) + -s * (0 - originY) + originX;
		y2 = y + s * (width - originX) + c * (0 - originY) + originY;

		x3 = x + c * (width - originX) + -s * (height - originY) + originX;
		y3 = y + s * (width - originX) + c * (height - originY) + originY;

		x4 = x + c * (0 - originX) + -s * (height - originY) + originX;
		y4 = y + s * (0 - originX) + c * (height - originY) + originY;

		if (currType == ShapeType.Line) {
			renderer.color(col1.r, col1.g, col1.b, col1.a);
			renderer.vertex(x1, y1, 0);
			renderer.color(col2.r, col2.g, col2.b, col2.a);
			renderer.vertex(x2, y2, 0);

			renderer.color(col2.r, col2.g, col2.b, col2.a);
			renderer.vertex(x2, y2, 0);
			renderer.color(col3.r, col3.g, col3.b, col3.a);
			renderer.vertex(x3, y3, 0);

			renderer.color(col3.r, col3.g, col3.b, col3.a);
			renderer.vertex(x3, y3, 0);
			renderer.color(col4.r, col4.g, col4.b, col4.a);
			renderer.vertex(x4, y4, 0);

			renderer.color(col4.r, col4.g, col4.b, col4.a);
			renderer.vertex(x4, y4, 0);
			renderer.color(col1.r, col1.g, col1.b, col1.a);
			renderer.vertex(x1, y1, 0);
		} else {
			renderer.color(color);
			renderer.vertex(x1, y1, 0);
			renderer.color(color);
			renderer.vertex(x2, y2, 0);
			renderer.color(color);
			renderer.vertex(x3, y3, 0);

			renderer.color(color);
			renderer.vertex(x3, y3, 0);
			renderer.color(color);
			renderer.vertex(x4, y4, 0);
			renderer.color(color);
			renderer.vertex(x1, y1, 0);
		}

	}

	/** @see #rectLine(float, float, float, float, float) */
	public void rectLine (Vector2 p1, Vector2 p2, float width) {
		rectLine(p1.x, p1.y, p2.x, p2.y, width);
	}

	/** Draws a rectangle with one edge centered at x1, y1 and the opposite edge centered at x2, y2. */
	public void rectLine (float x1, float y1, float x2, float y2, float width) {
		if (currType != ShapeType.Filled && currType != ShapeType.Line)
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");

		checkDirty();
		checkFlush(8);

		Vector2 t = tmp.set(y2 - y1, x1 - x2).nor();
		width *= 0.5f;
		float tx = t.x * width;
		float ty = t.y * width;
		if (currType == ShapeType.Line) {
			renderer.color(color);
			renderer.vertex(x1 + tx, y1 + ty, 0);
			renderer.color(color);
			renderer.vertex(x1 - tx, y1 - ty, 0);

			renderer.color(color);
			renderer.vertex(x2 + tx, y2 + ty, 0);
			renderer.color(color);
			renderer.vertex(x2 - tx, y2 - ty, 0);

			renderer.color(color);
			renderer.vertex(x2 + tx, y2 + ty, 0);
			renderer.color(color);
			renderer.vertex(x1 + tx, y1 + ty, 0);

			renderer.color(color);
			renderer.vertex(x2 - tx, y2 - ty, 0);
			renderer.color(color);
			renderer.vertex(x1 - tx, y1 - ty, 0);
		} else {
			renderer.color(color);
			renderer.vertex(x1 + tx, y1 + ty, 0);
			renderer.color(color);
			renderer.vertex(x1 - tx, y1 - ty, 0);
			renderer.color(color);
			renderer.vertex(x2 + tx, y2 + ty, 0);

			renderer.color(color);
			renderer.vertex(x2 - tx, y2 - ty, 0);
			renderer.color(color);
			renderer.vertex(x2 + tx, y2 + ty, 0);
			renderer.color(color);
			renderer.vertex(x1 - tx, y1 - ty, 0);
		}
	}

	/** Draws a cube. The x, y and z coordinate specify the bottom left front corner of the rectangle. The {@link ShapeType} passed
	 * to begin has to be {@link ShapeType#Line}. */
	public void box (float x, float y, float z, float width, float height, float depth) {
		if (currType != ShapeType.Filled && currType != ShapeType.Line)
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");

		checkDirty();

		depth = -depth;

		if (currType == ShapeType.Line) {
			checkFlush(24);
			renderer.color(color);
			renderer.vertex(x, y, z);
			renderer.color(color);
			renderer.vertex(x + width, y, z);

			renderer.color(color);
			renderer.vertex(x + width, y, z);
			renderer.color(color);
			renderer.vertex(x + width, y, z + depth);

			renderer.color(color);
			renderer.vertex(x + width, y, z + depth);
			renderer.color(color);
			renderer.vertex(x, y, z + depth);

			renderer.color(color);
			renderer.vertex(x, y, z + depth);
			renderer.color(color);
			renderer.vertex(x, y, z);

			renderer.color(color);
			renderer.vertex(x, y, z);
			renderer.color(color);
			renderer.vertex(x, y + height, z);

			renderer.color(color);
			renderer.vertex(x, y + height, z);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z);

			renderer.color(color);
			renderer.vertex(x + width, y + height, z);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z + depth);

			renderer.color(color);
			renderer.vertex(x + width, y + height, z + depth);
			renderer.color(color);
			renderer.vertex(x, y + height, z + depth);

			renderer.color(color);
			renderer.vertex(x, y + height, z + depth);
			renderer.color(color);
			renderer.vertex(x, y + height, z);

			renderer.color(color);
			renderer.vertex(x + width, y, z);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z);

			renderer.color(color);
			renderer.vertex(x + width, y, z + depth);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z + depth);

			renderer.color(color);
			renderer.vertex(x, y, z + depth);
			renderer.color(color);
			renderer.vertex(x, y + height, z + depth);
		} else {
			checkFlush(36);
			// Front
			renderer.color(color);
			renderer.vertex(x, y, z);
			renderer.color(color);
			renderer.vertex(x + width, y, z);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z);

			renderer.color(color);
			renderer.vertex(x, y, z);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z);
			renderer.color(color);
			renderer.vertex(x, y + height, z);

			// Back
			renderer.color(color);
			renderer.vertex(x + width, y, z + depth);
			renderer.color(color);
			renderer.vertex(x, y, z + depth);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z + depth);

			renderer.color(color);
			renderer.vertex(x, y + height, z + depth);
			renderer.color(color);
			renderer.vertex(x, y, z + depth);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z + depth);

			// Left
			renderer.color(color);
			renderer.vertex(x, y, z + depth);
			renderer.color(color);
			renderer.vertex(x, y, z);
			renderer.color(color);
			renderer.vertex(x, y + height, z);

			renderer.color(color);
			renderer.vertex(x, y, z + depth);
			renderer.color(color);
			renderer.vertex(x, y + height, z);
			renderer.color(color);
			renderer.vertex(x, y + height, z + depth);

			// Right
			renderer.color(color);
			renderer.vertex(x + width, y, z);
			renderer.color(color);
			renderer.vertex(x + width, y, z + depth);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z + depth);

			renderer.color(color);
			renderer.vertex(x + width, y, z);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z + depth);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z);

			// Top
			renderer.color(color);
			renderer.vertex(x, y + height, z);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z + depth);

			renderer.color(color);
			renderer.vertex(x, y + height, z);
			renderer.color(color);
			renderer.vertex(x + width, y + height, z + depth);
			renderer.color(color);
			renderer.vertex(x, y + height, z + depth);

			// Bottom
			renderer.color(color);
			renderer.vertex(x, y, z + depth);
			renderer.color(color);
			renderer.vertex(x + width, y, z + depth);
			renderer.color(color);
			renderer.vertex(x + width, y, z);

			renderer.color(color);
			renderer.vertex(x, y, z + depth);
			renderer.color(color);
			renderer.vertex(x + width, y, z);
			renderer.color(color);
			renderer.vertex(x, y, z);
		}

	}

	/** Draws two crossed lines. */
	public void x (float x, float y, float radius) {
		if (currType != ShapeType.Line) throw new GdxRuntimeException("Must call begin(ShapeType.Line)");
		line(x - radius, y - radius, x + radius, y + radius);
		line(x - radius, y + radius, x + radius, y - radius);
	}

	/** @see #x(float, float, float) */
	public void x (Vector2 p, float radius) {
		x(p.x, p.y, radius);
	}

	/** Calls {@link #arc(float, float, float, float, float, int)} by estimating the number of segments needed for a smooth arc. */
	public void arc (float x, float y, float radius, float start, float angle) {
		arc(x, y, radius, start, angle, Math.max(1, (int)(6 * (float)Math.cbrt(radius) * (angle / 360.0f))));
	}

	public void arc (float x, float y, float radius, float start, float angle, int segments) {
		if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
		if (currType != ShapeType.Filled && currType != ShapeType.Line)
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");
		checkDirty();

		float theta = (2 * MathUtils.PI * (angle / 360.0f)) / segments;
		float cos = MathUtils.cos(theta);
		float sin = MathUtils.sin(theta);
		float cx = radius * MathUtils.cos(start * MathUtils.degreesToRadians);
		float cy = radius * MathUtils.sin(start * MathUtils.degreesToRadians);

		if (currType == ShapeType.Line) {
			checkFlush(segments * 2 + 2);

			renderer.color(color);
			renderer.vertex(x, y, 0);
			renderer.color(color);
			renderer.vertex(x + cx, y + cy, 0);
			for (int i = 0; i < segments; i++) {
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, 0);
			}
			renderer.color(color);
			renderer.vertex(x + cx, y + cy, 0);
		} else {
			checkFlush(segments * 3 + 3);
			for (int i = 0; i < segments; i++) {
				renderer.color(color);
				renderer.vertex(x, y, 0);
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, 0);
			}
			renderer.color(color);
			renderer.vertex(x, y, 0);
			renderer.color(color);
			renderer.vertex(x + cx, y + cy, 0);
		}

		float temp = cx;
		cx = 0;
		cy = 0;
		renderer.color(color);
		renderer.vertex(x + cx, y + cy, 0);
	}

	/** Calls {@link #circle(float, float, float, int)} by estimating the number of segments needed for a smooth circle. */
	public void circle (float x, float y, float radius) {
		circle(x, y, radius, Math.max(1, (int)(6 * (float)Math.cbrt(radius))));
	}

	public void circle (float x, float y, float radius, int segments) {
		if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
		if (currType != ShapeType.Filled && currType != ShapeType.Line)
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");
		checkDirty();

		float angle = 2 * MathUtils.PI / segments;
		float cos = MathUtils.cos(angle);
		float sin = MathUtils.sin(angle);
		float cx = radius, cy = 0;
		if (currType == ShapeType.Line) {
			checkFlush(segments * 2 + 2);
			for (int i = 0; i < segments; i++) {
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, 0);
			}
			// Ensure the last segment is identical to the first.
			renderer.color(color);
			renderer.vertex(x + cx, y + cy, 0);
		} else {
			checkFlush(segments * 3 + 3);
			segments--;
			for (int i = 0; i < segments; i++) {
				renderer.color(color);
				renderer.vertex(x, y, 0);
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, 0);
			}
			// Ensure the last segment is identical to the first.
			renderer.color(color);
			renderer.vertex(x, y, 0);
			renderer.color(color);
			renderer.vertex(x + cx, y + cy, 0);
		}

		float temp = cx;
		cx = radius;
		cy = 0;
		renderer.color(color);
		renderer.vertex(x + cx, y + cy, 0);
	}

	/** Calls {@link #ellipse(float, float, float, float, int)} by estimating the number of segments needed for a smooth ellipse. */
	public void ellipse (float x, float y, float width, float height) {
		ellipse(x, y, width, height, Math.max(1, (int)(12 * (float)Math.cbrt(Math.max(width * 0.5f, height * 0.5f)))));
	}

	public void ellipse (float x, float y, float width, float height, int segments) {
		if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
		if (currType != ShapeType.Filled && currType != ShapeType.Line)
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");
		checkDirty();
		checkFlush(segments * 3);

		float angle = 2 * MathUtils.PI / segments;

		float cx = x + width / 2, cy = y + height / 2;
		if (currType == ShapeType.Line) {
			for (int i = 0; i < segments; i++) {
				renderer.color(color);
				renderer.vertex(cx + (width * 0.5f * MathUtils.cos(i * angle)), cy + (height * 0.5f * MathUtils.sin(i * angle)), 0);

				renderer.color(color);
				renderer.vertex(cx + (width * 0.5f * MathUtils.cos((i + 1) * angle)),
					cy + (height * 0.5f * MathUtils.sin((i + 1) * angle)), 0);
			}
		} else {
			for (int i = 0; i < segments; i++) {
				renderer.color(color);
				renderer.vertex(cx + (width * 0.5f * MathUtils.cos(i * angle)), cy + (height * 0.5f * MathUtils.sin(i * angle)), 0);

				renderer.color(color);
				renderer.vertex(cx, cy, 0);

				renderer.color(color);
				renderer.vertex(cx + (width * 0.5f * MathUtils.cos((i + 1) * angle)),
					cy + (height * 0.5f * MathUtils.sin((i + 1) * angle)), 0);
			}
		}
	}

	/** Calls {@link #cone(float, float, float, float, float, int)} by estimating the number of segments needed for a smooth
	 * circular base. */
	public void cone (float x, float y, float z, float radius, float height) {
		cone(x, y, z, radius, height, Math.max(1, (int)(4 * (float)Math.sqrt(radius))));
	}

	public void cone (float x, float y, float z, float radius, float height, int segments) {
		if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
		if (currType != ShapeType.Filled && currType != ShapeType.Line)
			throw new GdxRuntimeException("Must call begin(ShapeType.Filled) or begin(ShapeType.Line)");
		checkDirty();
		checkFlush(segments * 4 + 2);
		float angle = 2 * MathUtils.PI / segments;
		float cos = MathUtils.cos(angle);
		float sin = MathUtils.sin(angle);
		float cx = radius, cy = 0;
		if (currType == ShapeType.Line) {
			for (int i = 0; i < segments; i++) {
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, z);
				renderer.color(color);
				renderer.vertex(x, y, z + height);
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, z);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, z);
			}
			// Ensure the last segment is identical to the first.
			renderer.color(color);
			renderer.vertex(x + cx, y + cy, z);
		} else {
			segments--;
			for (int i = 0; i < segments; i++) {
				renderer.color(color);
				renderer.vertex(x, y, z);
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, z);
				float temp = cx;
				float temp2 = cy;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, z);

				renderer.color(color);
				renderer.vertex(x + temp, y + temp2, z);
				renderer.color(color);
				renderer.vertex(x + cx, y + cy, z);
				renderer.color(color);
				renderer.vertex(x, y, z + height);
			}
			// Ensure the last segment is identical to the first.
			renderer.color(color);
			renderer.vertex(x, y, z);
			renderer.color(color);
			renderer.vertex(x + cx, y + cy, z);
		}
		float temp = cx;
		float temp2 = cy;
		cx = radius;
		cy = 0;
		renderer.color(color);
		renderer.vertex(x + cx, y + cy, z);
		if (currType != ShapeType.Line) {
			renderer.color(color);
			renderer.vertex(x + temp, y + temp2, z);
			renderer.color(color);
			renderer.vertex(x + cx, y + cy, z);
			renderer.color(color);
			renderer.vertex(x, y, z + height);
		}
	}

	/** @see #polygon(float[], int, int) */
	public void polygon (float[] vertices) {
		polygon(vertices, 0, vertices.length);
	}

	/** Draws a polygon in the x/y plane. The vertices must contain at least 3 points (6 floats x,y). The {@link ShapeType} passed
	 * to begin has to be {@link ShapeType#Line}.
	 * @param vertices */
	public void polygon (float[] vertices, int offset, int count) {
		if (currType != ShapeType.Line) throw new GdxRuntimeException("Must call begin(ShapeType.Line)");
		if (count < 6) throw new IllegalArgumentException("Polygons must contain at least 3 points.");
		if (count % 2 != 0) throw new IllegalArgumentException("Polygons must have an even number of vertices.");

		checkDirty();
		checkFlush(count);

		float firstX = vertices[0];
		float firstY = vertices[1];

		for (int i = offset, n = offset + count; i < n; i += 2) {
			float x1 = vertices[i];
			float y1 = vertices[i + 1];

			float x2;
			float y2;

			if (i + 2 >= count) {
				x2 = firstX;
				y2 = firstY;
			} else {
				x2 = vertices[i + 2];
				y2 = vertices[i + 3];
			}

			renderer.color(color);
			renderer.vertex(x1, y1, 0);
			renderer.color(color);
			renderer.vertex(x2, y2, 0);
		}
	}

	/** @see #polyline(float[], int, int) */
	public void polyline (float[] vertices) {
		polyline(vertices, 0, vertices.length);
	}

	/** Draws a polyline in the x/y plane. The vertices must contain at least 2 points (4 floats x,y). The {@link ShapeType} passed
	 * to begin has to be {@link ShapeType#Line}.
	 * @param vertices */
	public void polyline (float[] vertices, int offset, int count) {
		if (currType != ShapeType.Line) throw new GdxRuntimeException("Must call begin(ShapeType.Line)");
		if (count < 4) throw new IllegalArgumentException("Polylines must contain at least 2 points.");
		if (count % 2 != 0) throw new IllegalArgumentException("Polylines must have an even number of vertices.");

		checkDirty();
		checkFlush(count);

		for (int i = offset, n = offset + count - 2; i < n; i += 2) {
			float x1 = vertices[i];
			float y1 = vertices[i + 1];

			float x2;
			float y2;

			x2 = vertices[i + 2];
			y2 = vertices[i + 3];

			renderer.color(color);
			renderer.vertex(x1, y1, 0);
			renderer.color(color);
			renderer.vertex(x2, y2, 0);
		}
	}

	private void checkDirty () {
		if (!matrixDirty) return;
		ShapeType type = currType;
		end();
		begin(type);
	}

	private void checkFlush (int newVertices) {
		if (renderer.getMaxVertices() - renderer.getNumVertices() >= newVertices) return;
		ShapeType type = currType;
		end();
		begin(type);
	}

	/** Finishes the batch of shapes and ensures they get rendered. */
	public void end () {
		renderer.end();
		currType = null;
	}

	public void flush () {
		ShapeType type = currType;
		end();
		begin(type);
	}

	/** Returns the current {@link ShapeType} used */
	public ShapeType getCurrentType () {
		return currType;
	}

	public ImmediateModeRenderer getRenderer () {
		return renderer;
	}

	public void dispose () {
		renderer.dispose();
	}
}
