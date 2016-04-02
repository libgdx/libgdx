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

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/** This class allows to reduce the static allocation needed for shape builders. It contains all the objects used internally by
 * shape builders.
 * @author realitix */
public class BaseShapeBuilder {
	/** Color */
	protected static final Color tmpColor0 = new Color();
	protected static final Color tmpColor1 = new Color();
	protected static final Color tmpColor2 = new Color();
	protected static final Color tmpColor3 = new Color();
	protected static final Color tmpColor4 = new Color();

	/** Vector3 */
	protected static final Vector3 tmpV0 = new Vector3();
	protected static final Vector3 tmpV1 = new Vector3();
	protected static final Vector3 tmpV2 = new Vector3();
	protected static final Vector3 tmpV3 = new Vector3();
	protected static final Vector3 tmpV4 = new Vector3();
	protected static final Vector3 tmpV5 = new Vector3();
	protected static final Vector3 tmpV6 = new Vector3();
	protected static final Vector3 tmpV7 = new Vector3();
}
