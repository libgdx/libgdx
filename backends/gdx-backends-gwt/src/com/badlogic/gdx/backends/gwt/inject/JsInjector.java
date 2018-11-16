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

package com.badlogic.gdx.backends.gwt.inject;

/** Stub interface, here to trigger the {@link JsInjectorGenerator} so we can get all .js files and inject them
 * @author Simon Gerst */
public interface JsInjector {
	/** @return returns all injectables i.e. classes that implement {@link Injectable} */
	Injectable[] getInjectables ();

	/** Represents a .js script that needs to be injected, it is ensured that the script is injected. See {@link FreetypeInjector}
	 * in gdx-freetype-gwt for an example. You can only use GWT in those methods, you can not use libgdx! */
	public static interface Injectable {
		/** @return return true if the injection succeeded */
		boolean isSuccess ();

		/** @return return true if the injection failed */
		boolean isError ();

		/** This method will be called by GWT and will inject the js you specified */
		void inject ();
	}
}
