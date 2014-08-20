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

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.viewport.Viewport;

/** @author xoppa */
public class DebugDrawer extends btIDebugDraw {

	public ShapeRenderer shapeRenderer = new ShapeRenderer();
	public SpriteBatch spriteBatch = new SpriteBatch();

	/** Used by {@link #draw3dText(Vector3, String)}. May be adjusted (e.g. scaled). */
	public BitmapFont font = new BitmapFont();

	private Camera camera;
	private Viewport viewport;
	private int debugMode = btIDebugDraw.DebugDrawModes.DBG_NoDebug;

	@Override
	public void drawLine (Vector3 from, Vector3 to, Vector3 color) {
		shapeRenderer.setColor(color.x, color.y, color.z, 1f);
		shapeRenderer.line(from, to);
	}

	private static final Vector3 to = new Vector3();

	@Override
	public void drawContactPoint (Vector3 pointOnB, Vector3 normalOnB, float distance, int lifeTime, Vector3 color) {
		shapeRenderer.setColor(color.x, color.y, color.z, 1f);
		shapeRenderer.point(pointOnB.x, pointOnB.y, pointOnB.z);

		to.set(pointOnB.x, pointOnB.y, pointOnB.z);
		to.add(normalOnB.scl(distance));
		shapeRenderer.line(pointOnB, to);
	}

	@Override
	public void drawTriangle (Vector3 v0, Vector3 v1, Vector3 v2, Vector3 color, float arg4) {
		shapeRenderer.setColor(color.x, color.y, color.z, arg4);
		shapeRenderer.line(v0, v1);
		shapeRenderer.line(v1, v2);
		shapeRenderer.line(v2, v0);
	}

	@Override
	public void reportErrorWarning (String warningString) {
		Gdx.app.error("Bullet", warningString);
	}

	private static final Vector3 tmp = new Vector3();

	@Override
	public void draw3dText (Vector3 location, String textString) {
		// this check is necessary to avoid "mirrored" instances of the text
		if (camera.frustum.pointInFrustum(location)) {
			if (viewport != null) {
				camera.project(location, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(),
					viewport.getScreenHeight());
			} else {
				camera.project(location);
			}

			shapeRenderer.end();
			spriteBatch.begin();

			// the text will be centered on the position
			TextBounds bounds = font.getBounds(textString);
			font.draw(spriteBatch, textString, location.x - (bounds.width / 2), location.y + (bounds.height / 2));

			spriteBatch.end();
			shapeRenderer.begin(ShapeType.Line);
		}
	}

	@Override
	public void setDebugMode (int debugMode) {
		this.debugMode = debugMode;
	}

	@Override
	public int getDebugMode () {
		return debugMode;
	}

	/** Use this in case no {@code glViewport} is in use. Otherwise please supply the used {@link Viewport} to
	 * {@link #begin(Viewport)}.
	 * @param camera The (perspective) camera to be used when doing the debug rendering. */
	public void begin (Camera camera) {
		this.camera = camera;

		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Line);
	}

	/** This has to be used in case the camera to be used is managed via a {@link Viewport}.
	 * @param viewport The currently used viewport with its managed (perspective) camera. */
	public void begin (Viewport viewport) {
		this.viewport = viewport;
		begin(viewport.getCamera());
	}

	/** Ends the debug rendering process and leads to a flush. */
	public void end () {
		shapeRenderer.end();
	}
}
