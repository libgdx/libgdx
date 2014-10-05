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

import static com.badlogic.gdx.scenes.scene2d.utils.Align.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.Pools;

/** 2D scene graph node. An actor has a position, rectangular size, origin, scale, rotation, Z index, and color. The position
 * corresponds to the unrotated, unscaled bottom left corner of the actor. The position is relative to the actor's parent. The
 * origin is relative to the position and is used for scale and rotation.
 * <p>
 * An actor has a list of in progress {@link Action actions} that are applied to the actor (often over time). These are generally
 * used to change the presentation of the actor (moving it, resizing it, etc). See {@link #act(float)}, {@link Action} and its
 * many subclasses.
 * <p>
 * An actor has two kinds of listeners associated with it: "capture" and regular. The listeners are notified of events the actor
 * or its children receive. The regular listeners are designed to allow an actor to respond to events that have been delivered.
 * The capture listeners are designed to allow a parent or container actor to handle events before child actors. See {@link #fire}
 * for more details.
 * <p>
 * An {@link InputListener} can receive all the basic input events. More complex listeners (like {@link ClickListener} and
 * {@link ActorGestureListener}) can listen for and combine primitive events and recognize complex interactions like multi-touch
 * or pinch.
 * @author mzechner
 * @author Nathan Sweet */
public class Actor {
	private Stage stage;
	Group parent;
	private final DelayedRemovalArray<EventListener> listeners = new DelayedRemovalArray(0);
	private final DelayedRemovalArray<EventListener> captureListeners = new DelayedRemovalArray(0);
	private final Array<Action> actions = new Array(0);

	private String name;
	private Touchable touchable = Touchable.enabled;
	private boolean visible = true, debug;
	float x, y;
	float width, height;
	float originX, originY;
	float scaleX = 1, scaleY = 1;
	float rotation;
	final Color color = new Color(1, 1, 1, 1);
	private Object userObject;
	
	static boolean actionsChanged;

	/** Draws the actor. The batch is configured to draw in the parent's coordinate system.
	 * {@link Batch#draw(com.badlogic.gdx.graphics.g2d.TextureRegion, float, float, float, float, float, float, float, float, float)
	 * This draw method} is convenient to draw a rotated and scaled TextureRegion. {@link Batch#begin()} has already been called on
	 * the batch. If {@link Batch#end()} is called to draw without the batch then {@link Batch#begin()} must be called before the
	 * method returns.
	 * <p>
	 * The default implementation does nothing.
	 * @param parentAlpha Should be multiplied with the actor's alpha, allowing a parent's alpha to affect all children. */
	public void draw (Batch batch, float parentAlpha) {
	}

	/** Updates the actor based on time. Typically this is called each frame by {@link Stage#act(float)}.
	 * <p>
	 * The default implementation calls {@link Action#act(float)} on each action and removes actions that are complete.
	 * @param delta Time in seconds since the last frame. */
	public void act (float delta) {
		Array<Action> actions = this.actions;
		if (actions.size > 0) {
			actionsChanged = true;
			for (int i = 0; i < actions.size; i++) {
				Action action = actions.get(i);
				if (action.act(delta) && i < actions.size) {
					Action current = actions.get(i);
					int actionIndex = current == action ? i : actions.indexOf(action, true);
					if (actionIndex != -1) {
						actions.removeIndex(actionIndex);
						action.setActor(null);
						i--;
					}
				}
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
		Group parent = this.parent;
		while (parent != null) {
			ancestors.add(parent);
			parent = parent.parent;
		}

		try {
			// Notify all parent capture listeners, starting at the root. Ancestors may stop an event before children receive it.
			Object[] ancestorsArray = ancestors.items;
			for (int i = ancestors.size - 1; i >= 0; i--) {
				Group currentTarget = (Group)ancestorsArray[i];
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
				((Group)ancestorsArray[i]).notify(event, false);
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
		actionsChanged = true;
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

	/** Returns the parent actor, or null if not in a group. */
	public Group getParent () {
		return parent;
	}

	/** Called by the framework when an actor is added to or removed from a group.
	 * @param parent May be null if the actor has been removed from the parent. */
	protected void setParent (Group parent) {
		this.parent = parent;
	}

	/** Returns true if input events are processed by this actor. */
	public boolean isTouchable () {
		return touchable == Touchable.enabled;
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

	/** Returns an application specific object for convenience, or null. */
	public Object getUserObject () {
		return userObject;
	}

	/** Sets an application specific object for convenience. */
	public void setUserObject (Object userObject) {
		this.userObject = userObject;
	}

	/** Returns the X position of the actor's left edge. */
	public float getX () {
		return x;
	}

	/** Returns the X position of the specified {@link Align alignment}. */
	public float getX (int align) {
		float x = this.x;
		if ((align & right) != 0)
			x += width;
		else if ((align & left) == 0) //
			x += width / 2;
		return x;
	}

	public void setX (float x) {
		if (this.x != x) {
			this.x = x;
			positionChanged();
		}
	}

	/** Returns the Y position of the actor's bottom edge. */
	public float getY () {
		return y;
	}

	public void setY (float y) {
		if (this.y != y) {
			this.y = y;
			positionChanged();
		}
	}

	/** Returns the Y position of the specified {@link Align alignment}. */
	public float getY (int align) {
		float y = this.y;
		if ((align & top) != 0)
			y += height;
		else if ((align & bottom) == 0) //
			y += height / 2;
		return y;
	}

	/** Sets the position of the actor's bottom left corner. */
	public void setPosition (float x, float y) {
		if (this.x != x || this.y != y) {
			this.x = x;
			this.y = y;
			positionChanged();
		}
	}

	/** Sets the position using the specified {@link Align alignment}. Note this may set the position to non-integer coordinates. */
	public void setPosition (float x, float y, int align) {
		if ((align & right) != 0)
			x -= width;
		else if ((align & left) == 0) //
			x -= width / 2;

		if ((align & top) != 0)
			y -= height;
		else if ((align & bottom) == 0) //
			y -= height / 2;

		if (this.x != x || this.y != y) {
			this.x = x;
			this.y = y;
			positionChanged();
		}
	}

	/** Add x and y to current position */
	public void moveBy (float x, float y) {
		if (x != 0 || y != 0) {
			this.x += x;
			this.y += y;
			positionChanged();
		}
	}

	public float getWidth () {
		return width;
	}

	public void setWidth (float width) {
		float oldWidth = this.width;
		this.width = width;
		if (width != oldWidth) sizeChanged();
	}

	public float getHeight () {
		return height;
	}

	public void setHeight (float height) {
		float oldHeight = this.height;
		this.height = height;
		if (height != oldHeight) sizeChanged();
	}

	/** Returns y plus height. */
	public float getTop () {
		return y + height;
	}

	/** Returns x plus width. */
	public float getRight () {
		return x + width;
	}

	/** Called when the actor's position has been changed. */
	protected void positionChanged () {
	}

	/** Called when the actor's size has been changed. */
	protected void sizeChanged () {
	}

	/** Sets the width and height. */
	public void setSize (float width, float height) {
		float oldWidth = this.width;
		float oldHeight = this.height;
		this.width = width;
		this.height = height;
		if (width != oldWidth || height != oldHeight) sizeChanged();
	}

	/** Adds the specified size to the current size. */
	public void sizeBy (float size) {
		width += size;
		height += size;
		sizeChanged();
	}

	/** Adds the specified size to the current size. */
	public void sizeBy (float width, float height) {
		this.width += width;
		this.height += height;
		sizeChanged();
	}

	/** Set bounds the x, y, width, and height. */
	public void setBounds (float x, float y, float width, float height) {
		if (this.x != x || this.y != y) {
			this.x = x;
			this.y = y;
			positionChanged();
		}
		if (this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			sizeChanged();
		}
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

	/** Sets the origin position which is relative to the actor's bottom left corner. */
	public void setOrigin (float originX, float originY) {
		this.originX = originX;
		this.originY = originY;
	}

	/** Sets the origin position to the specified {@link Align alignment}. */
	public void setOrigin (int align) {
		if ((align & left) != 0)
			originX = 0;
		else if ((align & right) != 0)
			originX = width;
		else
			originX = width / 2;

		if ((align & bottom) != 0)
			originY = 0;
		else if ((align & top) != 0)
			originY = height;
		else
			originY = height / 2;
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

	/** Sets the scale for both X and Y */
	public void setScale (float scaleXY) {
		this.scaleX = scaleXY;
		this.scaleY = scaleXY;
	}

	/** Sets the scale X and scale Y. */
	public void setScale (float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	/** Adds the specified scale to the current scale. */
	public void scaleBy (float scale) {
		scaleX += scale;
		scaleY += scale;
	}

	/** Adds the specified scale to the current scale. */
	public void scaleBy (float scaleX, float scaleY) {
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
	public void rotateBy (float amountInDegrees) {
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

	/** Retrieve custom actor name set with {@link Actor#setName(String)}, used for easier identification */
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
		Array<Actor> children = parent.children;
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
		return parent.children.indexOf(this, true);
	}

	/** Calls {@link #clipBegin(float, float, float, float)} to clip this actor's bounds. */
	public boolean clipBegin () {
		return clipBegin(x, y, width, height);
	}

	/** Clips the specified screen aligned rectangle, specified relative to the transform matrix of the stage's Batch. The transform
	 * matrix and the stage's camera must not have rotational components. Calling this method must be followed by a call to
	 * {@link #clipEnd()} if true is returned.
	 * @return false if the clipping area is zero and no drawing should occur.
	 * @see ScissorStack */
	public boolean clipBegin (float x, float y, float width, float height) {
		if (width <= 0 || height <= 0) return false;
		Rectangle tableBounds = Rectangle.tmp;
		tableBounds.x = x;
		tableBounds.y = y;
		tableBounds.width = width;
		tableBounds.height = height;
		Stage stage = this.stage;
		Rectangle scissorBounds = Pools.obtain(Rectangle.class);
		stage.calculateScissors(tableBounds, scissorBounds);
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
			final float tox = (localCoords.x - originX) * scaleX;
			final float toy = (localCoords.y - originY) * scaleY;
			localCoords.x = (tox * cos + toy * sin) + originX + x;
			localCoords.y = (tox * -sin + toy * cos) + originY + y;
		}
		return localCoords;
	}

	/** Converts coordinates for this actor to those of a parent actor. The ascendant does not need to be a direct parent. */
	public Vector2 localToAscendantCoordinates (Actor ascendant, Vector2 localCoords) {
		Actor actor = this;
		while (actor != null) {
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

	/** Draws this actor's debug lines if {@link #getDebug()} is true. */
	public void drawDebug (ShapeRenderer shapes) {
		drawDebugBounds(shapes);
	}

	/** Draws a rectange for the bounds of this actor if {@link #getDebug()} is true. */
	protected void drawDebugBounds (ShapeRenderer shapes) {
		if (!debug) return;
		shapes.set(ShapeType.Line);
		shapes.setColor(stage.getDebugColor());
		shapes.rect(x, y, originX, originY, width - 1, height - 1, scaleX, scaleY, rotation);
	}

	/** If true, {@link #drawDebug(ShapeRenderer)} will be called for this actor. */
	public void setDebug (boolean enabled) {
		debug = enabled;
		if (enabled) Stage.debug = true;
	}

	public boolean getDebug () {
		return debug;
	}

	/** Calls {@link #setDebug(boolean)} with {@code true}. */
	public Actor debug () {
		setDebug(true);
		return this;
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
