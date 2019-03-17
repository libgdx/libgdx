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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter;
import com.badlogic.gdx.tools.hiero.Hiero;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class BitmapFontWriterTest extends GdxTest {

	SpriteBatch spriteBatch;

	BitmapFont generatedFont;
	BitmapFont loadedFont;
	BitmapFont font;
	ShapeRenderer renderer;

	@Override
	public void create () {
		BitmapFontWriter.FontInfo info = new BitmapFontWriter.FontInfo();
		info.padding = new BitmapFontWriter.Padding(0, 0, 0, 0);

		FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
		param.size = 13;
		param.shadowOffsetY = 3;
		param.renderCount = 3;
		param.shadowColor = new Color(0, 0, 0, 1);
		param.characters = Hiero.EXTENDED_CHARS;
		param.packer = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 2, false, new PixmapPacker.SkylineStrategy());

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/unbom.ttf"));

		generatedFont = generator.generateFont(param);

		info.overrideMetrics(generatedFont.getData());

		BitmapFontWriter.writeFont(generatedFont.getData(), new String[] {"bitmapWrittenFont.png"}, Gdx.files.local("bitmapWrittenFont.fnt"), info, 512, 512);
		BitmapFontWriter.writePixmaps(param.packer.getPages(), Gdx.files.local(""), "bitmapWrittenFont");

		final float ascent = generatedFont.getAscent();
		final float descent = generatedFont.getDescent();
		final float capHeight = generatedFont.getCapHeight();
		final float lineHeight = generatedFont.getLineHeight();
		final float spaceXadvance = generatedFont.getSpaceXadvance();
		final float xHeight = generatedFont.getXHeight();

		loadedFont = new BitmapFont(Gdx.files.local("bitmapWrittenFont.fnt"));

		final float loadedFontascent = loadedFont.getAscent();
		final float loadedFontdescent = loadedFont.getDescent();
		final float loadedFontcapHeight = loadedFont.getCapHeight();
		final float loadedFontlineHeight = loadedFont.getLineHeight();
		final float loadedFontspaceXadvance = loadedFont.getSpaceXadvance();
		final float loadedFontxHeight = loadedFont.getXHeight();

		System.out.println("Ascent: " + ascent + " : " + loadedFontascent);
		System.out.println("Descent: " + descent + " : " + loadedFontdescent);
		System.out.println("Cap Height: " + capHeight + " : " + loadedFontcapHeight);
		System.out.println("Line height: " + lineHeight + " : " + loadedFontlineHeight);
		System.out.println("Space X advance: " + spaceXadvance + " : " + loadedFontspaceXadvance);
		System.out.println("xHeight: " + xHeight + " : " + loadedFontxHeight);

		if (!MathUtils.isEqual(ascent, loadedFontascent)) throw new GdxRuntimeException("Ascent is not equal");
		if (!MathUtils.isEqual(descent, loadedFontdescent)) throw new GdxRuntimeException("Descent is not equal");
		if (!MathUtils.isEqual(capHeight, loadedFontcapHeight)) throw new GdxRuntimeException("Cap height is not equal");
		if (!MathUtils.isEqual(lineHeight, loadedFontlineHeight)) throw new GdxRuntimeException("Line Height is not equal");
		if (!MathUtils.isEqual(spaceXadvance, loadedFontspaceXadvance)) throw new GdxRuntimeException("spaceXAdvance is not equal");
		if (!MathUtils.isEqual(xHeight, loadedFontxHeight)) throw new GdxRuntimeException("xHeight is not equal");

		spriteBatch = new SpriteBatch();

		renderer = new ShapeRenderer();
		renderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());

		font = new BitmapFont();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1f, 0.5f, 0.5f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		renderer.setProjectionMatrix(renderer.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

		spriteBatch.setProjectionMatrix(spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		spriteBatch.begin();


		String text = "Sphinx of black quartz.";
		generatedFont.setColor(Color.RED);

		int viewHeight = Gdx.graphics.getHeight();

		font.setColor(Color.BLACK);
		font.draw(spriteBatch, "draw position", 20, viewHeight - 0);
		font.setColor(Color.BLUE);
		font.draw(spriteBatch, "bounds", 20, viewHeight - 20);
		font.setColor(Color.MAGENTA);
		font.draw(spriteBatch, "baseline", 20, viewHeight - 40);
		font.setColor(Color.GREEN);
		font.draw(spriteBatch, "x height", 20, viewHeight - 60);
		font.setColor(Color.CYAN);
		font.draw(spriteBatch, "ascent", 20, viewHeight - 80);
		font.setColor(Color.RED);
		font.draw(spriteBatch, "descent", 20, viewHeight - 100);
		font.setColor(Color.ORANGE);
		font.draw(spriteBatch, "line height", 20, viewHeight - 120);
		font.setColor(Color.LIGHT_GRAY);
		font.draw(spriteBatch, "cap height", 20, viewHeight - 140);

		generatedFont.setColor(Color.WHITE);

		spriteBatch.end();

		renderFontWithMetrics(generatedFont, text, 100, 300);
		renderFontWithMetrics(loadedFont, text, 100, 200);

	}

	private void renderFontWithMetrics (BitmapFont font, String text, float x, float y) {
		float alignmentWidth;

		spriteBatch.begin();
		GlyphLayout layout = font.draw(spriteBatch, text, x, y);
		spriteBatch.end();

		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.setColor(Color.BLACK);
		renderer.rect(x - 3, y - 3, 6, 6);
		renderer.end();

		float baseline = y - font.getCapHeight();
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.LIGHT_GRAY);
		renderer.line(0, y, 9999, y);
		renderer.setColor(Color.MAGENTA);
		renderer.line(0, baseline, 9999, baseline);
		renderer.setColor(Color.GREEN);
		renderer.line(0, baseline + font.getXHeight(), 9999, baseline + font.getXHeight());
		renderer.setColor(Color.CYAN);
		renderer.line(0, y + font.getAscent(), 9999, y + font.getAscent());
		renderer.setColor(Color.RED);
		renderer.line(0, baseline + font.getDescent(), 9999, baseline + font.getDescent());
		renderer.setColor(Color.ORANGE);
		renderer.line(0, y - font.getLineHeight(), 9999, y - font.getLineHeight());
		renderer.end();

		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.BLUE);
		renderer.rect(x, y, layout.width, -layout.height);
		renderer.end();
	}

	@Override
	public void dispose () {

	}
}
