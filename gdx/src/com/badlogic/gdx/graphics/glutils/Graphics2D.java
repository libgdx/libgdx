
package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/** Provides a 2D API similar to that of the JDK's built in Java2D ({@link java.awt.Graphics}).
 * 
 * Here is a typical usage example:
 * 
 * <pre>
 * {@code
 * Graphics2D g = Graphics2D.create(); // or createdInverted()
 * g.clear(); // clear the screen
 * g.drawImage(texture, 50, 50);
 * g.color(Color.RED).drawOval(150, 100, 300, 50);
 * g.color(Color.BLUE).fillOval(150, 340, 50, 200);
 * g.color(Color.CYAN).drawRect(180, 100, 300, 50);
 * g.color(Color.DARK_GRAY).fillRect(250, 320, 50, 200);
 * g.color(Color.ORANGE).drawString(&quot;Hello!&quot;, 340, 200);
 * g.color(Color.WHITE).lineWidth(5).drawLine(100, 100, 200, 120);
 * }
 * </pre>
 * 
 * If you want the y-axis to be oriented the same way as in Java2D, call Graphics2D.createInverted().
 * 
 * @author mirraj2 */
public class Graphics2D {

	private static final BitmapFont DEFAULT_FONT = new BitmapFont();

	private final ShapeRenderer renderer = new ShapeRenderer();
	private final SpriteBatch batch = new SpriteBatch(1);
	private BitmapFont font = DEFAULT_FONT;
	private boolean inverted = true;

	private Graphics2D (boolean inverted) {
		this.inverted = inverted;
	}

	/** Clears the screen and resets this Graphics context. */
	public Graphics2D clear () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		return this;
	}

	public Graphics2D color (float r, float g, float b, float a) {
		renderer.setColor(r, g, b, a);
		return this;
	}

	public Graphics2D color (Color c) {
		renderer.setColor(c);
		font.setColor(c);
		return this;
	}

	public Graphics2D font (BitmapFont font) {
		this.font = font;
		return this;
	}

	public Graphics2D lineWidth (float lineWidth) {
		Gdx.gl.glLineWidth(lineWidth);
		return this;
	}

	public Graphics2D drawLine (float x, float y, float x2, float y2) {
		renderer.begin(ShapeType.Line);
		renderer.line(x, invert(y), x2, invert(y2));
		renderer.end();
		return this;
	}

	public Graphics2D drawString (String s, float x, float y) {
		batch.begin();
		font.draw(batch, s, x, invert(y));
		batch.end();
		return this;
	}

	public Graphics2D drawImage (Texture image, float x, float y) {
		int w = image.getWidth(), h = image.getHeight();
		return drawImage(image, x, y, w, h, 0, 0, w, h);
	}

	public Graphics2D drawImage (Texture image, float x, float y, float w, float h, float srcX, float srcY, float srcW, float srcH) {
		batch.begin();
		batch.draw(image, x, invert(y) - h, w, h, srcX / image.getWidth(), (srcY + srcH) / image.getHeight(),
			(srcX + srcW) / image.getWidth(), srcY / image.getHeight());
		batch.end();
		return this;
	}

	public Graphics2D drawRect (float x, float y, float width, float height) {
		renderer.begin(ShapeType.Line);
		renderer.rect(x, invert(y), width, height);
		renderer.end();
		return this;
	}

	public Graphics2D drawOval (float x, float y, float width, float height) {
		renderer.begin(ShapeType.Line);
		renderer.ellipse(x, invert(y), width, height);
		renderer.end();
		return this;
	}

	public Graphics2D fillRect (float x, float y, float width, float height) {
		renderer.begin(ShapeType.Filled);
		renderer.rect(x, invert(y), width, height);
		renderer.end();
		return this;
	}

	public Graphics2D fillOval (float x, float y, float width, float height) {
		renderer.begin(ShapeType.Filled);
		renderer.ellipse(x, invert(y), width, height);
		renderer.end();
		return this;
	}

	/** Resets this graphics context. */
	public Graphics2D reset () {
		return font(DEFAULT_FONT).lineWidth(1);
	}

	private float invert (float y) {
		if (inverted) {
			return Gdx.graphics.getHeight() - y;
		} else {
			return y;
		}
	}

	public static Graphics2D create () {
		return new Graphics2D(false);
	}

	/** Inverts the y-axis so that (y = 0) is at the TOP of the screen. */
	public static Graphics2D createInverted () {
		return new Graphics2D(true);
	}

}
