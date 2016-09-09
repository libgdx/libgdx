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

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** The base class of all the {@link SpawnShapeValue} values which spawn the particles on a geometric primitive.
 * @author Inferno */
public abstract class PrimitiveSpawnShapeValue extends SpawnShapeValue {
	protected static final Vector3 TMP_V1 = new Vector3();

	static public enum SpawnSide {
		both, top, bottom
	}

	public ScaledNumericValue spawnWidthValue, spawnHeightValue, spawnDepthValue;
	protected float spawnWidth, spawnWidthDiff;
	protected float spawnHeight, spawnHeightDiff;
	protected float spawnDepth, spawnDepthDiff;
	boolean edges = false;

	public PrimitiveSpawnShapeValue () {
		spawnWidthValue = new ScaledNumericValue();
		spawnHeightValue = new ScaledNumericValue();
		spawnDepthValue = new ScaledNumericValue();
	}

	public PrimitiveSpawnShapeValue (PrimitiveSpawnShapeValue value) {
		super(value);
		spawnWidthValue = new ScaledNumericValue();
		spawnHeightValue = new ScaledNumericValue();
		spawnDepthValue = new ScaledNumericValue();
	}

	@Override
	public void setActive (boolean active) {
		super.setActive(active);
		spawnWidthValue.setActive(true);
		spawnHeightValue.setActive(true);
		spawnDepthValue.setActive(true);
	}

	public boolean isEdges () {
		return edges;
	}

	public void setEdges (boolean edges) {
		this.edges = edges;
	}

	public ScaledNumericValue getSpawnWidth () {
		return spawnWidthValue;
	}

	public ScaledNumericValue getSpawnHeight () {
		return spawnHeightValue;
	}

	public ScaledNumericValue getSpawnDepth () {
		return spawnDepthValue;
	}

	public void setDimensions (float width, float height, float depth) {
		spawnWidthValue.setHigh(width);
		spawnHeightValue.setHigh(height);
		spawnDepthValue.setHigh(depth);
	}

	@Override
	public void start () {
		spawnWidth = spawnWidthValue.newLowValue();
		spawnWidthDiff = spawnWidthValue.newHighValue();
		if (!spawnWidthValue.isRelative()) spawnWidthDiff -= spawnWidth;

		spawnHeight = spawnHeightValue.newLowValue();
		spawnHeightDiff = spawnHeightValue.newHighValue();
		if (!spawnHeightValue.isRelative()) spawnHeightDiff -= spawnHeight;

		spawnDepth = spawnDepthValue.newLowValue();
		spawnDepthDiff = spawnDepthValue.newHighValue();
		if (!spawnDepthValue.isRelative()) spawnDepthDiff -= spawnDepth;
	}

	@Override
	public void load (ParticleValue value) {
		super.load(value);
		PrimitiveSpawnShapeValue shape = (PrimitiveSpawnShapeValue)value;
		edges = shape.edges;
		spawnWidthValue.load(shape.spawnWidthValue);
		spawnHeightValue.load(shape.spawnHeightValue);
		spawnDepthValue.load(shape.spawnDepthValue);
	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("spawnWidthValue", spawnWidthValue);
		json.writeValue("spawnHeightValue", spawnHeightValue);
		json.writeValue("spawnDepthValue", spawnDepthValue);
		json.writeValue("edges", edges);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		spawnWidthValue = json.readValue("spawnWidthValue", ScaledNumericValue.class, jsonData);
		spawnHeightValue = json.readValue("spawnHeightValue", ScaledNumericValue.class, jsonData);
		spawnDepthValue = json.readValue("spawnDepthValue", ScaledNumericValue.class, jsonData);
		edges = json.readValue("edges", boolean.class, jsonData);
	}

}
