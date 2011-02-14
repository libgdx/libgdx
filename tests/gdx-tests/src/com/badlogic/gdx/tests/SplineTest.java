
package com.badlogic.gdx.tests;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.tmp.OrthographicCamera;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class SplineTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	final int CONTROL_POINTS = 10;
	OrthographicCamera cam;
	ImmediateModeRenderer renderer;
	CatmullRomSpline spline;
	Vector3[] path;
	

	@Override public void create () {
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
		renderer = new ImmediateModeRenderer();
		spline = new CatmullRomSpline();
		float x = 0;
		float y = Gdx.graphics.getHeight() / 2;
		spline.add(new Vector3(x - 50, y, 0));
		for(int i = 0; i < CONTROL_POINTS; i++) {
			spline.add(new Vector3(x, y, 0));
			x += Gdx.graphics.getWidth() / (CONTROL_POINTS - 2);
		}
		spline.add(new Vector3(Gdx.graphics.getWidth() + 50, y, 0));
		path = new Vector3[(CONTROL_POINTS - 2) * 7 - 1];
		for(int i = 0; i < path.length; i++) 
			path[i] = new Vector3();
		spline.getPath(path, 5);		
	}	
	
	@Override public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);		
		cam.update();
		Gdx.gl10.glMatrixMode(GL10.GL_PROJECTION);
		Gdx.gl10.glLoadMatrixf(cam.projection.val, 0);
		Gdx.gl10.glMatrixMode(GL10.GL_MODELVIEW);
		Gdx.gl10.glLoadMatrixf(cam.view.val, 0);
		
		renderer.begin(GL10.GL_TRIANGLES);
		for(int i = 0; i < path.length - 1; i++) {
			Vector3 point1 = path[i];
			Vector3 point2 = path[i+1];
			renderer.color(1, 1, 1, 1);
			renderer.vertex(point1.x, point1.y, 0);
			renderer.color(1, 1, 1, 1);
			renderer.vertex(point1.x, 0, 0);
			renderer.color(1, 1, 1, 1);
			renderer.vertex(point2.x, point2.y, 0);
			
			renderer.color(1, 1, 1, 1);
			renderer.vertex(point2.x, point2.y, 0);
			renderer.color(1, 1, 1, 1);
			renderer.vertex(point1.x, 0, 0);
			renderer.color(1, 1, 1, 1);
			renderer.vertex(point2.x, 0, 0);
		}
		renderer.end();
		
		Gdx.gl10.glPointSize(4);
		renderer.begin(GL10.GL_POINTS);
		for(int i = 0; i < spline.getControlPoints().size(); i++) {
			Vector3 point = spline.getControlPoints().get(i);
			renderer.color(1, 0, 0, 1);
			renderer.vertex(point.x, point.y, 0);
		}
		renderer.end();
		Gdx.gl10.glPointSize(1);
		
		processInput();
	}
	
	Vector3 point = new Vector3();
	private void processInput() {
//		if(Gdx.input.isTouched()) {			
//			Vector3 nearest = null;
//			float nearestDist = Float.MAX_VALUE;
//			point.set(cam.getScreenToWorldX(Gdx.input.getX()), 
//				 cam.getScreenToWorldY(Gdx.input.getY()),
//				 0);			
//			
//			for(int i = 0; i < spline.getControlPoints().size(); i++) {
//				Vector3 controlPoint = spline.getControlPoints().get(i);
//				float dist = Math.abs(point.x - controlPoint.x);
//				if(dist < nearestDist) {
//					nearest = controlPoint;
//					nearestDist = dist; 
//				}								
//			}
//			
//			nearest.y += (point.y - nearest.y) * Gdx.graphics.getDeltaTime();		
//			spline.getPath(path, 5);
//		}
	}
}
