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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonRegionLoader;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class PolygonSpriteTest extends GdxTest {

	PolygonSpriteBatch batch;
	ShapeRenderer renderer;

	Texture texture;
	OrthographicCamera camera;
	PolygonRegion region;
	Rectangle bounds;

	Array<PolygonSprite> sprites = new Array<PolygonSprite>();

	@Override
	public void create () {
		texture = new Texture(Gdx.files.internal("data/tree.png"));

		PolygonRegionLoader loader = new PolygonRegionLoader();
		region = loader.load(new TextureRegion(texture), Gdx.files.internal("data/tree.psh"));

		renderer = new ShapeRenderer();

		camera = new OrthographicCamera(480, 320);
		camera.position.x = 240;
		camera.position.y = 160;
		camera.update();

		batch = new PolygonSpriteBatch();

		for (int i = 0; i < 50; i++) {
			PolygonSprite sprite = new PolygonSprite(region);
			sprite.setPosition(MathUtils.random(-30, 440), MathUtils.random(-30, 290));
			sprite.setColor(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1.0f);
			sprite.setScale(MathUtils.random(0.5f, 1.5f), MathUtils.random(0.5f, 1.5f));
			sprites.add(sprite);
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		for (int i = 0; i < sprites.size; i++) {
			PolygonSprite sprite = sprites.get(i);
			sprite.rotate(45 * Gdx.graphics.getDeltaTime());
			sprite.translateX(10 * Gdx.graphics.getDeltaTime());

			if (sprite.getX() > 450) sprite.setX(-50);

			sprite.draw(batch);
		}
		batch.end();

		// Some debug rendering, bounding box & origin of one sprite
		renderer.setProjectionMatrix(camera.combined);
		renderer.setColor(Color.GREEN);
		renderer.begin(ShapeType.Line);

		PolygonSprite sprite = sprites.get(49);

		bounds = sprite.getBoundingRectangle();
		renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

		renderer.end();

		renderer.begin(ShapeType.Filled);

		renderer.circle(sprite.getX() + sprite.getOriginX(), sprite.getY() + sprite.getOriginY(), 4);

		renderer.end();
	}

	@Override
	public void dispose () {
		texture.dispose();
		batch.dispose();
	}
}
