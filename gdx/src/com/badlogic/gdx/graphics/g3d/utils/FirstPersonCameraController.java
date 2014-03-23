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
	private static final int LEFT_FLAG = 1;
	private static final int RIGHT_FLAG = 2;
	private static final int FORWARD_FLAG = 4;
	private static final int BACKWARD_FLAG = 8;
	private static final int UP_FLAG = 16;
	private static final int DOWN_FLAG = 32;
	
	private final Camera camera;
	private int keyFlags = 0;
	private int STRAFE_LEFT = Keys.A;
	private int STRAFE_RIGHT = Keys.D;
	private int FORWARD = Keys.W;
	private int BACKWARD = Keys.S;
	private int UP = Keys.Q;
	private int DOWN = Keys.E;
	private float velocity = 5;
	private float degreesPerPixel = 0.5f;
	private final Vector3 tmp = new Vector3();
	private final Vector3 tmp2 = new Vector3();

	public FirstPersonCameraController (Camera camera) {
		this.camera = camera;
	}

	@Override
	public boolean keyDown (int keycode) {
		int flag = getFlag(keycode);
		if (flag != 0) {
			keyFlags |= flag;
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		int flag = getFlag(keycode);
		if (flag != 0) {
			keyFlags &= ~getFlag(keycode);
			return true;
		}
		return false;
	}
	/**
	 * Gets the flag for key 
	 * @param key target key flag
	 * @return returns the flag or 0 if not found.      
	 */
	private int getFlag (int key) {
		if (key == UP) {
			return UP_FLAG;
		}
		if (key == DOWN) {
			return DOWN_FLAG;
		}
		if (key == BACKWARD) {
			return BACKWARD_FLAG;	
		}
		if (key == FORWARD) {
			return FORWARD_FLAG;
		}
		if (key == STRAFE_LEFT) {
			return LEFT_FLAG;
		}
		if (key == STRAFE_RIGHT) {
			return RIGHT_FLAG;	
		}
		return 0;
	}
	
	/** Checks if a key flag has been set.
	 * @param flag the flag to check for
	 */
	private boolean hasFlag (int flag) {
		return (keyFlags & flag) != 0;
	}
	

	/** Sets the velocity in units per second for moving forward, backward and strafing left/right.
	 * @param velocity the velocity in units per second */
	public void setVelocity (float velocity) {
		this.velocity = velocity;
	}

	/** Sets how many degrees to rotate per pixel the mouse moved.
	 * @param degreesPerPixel */
	public void setDegreesPerPixel (float degreesPerPixel) {
		this.degreesPerPixel = degreesPerPixel;
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
		float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
		camera.direction.rotate(camera.up, deltaX);
		tmp.set(camera.direction).crs(camera.up).nor();
		camera.direction.rotate(tmp, deltaY);
// camera.up.rotate(tmp, deltaY);
		return true;
	}

	public void update () {
		update(Gdx.graphics.getDeltaTime());
	}

	public void update (float deltaTime) {
		if (hasFlag(FORWARD_FLAG)) {
			tmp.set(camera.direction).nor().scl(deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (hasFlag(BACKWARD_FLAG)) {
			tmp.set(camera.direction).nor().scl(-deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (hasFlag(LEFT_FLAG)) {
			tmp.set(camera.direction).crs(camera.up).nor().scl(-deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (hasFlag(RIGHT_FLAG)) {
			tmp.set(camera.direction).crs(camera.up).nor().scl(deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (hasFlag(UP_FLAG)) {
			tmp.set(camera.up).nor().scl(deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (hasFlag(DOWN_FLAG)) {
			tmp.set(camera.up).nor().scl(-deltaTime * velocity);
			camera.position.add(tmp);
		}
		camera.update(true);
	}
}
