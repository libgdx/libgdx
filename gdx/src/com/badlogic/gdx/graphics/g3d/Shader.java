package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.utils.Disposable;

public interface Shader extends Disposable {
	/** Initializes the Shader, must be called before the Shader can be used */
	void init();
	/** Compare this shader against the other, used for sorting, light weight shaders are rendered first. */
	int compareTo(Shader other); // TODO: probably better to add some weight value to sort on
	/** Whether this shader is intended to render the {@link Renderable} */
	boolean canRender(Renderable instance);
	/** Initializes the context for exclusive rendering by this shader */
	void begin(Camera camera, RenderContext context);
	/** Renders the {@link Renderable} must be called between {@link #begin(Camera, RenderContext)} and {@link #end()} */
	void render(final Renderable renderable);
	/** Cleanup the context so other shaders can render */
	void end();
}
