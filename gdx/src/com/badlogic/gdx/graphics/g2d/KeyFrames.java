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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/** <p>
 * KeyFrames stores a list of objects representing a sequence to be used over a period of frames. For example 
 * {@link Animation} is an implementation of KeyFrames<{@link TextureRegion}> to represent an animated sequence of textures, 
 * e.g. for running or jumping. Each object in the list is called a key frame.
 * </p>
 * 
 * @author mzechner */
public class KeyFrames<T> {

	/** Defines possible playback modes for a {@link KeyFrames}. */
	public enum PlayMode {
		NORMAL,
		REVERSED,
		LOOP,
		LOOP_REVERSED,
		LOOP_PINGPONG,
		LOOP_RANDOM,
	}

	final T[] keyFrames;
	public final float frameDuration;
	public final float animationDuration;

	private PlayMode playMode = PlayMode.NORMAL;

	/** Constructor, storing the frame duration and key frames.
	 * 
	 * @param frameDuration the time between frames in seconds.
	 * @param keyFrames the objects representing the frames. */
	public KeyFrames (float frameDuration, Array<? extends T> keyFrames) {
		this.frameDuration = frameDuration;
		this.animationDuration = keyFrames.size * frameDuration;
		this.keyFrames = (T[])new Object[keyFrames.size];
		for (int i = 0, n = keyFrames.size; i < n; i++) {
			this.keyFrames[i] = keyFrames.get(i);
		}

		this.playMode = PlayMode.NORMAL;
	}

	/** Constructor, storing the frame duration, key frames and play type.
	 * 
	 * @param frameDuration the time between frames in seconds.
	 * @param keyFrames the objects representing the frames.
	 * @param playMode the animation playback mode. */
	public KeyFrames (float frameDuration, Array<? extends T> keyFrames, PlayMode playMode) {

		this.frameDuration = frameDuration;
		this.animationDuration = keyFrames.size * frameDuration;
		this.keyFrames = (T[])new Object[keyFrames.size];
		for (int i = 0, n = keyFrames.size; i < n; i++) {
			this.keyFrames[i] = keyFrames.get(i);
		}

		this.playMode = playMode;
	}

	/** Constructor, storing the frame duration and key frames.
	 * 
	 * @param frameDuration the time between frames in seconds.
	 * @param keyFrames the objects representing the frames. */
	public KeyFrames (float frameDuration, T... keyFrames) {
		this.frameDuration = frameDuration;
		this.animationDuration = keyFrames.length * frameDuration;
		this.keyFrames = keyFrames;
		this.playMode = PlayMode.NORMAL;
	}

	/** Returns an object based on the so called state time. This is the amount of seconds an object has spent in the
	 * state this KeyFrames instance represents. The mode specifies whether the KeyFrames is
	 * looping or not.
	 * 
	 * @param stateTime the time spent in the state represented by this animation.
	 * @param looping whether the animation is looping or not.
	 * @return the TextureRegion representing the frame of animation for the given state time. */
	public T getKeyFrame (float stateTime, boolean looping) {
		// we set the play mode by overriding the previous mode based on looping
		// parameter value
		PlayMode oldPlayMode = playMode;
		if (looping && (playMode == PlayMode.NORMAL || playMode == PlayMode.REVERSED)) {
			if (playMode == PlayMode.NORMAL)
				playMode = PlayMode.LOOP;
			else
				playMode = PlayMode.LOOP_REVERSED;
		} else if (!looping && !(playMode == PlayMode.NORMAL || playMode == PlayMode.REVERSED)) {
			if (playMode == PlayMode.LOOP_REVERSED)
				playMode = PlayMode.REVERSED;
			else
				playMode = PlayMode.LOOP;
		}

		T frame = getKeyFrame(stateTime);
		playMode = oldPlayMode;
		return frame;
	}

	/** Returns an object based on the so called state time. This is the amount of seconds an object has spent in the
	 * state this KeyFrames instance represents using the mode specified by
	 * {@link #setPlayMode(PlayMode)} method.
	 * 
	 * @param stateTime
	 * @return the object representing the key frame for the given state time. */
	public T getKeyFrame (float stateTime) {
		int frameNumber = getKeyFrameIndex(stateTime);
		return keyFrames[frameNumber];
	}

	/** Returns the current frame number.
	 * @param stateTime
	 * @return current frame number */
	public int getKeyFrameIndex (float stateTime) {
		if (keyFrames.length == 1) return 0;

		int frameNumber = (int)(stateTime / frameDuration);
		switch (playMode) {
		case NORMAL:
			frameNumber = Math.min(keyFrames.length - 1, frameNumber);
			break;
		case LOOP:
			frameNumber = frameNumber % keyFrames.length;
			break;
		case LOOP_PINGPONG:
			frameNumber = frameNumber % ((keyFrames.length * 2) - 2);
			if (frameNumber >= keyFrames.length) frameNumber = keyFrames.length - 2 - (frameNumber - keyFrames.length);
			break;
		case LOOP_RANDOM:
			frameNumber = MathUtils.random(keyFrames.length - 1);
			break;
		case REVERSED:
			frameNumber = Math.max(keyFrames.length - frameNumber - 1, 0);
			break;
		case LOOP_REVERSED:
			frameNumber = frameNumber % keyFrames.length;
			frameNumber = keyFrames.length - frameNumber - 1;
			break;
		}

		return frameNumber;
	}

	/** Returns the keyFrames[] array where all the key frame objects are stored.
	 * @return keyFrames[] field */
	public T[] getKeyFrames () {
		return keyFrames;
	}

	/** Returns the key frame play mode. */
	public PlayMode getPlayMode () {
		return playMode;
	}

	/** Sets the key frame play mode.
	 * 
	 * @param playMode The key frame {@link PlayMode} to use. */
	public void setPlayMode (PlayMode playMode) {
		this.playMode = playMode;
	}

	/** Whether the key frame sequence would be finished if played without looping (PlayMode#NORMAL), given the state time.
	 * @param stateTime
	 * @return whether the key frame loop is finished. */
	public boolean isSequenceFinished (float stateTime) {
		int frameNumber = (int)(stateTime / frameDuration);
		return keyFrames.length - 1 < frameNumber;
	}

}
