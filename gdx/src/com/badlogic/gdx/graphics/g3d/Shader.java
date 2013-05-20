package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.utils.Disposable;

public interface Shader extends Disposable {
	int compareTo(Shader other); // TODO: probably better to add some weight value to sort on
	boolean canRender(Renderable instance);
	void begin(Camera camera, RenderContext context);
	void render(final Renderable renderable);
	void end();
}
