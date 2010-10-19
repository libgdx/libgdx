
package com.badlogic.gdx.twl.renderer;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL10;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.renderer.CacheContext;
import de.matthiasmann.twl.renderer.DynamicImage;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.FontParameter;
import de.matthiasmann.twl.renderer.LineRenderer;
import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.renderer.Renderer;
import de.matthiasmann.twl.renderer.Texture;

public class GdxRenderer implements Renderer {
	private final int[] temp = new int[1];

	private final TintStack tintStateRoot;
	private int mouseX;
	private int mouseY;
	private GdxCacheContext cacheContext;
	private boolean hasScissor;
	TintStack tintStack;

	public GdxRenderer () {
		this.tintStateRoot = new TintStack();
		this.tintStack = tintStateRoot;
	}

	public CacheContext createNewCacheContext () {
		return new GdxCacheContext(this);
	}

	private GdxCacheContext activeCacheContext () {
		if (cacheContext == null) {
			setActiveCacheContext(createNewCacheContext());
		}
		return cacheContext;
	}

	public CacheContext getActiveCacheContext () {
		return activeCacheContext();
	}

	public void setActiveCacheContext (CacheContext cc) throws IllegalStateException {
		if (cc == null) {
			throw new NullPointerException();
		}
		if (!cc.isValid()) {
			throw new IllegalStateException("CacheContext is invalid");
		}
		if (!(cc instanceof GdxCacheContext)) {
			throw new IllegalArgumentException("CacheContext object not from this renderer");
		}
		GdxCacheContext skorpiosCC = (GdxCacheContext)cc;
		if (skorpiosCC.renderer != this) {
			throw new IllegalArgumentException("CacheContext object not from this renderer");
		}
		this.cacheContext = skorpiosCC;
	}

	public long getTimeMillis () {
		return System.currentTimeMillis();
	}

	/**
	 * Setup GL to start rendering the GUI. It assumes default GL state.
	 */
	public void startRenderering () {
		hasScissor = false;
		tintStack = tintStateRoot;

		Graphics graphics = Gdx.graphics;
		GL10 gl = graphics.getGL10();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0, graphics.getWidth(), graphics.getHeight(), 0, -1.0f, 1.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	}

	public void endRendering () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}

	public int getHeight () {
		return Gdx.graphics.getHeight();
	}

	public int getWidth () {
		return Gdx.graphics.getWidth();
	}

	public Font loadFont (URL baseUrl, Map<String, String> parameter, Collection<FontParameter> conditionalParameter)
		throws IOException {
		String fileName = parameter.get("filename");
		if (fileName == null) {
			throw new IllegalArgumentException("filename parameter required");
		}
		URL url = new URL(baseUrl, fileName);
		BitmapFont bmFont = activeCacheContext().loadBitmapFont(url);
		return new GdxFont(this, bmFont, parameter, conditionalParameter);
	}

	public Texture loadTexture (URL url, String formatStr, String filterStr) throws IOException {
		return load(url);
	}

	public LineRenderer getLineRenderer () {
		return null;
	}

	public DynamicImage createDynamicImage (int width, int height) {
		return null;
	}

	public void setClipRect (Rect rect) {
		if (rect == null) {
			Gdx.graphics.getGL10().glScissor(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			hasScissor = false;
		} else {
			GL10 gl = Gdx.graphics.getGL10();
			gl.glScissor(rect.getX(), Gdx.graphics.getHeight() - rect.getBottom(), rect.getWidth(), rect.getHeight());
			if (!hasScissor) {
				gl.glEnable(GL10.GL_SCISSOR_TEST);
				hasScissor = true;
			}
		}
	}

	public void setCursor (MouseCursor cursor) {
	}

	public void setMousePosition (int mouseX, int mouseY) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public GdxTexture load (URL textureUrl) throws IOException {
		if (textureUrl == null) {
			throw new NullPointerException("textureUrl");
		}
		GdxCacheContext cc = activeCacheContext();
		return cc.loadTexture(textureUrl);
	}

	public void pushGlobalTintColor (float r, float g, float b, float a) {
		tintStack = tintStack.push(r, g, b, a);
	}

	public void popGlobalTintColor () {
		tintStack = tintStack.pop();
	}

	protected void getTintedColor (Color color, float[] result) {
		result[0] = tintStack.getR() * (color.getR() & 255);
		result[1] = tintStack.getG() * (color.getG() & 255);
		result[2] = tintStack.getB() * (color.getB() & 255);
		result[3] = tintStack.getA() * (color.getA() & 255);
	}

	int glGenTexture () {
		Gdx.graphics.getGL10().glGenTextures(1, temp, 0);
		return temp[0];
	}

	void glDeleteTexture (int id) {
		temp[0] = id;
		Gdx.graphics.getGL10().glDeleteTextures(1, temp, 0);
	}
}
