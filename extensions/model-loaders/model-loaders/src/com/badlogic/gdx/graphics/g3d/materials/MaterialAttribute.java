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

package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Pool;

public abstract class MaterialAttribute {
	private static final String FLAG = "Flag";
	public String name;
	protected final boolean isPooled;

	protected MaterialAttribute() {
		isPooled = true;
	}
	
	public MaterialAttribute (String name) {
		this.name = name;
		isPooled = false;
	}

	public abstract void bind ();

	public abstract void bind (ShaderProgram program);

	public abstract MaterialAttribute copy ();
	
	public abstract MaterialAttribute pooledCopy();
	
	public abstract void free();
	
	public abstract void set(MaterialAttribute attr);
	
	public String getShaderFlag () {
		return name + FLAG;
	}
}
