
package com.badlogic.gdx.graphics.loaders.md5;

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
