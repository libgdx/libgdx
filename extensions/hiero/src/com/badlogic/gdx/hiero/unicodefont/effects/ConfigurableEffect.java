
package com.badlogic.gdx.hiero.unicodefont.effects;

import java.util.List;

/**
 * An effect that has a number of configuration values. This allows the effect to be configured in the Hiero GUI and to be saved
 * and loaded to and from a file.
 * @author Nathan Sweet <misc@n4te.com>
 */
public interface ConfigurableEffect extends Effect {
	/**
	 * Returns the list of {@link Value}s for this effect. This list is not typically backed by the effect, so changes to the
	 * values will not take affect until {@link #setValues(List)} is called.
	 */
	public List getValues ();

	/**
	 * Sets the list of {@link Value}s for this effect.
	 */
	public void setValues (List values);

	/**
	 * Represents a configurable value for an effect.
	 */
	static public interface Value {
		/**
		 * Returns the name of the value.
		 */
		public String getName ();

		/**
		 * Sets the string representation of the value.
		 */
		public void setString (String value);

		/**
		 * Gets the string representation of the value.
		 */
		public String getString ();

		/**
		 * Gets the object representation of the value.
		 */
		public Object getObject ();

		/**
		 * Shows a dialog allowing a user to configure this value.
		 */
		public void showDialog ();
	}
}
