package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** A value which has a defined minimum and maximum upper and lower bounds.
 * Defines the variations of the value on a time line. 
 * @author Inferno */
public class ScaledNumericValue extends RangedNumericValue {
	private float[] scaling = {1};
	public float[] timeline = {0};
	private float highMin, highMax;
	private boolean relative = false;

	public float newHighValue () {
		return highMin + (highMax - highMin) * MathUtils.random();
	}

	public void setHigh (float value) {
		highMin = value;
		highMax = value;
	}

	public void setHigh (float min, float max) {
		highMin = min;
		highMax = max;
	}

	public float getHighMin () {
		return highMin;
	}

	public void setHighMin (float highMin) {
		this.highMin = highMin;
	}

	public float getHighMax () {
		return highMax;
	}

	public void setHighMax (float highMax) {
		this.highMax = highMax;
	}

	public float[] getScaling () {
		return scaling;
	}

	public void setScaling (float[] values) {
		this.scaling = values;
	}

	public float[] getTimeline () {
		return timeline;
	}

	public void setTimeline (float[] timeline) {
		this.timeline = timeline;
	}

	public boolean isRelative () {
		return relative;
	}

	public void setRelative (boolean relative) {
		this.relative = relative;
	}

	public float getScale (float percent) {
		int endIndex = -1;
		int n = timeline.length;
		//if (percent >= timeline[n-1]) 
		//	return scaling[n - 1];
		for (int i = 1; i < n; i++) {
			float t = timeline[i];
			if (t > percent) {
				endIndex = i;
				break;
			}
		}
		if (endIndex == -1) return scaling[n - 1];
		int startIndex = endIndex - 1;
		float startValue = scaling[startIndex];
		float startTime = timeline[startIndex];
		return startValue + (scaling[endIndex] - startValue) * ((percent - startTime) / (timeline[endIndex] - startTime));
	}

	public void load (ScaledNumericValue value) {
		super.load(value);
		highMax = value.highMax;
		highMin = value.highMin;
		scaling = new float[value.scaling.length];
		System.arraycopy(value.scaling, 0, scaling, 0, scaling.length);
		timeline = new float[value.timeline.length];
		System.arraycopy(value.timeline, 0, timeline, 0, timeline.length);
		relative = value.relative;
	}
	
	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("highMin", highMin);
		json.writeValue("highMax", highMax);
		json.writeValue("relative", relative);
		json.writeValue("scaling", scaling);
		json.writeValue("timeline", timeline);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		highMin = json.readValue("highMin", float.class, jsonData);
		highMax = json.readValue("highMax", float.class, jsonData);
		relative = json.readValue("relative", boolean.class, jsonData);
		scaling = json.readValue("scaling", float[].class, jsonData);
		timeline = json.readValue("timeline", float[].class, jsonData);
	}
	
	
}