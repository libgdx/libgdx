
package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;

/** FrustumShapeBuilder builds camera or frustum.
 * 
 * @author realitix */
public class FrustumShapeBuilder {

	/** Color used to set default value */
	private static final Color tmpColor = new Color();

	/** Vector3 used during vertices generation */
	private static final Vector3 tmp = new Vector3();
	private static final Vector3 tmp2 = new Vector3();

	/** Build camera with default colors
	 * @param builder MeshPartBuilder
	 * @param camera Camera */
	public static void build (MeshPartBuilder builder, Camera camera) {
		build(builder, camera, tmpColor.set(1, 0.66f, 0, 1).cpy(), tmpColor.set(1, 0, 0, 1).cpy(), tmpColor.set(0, 0.66f, 1, 1)
			.cpy(), tmpColor.set(1, 1, 1, 1).cpy(), tmpColor.set(0.2f, 0.2f, 0.2f, 1).cpy());
	}

	/** Build Camera with custom colors
	 * @param builder
	 * @param camera
	 * @param frustumColor
	 * @param coneColor
	 * @param upColor
	 * @param targetColor
	 * @param crossColor */
	public static void build (MeshPartBuilder builder, Camera camera, Color frustumColor, Color coneColor, Color upColor,
		Color targetColor, Color crossColor) {
		Vector3[] planePoints = camera.frustum.planePoints;

		// Frustum
		build(builder, camera.frustum, frustumColor, crossColor);

		// Cones (camera position to near plane)
		builder.line(planePoints[0], coneColor, camera.position, coneColor);
		builder.line(planePoints[1], coneColor, camera.position, coneColor);
		builder.line(planePoints[2], coneColor, camera.position, coneColor);
		builder.line(planePoints[3], coneColor, camera.position, coneColor);

		// Target line
		builder.line(camera.position, targetColor, centerPoint(planePoints[4], planePoints[5], planePoints[6]), targetColor);

		// Up triangle
		float halfNearSize = tmp.set(planePoints[1]).sub(planePoints[0]).scl(0.5f).len();
		Vector3 centerNear = centerPoint(planePoints[0], planePoints[1], planePoints[2]);
		tmp.set(camera.up).scl(halfNearSize * 2);
		centerNear.add(tmp);

		builder.line(centerNear, upColor, planePoints[2], upColor);
		builder.line(planePoints[2], upColor, planePoints[3], upColor);
		builder.line(planePoints[3], upColor, centerNear, upColor);
	}

	/** Build Frustum with custom colors
	 * @param builder
	 * @param frustum
	 * @param frustumColor
	 * @param crossColor */
	public static void build (MeshPartBuilder builder, Frustum frustum, Color frustumColor, Color crossColor) {
		Vector3[] planePoints = frustum.planePoints;

		// Near
		builder.line(planePoints[0], frustumColor, planePoints[1], frustumColor);
		builder.line(planePoints[1], frustumColor, planePoints[2], frustumColor);
		builder.line(planePoints[2], frustumColor, planePoints[3], frustumColor);
		builder.line(planePoints[3], frustumColor, planePoints[0], frustumColor);

		// Far
		builder.line(planePoints[4], frustumColor, planePoints[5], frustumColor);
		builder.line(planePoints[5], frustumColor, planePoints[6], frustumColor);
		builder.line(planePoints[6], frustumColor, planePoints[7], frustumColor);
		builder.line(planePoints[7], frustumColor, planePoints[4], frustumColor);

		// Sides
		builder.line(planePoints[0], frustumColor, planePoints[4], frustumColor);
		builder.line(planePoints[1], frustumColor, planePoints[5], frustumColor);
		builder.line(planePoints[2], frustumColor, planePoints[6], frustumColor);
		builder.line(planePoints[3], frustumColor, planePoints[7], frustumColor);

		// Cross near
		builder.line(middlePoint(planePoints[1], planePoints[0]), crossColor, middlePoint(planePoints[3], planePoints[2]),
			crossColor);
		builder.line(middlePoint(planePoints[2], planePoints[1]), crossColor, middlePoint(planePoints[3], planePoints[0]),
			crossColor);

		// Cross far
		builder.line(middlePoint(planePoints[5], planePoints[4]), crossColor, middlePoint(planePoints[7], planePoints[6]),
			crossColor);
		builder.line(middlePoint(planePoints[6], planePoints[5]), crossColor, middlePoint(planePoints[7], planePoints[4]),
			crossColor);
	}

	/** Return middle point's segment
	 * @param point0 First segment's point
	 * @param point1 Second segment's point
	 * @return the middle point */
	private static Vector3 middlePoint (Vector3 point0, Vector3 point1) {
		tmp.set(point1).sub(point0).scl(0.5f);
		return tmp2.set(point0).add(tmp);
	}

	/** Return center point's rectangle
	 * @param point0
	 * @param point1
	 * @param point2
	 * @return the center point */
	private static Vector3 centerPoint (Vector3 point0, Vector3 point1, Vector3 point2) {
		tmp.set(point1).sub(point0).scl(0.5f);
		tmp2.set(point0).add(tmp);
		tmp.set(point2).sub(point1).scl(0.5f);
		return tmp2.add(tmp);
	}
}
