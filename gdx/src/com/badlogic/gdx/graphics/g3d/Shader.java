package com.badlogic.gdx.graphics.g3d;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;
import com.badlogic.gdx.graphics.g3d.old.materials.Material;
import com.badlogic.gdx.graphics.g3d.test.Light;
import com.badlogic.gdx.graphics.g3d.test.NewModel;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Matrix4;

public interface Shader extends Comparator<RenderInstance> {
	int compareTo(Shader other); // TODO: probably better to add some weight value to sort on
	boolean canRender(RenderInstance instance);
	void begin(Camera camera, RenderContext context);
	void render(final RenderInstance instance);
	void render(final NewModel model, final Matrix4 transform, final Light[] lights);
	void end();
}
