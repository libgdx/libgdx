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
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GlyphLayoutWrapTest extends GdxTest {
    private Stage stage;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private ShapeRenderer renderer;
    private Label label;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        // font = new BitmapFont(Gdx.files.internal("data/verdana39.fnt"), false);
        font = new BitmapFont(Gdx.files.internal("data/arial-32-pad.fnt"), false);
        // font = new FreeTypeFontGenerator(Gdx.files.internal("data/arial.ttf")).generateFont(new FreeTypeFontParameter());
        font.getData().markupEnabled = true;
        font.getData().breakChars = new char[]{'-'};

        // Add user defined color
        renderer = new ShapeRenderer();
        renderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());

        stage = new Stage(new ScreenViewport());

        Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

        BitmapFont labelFont = skin.get("default-font", BitmapFont.class);
        labelFont.getData().markupEnabled = true;

        // Notice that the last [] has been deliberately added to test the effect of excessive pop operations.
        // They are silently ignored, as expected.
        label = new Label("Resize window to adjust targetWidth of glyphLayouts\n" +
                "Click to change scale of font", skin);
        stage.addActor(label);
    }

    @Override
    public void render() {
        if (Gdx.input.justTouched()) {
            final float newScale = font.getData().scaleX == 1 ? 2 : 1;
            font.getData().scaleX = newScale;
            font.getData().scaleY = newScale;
        }

        int viewHeight = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        final int screenWidth = stage.getViewport().getScreenWidth();

        label.setPosition(screenWidth / 2 - label.getWidth() / 2, 8);

        // Test various font features.
        spriteBatch.begin();

        String text = "Sphinx of black quartz, judge my vow.";
        font.setColor(Color.RED);

        float spacing = 20;
        float y = spacing;
        float targetWidth = screenWidth / 2 - 1.5f * spacing;

        font.draw(spriteBatch, text, spacing, viewHeight - y, targetWidth, Align.right, true);
        font.draw(spriteBatch, text, targetWidth + 2 * spacing, viewHeight - y, targetWidth, Align.left, true);

        spriteBatch.end();

        float rectangleHeight = 400;

        renderer.begin(ShapeType.Line);
        renderer.setColor(Color.GREEN);
        renderer.rect(spacing, viewHeight - y - rectangleHeight, targetWidth, rectangleHeight);
        renderer.rect(targetWidth + 2 * spacing, viewHeight - y - rectangleHeight, targetWidth, rectangleHeight);
        renderer.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void resize(int width, int height) {
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        renderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        renderer.dispose();
        font.dispose();

        // Restore predefined colors
        Colors.reset();
    }
}
