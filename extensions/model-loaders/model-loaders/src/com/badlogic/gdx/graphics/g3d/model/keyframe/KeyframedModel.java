package com.badlogic.gdx.graphics.g3d.model.keyframe;

import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.AnimatedModel;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;


public class KeyframedModel implements AnimatedModel {
	public final KeyframedSubMesh[] subMeshes;
	protected final KeyframedAnimation[] animations;

	public KeyframedModel(KeyframedSubMesh[] subMeshes) {
		this.subMeshes = subMeshes;

		Array<KeyframedAnimation> meshAnims = subMeshes[0].animations.values().toArray();
		animations = new KeyframedAnimation[meshAnims.size];
		for(int i = 0; i < animations.length; i++) {
			animations[i] = meshAnims.get(i);
		}
		
		checkCorrectness();
	}
	
	private void checkCorrectness() {
		for(int i = 0; i < subMeshes.length; i++) {
			if(subMeshes[i].animations.size != animations.length) throw new GdxRuntimeException("number of animations in subMesh[0] is not the same in subMesh[" + i + "]. All sub-meshes must have the same animations and number of frames");
		}
		
		for(int i = 0; i < animations.length; i++) {
			KeyframedAnimation anim = animations[i];
			for(int j = 0; j < subMeshes.length; j++) {
				KeyframedAnimation otherAnim = subMeshes[j].animations.get(anim.name);
				if(otherAnim == null) throw new GdxRuntimeException("animation '" +  anim.name + "' missing in subMesh[" + j + "]");
				if(otherAnim.frameDuration != anim.frameDuration) throw new GdxRuntimeException("animation '" + anim.name + "' in subMesh[" + j + "] has different frame duration than the same animation in subMesh[0]");
				if(otherAnim.keyframes.length != anim.keyframes.length) throw new GdxRuntimeException("animation '" + anim.name + "' in subMesh[" + j + "] has different number of keyframes than the same animation in subMesh[0]");
			}
		}
	}
	
	@Override public void render() {		
		int len = subMeshes.length;
		for(int i = 0; i < len; i++) {
			KeyframedSubMesh subMesh = subMeshes[i];
			if(i == 0 ) {
				subMesh.material.bind();
			} else if (!subMeshes[i-1].material.equals(subMesh.material)) {
				subMesh.material.bind();
			}
			subMesh.mesh.render(subMesh.primitiveType);
		}			
	}	
	
	@Override public void render (ShaderProgram program) {
		// FIXME
	}
	
	@Override public void setMaterials(Material ... materials) {
		if(materials.length != subMeshes.length) throw new UnsupportedOperationException("number of materials must equal number of sub-meshes");
		int len = materials.length;
		for(int i = 0; i < len; i++) {
			subMeshes[i].material = materials[i];
		}
	}
	
	@Override public void setMaterial(Material material) {
		int len = subMeshes.length;
		for(int i = 0; i < len; i++) {
			subMeshes[i].material = material;
		}
	}
	
	@Override public SubMesh getSubMesh(String name) {
		int len = subMeshes.length;
		for(int i = 0; i < len; i++) {
			if(subMeshes[i].name.equals(name)) return subMeshes[i];
		}		
		return null;
	}

	@Override public SubMesh[] getSubMeshes () {
		return subMeshes;
	}

	@Override public void setAnimation (String animation, float time, boolean loop) {
		int len = subMeshes.length;
		for(int i = 0; i < len; i++) {
			final KeyframedSubMesh subMesh = subMeshes[i];
			final KeyframedAnimation anim = subMesh.animations.get(animation);
			if(anim == null) throw new IllegalArgumentException("No animation with name '" + animation + "' in submesh #" + i);
			if(time < 0 || time > anim.totalDuration) throw new IllegalArgumentException("time must be 0 <= time <= animation duration");
			
			final int startIndex = (int)Math.floor((time / anim.frameDuration));
			final Keyframe startFrame = anim.keyframes[startIndex];
			final Keyframe endFrame = anim.keyframes[anim.keyframes.length-1==startIndex?loop?0:startIndex:startIndex + 1];			
			
			final int numComponents = startFrame.animatedComponents;
			final float[] src = startFrame.vertices;				
			final int srcLen = numComponents * subMesh.mesh.getNumVertices();
			
			final float[] dst = subMesh.blendedVertices;						
			final int dstInc = subMesh.mesh.getVertexSize() / 4 - numComponents;			
			
			if(startFrame == endFrame) {																			
				for(int srcIdx = 0, dstIdx = 0; srcIdx < srcLen; dstIdx += dstInc) {
					for(int j = 0; j < numComponents; j++) {
						dst[dstIdx++] = src[srcIdx++];
					}
				}
			} else {				
				float[] src2 = endFrame.vertices;
				float alpha = (time - (startIndex * anim.frameDuration)) / anim.frameDuration;
				for(int srcIdx = 0, dstIdx = 0; srcIdx < srcLen; dstIdx += dstInc) {
					for(int j = 0; j < numComponents; j++) {
						final float valSrc = src[srcIdx];
						final float valSrc2 = src2[srcIdx++];
						dst[dstIdx++] = valSrc + (valSrc2 - valSrc) * alpha;
					}
				}
			}
			
			subMesh.mesh.setVertices(dst);
		}
	}
	
	@Override public KeyframedAnimation getAnimation (String name) {
		return subMeshes[0].animations.get(name);		
	}
	
	@Override public KeyframedAnimation[] getAnimations () {
		return animations;
	}

	@Override
	public Model getSubModel(String... subMeshNames) {
		// FIXME
		return null;
	}

	private final static BoundingBox tmpBox = new BoundingBox();
	@Override public void getBoundingBox (BoundingBox bbox) {
		bbox.inf();
		for(int i = 0; i < subMeshes.length; i++) {
			subMeshes[i].mesh.calculateBoundingBox(tmpBox);
			bbox.ext(tmpBox);
		}
	}
}
