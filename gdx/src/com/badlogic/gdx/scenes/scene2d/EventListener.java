
package com.badlogic.gdx.scenes.scene2d;

/** Low level interface for receiving events. Typically there is a listener class for each specific event class.
 * @see ActorListener
 * @see ActorEvent */
public interface EventListener {
	public boolean handle (Event event);
}
