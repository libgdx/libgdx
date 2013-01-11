package com.badlogic.gdx.graphics.g3d.xoppa;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Camera;

public interface Shader extends Comparator<RenderInstance> {
	int compareTo(Shader other); // TODO: probably better to add some weight value to sort on
	void begin(Camera camera);
	void render(final RenderInstance instance);
	void end();
}
