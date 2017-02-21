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

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** Encapsulate the formulas to spawn a particle on a shape.
 * @author Inferno */
public abstract class SpawnShapeValue extends ParticleValue implements ResourceData.Configurable, Json.Serializable {

	public RangedNumericValue xOffsetValue, yOffsetValue, zOffsetValue;

	public SpawnShapeValue () {
		xOffsetValue = new RangedNumericValue();
		yOffsetValue = new RangedNumericValue();
		zOffsetValue = new RangedNumericValue();
	}

	public SpawnShapeValue (SpawnShapeValue spawnShapeValue) {
		this();
	}

	public abstract void spawnAux (Vector3 vector, float percent);

	public final Vector3 spawn (Vector3 vector, float percent) {
		spawnAux(vector, percent);
		if (xOffsetValue.active) vector.x += xOffsetValue.newLowValue();
		if (yOffsetValue.active) vector.y += yOffsetValue.newLowValue();
		if (zOffsetValue.active) vector.z += zOffsetValue.newLowValue();
		return vector;
	}

	public void init () {
	}

	public void start () {
	}

	@Override
	public void load (ParticleValue value) {
		super.load(value);
		SpawnShapeValue shape = (SpawnShapeValue)value;
		xOffsetValue.load(shape.xOffsetValue);
		yOffsetValue.load(shape.yOffsetValue);
		zOffsetValue.load(shape.zOffsetValue);
	}

	public abstract SpawnShapeValue copy ();

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("xOffsetValue", xOffsetValue);
		json.writeValue("yOffsetValue", yOffsetValue);
		json.writeValue("zOffsetValue", zOffsetValue);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		xOffsetValue = json.readValue("xOffsetValue", RangedNumericValue.class, jsonData);
		yOffsetValue = json.readValue("yOffsetValue", RangedNumericValue.class, jsonData);
		zOffsetValue = json.readValue("zOffsetValue", RangedNumericValue.class, jsonData);
	}

	@Override
	public void save (AssetManager manager, ResourceData data) {
	}

	@Override
	public void load (AssetManager manager, ResourceData data) {
	}

}
