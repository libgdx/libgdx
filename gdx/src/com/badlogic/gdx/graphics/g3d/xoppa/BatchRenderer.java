package com.badlogic.gdx.graphics.g3d.xoppa;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public interface BatchRenderer extends Comparator<RenderInstance> {
	ShaderProgram getShader(RenderInstance instance);
	void render(Camera camera, Array<RenderInstance> instances /*, Array<Light> lights*/);
}
