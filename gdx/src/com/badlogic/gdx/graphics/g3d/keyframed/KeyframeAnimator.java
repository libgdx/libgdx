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
package com.badlogic.gdx.graphics.g3d.keyframed;

import com.badlogic.gdx.graphics.g3d.Animator;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Quaternion;
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
	private int numMeshes = 0;
	private float invSampleRate = 0;
	
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
		this.numMeshes = numMeshes;
		// allocate vertex and index buffers for our temp result keyframe
		this.R = new Keyframe();
		this.R.vertices = new float[numMeshes][];
		this.R.indices = new short[numMeshes][];
		this.invSampleRate = 1.f/sampleRate;
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
		R.vertices[idx] = new float[numVertices];
		R.indices[idx] = new short[numIndices];
	}
	
	/**
	 * Set the number of tagged joints for allocation
	 * @param num
	 */
	public void setNumTaggedJoints(int num)
	{
		// allocate space for joint data in the result keyframe
		R.taggedJointPos = new Vector3[num];
		for(int i=0; i<num; i++)
			R.taggedJointPos[i] = new Vector3();
		R.taggedJoint = new Quaternion[num];
		for(int i=0; i<num; i++)
			R.taggedJoint[i] = new Quaternion(0,0,0,0);
	}
	
	@Override
	protected void setInterpolationFrames() {
		A = ((KeyframeAnimation)mCurrentAnim).keyframes[mCurrentFrameIdx];
		B = ((KeyframeAnimation)mCurrentAnim).keyframes[mNextFrameIdx];
	}


	static MD5Quaternion jointAOrient = new MD5Quaternion();
	static MD5Quaternion jointBOrient = new MD5Quaternion();

	//TODO: Optimise further if possible - this is the CPU bottleneck for animation
	@Override
	protected void interpolate()
	{
		float t = mFrameDelta*invSampleRate;
		for(int i=0; i<numMeshes; i++)
		{
			for(int n=0; n<A.vertices[i].length; n+=sStride)
			{
				// interpolated position
				float Ax = A.vertices[i][n];
				float Bx = B.vertices[i][n];
				float Rx = Ax + (Bx - Ax)*t;
				float Ay = A.vertices[i][n+1];
				float By = B.vertices[i][n+1];
				float Ry = Ay + (By - Ay)*t;
				float Az = A.vertices[i][n+2];
				float Bz = B.vertices[i][n+2];
				float Rz = Az + (Bz - Az)*t;

				R.vertices[i][n] = Rx;
				R.vertices[i][n+1] = Ry;
				R.vertices[i][n+2] = Rz;

				// texture coordinates
				R.vertices[i][n+3] = A.vertices[i][n+3];
				R.vertices[i][n+4] = A.vertices[i][n+4];
				
				// interpolated normals
				Ax = A.vertices[i][n+5];
				Bx = B.vertices[i][n+5];
				Rx = Ax + (Bx - Ax)*t;
				Ay = A.vertices[i][n+6];
				By = B.vertices[i][n+6];
				Ry = Ay + (By - Ay)*t;
				Az = A.vertices[i][n+7];
				Bz = B.vertices[i][n+7];
				Rz = Az + (Bz - Az)*t;
				R.vertices[i][n+5] = Rx;
				R.vertices[i][n+6] = Ry;
				R.vertices[i][n+7] = Rz;
			}

			if(!R.indicesSet)
			{
				for(int n=0; n<A.indices[i].length; n++)
				{
					R.indices[i][n] = A.indices[i][n];
				}
			}
		}
		R.indicesSet = true;
		
		//interpolate any tagged joints
		for(int tj = 0; tj<A.taggedJoint.length; tj++)
		{
			//position
			float PAX = A.taggedJointPos[tj].x;
			float PAY = A.taggedJointPos[tj].y;
			float PAZ = A.taggedJointPos[tj].z;
			float PBX = B.taggedJointPos[tj].x;
			float PBY = B.taggedJointPos[tj].y;
			float PBZ = B.taggedJointPos[tj].z;

			R.taggedJointPos[tj].x = PAX + (PBX - PAX)*t;
			R.taggedJointPos[tj].y = PAY + (PBY - PAY)*t;
			R.taggedJointPos[tj].z = PAZ + (PBZ - PAZ)*t;
			
			//orientation
			jointAOrient.x = A.taggedJoint[tj].x;
			jointAOrient.y = A.taggedJoint[tj].y;
			jointAOrient.z = A.taggedJoint[tj].z;
			jointAOrient.w = A.taggedJoint[tj].w;
			jointBOrient.x = B.taggedJoint[tj].x;
			jointBOrient.y = B.taggedJoint[tj].y;
			jointBOrient.z = B.taggedJoint[tj].z;
			jointBOrient.w = B.taggedJoint[tj].w;
			jointAOrient.slerp(jointBOrient, t);
			R.taggedJoint[tj].x = jointAOrient.x;
			R.taggedJoint[tj].y = jointAOrient.y;
			R.taggedJoint[tj].z = jointAOrient.z;
			R.taggedJoint[tj].w = jointAOrient.w;
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
