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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;

/** 2D scene graph node that may contain other actors.
 * <p>
 * Actors have a z-order equal to the order they were inserted into the group. Actors inserted later will be drawn on top of
 * actors added earlier. Touch events that hit more than one actor are distributed to topmost actors first.
 * @author mzechner
 * @author Nathan Sweet */
public class Group extends Actor implements Cullable {
	private final SnapshotArray<Actor> children = new SnapshotArray(true, 4, Actor.class);
	private final Matrix3 localTransform = new Matrix3();
	private final Matrix3 worldTransform = new Matrix3();
	private final Matrix4 batchTransform = new Matrix4();
	private final Matrix4 oldBatchTransform = new Matrix4();
	private boolean transform = true;
	private Rectangle cullingArea;
	private final Vector2 point = new Vector2();

	public void act (float delta) {
		super.act(delta);
		Actor[] actors = children.begin();
		for (int i = 0, n = children.size; i < n; i++)
			actors[i].act(delta);
		children.end();
	}

	/** Draws the group and its children. The default implementation calls {@link #applyTransform(SpriteBatch, Matrix4)} if needed,
	 * then {@link #drawChildren(SpriteBatch, float)}, then {@link #resetTransform(SpriteBatch)} if needed. */
	public void draw (SpriteBatch batch, float parentAlpha) {
		if (transform) applyTransform(batch, computeTransform());
		drawChildren(batch, parentAlpha);
		if (transform) resetTransform(batch);
	}

	/** Draws all children. {@link #applyTransform(SpriteBatch, Matrix4)} should be called before and
	 * {@link #resetTransform(SpriteBatch)} after this method if {@link #setTransform(boolean) transform} is true. If
	 * {@link #setTransform(boolean) transform} is false these methods don't need to be called, children positions are temporarily
	 * offset by the group position when drawn. This method avoids drawing children completely outside the
	 * {@link #setCullingArea(Rectangle) culling area}, if set. */
	protected void drawChildren (SpriteBatch batch, float parentAlpha) {
		parentAlpha *= getColor().a;
		SnapshotArray<Actor> children = this.children;
		Actor[] actors = children.begin();
		Rectangle cullingArea = this.cullingArea;
		if (cullingArea != null) {
			// Draw children only if inside culling area.
			float cullLeft = cullingArea.x;
			float cullRight = cullLeft + cullingArea.width;
			float cullBottom = cullingArea.y;
			float cullTop = cullBottom + cullingArea.height;
			if (transform) {
				for (int i = 0, n = children.size; i < n; i++) {
					Actor child = actors[i];
					if (!child.isVisible()) continue;
					float x = child.getX(), y = child.getY();
					if (x <= cullRight && y <= cullTop && x + child.getWidth() >= cullLeft && y + child.getHeight() >= cullBottom)
						child.draw(batch, parentAlpha);
				}
				batch.flush();
			} else {
				// No transform for this group, offset each child.
				float offsetX = getX(), offsetY = getY();
				setX(0);
				setY(0);
				for (int i = 0, n = children.size; i < n; i++) {
					Actor child = actors[i];
					if (!child.isVisible()) continue;
					float x = child.getX(), y = child.getY();
					if (x <= cullRight && y <= cullTop && x + child.getWidth() >= cullLeft && y + child.getHeight() >= cullBottom) {
						child.setX(x + offsetX);
						child.setY(y + offsetY);
						child.draw(batch, parentAlpha);
						child.setX(x);
						child.setY(y);
					}
				}
				setX(offsetX);
				setY(offsetY);
			}
		} else {
			// No culling, draw all children.
			if (transform) {
				for (int i = 0, n = children.size; i < n; i++) {
					Actor child = actors[i];
					if (!child.isVisible()) continue;
					child.draw(batch, parentAlpha);
				}
				batch.flush();
			} else {
				// No transform for this group, offset each child.
				float offsetX = getX(), offsetY = getY();
				setX(0);
				setY(0);
				for (int i = 0, n = children.size; i < n; i++) {
					Actor child = actors[i];
					if (!child.isVisible()) continue;
					float x = child.getX(), y = child.getY();
					child.setX(x + offsetX);
					child.setY(y + offsetY);
					child.draw(batch, parentAlpha);
					child.setX(x);
					child.setY(y);
				}
				setX(offsetX);
				setY(offsetY);
			}
		}
		children.end();
	}

	/** Set the SpriteBatch's transformation matrix, often with the result of {@link #computeTransform()}. Note this causes the
	 * batch to be flushed. {@link #resetTransform(SpriteBatch)} will restore the transform to what it was before this call. */
	protected void applyTransform (SpriteBatch batch, Matrix4 transform) {
		batch.end();
		oldBatchTransform.set(batch.getTransformMatrix());
		batch.setTransformMatrix(transform);
		batch.begin();
	}

	/** Returns the transform for this group's coordinate system. */
	protected Matrix4 computeTransform () {
		Matrix3 temp = worldTransform;

		float originX = getOriginX();
		float originY = getOriginY();
		float rotation = getRotation();
		float scaleX = getScaleX();
		float scaleY = getScaleY();

		if (originX != 0 || originY != 0)
			localTransform.setToTranslation(originX, originY);
		else
			localTransform.idt();
		if (rotation != 0) localTransform.rotate(rotation);
		if (scaleX != 1 || scaleY != 1) localTransform.scale(scaleX, scaleY);
		if (originX != 0 || originY != 0) localTransform.translate(-originX, -originY);
		localTransform.trn(getX(), getY());

		// Find the first parent that transforms.
		Group parentGroup = getParent();
		while (parentGroup != null) {
			if (parentGroup.transform) break;
			parentGroup = parentGroup.getParent();
		}

		if (parentGroup != null) {
			worldTransform.set(parentGroup.worldTransform);
			worldTransform.mul(localTransform);
		} else {
			worldTransform.set(localTransform);
		}

		batchTransform.set(worldTransform);
		return batchTransform;
	}

	/** Restores the SpriteBatch transform to what it was before {@link #applyTransform(SpriteBatch, Matrix4)}. Note this causes the
	 * batch to be flushed. */
	protected void resetTransform (SpriteBatch batch) {
		batch.end();
		batch.setTransformMatrix(oldBatchTransform);
		batch.begin();
	}

	/** Children completely outside of this rectangle will not be drawn. This is only valid for use with unrotated and unscaled
	 * actors! */
	public void setCullingArea (Rectangle cullingArea) {
		this.cullingArea = cullingArea;
	}

	public Actor hit (float x, float y, boolean touchable) {
		if (touchable && getTouchable() == Touchable.disabled) return null;
		Array<Actor> children = this.children;
		for (int i = children.size - 1; i >= 0; i--) {
			Actor child = children.get(i);
			if (!child.isVisible()) continue;
			child.parentToLocalCoordinates(point.set(x, y));
			Actor hit = child.hit(point.x, point.y, touchable);
			if (hit != null) return hit;
		}
		return super.hit(x, y, touchable);
	}

	/** Called when actors are added to or removed from the group. */
	protected void childrenChanged () {
	}

	/** Adds an actor as a child of this group. The actor is first removed from its parent group, if any. */
	public void addActor (Actor actor) {
		actor.remove();
		children.add(actor);
		actor.setParent(this);
		actor.setStage(getStage());
		childrenChanged();
	}

	/** Adds an actor as a child of this group, at a specific index. The actor is first removed from its parent group, if any.
	 * @param index May be greater than the number of children. */
	public void addActorAt (int index, Actor actor) {
		actor.remove();
		if (index >= children.size)
			children.add(actor);
		else
			children.insert(index, actor);
		actor.setParent(this);
		actor.setStage(getStage());
		childrenChanged();
	}

	/** Adds an actor as a child of this group, immediately before another child actor. The actor is first removed from its parent
	 * group, if any. */
	public void addActorBefore (Actor actorBefore, Actor actor) {
		actor.remove();
		int index = children.indexOf(actorBefore, true);
		children.insert(index, actor);
		actor.setParent(this);
		actor.setStage(getStage());
		childrenChanged();
	}

	/** Adds an actor as a child of this group, immediately after another child actor. The actor is first removed from its parent
	 * group, if any. */
	public void addActorAfter (Actor actorAfter, Actor actor) {
		actor.remove();
		int index = children.indexOf(actorAfter, true);
		if (index == children.size)
			children.add(actor);
		else
			children.insert(index + 1, actor);
		actor.setParent(this);
		actor.setStage(getStage());
		childrenChanged();
	}

	/** Removes an actor from this group. If the actor will not be used again and has actions, they should be
	 * {@link Actor#clearActions() cleared} so the actions will be returned to their
	 * {@link Action#setPool(com.badlogic.gdx.utils.Pool) pool}, if any. This is not done automatically. */
	public boolean removeActor (Actor actor) {
		if (!children.removeValue(actor, true)) return false;
		Stage stage = getStage();
		if (stage != null) stage.unfocus(actor);
		actor.setParent(null);
		actor.setStage(null);
		childrenChanged();
		return true;
	}

	/** Removes all actors from this group. */
	public void clear () {
		Actor[] actors = children.begin();
		for (int i = 0, n = children.size; i < n; i++) {
			Actor child = actors[i];
			child.setStage(null);
			child.setParent(null);
		}
		children.end();
		children.clear();
		childrenChanged();
	}

	/** Returns the first actor found with the specified name. Note this recursively compares the name of every actor in the group. */
	public Actor findActor (String name) {
		Array<Actor> children = this.children;
		for (int i = 0, n = children.size; i < n; i++)
			if (name.equals(children.get(i).getName())) return children.get(i);
		for (int i = 0, n = children.size; i < n; i++) {
			Actor child = children.get(i);
			if (child instanceof Group) {
				Actor actor = ((Group)child).findActor(name);
				if (actor != null) return actor;
			}
		}
		return null;
	}

	protected void setStage (Stage stage) {
		super.setStage(stage);
		Array<Actor> children = this.children;
		for (int i = 0, n = children.size; i < n; i++)
			children.get(i).setStage(stage);
	}

	/** Swaps two actors by index. Returns false if the swap did not occur because the indexes were out of bounds. */
	public boolean swapActor (int first, int second) {
		int maxIndex = children.size;
		if (first < 0 || first >= maxIndex) return false;
		if (second < 0 || second >= maxIndex) return false;
		children.swap(first, second);
		return true;
	}

	/** Swaps two actors. Returns false if the swap did not occur because the actors are not children of this group. */
	public boolean swapActor (Actor first, Actor second) {
		int firstIndex = children.indexOf(first, true);
		int secondIndex = children.indexOf(second, true);
		if (firstIndex == -1 || secondIndex == -1) return false;
		children.swap(firstIndex, secondIndex);
		return true;
	}

	/** Returns an ordered list of child actors in this group. */
	public SnapshotArray<Actor> getChildren () {
		return children;
	}

	/** When true (the default), the SpriteBatch is transformed so children are drawn in their parent's coordinate system. This has
	 * a performance impact because {@link SpriteBatch#flush()} must be done before and after the transform. If the actors in a
	 * group are not rotated or scaled, then the transform for the group can be set to false. In this case, each child's position
	 * will be offset by the group's position for drawing, causing the children to appear in the correct location even though the
	 * SpriteBatch has not been transformed. */
	public void setTransform (boolean transform) {
		this.transform = transform;
	}

	public boolean isTransform () {
		return transform;
	}

	/** Converts coordinates for this group to those of a descendant actor. The descendant does not need to be a direct child.
	 * @throws IllegalArgumentException if the specified actor is not a descendant of this group. */
	public Vector2 localToDescendantCoordinates (Actor descendant, Vector2 localCoords) {
		Group parent = descendant.getParent();
		if (parent == null) throw new IllegalArgumentException("Child is not a descendant: " + descendant);
		// First convert to the actor's parent coordinates.
		if (parent != this) localToDescendantCoordinates(parent, localCoords);
		// Then from each parent down to the descendant.
		descendant.parentToLocalCoordinates(localCoords);
		return localCoords;
	}
}
