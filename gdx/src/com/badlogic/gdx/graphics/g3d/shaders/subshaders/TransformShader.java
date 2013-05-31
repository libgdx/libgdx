package com.badlogic.gdx.graphics.g3d.shaders.subshaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;

/**
 * Responsible for calculating the world space vertex position
 * @author badlogic
 *
 */
public class TransformShader implements SubShader {
	private Array<String> vertexVars = new Array<String>(new String[] {
		"uniform mat4 u_worldTrans;"
	});
	private String[] vertexCode = {
		"#ifdef skinnedFlag",
		"  mat4 skinning = mat4(0.0);",
		"  #ifdef boneWeight0Flag",
		"    skinning += (a_boneWeight0.y) * u_bones[int(a_boneWeight0.x)];",
		"  #endif",
		"  #ifdef boneWeight1Flag",
		"    skinning += (a_boneWeight1.y) * u_bones[int(a_boneWeight1.x)];",
		"  #endif",
		"  #ifdef boneWeight2Flag",
		"    skinning += (a_boneWeight2.y) * u_bones[int(a_boneWeight2.x)];",
		"  #endif",
		"  #ifdef boneWeight3Flag",
		"    skinning += (a_boneWeight3.y) * u_bones[int(a_boneWeight3.x)];",
		"  #endif",
		"  #ifdef boneWeight4Flag",
		"    skinning += (a_boneWeight4.y) * u_bones[int(a_boneWeight4.x)];",
		"  #endif",
		"  #ifdef boneWeight5Flag",
		"    skinning += (a_boneWeight5.y) * u_bones[int(a_boneWeight5.x)];",
		"  #endif",
		"  #ifdef boneWeight6Flag",
		"    skinning += (a_boneWeight6.y) * u_bones[int(a_boneWeight6.x)];",
		"  #endif",
		"  #ifdef boneWeight7Flag",
		"    skinning += (a_boneWeight7.y) * u_bones[int(a_boneWeight7.x)];",
		"  #endif",
		"  vec4 position = u_worldTrans * skinning * vec4(a_position, 1.0);",
		"#else",
		"  vec4 position = u_worldTrans * vec4(" + ShaderProgram.POSITION_ATTRIBUTE + ", 1);",
		"#endif",
		"position = u_projTrans * position;"
	};
	private String[] fragmentVars = {
		
	};
	private String[] fragmentCode = {
		"vec4 color = vec4(1, 1, 1, 1);"
	};
	
	private int NUM_BONES = 12; // FIXME kinda arbitrary eh...
	private boolean skinned;
	private float[] bones = new float[NUM_BONES * 16];
	private Matrix4 idtMatrix = new Matrix4();
	
	@Override
	public void init (Renderable renderable) {
		// emit vertex attributes that are influenced by the transformation
		VertexAttributes attributes = renderable.mesh.getVertexAttributes();
		int boneWeightsPerVertex = 0;
		for(int i = 0; i < attributes.size(); i++) {
			VertexAttribute attr = attributes.get(i);
			if(attr.usage == VertexAttributes.Usage.Position) vertexVars.add("attribute vec3 " + attr.alias + ";");
			if(attr.usage == VertexAttributes.Usage.Normal) vertexVars.add("attribute vec3 " + attr.alias + ";");
			if(attr.usage == VertexAttributes.Usage.BiNormal) vertexVars.add("attribute vec3 " + attr.alias + ";");
			if(attr.usage == VertexAttributes.Usage.Tangent) vertexVars.add("attribute vec3 " + attr.alias + ";");
			if(attr.usage == VertexAttributes.Usage.BoneWeight) {
				vertexVars.add("#define boneWeight" + boneWeightsPerVertex + "Flag");
				vertexVars.add("attribute vec2 " + attr.alias + ";");
				boneWeightsPerVertex++;
			}
		}
		
		// check if we need skinning, emit code for #bones and bone matrix stack
		if(boneWeightsPerVertex > 0) {
			skinned = true;
			vertexVars.add("#define skinnedFlag");
			vertexVars.add("#define numBones " + NUM_BONES);
			vertexVars.add("uniform mat4 u_bones[numBones];");
		}
	}
	
	@Override
	public void apply (ShaderProgram program, RenderContext context, Camera camera, Renderable renderable) {
		program.setUniformMatrix("u_worldTrans", renderable.worldTransform);
		if(skinned) {
			for (int i = 0, offset = 0; i < NUM_BONES; i++, offset += 16) {
				Matrix4 mat = null;
				if(renderable.bones != null && i < renderable.bones.length && renderable.bones[i] != null) {
					mat = renderable.bones[i];
				} else {
					mat = idtMatrix;
				}
				System.arraycopy(mat.val, 0, bones, offset, 16);
			}
			program.setUniformMatrix4fv("u_bones", bones, 0, bones.length);
		}
	}
	
	@Override
	public String[] getVertexShaderVars () {
		return vertexVars.toArray();
	}

	@Override
	public String[] getVertexShaderCode () {
		return vertexCode;
	}

	@Override
	public String[] getFragmentShaderVars () {
		return fragmentVars;
	}

	@Override
	public String[] getFragmentShaderCode () {
		return fragmentCode;
	}
}
