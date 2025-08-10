
package com.badlogic.gdx.scenes.scene2d.ui;

/** This interface marks an Actor as Styleable
 * @param <T> The Style object type */
public interface Styleable<T> {

	/** Get the current style of the actor */
	T getStyle ();

	/** Set the current style of the actor */
	void setStyle (T style);
}
