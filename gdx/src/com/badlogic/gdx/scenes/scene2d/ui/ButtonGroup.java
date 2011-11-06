
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.utils.Array;

/** Manages a group of buttons to enforce a minimum and maximum number of checked buttons. This enables "radio button"
 * functionality and more.
 * @author Nathan Sweet */
public class ButtonGroup {
	private final Array<Button> buttons = new Array();
	private Array<Button> checkedButtons = new Array(1);
	private int minCheckCount = 1, maxCheckCount = 1;
	private boolean uncheckLast = true;
	private Button lastChecked;
	private ClickListener listener;

	public ButtonGroup () {
	}

	public ButtonGroup (Button... buttons) {
		add(buttons);
	}

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

	/** Sets the first button with the specified {@link Button#getText() text} to checked. */
	public void setChecked (String text) {
		if (text == null) throw new IllegalArgumentException("text cannot be null.");
		for (int i = 0, n = buttons.size; i < n; i++) {
			Button button = buttons.get(i);
			if (text.equals(button.getText())) {
				button.setChecked(true);
				return;
			}
		}
	}

	/** Called when a button is checked or unchecked.
	 * @return true if the new state should be allowed. */
	protected boolean canCheck (Button button, boolean newState) {
		if (button.isChecked == newState) return false;

		if (!newState) {
			// Keep button checked to enforce minCheckCount.
			if (checkedButtons.size <= minCheckCount) return false;
			checkedButtons.removeValue(button, true);
		} else {
			// Keep button unchecked to enforce maxCheckCount.
			if (maxCheckCount != -1 && checkedButtons.size >= maxCheckCount) {
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

		if (listener != null) listener.click(button, 0, 0);

		return true;
	}

	/** Sets all buttons' {@link Button#isChecked()} to false, regardless of {@link #setMinCheckCount(int)}. */
	public void uncheckAll () {
		int old = minCheckCount;
		minCheckCount = 0;
		for (int i = 0, n = buttons.size; i < n; i++) {
			Button button = buttons.get(i);
			button.setChecked(false);
		}
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

	public Array<Button> getButtons () {
		return buttons;
	}

	/** Sets the minimum number of buttons that must be checked. Default is 1. */
	public void setMinCheckCount (int minCheckCount) {
		this.minCheckCount = minCheckCount;
	}

	/** Sets the maximum number of buttons that can be checked. Set to -1 for no maximum. Default is 1. */
	public void setMaxCheckCount (int maxCheckCount) {
		this.maxCheckCount = maxCheckCount;
	}

	/** Sets a listener that is invoked whenever a button is checked or unchecked.
	 * @param listener May be null. */
	public void setClickListener (ClickListener listener) {
		this.listener = listener;
	}

	/** If true, when the maximum number of buttons are checked and an additional button is checked, the last button to be checked
	 * is unchecked so that the maximum is not exceeded. If false, additional buttons beyond the maximum are not allowed to be
	 * checked. Default is true. */
	public void setUncheckLast (boolean uncheckLast) {
		this.uncheckLast = uncheckLast;
	}
}
