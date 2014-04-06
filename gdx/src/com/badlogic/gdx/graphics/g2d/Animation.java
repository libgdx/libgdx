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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.utils.Array;

/** <p>
 * An Animation stores a list of {@link TextureRegion}s representing an animated sequence, e.g. for running or jumping. Each
 * region of an Animation is called a key frame, multiple key frames make up the animation.
 * </p>
 * 
 * @author mzechner */
public class Animation extends KeyFrames<TextureRegion> {

	/** Constructor, storing the frame duration, key frames and play type.
	 * 
	 * @param frameDuration the time between frames in seconds.
	 * @param keyFrames the {@link TextureRegion}s representing the frames.
	 * @param playMode the animation playback mode. */
	public Animation (float frameDuration, Array<? extends TextureRegion> keyFrames, PlayMode playMode) {
		super(frameDuration, keyFrames, playMode);
	}

	/** Constructor, storing the frame duration and key frames.
	 * 
	 * @param frameDuration the time between frames in seconds.
	 * @param keyFrames the {@link TextureRegion}s representing the frames. */
	public Animation (float frameDuration, Array<? extends TextureRegion> keyFrames) {
		super(frameDuration, keyFrames);
	}

	/** Constructor, storing the frame duration and key frames.
	 * 
	 * @param frameDuration the time between frames in seconds.
	 * @param keyFrames the {@link TextureRegion}s representing the frames. */
	public Animation (float frameDuration, TextureRegion... keyFrames) {
		super(frameDuration, keyFrames);
	}

	/** Whether the animation would be finished if played without looping (PlayMode#NORMAL), given the state time.
	 * @param stateTime
	 * @return whether the key frame loop is finished. */
	public boolean isAnimationFinished (float stateTime) {
		return isSequenceFinished(stateTime);
	}
	
}
