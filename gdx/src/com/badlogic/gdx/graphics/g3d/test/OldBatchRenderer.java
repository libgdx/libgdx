package com.badlogic.gdx.graphics.g3d.test;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.RenderInstance;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public interface OldBatchRenderer extends Comparator<RenderInstance> {
	ShaderProgram getShader(RenderInstance instance);
	void render(Camera camera, Array<RenderInstance> instances /*, Array<Light> lights*/);
}
