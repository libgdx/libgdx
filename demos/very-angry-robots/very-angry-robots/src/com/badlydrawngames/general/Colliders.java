/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
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

package com.badlydrawngames.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlydrawngames.veryangryrobots.mobiles.GameObject;

/** Functions that are useful for collision detection.
 * @author Rod */
public final class Colliders {

	public static interface RemovalHandler<T extends GameObject> {
		void onRemove (T t);
	}

	public static interface ColliderHandler<T extends GameObject, U extends GameObject> {
		void onCollision (T t, U u);
	}

	public static interface SceneryHandler<T extends GameObject> {
		void onCollision (T t, Rectangle r);
	}

	private Colliders () {
	}

	/** Returns true if two rectangles intersect.
	 * @param a the first rectangle.
	 * @param b the second rectangle.
	 * @return true if the rectangles intersect, otherwise false. */
	public static boolean intersects (Rectangle a, Rectangle b) {
		return (a.x + a.width > b.x) && (a.x < b.x + b.width) && (a.y + a.height > b.y) && (a.y < b.y + b.height);
	}

	/** Returns true if a rectangle intersects with any rectangles in an array.
	 * @param a the rectangle.
	 * @param rects the array of rectangles.
	 * @return true if they intersect, otherwise false. */
	public static boolean intersects (Rectangle a, Array<Rectangle> rects) {
		for (int i = rects.size - 1; i >= 0; i--) {
			Rectangle b = rects.get(i);
			if (intersects(a, b)) {
				return true;
			}
		}
		return false;
	}

	public static <V extends GameObject, W extends GameObject, T extends V, U extends W> void collide (T a, U b,
		ColliderHandler<V, W> callback) {
		if (a != b) {
			if (a.intersects(b)) {
				callback.onCollision(a, b);
			}
		}
	}

	public static <V extends GameObject, W extends GameObject, T extends V, U extends W> void collide (T go, Array<U> gos,
		ColliderHandler<V, W> callback) {
		for (int i = gos.size - 1; i >= 0; i--) {
			U other = gos.get(i);
			if (go.intersects(other)) {
				callback.onCollision(go, other);
			}
		}
	}

	public static <U extends GameObject, T extends U> void collide (Array<T> a, ColliderHandler<U, U> callback) {
		for (int i = a.size - 1; i >= 0; i--) {
			T go = a.get(i);
			for (int j = i - 1; j >= 0; j--) {
				T other = a.get(j);
				if (go.intersects(other)) {
					callback.onCollision(go, other);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <V extends GameObject, W extends GameObject, T extends V, U extends W> void collide (Array<T> a, Array<U> b,
		ColliderHandler<V, W> callback) {
		if (a != b) {
			for (int i = a.size - 1; i >= 0; i--) {
				T go = a.get(i);
				collide(go, b, callback);
			}
		} else {
			collide(a, (ColliderHandler<V, V>)callback);
		}
	}

	public static <U extends GameObject, T extends U> void collide (Array<T> gos, Array<Rectangle> rects,
		SceneryHandler<U> callback) {
		for (int i = gos.size - 1; i >= 0; i--) {
			T go = gos.get(i);
			collide(go, rects, callback);
		}
	}

	public static <U extends GameObject, T extends U> void collide (T go, Array<Rectangle> rects, SceneryHandler<U> callback) {
		for (int i = 0; i < rects.size && !go.inCollision; i++) {
			Rectangle r = rects.get(i);
			if (go.intersects(r)) {
				callback.onCollision(go, r);
			}
		}
	}

	/** Removes game objects that are marked as in collision, calling a removal handler for each one that is marked. Note that the
	 * "U extends T" part of this method's signature means, for example, that PlayerShot and RobotShot can both have the same
	 * handler if they are both derived from BaseShot.
	 * 
	 * @param <T> the base class of the object.
	 * @param <U> a more specific class of the object.
	 * @param pool the pool to which the object belongs.
	 * @param gos the array of game objects to check.
	 * @param handler the collision handler callback. */
	public static <T extends GameObject, U extends T> void removeMarkedCollisions (Pool<U> pool, Array<U> gos,
		RemovalHandler<T> handler) {
		// The "U extends T" allows for shotHandler to cope with both PlayerShot and RobotShot, because they both
		// extend BaseShot.
		for (int i = gos.size - 1; i >= 0; i--) {
			U go = gos.get(i);
			if (go.inCollision) {
				handler.onRemove(go);
				gos.removeIndex(i);
				pool.free(go);
			}
		}
	}

	/** Removes game objects that are outside of the given rectangular bounds.
	 * @param <T> the object's class.
	 * @param pool the pool to which the object belongs.
	 * @param gos the array of game objects to check.
	 * @param bounds the rectangular bounds. */
	public static <T extends GameObject> void removeOutOfBounds (Pool<T> pool, Array<T> gos, Rectangle bounds) {
		float minX = bounds.x;
		float maxX = minX + bounds.width;
		float minY = bounds.y;
		float maxY = minY + bounds.height;
		for (int i = gos.size - 1; i >= 0; i--) {
			T go = gos.get(i);
			if (go.x >= maxX || go.x + go.width <= minX || go.y >= maxY || go.y + go.height <= minY) {
				gos.removeIndex(i);
				pool.free(go);
			}
		}
	}
}
