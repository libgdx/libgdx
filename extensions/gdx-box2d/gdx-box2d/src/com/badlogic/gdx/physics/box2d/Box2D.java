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

package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.utils.SharedLibraryLoader;

/** This class's only purpose is to initialize Box2D by calling its {@link #init()} method.
 * @author Daniel Holderbaum */
public final class Box2D {

	private Box2D () {
	}

	/** Loads the Box2D native library and initializes the gdx-box2d extension. Must be called before any of the box2d
	 * classes/methods can be used. Currently with the exception of the {@link World} class, which will also cause the Box2D
	 * natives to be loaded. */
	public static void init () {
		new SharedLibraryLoader().load("gdx-box2d");
	}

}
