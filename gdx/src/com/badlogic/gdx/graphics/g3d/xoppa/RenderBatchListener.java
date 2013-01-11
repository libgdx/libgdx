package com.badlogic.gdx.graphics.g3d.xoppa;

import java.util.Comparator;

public interface RenderBatchListener extends Comparator<RenderInstance> {
	Shader getShader(RenderInstance instance);
}
