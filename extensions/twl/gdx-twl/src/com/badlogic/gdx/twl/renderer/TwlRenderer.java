
package com.badlogic.gdx.twl.renderer;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.CacheContext;
import de.matthiasmann.twl.renderer.DynamicImage;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.FontParameter;
import de.matthiasmann.twl.renderer.LineRenderer;
import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.renderer.Renderer;
import de.matthiasmann.twl.renderer.Texture;
import de.matthiasmann.twl.theme.ThemeManager;

public class TwlRenderer implements Renderer {
	private int mouseX, mouseY;
	private GdxCacheContext cacheContext;
	private boolean hasScissor;
	private final TintStack tintStateRoot = new TintStack();
	private TintStack tintStack = tintStateRoot;
	private final Color tempColor = new Color(1, 1, 1, 1);
	final SpriteBatch spriteBatch = new SpriteBatch();

	public TwlRenderer () {
		spriteBatch.setProjectionMatrix(new Matrix4().setToOrtho(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1));
	}

	public GdxCacheContext createNewCacheContext () {
		return new GdxCacheContext(this);
	}

	public GdxCacheContext getActiveCacheContext () {
		if (cacheContext == null) setActiveCacheContext(createNewCacheContext());
		return cacheContext;
	}

	public void setActiveCacheContext (CacheContext cacheContext) throws IllegalStateException {
		if (cacheContext == null) throw new IllegalArgumentException("cacheContext cannot be null.");
		if (!cacheContext.isValid()) throw new IllegalStateException("cacheContext is invalid.");
		if (!(cacheContext instanceof GdxCacheContext))
			throw new IllegalArgumentException("cacheContext is not from this renderer.");
		if (((GdxCacheContext)cacheContext).renderer != this)
			throw new IllegalArgumentException("cacheContext is not from this renderer.");
		this.cacheContext = (GdxCacheContext)cacheContext;
	}

	public long getTimeMillis () {
		return System.currentTimeMillis();
	}

	public void startRenderering () {
		tintStack = tintStateRoot;
		spriteBatch.begin();
	}

	public void endRendering () {
		spriteBatch.end();
		if (hasScissor) {
			Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);
			hasScissor = false;
		}
	}

	public void setClipRect (Rect rect) {
		if (rect == null) {
			Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);
			hasScissor = false;
		} else {
			Gdx.gl.glScissor(rect.getX(), Gdx.graphics.getHeight() - rect.getBottom(), rect.getWidth(), rect.getHeight());
			if (!hasScissor) {
				Gdx.gl.glEnable(GL10.GL_SCISSOR_TEST);
				hasScissor = true;
			}
		}
	}

	public Font loadFont (URL baseUrl, Map<String, String> parameter, Collection<FontParameter> conditionalParameter)
		throws IOException {
		String fileName = parameter.get("filename");
		if (fileName == null) {
			throw new IllegalArgumentException("filename parameter required");
		}
		BitmapFont bitmapFont = getActiveCacheContext().loadBitmapFont(new URL(baseUrl, fileName));
		return new GdxFont(this, bitmapFont, parameter, conditionalParameter);
	}

	public Texture loadTexture (URL url, String formatStr, String filterStr) throws IOException {
		if (url == null) throw new IllegalArgumentException("url cannot be null.");
		return getActiveCacheContext().loadTexture(url);
	}

	public int getHeight () {
		return Gdx.graphics.getHeight();
	}

	public int getWidth () {
		return Gdx.graphics.getWidth();
	}

	public LineRenderer getLineRenderer () {
		return null;
	}

	public DynamicImage createDynamicImage (int width, int height) {
		return null;
	}

	public void setCursor (MouseCursor cursor) {
	}

	public void setMousePosition (int mouseX, int mouseY) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public void pushGlobalTintColor (float r, float g, float b, float a) {
		tintStack = tintStack.push(r, g, b, a);
	}

	public void popGlobalTintColor () {
		tintStack = tintStack.previous;
	}

	public Color getColor (de.matthiasmann.twl.Color color) {
		Color tempColor = this.tempColor;
		TintStack tintStack = this.tintStack;
		tempColor.r = tintStack.r * (color.getR() & 255);
		tempColor.g = tintStack.g * (color.getG() & 255);
		tempColor.b = tintStack.b * (color.getB() & 255);
		tempColor.a = tintStack.a * (color.getA() & 255);
		return tempColor;
	}

	static private class TintStack extends Color {
		final TintStack previous;

		public TintStack () {
			super(0, 0, 0, 0);
			this.previous = this;
		}

		private TintStack (TintStack prev) {
			super(prev.r, prev.g, prev.b, prev.a);
			this.previous = prev;
		}

		public TintStack push (float r, float g, float b, float a) {
			TintStack next = new TintStack(this);
			next.r = this.r * r;
			next.g = this.g * g;
			next.b = this.b * b;
			next.a = this.a * a;
			return next;
		}
	}

	static public GUI createGUI (Widget root, FileHandle themeFile) {
		TwlRenderer renderer = new TwlRenderer();
		GUI gui = new GUI(root, renderer, null);
		try {
			URL themeURL = new URL("file", "", themeFile.toString());
			gui.applyTheme(ThemeManager.createThemeManager(themeURL, renderer));
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error loading theme: " + themeFile, ex);
		}
		Gdx.input.addInputListener(new TwlInputListener(gui));
		return gui;
	}
}
