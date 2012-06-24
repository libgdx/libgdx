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

package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ActorEvent.Type;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;

/** A 2D scenegraph containing hierarchies of {@link Actor actors}. Stage handles the viewport and distributing events.
 * <p>
 * A stage fills the whole screen. {@link #setViewport} controls the coordinates used within the stage and sets up the camera used
 * to convert between stage coordinates and screen coordinates. *
 * <p>
 * A stage must receive input events so it can distribute them to actors. This is typically done by passing the stage to
 * {@link Input#setInputProcessor(com.badlogic.gdx.InputProcessor) Gdx.input.setInputProcessor}. An {@link InputMultiplexer} may be
 * used to handle input events before or after the stage does. If an actor handles an event by returning true from the input
 * method, then the stage's input method will also return true, causing subsequent InputProcessors to not receive the event.
 * @author mzechner
 * @author Nathan Sweet */
public class Stage extends InputAdapter implements Disposable {
	private float width, height;
	private float centerX, centerY;
	private Camera camera;
	private final SpriteBatch batch;
	private final boolean ownsBatch;
	private final Group root;
	private Actor[] pointerOverActors = new Actor[20];
	private Actor mouseOverActor;
	private Actor keyboardFocus, scrollFocus;

	/** Creates a stage with a {@link #setViewport(float, float, boolean) viewport} equal to the device screen resolution. The stage
	 * will use its own {@link SpriteBatch}. */
	public Stage () {
		this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
	}

	/** Creates a stage with the specified {@link #setViewport(float, float, boolean) viewport}. The stage will use its own
	 * {@link SpriteBatch}, which will be disposed when the stage is disposed. */
	public Stage (float width, float height, boolean stretch) {
		this.width = width;
		this.height = height;

		ownsBatch = true;
		batch = new SpriteBatch();

		root = new Group("root");
		root.setStage(this);

		camera = new OrthographicCamera();
		setViewport(width, height, stretch);
	}

	/** Creates a stage with the specified {@link #setViewport(float, float, boolean) viewport} and {@link SpriteBatch}. This can be
	 * used to avoid create a new SpriteBatch, which can be somewhat slow.
	 * @param batch Will not be disposed if {@link #dispose()} is called. */
	public Stage (float width, float height, boolean stretch, SpriteBatch batch) {
		this.width = width;
		this.height = height;

		this.batch = batch;
		ownsBatch = false;

		root = new Group("root");
		root.setStage(this);

		camera = new OrthographicCamera();
		setViewport(width, height, stretch);
	}

	/** Sets the dimensions of the stage's viewport. The viewport covers the entire screen. If keepAspectRatio is false and the
	 * specified viewport size is not equal to the screen resolution, it is stretched to the screen resolution. If keepAspectRatio
	 * is true and the specified viewport size is not equal to the screen resolution, it is enlarged in the shorter dimension. */
	public void setViewport (float width, float height, boolean keepAspectRatio) {
		if (keepAspectRatio) {
			if (width > height && width / Gdx.graphics.getWidth() <= height / Gdx.graphics.getHeight()) {
				float toDeviceSpace = Gdx.graphics.getHeight() / height;
				float toViewportSpace = height / Gdx.graphics.getHeight();

				float deviceWidth = width * toDeviceSpace;
				this.width = width + (Gdx.graphics.getWidth() - deviceWidth) * toViewportSpace;
				this.height = height;
			} else {
				float toDeviceSpace = Gdx.graphics.getWidth() / width;
				float toViewportSpace = width / Gdx.graphics.getWidth();

				float deviceHeight = height * toDeviceSpace;
				this.height = height + (Gdx.graphics.getHeight() - deviceHeight) * toViewportSpace;
				this.width = width;
			}
		} else {
			this.width = width;
			this.height = height;
		}

		centerX = this.width / 2;
		centerY = this.height / 2;

		camera.position.set(centerX, centerY, 0);
		camera.viewportWidth = this.width;
		camera.viewportHeight = this.height;
	}

	public void draw () {
		camera.update();
		if (!root.isVisible()) return;
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		root.draw(batch, 1);
		batch.end();
	}

	/** Calls the {@link Actor#act(float)} method on each actor in the stage. Typically called each frame.
	 * @param delta Time in seconds since the last frame. */
	public void act (float delta) {
		// Update over actors.
		for (int pointer = 0, n = pointerOverActors.length; pointer < n; pointer++) {
			Actor overLast = pointerOverActors[pointer];
			// Check if pointer is gone.
			if (!Gdx.input.isTouched(pointer)) {
				if (overLast != null) {
					pointerOverActors[pointer] = null;
					Vector2 stageCoords = toStageCoordinates(Gdx.input.getX(pointer), Gdx.input.getY(pointer));
					// Exit over last.
					ActorEvent event = Pools.obtain(ActorEvent.class);
					event.setRelatedActor(overLast);
					event.setPointer(pointer);
					event.setStageX(stageCoords.x);
					event.setStageY(stageCoords.y);
					event.setType(ActorEvent.Type.exit);
					overLast.fire(event);
					Pools.free(event);
				}
				continue;
			}
			// Update over actor for the pointer.
			pointerOverActors[pointer] = fireEnterAndExit(overLast, Gdx.input.getX(pointer), Gdx.input.getY(pointer), pointer);
		}
		// Update over actor for the mouse on the desktop.
		if (Gdx.app.getType() == ApplicationType.Desktop)
			mouseOverActor = fireEnterAndExit(mouseOverActor, Gdx.input.getX(), Gdx.input.getY(), -1);

		root.act(delta);
	}

	private Actor fireEnterAndExit (Actor overLast, int screenX, int screenY, int pointer) {
		// Find the actor under the pointer.
		Vector2 stageCoords = toStageCoordinates(screenX, screenY);
		Actor over = hit(stageCoords.x, stageCoords.y);
		if (over == overLast) return overLast;

		ActorEvent event = Pools.obtain(ActorEvent.class);
		event.setPointer(pointer);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);
		// Exit overLast.
		if (overLast != null) {
			event.setType(ActorEvent.Type.exit);
			event.setRelatedActor(over);
			overLast.fire(event);
		}
		// Enter over.
		if (over != null) {
			event.setType(ActorEvent.Type.enter);
			event.setRelatedActor(overLast);
			over.fire(event);
		}
		Pools.free(event);
		return over;
	}

	/** Returns deepest actor at x,y that is touchable and visible. */
	private boolean fireTouch (ActorEvent event, ActorEvent.Type type, float stageX, float stageY) {
		event.setType(type);
		event.setStageX(stageX);
		event.setStageY(stageY);

		Actor target = hit(stageX, stageY);
		while (target != null && (!target.isTouchable() || !target.isVisible()))
			target = target.getParent();
		if (target == null) target = root;

		boolean handled = target.fire(event);
		Pools.free(event);
		return handled;
	}

	/** Applies a touch down event to the stage and returns true if an actor in the scene processed the event. */
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		ActorEvent event = Pools.obtain(ActorEvent.class);
		event.setPointer(pointer);
		event.setButton(button);
		Vector2 stageCoords = toStageCoordinates(screenX, screenY);
		return fireTouch(event, ActorEvent.Type.touchDown, stageCoords.x, stageCoords.y);
	}

	/** Applies a touch up event to the stage and returns true if an actor in the scene processed the event. */
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		ActorEvent event = Pools.obtain(ActorEvent.class);
		event.setPointer(pointer);
		event.setButton(button);
		Vector2 stageCoords = toStageCoordinates(screenX, screenY);
		return fireTouch(event, ActorEvent.Type.touchUp, stageCoords.x, stageCoords.y);
	}

	/** Applies a touch moved event to the stage and returns true if an actor in the scene processed the event. */
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		ActorEvent event = Pools.obtain(ActorEvent.class);
		event.setPointer(pointer);
		Vector2 stageCoords = toStageCoordinates(screenX, screenY);
		return fireTouch(event, ActorEvent.Type.touchDragged, stageCoords.x, stageCoords.y);
	}

	/** Applies a touch moved event to the stage and returns true if an actor in the scene processed the event. This event only
	 * occurs on the desktop. */
	public boolean mouseMoved (int screenX, int screenY) {
		ActorEvent event = Pools.obtain(ActorEvent.class);
		Vector2 stageCoords = toStageCoordinates(screenX, screenY);
		return fireTouch(event, ActorEvent.Type.touchMoved, stageCoords.x, stageCoords.y);
	}

	/** Applies a mouse scroll event to the stage and returns true if an actor in the scene processed the event. This event only
	 * occurs on the desktop. */
	public boolean scrolled (int amount) {
		if (scrollFocus == null) return false;
		ActorEvent event = Pools.obtain(ActorEvent.class);
		event.setType(ActorEvent.Type.scrolled);
		event.setScrollAmount(amount);
		boolean handled = scrollFocus.fire(event);
		Pools.free(event);
		return handled;
	}

	/** Applies a key down event to the actor that has {@link Stage#setKeyboardFocus(Actor) keyboard focus}, if any, and returns
	 * true if the event was processed. */
	public boolean keyDown (int keyCode) {
		if (keyboardFocus == null) return false;
		ActorEvent event = Pools.obtain(ActorEvent.class);
		event.setType(ActorEvent.Type.keyDown);
		event.setKeyCode(keyCode);
		boolean handled = keyboardFocus.fire(event);
		Pools.free(event);
		return handled;
	}

	/** Applies a key up event to the actor that has {@link Stage#setKeyboardFocus(Actor) keyboard focus}, if any, and returns true
	 * if the event was processed. */
	public boolean keyUp (int keyCode) {
		if (keyboardFocus == null) return false;
		ActorEvent event = Pools.obtain(ActorEvent.class);
		event.setType(ActorEvent.Type.keyUp);
		event.setKeyCode(keyCode);
		boolean handled = keyboardFocus.fire(event);
		Pools.free(event);
		return handled;
	}

	/** Applies a key typed event to the actor that has {@link Stage#setKeyboardFocus(Actor) keyboard focus}, if any, and returns
	 * true if the event was processed. */
	public boolean keyTyped (char character) {
		if (keyboardFocus == null) return false;
		ActorEvent event = Pools.obtain(ActorEvent.class);
		event.setType(ActorEvent.Type.keyTyped);
		event.setCharacter(character);
		boolean handled = keyboardFocus.fire(event);
		Pools.free(event);
		return handled;
	}

	/** Adds an actor to the root of the stage.
	 * @see Group#addActor(Actor) */
	public void addActor (Actor actor) {
		root.addActor(actor);
	}

	/** Returns finds the actor in the stage's root. Note this scans potentially all actors in the stage.
	 * @see Group#findActor(String) */
	public Actor findActor (String name) {
		return root.findActor(name);
	}

	/** Returns the root's child actors.
	 * @see Group#getActors() */
	public Array<Actor> getActors () {
		return root.getActors();
	}

	/** Adds a listener to the root.
	 * @see Actor#addListener(EventListener) */
	public boolean addListener (EventListener listener) {
		return root.addListener(listener);
	}

	/** Removes a listener from the root.
	 * @see Actor#removeListener(EventListener) */
	public boolean removeListener (EventListener listener) {
		return root.removeListener(listener);
	}

	/** Adds a capture listener to the root.
	 * @see Actor#addCaptureListener(EventListener) */
	public boolean addCaptureListener (EventListener listener) {
		return root.addCaptureListener(listener);
	}

	/** Adds a capture listener to the root.
	 * @see Actor#addCaptureListener(EventListener, Actor) */
	public boolean addCaptureListener (EventListener listener, Actor contextActor) {
		return root.addCaptureListener(listener, contextActor);
	}

	/** Removes a listener from the root.
	 * @see Actor#removeCaptureListener(EventListener) */
	public boolean removeCaptureListener (EventListener listener) {
		return root.removeCaptureListener(listener);
	}

	/** Removes a listener from the root.
	 * @see Actor#removeCaptureListener(EventListener, Actor) */
	public boolean removeCaptureListener (EventListener listener, Actor contextActor) {
		return root.removeCaptureListener(listener, contextActor);
	}

	/** Clears the stage, removing all actors. */
	public void clear () {
		root.clear();
		unfocusAll();
	}

	/** Returns the {@link Actor} at the specified location in stage coordinates. Hit testing is performed in the order the Actors
	 * were inserted into the Stage, last inserted Actors being tested first. To get stage coordinates from screen coordinates, use
	 * {@link #toStageCoordinates(int, int, Vector2)}.
	 * @return May be null if no actor was hit. */
	public Actor hit (float stageX, float stageY) {
		Vector2 actorCoords = Vector2.tmp;
		Group.toChildCoordinates(root, stageX, stageY, actorCoords);
		return root.hit(actorCoords.x, actorCoords.y);
	}

	/** Converts the given screen coordinates to stage coordinates.
	 * @param out Stores the result. */
	public void toStageCoordinates (int screenX, int screenY, Vector2 out) {
		camera.unproject(Vector3.tmp.set(screenX, screenY, 0));
		out.x = Vector3.tmp.x;
		out.y = Vector3.tmp.y;
	}

	/** Converts the given screen coordinates to stage coordinates. The returned object is {@link Vector2#tmp}, referenecs to it
	 * must not be held. */
	public Vector2 toStageCoordinates (int screenX, int screenY) {
		camera.unproject(Vector3.tmp.set(screenX, screenY, 0));
		Vector2 stageCoords = Vector2.tmp;
		stageCoords.x = Vector3.tmp.x;
		stageCoords.y = Vector3.tmp.y;
		return stageCoords;
	}

	/** Removes the touch, keyboard, and scroll focused actors. */
	public void unfocusAll () {
		scrollFocus = null;
		keyboardFocus = null;
	}

	/** Removes the touch, keyboard, and scroll focus for the specified actor. */
	public void unfocus (Actor actor) {
		if (scrollFocus == actor) scrollFocus = null;
		if (keyboardFocus == actor) keyboardFocus = null;
	}

	/** Sets the actor that will receive key events. */
	public void setKeyboardFocus (Actor actor) {
		this.keyboardFocus = actor;
	}

	/** Gets the actor that will receive key events. */
	public Actor getKeyboardFocus () {
		return keyboardFocus;
	}

	/** Sets the actor that will receive scroll events. */
	public void setScrollFocus (Actor actor) {
		this.scrollFocus = actor;
	}

	/** Gets the actor that will receive scroll events. */
	public Actor getScrollFocus () {
		return scrollFocus;
	}

	/** The width of the stage's viewport.
	 * @see #setViewport(float, float, boolean) */
	public float getWidth () {
		return width;
	}

	/** The height of the stage's viewport.
	 * @see #setViewport(float, float, boolean) */
	public float getHeight () {
		return height;
	}

	public SpriteBatch getSpriteBatch () {
		return batch;
	}

	public Camera getCamera () {
		return camera;
	}

	/** Sets the stage's camera. The camera must be configured properly or {@link #setViewport(float, float, boolean)} can be called
	 * after the camera is set. {@link Stage#draw()} will call {@link Camera#update()} and use the {@link Camera#combined} matrix
	 * for the SpriteBatch {@link SpriteBatch#setProjectionMatrix(com.badlogic.gdx.math.Matrix4) projection matrix}.
	 * @param camera the {@link Camera} */
	public void setCamera (Camera camera) {
		this.camera = camera;
	}

	/** Reurns the root group which holds all actors in the stage. */
	public Group getRoot () {
		return root;
	}

	public void dispose () {
		if (ownsBatch) batch.dispose();
	}

	static private final class TouchFocus {
		Actor actor;
		EventListener listener;
		int pointer;
	}
}
