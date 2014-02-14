package com.badlogic.gdx.graphics.g3d.particles;

import java.awt.print.Printable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

public class ParticleEmitterNode extends Node {
	private static final Matrix4 TMP_MATRIX4 = new Matrix4();
	
	public ParticleEmitter emitter;
	public ParticleEmitterNode(ParticleEmitter emitter){
		this.emitter = emitter;
	}
	
	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool, Matrix4 modelTransform, Object userData) {
		//Calculate world transform
		TMP_MATRIX4.set(modelTransform).mul(globalTransform);
		emitter.setTransform(TMP_MATRIX4);
		emitter.updateMesh();
		renderables.add(pool.obtain().set(emitter.getRenderable()));
	}

	@Override
	public Node copy (ObjectMap<NodePart, ArrayMap<Node, Matrix4>> nodePartBones, Array<Material> materials) {
		return super.copy(new ParticleEmitterNode(new ParticleEmitter(emitter)), nodePartBones, materials);
	}
}
