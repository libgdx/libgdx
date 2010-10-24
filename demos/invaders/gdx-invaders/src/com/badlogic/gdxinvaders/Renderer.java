/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdxinvaders;

import java.io.InputStream;
import java.util.ArrayList;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.ModelLoader;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdxinvaders.simulation.Block;
import com.badlogic.gdxinvaders.simulation.Explosion;
import com.badlogic.gdxinvaders.simulation.Invader;
import com.badlogic.gdxinvaders.simulation.Ship;
import com.badlogic.gdxinvaders.simulation.Shot;
import com.badlogic.gdxinvaders.simulation.Simulation;

/**
 * The renderer receives a simulation and renders it.
 * @author mzechner
 * 
 */
public class Renderer {
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
	private Font font;
	/** the rotation angle of all invaders around y **/
	private float invaderAngle = 0;
	/** status string **/
	private String status = "";
	/** keeping track of the last score so we don't constantly construct a new string **/
	private int lastScore = 0;
	private int lastLives = 0;
	private int lastWave = 0;

	/** view and transform matrix for text rendering **/
	private Matrix4 viewMatrix = new Matrix4();
	private Matrix4 transformMatrix = new Matrix4();

	/** perspective camera **/
	private PerspectiveCamera camera;

	public Renderer (Application app) {
		try {
			spriteBatch = new SpriteBatch();

			InputStream in = app.getFiles().readFile("data/ship.obj", FileType.Internal);
			shipMesh = ModelLoader.loadObj(in, true);
			in.close();

			in = app.getFiles().readFile("data/invader.obj", FileType.Internal);
			invaderMesh = ModelLoader.loadObj(in, true);
			in.close();

			in = app.getFiles().readFile("data/block.obj", FileType.Internal);
			blockMesh = ModelLoader.loadObj(in, true);
			in.close();

			in = app.getFiles().readFile("data/shot.obj", FileType.Internal);
			shotMesh = ModelLoader.loadObj(in, true);
			in.close();

			shipTexture = app.getGraphics().newTexture(app.getFiles().getFileHandle("data/ship.png", FileType.Internal),
				TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
			invaderTexture = app.getGraphics().newTexture(app.getFiles().getFileHandle("data/invader.png", FileType.Internal),
				TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
			backgroundTexture = app.getGraphics().newTexture(app.getFiles().getFileHandle("data/planet.jpg", FileType.Internal),
				TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
			explosionTexture = app.getGraphics().newTexture(app.getFiles().getFileHandle("data/explode.png", FileType.Internal),
				TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);

			explosionMesh = new Mesh(true, false, 4 * 16, 0, new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord"));

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

			font = app.getGraphics().newFont(app.getFiles().getFileHandle("data/font.ttf", FileType.Internal), 10, FontStyle.Plain);

			camera = new PerspectiveCamera();
			camera.setFov(67);
			camera.setViewport(480, 320);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void render (Application app, Simulation simulation) {
		GL10 gl = app.getGraphics().getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glViewport(0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight());

		renderBackground(gl);

		gl.glDisable(GL10.GL_DITHER);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_CULL_FACE);

		setProjectionAndCamera(app.getGraphics(), simulation.ship, app);
		setLighting(gl);

		gl.glEnable(GL10.GL_TEXTURE_2D);

		renderShip(gl, simulation.ship, app);
		renderInvaders(gl, simulation.invaders);

		gl.glDisable(GL10.GL_TEXTURE_2D);
		renderBlocks(gl, simulation.blocks);

		gl.glDisable(GL10.GL_LIGHTING);
		renderShots(gl, simulation.shots);

		gl.glEnable(GL10.GL_TEXTURE_2D);
		renderExplosions(gl, simulation.explosions);

		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_DEPTH_TEST);

		spriteBatch.begin(viewMatrix, transformMatrix);
		if (simulation.ship.lives != lastLives || simulation.score != lastScore || simulation.wave != lastWave) {
			status = "lives: " + simulation.ship.lives + " wave: " + simulation.wave + " score: " + simulation.score;
			lastLives = simulation.ship.lives;
			lastScore = simulation.score;
			lastWave = simulation.wave;
		}
		spriteBatch.drawText(font, status, 0, 320, Color.WHITE);
		spriteBatch.end();

		invaderAngle += app.getGraphics().getDeltaTime() * 90;
		if (invaderAngle > 360) invaderAngle -= 360;
	}

	private void renderBackground (GL10 gl) {
		viewMatrix.setToOrtho2D(0, 0, 400, 320);
		spriteBatch.begin(viewMatrix, transformMatrix);
		spriteBatch.disableBlending();
		spriteBatch.draw(backgroundTexture, 0, 320, 480, 320, 0, 0, 512, 512, Color.WHITE, false, false);
		spriteBatch.end();
	}

	final Vector3 dir = new Vector3();

	private void setProjectionAndCamera (Graphics graphics, Ship ship, Application app) {
		camera.getPosition().set(ship.position.x, 6, 2);
		dir.set(ship.position.x, 0, -4).sub(camera.getPosition()).nor();
		camera.getDirection().set(dir);
		camera.update();
		GL10 gl = graphics.getGL10();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadMatrixf(camera.getCombinedMatrix().val, 0);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	float[] direction = {1, 0.5f, 0, 0};

	private void setLighting (GL10 gl) {
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, direction, 0);
		gl.glEnable(GL10.GL_COLOR_MATERIAL);
	}

	private void renderShip (GL10 gl, Ship ship, Application app) {
		if (ship.isExploding) return;

		shipTexture.bind();
		gl.glPushMatrix();
		gl.glTranslatef(ship.position.x, ship.position.y, ship.position.z);
		gl.glRotatef(45 * (-app.getInput().getAccelerometerY() / 5), 0, 0, 1);
		gl.glRotatef(180, 0, 1, 0);
		shipMesh.render(GL10.GL_TRIANGLES);
		gl.glPopMatrix();
	}

	private void renderInvaders (GL10 gl, ArrayList<Invader> invaders) {
		invaderTexture.bind();
		for (int i = 0; i < invaders.size(); i++) {
			Invader invader = invaders.get(i);
			gl.glPushMatrix();
			gl.glTranslatef(invader.position.x, invader.position.y, invader.position.z);
			gl.glRotatef(invaderAngle, 0, 1, 0);
			invaderMesh.render(GL10.GL_TRIANGLES);
			gl.glPopMatrix();
		}
	}

	private void renderBlocks (GL10 gl, ArrayList<Block> blocks) {
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glColor4f(0.2f, 0.2f, 1, 0.7f);
		for (int i = 0; i < blocks.size(); i++) {
			Block block = blocks.get(i);
			gl.glPushMatrix();
			gl.glTranslatef(block.position.x, block.position.y, block.position.z);
			blockMesh.render(GL10.GL_TRIANGLES);
			gl.glPopMatrix();
		}
		gl.glColor4f(1, 1, 1, 1);
		gl.glDisable(GL10.GL_BLEND);
	}

	private void renderShots (GL10 gl, ArrayList<Shot> shots) {
		gl.glColor4f(1, 1, 0, 1);
		for (int i = 0; i < shots.size(); i++) {
			Shot shot = shots.get(i);
			gl.glPushMatrix();
			gl.glTranslatef(shot.position.x, shot.position.y, shot.position.z);
			shotMesh.render(GL10.GL_TRIANGLES);
			gl.glPopMatrix();
		}
		gl.glColor4f(1, 1, 1, 1);
	}

	private void renderExplosions (GL10 gl, ArrayList<Explosion> explosions) {
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		explosionTexture.bind();
		for (int i = 0; i < explosions.size(); i++) {
			Explosion explosion = explosions.get(i);
			gl.glPushMatrix();
			gl.glTranslatef(explosion.position.x, explosion.position.y, explosion.position.z);
			explosionMesh.render(GL10.GL_TRIANGLE_FAN, (int)((explosion.aliveTime / Explosion.EXPLOSION_LIVE_TIME) * 15) * 4, 4);
			gl.glPopMatrix();
		}
		gl.glDisable(GL10.GL_BLEND);
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
