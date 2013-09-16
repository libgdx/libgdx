package com.badlogic.gdx.graphics.g3d.lights;

import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.math.Matrix4;

public interface ShadowMap {
	Matrix4 getProjViewTrans();
	TextureDescriptor getDepthMap();
}
