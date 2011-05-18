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
package com.badlogic.gdx.graphics.g3d.loaders.md5;

public class MD5AnimationInfo {
	int currFrame = 0;
	int nextFrame = 1;
	int maxFrame;

	float lastTime;
	float maxTime;

	public MD5AnimationInfo (int maxFrame, float maxTime) {
		this.maxFrame = maxFrame;
		this.maxTime = maxTime;
	}

	public void reset () {
		reset(maxFrame, maxTime);
	}

	public void reset (int maxFrame, float maxTime) {
		this.maxFrame = maxFrame;
		this.maxTime = maxTime;
		this.currFrame = 0;
		this.nextFrame = 1;
		this.lastTime = 0;
	}

	public void update (float delta) {
		lastTime += delta;

		if (lastTime >= maxTime) {
			currFrame++;
			nextFrame++;
			lastTime = 0;

			if (currFrame >= maxFrame) currFrame = 0;
			if (nextFrame >= maxFrame) nextFrame = 0;
		}
	}

	public int getCurrentFrame () {
		return currFrame;
	}

	public int getNextFrame () {
		return nextFrame;
	}

	public float getInterpolation () {
		return lastTime / maxTime;
	}
}
