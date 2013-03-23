package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.graphics.g3d.RenderInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;

public interface NewModel {
	Iterable<Renderable> getParts(float distance);
}
