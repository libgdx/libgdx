
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;

/** A selection that supports range selection by knowing about the array of items being selected.
 * @author Nathan Sweet */
public class ArraySelection<T> extends Selection<T> {
	private Array<T> items;
	private boolean rangeSelect = true;

	public ArraySelection (Array<T> items) {
		this.items = items;
	}

	public void choose (T item) {
		if (item == null) throw new IllegalArgumentException("item cannot be null.");
		if (isDisabled) return;
		if (selected.size > 0 && rangeSelect && multiple
			&& (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))) {
			int low = items.indexOf(getLastSelected(), false);
			int high = items.indexOf(item, false);
			if (low > high) {
				int temp = low;
				low = high;
				high = temp;
			}
			snapshot();
			if (!UIUtils.ctrl()) selected.clear();
			for (; low <= high; low++)
				selected.add(items.get(low));
			if (fireChangeEvent()) revert();
			cleanup();
			return;
		}
		super.choose(item);
	}

	public boolean getRangeSelect () {
		return rangeSelect;
	}

	public void setRangeSelect (boolean rangeSelect) {
		this.rangeSelect = rangeSelect;
	}
}
