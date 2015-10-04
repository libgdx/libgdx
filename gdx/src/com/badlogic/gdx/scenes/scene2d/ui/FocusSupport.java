package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

public final class FocusSupport {
	private FocusSupport() {}

	public static <T extends Actor & Focusable> T getNextFocusActor(Focusable focusable) {
		T result = focusable.getNextKeyFocusActor();
		while (result != null && result.isDisabled()) {
			result = result.getNextKeyFocusActor();
		}
		return result;
	}
	public static <T extends Actor & Focusable> T getPreviousFocusActor(Focusable focusable) {
		T result = focusable.getPreviousKeyFocusActor();
		while (result != null && result.isDisabled()) {
			result = result.getPreviousKeyFocusActor();
		}
		return result;
	}
}
