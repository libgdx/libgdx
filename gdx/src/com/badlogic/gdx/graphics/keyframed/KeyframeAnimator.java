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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.animation.Animator;
import com.badlogic.gdx.graphics.loaders.md5.MD5Animation;
import com.badlogic.gdx.graphics.loaders.md5.MD5Quaternion;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * An animation controller for keyframed animations.
 *
 * @author Dave Clayton <contact@redskyforge.com>
 *
 */
public class KeyframeAnimator extends Animator {

	// constants
	public static int sStride = 8; // hmmm.

	private Keyframe A = null;
	private Keyframe B = null;
	private Keyframe R = null;
	private int mNumMeshes = 0;
	private float mInvSampleRate = 0;
	
	/**
	 * Get the current {@link Keyframe}.
	 * @return the current keyframe.
	 */
	public Keyframe getInterpolatedKeyframe() { return R; }

	/**
	 * Constructs a new KeyframeAnimator.
	 * @param numMeshes
	 *           The number of meshes in the {@link KeyframedModel}.
	 * @param sampleRate
	 *           The sample rate used to make the {@link KeyframeAnimation}s.
	 */
	public KeyframeAnimator(int numMeshes, float sampleRate)
	{
		mNumMeshes = numMeshes;
		// allocate vertex and index buffers for our temp result keyframe
		R = new Keyframe();
		R.Vertices = new float[mNumMeshes][];
		R.Indices = new short[mNumMeshes][];
		mInvSampleRate = 1.f/sampleRate;
	}
	
	/**
	 * Set the result (interpolated) keyframe internal array dimensions. 
	 * @param idx
	 *          The index of the mesh the keyframe represents.
	 * @param numVertices
	 *          The number of vertices the mesh has.
	 * @param numIndices
	 *          The number of indices the mesh has.
	 */
	public void setKeyframeDimensions(int idx, int numVertices, int numIndices)
	{
		R.Vertices[idx] = new float[numVertices];
		R.Indices[idx] = new short[numIndices];
	}
	
	/**
	 * Set the number of tagged joints for allocation
	 * @param num
	 */
	public void setNumTaggedJoints(int num)
	{
		// allocate space for joint data in the result keyframe
		R.TaggedJointPos = new Vector3[num];
		for(int i=0; i<num; i++)
			R.TaggedJointPos[i] = new Vector3();
		R.TaggedJoint = new Quaternion[num];
		for(int i=0; i<num; i++)
			R.TaggedJoint[i] = new Quaternion(0,0,0,0);
	}
	
	@Override
	protected void setInterpolationFrames() {
		A = ((KeyframeAnimation)mCurrentAnim).mKeyframes[mCurrentFrameIdx];
		B = ((KeyframeAnimation)mCurrentAnim).mKeyframes[mNextFrameIdx];
	}


	static MD5Quaternion jointAOrient = new MD5Quaternion();
	static MD5Quaternion jointBOrient = new MD5Quaternion();

	//TODO: Optimise further if possible - this is the CPU bottleneck for animation
	@Override
	protected void interpolate()
	{
		float t = mFrameDelta*mInvSampleRate;
		for(int i=0; i<mNumMeshes; i++)
		{
			for(int n=0; n<A.Vertices[i].length; n+=sStride)
			{
				// interpolated position
				float Ax = A.Vertices[i][n];
				float Bx = B.Vertices[i][n];
				float Rx = Ax + (Bx - Ax)*t;
				float Ay = A.Vertices[i][n+1];
				float By = B.Vertices[i][n+1];
				float Ry = Ay + (By - Ay)*t;
				float Az = A.Vertices[i][n+2];
				float Bz = B.Vertices[i][n+2];
				float Rz = Az + (Bz - Az)*t;

				R.Vertices[i][n] = Rx;
				R.Vertices[i][n+1] = Ry;
				R.Vertices[i][n+2] = Rz;

				// texture coordinates
				R.Vertices[i][n+3] = A.Vertices[i][n+3];
				R.Vertices[i][n+4] = A.Vertices[i][n+4];
				
				// interpolated normals
				Ax = A.Vertices[i][n+5];
				Bx = B.Vertices[i][n+5];
				Rx = Ax + (Bx - Ax)*t;
				Ay = A.Vertices[i][n+6];
				By = B.Vertices[i][n+6];
				Ry = Ay + (By - Ay)*t;
				Az = A.Vertices[i][n+7];
				Bz = B.Vertices[i][n+7];
				Rz = Az + (Bz - Az)*t;
				R.Vertices[i][n+5] = Rx;
				R.Vertices[i][n+6] = Ry;
				R.Vertices[i][n+7] = Rz;
			}

			if(!R.IndicesSet)
			{
				for(int n=0; n<A.Indices[i].length; n++)
				{
					R.Indices[i][n] = A.Indices[i][n];
				}
			}
		}
		R.IndicesSet = true;
		
		//interpolate any tagged joints
		for(int tj = 0; tj<A.TaggedJoint.length; tj++)
		{
			//position
			float PAX = A.TaggedJointPos[tj].x;
			float PAY = A.TaggedJointPos[tj].y;
			float PAZ = A.TaggedJointPos[tj].z;
			float PBX = B.TaggedJointPos[tj].x;
			float PBY = B.TaggedJointPos[tj].y;
			float PBZ = B.TaggedJointPos[tj].z;

			R.TaggedJointPos[tj].x = PAX + (PBX - PAX)*t;
			R.TaggedJointPos[tj].y = PAY + (PBY - PAY)*t;
			R.TaggedJointPos[tj].z = PAZ + (PBZ - PAZ)*t;
			
			//orientation
			jointAOrient.x = A.TaggedJoint[tj].x;
			jointAOrient.y = A.TaggedJoint[tj].y;
			jointAOrient.z = A.TaggedJoint[tj].z;
			jointAOrient.w = A.TaggedJoint[tj].w;
			jointBOrient.x = B.TaggedJoint[tj].x;
			jointBOrient.y = B.TaggedJoint[tj].y;
			jointBOrient.z = B.TaggedJoint[tj].z;
			jointBOrient.w = B.TaggedJoint[tj].w;
			jointAOrient.slerp(jointBOrient, t);
			R.TaggedJoint[tj].x = jointAOrient.x;
			R.TaggedJoint[tj].y = jointAOrient.y;
			R.TaggedJoint[tj].z = jointAOrient.z;
			R.TaggedJoint[tj].w = jointAOrient.w;
		}
	}

	/**
	 * Whether the controller is currently playing an animation.
	 * @return If an animation is being played.
	 */
	public boolean hasAnimation() {
		return mCurrentAnim != null;
	}
}
