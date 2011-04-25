package com.badlogic.gdx.graphics.g3d.model.keyframe;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.model.AnimatedModel;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonSubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;


public class KeyframedModel implements AnimatedModel {
	public KeyframedSubMesh[] subMeshes;

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
			subMesh.mesh.setVertices(anim.keyframes[0].vertices);
		}
	}
	
	@Override public Animation getAnimation (String name) {
		// FIXME
		return null;
	}

	@Override public Animation[] getAnimations () {
		// FIXME
		return null;
	}
}
