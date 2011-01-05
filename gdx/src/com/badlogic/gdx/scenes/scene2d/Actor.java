/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.PooledLinkedList;

/**
 * <p>
 * An Actor is part of a {@link Stage} or a {@link Group} within a Stage. It has a position, a rectangular size given as width and
 * height, a rotation angle, a scale in x and y and an origin relative to the position which is used for rotation and scaling.
 * </p>
 * 
 * <p>
 * The position of an Actor is coincident with its unrotated, unscaled bottom left corner.
 * </p>
 * 
 * <p>
 * An Actor can be a child of a Group or the Stage. The object it belongs to is called the Actor's parent. An Actor's position is
 * always relative to the bottom left corner of its parent.
 * </p>
 * 
 * <p>
 * Every Actor must have a unique name within a Stage.
 * </p>
 * 
 * <p>
 * An Actor can receive touch events when its {@link #touchable} attribute is set to true. The Stage will delegate touch events to
 * an Actor in this case, calling its {@link #touchDown(float, float, int)}, {@link #touchUp(float, float, int)} and
 * {@link #touchDragged(float, float, int)} methods. The coordinates passed to an Actor will be in the Actor's coordinate system,
 * easing the pain of intersection testing. The coordinate system has it's origin at the Actor's rotated and scaled bottom left
 * corner, with the x-axis pointing to the right and the y-axis pointing to the left.
 * </p>
 * 
 * <p>
 * An Actor can be intersection tested via a call this its {@link #hit(float, float)} method. The coordinates given are again in
 * the Actor's coordinate system.
 * </p>
 * 
 * <p>
 * An Actor might render itself when its {@link #render(SpriteBatch)} method is called. The projection and transform matrices are
 * setup so that an Actor can simply call the
 * {@link SpriteBatch#draw(com.badlogic.gdx.graphics.Texture, float, float, float, float, float, float, float, float, float, int, int, int, int, boolean, boolean)}
 * method to render himself. Using a {@link Sprite} instance is also an option. An Actor might decide to not render itself at all
 * or chose another way to render itself. For the later it has to call {@link SpriteBatch#end()} first, setup up all render states
 * needed to render itself, render itself and then call {@link SpriteBatch#begin()} again so that the rendering for other Actor's
 * is undisturbed. You have to know what you do if you want to try this.
 * </p>
 * 
 * <p>
 * An Actor can be controlled by {@link Action}s. An Action will modify some of the attributes of an Actor such as its position or
 * rotation. Actions can be chained and make for rather sophisticated time based behaviour.
 * <p>
 * 
 * @author mzechner
 * 
 */
public abstract class Actor {
	public Group parent;
	public final String name;
	public boolean touchable = true;

	public float x;
	public float y;
	public float width;
	public float height;
	public float originX;
	public float originY;
	public float scaleX = 1;
	public float scaleY = 1;
	public float rotation;
	public final Color color = new Color(1, 1, 1, 1);

	protected PooledLinkedList<Action> actions = new PooledLinkedList<Action>(10);

	public Actor (String name) {
		this.name = name;
	}

	/**
	 * Renders the Actor. The spriteBatch is configured so that the Actor can render in its parents coordinate system.
	 * 
	 * @param batch the spritebatch to render with
	 */
	protected abstract void render (SpriteBatch batch);

	protected abstract boolean touchDown (float x, float y, int pointer);

	protected abstract boolean touchUp (float x, float y, int pointer);

	protected abstract boolean touchDragged (float x, float y, int pointer);

	public abstract Actor hit (float x, float y);

	/**
	 * Transforms the given point in stage coordinates to the Actor's local coordinate system.
	 * @param point the point
	 */
	public void toLocalCoordinates (Vector2 point) {
		if (parent == null) return;

		parent.toLocalCoordinates(point);
		Group.toChildCoordinates(this, point.x, point.y, point);
	}

	/**
	 * Removes this actor from the Stage
	 */
	public void remove () {
		parent.removeActor(this);
	}

	protected void act (float delta) {
		actions.iter();
		Action action;

		while ((action = actions.next()) != null) {
			action.act(delta);
			if (action.isDone()) {
				action.finish();
				actions.remove();
			}
		}
	}

	/**
	 * Adds an {@link Action} to the Actor. Actions will be automatically performed in the order added to the Actor and will be
	 * removed when they are done.
	 * 
	 * @param action the action
	 */
	public void action (Action action) {
		action.setTarget(this);
		actions.add(action);
	}

	/**
	 * Clears all actions of this Actor.
	 */
	public void clearActions () {
		actions.clear();
	}

	public String toString () {
		return name + ": [x=" + x + ", y=" + y + ", refX=" + originX + ", refY=" + originY + ", width=" + width + ", height="
			+ height + "]";
	}

}
