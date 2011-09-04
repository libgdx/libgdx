package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Class to render points, lines, triangles and rectangles, either
 * as outlines or filled primitives. This class is not mean to be
 * used for performance sensitive applications.
 * @author mzechner
 *
 */
public class ShapeRenderer {
	/**
	 * shape type to be used with {@link #begin(ShapeType)}.
	 * @author mzechner
	 *
	 */
	public enum ShapeType {
		Point(GL10.GL_POINT),
		Line(GL10.GL_LINES), 
		Rectangle(GL10.GL_LINES),
		FilledRectangle(GL10.GL_TRIANGLES);
		
		private final int glType;

		ShapeType(int glType) {
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
	Matrix4 tmp = new Matrix4();
	Color color = new Color(1, 1, 1, 1);
	ShapeType currType = null;
	
	
	
	public ShapeRenderer() {
		if(Gdx.graphics.isGL20Available())
			renderer = new ImmediateModeRenderer20(false, true, 0);
		else 
			renderer = new ImmediateModeRenderer10();
	}
	
	/**
	 * Sets the {@link Color} to be used by shapes.
	 * @param color
	 */
	public void setColor(Color color) {
		this.color.set(color);
	}
	
	/**
	 * Sets the combined projection and view matrix of a camera for rendering (see {@link Camera#combined}).
	 * Can only be called outside of a begin/end block.
	 * @param matrix
	 */
	public void setCameraMatrix(Matrix4 matrix) {
		projView.set(matrix);
		matrixDirty = true;
	}
	
	/**
	 * Sets the transformation matrix applied to all subsequent shapes
	 * @param matrix
	 */
	public void setTransformMatrix(Matrix4 matrix) {
		transform.set(matrix);
		matrixDirty = true;
	}
	
	/**
	 * Sets the transformation matrix to identity.
	 */
	public void indentity() {
		transform.idt();
		matrixDirty = true;
	}
	
	/**
	 * Multiplies the current transformation matrix by a translation matrix.
	 * @param x
	 * @param y
	 * @param z
	 */
	public void translate(float x, float y, float z) {
		transform.translate(x, y, z);
		matrixDirty = true;
	}
	
	/**
	 * Multiplies the current transformation matrix by a rotation matrix.
	 * @param angle angle in degrees
	 * @param axisX
	 * @param axisY
	 * @param axisZ
	 */
	public void rotate(float axisX, float axisY, float axisZ, float angle) {
		transform.rotate(axisX, axisY, axisY, angle);
		matrixDirty = true;
	}
	
	/**
	 * Multiplies the current transformation matrix by a scale matrix.
	 * @param scaleX
	 * @param scaleY
	 * @param scaleZ
	 */
	public void scale(float scaleX, float scaleY, float scaleZ) {
		transform.scale(scaleX, scaleY, scaleZ);
		matrixDirty = true;
	}
	
	/**
	 * Starts a new batch of shapes. All shapes within the batch have to have
	 * the type specified. E.g. if {@link ShapeType#Point}  is specified, only call #point().
	 * 
	 * The call to this method must be paired with a call to {@link #end()}.
	 * 
	 * In case OpenGL ES 1.x is used, the projection and modelview matrix will
	 * be modified.
	 * 
	 * @param type the {@link ShapeType}.
	 */
	public void begin(ShapeType type) {
		if(currType != null) throw new GdxRuntimeException("Call end() before beginning a new shape batch");
		currType = type;
		if(renderer instanceof ImmediateModeRenderer10) {
			Gdx.gl10.glMatrixMode(GL10.GL_PROJECTION);
			Gdx.gl10.glLoadMatrixf(projView.val, 0);
			Gdx.gl10.glMatrixMode(GL10.GL_MODELVIEW);
			Gdx.gl10.glLoadIdentity();
		} else {
			((ImmediateModeRenderer20)renderer).begin(projView, currType.getGlType());
		}
	}
	
	/**
	 * Draws a point. The {@link ShapeType} passed to begin has to be {@link ShapeType#Point}.
	 * @param x
	 * @param y
	 * @param z
	 */
	public void point(float x, float y, float z) {
		if(currType != ShapeType.Point) throw new GdxRuntimeException("Must call begin(ShapeType.Point)");
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x, y, z);
	}
	
	/** 
	 * Draws a line. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}.
	 * @param x
	 * @param y
	 * @param z
	 * @param x2
	 * @param y2
	 * @param z2
	 */
	public void line(float x, float y, float z, float x2, float y2, float z2) {
		if(currType != ShapeType.Line) throw new GdxRuntimeException("Must call begin(ShapeType.Line)");
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x, y, z);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x2, y2, z2);
	}
	
	/** 
	 * Draws a line in the x/y plane. The {@link ShapeType} passed to begin has to be {@link ShapeType#Line}.
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 */
	public void line(float x, float y, float x2, float y2) {
		if(currType != ShapeType.Line) throw new GdxRuntimeException("Must call begin(ShapeType.Line)");
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x, y, 0);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x2, y2, 0);
	}
	
	/**
	 * Draws a rectangle in the x/y plane. The x and y coordinate specify the bottom left corner
	 * of the rectangle. The {@link ShapeType} passed to begin has to be {@link ShapeType#Rectangle}.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void rect(float x, float y, float width, float height) {
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x, y, 0);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x + width, y, 0);
		
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x + width, y, 0);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x + width, y + height, 0);
		
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x + width, y + height, 0);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x, y + height, 0);
		
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x, y + height, 0);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x, y, 0);
	}
	
	/**
	 * Draws a filled rectangle in the x/y plane. The x and y coordinate specify the bottom left corner
	 * of the rectangle. The {@link ShapeType} passed to begin has to be {@link ShapeType#FilledRectangle}.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void filledRect(float x, float y, float width, float height) {
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x, y, 0);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x + width, y, 0);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x + width, y + height, 0);
		
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x + width, y + height, 0);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x, y + height, 0);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x, y, 0);
	}
	
	/**
	 * Finishes the batch of shapes and ensures they get rendered.
	 */
	public void end() {
		renderer.end();
	}
}
