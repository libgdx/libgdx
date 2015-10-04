package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;

public interface Focusable extends Disableable {
	/**
	 * Returns if focusing is disabled for this instance
	 */
	boolean isFocusTraversal();
	/**
	 * Enables or disables focusing for this instance
	 */
	void setFocusTraversal(boolean focusTraversal);

	/**
	 * Returns the actor that will be focused after pressing the "focus next actor" shortcut (i.e. TAB)
	 */
	<T extends Actor & Focusable> T getNextKeyFocusActor();

	/**
	 * Sets the actor that will be focused after pressing the "focus next actor" shortcut (i.e. TAB)
	 *
	 * @see Focusable#chainKeyFocusActor(Actor)
	 */
	<T extends Actor & Focusable> void setNextKeyFocusActor(T actor);
	/**
	 * Returns the actor that will be focused after pressing the "focus previous actor" shortcut (i.e. SHIFT+TAB)
	 */
	<T extends Actor & Focusable> T getPreviousKeyFocusActor();
	/**
	 * Sets the actor that will be focused after pressing the "focus previous actor" shortcut (i.e. SHIFT+TAB)
	 *
	 * @see Focusable#chainKeyFocusActor(Actor)
	 */
	<T extends Actor & Focusable> void setPreviousKeyFocusActor(T actor);

	/**
	 * Modifies this and {@code nextActor} to be chained. The {@code nextKeyFocusActor} for this instance will be {@code nextActor},
	 * and the {@code previousKeyFocusActor} for {@code nextActor} will be this instance
	 */
	<T extends Actor & Focusable> void chainKeyFocusActor(T nextActor);
}
