package com.badlogic.gdx.graphics.g3d.model.keyframe;

import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.AnimatedModel;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;


public class KeyframedModel implements AnimatedModel {
	public final KeyframedSubMesh[] subMeshes;
	protected final Animation[] animations;

	public KeyframedModel(KeyframedSubMesh[] subMeshes) {
		this.subMeshes = subMeshes;

		Array<KeyframedAnimation> meshAnims = subMeshes[0].animations.values().toArray();
		animations = new KeyframedAnimation[meshAnims.size];
		for(int i = 0; i < animations.length; i++) {
			animations[i] = meshAnims.get(i);
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

	@Override public void setAnimation (String animation, float time) {
		int len = subMeshes.length;
		for(int i = 0; i < len; i++) {
			KeyframedSubMesh subMesh = subMeshes[i];
			KeyframedAnimation anim = subMesh.animations.get(animation);
			if(anim == null) throw new IllegalArgumentException("No animation with name '" + animation + "' in submesh #" + i);
			// FIXME actually select frames and blend...
			Keyframe keyframe = anim.keyframes[(int)time];					
			
			float[] src = keyframe.vertices;
			int numComponents = keyframe.animatedComponents;
			int srcLen = numComponents * subMesh.mesh.getNumVertices();
			
			float[] dst = subMesh.blendedVertices;
			int dstInc = subMesh.mesh.getVertexSize() / 4 - numComponents;
			
			for(int srcIdx = 0, dstIdx = 0; srcIdx < srcLen; dstIdx += dstInc) {
				for(int j = 0; j < numComponents; j++) {
					dst[dstIdx++] = src[srcIdx++];
				}
			}
			
			subMesh.mesh.setVertices(dst);
		}
	}
	
	@Override public Animation getAnimation (String name) {
		return subMeshes[0].animations.get(name);		
	}
	
	@Override public Animation[] getAnimations () {
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
