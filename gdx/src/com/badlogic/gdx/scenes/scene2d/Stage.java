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
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.SnapshotArray;

/** A 2D scenegraph containing hierarchies of {@link Actor actors}. Stage handles the viewport and distributing input events.
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
	private float gutterWidth, gutterHeight;
	private float centerX, centerY;
	private Camera camera;
	private final SpriteBatch batch;
	private final boolean ownsBatch;
	private Group root;
	private final Vector2 stageCoords = new Vector2();
	private Actor[] pointerOverActors = new Actor[20];
	private boolean[] pointerTouched = new boolean[20];
	private int[] pointerScreenX = new int[20];
	private int[] pointerScreenY = new int[20];
	private int mouseScreenX, mouseScreenY;
	private Actor mouseOverActor;
	private Actor keyboardFocus, scrollFocus;
	private SnapshotArray<TouchFocus> touchFocuses = new SnapshotArray(true, 4, TouchFocus.class);

	/** Creates a stage with a {@link #setViewport(float, float, boolean) viewport} equal to the device screen resolution. The stage
	 * will use its own {@link SpriteBatch}. */
	public Stage () {
		this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
	}

	/** Creates a stage with the specified {@link #setViewport(float, float, boolean) viewport}. The stage will use its own
	 * {@link SpriteBatch}, which will be disposed when the stage is disposed. */
	public Stage (float width, float height, boolean stretch) {
		batch = new SpriteBatch();
		ownsBatch = true;
		initialize(width, height, stretch);
	}

	/** Creates a stage with the specified {@link #setViewport(float, float, boolean) viewport} and {@link SpriteBatch}. This can be
	 * used to avoid creating a new SpriteBatch (which can be somewhat slow) if multiple stages are used during an applications
	 * life time.
	 * @param batch Will not be disposed if {@link #dispose()} is called. Handle disposal yourself. */
	public Stage (float width, float height, boolean stretch, SpriteBatch batch) {
		this.batch = batch;
		ownsBatch = false;
		initialize(width, height, stretch);
	}

	private void initialize (float width, float height, boolean stretch) {
		this.width = width;
		this.height = height;

		root = new Group();
		root.setStage(this);

		camera = new OrthographicCamera();
		setViewport(width, height, stretch);
	}

	/** Sets the dimensions of the stage's viewport. The viewport covers the entire screen. If keepAspectRatio is false, the
	 * viewport is simply stretched to the screen resolution, which may distort the aspect ratio. If keepAspectRatio is true, the
	 * viewport is first scaled to fit then the shorter dimension is lengthened to fill the screen, which keeps the aspect ratio
	 * from changing. The {@link #getGutterWidth()} and {@link #getGutterHeight()} provide access to the amount that was
	 * lengthened. */
	public void setViewport (float width, float height, boolean keepAspectRatio) {
		if (keepAspectRatio) {
			float screenWidth = Gdx.graphics.getWidth();
			float screenHeight = Gdx.graphics.getHeight();
			if (screenHeight / screenWidth < height / width) {
				float toScreenSpace = screenHeight / height;
				float toViewportSpace = height / screenHeight;
				float deviceWidth = width * toScreenSpace;
				float lengthen = (screenWidth - deviceWidth) * toViewportSpace;
				this.width = width + lengthen;
				this.height = height;
				gutterWidth = lengthen / 2;
				gutterHeight = 0;
			} else {
				float toScreenSpace = screenWidth / width;
				float toViewportSpace = width / screenWidth;
				float deviceHeight = height * toScreenSpace;
				float lengthen = (screenHeight - deviceHeight) * toViewportSpace;
				this.height = height + lengthen;
				this.width = width;
				gutterWidth = 0;
				gutterHeight = lengthen / 2;
			}
		} else {
			this.width = width;
			this.height = height;
			gutterWidth = 0;
			gutterHeight = 0;
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

	/** Calls {@link #act(float)} with {@link Graphics#getDeltaTime()}. */
	public void act () {
		act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
	}

	/** Calls the {@link Actor#act(float)} method on each actor in the stage. Typically called each frame. This method also fires
	 * enter and exit events.
	 * @param delta Time in seconds since the last frame. */
	public void act (float delta) {
		// Update over actors. Done in act() because actors may change position, which can fire enter/exit without an input event.
		for (int pointer = 0, n = pointerOverActors.length; pointer < n; pointer++) {
			Actor overLast = pointerOverActors[pointer];
			// Check if pointer is gone.
			if (!pointerTouched[pointer]) {
				if (overLast != null) {
					pointerOverActors[pointer] = null;
					screenToStageCoordinates(stageCoords.set(pointerScreenX[pointer], pointerScreenY[pointer]));
					// Exit over last.
					InputEvent event = Pools.obtain(InputEvent.class);
					event.setType(InputEvent.Type.exit);
					event.setStage(this);
					event.setStageX(stageCoords.x);
					event.setStageY(stageCoords.y);
					event.setRelatedActor(overLast);
					event.setPointer(pointer);
					overLast.fire(event);
					Pools.free(event);
				}
				continue;
			}
			// Update over actor for the pointer.
			pointerOverActors[pointer] = fireEnterAndExit(overLast, pointerScreenX[pointer], pointerScreenY[pointer], pointer);
		}
		// Update over actor for the mouse on the desktop.
		ApplicationType type = Gdx.app.getType();
		if (type == ApplicationType.Desktop || type == ApplicationType.Applet || type == ApplicationType.WebGL)
			mouseOverActor = fireEnterAndExit(mouseOverActor, mouseScreenX, mouseScreenY, -1);

		root.act(delta);
	}

	private Actor fireEnterAndExit (Actor overLast, int screenX, int screenY, int pointer) {
		// Find the actor under the point.
		screenToStageCoordinates(stageCoords.set(screenX, screenY));
		Actor over = hit(stageCoords.x, stageCoords.y, true);
		if (over == overLast) return overLast;

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);
		event.setPointer(pointer);
		// Exit overLast.
		if (overLast != null) {
			event.setType(InputEvent.Type.exit);
			event.setRelatedActor(over);
			overLast.fire(event);
		}
		// Enter over.
		if (over != null) {
			event.setType(InputEvent.Type.enter);
			event.setRelatedActor(overLast);
			over.fire(event);
		}
		Pools.free(event);
		return over;
	}

	/** Applies a touch down event to the stage and returns true if an actor in the scene {@link Event#handle() handled} the event. */
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		pointerTouched[pointer] = true;
		pointerScreenX[pointer] = screenX;
		pointerScreenY[pointer] = screenY;

		screenToStageCoordinates(stageCoords.set(screenX, screenY));

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setType(Type.touchDown);
		event.setStage(this);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);
		event.setPointer(pointer);
		event.setButton(button);

		Actor target = hit(stageCoords.x, stageCoords.y, true);
		if (target == null) target = root;

		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a touch moved event to the stage and returns true if an actor in the scene {@link Event#handle() handled} the event.
	 * Only {@link InputListener listeners} that returned true for touchDown will receive this event. */
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		pointerScreenX[pointer] = screenX;
		pointerScreenY[pointer] = screenY;

		if (touchFocuses.size == 0) return false;

		screenToStageCoordinates(stageCoords.set(screenX, screenY));

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setType(Type.touchDragged);
		event.setStage(this);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);
		event.setPointer(pointer);

		SnapshotArray<TouchFocus> touchFocuses = this.touchFocuses;
		TouchFocus[] focuses = touchFocuses.begin();
		for (int i = 0, n = touchFocuses.size; i < n; i++) {
			TouchFocus focus = focuses[i];
			if (focus.pointer != pointer) continue;
			event.setTarget(focus.target);
			event.setListenerActor(focus.listenerActor);
			if (focus.listener.handle(event)) event.handle();
		}
		touchFocuses.end();

		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a touch up event to the stage and returns true if an actor in the scene {@link Event#handle() handled} the event.
	 * Only {@link InputListener listeners} that returned true for touchDown will receive this event. */
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		pointerTouched[pointer] = false;
		pointerScreenX[pointer] = screenX;
		pointerScreenY[pointer] = screenY;

		if (touchFocuses.size == 0) return false;

		screenToStageCoordinates(stageCoords.set(screenX, screenY));

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setType(Type.touchUp);
		event.setStage(this);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);
		event.setPointer(pointer);
		event.setButton(button);

		SnapshotArray<TouchFocus> touchFocuses = this.touchFocuses;
		TouchFocus[] focuses = touchFocuses.begin();
		for (int i = 0, n = touchFocuses.size; i < n; i++) {
			TouchFocus focus = focuses[i];
			if (focus.pointer != pointer || focus.button != button) continue;
			event.setTarget(focus.target);
			event.setListenerActor(focus.listenerActor);
			if (focus.listener.handle(event)) event.handle();
		}
		touchFocuses.end();

		for (int i = touchFocuses.size - 1; i >= 0; i--) {
			TouchFocus focus = touchFocuses.get(i);
			if (focus.pointer != pointer || focus.button != button) continue;
			touchFocuses.removeIndex(i);
			Pools.free(focus);
		}

		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a mouse moved event to the stage and returns true if an actor in the scene {@link Event#handle() handled} the event.
	 * This event only occurs on the desktop. */
	public boolean mouseMoved (int screenX, int screenY) {
		mouseScreenX = screenX;
		mouseScreenY = screenY;

		screenToStageCoordinates(stageCoords.set(screenX, screenY));

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(Type.mouseMoved);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);

		Actor target = hit(stageCoords.x, stageCoords.y, true);
		if (target == null) target = root;

		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a mouse scroll event to the stage and returns true if an actor in the scene {@link Event#handle() handled} the
	 * event. This event only occurs on the desktop. */
	public boolean scrolled (int amount) {
		Actor target = scrollFocus == null ? root : scrollFocus;

		screenToStageCoordinates(stageCoords.set(mouseScreenX, mouseScreenY));

		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(InputEvent.Type.scrolled);
		event.setScrollAmount(amount);
		event.setStageX(stageCoords.x);
		event.setStageY(stageCoords.y);
		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a key down event to the actor that has {@link Stage#setKeyboardFocus(Actor) keyboard focus}, if any, and returns
	 * true if the event was {@link Event#handle() handled}. */
	public boolean keyDown (int keyCode) {
		Actor target = keyboardFocus == null ? root : keyboardFocus;
		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(InputEvent.Type.keyDown);
		event.setKeyCode(keyCode);
		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a key up event to the actor that has {@link Stage#setKeyboardFocus(Actor) keyboard focus}, if any, and returns true
	 * if the event was {@link Event#handle() handled}. */
	public boolean keyUp (int keyCode) {
		Actor target = keyboardFocus == null ? root : keyboardFocus;
		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(InputEvent.Type.keyUp);
		event.setKeyCode(keyCode);
		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Applies a key typed event to the actor that has {@link Stage#setKeyboardFocus(Actor) keyboard focus}, if any, and returns
	 * true if the event was {@link Event#handle() handled}. */
	public boolean keyTyped (char character) {
		Actor target = keyboardFocus == null ? root : keyboardFocus;
		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(InputEvent.Type.keyTyped);
		event.setCharacter(character);
		target.fire(event);
		boolean handled = event.isHandled();
		Pools.free(event);
		return handled;
	}

	/** Adds the listener to be notified for all touchDragged and touchUp events for the specified pointer and button. The actor
	 * will be used as the {@link Event#getListenerActor() listener actor} and {@link Event#getTarget() target}. */
	public void addTouchFocus (EventListener listener, Actor listenerActor, Actor target, int pointer, int button) {
		TouchFocus focus = Pools.obtain(TouchFocus.class);
		focus.listenerActor = listenerActor;
		focus.target = target;
		focus.listener = listener;
		focus.pointer = pointer;
		focus.button = button;
		touchFocuses.add(focus);
	}

	/** Removes the listener from being notified for all touchDragged and touchUp events for the specified pointer and button. Note
	 * the listener may never receive a touchUp event if this method is used. */
	public void removeTouchFocus (EventListener listener, Actor listenerActor, Actor target, int pointer, int button) {
		SnapshotArray<TouchFocus> touchFocuses = this.touchFocuses;
		for (int i = touchFocuses.size - 1; i >= 0; i--) {
			TouchFocus focus = touchFocuses.get(i);
			if (focus.listener == listener && focus.listenerActor == listenerActor && focus.target == target
				&& focus.pointer == pointer && focus.button == button) {
				touchFocuses.removeIndex(i);
				Pools.free(focus);
			}
		}
	}

	/** Sends a touchUp event to all listeners that are registered to receive touchDragged and touchUp events and removes their
	 * touch focus. The location of the touchUp is {@link Integer#MIN_VALUE}. This method removes all touch focus listeners, but
	 * sends a touchUp event so that the state of the listeners remains consistent (listeners typically expect to receive touchUp
	 * eventually). */
	public void cancelTouchFocus () {
		cancelTouchFocus(null, null);
	}

	/** Cancels touch focus for all listeners except the specified listener.
	 * @see #cancelTouchFocus() */
	public void cancelTouchFocus (EventListener listener, Actor actor) {
		InputEvent event = Pools.obtain(InputEvent.class);
		event.setStage(this);
		event.setType(InputEvent.Type.touchUp);
		event.setStageX(Integer.MIN_VALUE);
		event.setStageY(Integer.MIN_VALUE);

		SnapshotArray<TouchFocus> touchFocuses = this.touchFocuses;
		for (int i = touchFocuses.size - 1; i >= 0; i--) {
			TouchFocus focus = touchFocuses.get(i);
			if (focus.listener == listener && focus.listenerActor == actor) continue;
			event.setTarget(focus.target);
			event.setListenerActor(focus.listenerActor);
			event.setPointer(focus.pointer);
			event.setButton(focus.button);
			touchFocuses.removeIndex(i);
			focus.listener.handle(event);
			// Cannot return TouchFocus to the pool, as it may still be in use (eg if cancelTouchFocus is called from touchDragged).
		}

		Pools.free(event);
	}

	/** Adds an actor to the root of the stage.
	 * @see Group#addActor(Actor) */
	public void addActor (Actor actor) {
		root.addActor(actor);
	}

	/** Adds an action to the root of the stage.
	 * @see Group#addAction(Action) */
	public void addAction (Action action) {
		root.addAction(action);
	}

	/** Returns the root's child actors.
	 * @see Group#getChildren() */
	public Array<Actor> getActors () {
		return root.getChildren();
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

	/** Removes a listener from the root.
	 * @see Actor#removeCaptureListener(EventListener) */
	public boolean removeCaptureListener (EventListener listener) {
		return root.removeCaptureListener(listener);
	}

	/** Clears the stage, removing all actors. */
	public void clear () {
		unfocusAll();
		root.clear();
	}

	/** Removes the touch, keyboard, and scroll focused actors. */
	public void unfocusAll () {
		scrollFocus = null;
		keyboardFocus = null;
		cancelTouchFocus();
	}

	/** Removes the touch, keyboard, and scroll focus for the specified actor. */
	public void unfocus (Actor actor) {
		if (scrollFocus == actor) scrollFocus = null;
		if (keyboardFocus == actor) keyboardFocus = null;
	}

	/** Sets the actor that will receive key events.
	 * @param actor May be null. */
	public void setKeyboardFocus (Actor actor) {
		if (keyboardFocus == actor) return;
		FocusEvent event = Pools.obtain(FocusEvent.class);
		event.setStage(this);
		event.setType(FocusEvent.Type.keyboard);
		if (keyboardFocus != null) {
			event.setFocused(false);
			keyboardFocus.fire(event);
		}
		keyboardFocus = actor;
		if (keyboardFocus != null) {
			event.setFocused(true);
			keyboardFocus.fire(event);
		}
		Pools.free(event);
	}

	/** Gets the actor that will receive key events.
	 * @return May be null. */
	public Actor getKeyboardFocus () {
		return keyboardFocus;
	}

	/** Sets the actor that will receive scroll events.
	 * @param actor May be null. */
	public void setScrollFocus (Actor actor) {
		if (scrollFocus == actor) return;
		FocusEvent event = Pools.obtain(FocusEvent.class);
		event.setStage(this);
		event.setType(FocusEvent.Type.scroll);
		if (scrollFocus != null) {
			event.setFocused(false);
			scrollFocus.fire(event);
		}
		scrollFocus = actor;
		if (scrollFocus != null) {
			event.setFocused(true);
			scrollFocus.fire(event);
		}
		Pools.free(event);
	}

	/** Gets the actor that will receive scroll events.
	 * @return May be null. */
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

	/** Half the amount in the x direction that the stage's viewport was lengthened to fill the screen.
	 * @see #setViewport(float, float, boolean) */
	public float getGutterWidth () {
		return gutterWidth;
	}

	/** Half the amount in the y direction that the stage's viewport was lengthened to fill the screen.
	 * @see #setViewport(float, float, boolean) */
	public float getGutterHeight () {
		return gutterHeight;
	}

	public SpriteBatch getSpriteBatch () {
		return batch;
	}

	public Camera getCamera () {
		return camera;
	}

	/** Sets the stage's camera. The camera must be configured properly or {@link #setViewport(float, float, boolean)} can be called
	 * after the camera is set. {@link Stage#draw()} will call {@link Camera#update()} and use the {@link Camera#combined} matrix
	 * for the SpriteBatch {@link SpriteBatch#setProjectionMatrix(com.badlogic.gdx.math.Matrix4) projection matrix}. */
	public void setCamera (Camera camera) {
		this.camera = camera;
	}

	/** Returns the root group which holds all actors in the stage. */
	public Group getRoot () {
		return root;
	}

	/** Returns the {@link Actor} at the specified location in stage coordinates. Hit testing is performed in the order the actors
	 * were inserted into the stage, last inserted actors being tested first. To get stage coordinates from screen coordinates, use
	 * {@link #screenToStageCoordinates(Vector2)}.
	 * @param touchable If true, the hit detection will respect the {@link Actor#setTouchable(Touchable) touchability}.
	 * @return May be null if no actor was hit. */
	public Actor hit (float stageX, float stageY, boolean touchable) {
		Vector2 actorCoords = Vector2.tmp;
		root.parentToLocalCoordinates(actorCoords.set(stageX, stageY));
		return root.hit(actorCoords.x, actorCoords.y, touchable);
	}

	/** Transforms the screen coordinates to stage coordinates.
	 * @param screenCoords Stores the result. */
	public Vector2 screenToStageCoordinates (Vector2 screenCoords) {
		camera.unproject(Vector3.tmp.set(screenCoords.x, screenCoords.y, 0));
		screenCoords.x = Vector3.tmp.x;
		screenCoords.y = Vector3.tmp.y;
		return screenCoords;
	}

	/** Transforms the stage coordinates to screen coordinates. */
	public Vector2 stageToScreenCoordinates (Vector2 stageCoords) {
		Vector3.tmp.set(stageCoords.x, stageCoords.y, 0);
		camera.project(Vector3.tmp);
		stageCoords.x = Vector3.tmp.x;
		stageCoords.y = Vector3.tmp.y;
		return stageCoords;
	}

	/** Transforms the coordinates to screen coordinates. The coordinates can be anywhere in the stage since the transform matrix
	 * describes how to convert them. The transform matrix is typically obtained from {@link SpriteBatch#getTransformMatrix()}. */
	public Vector2 toScreenCoordinates (Vector2 coords, Matrix4 transformMatrix) {
		ScissorStack.toWindowCoordinates(camera, transformMatrix, coords);
		return coords;
	}

	public void dispose () {
		if (ownsBatch) batch.dispose();
	}

	/** Internal class for managing touch focus. Public only for GWT.
	 * @author Nathan Sweet */
	public static final class TouchFocus implements Poolable {
		EventListener listener;
		Actor listenerActor, target;
		int pointer, button;

		public void reset () {
			listenerActor = null;
			listener = null;
		}
	}
}
