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
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.ExpandableSprite;
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

	static final String ROTATION_ATTRIBUTE = "a_rotation";
	static final String SHININESS_ATTRIBUTE = "a_shininess";

	/** For simplicity, doesn't check to ensure second sprite is referencing same Texture as first. */
	static class DoubleSprite extends ExpandableSprite {
		static final VertexAttribute[] ATTRIBUTES = {VertexAttribute.TexCoords(1),
			new VertexAttribute(Usage.Generic, 1, ROTATION_ATTRIBUTE), new VertexAttribute(Usage.Generic, 1, SHININESS_ATTRIBUTE)};
		private static final Template TEMPLATE = new Template(ATTRIBUTES);

		private TextureRegion secondRegion;
		float uTwo1, uTwo2, vTwo1, vTwo2;
		final Vector2 tmp = new Vector2();

		public DoubleSprite (TextureRegion firstRegion, TextureRegion secondRegion, float shininess) {
			super(TEMPLATE, firstRegion);
			setSecondSpriteRegion(secondRegion);
			setShininess(shininess);
		}

		public void setSecondSpriteRegion (TextureRegion region) {
			secondRegion = region;
			uTwo1 = region.getU();
			uTwo2 = region.getU2();
			vTwo1 = region.getV();
			vTwo2 = region.getV2();

			AttributesMapping attributes = DoubleSprite.this.attributes;

			setExtraAttributeValue(uTwo1, 0, 0, 0); // 1st attribute == texCoords, 1st element == U, 1st vertex
			setExtraAttributeValue(vTwo2, 0, 1, 0); // 1st attribute == texCoords, 2nd element == V, 1st vertex

			setExtraAttributeValue(uTwo1, 0, 0, 1); // 1st attribute == texCoords, 1st element == U, 2nd vertex
			setExtraAttributeValue(vTwo1, 0, 1, 1); // 1st attribute == texCoords, 2nd element == V, 2nd vertex

			setExtraAttributeValue(uTwo2, 0, 0, 2); // 1st attribute == texCoords, 1st element == U, 3rd vertex
			setExtraAttributeValue(vTwo1, 0, 1, 2); // 1st attribute == texCoords, 2nd element == V, 3rd vertex

			setExtraAttributeValue(uTwo2, 0, 0, 3); // 1st attribute == texCoords, 1st element == U, 4th vertex
			setExtraAttributeValue(vTwo2, 0, 1, 3); // 1st attribute == texCoords, 2nd element == V, 4th vertex
		}

		public void setShininess (float shininess) {
			setExtraAttributeValue(shininess, 2, 0); // 3rd attribute == shininess, 1st and only element
		}

		@Override
		public float[] getVertices () {
			float[] vertices = super.getVertices();
			setExtraAttributeValue(getRotation() * MathUtils.degRad, 1, 0); // 2nd attribute == rotation, 1st and only element
			return vertices;
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
			orbitSprites.add(new DoubleSprite(textureAtlas.findRegion("brickRound_diffuse"), textureAtlas
				.findRegion("brickRound_norm"), 3f));
		}
		for (DoubleSprite ds : orbitSprites)
			ds.setOriginCenter();

		shader = createShader();
		batch = new SpriteBatch(1000, null, DoubleSprite.ATTRIBUTES);
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

		shapeRenderer.setProjectionMatrix(cam.combined);
		shapeRenderer.begin();
		shapeRenderer.circle(lightPosition.x, lightPosition.y, 20);
		shapeRenderer.end();
	}

	@Override
	public void dispose () {
		shader.dispose();
	}

	static public ShaderProgram createShader () {
		String vertexShader = "attribute vec4 "
			+ ShaderProgram.POSITION_ATTRIBUTE
			+ ";\n" //
			+ "attribute vec4 "
			+ ShaderProgram.COLOR_ATTRIBUTE
			+ ";\n" //
			+ "attribute vec2 "
			+ ShaderProgram.TEXCOORD_ATTRIBUTE
			+ "0;\n" //
			+ "attribute vec2 "
			+ ShaderProgram.TEXCOORD_ATTRIBUTE
			+ "1;\n" //
			+ "attribute float "
			+ ROTATION_ATTRIBUTE
			+ ";\n" //
			+ "attribute float "
			+ SHININESS_ATTRIBUTE
			+ ";\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "uniform vec3 u_lightPosition;\n"
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords0;\n" //
			+ "varying vec2 v_texCoords1;\n" //
			+ "varying vec3 v_lightDir;\n" //
			+ "varying vec3 v_pos;\n" //
			+ "varying float v_shininess;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = "
			+ ShaderProgram.COLOR_ATTRIBUTE
			+ ";\n" //
			+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
			+ "   v_texCoords0 = "
			+ ShaderProgram.TEXCOORD_ATTRIBUTE
			+ "0;\n" //
			+ "   v_texCoords1 = "
			+ ShaderProgram.TEXCOORD_ATTRIBUTE
			+ "1;\n" //
			+ "   gl_Position =  u_projTrans * "
			+ ShaderProgram.POSITION_ATTRIBUTE
			+ ";\n" //
			+ "   vec2 tangent = vec2(cos(" + ROTATION_ATTRIBUTE + "), sin(" + ROTATION_ATTRIBUTE + "));\n"
			+ "   vec2 binormal = vec2(-tangent.y, tangent.x);\n" + "   v_lightDir = u_lightPosition.xyz - "
			+ ShaderProgram.POSITION_ATTRIBUTE + ";\n"
			+ "   v_lightDir = vec3(dot(v_lightDir.xy, tangent), dot(v_lightDir.xy, binormal), v_lightDir.z);\n"// rotate to tangent
// space
			+ "   v_pos = " + ShaderProgram.POSITION_ATTRIBUTE + ".xyz;\n" + "   v_shininess = " + SHININESS_ATTRIBUTE + ";\n" //
			+ "}\n";
		String fragmentShader = "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n" //
			+ "varying LOWP vec4 v_color;\n" //
			+ "varying vec2 v_texCoords0;\n" //
			+ "varying vec2 v_texCoords1;\n" //
			+ "varying vec3 v_lightDir;\n" //
			+ "varying vec3 v_pos;\n" //
			+ "varying float v_shininess;\n" //
			+ "uniform vec3 u_camPosition;\n"
			+ "uniform sampler2D u_texture;\n" //
			+ "const vec3 AMBIENT = vec3(0.05, 0.05, 0.1);\n" //
			+ "const float SPEC_STRENGTH = 0.7;\n" //
			+ "const float ATT_FACTOR = 0.002;\n" //
			+ "void main()\n"//
			+ "{\n" //
			+ "  float lightDist2 = dot(v_lightDir, v_lightDir);\n"
			+ "  normalize(v_lightDir);\n"
			+ "  vec3 viewDir = normalize(u_camPosition - v_pos);\n"
			+ "  vec3 halfDir = normalize(v_lightDir + viewDir);\n"
			+ "  LOWP vec4 texture = texture2D (u_texture, v_texCoords0);\n"
			+ "  LOWP vec3 normal = texture2D (u_texture, v_texCoords1).xyz * 2.0 - 1.0;\n"
			+ "  float att = 1.0 / (1.0 + ATT_FACTOR * lightDist2);\n"
			+ "  LOWP vec3 diffuse = min(1.0, (max(0.0, dot(normal, v_lightDir)) * att + AMBIENT)) * texture.rgb;\n"
			+ "  float specular = SPEC_STRENGTH * pow(max(0.0, dot(normal, halfDir)), v_shininess);\n"
			+ "  gl_FragColor = v_color * vec4(diffuse + specular, texture.a);\n" //
			+ "}";

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (shader.isCompiled() == false) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		return shader;
	}

	InputAdapter inputAdapter = new InputAdapter() {

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
