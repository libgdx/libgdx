package com.badlogic.gdx.graphics.g3d.xoppa;

import java.util.Comparator;

public interface RenderBatchListener extends Comparator<RenderInstance> {
	/** Returns the shader to be used for this instance. If the suggestedShader isn't null the preferred action is to return
	 * that shader, but the implementation can choose to return another shader. */
	Shader getShader(RenderInstance instance, Shader suggestedShader);
}
