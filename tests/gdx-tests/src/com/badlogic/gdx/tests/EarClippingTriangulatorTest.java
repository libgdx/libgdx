package com.badlogic.gdx.tests;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Disposable;

public class EarClippingTriangulatorTest extends GdxTest {
	
	private List<TestCase> testCases = new ArrayList<TestCase>();
	private int casesX;
	private int casesY;

	@Override
	public void create() {
		// An empty "polygon"
		testCases.add(new TestCase(new float[] {}));
		
		// A point
		testCases.add(new TestCase(new float[] {0, 0}));
		
		// A line segment
		testCases.add(new TestCase(new float[] {0, 0, 1, 1}));
		
		// A counterclockwise triangle
		testCases.add(new TestCase(new float[] {
			0, 0,
			0, 1,
			1, 0,
		}));

		// A counterclockwise square
		testCases.add(new TestCase(new float[] {
			0, 0,
			0, 1,
			1, 1,
			1, 0,
		}));

		// A clockwise square
		testCases.add(new TestCase(new float[] {
			0, 0,
			1, 0,
			1, 1,
			0, 1,
		}));

		// A Starfleet insigna
		testCases.add(new TestCase(new float[] {
			0, 0,
			0.6f, 0.4f,
			1, 0,
			0.5f, 1,
		}));

		// A Starfleet insigna, different vertex order
		testCases.add(new TestCase(new float[] {
			1, 0,
			0.5f, 1,
			0, 0,
			0.6f, 0.4f,
		}));
		
		// Three collinear points
		testCases.add(new TestCase(new float[] {
			0, 0,
			1, 0,
			2, 0,
		}));
		
		// Four collinear points
		testCases.add(new TestCase(new float[] {
			0, 0,
			1, 0,
			2, 0,
			3, 0,
		}));
		
		// Non-consecutive collinear points
		testCases.add(new TestCase(new float[] {
			0, 0,
			1, 1,
			2, 0,
			3, 1,
			4, 0,
		}));
		
		// Plus shape
		testCases.add(new TestCase(new float[] {
			1, 0,
			2, 0,
			2, 1,
			3, 1,
			3, 2,
			2, 2,
			2, 3,
			1, 3,
			1, 2,
			0, 2,
			0, 1,
			1, 1,
		}));
		
		// Star shape
		testCases.add(new TestCase(new float[] {
			4, 0,
			5, 3,
			8, 4,
			5, 5,
			4, 8,
			3, 5,
			0, 4,
			3, 3,
		}));
		
		// U shape
		testCases.add(new TestCase(new float[] {
			1, 0,
			2, 0,
			3, 1,
			3, 3,
			2, 3,
			2, 1,
			1, 1,
			1, 3,
			0, 3,
			0, 1,
		}));
		
		// Spiral
		testCases.add(new TestCase(new float[] {
			1, 0,
			4, 0,
			5, 1,
			5, 4,
			4, 5,
			1, 5,
			0, 4,
			0, 3,
			1, 2,
			2, 2,
			3, 3,
			1, 3,
			1, 4,
			4, 4,
			4, 1,
			0, 1,
		}));
		
		// Issue 815, http://code.google.com/p/libgdx/issues/detail?id=815
		testCases.add(new TestCase(new float[] {
			-2.0f, 0.0f,
			-2.0f, 0.5f,
			0.0f, 1.0f,
			0.5f, 2.875f,
			1.0f, 0.5f,
			1.5f, 1.0f,
			2.0f, 1.0f,
			2.0f, 0.0f,
		}));
		
		// Issue 207, comment #1, http://code.google.com/p/libgdx/issues/detail?id=207#c1
		testCases.add(new TestCase(new float[] {
			72.42465f, 197.07095f,
			78.485535f, 189.92776f,
			86.12059f, 180.92929f,
			99.68253f, 164.94557f,
			105.24325f, 165.79604f,
			107.21862f, 166.09814f,
			112.41958f, 162.78253f,
			113.73238f, 161.94562f,
			123.29477f, 167.93805f,
			126.70667f, 170.07617f,
			73.22717f, 199.51062f,
		}));

		// Issue 207, comment #11, http://code.google.com/p/libgdx/issues/detail?id=207#c11
		// Also on issue 1081, http://code.google.com/p/libgdx/issues/detail?id=1081
		/*
		testCases.add(new TestCase(new float[] {
			2400.0f, 480.0f,
			2400.0f, 176.0f,
			1920.0f, 480.0f,
			1920.0459f, 484.22314f,
			1920.1797f, 487.91016f,
			1920.3955f, 491.0874f,
			1920.6875f, 493.78125f,
			1921.0498f, 496.01807f,
			1921.4766f, 497.82422f,
			1921.9619f, 499.22607f,
			1922.5f, 500.25f,
			1923.085f, 500.92236f,
			1923.7109f, 501.26953f,
			1924.3721f, 501.31787f,
			1925.0625f, 501.09375f,
			1925.7764f, 500.62354f,
			1926.5078f, 499.9336f,
			1927.251f, 499.0503f,
			1928.0f, 498.0f,
			1928.749f, 496.80908f,
			1929.4922f, 495.5039f,
			1930.2236f, 494.11084f,
			1930.9375f, 492.65625f,
			1931.6279f, 491.1665f,
			1932.2891f, 489.66797f,
			1932.915f, 488.187f,
			1933.5f, 486.75f,
			1934.0381f, 485.3833f,
			1934.5234f, 484.11328f,
			1934.9502f, 482.9663f,
			1935.3125f, 481.96875f,
			1935.6045f, 481.14697f,
			1935.8203f, 480.52734f,
			1935.9541f, 480.13623f,
			1936.0f, 480.0f,
		}));
		//*/
		
		// Issue 1407, http://code.google.com/p/libgdx/issues/detail?id=1407
		// The last point is not in the issue description, but it ensures we start with
		// a non-self-intersecting polygon and still reproduces the issue.
		/*
		testCases.add(new TestCase(new float[] {
			3.914329f, 1.9008259f,
			4.414321f, 1.903619f,
			4.8973203f, 1.9063174f,
			5.4979978f, 1.9096732f,
			4, 4,
		}));
		//*/
		
		casesX = (int) Math.ceil(Math.sqrt(testCases.size()));
		casesY = (int) Math.ceil((float) testCases.size() / casesX);
	}
	
	@Override
	public void render() {
		Gdx.gl10.glClearColor(1, 1, 1, 1);
      Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
      
      int w = Gdx.graphics.getWidth();
      int h = Gdx.graphics.getHeight();
      Gdx.gl10.glViewport(0, 0, w, h);
      
      final float M = 0.1f;
      Gdx.gl10.glMatrixMode(GL10.GL_PROJECTION);
      Gdx.gl10.glLoadIdentity();
      Gdx.gl10.glOrthof(-M, casesX * (1 + M), -M, casesY * (1 + M), -1, 1);
      Gdx.gl10.glMatrixMode(GL10.GL_MODELVIEW);
      Gdx.gl10.glLoadIdentity();
      
      int x = 0;
      int y = 0;
		for (TestCase testCase : testCases) {
			Gdx.gl10.glPushMatrix();
			Gdx.gl10.glTranslatef(x * (1 + M), y * (1 + M), 0);
			testCase.render();
			Gdx.gl10.glPopMatrix();
			
			x++;
			if (x >= casesX) {
				x = 0;
				y++;
			}
		}
	}
	
	@Override
	public void dispose() {
		for (TestCase testCase : testCases) {
			testCase.dispose();
		}
	}
	
	private class TestCase implements Disposable {
		final Mesh polygonMesh;
		final Mesh interiorMesh;
		final Mesh triangleOutlineMesh;
		final Rectangle boundingRect;
		
		public TestCase(float[] p) {
			List<Vector2> polygon = vertexArrayToList(p);
			int numPolygonVertices = polygon.size();
			Vector2 min = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
			Vector2 max = new Vector2(-Float.MAX_VALUE, -Float.MAX_VALUE);
			for (int i = 0; i < numPolygonVertices; i++) {
				Vector2 v = polygon.get(i);
				min.x = Math.min(min.x, v.x);
				min.y = Math.min(min.y, v.y);
				max.x = Math.max(max.x, v.x);
				max.y = Math.max(max.y, v.y);
			}
			boundingRect = new Rectangle(min.x, min.y, Math.max(0.001f, max.x - min.x), Math.max(0.001f, max.y - min.y));
			
			List<Vector2> triangles = new EarClippingTriangulator().computeTriangles(polygon);

			int numTriangleVertices = triangles.size();
			ArrayList<Vector2> triangleOutlines = new ArrayList<Vector2>(2 * numTriangleVertices);
			for (int i = 0, j = 0; i < numTriangleVertices;) {
				Vector2 a = triangles.get(i++);
				Vector2 b = triangles.get(i++);
				Vector2 c = triangles.get(i++);
				triangleOutlines.add(a);
				triangleOutlines.add(b);
				triangleOutlines.add(b);
				triangleOutlines.add(c);
				triangleOutlines.add(c);
				triangleOutlines.add(a);
			}
			
			VertexAttributes attributes = new VertexAttributes(new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE));
			polygonMesh = new Mesh(true, polygon.size(), 0, attributes);
			polygonMesh.setVertices(listToVertexArray(polygon));
			interiorMesh = new Mesh(true, triangles.size(), 0, attributes);
			interiorMesh.setVertices(listToVertexArray(triangles));
			triangleOutlineMesh = new Mesh(true, triangleOutlines.size(), 0, attributes);
			triangleOutlineMesh.setVertices(listToVertexArray(triangleOutlines));
		}
		
		public void render() {
			Gdx.gl10.glScalef(1 / boundingRect.width, 1 / boundingRect.height, 1);
			Gdx.gl10.glTranslatef(-boundingRect.x, -boundingRect.y, 0);
			
			Gdx.gl10.glColor4f(0.8f, 0.8f, 0.9f, 1.0f);
			interiorMesh.render(GL10.GL_TRIANGLES);
			
			Gdx.gl10.glColor4f(0.4f, 0.4f, 0.4f, 1.0f);
			Gdx.gl10.glLineWidth(1.0f);
			triangleOutlineMesh.render(GL10.GL_LINES);
			
			Gdx.gl10.glColor4f(0.3f, 0.0f, 0.0f, 1.0f);
			Gdx.gl10.glLineWidth(2.0f);
			polygonMesh.render(GL10.GL_LINE_LOOP);
		}
		
		@Override
		public void dispose() {
			polygonMesh.dispose();
			interiorMesh.dispose();
			triangleOutlineMesh.dispose();
		}
	}
	
	static List<Vector2> vertexArrayToList(float[] array) {
		int n = array.length;
		List<Vector2> list = new ArrayList<Vector2>(n / 2);
		for (int i = 0; i < n; i += 2) {
			list.add(new Vector2(array[i], array[i+1]));
		}
		return list;
	}
	
	static float[] listToVertexArray(List<Vector2> list) {
		int n = list.size();
		float[] array = new float[n * 2];
		int i = 0;
		for (Vector2 v : list) {
			array[i++] = v.x;
			array[i++] = v.y;
		}
		return array;
	}
}
