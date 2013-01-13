package com.badlogic.gdx.graphics.g3d.xoppa;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.materials.Material;

public interface Shader extends Comparator<RenderInstance> {
	int compareTo(Shader other); // TODO: probably better to add some weight value to sort on
	boolean canRender(Material material);
	void begin(Camera camera, RenderContext context);
	void render(final RenderInstance instance);
	void end();
}
