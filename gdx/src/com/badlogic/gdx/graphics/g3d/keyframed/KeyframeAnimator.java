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
	public final static int sStride = 8; // TODO: fix hard-coded wrongness like this.

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

	@Override
	protected void interpolate()
	{
		if(mWrapMode == WrapMode.SingleFrame &&
				R.indicesSet)
			return;

		float t = mFrameDelta*invSampleRate;
		for(int i=0; i<numMeshes; i++)
		{
			float[] Rvertices = R.vertices[i];
			float[] Avertices = A.vertices[i];
			float[] Bvertices = B.vertices[i];
			for(int n=0; n<Avertices.length; n+=sStride)
			{
				// interpolated position
				float Ax = Avertices[n];
				float Bx = Bvertices[n];
				float Rx = Ax + (Bx - Ax)*t;
				float Ay = Avertices[n+1];
				float By = Bvertices[n+1];
				float Ry = Ay + (By - Ay)*t;
				float Az = Avertices[n+2];
				float Bz = Bvertices[n+2];
				float Rz = Az + (Bz - Az)*t;

				Rvertices[n] = Rx;
				Rvertices[n+1] = Ry;
				Rvertices[n+2] = Rz;

				// texture coordinates
				Rvertices[n+3] = Avertices[n+3];
				Rvertices[n+4] = Avertices[n+4];
				
				// interpolated normals
				Ax = Avertices[n+5];
				Bx = Bvertices[n+5];
				Rx = Ax + (Bx - Ax)*t;
				Ay = Avertices[n+6];
				By = Bvertices[n+6];
				Ry = Ay + (By - Ay)*t;
				Az = Avertices[n+7];
				Bz = Bvertices[n+7];
				Rz = Az + (Bz - Az)*t;
				Rvertices[n+5] = Rx;
				Rvertices[n+6] = Ry;
				Rvertices[n+7] = Rz;
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
		
		if(A.taggedJoint != null)
		{
			interpolateJoints(t);
		}
	}
	
	private void interpolateJoints(float t)
	{
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
