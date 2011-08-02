package com.badlogic.gdx.box2deditor.renderpanel;

import com.badlogic.gdx.box2deditor.AppContext;
import com.badlogic.gdx.box2deditor.models.ShapeModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.List;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class AppDrawer {
	private static final Color SHAPE_LINE_COLOR = new Color(0.2f, 0.2f, 0.8f, 1);
	private static final Color SHAPE_LASTLINE_COLOR = new Color(0.5f, 0.5f, 0.5f, 1);
	private static final Color SHAPE_POLY_COLOR = new Color(0.2f, 0.8f, 0.2f, 1);
	private static final Color MOUSEPATH_COLOR = new Color(0.2f, 0.2f, 0.2f, 1);
	private static final Color BALLTHROWPATH_COLOR = new Color(0.2f, 0.2f, 0.2f, 1);

	private final OrthographicCamera camera;
	private final ImmediateModeRenderer imr;
	private final Vector2 tp1 = new Vector2();
	private final Vector2 tp2 = new Vector2();

	public AppDrawer(OrthographicCamera camera) {
		this.camera = camera;
		this.imr = new ImmediateModeRenderer();
	}

	// -------------------------------------------------------------------------

	public void draw() {
		ShapeModel[] shapes = AppContext.instance().getTempShapes();
		Vector2[][] polys = AppContext.instance().getTempPolygons();

		if (AppContext.instance().arePolyDrawn) {
			drawPolys(polys);
		}

		if (AppContext.instance().isShapeDrawn) {
			drawShapes(shapes);
			drawPoints(shapes);
		}

		drawMousePath();
		drawBallThrowPath();
	}

	// -------------------------------------------------------------------------

	private void drawShapes(ShapeModel[] shapes) {
		for (ShapeModel shape : shapes) {
			Vector2[] points = shape.getPoints();
			if (points.length > 0) {
				for (int i=1; i<points.length; i++)
					drawLine(points[i], points[i-1], SHAPE_LINE_COLOR, 2);

				if (shape.isClosed()) {
					drawLine(points[0], points[points.length-1], SHAPE_LINE_COLOR, 2);
				} else {
					Vector2 nextPoint = AppContext.instance().nextPoint;
					if (nextPoint != null)
						drawLine(points[points.length-1], nextPoint, SHAPE_LASTLINE_COLOR, 2);
				}
			}
		}
	}

	private void drawPoints(ShapeModel[] shapes) {
		Vector2 np = AppContext.instance().nearestPoint;
		List<Vector2> sp = AppContext.instance().selectedPoints;
		float w = 10 * camera.zoom;

		for (ShapeModel shape : shapes) {
			for (Vector2 p : shape.getPoints()) {
				if (p == np || sp.contains(p))
					fillRect(p, w, w, SHAPE_LINE_COLOR);
				drawRect(p, w, w, SHAPE_LINE_COLOR, 2);
			}
		}
	}

	private void drawPolys(Vector2[][] polys) {
		for (Vector2[] poly : polys) {
			for (int i=1; i<poly.length; i++)
				drawLine(poly[i], poly[i-1], SHAPE_POLY_COLOR, 2);
			if (poly.length > 0)
				drawLine(poly[0], poly[poly.length-1], SHAPE_POLY_COLOR, 2);
		}
	}

	private void drawMousePath() {
		List<Vector2> mp = AppContext.instance().mousePath;
		for (int i=1; i<mp.size(); i++)
			drawLine(mp.get(i), mp.get(i-1), MOUSEPATH_COLOR, 1);
		if (mp.size() > 1)
			drawLine(mp.get(0), mp.get(mp.size()-1), MOUSEPATH_COLOR, 1);
	}

	private void drawBallThrowPath() {
		Vector2 v1 = AppContext.instance().ballThrowFirstPoint;
		Vector2 v2 = AppContext.instance().ballThrowLastPoint;
		float w = 10 * camera.zoom;

		if (v1 != null && v2 != null) {
			drawLine(v1, v2, BALLTHROWPATH_COLOR, 3);
			drawRect(v2, w, w, BALLTHROWPATH_COLOR, 3);
		}
	}

	// -------------------------------------------------------------------------

	public void drawLine(Vector2 p1, Vector2 p2, Color c, float lineWidth) {
		Gdx.gl10.glLineWidth(lineWidth);
		imr.begin(GL10.GL_LINES);
		imr.color(c.r, c.g, c.b, c.a); imr.vertex(p1.x, p1.y, 0);
		imr.color(c.r, c.g, c.b, c.a); imr.vertex(p2.x, p2.y, 0);
		imr.end();
	}

	public void drawRect(Vector2 p, float w, float h, Color c, float lineWidth) {
		Gdx.gl10.glLineWidth(lineWidth);
		imr.begin(GL10.GL_LINE_STRIP);
		imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x - w/2, p.y - h/2, 0);
		imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x - w/2, p.y + h/2, 0);
		imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x + w/2, p.y + h/2, 0);
		imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x + w/2, p.y - h/2, 0);
		imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x - w/2, p.y - h/2, 0);
		imr.end();
	}

	public void fillRect(Vector2 p, float w, float h, Color c) {
		imr.begin(GL10.GL_TRIANGLE_FAN);
		imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x - w/2, p.y - h/2, 0);
		imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x - w/2, p.y + h/2, 0);
		imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x + w/2, p.y + h/2, 0);
		imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x + w/2, p.y - h/2, 0);
		imr.end();
	}
}
