
package com.badlogic.gdx.awesomium;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class GLTest implements ApplicationListener {
	PerspectiveCamera cam;
	Texture texture;
	Mesh mesh;
	WebCore webCore;
	WebView webView;
	Vector3 yAxis = new Vector3(0, 1, 0);
	float[] triangles = { -1, -1, 0, 1, -1, 0, 1, 1, 0, 1, 1, 0, -1, 1, 0, -1, -1, 0 };
	Plane plane = new Plane(new Vector3(0,0,1), new Vector3());
	Matrix4 matrix = new Matrix4();
	Matrix4 inv = new Matrix4();
	float angle = 0;
	float angleInc = 1;

	@Override public void create () {
		cam = new PerspectiveCamera();
		cam.setFov(67);
		cam.setNear(0.1f);
		cam.setFar(100);
		cam.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.getPosition().z = 2;

		mesh = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 2, "a_pos"), new VertexAttribute(Usage.TextureCoordinates,
			2, "a_texCoords"));

		mesh.setVertices(new float[] {-1, -1, 0, 1, 1, -1, 1, 1, 1, 1, 1, 0, -1, 1, 0, 0});

		mesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});
		texture = new Texture(512, 512, Format.RGBA8888);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);				

		webCore = new WebCore(".");
		webView = webCore.createWebView(512, 512);
		webView.focus();
		webView.loadURL("http://www.google.at", "", "", "");

		Gdx.input.setInputProcessor(new InputAdapter() {
			Vector3 point = new Vector3();
			Vector3 tmp = new Vector3();

			public void calculatePoint (int x, int y) {
				Ray ray = cam.getPickRay(x, y);
				ray.mul(inv);
				if(!Intersector.intersectRayTriangles(ray, triangles, tmp))
					return;
				point.set(tmp);
				point.x = (point.x + 1) / 2 * 512;
				point.y = 512 - (point.y + 1) / 2 * 512;
				if(point.x < 0) point.x = 0;
				if(point.x > 511) point.x = 511;
				if(point.y < 0) point.y = 0;
				if(point.y > 511) point.y = 511;				
			}

			@Override public boolean touchDown (int x, int y, int pointer, int newParam) {
				calculatePoint(x, y);
				webView.injectMouseMove((int)point.x, (int)point.y);
				webView.injectMouseDown(MouseButton.Left);
				return false;
			}

			@Override public boolean touchUp (int x, int y, int pointer, int button) {
				calculatePoint(x, y);
				webView.injectMouseMove((int)point.x, (int)point.y);
				webView.injectMouseUp(MouseButton.Left);
				return false;
			}

			@Override public boolean touchDragged (int x, int y, int pointer) {
				calculatePoint(x, y);
				webView.injectMouseMove((int)point.x, (int)point.y);
				return false;
			}

			@Override public boolean keyTyped (char key) {
				webView.injectKeyTyped(key);
				return false;
			}

			public int translateKeycode(int keycode) {
				int virtualKeycode = 0;
				if(keycode == Keys.KEYCODE_DPAD_LEFT)
					virtualKeycode = Awesomium.AWE_AK_LEFT;
				if(keycode == Keys.KEYCODE_DPAD_RIGHT)
					virtualKeycode = Awesomium.AWE_AK_RIGHT;
				if(keycode == Keys.KEYCODE_DPAD_UP)
					virtualKeycode = Awesomium.AWE_AK_UP;
				if(keycode == Keys.KEYCODE_DPAD_DOWN)
					virtualKeycode = Awesomium.AWE_AK_DOWN;
				if(keycode == Keys.KEYCODE_ENTER)
					virtualKeycode = Awesomium.AWE_AK_RETURN;
				if(keycode == Keys.KEYCODE_DEL)					
					virtualKeycode = Awesomium.AWE_AK_BACK;
				if(keycode == Keys.KEYCODE_SHIFT_LEFT || keycode == Keys.KEYCODE_SHIFT_RIGHT)
					virtualKeycode = Awesomium.AWE_AK_SHIFT;				
				return virtualKeycode;
			}
			
			private int getModifiers() {
				int modifiers = 0;
				if(Gdx.input.isKeyPressed(Keys.KEYCODE_SHIFT_LEFT) ||
					Gdx.input.isKeyPressed(Keys.KEYCODE_SHIFT_RIGHT))
					modifiers |= Awesomium.AWE_MOD_SHIFT_KEY;
				return modifiers;
			}
			
			@Override
			public boolean keyDown(int keycode) {
				int vkey = translateKeycode(keycode);
				int modifiers = getModifiers();
				if(vkey != 0) {					
					webView.injectKeyDown(vkey, modifiers, false);
				}
				return false;
			}
			
			@Override
			public boolean keyUp(int keycode) {
				int vkey = translateKeycode(keycode);
				int modifiers = getModifiers();
				if(vkey != 0)
					webView.injectKeyUp(vkey, modifiers, false);
				return false;
			}
		});
	}

	@Override public void resume () {

	}

	private void update () {
		webCore.update();
		if (webView.isDirty()) {
			RenderBuffer renderBuffer = webView.render();
			if (renderBuffer != null) {
				texture.bind();
				Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, 512, 512, 0, 0x80E1, GL10.GL_UNSIGNED_BYTE,
					renderBuffer.getBuffer());
			}
		}

		angle += Gdx.graphics.getDeltaTime() * 20 * angleInc;
		if(angle > 20) {
			angle = 20;
			angleInc = -1;
		}
		if(angle < -20) {
			angle = -20;
			angleInc = 1;
		}
		matrix.setToRotation(yAxis, angle);
		inv.set(matrix).inv();
	}

	@Override public void render () {
		update();

		GLCommon gl = Gdx.gl;
		gl.glClearColor(0.6f, 0.6f, 0.6f, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		texture.bind();
		cam.setMatrices();
		Gdx.gl10.glLoadMatrixf(matrix.getValues(), 0);
		mesh.render(GL10.GL_TRIANGLES);
	}

	@Override public void resize (int width, int height) {

	}

	@Override public void pause () {

	}

	@Override public void dispose () {
		webView.destroy();
		webCore.dispose();
	}

	public static void main (String[] argv) {
		new JoglApplication(new GLTest(), "Awesomium GL Test", 480, 320, false);
	}
}
