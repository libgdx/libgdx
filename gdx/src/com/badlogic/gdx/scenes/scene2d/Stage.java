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

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/** A stage is a container for {@link Actor}s and handles distributing touch events, animating actors and asking them to render
 * themselves. A stage is 2D scenegraph with hierarchies of Actors.
 * <p>
 * A stage fills the whole screen. It has a width and height given in device independent pixels. It has a {@link Camera} that maps
 * this viewport to the given real screen resolution. If the stretched attribute is set to true then the viewport is enforced no
 * matter the difference in aspect ratio between the stage object and the screen dimensions. In case stretch is disabled then the
 * viewport is extended in the bigger screen dimensions.
 * <p>
 * Actors have a z-order which is equal to the order they were inserted into the stage. Actors inserted later will be drawn on top
 * of actors added earlier. Touch events that will get distributed to later actors first.
 * <p>
 * Actors can be focused. When your game pauses and resumes be sure to call the {@link Stage#unfocusAll()} method so that the
 * focus states get reset for each pointer id. You also have to make sure that the actors that were focused reset their state if
 * the depend on being focused, e.g. wait for a touch up event. An easier way to tackle this is to recreate the stage if possible.
 * @author mzechner */
public class Stage extends InputAdapter implements Disposable {
	protected float width;
	protected float height;
	protected float centerX;
	protected float centerY;
	protected boolean stretch;

	protected final Group root;

	protected final SpriteBatch batch;
	protected Camera camera;

	/** <p>
	 * Constructs a new Stage object with the given dimensions. If the device resolution does not equal the Stage objects
	 * dimensions the stage object will setup a projection matrix to guarantee a fixed coordinate system. If stretch is disabled
	 * then the bigger dimension of the Stage will be increased to accomodate the actual device resolution.
	 * </p>
	 * 
	 * @param width the width of the viewport
	 * @param height the height of the viewport
	 * @param stretch whether to stretch the viewport to the real device resolution */
	public Stage (float width, float height, boolean stretch) {
		this.width = width;
		this.height = height;
		this.stretch = stretch;
		this.root = new Group("root");
		this.batch = new SpriteBatch();
		this.camera = new OrthographicCamera();
		setViewport(width, height, stretch);
	}

	/** Sets the viewport dimensions in device independent pixels. If stretch is false and the viewport aspect ratio is not equal to
	 * the device ratio then the bigger dimension of the viewport will be extended (device independent pixels stay quardatic
	 * instead of getting stretched).
	 * 
	 * @param width thew width of the viewport in device independent pixels
	 * @param height the height of the viewport in device independent pixels
	 * @param stretch whether to stretch the viewport or not */
	public void setViewport (float width, float height, boolean stretch) {
		if (!stretch) {
			if (width > height && width / (float)Gdx.graphics.getWidth() <= height / (float)Gdx.graphics.getHeight()) {
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

		this.stretch = stretch;
		centerX = this.width / 2;
		centerY = this.height / 2;

		camera.position.set(centerX, centerY, 0);
		camera.viewportWidth = this.width;
		camera.viewportHeight = this.height;
	}

	/** 8
	 * @return the width of the stage in dips */
	public float width () {
		return width;
	}

	/** @return the height of the stage in dips */
	public float height () {
		return height;
	}

	/** @return the x-coordinate of the left edge of the stage in dips */
	public int left () {
		return 0;
	}

	/** @return the x-coordinate of the right edge of the stage in dips */
	public float right () {
		return width - 1;
	}

	/** @return the y-coordinate of the top edge of the stage in dips */
	public float top () {
		return height - 1;
	}

	/** @return the y-coordinate of the bottom edge of the stage in dips */
	public float bottom () {
		return 0;
	}

	/** @return the center x-coordinate of the stage in dips */
	public float centerX () {
		return centerX;
	}

	/** @return the center y-coordinate of the stage in dips */
	public float centerY () {
		return centerY;
	}

	/** @return whether the stage is stretched */
	public boolean isStretched () {
		return stretch;
	}

	/** Finds the {@link Actor} with the given name in the stage hierarchy.
	 * @param name
	 * @return the Actor or null if it couldn't be found. */
	public Actor findActor (String name) {
		return root.findActor(name);
	}

	/** @return all top level {@link Actor}s */
	public List<Actor> getActors () {
		return root.getActors();
	}

	/** @return all top level {@link Group}s */
	public List<Group> getGroups () {
		return root.getGroups();
	}

	final Vector2 point = new Vector2();
	final Vector2 coords = new Vector2();

	/** Call this to distribute a touch down event to the stage.
	 * @param x the x coordinate of the touch in screen coordinates
	 * @param y the y coordinate of the touch in screen coordinates
	 * @param pointer the pointer index
	 * @param button the button that's been pressed
	 * @return whether an {@link Actor} in the scene processed the event or not */
	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		root.keyboardFocus(null);
		toStageCoordinates(x, y, coords);
		Group.toChildCoordinates(root, coords.x, coords.y, point);
		return root.touchDown(point.x, point.y, pointer);
	}

	/** Call this to distribute a touch Up event to the stage.
	 * 
	 * @param x the x coordinate of the touch in screen coordinates
	 * @param y the y coordinate of the touch in screen coordinates
	 * @param pointer the pointer index
	 * @return whether an {@link Actor} in the scene processed the event or not */
	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		Actor actor = root.focusedActor[pointer];
		if (actor == null) return false;
		toStageCoordinates(x, y, coords);
		Group.toChildCoordinates(root, coords.x, coords.y, point);
		root.touchUp(point.x, point.y, pointer);
		return true;
	}

	/** Call this to distribute a touch dragged event to the stage.
	 * @param x the x coordinate of the touch in screen coordinates
	 * @param y the y coordinate of the touch in screen coordinates
	 * @param pointer the pointer index
	 * @return whether an {@link Actor} in the scene processed the event or not */
	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		boolean foundFocusedActor = false;
		for (int i = 0, n = root.focusedActor.length; i < n; i++) {
			if (root.focusedActor[i] != null) {
				foundFocusedActor = true;
				break;
			}
		}
		if (!foundFocusedActor) return false;
		toStageCoordinates(x, y, coords);
		Group.toChildCoordinates(root, coords.x, coords.y, point);
		root.touchDragged(point.x, point.y, pointer);
		return true;
	}

	/** Call this to distribute a touch moved event to the stage. This event will only ever appear on the desktop.
	 * @param x the x coordinate of the touch in screen coordinates
	 * @param y the y coordinate of the touch in screen coordinates
	 * @return whether an {@link Actor} in the scene processed the event or not */
	@Override
	public boolean touchMoved (int x, int y) {
		toStageCoordinates(x, y, coords);
		Group.toChildCoordinates(root, coords.x, coords.y, point);
		return root.touchMoved(point.x, point.y);
	}

	/** Call this to distribute a mouse scroll event to the stage. This event will only ever appear on the desktop.
	 * @param amount the scroll amount.
	 * @return whether an {@link Actor} in the scene processed the event or not. */
	@Override
	public boolean scrolled (int amount) {
		return root.scrolled(amount);
	}

	/** Called when a key was pressed
	 * 
	 * @param keycode one of the constants in {@link Keys}
	 * @return whether the input was processed */
	@Override
	public boolean keyDown (int keycode) {
		return root.keyDown(keycode);
	}

	/** Called when a key was released
	 * 
	 * @param keycode one of the constants in {@link Keys}
	 * @return whether the input was processed */
	@Override
	public boolean keyUp (int keycode) {
		return root.keyUp(keycode);
	}

	/** Called when a key was typed
	 * 
	 * @param character The character
	 * @return whether the input was processed */
	@Override
	public boolean keyTyped (char character) {
		return root.keyTyped(character);
	}

	/** Calls the {@link Actor#act(float)} method of all contained Actors. This will advance any {@link Action}s active for an
	 * Actor.
	 * @param delta the delta time in seconds since the last invocation */
	public void act (float delta) {
		root.act(delta);
	}

	/** Renders the stage */
	public void draw () {
		camera.update();
		if (!root.visible) return;
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		root.draw(batch, 1);
		batch.end();
	}

	/** Disposes the stage */
	public void dispose () {
		batch.dispose();
	}

	/** Adds an {@link Actor} to this stage
	 * @param actor the Actor */
	public void addActor (Actor actor) {
		root.addActor(actor);
	}

	/** @return the Stage graph as a silly string */
	public String graphToString () {
		StringBuilder buffer = new StringBuilder();
		graphToString(buffer, root, 0);
		return buffer.toString();
	}

	private void graphToString (StringBuilder buffer, Actor actor, int level) {
		for (int i = 0; i < level; i++)
			buffer.append(' ');

		buffer.append(actor);
		buffer.append("\n");

		if (actor instanceof Group) {
			Group group = (Group)actor;
			for (int i = 0; i < group.getActors().size(); i++)
				graphToString(buffer, group.getActors().get(i), level + 1);
		}
	}

	/** @return the root {@link Group} of this Stage. */
	public Group getRoot () {
		return root;
	}

	/** @return the {@link SpriteBatch} offers its {@link Actor}s for rendering. */
	public SpriteBatch getSpriteBatch () {
		return batch;
	}

	/** @return the {@link Camera} of this stage. */
	public Camera getCamera () {
		return camera;
	}

	/** Sets the {@link Camera} this stage uses. You are responsible for setting it up properly! The {@link Stage#draw()} will call
	 * the Camera's update() method and use it's combined matrix as the projection matrix for the SpriteBatch.
	 * @param camera the {@link Camera} */
	public void setCamera (Camera camera) {
		this.camera = camera;
	}

	/** @return the {@link Actor} last hit by a touch event. */
	public Actor getLastTouchedChild () {
		return root.lastTouchedChild;
	}

	/** Returns the {@link Actor} intersecting with the point (x,y) in stage coordinates. Hit testing is performed in the order the
	 * Actors were inserted into the Stage, last inserted Actors being tested first. To get stage coordinates from screen
	 * coordinates use {@link #toStageCoordinates(int, int, Vector2)}.
	 * 
	 * @param x the x-coordinate in stage coordinates
	 * @param y the y-coordinate in stage coordinates
	 * @return the hit Actor or null */
	public Actor hit (float x, float y) {
		Group.toChildCoordinates(root, x, y, point);
		return root.hit(point.x, point.y);
	}

	final Vector3 tmp = new Vector3();

	/** Transforms the given screen coordinates to stage coordinates
	 * @param x the x-coordinate in screen coordinates
	 * @param y the y-coordinate in screen coordinates
	 * @param out the output {@link Vector2}. */
	public void toStageCoordinates (int x, int y, Vector2 out) {
		camera.unproject(tmp.set(x, y, 0));
		out.x = tmp.x;
		out.y = tmp.y;
	}

	/** Clears this stage, removing all {@link Actor}s and {@link Group}s. */
	public void clear () {
		root.clear();
	}

	/** Removes the given {@link Actor} from the stage by trying to find it recursively in the scenegraph.
	 * @param actor the actor */
	public void removeActor (Actor actor) {
		root.removeActorRecursive(actor);
	}

	/** Unfocues all {@link Actor} instance currently focused. You should call this in case your app resumes to clear up any pressed
	 * states. Make sure the Actors forget their states as well! */
	public void unfocusAll () {
		root.unfocusAll();
	}
}
