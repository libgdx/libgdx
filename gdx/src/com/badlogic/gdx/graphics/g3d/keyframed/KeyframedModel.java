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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Animator;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Animation;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Animator;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Joints;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Model;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Renderer;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * An animated model with {@link KeyframeAnimation}s. Currently the animations can only be instanced from an {@link MD5Animation}.
 * Support for binary serialization may be included in future development.
 *  
 * @author Dave Clayton <contact@redskyforge.com>
 *
 */
public class KeyframedModel {
	
	private Material[] mMaterials;
	private ObjectMap<String, KeyframeAnimation> mAnimations = new ObjectMap<String, KeyframeAnimation>();
	private KeyframeAnimator mAnimator = null;
	private Mesh[] mTarget = null;
	private int mNumMeshes = 0;
	private ArrayList<String> mTaggedJointNames = new ArrayList<String>();
	
	/**
	 * Gets the {@link KeyframeAnimator} for this model.
	 * @return the animator.
	 */
	public Animator getAnimator() { return mAnimator; }
	
	public void write(DataOutputStream out)
	{
		throw new GdxRuntimeException("Not implemented");
	}
	
	public void read(DataInputStream in)
	{		
		throw new GdxRuntimeException("Not implemented");
	}
	
	/**
	 * Sets the {@link Material} list for this model, one for each mesh, in mesh order.
	 * @param mats
	 *          An array of materials.
	 */
	public void setMaterials(Material[] mats)
	{
		mMaterials = new Material[mats.length];
		for(int i=0; i<mats.length; i++)
		{
			mMaterials[i] = mats[i];
		}
	}
	
	/**
	 * Sets the tagged joints for this model's animations. Tagged joints have their data preserved post-sampling.
	 * @param joints
	 *           An array of joint names.
	 */
	public void setTaggedJoints(ArrayList<String> joints)
	{
		mTaggedJointNames = joints;
	}

	//TODO: Split this out to an MD5toKeyframe loader in com.badlogic.gdx.graphics.loaders
	/**
	 * Loads a single {@link KeyframeAnimation} from an {@link MD5Animation}, then stores it in the model's animation map for runtime playback.
	 * @param model
	 *           The source {@link MD5Model}.
	 * @param renderer
	 *           An {@link MD5Renderer} instance, used to calculate the skinned geometry.
	 * @param animator
	 *           An {@link MD5Animator} instance to control the MD5 animation cycle the keyframing samples from.
	 * @param animation
	 *           The {@link MD5Animation} to sample.
	 * @param sampleRate
	 *           The sample rate (use smaller values for smoother animation but greater memory usage). Recommended value: 0.3
	 * @param animKey
	 *           The name used to store the animation in the mode's animation map.
	 */
	public void sampleAnimationFromMD5(MD5Model model, MD5Renderer renderer, MD5Animator animator, MD5Animation animation, float sampleRate, String animKey)
	{
		animator.setAnimation(animation, false);

		float len = animation.frames.length*animation.secondsPerFrame;
		int numSamples = (int)(len/sampleRate)+1;
		mNumMeshes = model.meshes.length;
	
		if(mAnimator == null)
		{
			mAnimator = new KeyframeAnimator(mNumMeshes, sampleRate);
			mTarget = new Mesh[mNumMeshes];
		}
		
		KeyframeAnimation a = new KeyframeAnimation(animation.name, numSamples, len);
		
		animator.update(0);
		renderer.setSkeleton(animator.getSkeleton());

		int i=0;
		int numVertices=0,numIndices=0;
		for(float t = 0; t < len; t += sampleRate)
		{
			//store meshes.
			Keyframe k = new Keyframe();
			k.Vertices = new float[mNumMeshes][];
			k.Indices = new short[mNumMeshes][];
			if(mTaggedJointNames.size() > 0)
			{
				k.TaggedJointPos = new Vector3[mTaggedJointNames.size()];
				k.TaggedJoint = new Quaternion[mTaggedJointNames.size()];
			}
			
			for(int m=0; m<mNumMeshes; m++)
			{
				float vertices[] = renderer.getVertices(m);
				short indices[] = renderer.getIndices(m);
				numVertices = vertices.length;
				numIndices = indices.length;
				k.Vertices[m] = new float[vertices.length];
				k.Vertices[m] = vertices.clone();
				k.Indices[m] = new short[indices.length];
				k.Indices[m] = indices.clone();

				if(mTarget[m] == null)
				{
					mAnimator.setKeyframeDimensions(m, numVertices, numIndices);
					mAnimator.setNumTaggedJoints(mTaggedJointNames.size());

					VertexAttributes attribs = renderer.getMesh().getVertexAttributes();
					mTarget[m] = new Mesh(false, numVertices, numIndices, attribs);
					if(mTarget[m].getVertexSize()/4 != KeyframeAnimator.sStride)
						throw new GdxRuntimeException("Mesh vertexattributes != 8 - is this a valid MD5 source mesh?");
				}
			}
			
			//store tagged joints.
			MD5Joints skel = animator.getSkeleton();
			for(int tj=0; tj<mTaggedJointNames.size(); tj++)
			{
				String name = mTaggedJointNames.get(tj);
				for(int j=0; j<skel.numJoints; j++)
				{
					if(name.equals(skel.names[j]))
					{
						int idx = j*8;
						float p = skel.joints[idx];
						float x = skel.joints[idx+1];
						float y = skel.joints[idx+2];
						float z = skel.joints[idx+3];
						k.TaggedJointPos[tj] = new Vector3(x, y, z);
						float qx = skel.joints[idx+4];
						float qy = skel.joints[idx+5];
						float qz = skel.joints[idx+6];
						float qw = skel.joints[idx+7];
						k.TaggedJoint[tj] = new Quaternion(qx, qy, qz, qw);
						break;
					}
				}
			}
			
			a.mKeyframes[i] = k;
			
			animator.update(sampleRate);
			renderer.setSkeleton(animator.getSkeleton());
			i++;
		}
		
		//Gdx.app.log("Loader", "Added animation "+animation.name+" to keyframes ("+i+" keyframes generated)");
		mAnimations.put(animKey, a);
	}
	
	public void getJointData(int tagIndex, Vector3 pos, Quaternion orient)
	{
		Keyframe kf = mAnimator.getInterpolatedKeyframe();
		pos.set(kf.TaggedJointPos[tagIndex]);
		orient.x = kf.TaggedJoint[tagIndex].x;
		orient.y = kf.TaggedJoint[tagIndex].y;
		orient.z = kf.TaggedJoint[tagIndex].z;
		orient.w = kf.TaggedJoint[tagIndex].w;
	}
	
	/**
	 * Set the current playing animation.
	 * @param animKey
	 *           The name of the animation.
	 * @param loop
	 *           Whether the animation should loop.
	 */
	public void setAnimation(String animKey, boolean loop)
	{
		KeyframeAnimation anim = mAnimations.get(animKey);
		mAnimator.setAnimation(anim, loop);
	}
	
	/**
	 * Gets the specified {@link KeyframeAnimation} from the animation map.
	 * @param animKey
	 *           The name of the animation.
	 * @return The animation.
	 */
	public KeyframeAnimation getAnimation(String animKey)
	{
		return mAnimations.get(animKey);
	}
	
	/**
	 * Updates the model, causing the model's {@link KeyframeAnimator} to interpolate the animation and update the render geometry.
	 * @param dt
	 *          Delta time since last frame.
	 */
	public void update(float dt)
	{
		if(mAnimator != null)
		{
			mAnimator.update(dt);
			
			if(mAnimator.hasAnimation())
			{
				Keyframe ikf = mAnimator.getInterpolatedKeyframe();
				
				// send our target index and vertex data to the target mesh
				for(int i=0; i<mNumMeshes; i++)
				{
					mTarget[i].setVertices(ikf.Vertices[i]);
					if(!ikf.IndicesSent)
					{
						mTarget[i].setIndices(ikf.Indices[i]);
					}
				}
				ikf.IndicesSent = true;
			}
		}
	}
	
	/**
	 * Draws the model using the current interpolated animation frame and the material list set by {@link #setMaterials}.
	 * {@link #update} must be called prior to this.
	 */
	public void render()
	{
		for(int i=0; i<mNumMeshes; i++)
		{
			// bind textures etc.
			Material mat = mMaterials[i];
			if(mat != null)
			{
				if(mat.Texture != null)
				{
					mat.Texture.bind();
				}
				mat.set(GL10.GL_FRONT);
			}
			mTarget[i].render(GL10.GL_TRIANGLES, 0, mTarget[i].getNumIndices());
		}
	}
}
