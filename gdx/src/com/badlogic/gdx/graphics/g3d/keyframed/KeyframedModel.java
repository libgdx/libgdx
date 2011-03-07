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

import java.util.ArrayList;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Animator;
import com.badlogic.gdx.graphics.g3d.Animator.WrapMode;
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
	
	private Material[] materials;
	private static ObjectMap<String, KeyframeAnimation> animations = null;
	private ArrayList<String> animationRefs = new ArrayList<String>();
	private String assetName;
	private KeyframeAnimator animator = null;
	private Mesh[] target = null;
	private boolean[] visible = null;
	private int numMeshes = 0;
	private ArrayList<String> taggedJointNames = new ArrayList<String>();
	
	/**
	 * Gets the {@link KeyframeAnimator} for this model.
	 * @return the animator.
	 */
	public Animator getAnimator() { return animator; }
	
	/**
	 * Sets the {@link Material} list for this model, one for each mesh, in mesh order.
	 * @param mats
	 *          An array of materials.
	 */
	public void setMaterials(Material[] mats)
	{
		materials = new Material[mats.length];
		for(int i=0; i<mats.length; i++)
		{
			materials[i] = mats[i];
		}
	}
	
	/**
	 * Sets the tagged joints for this model's animations. Tagged joints have their data preserved post-sampling.
	 * @param joints
	 *           An array of joint names.
	 */
	public void setTaggedJoints(ArrayList<String> joints)
	{
		taggedJointNames = joints;
	}

	//TODO: Split this out to an MD5toKeyframe loader in com.badlogic.gdx.graphics.loaders
	/**
	 * Loads a single {@link KeyframeAnimation} from an {@link MD5Animation}, then stores it in the animation dictionary for runtime playback.
	 * The dictionary manages ref counted animations so you do not have multiple copies of 100k animations in memory when you only need one per
	 * unique MD5 model. You must call dispose() when finished with this class.
	 * @param md5model
	 *           The source {@link MD5Model}.
	 * @param md5renderer
	 *           An {@link MD5Renderer} instance, used to calculate the skinned geometry.
	 * @param md5animator
	 *           An {@link MD5Animator} instance to control the MD5 animation cycle the keyframing samples from.
	 * @param md5animation
	 *           The {@link MD5Animation} to sample.
	 * @param sampleRate
	 *           The sample rate (use smaller values for smoother animation but greater memory usage). Recommended value: 0.3
	 * @param modelAsset
	 *           The name of the model asset. Must be unique to the model. Using its path is recommended
	 * @param animKey
	 *           The name used to store the animation in the mode's animation map.
	 */
	public KeyframeAnimation sampleAnimationFromMD5(MD5Model md5model, MD5Renderer md5renderer, MD5Animator md5animator, MD5Animation md5animation, float sampleRate, String modelAsset, String animKey)
	{
		this.assetName = modelAsset;
		numMeshes = md5model.meshes.length;
		boolean cached = false;
		
		if(animator == null)
		{
			animator = new KeyframeAnimator(numMeshes, sampleRate);
			target = new Mesh[numMeshes];
			visible = new boolean[numMeshes];
			for(int i = 0; i < visible.length; i++) {
				visible[i] = true;
			}
		}

		if(animations == null)
		{
			animations = new ObjectMap<String, KeyframeAnimation>();
		}
		String key = modelAsset+"_"+animKey;
		
		KeyframeAnimation a = null;
		if(animations.containsKey(key))
		{
			a = animations.get(key);
			a.addRef();
			cached = true;
		}
		animationRefs.add(key);
		
		md5animator.setAnimation(md5animation, WrapMode.Clamp);

		float len = md5animation.frames.length*md5animation.secondsPerFrame;
		int numSamples = (int)(len/sampleRate)+1;
	
		if(!cached)
		{
			a = new KeyframeAnimation(md5animation.name, numSamples, len, sampleRate);
			animations.put(key, a);
		}

		md5animator.update(0.1f);
		md5renderer.setSkeleton(md5animator.getSkeleton());

		int i=0;
		int numVertices=0,numIndices=0;
		for(float t = 0; t < len; t += sampleRate)
		{
			//store meshes.
			Keyframe k = null;
			if(!cached)
			{
				k = new Keyframe();
				k.vertices = new float[numMeshes][];
				k.indices = new short[numMeshes][];
				if(taggedJointNames.size() > 0)
				{
					k.taggedJointPos = new Vector3[taggedJointNames.size()];
					k.taggedJoint = new Quaternion[taggedJointNames.size()];
				}
			}			
			for(int m=0; m<numMeshes; m++)
			{
				float vertices[] = md5renderer.getVertices(m);
				short indices[] = md5renderer.getIndices(m);
				numVertices = vertices.length;
				numIndices = indices.length;
				if(!cached)
				{
					k.vertices[m] = new float[vertices.length];
					k.vertices[m] = vertices.clone();
					k.indices[m] = new short[indices.length];
					k.indices[m] = indices.clone();
				}
				
				if(target[m] == null)
				{
					animator.setKeyframeDimensions(m, numVertices, numIndices);
					animator.setNumTaggedJoints(taggedJointNames.size());

					VertexAttributes attribs = md5renderer.getMesh().getVertexAttributes();
					target[m] = new Mesh(false, numVertices, numIndices, attribs);
					if(target[m].getVertexSize()/4 != KeyframeAnimator.sStride)
						throw new GdxRuntimeException("Mesh vertexattributes != 8 - is this a valid MD5 source mesh?");
				}
			}
			
			if(!cached)
			{
				//store tagged joints.
				MD5Joints skel = md5animator.getSkeleton();
				for(int tj=0; tj<taggedJointNames.size(); tj++)
				{
					String name = taggedJointNames.get(tj);
					for(int j=0; j<skel.numJoints; j++)
					{
						if(name.equals(skel.names[j]))
						{
							int idx = j*8;
							// FIXME what is this? float p = skel.joints[idx];
							float x = skel.joints[idx+1];
							float y = skel.joints[idx+2];
							float z = skel.joints[idx+3];
							k.taggedJointPos[tj] = new Vector3(x, y, z);
							float qx = skel.joints[idx+4];
							float qy = skel.joints[idx+5];
							float qz = skel.joints[idx+6];
							float qw = skel.joints[idx+7];
							k.taggedJoint[tj] = new Quaternion(qx, qy, qz, qw);
							break;
						}
					}
				}
				
				a.keyframes[i] = k;
			}			
			md5animator.update(sampleRate);
			md5renderer.setSkeleton(md5animator.getSkeleton());
			i++;
		}
		
		if(cached)
		{
			//Gdx.app.log("Loader", "Added ref to animation "+key+" - keyframes ("+i+" keyframes generated). animations.size = "+animations.size);
		}
		else
		{
			//Gdx.app.log("Loader", "Loaded animation "+key+" - keyframes ("+i+" keyframes generated). animations.size = "+animations.size);
		}
		
		return a;
	}
	
	public void getJointData(int tagIndex, Vector3 pos, Quaternion orient)
	{
		Keyframe kf = animator.getInterpolatedKeyframe();
		pos.set(kf.taggedJointPos[tagIndex]);
		orient.x = kf.taggedJoint[tagIndex].x;
		orient.y = kf.taggedJoint[tagIndex].y;
		orient.z = kf.taggedJoint[tagIndex].z;
		orient.w = kf.taggedJoint[tagIndex].w;
	}
	
	/**
	 * Set the current playing animation.
	 * @param animKey
	 *           The name of the animation.
	 * @param wrapMode
	 *           The animation {@link WrapMode}.
	 */
	public void setAnimation(String animKey, WrapMode wrapMode)
	{
		KeyframeAnimation anim = getAnimation(animKey);
		if(anim != null)
		{
			animator.setAnimation(anim, wrapMode);
			animator.getInterpolatedKeyframe().indicesSet = false;
			animator.getInterpolatedKeyframe().indicesSent = false;
		}
	}
	
	/**
	 * Gets the specified {@link KeyframeAnimation} from the animation map.
	 * @param animKey
	 *           The name of the animation.
	 * @return The animation.
	 */
	public KeyframeAnimation getAnimation(String animKey)
	{
		return animations.get(assetName + "_" + animKey);
	}
	
	/**
	 * Updates the model, causing the model's {@link KeyframeAnimator} to interpolate the animation and update the render geometry.
	 * @param dt
	 *          Delta time since last frame.
	 */
	public void update(float dt)
	{
		if(animator != null)
		{
			animator.update(dt);
			
			if(animator.hasAnimation())
			{
				Keyframe ikf = animator.getInterpolatedKeyframe();
				
				if(animator.getCurrentWrapMode() == WrapMode.SingleFrame &&
					ikf.indicesSent)
					return; /* early out for single frame animations */
				
				// send our target index and vertex data to the target mesh
				for(int i=0; i<numMeshes; i++)
				{
					target[i].setVertices(ikf.vertices[i]);
					if(!ikf.indicesSent)
					{
						target[i].setIndices(ikf.indices[i]);
					}
				}
				ikf.indicesSent = true;
			}
		}
	}
	
	/**
	 * Draws the model using the current interpolated animation frame and the material list set by {@link #setMaterials}.
	 * {@link #update} must be called prior to this.
	 */
	public void render()
	{
		for(int i=0; i<numMeshes; i++)
		{
			// bind textures etc.
			Material mat = materials[i];
			if(mat != null)
			{
				if(mat.Texture != null)
				{
					mat.Texture.bind();
				}
				mat.set(GL10.GL_FRONT);
			}
			if(visible[i])
				target[i].render(GL10.GL_TRIANGLES, 0, target[i].getNumIndices());
		}
	}

	/**
	 * Sets the specified mesh's visibility (MD5 models typically consist of a number of meshes).
	 * @param idx
	 *          the mesh's index (same order as found in the .md5mesh file)
	 * @param visible
	 *          whether the mesh should be drawn or not
	 */
	public void setMeshVisible(int idx, boolean visible) {
		this.visible[idx] = visible;
	}
	
	public void dispose()
	{
		for(String key : animationRefs)
		{
			KeyframeAnimation anim = animations.get(key);
			if(anim.removeRef() == 0)
			{
				//Gdx.app.log("Engine", "Removing anim "+key+" from dict. Dict size = "+animations.size);
				animations.remove(key);
			}
		}
		for(Mesh m : target)
		{
			if(m != null)
			{
				m.dispose();
			}
		}
		//Gdx.app.log("Engine", "Disposed kfmodel "+this.assetName+", "+animations.size+" anims remain in cache");
	}
}
