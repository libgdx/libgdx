package com.badlogic.gdx.graphics.g3d.xoppa.test;

import com.badlogic.gdx.graphics.g3d.xoppa.RenderInstance;
import com.badlogic.gdx.graphics.g3d.xoppa.Renderable;

public interface NewModel {
	Iterable<Renderable> getParts(float distance);
}
