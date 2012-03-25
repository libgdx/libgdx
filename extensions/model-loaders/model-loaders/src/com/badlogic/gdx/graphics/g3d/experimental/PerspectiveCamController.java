package com.badlogic.gdx.graphics.g3d.experimental;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;

public class PerspectiveCamController extends InputAdapter {

	static final float MOUSE_SENSITIVITY = 0.25f;
	static final float MOVE_SPEED = 2;
	static final float MOVE_SPEED_SQRT = (float) Math.sqrt(MOVE_SPEED);

	static final float NINETY_DEGREE = 89.99f; //gimbal lock prevention 
	
	PerspectiveCamera cam;
	int lastX;
	int lastY;
	float angleX = -90;
	float angleY = 0;

	boolean W, A, S, D;

	public PerspectiveCamController(PerspectiveCamera cam) {
		this.cam = cam;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		lastX = x;
		lastY = y;
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {

		angleX += (lastX - x) * MOUSE_SENSITIVITY;
		lastX = x;
		angleY += (lastY - y) * -MOUSE_SENSITIVITY;
		lastY = y;

		if (angleY > NINETY_DEGREE)
			angleY = NINETY_DEGREE;
		else if (angleY < -NINETY_DEGREE)
			angleY = -NINETY_DEGREE;

		// first rotate around y axel
		// then rotate up/down, and
		final float cos = MathUtils.cosDeg(angleY);
		cam.direction.x = MathUtils.cosDeg(angleX) * cos;
		cam.direction.y = MathUtils.sinDeg(angleY) * 1f;
		cam.direction.z = MathUtils.sinDeg(angleX) * cos;
		cam.update();
		return true;
	}

	@Override
	public boolean scrolled(int amount) {
		cam.fieldOfView -= -amount * 0.1f;
		cam.update();
		return true;
	}

	@Override
	public boolean keyDown(int keycode) {

		if (keycode == Keys.A)
			A = true;
		else if (keycode == Keys.D)
			D = true;
		else if (keycode == Keys.S)
			S = true;
		else if (keycode == Keys.W)
			W = true;

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.A)
			A = false;
		else if (keycode == Keys.D)
			D = false;
		else if (keycode == Keys.S)
			S = false;
		else if (keycode == Keys.W)
			W = false;

		return false;
	}

	void update(float delta) {

		// if all is false
		if (!(A | D | W | S))
			return;

		// is moving diagonal move speed is sqrt of normal
		if ((A ^ D) & (W ^ S))
			delta *= MOVE_SPEED_SQRT;
		else {
			// if moving one direction move speed is full
			delta *= MOVE_SPEED;
		}

		if (A & !D) {
			cam.position.x += delta * MathUtils.sinDeg(angleX);
			cam.position.z -= delta * MathUtils.cosDeg(angleX);
		}
		if (D & !A) {
			cam.position.x -= delta * MathUtils.sinDeg(angleX);
			cam.position.z += delta * MathUtils.cosDeg(angleX);
		}

		if (W & !S) {
			cam.position.x += delta * cam.direction.x;
			cam.position.y += delta * cam.direction.y;
			cam.position.z += delta * cam.direction.z;
		}
		if (S & !W) {
			cam.position.x -= delta * cam.direction.x;
			cam.position.y -= delta * cam.direction.y;
			cam.position.z -= delta * cam.direction.z;
		}
		cam.update();

	}

}