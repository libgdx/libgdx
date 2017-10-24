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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.DistanceFieldFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class BitmapFontDistanceFieldFromSkinWithStageTest extends GdxTest {

	private static final String TEXT = "Ta";
	private static final float[] SCALES = {0.25f, 0.5f, 1, 2, 4};

	private Skin skin;

	private Stage stage;
	private Table rootTable;
	private LabelStyle descriptionLabelStyle;
	private LabelStyle regularLabelStyle;
	private LabelStyle distanceLabelStyle;

	private ShaderProgram distanceFieldShader;
	private CustomDistanceFieldShader customDistanceFieldShader;

	@Override
	public void create () {
		Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth() + 250, Gdx.graphics.getHeight());

		// IMPORTANT!!!
		distanceFieldShader = DistanceFieldFont.createDistanceFieldShader();

		customDistanceFieldShader = new CustomDistanceFieldShader();
		ShaderProgram.pedantic = false; // Useful when debugging this test

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		stage = new Stage(new ScreenViewport());
		stage.getBatch().setShader(distanceFieldShader);

		rootTable = new Table(skin);
		rootTable.setFillParent(true);
		rootTable.padLeft(10f).padTop(10f).top().left();
		stage.addActor(rootTable);

		descriptionLabelStyle = new LabelStyle();
		descriptionLabelStyle.font = skin.getFont("description-font");
		descriptionLabelStyle.fontColor = Color.RED;

		regularLabelStyle = new LabelStyle();
		regularLabelStyle.font = skin.getFont("regular-font");
		regularLabelStyle.fontColor = Color.BLACK;

		distanceLabelStyle = new LabelStyle();
		distanceLabelStyle.font = skin.getDistanceFieldFont("distance-field-font");
		distanceLabelStyle.fontColor = skin.getColor("black");

		drawFont(regularLabelStyle, "Regular font\nNearest filter", false, false, 0);
		drawFont(regularLabelStyle, "Regular font\nLinear filter", true, false, 0);
		drawFont(regularLabelStyle, "Regular font\nCustom shader", true, true, 1.0f);
		drawFont(distanceLabelStyle, "Distance field\nCustom shader", true, true, 4f);
		drawFont(distanceLabelStyle, "Distance field\nShowing distance field", false, false, 0);
	}

	private void drawFont(LabelStyle style, String description, boolean linearFiltering, boolean useCustomShader, float smoothing) {
		Table column = new Table(skin);
		rootTable.add(column).top();

		Label descriptionLabel = new Label(description, descriptionLabelStyle);

		column.add(descriptionLabel).left().row();
		column.add().height(10f);

		for (float scale : SCALES) {
			LabelCustomShader label = new LabelCustomShader(TEXT, style, linearFiltering, useCustomShader, smoothing);
			label.setFontScale(scale);
			column.row();
			column.add(label).height(style.font.getLineHeight() * scale).left();
		}

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		stage.dispose();
		distanceFieldShader.dispose();
		customDistanceFieldShader.dispose();
	}

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

	private class LabelCustomShader extends Label {
		private boolean linearFiltering;
		private boolean useCustomShader;
		private float smoothing;

		public LabelCustomShader(CharSequence text, LabelStyle style, boolean linearFiltering, boolean useCustomShader, float smoothing) {
			super(text, style);
			this.linearFiltering = linearFiltering;
			this.useCustomShader = useCustomShader;
			this.smoothing = smoothing;
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			// set filters for each page
			TextureFilter minFilter = linearFiltering ? TextureFilter.MipMapLinearNearest : TextureFilter.Nearest;
			TextureFilter magFilter = linearFiltering ? TextureFilter.Linear : TextureFilter.Nearest;
			for (int i = 0; i < getStyle().font.getRegions().size; i++) {
				getStyle().font.getRegion(i).getTexture().setFilter(minFilter, magFilter);
			}

			// draw distance field font
			if (getStyle().font instanceof DistanceFieldFont) {
				batch.setShader(distanceFieldShader);
				if (useCustomShader)
					// causes DistanceFieldFontCache.getSmoothingFactor() used font.scale, instead we use Label.setFontScale()
					((DistanceFieldFont) getStyle().font).setDistanceFieldSmoothing(smoothing * getFontScaleX());
				else
					((DistanceFieldFont) getStyle().font).setDistanceFieldSmoothing(0f * getFontScaleX());

			} else { // draw bitmap font
				if (useCustomShader) {
					batch.setShader(customDistanceFieldShader);
					customDistanceFieldShader.setSmoothing(smoothing / getFontScaleX());
				}
			}

			super.draw(batch, parentAlpha);
			batch.setShader(null);
		}
	}
}
