
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.utils.Array;

/** @author Nathan Sweet */
public class ButtonGroup {
	private final Array<Button> buttons = new Array();
	private Button checkedButton;
	private boolean allowNoneChecked;
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
	protected void setChecked (Button button, boolean isChecked) {
		Button oldCheckedButton = checkedButton;
		if (isChecked) {
			this.checkedButton = button;
			if (button != oldCheckedButton) {
				uncheckAllExcept(button);
				if (listener != null) listener.click(button);
			}
		} else {
			if (button == oldCheckedButton && !allowNoneChecked) button.setChecked(true);
		}
	}

	protected void uncheckAllExcept (Button except) {
		for (int i = 0, n = buttons.size; i < n; i++) {
			Button button = buttons.get(i);
			if (button == except) continue;
			button.setChecked(false);
		}
	}

	/** Sets all buttons' {@link Button#isChecked()} to false, regardless of {@link #setAllowNoneChecked(boolean)}. */
	public void uncheckAll () {
		boolean old = allowNoneChecked;
		allowNoneChecked = true;
		uncheckAllExcept(null);
		allowNoneChecked = old;
	}

	/** @return the checked button, or null. */
	public Button getChecked () {
		return checkedButton;
	}

	/** Default is false; */
	public void setAllowNoneChecked (boolean allowNoneChecked) {
		this.allowNoneChecked = allowNoneChecked;
	}

	public void setClickListener (ClickListener listener) {
		this.listener = listener;
	}
}
