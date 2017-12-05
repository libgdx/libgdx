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

package com.badlogic.gdx.tests.extensions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class FreeTypeIncrementalTest extends GdxTest {
	SpriteBatch batch;
	ShapeRenderer shapes;
	BitmapFont font;
	FreeTypeFontGenerator generator;

	public void create () {
		batch = new SpriteBatch();
		shapes = new ShapeRenderer();
		shapes.setColor(Color.RED);

		FreeTypeFontGenerator.setMaxTextureSize(128);

		generator = new FreeTypeFontGenerator(Gdx.files.internal("data/arial.ttf"));

		FreeTypeFontParameter param = new FreeTypeFontParameter();
		param.incremental = true;
		param.size = 24;
		param.characters = "howdY\u0000";

		FreeTypeBitmapFontData data = new FreeTypeBitmapFontData() {
			public int getWrapIndex (Array<Glyph> glyphs, int start) {
				return SimplifiedChinese.getWrapIndex(glyphs, start);
			}
		};

		// By default latin chars are used for x and cap height, causing some fonts to display non-latin chars out of bounds.
		data.xChars = new char[] {'动'};
		data.capChars = new char[] {'动'};

		font = generator.generateFont(param, data);
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Draw rects.
		shapes.begin(ShapeType.Line);
		float x = 0, y = Gdx.graphics.getHeight() - font.getRegion().getRegionHeight() - 1;
		for (int i = 0, n = font.getRegions().size; i < n; i++) {
			TextureRegion region = font.getRegions().get(i);
			shapes.rect(x, y, region.getRegionWidth(), region.getRegionHeight());
			x += region.getRegionWidth() + 2;
		}
		shapes.rect(10, 250, Gdx.graphics.getWidth() - 20, -240);
		shapes.end();

		batch.begin();
		x = 0;
		for (int i = 0, n = font.getRegions().size; i < n; i++) {
			TextureRegion region = font.getRegions().get(i);
			batch.draw(region, x, y);
			x += region.getRegionWidth() + 2;
		}
		font.draw(batch, "LYA", 10, 300); // Shows kerning.
		font.draw(batch, "hello world", 100, 300);
		font.draw(batch,
			"动画能给游戏带来生机和灵气。我们相信创作一段美妙的动画，不仅需要强大的软件工具，更需要一套牛 B 的工作流程。" //
				+ "Spine专注于此，为您创建惊艳的骨骼动画，并将其整合到游戏当中，提供了一套高效的工作流程。",
			10, 250, //
			Gdx.graphics.getWidth() - 20, Align.left, true);
		batch.end();
	}

	public void resize (int width, int height) {
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		shapes.setProjectionMatrix(batch.getProjectionMatrix());
	}

	static public class SimplifiedChinese {
		public static int getWrapIndex (Array<Glyph> glyphs, int start) {
			int i = start - 1;
			for (; i >= 1; i--) {
				int startChar = glyphs.get(i).id;
				if (!SimplifiedChinese.legalAtStart(startChar)) continue;
				int endChar = glyphs.get(i - 1).id;
				if (!SimplifiedChinese.legalAtEnd(endChar)) continue;
				// Don't wrap between ASCII chars.
				if (startChar < 127 && endChar < 127 && !Character.isWhitespace(startChar)) continue;
				return i;
			}
			return start;
		}

		static private boolean legalAtStart (int ch) {
			switch (ch) {
			case '!':
			case '%':
			case ')':
			case ',':
			case '.':
			case ':':
			case ';':
			case '>':
			case '?':
			case ']':
			case '}':
			case '¢':
			case '¨':
			case '°':
			case '·':
			case 'ˇ':
			case 'ˉ':
			case '―':
			case '‖':
			case '’':
			case '”':
			case '„':
			case '‟':
			case '†':
			case '‡':
			case '›':
			case '℃':
			case '∶':
			case '、':
			case '。':
			case '〃':
			case '〆':
			case '〈':
			case '《':
			case '「':
			case '『':
			case '〕':
			case '〗':
			case '〞':
			case '﹘':
			case '﹚':
			case '﹜':
			case '！':
			case '＂':
			case '％':
			case '＇':
			case '）':
			case '，':
			case '．':
			case '：':
			case '；':
			case '？':
			case '］':
			case '｀':
			case '｜':
			case '｝':
			case '～':
				return false;
			}
			return true;
		}

		static private boolean legalAtEnd (int ch) {
			switch (ch) {
			case '$':
			case '(':
			case '*':
			case ',':
			case '£':
			case '¥':
			case '·':
			case '‘':
			case '“':
			case '〈':
			case '《':
			case '「':
			case '『':
			case '【':
			case '〔':
			case '〖':
			case '〝':
			case '﹗':
			case '﹙':
			case '﹛':
			case '＄':
			case '（':
			case '．':
			case '［':
			case '｛':
			case '￡':
			case '￥':
				return false;
			}
			return true;
		}
	}
}
