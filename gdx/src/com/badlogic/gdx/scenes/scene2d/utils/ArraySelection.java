
package com.badlogic.gdx.scenes.scene2d.utils;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;

/** A selection that supports range selection by knowing about the array of items being selected.
 * @author Nathan Sweet */
public class ArraySelection<T> extends Selection<T> {
	private Array<T> array;
	private boolean rangeSelect = true;

	public ArraySelection (Array<T> array) {
		this.array = array;
	}

	public void choose (T item) {
		if (item == null) throw new IllegalArgumentException("item cannot be null.");
		if (isDisabled) return;
		if (selected.size > 0 && rangeSelect && multiple
			&& (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))) {
			int low = array.indexOf(getLastSelected(), false);
			int high = array.indexOf(item, false);
			if (low > high) {
				int temp = low;
				low = high;
				high = temp;
			}
			snapshot();
			if (!UIUtils.ctrl()) selected.clear();
			for (; low <= high; low++)
				selected.add(array.get(low));
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

	/** Removes objects from the selection that are no longer in the items array. If {@link #getRequired()} is true and there is no
	 * selected item, the first item is selected. */
	public void validate () {
		Array<T> array = this.array;
		if (array.size == 0) {
			clear();
			return;
		}
		for (Iterator<T> iter = items().iterator(); iter.hasNext();) {
			T selected = iter.next();
			if (!array.contains(selected, false)) iter.remove();
		}
		if (required && selected.size == 0) set(array.first());
	}
}
