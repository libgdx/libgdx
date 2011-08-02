package com.badlogic.gdx.box2deditor.renderpanel.inputprocessors;

import com.badlogic.gdx.box2deditor.AppContext;
import com.badlogic.gdx.box2deditor.models.ShapeModel;
import com.badlogic.gdx.box2deditor.renderpanel.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class ShapeEditionInputProcessor extends InputAdapter {
	boolean isActive = false;
	private Vector2 draggedPoint;

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		boolean isValid = button == Buttons.LEFT
			&& (!Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) &&
			    !Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))
			&& (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) &&
			    !Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT));

		if (!isValid)
			return false;
		isActive = true;

		if (!AppContext.instance().isCurrentModelValid())
			return true;

		draggedPoint = AppContext.instance().nearestPoint;
		List<Vector2> selectedPoints = AppContext.instance().selectedPoints;

		if (draggedPoint == null) {
			selectedPoints.clear();
			Vector2 p = App.instance().screenToWorld(x, y);
			AppContext.instance().mousePath.add(p);
		} else if (!selectedPoints.contains(draggedPoint)) {
			selectedPoints.clear();
		}

		return true;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (!isActive)
			return false;
		isActive = false;

		if (!AppContext.instance().isCurrentModelValid())
			return true;

		if (draggedPoint != null) {
			draggedPoint = null;
			AppContext.instance().saveCurrentModel();
		}

		List<Vector2> mousePath = AppContext.instance().mousePath;
		if (mousePath.size() > 2) {
			Vector2[] polygonPoints = mousePath.toArray(new Vector2[mousePath.size()]);
			Vector2[] testedPoints = getAllShapePoints();
			Vector2[] result = getPointsInPolygon(polygonPoints, testedPoints);
			Collections.addAll(AppContext.instance().selectedPoints, result);
		}
		mousePath.clear();
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if (!isActive)
			return false;

		if (!AppContext.instance().isCurrentModelValid())
			return true;

		Vector2 p = App.instance().screenToWorld(x, y);

		if (draggedPoint != null) {
			AppContext.instance().clearTempPolygons();

			float dx = p.x - draggedPoint.x;
			float dy = p.y - draggedPoint.y;
			draggedPoint.add(dx, dy);

			for (int i=0; i<AppContext.instance().selectedPoints.size(); i++) {
				Vector2 sp = AppContext.instance().selectedPoints.get(i);
				if (sp != draggedPoint)
					sp.add(dx, dy);
			}
		} else {
			AppContext.instance().mousePath.add(p);
		}
		
		return true;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		if (!AppContext.instance().isCurrentModelValid())
			return false;

		Vector2 p = App.instance().screenToWorld(x, y);

		// Nearest point computation
		AppContext.instance().nearestPoint = null;
		for (Vector2 v : getAllShapePoints())
			if (v.dst(p) < 10 * App.instance().getCamera().zoom)
				AppContext.instance().nearestPoint = v;

		return false;
	}

	// -------------------------------------------------------------------------

	private Vector2[] getPointsInPolygon(Vector2[] polygonPoints, Vector2[] points) {
		List<Vector2> circledPoints = new ArrayList<Vector2>();
		Polygon polygon = new Polygon();

		for (Vector2 p : polygonPoints)
			polygon.addPoint((int)(p.x * 1000), (int)(p.y * 1000));

		for (Vector2 p : points)
			if (polygon.contains(p.x * 1000, p.y * 1000))
				circledPoints.add(p);

		return circledPoints.toArray(new Vector2[0]);
	}

	private Vector2[] getAllShapePoints() {
		List<Vector2> points = new ArrayList<Vector2>();
		for (ShapeModel shape : AppContext.instance().getTempShapes())
			Collections.addAll(points, shape.getPoints());
		return points.toArray(new Vector2[points.size()]);
	}
}
