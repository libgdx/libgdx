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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pools;

/** 2D scene graph node. An actor has a position, rectangular size, origin, scale, rotation, and color. The position corresponds to
 * the unrotated, unscaled bottom left corner of the actor. The position is relative to the actor's parent. The origin is relative
 * to the position and is used for scale and rotation.
 * <p>
 * An actor also has a list of actions that can manipulate the actor over time, and a list of listeners that are notified of
 * events the actor receives.
 * @author mzechner
 * @author Nathan Sweet */
public class Actor {
	private Stage stage;
	private Group parent;
	private final DelayedRemovalArray<EventListener> listeners = new DelayedRemovalArray(0);
	private final DelayedRemovalArray<EventListener> captureListeners = new DelayedRemovalArray(0);
	private final Array<Action> actions = new Array(0);

	private String name;
	private Touchable touchable = Touchable.enabled;
	private boolean visible = true;
	private float x, y;
	private float width, height;
	private float originX, originY;
	private float scaleX = 1, scaleY = 1;
	private float rotation;
	private final Color color = new Color(1, 1, 1, 1);

	/** Draws the actor. The SpriteBatch is configured to draw in the parent's coordinate system.
	 * {@link SpriteBatch#draw(com.badlogic.gdx.graphics.g2d.TextureRegion, float, float, float, float, float, float, float, float, float)
	 * This draw method} is convenient to draw a rotated and scaled TextureRegion. {@link SpriteBatch#begin()} has already been
	 * called on the SpriteBatch. If {@link SpriteBatch#end()} is called to draw without the SpriteBatch then
	 * {@link SpriteBatch#begin()} must be called before the method returns.
	 * <p>
	 * The default implementation does nothing.
	 * @param parentAlpha Should be multipied with the actor's alpha, allowing a parent's alpha to affect all children. */
	public void draw (SpriteBatch batch, float parentAlpha) {
	}

	/** Updates the actor based on time. Typically this is called each frame by {@link Stage#act(float)}.
	 * <p>
	 * The default implementation calls {@link Action#act(float)} on each action and removes actions that are complete.
	 * @param delta Time in seconds since the last frame. */
	public void act (float delta) {
		for (int i = 0, n = actions.size; i < n; i++) {
			Action action = actions.get(i);
			if (action.act(delta)) {
				actions.removeIndex(i);
				action.setActor(null);
				i--;
				n--;
			}
		}
	}

	/** Sets this actor as the event {@link Event#setTarget(Actor) target} and propagates the event to this actor and ancestor
	 * actors as necessary. If this actor is not in the stage, the stage must be set before calling this method.
	 * <p>
	 * Events are fired in 2 phases. The first phase notifies listeners on each actor starting at the root and propagating downward
	 * to (and including) this actor. The second phase notifes listeners on each actor starting at this actor and, if
	 * {@link Event#getBubbles()} is true, propagating upward to the root. If the event is {@link Event#stop() stopped} at any time,
	 * it will not propagate to the next actor.
	 * @return true of the event was {@link Event#cancel() cancelled}. */
	public boolean fire (Event event) {
		if (event.getStage() == null) event.setStage(getStage());
		event.setTarget(this);

		// Collect ancestors so event propagation is unaffected by hierarchy changes.
		Array<Group> ancestors = Pools.obtain(Array.class);
		Group parent = getParent();
		while (parent != null) {
			ancestors.add(parent);
			parent = parent.getParent();
		}

		try {
			// Notify all parent capture listeners, starting at the root. Ancestors may stop an event before children receive it.
			for (int i = ancestors.size - 1; i >= 0; i--) {
				Group currentTarget = ancestors.get(i);
				currentTarget.notify(event, true);
				if (event.isStopped()) return event.isCancelled();
			}

			// Notify the target capture listeners.
			notify(event, true);
			if (event.isStopped()) return event.isCancelled();

			// Notify the target listeners.
			notify(event, false);
			if (!event.getBubbles()) return event.isCancelled();
			if (event.isStopped()) return event.isCancelled();

			// Notify all parent listeners, starting at the target. Children may stop an event before ancestors receive it.
			for (int i = 0, n = ancestors.size; i < n; i++) {
				ancestors.get(i).notify(event, false);
				if (event.isStopped()) return event.isCancelled();
			}

			return event.isCancelled();
		} finally {
			ancestors.clear();
			Pools.free(ancestors);
		}
	}

	/** Notifies this actor's listeners of the event. The event is not propagated to any parents. Before notifying the listeners,
	 * this actor is set as the {@link Event#getListenerActor() listener actor}. The event {@link Event#setTarget(Actor) target}
	 * must be set before calling this method. If this actor is not in the stage, the stage must be set before calling this method.
	 * @param capture If true, the capture listeners will be notified instead of the regular listeners.
	 * @return true of the event was {@link Event#cancel() cancelled}. */
	public boolean notify (Event event, boolean capture) {
		if (event.getTarget() == null) throw new IllegalArgumentException("The event target cannot be null.");

		DelayedRemovalArray<EventListener> listeners = capture ? captureListeners : this.listeners;
		if (listeners.size == 0) return event.isCancelled();

		event.setListenerActor(this);
		event.setCapture(capture);
		if (event.getStage() == null) event.setStage(stage);

		listeners.begin();
		for (int i = 0, n = listeners.size; i < n; i++) {
			EventListener listener = listeners.get(i);
			if (listener.handle(event)) {
				event.handle();
				if (event instanceof InputEvent) {
					InputEvent inputEvent = (InputEvent)event;
					if (inputEvent.getType() == Type.touchDown) {
						event.getStage().addTouchFocus(listener, this, inputEvent.getTarget(), inputEvent.getPointer(),
							inputEvent.getButton());
					}
				}
			}
		}
		listeners.end();

		return event.isCancelled();
	}

	/** Returns the deepest actor that contains the specified point and is {@link #getTouchable() touchable} and
	 * {@link #isVisible() visible}, or null if no actor was hit. The point is specified in the actor's local coordinate system (0,0
	 * is the bottom left of the actor and width,height is the upper right).
	 * <p>
	 * This method is used to delegate touchDown events. If this method returns null, touchDown will not occur.
	 * <p>
	 * The default implementation returns this actor if the point is within this actor's bounds. */
	public Actor hit (float x, float y) {
		return touchable == Touchable.enabled && x >= 0 && x < width && y >= 0 && y < height ? this : null;
	}

	/** Removes this actor from its parent, if it has a parent.
	 * @see Group#removeActor(Actor) */
	public boolean remove () {
		if (parent != null) return parent.removeActor(this);
		return false;
	}

	public boolean addListener (EventListener listener) {
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
			return true;
		}
		return false;
	}

	public boolean removeListener (EventListener listener) {
		return listeners.removeValue(listener, true);
	}

	public Array<EventListener> getListeners () {
		return listeners;
	}

	/** Adds a listener that is only notified during the capture phase.
	 * @see #fire(Event) */
	public boolean addCaptureListener (EventListener listener) {
		if (!captureListeners.contains(listener, true)) captureListeners.add(listener);
		return true;
	}

	public boolean removeCaptureListener (EventListener listener) {
		return captureListeners.removeValue(listener, true);
	}

	public Array<EventListener> getCaptureListeners () {
		return captureListeners;
	}

	public void addAction (Action action) {
		action.setActor(this);
		actions.add(action);
	}

	public void removeAction (Action action) {
		if (actions.removeValue(action, true)) action.setActor(null);
	}

	public Array<Action> getActions () {
		return actions;
	}

	/** Removes all actions on this actor. */
	public void clearActions () {
		for (int i = actions.size - 1; i >= 0; i--)
			actions.get(i).setActor(null);
		actions.clear();
	}

	/** Returns the stage that this actor is currently in, or null if not in a stage. */
	public Stage getStage () {
		return stage;
	}

	/** Called by the framework when this actor or any parent is added to a group that is in the stage.
	 * @param stage May be null if the actor or any parent is no longer in a stage. */
	protected void setStage (Stage stage) {
		this.stage = stage;
	}

	/** Returns true if the specified actor is this actor or a descendant of this actor. */
	public boolean isDescendant (Actor actor) {
		if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
		Actor parent = this;
		while (true) {
			if (parent == null) return false;
			if (parent == actor) return true;
			parent = parent.getParent();
		}
	}

	/** Returns true if the specified actor is this actor or an ancestor of this actor. */
	public boolean isAscendant (Actor actor) {
		if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
		while (true) {
			if (actor == null) return false;
			if (actor == this) return true;
			actor = actor.getParent();
		}
	}

	/** Returns true if the actor's parent is not null. */
	public boolean hasParent () {
		return parent != null;
	}

	/** Returns the parent actor, or null if not in a stage. */
	public Group getParent () {
		return parent;
	}

	/** Called by the framework when an actor is added to a group.
	 * @param parent May be null if the actor has been removed from the parent. */
	protected void setParent (Group parent) {
		this.parent = parent;
	}

	public Touchable getTouchable () {
		return touchable;
	}

	/** Determines how touch events are distributed to this actor. Default is {@link Touchable#enabled}. */
	public void setTouchable (Touchable touchable) {
		this.touchable = touchable;
	}

	public boolean isVisible () {
		return visible;
	}

	/** If false, the actor will not be drawn and will not receive touch events. Default is true. */
	public void setVisible (boolean visible) {
		this.visible = visible;
	}

	public float getX () {
		return x;
	}

	public void setX (float x) {
		this.x = x;
	}

	public float getY () {
		return y;
	}

	public void setY (float y) {
		this.y = y;
	}

	/** Sets the x and y. */
	public void setPosition (float x, float y) {
		setX(x);
		setY(y);
	}

	public void translate (float x, float y) {
		setX(this.x + x);
		setY(this.y + y);
	}

	public float getWidth () {
		return width;
	}

	public void setWidth (float width) {
		this.width = width;
	}

	public float getHeight () {
		return height;
	}

	public void setHeight (float height) {
		this.height = height;
	}

	/** Returns y plus height. */
	public float getTop () {
		return getY() + getHeight();
	}

	/** Returns x plus width. */
	public float getRight () {
		return getX() + getWidth();
	}

	/** Sets the width and height. */
	public void setSize (float width, float height) {
		setWidth(width);
		setHeight(height);
	}

	/** Adds the specified size to the current size. */
	public void size (float size) {
		setWidth(width + size);
		setHeight(height + size);
	}

	/** Adds the specified size to the current size. */
	public void size (float width, float height) {
		setWidth(this.width + width);
		setHeight(this.height + height);
	}

	/** Set bounds the x, y, width, and height. */
	public void setBounds (float x, float y, float width, float height) {
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
	}

	public float getOriginX () {
		return originX;
	}

	public void setOriginX (float originX) {
		this.originX = originX;
	}

	public float getOriginY () {
		return originY;
	}

	public void setOriginY (float originY) {
		this.originY = originY;
	}

	/** Sets the originx and originy. */
	public void setOrigin (float originX, float originY) {
		setOriginX(originX);
		setOriginY(originY);
	}

	public float getScaleX () {
		return scaleX;
	}

	public void setScaleX (float scaleX) {
		this.scaleX = scaleX;
	}

	public float getScaleY () {
		return scaleY;
	}

	public void setScaleY (float scaleY) {
		this.scaleY = scaleY;
	}

	/** Sets the scalex and scaley. */
	public void setScale (float scale) {
		setScaleX(scale);
		setScaleY(scale);
	}

	/** Sets the scalex and scaley. */
	public void setScale (float scaleX, float scaleY) {
		setScaleX(scaleX);
		setScaleY(scaleY);
	}

	/** Adds the specified scale to the current scale. */
	public void scale (float scale) {
		setScaleX(scaleX + scale);
		setScaleY(scaleY + scale);
	}

	/** Adds the specified scale to the current scale. */
	public void scale (float scaleX, float scaleY) {
		setScaleX(this.scaleX + scaleX);
		setScaleY(this.scaleY + scaleY);
	}

	public float getRotation () {
		return rotation;
	}

	public void setRotation (float degrees) {
		this.rotation = degrees;
	}

	/** Adds the specified rotation to the current rotation. */
	public void rotate (float amountInDegrees) {
		setRotation(rotation + amountInDegrees);
	}

	public void setColor (Color color) {
		this.color.set(color);
	}

	public void setColor (float r, float g, float b, float a) {
		color.set(r, g, b, a);
	}

	/** Returns the actor's color, which is mutable. */
	public Color getColor () {
		return color;
	}

	public String getName () {
		return name;
	}

	/** Sets a name for easier identification of the actor in application code.
	 * @see Group#findActor(String) */
	public void setName (String name) {
		this.name = name;
	}

	/** Changes the z-order for this actor so it is in front of all siblings. */
	public void toFront () {
		setZIndex(Integer.MAX_VALUE);
	}

	/** Changes the z-order for this actor so it is in back of all siblings. */
	public void toBack () {
		setZIndex(0);
	}

	/** Sets the z-index of this actor. The z-index is the index into the parent's {@link Group#getChildren() children}, where a
	 * lower index is below a higher index. Setting a z-index higher than the number of children will move the child to the front.
	 * Setting a z-index less than zero is invalid. */
	public void setZIndex (int index) {
		if (index < 0) throw new IllegalArgumentException("ZIndex cannot be < 0.");
		Group parent = getParent();
		if (parent == null) return;
		Array<Actor> children = parent.getChildren();
		if (children.size == 1) return;
		if (!children.removeValue(this, true)) return;
		if (index >= children.size)
			children.add(this);
		else
			children.insert(index, this);
	}

	/** Returns the z-index of this actor.
	 * @see #setZIndex(int) */
	public int getZIndex () {
		Group parent = getParent();
		if (parent == null) return -1;
		return parent.getChildren().indexOf(this, true);
	}

	/** Transforms the specified point in the stage's coordinates to the actor's local coordinate system. */
	public void stageToLocalCoordinates (Vector2 stageCoords) {
		if (parent == null) return;
		parent.stageToLocalCoordinates(stageCoords);
		parentToLocalCoordinates(stageCoords);
	}

	/** Transforms the specified point in the actor's coordinates to be in the stage's coordinates. Note this method will ONLY work
	 * for screen aligned, unrotated, unscaled actors! */
	public void localToStageCoordinates (Vector2 localCoords) {
		Actor actor = this;
		while (actor != null) {
			if (actor.getRotation() != 0 || actor.getScaleX() != 1 || actor.getScaleY() != 1)
				throw new GdxRuntimeException("Only unrotated and unscaled actors may use this method.");
			localCoords.x += actor.getX();
			localCoords.y += actor.getY();
			actor = actor.getParent();
		}
	}

	/** Converts the coordinates given in the parent's coordinate system to this actor's coordinate system. */
	public void parentToLocalCoordinates (Vector2 parentCoords) {
		final float rotation = getRotation();
		final float scaleX = getScaleX();
		final float scaleY = getScaleY();
		final float childX = getX();
		final float childY = getY();

		if (rotation == 0) {
			if (scaleX == 1 && scaleY == 1) {
				parentCoords.x -= childX;
				parentCoords.y -= childY;
			} else {
				final float originX = getOriginX();
				final float originY = getOriginY();
				if (originX == 0 && originY == 0) {
					parentCoords.x = (parentCoords.x - childX) / scaleX;
					parentCoords.y = (parentCoords.y - childY) / scaleY;
				} else {
					parentCoords.x = (parentCoords.x - childX - originX) / scaleX + originX;
					parentCoords.y = (parentCoords.y - childY - originY) / scaleY + originY;
				}
			}
		} else {
			final float cos = (float)Math.cos(rotation * MathUtils.degreesToRadians);
			final float sin = (float)Math.sin(rotation * MathUtils.degreesToRadians);

			final float originX = getOriginX();
			final float originY = getOriginY();

			if (scaleX == 1 && scaleY == 1) {
				if (originX == 0 && originY == 0) {
					float tox = parentCoords.x - childX;
					float toy = parentCoords.y - childY;

					parentCoords.x = tox * cos + toy * sin;
					parentCoords.y = tox * -sin + toy * cos;
				} else {
					final float worldOriginX = childX + originX;
					final float worldOriginY = childY + originY;
					final float fx = -originX;
					final float fy = -originY;

					final float x1 = cos * fx - sin * fy + worldOriginX;
					final float y1 = sin * fx + cos * fy + worldOriginY;

					final float tox = parentCoords.x - x1;
					final float toy = parentCoords.y - y1;

					parentCoords.x = tox * cos + toy * sin;
					parentCoords.y = tox * -sin + toy * cos;
				}
			} else {
				if (originX == 0 && originY == 0) {
					final float tox = parentCoords.x - childX;
					final float toy = parentCoords.y - childY;

					parentCoords.x = (tox * cos + toy * sin) / scaleX;
					parentCoords.y = (tox * -sin + toy * cos) / scaleY;
				} else {
					final float worldOriginX = childX + originX;
					final float worldOriginY = childY + originY;
					final float fx = -originX * scaleX;
					final float fy = -originY * scaleY;

					final float x1 = cos * fx - sin * fy + worldOriginX;
					final float y1 = sin * fx + cos * fy + worldOriginY;

					final float tox = parentCoords.x - x1;
					final float toy = parentCoords.y - y1;

					parentCoords.x = (tox * cos + toy * sin) / scaleX;
					parentCoords.y = (tox * -sin + toy * cos) / scaleY;
				}
			}
		}
	}

	public String toString () {
		String name = this.name;
		if (name == null) {
			name = getClass().getName();
			int dotIndex = name.lastIndexOf('.');
			if (dotIndex != -1) name = name.substring(dotIndex + 1);
		}
		return name + " " + x + "," + y + " " + width + "x" + height;
	}
}
