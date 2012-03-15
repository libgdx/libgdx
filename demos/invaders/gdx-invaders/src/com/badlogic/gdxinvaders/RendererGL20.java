/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdxinvaders;

import java.io.InputStream;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderOld;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdxinvaders.simulation.Block;
import com.badlogic.gdxinvaders.simulation.Explosion;
import com.badlogic.gdxinvaders.simulation.Invader;
import com.badlogic.gdxinvaders.simulation.Ship;
import com.badlogic.gdxinvaders.simulation.Shot;
import com.badlogic.gdxinvaders.simulation.Simulation;

/** The renderer receives a simulation and renders it.
 * @author mzechner */
public class RendererGL20 implements Renderer {
	/** sprite batch to draw text **/
	private SpriteBatch spriteBatch;
	/** the ship mesh **/
	private Mesh shipMesh;
	/** the ship texture **/
	private Texture shipTexture;
	/** the invader mesh **/
	private Mesh invaderMesh;
	/** the invader texture **/
	private Texture invaderTexture;
	/** the block mesh **/
	private Mesh blockMesh;
	/** the shot mesh **/
	private Mesh shotMesh;
	/** the background texture **/
	private Texture backgroundTexture;
	/** the explosion mesh **/
	private Mesh explosionMesh;
	/** the explosion texture **/
	private Texture explosionTexture;
	/** the font **/
	private BitmapFont font;
	/** the rotation angle of all invaders around y **/
	private float invaderAngle = 0;
	/** status string **/
	private String status = "";
	/** keeping track of the last score so we don't constantly construct a new string **/
	private int lastScore = 0;
	private int lastLives = 0;
	private int lastWave = 0;

	/** view and transform matrix for text rendering and transforming 3D objects**/
	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transform = new Matrix4();
	private final Matrix4 normal = new Matrix4();
	private final Matrix3 normal3 = new Matrix3();
	
	/** perspective camera **/
	private PerspectiveCamera camera;
	
	/** shaders **/
	ShaderProgram texShader;
	ShaderProgram colorShader;
	ShaderProgram lightTexShader;

	public RendererGL20 () {
		try {
			spriteBatch = new SpriteBatch();

			texShader = new ShaderProgram(Gdx.files.internal("data/shaders/tex-vs.glsl"), Gdx.files.internal("data/shaders/tex-fs.glsl"));
			colorShader = new ShaderProgram(Gdx.files.internal("data/shaders/color-vs.glsl"), Gdx.files.internal("data/shaders/color-fs.glsl"));
			lightTexShader = new ShaderProgram(Gdx.files.internal("data/shaders/light-tex-vs.glsl"), Gdx.files.internal("data/shaders/light-tex-fs.glsl"));
			
			if(!texShader.isCompiled()) throw new GdxRuntimeException("Couldn't compile tex shader");
			if(!colorShader.isCompiled()) throw new GdxRuntimeException("Couldn't compile color shader");
			if(!lightTexShader.isCompiled()) throw new GdxRuntimeException("Couldn't compile light/tex shader");
			
			InputStream in = Gdx.files.internal("data/ship.obj").read();
			shipMesh = ObjLoader.loadObj(in);
			in.close();

			in = Gdx.files.internal("data/invader.obj").read();
			invaderMesh = ObjLoader.loadObj(in);
			in.close();

			in = Gdx.files.internal("data/block.obj").read();
			blockMesh = ObjLoader.loadObj(in);
			in.close();

			in = Gdx.files.internal("data/shot.obj").read();
			shotMesh = ObjLoader.loadObj(in);
			in.close();

			shipTexture = new Texture(Gdx.files.internal("data/ship.png"), Format.RGB565, true);
			shipTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
			invaderTexture = new Texture(Gdx.files.internal("data/invader.png"), Format.RGB565, true);
			invaderTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
			backgroundTexture = new Texture(Gdx.files.internal("data/planet.jpg"), Format.RGB565, true);
			backgroundTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
			explosionTexture = new Texture(Gdx.files.internal("data/explode.png"), Format.RGBA4444, true);
			explosionTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);

			explosionMesh = new Mesh(true, 4 * 16, 0, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(
				Usage.TextureCoordinates, 2, "a_texCoord0"));

			float[] vertices = new float[4 * 16 * (3 + 2)];
			int idx = 0;
			for (int row = 0; row < 4; row++) {
				for (int column = 0; column < 4; column++) {
					vertices[idx++] = 1;
					vertices[idx++] = 1;
					vertices[idx++] = 0;
					vertices[idx++] = 0.25f + column * 0.25f;
					vertices[idx++] = 0 + row * 0.25f;

					vertices[idx++] = -1;
					vertices[idx++] = 1;
					vertices[idx++] = 0;
					vertices[idx++] = 0 + column * 0.25f;
					vertices[idx++] = 0 + row * 0.25f;

					vertices[idx++] = -1;
					vertices[idx++] = -1;
					vertices[idx++] = 0;
					vertices[idx++] = 0f + column * 0.25f;
					vertices[idx++] = 0.25f + row * 0.25f;

					vertices[idx++] = 1;
					vertices[idx++] = -1;
					vertices[idx++] = 0;
					vertices[idx++] = 0.25f + column * 0.25f;
					vertices[idx++] = 0.25f + row * 0.25f;
				}
			}

			explosionMesh.setVertices(vertices);
			font = new BitmapFont(Gdx.files.internal("data/font10.fnt"), Gdx.files.internal("data/font10.png"), false);

			camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void render (Simulation simulation, float delta) {
		// We explicitly require GL10, otherwise we could've used the GLCommon
		// interface via Gdx.gl
		GLCommon gl = Gdx.gl;
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		renderBackground();
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_CULL_FACE);
		setProjectionAndCamera(simulation.ship);
		renderShip(simulation.ship);
		renderInvaders(simulation.invaders);
		renderBlocks(simulation.blocks);
		renderShots(simulation.shots);
		renderExplosions(simulation.explosions);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_DEPTH_TEST);

		spriteBatch.setProjectionMatrix(viewMatrix);
		spriteBatch.begin();
		if (simulation.ship.lives != lastLives || simulation.score != lastScore || simulation.wave != lastWave) {
			status = "lives: " + simulation.ship.lives + " wave: " + simulation.wave + " score: " + simulation.score;
			lastLives = simulation.ship.lives;
			lastScore = simulation.score;
			lastWave = simulation.wave;
		}
		spriteBatch.enableBlending();
		font.draw(spriteBatch, status, 0, 320);
		spriteBatch.end();

		invaderAngle += delta * 90;
		if (invaderAngle > 360) invaderAngle -= 360;
	}

	private void renderBackground () {
		viewMatrix.setToOrtho2D(0, 0, 400, 320);
		spriteBatch.setProjectionMatrix(viewMatrix);
		spriteBatch.begin();
		spriteBatch.disableBlending();
		spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(backgroundTexture, 0, 0, 480, 320, 0, 0, 512, 512, false, false);
		spriteBatch.end();
	}

	final Vector3 dir = new Vector3();

	private void setProjectionAndCamera (Ship ship) {
		camera.position.set(ship.position.x, 6, 2);
		camera.direction.set(ship.position.x, 0, -4).sub(camera.position).nor();
		camera.update();
	}

	private void renderShip (Ship ship) {
		if (ship.isExploding) return;

		shipTexture.bind();
		lightTexShader.begin();
		lightTexShader.setUniformi("u_diffuse", 0);
		transform.set(camera.combined);
		transform.translate(ship.position.x, ship.position.y, ship.position.z);
		transform.rotate(0, 0, 1, 45 * (-Gdx.input.getAccelerometerY() / 5));
		transform.rotate(0, 1, 0, 180);
		lightTexShader.setUniformMatrix("u_projView", transform);
		normal.idt();
		normal.rotate(0, 1, 0, 180);
		normal3.set(normal.toNormalMatrix());
		lightTexShader.setUniformMatrix("u_normal", normal3); 
		shipMesh.render(lightTexShader, GL10.GL_TRIANGLES);
		lightTexShader.end();
	}

	private void renderInvaders (ArrayList<Invader> invaders) {
		lightTexShader.begin();
		normal.idt();
		normal.rotate(0, 1, 0, invaderAngle);
		normal3.set(normal.toNormalMatrix());
		lightTexShader.setUniformMatrix("u_normal", normal3); 
		invaderTexture.bind();
		for (int i = 0; i < invaders.size(); i++) {
			Invader invader = invaders.get(i);
			transform.set(camera.combined);
			transform.translate(invader.position.x, invader.position.y, invader.position.z);
			transform.rotate(0, 1, 0, invaderAngle);
			lightTexShader.setUniformMatrix("u_projView", transform);
			invaderMesh.render(lightTexShader, GL10.GL_TRIANGLES);
		}
		lightTexShader.end();
	}

	private void renderBlocks (ArrayList<Block> blocks) {
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		colorShader.begin();
		colorShader.setUniformf("u_color", 0, 0, 1, 0.5f);
		for (int i = 0; i < blocks.size(); i++) {
			Block block = blocks.get(i);
			transform.set(camera.combined);
			transform.translate(block.position.x, block.position.y, block.position.z);
			colorShader.setUniformMatrix("u_projView", transform);
			blockMesh.render(colorShader, GL10.GL_TRIANGLES);
		}
		colorShader.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);
	}

	private void renderShots (ArrayList<Shot> shots) {
		colorShader.begin();
		colorShader.setUniformf("u_color", 1, 1, 0, 1f);
		for (int i = 0; i < shots.size(); i++) {
			Shot shot = shots.get(i);
			transform.set(camera.combined);
			transform.translate(shot.position.x, shot.position.y, shot.position.z);
			colorShader.setUniformMatrix("u_projView", transform);
			shotMesh.render(colorShader, GL10.GL_TRIANGLES);
		}
		colorShader.end();
	}

	private void renderExplosions (ArrayList<Explosion> explosions) {
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		explosionTexture.bind();
		texShader.begin();
		texShader.setUniformi("u_diffuse", 0);
		for (int i = 0; i < explosions.size(); i++) {
			Explosion explosion = explosions.get(i);
			transform.set(camera.combined);
			transform.translate(explosion.position.x, explosion.position.y, explosion.position.z);
			texShader.setUniformMatrix("u_projView", transform);
			explosionMesh.render(texShader, GL10.GL_TRIANGLE_FAN, (int)(explosion.aliveTime / Explosion.EXPLOSION_LIVE_TIME * 15) * 4, 4);
		}
		texShader.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);
	}

	public void dispose () {
		spriteBatch.dispose();
		shipTexture.dispose();
		invaderTexture.dispose();
		backgroundTexture.dispose();
		explosionTexture.dispose();
		font.dispose();
		explosionMesh.dispose();
		shipMesh.dispose();
		invaderMesh.dispose();
		shotMesh.dispose();
		blockMesh.dispose();
	}
}
