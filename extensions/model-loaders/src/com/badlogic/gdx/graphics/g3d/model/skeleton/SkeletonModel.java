package com.badlogic.gdx.graphics.g3d.model.skeleton;

import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class SkeletonModel {
	public Skeleton skeleton;
	public SubMesh[] subMeshes;
	
	public void setBindPose() {
		skeleton.setBindPose();
		for(int i = 0; i < subMeshes.length; i++) {
			skin(subMeshes[i], skeleton.combinedMatrices);
		}
	}
	
	public void setAnimation(String animation, float time) {
		skeleton.setAnimation(animation, time);
		for(int i = 0; i < subMeshes.length; i++) {
			skin(subMeshes[i], skeleton.combinedMatrices);
		}
	}
	
	final Vector3 v = new Vector3();
	public void skin(SubMesh subMesh, Array<Matrix4> boneMatrices) {
		final int stride = subMesh.mesh.getVertexSize() / 4;
		final int numVertices = subMesh.mesh.getNumVertices();
		int idx = 0;
		int nidx = subMesh.mesh.getVertexAttribute(Usage.Normal) == null? -1: subMesh.mesh.getVertexAttribute(Usage.Normal).offset / 4; 
		final float[] vertices = subMesh.vertices;
		final float[] skinnedVertices = subMesh.skinnedVertices;
			
		System.arraycopy(subMesh.vertices, 0, skinnedVertices, 0, subMesh.vertices.length);
		
		for(int i = 0; i < numVertices; i++, idx += stride, nidx += stride) {
			final int[] boneIndices = subMesh.boneAssignments[i];
			final float[] boneWeights = subMesh.boneWeights[i];
			
			final float ox = vertices[idx], oy = vertices[idx+1], oz = vertices[idx+2];			
			float x = 0, y = 0, z = 0;
			final float onx = 0, ony = 0, onz = 0;
			float nx = 0, ny = 0, nz = 0;
			
			if(nidx != -1) {
				nx = vertices[nidx];
				ny = vertices[nidx+1];
				nz = vertices[nidx+2];
			}
			
			for(int j = 0; j < boneIndices.length; j++) {
				int boneIndex = boneIndices[j];
				float weight = boneWeights[j];
				v.set(ox, oy, oz);
				v.mul(boneMatrices.get(boneIndex));
				x += v.x * weight;
				y += v.y * weight;
				z += v.z * weight;
				
				if(nidx != -1) {
					v.set(onx, ony, onz);
					v.rot(boneMatrices.get(boneIndex));
					nx += v.x * weight;
					ny += v.y * weight;
					nz += v.z * weight;
				}
			}			
			
			skinnedVertices[idx] = x;
			skinnedVertices[idx+1] = y;
			skinnedVertices[idx+2] = z;
			
			if(nidx != -1) {
				skinnedVertices[nidx] = nx;
				skinnedVertices[nidx+1] = ny;
				skinnedVertices[nidx+2] = nz;
			}
		}
		
		subMesh.mesh.setVertices(skinnedVertices);
	}
	
	public void render() {
		int len = subMeshes.length;
		for(int i = 0; i < len; i++) {
			SubMesh subMesh = subMeshes[i];
			if(i == 0 ) {
				subMesh.material.bind();
			} else if (!subMeshes[i-1].material.equals(subMesh.material)) {
				subMesh.material.bind();
			}
			subMesh.mesh.render(subMesh.primitiveType);
		}	
	}	
	
	public void setMaterials(Material ... materials) {
		if(materials.length != subMeshes.length) throw new UnsupportedOperationException("number of materials must equal number of sub-meshes");
		int len = materials.length;
		for(int i = 0; i < len; i++) {
			subMeshes[i].material = materials[i];
		}
	}
	
	public void setMaterial(Material material) {
		int len = subMeshes.length;
		for(int i = 0; i < len; i++) {
			subMeshes[i].material = material;
		}
	}
	
	public SubMesh getSubMesh(String name) {
		int len = subMeshes.length;
		for(int i = 0; i < len; i++) {
			if(subMeshes[i].name.equals(name)) return subMeshes[i];
		}		
		return null;
	}
}
