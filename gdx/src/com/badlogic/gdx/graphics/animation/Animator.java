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
package com.badlogic.gdx.graphics.animation;

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
	protected boolean mAnimLoop = false;
	protected int mCurrentFrameIdx = 0;
	protected int mNextFrameIdx = 0;
	protected float mFrameDelta = 0.f;
	protected Animation mCurrentAnim = null;
	
	/**
	 * Sets the currently playing {@link Animation}.
	 * @param anim
	 *          The animation to play.
	 * @param loop
	 *          Whether to loop the animation.
	 */
	public void setAnimation(Animation anim, boolean loop)
	{
		mCurrentAnim = anim;
		mAnimLoop = loop;

		if(mCurrentAnim != null)
		{
			mAnimLen = mCurrentAnim.getLength(); 
			mAnimPos = mFrameDelta = 0.f;
			mCurrentFrameIdx = 0;
			mNextFrameIdx = 1;
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
	 * Updates the controller.
	 * @param dt
	 *         Delta time since last frame.
	 */
	public void update(float dt)
	{
		if(mCurrentAnim != null)
		{
			mAnimPos += dt;
			if(mAnimPos > mAnimLen)
			{
				if(mAnimLoop)
				{
					mAnimPos = 0.f;
				}
				else
				{
					mAnimPos = mAnimLen;
				}
			}
			// select the frames
			float animPos = mAnimPos/mAnimLen;
			int numFrames = mCurrentAnim.getNumFrames();
			
			int currentFrameIdx = Math.min(numFrames-1, (int)(animPos*(float)numFrames));
			
			if(currentFrameIdx != mCurrentFrameIdx)
			{
				int nextFrame = 0;
				
				if(currentFrameIdx < numFrames-1)
				{
					mNextFrameIdx = currentFrameIdx+1;
				}
				else
				{
					if(mAnimLoop)
					{
						mNextFrameIdx = 0;
					}
					else
					{
						mNextFrameIdx = currentFrameIdx;
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
