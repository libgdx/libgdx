/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tools.hiero.unicodefont.effects;

import java.util.List;

/** An effect that has a number of configuration values. This allows the effect to be configured in the Hiero GUI and to be saved
 * and loaded to and from a file.
 * @author Nathan Sweet */
public interface ConfigurableEffect extends Effect {
	/** Returns the list of {@link Value}s for this effect. This list is not typically backed by the effect, so changes to the
	 * values will not take affect until {@link #setValues(List)} is called. */
	public List getValues ();

	/** Sets the list of {@link Value}s for this effect. */
	public void setValues (List values);

	/** Represents a configurable value for an effect. */
	static public interface Value {
		/** Returns the name of the value. */
		public String getName ();

		/** Sets the string representation of the value. */
		public void setString (String value);

		/** Gets the string representation of the value. */
		public String getString ();

		/** Gets the object representation of the value. */
		public Object getObject ();

		/** Shows a dialog allowing a user to configure this value. */
		public void showDialog ();
	}
}
