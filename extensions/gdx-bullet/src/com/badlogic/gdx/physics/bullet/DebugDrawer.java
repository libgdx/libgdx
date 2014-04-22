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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;

/** @author xoppa */
public class DebugDrawer extends btIDebugDraw {
	public int debugMode = 0;
	public ShapeRenderer shapeRenderer = new ShapeRenderer();

	@Override
	public void drawLine (btVector3 from, btVector3 to, btVector3 color) {
		shapeRenderer.setColor(color.getX(), color.getY(), color.getZ(), 1f);
		shapeRenderer.line(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
	}

	@Override
	public void drawContactPoint (btVector3 PointOnB, btVector3 normalOnB, float distance, int lifeTime, btVector3 color) {
	}

	@Override
	public void reportErrorWarning (String warningString) {
	}

	@Override
	public void draw3dText (btVector3 location, String textString) {
	}

	@Override
	public void setDebugMode (int debugMode) {
		this.debugMode = debugMode;
	}

	@Override
	public int getDebugMode () {
		return debugMode;
	}

	public void begin (Camera cam) {
		shapeRenderer.setProjectionMatrix(cam.combined);
		shapeRenderer.begin(ShapeType.Line);
	}

	public void end () {
		shapeRenderer.end();
	}
}
