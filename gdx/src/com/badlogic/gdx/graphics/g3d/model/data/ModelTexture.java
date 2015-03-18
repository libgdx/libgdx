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

package com.badlogic.gdx.graphics.g3d.model.data;

import com.badlogic.gdx.math.Vector2;

public class ModelTexture {
	public final static int USAGE_UNKNOWN = 0;
	public final static int USAGE_NONE = 1;
	public final static int USAGE_DIFFUSE = 2;
	public final static int USAGE_EMISSIVE = 3;
	public final static int USAGE_AMBIENT = 4;
	public final static int USAGE_SPECULAR = 5;
	public final static int USAGE_SHININESS = 6;
	public final static int USAGE_NORMAL = 7;
	public final static int USAGE_BUMP = 8;
	public final static int USAGE_TRANSPARENCY = 9;
	public final static int USAGE_REFLECTION = 10;

	public String id;
	public String fileName;
	public Vector2 uvTranslation;
	public Vector2 uvScaling;
	public int usage;
}
