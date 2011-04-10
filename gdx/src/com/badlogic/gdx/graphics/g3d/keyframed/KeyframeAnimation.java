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
package com.badlogic.gdx.graphics.g3d.keyframed;

import com.badlogic.gdx.graphics.g3d.Animation;

/**
 * 
 * @author Dave Clayton <contact@redskyforge.com>
 *
 */
public class KeyframeAnimation extends Animation {
	public String name;
	public Keyframe[] keyframes;
	public float length;
	public float sampleRate;
	public int refs;
	
	public KeyframeAnimation(String name, int frames, float length, float sampleRate)
	{
		this.name = name;
		this.keyframes = new Keyframe[frames];
		this.length = length;
		this.sampleRate = sampleRate;
		this.refs = 1;
	}

	@Override
	public float getLength() {
		return length;
	}

	@Override
	public int getNumFrames() {
		return keyframes.length;
	}
	
	public void addRef()
	{
		refs++;
	}
	
	public int removeRef()
	{
		return --refs;
	}
}
