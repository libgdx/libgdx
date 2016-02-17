package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

/** @author realitix */
public class Debug3dRenderer implements Disposable, RenderableProvider {
	private float vertices[];
	private short indices[];
	private int idVertice;
	private int idIndice;
	private Vector3 tmp = new Vector3();
	private Vector3 tmp2 = new Vector3();
	private Renderable renderable = new Renderable();
	private boolean building;
	private Color color0 = new Color();
	private Color color1 = new Color();
	private Color color2 = new Color();
	private Color color3 = new Color();
	private Color color4 = new Color();

	public static int maxVertices = 5000;
	public static int maxIndices = 5000;

	public Debug3dRenderer () {
		// Init mesh
		Mesh mesh = new Mesh(false, maxVertices, maxIndices, new VertexAttribute(Usage.Position, 3, "a_position"),
			new VertexAttribute(Usage.ColorPacked, 4, "a_color"));
		// Init vertices
		vertices = new float[maxVertices * (mesh.getVertexSize() / 4)];
		// Init indices
		indices = new short[maxIndices];

		// Init renderable
		renderable.meshPart.mesh = mesh;
		renderable.meshPart.offset = 0;
		renderable.meshPart.primitiveType = GL20.GL_LINES;
		renderable.material = new Material();
	}

	public void begin () {
		if (building) throw new GdxRuntimeException("Call end() after calling begin()");
		building = true;

		idVertice = 0;
		idIndice = 0;
	}
	
	public void add (Camera camera) {
		add(camera, color0.set(1, 0.66f, 0, 1), color1.set(1, 0, 0, 1), color2.set(0, 0.66f, 1, 1), color3.set(1, 1, 1, 1),
			color4.set(0.2f, 0.2f, 0.2f, 1));
	}

	public void add (Camera camera, Color frustumColor, Color coneColor, Color upColor, Color targetColor, Color crossColor) {
		int ov = idVertice / (renderable.meshPart.mesh.getVertexSize() / 4);
		Vector3[] planePoints = camera.frustum.planePoints;

		// 0 - 7 = Frustum points
		for (int i = 0; i < planePoints.length; i++) {
			vertice(planePoints[i], frustumColor);
		}

		// 8 = Camera position
		vertice(camera.position, coneColor);

		// 9 - 12 = Cross near
		vertice(middlePoint(planePoints[1], planePoints[0]), crossColor);
		vertice(middlePoint(planePoints[3], planePoints[2]), crossColor);
		vertice(middlePoint(planePoints[2], planePoints[1]), crossColor);
		vertice(middlePoint(planePoints[3], planePoints[0]), crossColor);

		// 13 - 16 = Cross far
		vertice(middlePoint(planePoints[5], planePoints[4]), crossColor);
		vertice(middlePoint(planePoints[7], planePoints[6]), crossColor);
		vertice(middlePoint(planePoints[6], planePoints[5]), crossColor);
		vertice(middlePoint(planePoints[7], planePoints[4]), crossColor);

		// 17 - 18 = Target point
		vertice(centerPoint(planePoints[0], planePoints[1], planePoints[2]), crossColor);
		vertice(centerPoint(planePoints[4], planePoints[5], planePoints[6]), targetColor);

		// 19 = Up vertice
		float halfNearSize = tmp.set(planePoints[1]).sub(planePoints[0]).scl(0.5f).len();
		Vector3 centerNear = centerPoint(planePoints[0], planePoints[1], planePoints[2]);
		tmp.set(camera.up).scl(halfNearSize * 2);
		vertice(centerNear.add(tmp), upColor);

		// Set indices
		setIndices(
			// Near
			0, 1, 1, 2, 2, 3, 3, 0,
			// Far
			4, 5, 5, 6, 6, 7, 7, 4,
			// Sides
			0, 4, 1, 5, 2, 6, 3, 7,
			// Cone
			0, 8, 1, 8, 2, 8, 3, 8,
			// Cross near
			9, 10, 11, 12,
			// Cross far
			13, 14, 15, 16,
			// Target (position -> near -> far)
			8, 17, 17, 18,
			// Up triangle
			19, 2, 2, 3, 3, 19);
	}

	public void add(BoundingBox box) {
		add(box, color0.set(1, 0.66f, 0, 1));
	}

	public void add (BoundingBox box, Color color) {
		int ov = idVertice / (renderable.meshPart.mesh.getVertexSize() / 4);

		box.getCorner000(tmp);
		vertice(tmp, color);

		box.getCorner001(tmp);
		vertice(tmp, color);

		box.getCorner011(tmp);
		vertice(tmp, color);

		box.getCorner010(tmp);
		vertice(tmp, color);

		box.getCorner100(tmp);
		vertice(tmp, color);

		box.getCorner101(tmp);
		vertice(tmp, color);

		box.getCorner111(tmp);
		vertice(tmp, color);

		box.getCorner110(tmp);
		vertice(tmp, color);

		// Set indices
		setIndices(
			// Face 1
			ov, ov + 1, ov + 1, ov + 2, ov + 2, ov + 3, ov + 3, ov,
			// Face 2
			ov + 4, ov + 5, ov + 5, ov + 6, ov + 6, ov + 7, ov + 7, ov + 4,
			// Close cube
			ov, ov + 4, ov + 1, ov + 5, ov + 2, ov + 6, ov + 3, ov + 7);
	}

	public void end () {
		if (!building) throw new GdxRuntimeException("Call begin() prior to calling end()");
		building = false;

		renderable.meshPart.mesh.setVertices(vertices, 0, idVertice);
		renderable.meshPart.mesh.setIndices(indices, 0, idIndice);
		renderable.meshPart.size = idIndice;
		renderable.meshPart.update();
	}

	/** Return the middle point of the segment
	 * @param point0 First segment's point
	 * @param point1 Second segment's point
	 * @return the middle point */
	private Vector3 middlePoint (Vector3 point0, Vector3 point1) {
		tmp.set(point1).sub(point0).scl(0.5f);
		return tmp2.set(point0).add(tmp);
	}

	/** Return the center point of the rectangle
	 * @param point0
	 * @param point1
	 * @param point2
	 * @return the center point */
	private Vector3 centerPoint (Vector3 point0, Vector3 point1, Vector3 point2) {
		tmp.set(point1).sub(point0).scl(0.5f);
		tmp2.set(point0).add(tmp);
		tmp.set(point2).sub(point1).scl(0.5f);
		return tmp2.add(tmp);
	}

	/** Fill the vertices array with the point and color
	 * @param point
	 * @param color */
	private void vertice (Vector3 point, Color color) {
		vertices[idVertice++] = point.x;
		vertices[idVertice++] = point.y;
		vertices[idVertice++] = point.z;
		vertices[idVertice++] = color.toFloatBits();
	}

	/** Fill indices array with values
	 * @param values */
	private void setIndices (int... values) {
		for (int v : values) {
			indices[idIndice++] = (short)v;
		}
	}

	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		renderables.add(renderable);
	}

	@Override
	public void dispose () {
		renderable.meshPart.mesh.dispose();
	}
}
