/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.ExpandedSprite;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class SpriteExtraAttributesTest extends GdxTest {
	ShaderProgram shader;
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;
	TextureAtlas textureAtlas;
	DoubleSprite sprite;
	Array<DoubleSprite> orbitSprites;
	OrthographicCamera cam;
	Vector3 lightPosition = new Vector3();
	boolean autoLight = true;
	float lightPathRadius;
	float orbitRadius;
	float elapsed;

	/** For simplicity, doesn't check to ensure second sprite is referencing same Texture as first. */
	static class DoubleSprite extends ExpandedSprite {
		public static final Template TEMPLATE = new Template(VertexAttribute.TexCoords(1),
			new VertexAttribute(Usage.Generic, 1, "a_rotation"), new VertexAttribute(Usage.Generic, 1, "a_shininess"));

		private TextureRegion secondRegion;

		public DoubleSprite (TextureRegion firstRegion, TextureRegion secondRegion, float shininess) {
			super(TEMPLATE, firstRegion);
			setSecondSpriteRegion(secondRegion);
			setShininess(shininess);
		}

		public void setSecondSpriteRegion (TextureRegion region) {
			secondRegion = region;
			float u = region.getU();
			float u2 = region.getU2();
			float v = region.getV();
			float v2 = region.getV2();

			setExtraAttributeValue(u, 0, 0, 0); // 1st attribute == texCoords, 1st component == U, 1st vertex
			setExtraAttributeValue(v2, 0, 1, 0); // 1st attribute == texCoords, 2nd component == V, 1st vertex

			setExtraAttributeValue(u, 0, 0, 1); // 1st attribute == texCoords, 1st component == U, 2nd vertex
			setExtraAttributeValue(v, 0, 1, 1); // 1st attribute == texCoords, 2nd component == V, 2nd vertex

			setExtraAttributeValue(u2, 0, 0, 2); // 1st attribute == texCoords, 1st component == U, 3rd vertex
			setExtraAttributeValue(v, 0, 1, 2); // 1st attribute == texCoords, 2nd component == V, 3rd vertex

			setExtraAttributeValue(u2, 0, 0, 3); // 1st attribute == texCoords, 1st component == U, 4th vertex
			setExtraAttributeValue(v2, 0, 1, 3); // 1st attribute == texCoords, 2nd component == V, 4th vertex
		}

		public void setShininess (float shininess) {
			setExtraAttributeSoleValue(shininess, 2); // 3rd attribute == shininess
		}

		@Override
		public float[] getVertices () {
			// Apply rotation as a vertex attribute so it can be used in shader to transform light direction
			setExtraAttributeSoleValue(getRotation() * MathUtils.degRad, "a_rotation"); // string lookup theoretically slower (just
																													// testing)

			return super.getVertices();
		}
	}

	@Override
	public void create () {
		textureAtlas = new TextureAtlas(Gdx.files.internal("data/normalMappedSprites.atlas"));

		sprite = new DoubleSprite(textureAtlas.findRegion("badlogic_diffuse"), textureAtlas.findRegion("badlogic_norm"), 30f);

		orbitSprites = new Array<DoubleSprite>();
		for (int i = 0; i < 2; i++) {
			orbitSprites.add(new DoubleSprite(textureAtlas.findRegion("wood_diffuse"), textureAtlas.findRegion("wood_norm"), 30f));
			orbitSprites.add(new DoubleSprite(textureAtlas.findRegion("brick_diffuse"), textureAtlas.findRegion("brick_norm"), 3f));
			orbitSprites
				.add(new DoubleSprite(textureAtlas.findRegion("brickRound_diffuse"), textureAtlas.findRegion("brickRound_norm"), 3f));
		}
		for (DoubleSprite ds : orbitSprites)
			ds.setOriginCenter();

		shader = new ShaderProgram(Gdx.files.internal("data/shaders/litsprite.vert").readString(),
			Gdx.files.internal("data/shaders/litsprite.frag.").readString());
		shader.begin();
		shader.setUniformf("u_ambient", new Color(0.05f, 0.05f, 0.1f, 1));
		shader.setUniformf("u_specularStrength", 0.7f);
		shader.setUniformf("u_attenuation", 0.002f);
		shader.end();

		batch = new SpriteBatch(1000, null, DoubleSprite.TEMPLATE.getExtraAttributes());
		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setAutoShapeType(true);
		cam = new OrthographicCamera();

		Gdx.input.setInputProcessor(inputAdapter);
	}

	@Override
	public void resize (int width, int height) {
		cam.setToOrtho(false, width, height);
		cam.update();
		lightPathRadius = 0.4f * Math.min(width, height);
		orbitRadius = 0.45f * Math.min(width, height);
		sprite.setPosition(width / 2 - sprite.getWidth() / 2, height / 2 - sprite.getHeight() / 2);
	}

	@Override
	public void render () {
		elapsed += Gdx.graphics.getDeltaTime();

		sprite.setRotation(20 * elapsed);

		for (int i = 0; i < orbitSprites.size; i++) {
			orbitSprites.get(i).setRotation(-2 * elapsed);
			float t = 25f * elapsed + 360f * (float)i / orbitSprites.size;
			orbitSprites.get(i).setCenter(cam.position.x + orbitRadius * MathUtils.cosDeg(t),
				cam.position.y + orbitRadius * MathUtils.sinDeg(t));
		}

		if (autoLight) {
			float t = -0.3f * elapsed;
			lightPosition.set(cam.position.x + lightPathRadius * MathUtils.cos(t),
				cam.position.y + lightPathRadius * MathUtils.sin(t), 100);
		}

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(cam.combined);
		batch.setShader(shader);
		batch.begin();
		shader.setUniformf("u_camPosition", cam.position);
		shader.setUniformf("u_lightPosition", lightPosition);
		for (DoubleSprite ds : orbitSprites)
			ds.draw(batch);
		sprite.draw(batch);
		batch.end();

		// batch still usable for traditional stuff if desired, but extra vertex data is transferred.
		batch.setShader(null);
		batch.begin();
		batch.draw(sprite, 0, 0, 50, 50);
		batch.end();

		if (autoLight) {
			shapeRenderer.setProjectionMatrix(cam.combined);
			shapeRenderer.begin();
			shapeRenderer.setColor(0, 0, 0, 1);
			shapeRenderer.circle(lightPosition.x, lightPosition.y, 4);
			shapeRenderer.setColor(1, 1, 1, 1);
			shapeRenderer.circle(lightPosition.x, lightPosition.y, 3);
			shapeRenderer.end();
		}
	}

	@Override
	public void dispose () {
		shader.dispose();
	}

	private InputAdapter inputAdapter = new InputAdapter() {

		final Vector3 tmp = new Vector3();

		public boolean touchDown (int screenX, int screenY, int pointer, int button) {
			autoLight = false;
			cam.unproject(tmp.set(screenX, screenY, 0));
			lightPosition.x = tmp.x;
			lightPosition.y = tmp.y;
			return true;
		}

		public boolean touchDragged (int screenX, int screenY, int pointer) {
			cam.unproject(tmp.set(screenX, screenY, 0));
			lightPosition.x = tmp.x;
			lightPosition.y = tmp.y;
			return true;
		}

		public boolean touchUp (int screenX, int screenY, int pointer, int button) {
			autoLight = true;
			return false;
		}
	};
}
