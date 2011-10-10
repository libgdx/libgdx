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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Cullable;
import com.badlogic.gdx.utils.ObjectMap;

/** A group is an Actor that contains other Actors (also other Groups which are Actors).
 * @author mzechner
 * @author Nathan Sweet */
public class Group extends Actor implements Cullable {
	public static Texture debugTexture;
	public static boolean debug = false;

	protected final List<Actor> children;
	protected final List<Actor> immutableChildren;
	protected final List<Group> groups;
	protected final List<Group> immutableGroups;
	protected final ObjectMap<String, Actor> namesToActors;

	protected final Matrix3 localTransform = new Matrix3();
	protected final Matrix3 worldTransform = new Matrix3();
	protected final Matrix4 batchTransform = new Matrix4();
	protected final Matrix4 oldBatchTransform = new Matrix4();

	public boolean transform = true;
	public Actor lastTouchedChild;
	public Actor[] focusedActor = new Actor[20];
	public Actor keyboardFocusedActor = null;
	public Actor scrollFocusedActor = null;

	protected Rectangle cullingArea;
	protected final Vector2 point = new Vector2();

	public Group () {
		this(null);
	}

	/** Creates a new Group with the given name.
	 * @param name the name of the group */
	public Group (String name) {
		super(name);
		children = new ArrayList<Actor>();
		immutableChildren = Collections.unmodifiableList(children);
		groups = new ArrayList<Group>();
		immutableGroups = Collections.unmodifiableList(groups);
		namesToActors = new ObjectMap<String, Actor>();
	}

	public void act (float delta) {
		super.act(delta);
		for (int i = 0; i < children.size(); i++) {
			Actor child = children.get(i);
			child.act(delta);
			if (child.isMarkedToRemove()) {
				child.markToRemove(false);
				removeActor(child);
				i--;
			}
		}
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		if (!visible) return;

		if (debug && debugTexture != null && parent != null)
			batch.draw(debugTexture, x, y, originX, originY, width == 0 ? 200 : width, height == 0 ? 200 : height, scaleX, scaleY,
				rotation, 0, 0, debugTexture.getWidth(), debugTexture.getHeight(), false, false);

		if (transform) applyTransform(batch);
		drawChildren(batch, parentAlpha);
		if (transform) resetTransform(batch);
	}

	protected void drawChildren (SpriteBatch batch, float parentAlpha) {
		parentAlpha *= color.a;
		if (cullingArea != null) {
			if (transform) {
				for (int i = 0; i < children.size(); i++) {
					Actor child = children.get(i);
					if (!child.visible) continue;
					if (child.x <= cullingArea.x + cullingArea.width && child.x + child.width >= cullingArea.x
						&& child.y <= cullingArea.y + cullingArea.height && child.y + child.height >= cullingArea.y) {
						child.draw(batch, parentAlpha);
					}
				}
				batch.flush();
			} else {
				for (int i = 0; i < children.size(); i++) {
					Actor child = children.get(i);
					if (!child.visible) continue;
					if (child.x <= cullingArea.x + cullingArea.width && child.x + child.width >= cullingArea.x
						&& child.y <= cullingArea.y + cullingArea.height && child.y + child.height >= cullingArea.y) {
						child.x += x;
						child.y += y;
						child.draw(batch, parentAlpha);
						child.x -= x;
						child.y -= y;
					}
				}
			}
		} else {
			if (transform) {
				for (int i = 0; i < children.size(); i++) {
					Actor child = children.get(i);
					if (!child.visible) continue;
					child.draw(batch, parentAlpha);
				}
				batch.flush();
			} else {
				for (int i = 0; i < children.size(); i++) {
					Actor child = children.get(i);
					if (!child.visible) continue;
					child.x += x;
					child.y += y;
					child.draw(batch, parentAlpha);
					child.x -= x;
					child.y -= y;
				}
			}
		}
	}

	protected void drawChild (Actor child, SpriteBatch batch, float parentAlpha) {
		if (child.visible) child.draw(batch, parentAlpha * color.a);
		if (transform) batch.flush();
	}

	protected void applyTransform (SpriteBatch batch) {
		Matrix4 newBatchTransform = updateTransform();
		batch.end();
		oldBatchTransform.set(batch.getTransformMatrix());
		batch.setTransformMatrix(newBatchTransform);
		batch.begin();
	}

	protected Matrix4 updateTransform () {
		Matrix3 temp = worldTransform;
		if (originX != 0 || originY != 0)
			localTransform.setToTranslation(originX, originY);
		else
			localTransform.idt();
		if (rotation != 0) localTransform.mul(temp.setToRotation(rotation));
		if (scaleX != 1 || scaleY != 1) localTransform.mul(temp.setToScaling(scaleX, scaleY));
		if (originX != 0 || originY != 0) localTransform.mul(temp.setToTranslation(-originX, -originY));
		localTransform.trn(x, y);

		Group parentGroup = parent;
		while (parentGroup != null) {
			if (parentGroup.transform) break;
			parentGroup = parentGroup.parent;
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

	protected void resetTransform (SpriteBatch batch) {
		batch.end();
		batch.setTransformMatrix(oldBatchTransform);
		batch.begin();
	}

	public void setCullingArea (Rectangle cullingArea) {
		this.cullingArea = cullingArea;
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (!touchable) return false;

		if (debug) Gdx.app.log("Group", name + ": " + x + ", " + y);

		if (focusedActor[pointer] != null) {
			point.x = x;
			point.y = y;
			focusedActor[pointer].toLocalCoordinates(point);
			focusedActor[pointer].touchDown(point.x, point.y, pointer);
			return true;
		}

		int len = children.size() - 1;
		for (int i = len; i >= 0; i--) {
			Actor child = children.get(i);
			if (!child.touchable) continue;

			toChildCoordinates(child, x, y, point);
			if (child.hit(point.x, point.y) == null) continue;
			if (child.touchDown(point.x, point.y, pointer)) {
				if (focusedActor[pointer] == null) {
					focus(child, pointer);
				}
				if (child instanceof Group)
					lastTouchedChild = ((Group)child).lastTouchedChild;
				else
					lastTouchedChild = child;
				return true;
			}
		}

		return false;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		if (!touchable) return;
		point.x = x;
		point.y = y;
		Actor actor = focusedActor[pointer];
		if (actor != this) {
			actor.toLocalCoordinates(point);
			actor.touchUp(point.x, point.y, pointer);
		}
		// If the focused actor hasn't changed, remove its focus.
		if (focusedActor[pointer] == actor) actor.parent.focus(null, pointer);
	}

	@Override
	public boolean touchMoved (float x, float y) {
		if (!touchable) return false;

		int len = children.size() - 1;
		for (int i = len; i >= 0; i--) {
			Actor child = children.get(i);
			if (!child.touchable) continue;

			toChildCoordinates(child, x, y, point);

			if (child.touchMoved(point.x, point.y)) return true;
		}
		return false;
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
		if (focusedActor[pointer] != null) {
			if (!touchable) return;
			point.x = x;
			point.y = y;
			focusedActor[pointer].toLocalCoordinates(point);
			focusedActor[pointer].touchDragged(point.x, point.y, pointer);
		}
	}

	@Override
	public boolean scrolled (int amount) {
		if (scrollFocusedActor != null) scrollFocusedActor.scrolled(amount);
		return false;
	}

	public boolean keyDown (int keycode) {
		if (keyboardFocusedActor != null)
			return keyboardFocusedActor.keyDown(keycode);
		else
			return false;
	}

	public boolean keyUp (int keycode) {
		if (keyboardFocusedActor != null)
			return keyboardFocusedActor.keyUp(keycode);
		else
			return false;
	}

	public boolean keyTyped (char character) {
		if (keyboardFocusedActor != null)
			return keyboardFocusedActor.keyTyped(character);
		else
			return false;
	}

	public Actor hit (float x, float y) {
		int len = children.size() - 1;
		for (int i = len; i >= 0; i--) {
			Actor child = children.get(i);

			toChildCoordinates(child, x, y, point);

			Actor hit = child.hit(point.x, point.y);
			if (hit != null) {
				return hit;
			}
		}
		return null;
	}

	/** Called when actors are added to or removed from the group. */
	protected void childrenChanged () {
	}

	/** Adds an {@link Actor} to this Group. The order Actors are added is reversed for hit testing.
	 * @param actor the Actor */
	public void addActor (Actor actor) {
		children.add(actor);
		if (actor instanceof Group) groups.add((Group)actor);
		if (actor.name != null) namesToActors.put(actor.name, actor);
		actor.parent = this;
		childrenChanged();
	}

	/** Adds an {@link Actor} at the given index in the group. The first Actor added will be at index 0 and so on. Throws an
	 * IndexOutOfBoundsException in case the index is invalid.
	 * @param index the index to add the actor at. */
	public void addActorAt (int index, Actor actor) {
		children.add(index, actor);
		if (actor instanceof Group) groups.add((Group)actor);
		if (actor.name != null) namesToActors.put(actor.name, actor);
		actor.parent = this;
		childrenChanged();
	}

	/** Adds an {@link Actor} before the given Actor.
	 * @param actorBefore the Actor to add the other actor in front of
	 * @param actor the Actor to add */
	public void addActorBefore (Actor actorBefore, Actor actor) {
		int index = children.indexOf(actorBefore);
		children.add(index, actor);
		if (actor instanceof Group) groups.add((Group)actor);
		if (actor.name != null) namesToActors.put(actor.name, actor);
		actor.parent = this;
		childrenChanged();
	}

	/** Adds an {@link Actor} after the given Actor.
	 * @param actorAfter the Actor to add the other Actor behind
	 * @param actor the Actor to add */
	public void addActorAfter (Actor actorAfter, Actor actor) {
		int index = children.indexOf(actorAfter);
		if (index == children.size())
			children.add(actor);
		else
			children.add(index + 1, actor);
		if (actor instanceof Group) groups.add((Group)actor);
		if (actor.name != null) namesToActors.put(actor.name, actor);
		actor.parent = this;
		childrenChanged();
	}

	/** Removes an {@link Actor} from this Group.
	 * @param actor */
	public void removeActor (Actor actor) {
		children.remove(actor);
		if (actor instanceof Group) groups.remove((Group)actor);
		if (actor.name != null) namesToActors.remove(actor.name);
		unfocusAll(actor);
		childrenChanged();
	}

	/** Removes an {@link Actor} from this Group recursively by checking if the Actor is in this group or one of its child-groups.
	 * @param actor the Actor */
	public void removeActorRecursive (Actor actor) {
		if (children.remove(actor)) {
			if (actor instanceof Group) groups.remove((Group)actor);
			if (actor.name != null) namesToActors.remove(actor.name);
			unfocusAll(actor);
			return;
		}

		for (int i = 0; i < groups.size(); i++) {
			groups.get(i).removeActorRecursive(actor);
		}
		childrenChanged();
	}

	/** Finds the {@link Actor} with the given name in this Group and its children.
	 * @param name the name of the Actor
	 * @return the Actor or null */
	public Actor findActor (String name) {
		Actor actor = namesToActors.get(name);
		if (actor == null) {
			int len = groups.size();
			for (int i = 0; i < len; i++) {
				actor = groups.get(i).findActor(name);
				if (actor != null) return actor;
			}
		}

		return actor;
	}

	/** Swap two actors' sort order by index. 0 is lowest while getActors().size() - 1 is largest.
	 * @param first first Actor index
	 * @param second second Actor index
	 * @return false if indices are out of bound. */
	public boolean swapActor (int first, int second) {
		int maxIndex = children.size();
		if (first < 0 || first >= maxIndex) return false;
		if (second < 0 || second >= maxIndex) return false;
		Collections.swap(children, first, second);
		return true;
	}

	/** Swap two actors' sort order by reference.
	 * @param first first Actor
	 * @param second second Actor
	 * @return false if any of the Actors is not the child of this Group. */
	public boolean swapActor (Actor first, Actor second) {
		int firstIndex = children.indexOf(first);
		int secondIndex = children.indexOf(second);
		if (firstIndex == -1 || secondIndex == -1) return false;
		Collections.swap(children, firstIndex, secondIndex);
		return true;
	}

	/** @return all child {@link Actor}s as an ordered list. */
	public List<Actor> getActors () {
		return immutableChildren;
	}

	/** @return all child {@link Group}s as an unordered list. */
	public List<Group> getGroups () {
		return immutableGroups;
	}

	/** Sets the focus to the given child {@link Actor}. All subsequent touch events with the given pointer id will be passed to
	 * this child Actor. To unset the focus simply pass null.
	 * 
	 * @param actor the Actor */
	public void focus (Actor actor, int pointer) {
		Actor existingActor = focusedActor[pointer];
		if (existingActor != null) {
			// An actor already has focus. Remove the focus if it is not a child of this group, because the focused actor could be
			// further down in the hiearchy.
			focusedActor[pointer] = null;
			if (existingActor.parent != this) existingActor.parent.focus(null, pointer);
		}
		if (debug) Gdx.app.log("Group", "focus: " + (actor == null ? "null" : actor.name));
		focusedActor[pointer] = actor;
		if (parent != null) parent.focus(actor, pointer);
	}

	/** Sets the keyboard focus to the given child {@link Actor}. All subsequent keyboard events will be passed to that actor. To
	 * unfocus an actor simply pass null.
	 * @param actor the Actor */
	public void keyboardFocus (Actor actor) {
		keyboardFocusedActor = actor;
		if (parent != null) parent.keyboardFocus(actor);
	}

	/** Sets the scroll focus to the given child {@link Actor}. All subsequent scroll events will be passed to that actor. To
	 * unfocus an actor simply pass null.
	 * @param actor the Actor. */
	public void scrollFocus (Actor actor) {
		scrollFocusedActor = actor;
		if (parent != null) parent.scrollFocus(actor);
	}

	/** Clears this Group, removing all contained {@link Actor}s. */
	public void clear () {
		children.clear();
		groups.clear();
		namesToActors.clear();
		childrenChanged();
	}

	/** Sorts the children via the given {@link Comparator}.
	 * @param comparator the comparator. */
	public void sortChildren (Comparator<Actor> comparator) {
		Collections.sort(children, comparator);
	}

	/** Unfocuses all {@link Actor} instance currently focused for touch and keyboard events. You should call this in case your app
	 * resumes to clear up any pressed states. Make sure the Actors forget their states as well! This will be applied to all child
	 * groups recursively. */
	public void unfocusAll () {
		for (int i = 0; i < focusedActor.length; i++) {
			focusedActor[i] = null;
		}

		for (int i = 0; i < groups.size(); i++) {
			groups.get(i).unfocusAll();
		}

		keyboardFocusedActor = null;
		scrollFocusedActor = null;
	}

	/** Unfocuses this {@link Actor} from the touch, keyboard and scroll focus from this group and all groups above it.
	 * @param actor the Actor */
	public void unfocusAll (Actor actor) {
		for (int i = 0; i < focusedActor.length; i++) {
			if (focusedActor[i] == actor) {
				focus(null, i);
			}
		}

		if (keyboardFocusedActor == actor) keyboardFocus(null);
		if (scrollFocusedActor == actor) scrollFocus(null);
	}

	/** Transforms the coordinates given in the child's parent coordinate system to the child {@link Actor}'s coordinate system.
	 * @param child the child Actor
	 * @param x the x-coordinate in the Group's coordinate system
	 * @param y the y-coordinate in the Group's coordinate system
	 * @param out the output {@link Vector2} */
	static public void toChildCoordinates (Actor child, float x, float y, Vector2 out) {
		if (child.rotation == 0) {
			if (child.scaleX == 1 && child.scaleY == 1) {
				out.x = x - child.x;
				out.y = y - child.y;
			} else {
				if (child.originX == 0 && child.originY == 0) {
					out.x = (x - child.x) / child.scaleX;
					out.y = (y - child.y) / child.scaleY;
				} else {
					out.x = (x - child.x - child.originX) / child.scaleX + child.originX;
					out.y = (y - child.y - child.originY) / child.scaleY + child.originY;
				}
			}
		} else {
			final float cos = (float)Math.cos(child.rotation * MathUtils.degreesToRadians);
			final float sin = (float)Math.sin(child.rotation * MathUtils.degreesToRadians);

			if (child.scaleX == 1 && child.scaleY == 1) {
				if (child.originX == 0 && child.originY == 0) {
					float tox = x - child.x;
					float toy = y - child.y;

					out.x = tox * cos + toy * sin;
					out.y = tox * -sin + toy * cos;
				} else {
					final float worldOriginX = child.x + child.originX;
					final float worldOriginY = child.y + child.originY;
					float fx = -child.originX;
					float fy = -child.originY;

					float x1 = cos * fx - sin * fy;
					float y1 = sin * fx + cos * fy;
					x1 += worldOriginX;
					y1 += worldOriginY;

					float tox = x - x1;
					float toy = y - y1;

					out.x = tox * cos + toy * sin;
					out.y = tox * -sin + toy * cos;
				}
			} else {
				if (child.originX == 0 && child.originY == 0) {
					float tox = x - child.x;
					float toy = y - child.y;

					out.x = tox * cos + toy * sin;
					out.y = tox * -sin + toy * cos;

					out.x /= child.scaleX;
					out.y /= child.scaleY;
				} else {
					float srefX = child.originX * child.scaleX;
					float srefY = child.originY * child.scaleY;

					final float worldOriginX = child.x + child.originX;
					final float worldOriginY = child.y + child.originY;
					float fx = -srefX;
					float fy = -srefY;

					float x1 = cos * fx - sin * fy;
					float y1 = sin * fx + cos * fy;
					x1 += worldOriginX;
					y1 += worldOriginY;

					float tox = x - x1;
					float toy = y - y1;

					out.x = tox * cos + toy * sin;
					out.y = tox * -sin + toy * cos;

					out.x /= child.scaleX;
					out.y /= child.scaleY;
				}
			}
		}
	}

	static public void enableDebugging (String debugTextureFile) {
		debugTexture = new Texture(Gdx.files.internal(debugTextureFile), false);
		debug = true;
	}

	static public void disableDebugging () {
		if (debugTexture != null) debugTexture.dispose();
		debug = false;
	}
}
