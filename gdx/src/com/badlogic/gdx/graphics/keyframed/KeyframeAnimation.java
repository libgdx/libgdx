/*
 * Copyright 2010 Dave Clayton (contact@redskyforge.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.badlogic.gdx.graphics.keyframed;

import com.badlogic.gdx.graphics.animation.Animation;

/**
 * 
 * @author Dave Clayton <contact@redskyforge.com>
 *
 */
public class KeyframeAnimation extends Animation {
	public String mName;
	public Keyframe[] mKeyframes;
	public float mLength;
	
	public KeyframeAnimation(String name, int frames, float length)
	{
		mName = name;
		mKeyframes = new Keyframe[frames];
		mLength = length;
	}

	@Override
	public float getLength() {
		return mLength;
	}

	@Override
	public int getNumFrames() {
		return mKeyframes.length;
	}
}
