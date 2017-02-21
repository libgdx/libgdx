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

/** Defines a variation of red, green and blue on a given time line.
 * @author Inferno */
public class GradientColorValue extends ParticleValue {
	static private float[] temp = new float[3];

	private float[] colors = {1, 1, 1};
	public float[] timeline = {0};

	public float[] getTimeline () {
		return timeline;
	}

	public void setTimeline (float[] timeline) {
		this.timeline = timeline;
	}

	public float[] getColors () {
		return colors;
	}

	public void setColors (float[] colors) {
		this.colors = colors;
	}

	public float[] getColor (float percent) {
		getColor(percent, temp, 0);
		return temp;
	}

	public void getColor (float percent, float[] out, int index) {
		int startIndex = 0, endIndex = -1;
		float[] timeline = this.timeline;
		int n = timeline.length;
		for (int i = 1; i < n; i++) {
			float t = timeline[i];
			if (t > percent) {
				endIndex = i;
				break;
			}
			startIndex = i;
		}
		float startTime = timeline[startIndex];
		startIndex *= 3;
		float r1 = colors[startIndex];
		float g1 = colors[startIndex + 1];
		float b1 = colors[startIndex + 2];
		if (endIndex == -1) {
			out[index] = r1;
			out[index + 1] = g1;
			out[index + 2] = b1;
			return;
		}
		float factor = (percent - startTime) / (timeline[endIndex] - startTime);
		endIndex *= 3;
		out[index] = r1 + (colors[endIndex] - r1) * factor;
		out[index + 1] = g1 + (colors[endIndex + 1] - g1) * factor;
		out[index + 2] = b1 + (colors[endIndex + 2] - b1) * factor;
	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("colors", colors);
		json.writeValue("timeline", timeline);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		colors = json.readValue("colors", float[].class, jsonData);
		timeline = json.readValue("timeline", float[].class, jsonData);
	}

	public void load (GradientColorValue value) {
		super.load(value);
		colors = new float[value.colors.length];
		System.arraycopy(value.colors, 0, colors, 0, colors.length);
		timeline = new float[value.timeline.length];
		System.arraycopy(value.timeline, 0, timeline, 0, timeline.length);
	}
}
