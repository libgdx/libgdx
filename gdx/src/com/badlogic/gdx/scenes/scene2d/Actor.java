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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pools;

/** 2D scene graph node. An actor has a position, rectangular size, origin, scale, rotation, Z index, and color. The position
 * corresponds to the unrotated, unscaled bottom left corner of the actor. The position is relative to the actor's parent. The
 * origin is relative to the position and is used for scale and rotation.
 * <p>
 * An actor has a list of in-progress {@link Action actions} that are applied to the actor (over time). These are generally used to
 * change the presentation of the actor (moving it, resizing it, etc). See {@link #act(float)} and {@link Action}.
 * <p>
 * An actor has two kinds of listeners associated with it: "capture" and regular. The listeners are notified of events the actor
 * or its children receive. The capture listeners are designed to allow a parent or container actor to hide events from child
 * actors. The regular listeners are designed to allow an actor to respond to events that have been delivered. See {@link #fire}
 * for more details.
 * <p>
 * An {@link InputListener} can receive all the basic input events, and more complex listeners (like {@link ClickListener} and
 * {@link ActorGestureListener}) can listen for and combine primitive events and recognize complex interactions like multi-click
 * or pinch.
 * 
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
	float x, y;
	float width, height;
	float originX, originY;
	float scaleX = 1, scaleY = 1;
	float rotation;
	final Color color = new Color(1, 1, 1, 1);

	/** Draws the actor. The SpriteBatch is configured to draw in the parent's coordinate system.
	 * {@link SpriteBatch#draw(com.badlogic.gdx.graphics.g2d.TextureRegion, float, float, float, float, float, float, float, float, float)
	 * This draw method} is convenient to draw a rotated and scaled TextureRegion. {@link SpriteBatch#begin()} has already been
	 * called on the SpriteBatch. If {@link SpriteBatch#end()} is called to draw without the SpriteBatch then
	 * {@link SpriteBatch#begin()} must be called before the method returns.
	 * <p>
	 * The default implementation does nothing.
	 * @param parentAlpha Should be multiplied with the actor's alpha, allowing a parent's alpha to affect all children. */
	public void draw (SpriteBatch batch, float parentAlpha) {
	}

	/** Updates the actor based on time. Typically this is called each frame by {@link Stage#act(float)}.
	 * <p>
	 * The default implementation calls {@link Action#act(float)} on each action and removes actions that are complete.
	 * @param delta Time in seconds since the last frame. */
	public void act (float delta) {
		for (int i = 0; i < actions.size; i++) {
			Action action = actions.get(i);
			if (action.act(delta) && i < actions.size) {
				actions.removeIndex(i);
				action.setActor(null);
				i--;
			}
		}
	}

	/** Sets this actor as the event {@link Event#setTarget(Actor) target} and propagates the event to this actor and ancestor
	 * actors as necessary. If this actor is not in the stage, the stage must be set before calling this method.
	 * <p>
	 * Events are fired in 2 phases.
	 * <ol>
	 * <li>The first phase (the "capture" phase) notifies listeners on each actor starting at the root and propagating downward to
	 * (and including) this actor.</li>
	 * <li>The second phase notifies listeners on each actor starting at this actor and, if {@link Event#getBubbles()} is true,
	 * propagating upward to the root.</li>
	 * </ol>
	 * If the event is {@link Event#stop() stopped} at any time, it will not propagate to the next actor.
	 * @return true if the event was {@link Event#cancel() cancelled}. */
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
	 * This method is used to delegate touchDown, mouse, and enter/exit events. If this method returns null, those events will not
	 * occur on this Actor.
	 * <p>
	 * The default implementation returns this actor if the point is within this actor's bounds.
	 * 
	 * @param touchable If true, the hit detection will respect the {@link #setTouchable(Touchable) touchability}.
	 * @see Touchable */
	public Actor hit (float x, float y, boolean touchable) {
		if (touchable && this.touchable != Touchable.enabled) return null;
		return x >= 0 && x < width && y >= 0 && y < height ? this : null;
	}

	/** Removes this actor from its parent, if it has a parent.
	 * @see Group#removeActor(Actor) */
	public boolean remove () {
		if (parent != null) return parent.removeActor(this);
		return false;
	}

	/** Add a listener to receive events that {@link #hit(float, float, boolean) hit} this actor. See {@link #fire(Event)}.
	 * 
	 * @see InputListener
	 * @see ClickListener */
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

	/** Removes all listeners on this actor. */
	public void clearListeners () {
		listeners.clear();
		captureListeners.clear();
	}

	/** Removes all actions and listeners on this actor. */
	public void clear () {
		clearActions();
		clearListeners();
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

	/** Returns true if this actor is the same as or is the descendant of the specified actor. */
	public boolean isDescendantOf (Actor actor) {
		if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
		Actor parent = this;
		while (true) {
			if (parent == null) return false;
			if (parent == actor) return true;
			parent = parent.parent;
		}
	}

	/** Returns true if this actor is the same as or is the ascendant of the specified actor. */
	public boolean isAscendantOf (Actor actor) {
		if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
		while (true) {
			if (actor == null) return false;
			if (actor == this) return true;
			actor = actor.parent;
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

	/** Called by the framework when an actor is added to or removed from a group.
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
		this.x = x;
		this.y = y;
	}

	public void translate (float x, float y) {
		this.x += x;
		this.y += y;
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
		return y + height;
	}

	/** Returns x plus width. */
	public float getRight () {
		return x + width;
	}

	/** Sets the width and height. */
	public void setSize (float width, float height) {
		this.width = width;
		this.height = height;
	}

	/** Adds the specified size to the current size. */
	public void size (float size) {
		width += size;
		height += size;
	}

	/** Adds the specified size to the current size. */
	public void size (float width, float height) {
		this.width += width;
		this.height += height;
	}

	/** Set bounds the x, y, width, and height. */
	public void setBounds (float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
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
		this.originX = originX;
		this.originY = originY;
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
		this.scaleX = scale;
		this.scaleY = scale;
	}

	/** Sets the scalex and scaley. */
	public void setScale (float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	/** Adds the specified scale to the current scale. */
	public void scale (float scale) {
		scaleX += scale;
		scaleY += scale;
	}

	/** Adds the specified scale to the current scale. */
	public void scale (float scaleX, float scaleY) {
		this.scaleX += scaleX;
		this.scaleY += scaleY;
	}

	public float getRotation () {
		return rotation;
	}

	public void setRotation (float degrees) {
		this.rotation = degrees;
	}

	/** Adds the specified rotation to the current rotation. */
	public void rotate (float amountInDegrees) {
		rotation += amountInDegrees;
	}

	public void setColor (Color color) {
		this.color.set(color);
	}

	public void setColor (float r, float g, float b, float a) {
		color.set(r, g, b, a);
	}

	/** Returns the color the actor will be tinted when drawn. The returned instance can be modified to change the color. */
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
		Group parent = this.parent;
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
		Group parent = this.parent;
		if (parent == null) return -1;
		return parent.getChildren().indexOf(this, true);
	}

	/** Calls {@link #clipBegin(float, float, float, float)} to clip this actor's bounds. */
	public boolean clipBegin () {
		return clipBegin(x, y, width, height);
	}

	/** Clips the specified screen aligned rectangle, specified relative to the transform matrix of the stage's SpriteBatch. The
	 * transform matrix and the stage's camera must not have rotational components. Calling this method must be followed by a call
	 * to {@link #clipEnd()} if true is returned.
	 * @return false if the clipping area is zero and no drawing should occur.
	 * @see ScissorStack */
	public boolean clipBegin (float x, float y, float width, float height) {
		Rectangle tableBounds = Rectangle.tmp;
		tableBounds.x = x;
		tableBounds.y = y;
		tableBounds.width = width;
		tableBounds.height = height;
		Stage stage = this.stage;
		Rectangle scissorBounds = Pools.obtain(Rectangle.class);
		ScissorStack.calculateScissors(stage.getCamera(), stage.getSpriteBatch().getTransformMatrix(), tableBounds, scissorBounds);
		if (ScissorStack.pushScissors(scissorBounds)) return true;
		Pools.free(scissorBounds);
		return false;
	}

	/** Ends clipping begun by {@link #clipBegin(float, float, float, float)}. */
	public void clipEnd () {
		Pools.free(ScissorStack.popScissors());
	}

	/** Transforms the specified point in screen coordinates to the actor's local coordinate system. */
	public Vector2 screenToLocalCoordinates (Vector2 screenCoords) {
		Stage stage = this.stage;
		if (stage == null) return screenCoords;
		return stageToLocalCoordinates(stage.screenToStageCoordinates(screenCoords));
	}

	/** Transforms the specified point in the stage's coordinates to the actor's local coordinate system. */
	public Vector2 stageToLocalCoordinates (Vector2 stageCoords) {
		if (parent == null) return stageCoords;
		parent.stageToLocalCoordinates(stageCoords);
		parentToLocalCoordinates(stageCoords);
		return stageCoords;
	}

	/** Transforms the specified point in the actor's coordinates to be in the stage's coordinates.
	 * @see Stage#toScreenCoordinates(Vector2, com.badlogic.gdx.math.Matrix4) */
	public Vector2 localToStageCoordinates (Vector2 localCoords) {
		return localToAscendantCoordinates(null, localCoords);
	}

	/** Transforms the specified point in the actor's coordinates to be in the parent's coordinates. */
	public Vector2 localToParentCoordinates (Vector2 localCoords) {
		final float rotation = -this.rotation;
		final float scaleX = this.scaleX;
		final float scaleY = this.scaleY;
		final float x = this.x;
		final float y = this.y;
		if (rotation == 0) {
			if (scaleX == 1 && scaleY == 1) {
				localCoords.x += x;
				localCoords.y += y;
			} else {
				final float originX = this.originX;
				final float originY = this.originY;
				localCoords.x = (localCoords.x - originX) * scaleX + originX + x;
				localCoords.y = (localCoords.y - originY) * scaleY + originY + y;
			}
		} else {
			final float cos = (float)Math.cos(rotation * MathUtils.degreesToRadians);
			final float sin = (float)Math.sin(rotation * MathUtils.degreesToRadians);
			final float originX = this.originX;
			final float originY = this.originY;
			final float tox = localCoords.x - originX;
			final float toy = localCoords.y - originY;
			localCoords.x = (tox * cos + toy * sin) * scaleX + originX + x;
			localCoords.y = (tox * -sin + toy * cos) * scaleY + originY + y;
		}
		return localCoords;
	}

	/** Converts coordinates for this actor to those of a parent actor. The ascendant does not need to be a direct parent. */
	public Vector2 localToAscendantCoordinates (Actor ascendant, Vector2 localCoords) {
		Actor actor = this;
		while (actor.parent != null) {
			actor.localToParentCoordinates(localCoords);
			actor = actor.parent;
			if (actor == ascendant) break;
		}
		return localCoords;
	}

	/** Converts the coordinates given in the parent's coordinate system to this actor's coordinate system. */
	public Vector2 parentToLocalCoordinates (Vector2 parentCoords) {
		final float rotation = this.rotation;
		final float scaleX = this.scaleX;
		final float scaleY = this.scaleY;
		final float childX = x;
		final float childY = y;
		if (rotation == 0) {
			if (scaleX == 1 && scaleY == 1) {
				parentCoords.x -= childX;
				parentCoords.y -= childY;
			} else {
				final float originX = this.originX;
				final float originY = this.originY;
				parentCoords.x = (parentCoords.x - childX - originX) / scaleX + originX;
				parentCoords.y = (parentCoords.y - childY - originY) / scaleY + originY;
			}
		} else {
			final float cos = (float)Math.cos(rotation * MathUtils.degreesToRadians);
			final float sin = (float)Math.sin(rotation * MathUtils.degreesToRadians);
			final float originX = this.originX;
			final float originY = this.originY;
			final float tox = parentCoords.x - childX - originX;
			final float toy = parentCoords.y - childY - originY;
			parentCoords.x = (tox * cos + toy * sin) / scaleX + originX;
			parentCoords.y = (tox * -sin + toy * cos) / scaleY + originY;
		}
		return parentCoords;
	}

	public String toString () {
		String name = this.name;
		if (name == null) {
			name = getClass().getName();
			int dotIndex = name.lastIndexOf('.');
			if (dotIndex != -1) name = name.substring(dotIndex + 1);
		}
		return name;
	}
}
