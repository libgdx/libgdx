
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.utils.Pool;

/**
 * A pool taking care of the {@link Action} life cycle and resets all its properties when obtained from this pool.
 * 
 * @author Moritz Post <moritzpost@gmail.com>
 * @param <T> the type action to manage
 */
abstract class ActionResetingPool<T extends Action> extends Pool<T> {

	public ActionResetingPool (int initialCapacity, int max) {
		super(initialCapacity, max);
	}

	@Override public T obtain () {
		T elem = super.obtain();
		elem.reset();
		return elem;
	}
}
