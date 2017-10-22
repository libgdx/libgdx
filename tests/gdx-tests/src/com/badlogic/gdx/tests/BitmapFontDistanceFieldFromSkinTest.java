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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.DistanceFieldFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.tests.utils.GdxTest;

public class BitmapFontDistanceFieldFromSkinTest extends GdxTest {

	private static final String TEXT = "Ta";
	private static final float[] SCALES = {0.25f, 0.5f, 1, 2, 4};

	private static class CustomDistanceFieldShader extends ShaderProgram {
		public CustomDistanceFieldShader() {
			super(Gdx.files.internal("data/shaders/distancefield.vert"), Gdx.files.internal("data/shaders/distancefield.frag"));
			if (!isCompiled()) {
				throw new RuntimeException("Shader compilation failed:\n" + getLog());
			}
		}

		/** @param smoothing a value between 0 and 1 */
		public void setSmoothing (float smoothing) {
			float delta = 0.5f * MathUtils.clamp(smoothing, 0, 1);
			setUniformf("u_lower", 0.5f - delta);
			setUniformf("u_upper", 0.5f + delta);
		}
	}

	private OrthographicCamera camera;
	private SpriteBatch spriteBatch;

	private Skin skin;

	private BitmapFont descriptionFont;
	private BitmapFont regularFont;
	private DistanceFieldFont distanceFieldFont;
    private ShaderProgram distanceFieldShader;
    private CustomDistanceFieldShader customDistanceFieldShader;
	private GlyphLayout layout = new GlyphLayout();

	@Override
	public void create () {
		Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth() + 250, Gdx.graphics.getHeight());
		
		camera = new OrthographicCamera();
        // IMPORTANT!!!
        distanceFieldShader = DistanceFieldFont.createDistanceFieldShader();
        spriteBatch = new SpriteBatch();
        spriteBatch.setShader(distanceFieldShader);

        skin = new Skin(Gdx.files.internal("data/uiskin.json"));

        descriptionFont = skin.getFont("description-font");
        descriptionFont.setColor(skin.getColor("red"));

        regularFont = skin.getFont("regular-font");
        regularFont.setColor(Color.BLACK);

        distanceFieldFont = skin.getDistanceFieldFont("distance-field-font");
        distanceFieldFont.setColor(skin.getColor("black"));

        customDistanceFieldShader = new CustomDistanceFieldShader();
        ShaderProgram.pedantic = false; // Useful when debugging this test

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		spriteBatch.begin();

		int x = 10;
		x += drawFont(regularFont, "Regular font\nNearest filter", false, false, 0, x);
		x += drawFont(regularFont, "Regular font\nLinear filter", true, false, 0, x);
		x += drawFont(regularFont, "Regular font\nCustom shader", true, true, 1.0f, x);
		x += drawFont(distanceFieldFont, "Distance field\nCustom shader", true, true, 1 / 8f, x);
		x += drawFont(distanceFieldFont, "Distance field\nShowing distance field", false, false, 0, x);

		spriteBatch.end();
	}

	private int drawFont (BitmapFont font, String description, boolean linearFiltering, boolean useShader, float smoothing, int x) {
		int y = 10;
		float maxWidth = 0;

		spriteBatch.setShader(distanceFieldShader);
		descriptionFont.draw(spriteBatch, description, x, y);
		spriteBatch.flush();
		y += 10 + 2 * descriptionFont.getLineHeight();

		// set filters for each page
		TextureFilter minFilter = linearFiltering ? TextureFilter.MipMapLinearNearest : TextureFilter.Nearest;
		TextureFilter magFilter = linearFiltering ? TextureFilter.Linear : TextureFilter.Nearest;
		for (int i = 0; i < font.getRegions().size; i++) {
			font.getRegion(i).getTexture().setFilter(minFilter, magFilter);
		}

		// draw distance field font
		if (font instanceof DistanceFieldFont) {
            if (useShader)
                ((DistanceFieldFont) font).setDistanceFieldSmoothing(4);
            else
                ((DistanceFieldFont) font).setDistanceFieldSmoothing(0);

            for (float scale : SCALES) {
                font.getData().setScale(scale);
                layout.setText(font, TEXT);
                maxWidth = Math.max(maxWidth, layout.width);
                font.draw(spriteBatch, layout, x, y);
                y += font.getLineHeight();
                spriteBatch.flush();
            }
            return (int)Math.ceil(maxWidth);
        }

        // draw bitmap font
		if (useShader) {
			spriteBatch.setShader(customDistanceFieldShader);
		} else {
			spriteBatch.setShader(distanceFieldShader);
		}

		for (float scale : SCALES) {
			font.getData().setScale(scale);
			layout.setText(font, TEXT);
			maxWidth = Math.max(maxWidth, layout.width);
			if (useShader) {
				customDistanceFieldShader.setSmoothing(smoothing / scale);
			}
			font.draw(spriteBatch, layout, x, y);
			y += font.getLineHeight();
			spriteBatch.flush();
		}
		return (int)Math.ceil(maxWidth);
	}

	private float getBaselineShift (float shift) {
		return shift;
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		camera.setToOrtho(true, width, height);
		spriteBatch.setTransformMatrix(camera.view);
		spriteBatch.setProjectionMatrix(camera.projection);
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		descriptionFont.dispose();
		regularFont.dispose();
		distanceFieldFont.dispose();
        distanceFieldShader.dispose();
        customDistanceFieldShader.dispose();
	}
}
