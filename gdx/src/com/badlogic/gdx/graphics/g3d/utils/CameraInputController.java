package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class CameraInputController extends InputAdapter {
	/** The button for rotating the camera. */ 
	public int rotateButton = Buttons.LEFT;
	/** The angle to rotate when moved the full width or height of the screen. */
	public float rotateAngle = 360f;
	/** The button for translating the camera along the up/right plane */
	public int translateButton = Buttons.RIGHT;
	/** The units to translate the camera when moved the full width or height of the screen. */
	public float translateUnits = 10f; // FIXME auto calculate this based on the target
	/** The button for translating the camera along the direction axis */
	public int forwardButton = Buttons.MIDDLE;
	/** The key which must be pressed to activate rotate, translate and forward or 0 to always activate. */ 
	public int activateKey = 0;
	/** Indicates if the activateKey is currently being pressed. */
	protected boolean activatePressed;
	/** Whether scrolling requires the activeKey to be pressed (false) or always allow scrolling (true). */
	public boolean alwaysScroll = true;
	/** The weight for each scrolled amount. */
	public float scrollFactor = -0.1f;
	/** Whether to update the camera after it has been changed. */
	public boolean autoUpdate = true;
	/** The target to rotate around. */
	public Vector3 target = new Vector3();
	/** Whether to update the target on translation */ 
	public boolean translateTarget = true;
	/** Whether to update the target on forward */
	public boolean forwardTarget = true;
	/** Whether to update the target on scroll */
	public boolean scrollTarget = false;
	public int forwardKey = Keys.W;
	protected boolean forwardPressed;
	public int backwardKey = Keys.S;
	protected boolean backwardPressed;
	public int rotateRightKey = Keys.A;
	protected boolean rotateRightPressed;
	public int rotateLeftKey = Keys.D;
	protected boolean rotateLeftPressed;
	/** The camera. */
	public Camera camera;
	/** The current (first) button being pressed. */
	protected int button = -1;
	
	private float startX, startY;
	private final Vector3 tmpV1 = new Vector3();
	private final Vector3 tmpV2 = new Vector3();
	
	public CameraInputController(final Camera camera) {
		this.camera = camera;
	}
	
	public void update() {
		if (rotateRightPressed || rotateLeftPressed || forwardPressed || backwardPressed) {
			final float delta = Gdx.graphics.getDeltaTime();
			if (rotateRightPressed)
				camera.rotate(camera.up, -delta * rotateAngle);
			if (rotateLeftPressed)
				camera.rotate(camera.up, delta * rotateAngle);
			if (forwardPressed) {
				camera.translate(tmpV1.set(camera.direction).scl(delta * translateUnits));
				if (forwardTarget)
					target.add(tmpV1);
			}
			if (backwardPressed) {
				camera.translate(tmpV1.set(camera.direction).scl(-delta * translateUnits));
				if (forwardTarget)
					target.add(tmpV1);
			}
			if (autoUpdate)
				camera.update();
		}
	}
	
	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (this.button < 0 && (activateKey == 0 || activatePressed)) {
			startX = screenX;
			startY = screenY;
			this.button = button;
		}
		return activatePressed;
	}
	
	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button == this.button)
			this.button = -1;
		return activatePressed;
	}
	
	protected boolean process(float deltaX, float deltaY, int button) {
		if (button == rotateButton) {
			tmpV1.set(camera.direction).crs(camera.up).y = 0f;
			camera.rotateAround(target, tmpV1.nor(), deltaY * rotateAngle);
			camera.rotateAround(target, Vector3.Y, deltaX * -rotateAngle);
		} else if (button == translateButton) {
			camera.translate(tmpV1.set(camera.direction).crs(camera.up).nor().scl(-deltaX * translateUnits));
			camera.translate(tmpV2.set(camera.up).scl(-deltaY * translateUnits));
			if (translateTarget)
				target.add(tmpV1).add(tmpV2);				
		} else if (button == forwardButton) {
			camera.translate(tmpV1.set(camera.direction).scl(deltaY * translateUnits));
			if (forwardTarget)
				target.add(tmpV1);
		}
		if (autoUpdate)
			camera.update();
		return true;
	}
	
	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		if (this.button < 0)
			return false;
		final float deltaX = (screenX - startX) / Gdx.graphics.getWidth();
		final float deltaY = (startY - screenY) / Gdx.graphics.getHeight();
		startX = screenX;
		startY = screenY;
		return process(deltaX, deltaY, button);
	}
	
	@Override
	public boolean scrolled (int amount) {
		if (!alwaysScroll && activateKey != 0 && !activatePressed)
			return false;
		camera.translate(tmpV1.set(camera.direction).scl(amount * scrollFactor * translateUnits));
		if (scrollTarget)
			target.add(tmpV1);
		if (autoUpdate)
			camera.update();
		return true;
	}
	
	@Override
	public boolean keyDown (int keycode) {
		if (keycode == activateKey)
			activatePressed = true;
		if (keycode == forwardKey)
			forwardPressed = true;
		else if (keycode == backwardKey)
			backwardPressed = true;
		else if (keycode == rotateRightKey)
			rotateRightPressed = true;
		else if (keycode == rotateLeftKey)
			rotateLeftPressed = true;
		return false;
	}
	
	@Override
	public boolean keyUp (int keycode) {
		if (keycode == activateKey) {
			activatePressed = false;
			button = -1;
		}
		if (keycode == forwardKey)
			forwardPressed = false;
		else if (keycode == backwardKey)
			backwardPressed = false;
		else if (keycode == rotateRightKey)
			rotateRightPressed = false;
		else if (keycode == rotateLeftKey)
			rotateLeftPressed = false;
		return false;
	}
}