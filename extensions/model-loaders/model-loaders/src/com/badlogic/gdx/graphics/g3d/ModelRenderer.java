package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.model.still.StillModel;

public interface ModelRenderer {
	public void begin();
	public void draw(StillModel model, StillModelInstance instance);
	public void draw(StillModel model, AnimatedModelInstance instance);
	public void end();
}
