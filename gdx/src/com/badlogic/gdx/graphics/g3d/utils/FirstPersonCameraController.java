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

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;

/** Takes a {@link Camera} instance and controls it via w,a,s,d and mouse panning.
 * @author badlogic */
public class FirstPersonCameraController extends InputAdapter {
	private final Camera camera;
	private final IntIntMap keys = new IntIntMap();
	private boolean autoUpdate = true;
	private int strafeLeftKey = Keys.A;
	private int strafeRightKey = Keys.D;
	private int forwardKey = Keys.W;
	private int backwardKey = Keys.S;
	private int upKey = Keys.Q;
	private int downKey = Keys.E;
	private float velocity = 5;
	private float degreesPerPixel = 0.5f;
	private final Vector3 tmp = new Vector3();

	public FirstPersonCameraController (Camera camera) {
		this.camera = camera;
	}

	@Override
	public boolean keyDown (int keycode) {
		keys.put(keycode, keycode);
		return true;
	}

	@Override
	public boolean keyUp (int keycode) {
		keys.remove(keycode, 0);
		return true;
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
		float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
		camera.direction.rotate(camera.up, deltaX);
		tmp.set(camera.direction).crs(camera.up).nor();
		camera.direction.rotate(tmp, deltaY);
		return true;
	}

	public void update () {
		update(Gdx.graphics.getDeltaTime());
	}

	public void update (float deltaTime) {
		if (keys.containsKey(forwardKey)) {
			tmp.set(camera.direction).nor().scl(deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (keys.containsKey(backwardKey)) {
			tmp.set(camera.direction).nor().scl(-deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (keys.containsKey(strafeLeftKey)) {
			tmp.set(camera.direction).crs(camera.up).nor().scl(-deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (keys.containsKey(strafeRightKey)) {
			tmp.set(camera.direction).crs(camera.up).nor().scl(deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (keys.containsKey(upKey)) {
			tmp.set(camera.up).nor().scl(deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (keys.containsKey(downKey)) {
			tmp.set(camera.up).nor().scl(-deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (autoUpdate)
			 camera.update(true);
	}

	 public float getVelocity () {
		  return velocity;
	 }

	 /** Sets the velocity in units per second for moving forward, backward and strafing left/right.
	  * @param velocity the velocity in units per second */
	 public void setVelocity (float velocity) {
		  this.velocity = velocity;
	 }

	 public float getDegreesPerPixel () {
		  return degreesPerPixel;
	 }

	 /** Sets how many degrees to rotate per pixel the mouse moved.
	  * @param degreesPerPixel */
	 public void setDegreesPerPixel (float degreesPerPixel) {
		  this.degreesPerPixel = degreesPerPixel;
	 }

	 public boolean isAutoUpdate () {
		  return autoUpdate;
	 }

	 public void setAutoUpdate (boolean autoUpdate) {
		  this.autoUpdate = autoUpdate;
	 }

	 public int getStrafeLeftKey () {
		  return strafeLeftKey;
	 }

	 public void setStrafeLeftKey (int strafeLeftKey) {
		  this.strafeLeftKey = strafeLeftKey;
	 }

	 public int getStrafeRightKey () {
		  return strafeRightKey;
	 }

	 public void setStrafeRightKey (int strafeRightKey) {
		  this.strafeRightKey = strafeRightKey;
	 }

	 public int getForwardKey () {
		  return forwardKey;
	 }

	 public void setForwardKey (int forwardKey) {
		  this.forwardKey = forwardKey;
	 }

	 public int getBackwardKey () {
		  return backwardKey;
	 }

	 public void setBackwardKey (int backwardKey) {
		  this.backwardKey = backwardKey;
	 }

	 public int getUpKey () {
		  return upKey;
	 }

	 public void setUpKey (int upKey) {
		  this.upKey = upKey;
	 }

	 public int getDownKey () {
		  return downKey;
	 }

	 public void setDownKey (int downKey) {
		  this.downKey = downKey;
	 }
}
