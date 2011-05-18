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
package com.badlogic.gdx.graphics.g3d;

/**
 * Abstract class for a single-track animation controller. Keeps track of the animation position and invokes interpolation on
 * concrete classes.
 * 
 * @author Dave Clayton <contact@redskyforge.com>
 *
 */
public abstract class Animator {
	protected float mAnimPos = 0.f;
	protected float mAnimLen = 0.f;
	protected WrapMode mWrapMode = WrapMode.Clamp;
	protected int mCurrentFrameIdx = -1;
	protected int mNextFrameIdx = -1;
	protected float mFrameDelta = 0.f;
	protected Animation mCurrentAnim = null;
	
	public enum WrapMode
	{
		Loop,
		Clamp,
		//PingPong, //TODO
		SingleFrame,
	}
	
	/**
	 * Sets the currently playing {@link Animation}.
	 * @param anim
	 *          The animation to play.
	 * @param mode
	 *          The animation's {@link WrapMode}.
	 */
	public void setAnimation(Animation anim, WrapMode mode)
	{
		mCurrentAnim = anim;
		mWrapMode = mode;

		mAnimPos = mFrameDelta = 0.f;
		mCurrentFrameIdx = -1;
		mNextFrameIdx = -1;

		if(mCurrentAnim != null)
		{
			mAnimLen = mCurrentAnim.getLength(); 
		}
	}
	
	/**
	 * Gets the currently playing {@link Animation}.
	 * @return the current animation.
	 */
	public Animation getCurrentAnimation()
	{
		return mCurrentAnim;
	}
	
	/**
	 * Gets the current animation {@link WrapMode}.
	 * @return the current wrapmode.
	 */
	public WrapMode getCurrentWrapMode()
	{
		return mWrapMode;
	}

	/**
	 * Updates the controller.
	 * @param dt
	 *         Delta time since last frame.
	 */
	public void update(float dt)
	{
		if(mCurrentAnim != null)
		{
			if(mWrapMode != WrapMode.SingleFrame)
			{
				mAnimPos += dt;
				if(mAnimPos > mAnimLen)
				{
					if(mWrapMode == WrapMode.Loop)
					{
						mAnimPos = 0.f;
					}
					else if(mWrapMode == WrapMode.Clamp)
					{
						mAnimPos = mAnimLen;
					}
				}
			}
			// select the frames
			float animPos = mAnimPos/mAnimLen;
			int numFrames = mCurrentAnim.getNumFrames();
			
			int currentFrameIdx = Math.min(numFrames-1, (int)(animPos*(float)numFrames));
			
			if(currentFrameIdx != mCurrentFrameIdx)
			{
				if(currentFrameIdx < numFrames-1)
				{
					mNextFrameIdx = currentFrameIdx+1;
				}
				else
				{
					switch(mWrapMode)
					{
					case Loop:
					case SingleFrame:
						mNextFrameIdx = 0; break;
					case Clamp:
						mNextFrameIdx = currentFrameIdx; break;
					}
				}
				
				mFrameDelta = 0.f;
				mCurrentFrameIdx = currentFrameIdx;
			}

			mFrameDelta += dt;
			
			setInterpolationFrames();
	
			interpolate();
		}
	}
	
	/**
	 * Implementations should set the 'current' and 'next' frames of animation that will be interpolated.
	 */
	protected abstract void setInterpolationFrames();
	
	/**
	 * Implementations should interpolate between the 'current' and 'next' frames of animation.
	 */
	protected abstract void interpolate();
}
