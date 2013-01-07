package com.badlogic.gdx.graphics.g3d.xoppa;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.math.Matrix4;

public interface RenderBatch {
	void begin(Camera cam);
	void end();
	void addMesh(final SubMesh mesh, final Matrix4 transform);
	void addMesh (final SubMesh mesh, final Matrix4 transform, float distance);
	void addModel(final Model model, final Matrix4 transform);
	void addModel(final Model model, final Matrix4 transform, float distance);
	//void addLight(Light light);
}
