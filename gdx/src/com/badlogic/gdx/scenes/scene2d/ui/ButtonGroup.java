
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.utils.Array;

/** @author Nathan Sweet */
public class ButtonGroup {
	private final Array<Button> buttons = new Array();
	private Array<Button> checkedButtons = new Array(1);
	private int minCheckCount = 1, maxCheckCount = 1;
	private boolean uncheckLast = true;
	private Button lastChecked;
	private ClickListener listener;

	public void add (Button button) {
		if (button == null) throw new IllegalArgumentException("button cannot be null.");
		button.buttonGroup = this;
		buttons.add(button);
		if (button.isChecked() || buttons.size == 0) button.setChecked(true);
	}

	public void add (Button... buttons) {
		if (buttons == null) throw new IllegalArgumentException("buttons cannot be null.");
		for (int i = 0, n = buttons.length; i < n; i++)
			add(buttons[i]);
	}

	public void setChecked (String text) {
		for (int i = 0, n = buttons.size; i < n; i++) {
			Button button = buttons.get(i);
			if (text.equals(button.getText())) {
				button.setChecked(true);
				return;
			}
		}
	}

	/** Called when a button is checked or unchecked. */
	protected boolean canCheck (Button button, boolean newState) {
		if (button.isChecked == newState) return false;

		if (!newState) {
			// Keep button checked to enforce minCheckCount.
			if (checkedButtons.size <= minCheckCount) return false;
			checkedButtons.removeValue(button, true);
		} else {
			// Keep button unchecked to enforce maxCheckCount.
			if (checkedButtons.size >= maxCheckCount) {
				if (uncheckLast) {
					int old = minCheckCount;
					minCheckCount = 0;
					lastChecked.setChecked(false);
					minCheckCount = old;
				} else
					return false;
			}
			checkedButtons.add(button);
			lastChecked = button;
		}

		if (listener != null) listener.click(button);

		return true;
	}

	protected void uncheckAllExcept (Button except) {
		for (int i = 0, n = buttons.size; i < n; i++) {
			Button button = buttons.get(i);
			if (button == except) continue;
			button.setChecked(false);
		}
	}

	/** Sets all buttons' {@link Button#isChecked()} to false, regardless of {@link #setMinCheckCount(int)}. */
	public void uncheckAll () {
		int old = minCheckCount;
		minCheckCount = 0;
		uncheckAllExcept(null);
		minCheckCount = old;
	}

	/** @return the first checked button, or null. */
	public Button getChecked () {
		if (checkedButtons.size > 0) checkedButtons.get(0);
		return null;
	}

	public Array<Button> getAllChecked () {
		return checkedButtons;
	}

	public void setMinCheckCount (int minCheckCount) {
		this.minCheckCount = minCheckCount;
	}

	public void setMaxCheckCount (int maxCheckCount) {
		this.maxCheckCount = maxCheckCount;
	}

	public void setClickListener (ClickListener listener) {
		this.listener = listener;
	}

	public void setUncheckLast (boolean uncheckLast) {
		this.uncheckLast = uncheckLast;
	}
}
