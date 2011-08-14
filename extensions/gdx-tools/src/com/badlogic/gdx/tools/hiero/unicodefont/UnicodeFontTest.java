/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tools.hiero.unicodefont;

import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.ColorEffect;

import static org.lwjgl.opengl.GL11.*;

public class UnicodeFontTest implements ApplicationListener {
	private UnicodeFont unicodeFont;

	@Override
	public void create () {
		unicodeFont = new UnicodeFont("c:/windows/fonts/arial.ttf", 48, false, false);
		unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.white));
	}

	@Override
	public void resize (int width, int height) {
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

	@Override
	public void render () {
		GL11.glClear(GL_COLOR_BUFFER_BIT);

		unicodeFont.loadGlyphs(1);

		String text = "This is UnicodeFont!\nIt rockz. Kerning: T,";
		unicodeFont.setDisplayListCaching(false);
		unicodeFont.drawString(10, 33, text);
		unicodeFont.drawString(10, 330, text);

		unicodeFont.addGlyphs("~!@!#!#$%___--");
		// Cypriot Syllabary glyphs (Everson Mono font):
		// \uD802\uDC02\uD802\uDC03\uD802\uDC12 == 0x10802, 0x10803, s0x10812
	}

	public static void main (String[] args) {
		new LwjglApplication(new UnicodeFontTest(), "UnicodeFont Test", 800, 600, false);
	}

	@Override
	public void dispose () {
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}
}
