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

import com.badlogic.gdx.graphics.g3d.Animation;
import com.badlogic.gdx.graphics.g3d.Animator;

/**
 * An animation controller for MD5 (Doom 3) animations.
 * 
 * @author Dave Clayton <contact@redskyforge.com>
 *
 */
public class MD5Animator extends Animator {

	protected MD5Joints mCurrentFrame = null;
	protected MD5Joints mNextFrame = null;
	protected MD5Joints mSkeleton = null;
	
	/**
	 * Set the current skeleton.
	 * @param skeleton
	 */
	public void setSkeleton(MD5Joints skeleton)
	{
		mSkeleton = skeleton;
	}
	
	/**
	 * Get the current skeleton.
	 * @return the skeleton.
	 */
	public MD5Joints getSkeleton() { return mSkeleton; }

	@Override
	/**
	 * Sets the currently playing {@link MD5Animation}.
	 * @param anim
	 *          An {@link MD5Animation}.
	 * @param WrapMode
	 *          The animation {@link WrapMode}.
	 */
	public void setAnimation(Animation anim, WrapMode wrapMode)
	{
		super.setAnimation(anim, wrapMode);
		
		if(anim != null)
		{
			mCurrentFrame = mSkeleton = mNextFrame = ((MD5Animation)anim).frames[0];
		}
	}

	@Override
	protected void setInterpolationFrames() {
		mCurrentFrame = ((MD5Animation)mCurrentAnim).frames[mCurrentFrameIdx];
		mNextFrame = ((MD5Animation)mCurrentAnim).frames[mNextFrameIdx];
	}

	@Override
	protected void interpolate() {
		MD5Animation.interpolate(mCurrentFrame, mNextFrame, mSkeleton, mFrameDelta);
	}
}

