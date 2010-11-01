
package com.badlogic.gdx.hiero.unicodefont;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.LwjglApplication;
import com.badlogic.gdx.hiero.unicodefont.effects.ColorEffect;

public class UnicodeFontTest implements RenderListener {
	private UnicodeFont unicodeFont;

	public void created () {
		unicodeFont = new UnicodeFont("c:/windows/fonts/arial.ttf", 48, false, false);
		unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.white));
		// unicodeFont.addAsciiGlyphs();
		// unicodeFont.loadGlyphs();
	}

	public void resized (int width, int height) {
		glViewport(0, 0, width, height);
		glScissor(0, 0, width, height);
		glEnable(GL_SCISSOR_TEST);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, height, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		glEnable(GL_TEXTURE_2D);
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glEnableClientState(GL_VERTEX_ARRAY);

		glClearColor(0, 0, 0, 0);
		glClearDepth(1);

		glDisable(GL_LIGHTING);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	public void render () {
		GL11.glClear(GL_COLOR_BUFFER_BIT);

		unicodeFont.loadGlyphs(1);

		String text = "This is UnicodeFont!\nIt rockz. Kerning: T,";
		unicodeFont.setDisplayListCaching(false);
		unicodeFont.drawString(10, 33, text);
		unicodeFont.drawString(10, 330, text);

		unicodeFont.addGlyphs("~!@!#!#$%___--");
		// Cypriot Syllabary glyphs (Everson Mono font): \uD802\uDC02\uD802\uDC03\uD802\uDC12 == 0x10802, 0x10803, s0x10812
	}

	public void dispose () {
	}

	public static void main (String[] args) {
		LwjglApplication app = new LwjglApplication("UnicodeFont Test", 800, 600, false);
		app.getGraphics().setRenderListener(new UnicodeFontTest());
	}
}
