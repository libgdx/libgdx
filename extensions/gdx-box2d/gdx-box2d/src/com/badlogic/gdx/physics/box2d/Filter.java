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

/** This holds contact filtering data.
 * @author mzechner */
public class Filter {
	/** The collision category bits. Normally you would just set one bit. */
	public short categoryBits = 0x0001;

	/** The collision mask bits. This states the categories that this shape would accept for collision. */
	public short maskBits = -1;

	/** Collision groups allow a certain group of objects to never collide (negative) or always collide (positive). Zero means no
	 * collision group. Non-zero group filtering always wins against the mask bits. */
	public short groupIndex = 0;
}
