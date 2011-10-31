
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

/** Callback for when a value in an actor is selected.
 * @author mzechner */
public interface SelectionListener {
	public void selected (Actor actor, int index, String value);
}
