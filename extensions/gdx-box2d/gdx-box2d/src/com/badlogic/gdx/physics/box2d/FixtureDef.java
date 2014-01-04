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

/** A fixture definition is used to create a fixture. This class defines an abstract fixture definition. You can reuse fixture
 * definitions safely.
 * @author mzechner */
public class FixtureDef {
	/** The shape, this must be set. The shape will be cloned, so you can create the shape on the stack. */
	public Shape shape;

	/** The friction coefficient, usually in the range [0,1]. **/
	public float friction = 0.2f;

	/** The restitution (elasticity) usually in the range [0,1]. **/
	public float restitution = 0;

	/** The density, usually in kg/m^2. **/
	public float density = 0;

	/** A sensor shape collects contact information but never generates a collision response. */
	public boolean isSensor = false;

	/** Contact filtering data. **/
	public final Filter filter = new Filter();
}
