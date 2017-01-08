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

package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** A value which contains a single float variable.
 * @author Inferno */
public class NumericValue extends ParticleValue {
	private float value;

	public float getValue () {
		return value;
	}

	public void setValue (float value) {
		this.value = value;
	}

	public void load (NumericValue value) {
		super.load(value);
		this.value = value.value;
	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("value", value);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		value = json.readValue("value", float.class, jsonData);
	}

}
