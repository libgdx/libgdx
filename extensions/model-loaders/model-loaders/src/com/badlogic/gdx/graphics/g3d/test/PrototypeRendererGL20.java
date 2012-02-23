package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.graphics.g3d.AnimatedModelInstance;
import com.badlogic.gdx.graphics.g3d.ModelRenderer;
import com.badlogic.gdx.graphics.g3d.StillModelInstance;
import com.badlogic.gdx.graphics.g3d.model.AnimatedModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.utils.Array;

public class PrototypeRendererGL20 implements ModelRenderer {

	// static final int SIZE = 128;
	// Array<StillModel> StillModelQueue = new
	// Array<StillModel>(false,SIZE,StillModel.class);
	// Array<StillModel> AnimatedModelQueue = new
	// Array<StillModel>(false,SIZE,StillModel.class);

	@Override
	public void begin() {
		// all setting has to be done before this

		// example: camera updating or updating lights positions
	}

	@Override
	public void draw(StillModel model, StillModelInstance instance) {
		// add render queue
	}

	@Override
	public void draw(AnimatedModel model, AnimatedModelInstance instance) {
		// add animated render queue
	}

	@Override
	public void end() {

		// TODO how materials is accounted

		// batched frustum vs bounding box culling(if slow JNI) for all models

		// sort models(submeshes??)to tranparent and opaque render queue

		// frustum culling for all point lights (sphere)

		// find N nearest lights per model

		// draw for opaque queu

		// if transparent queue is not empty enable blending(this force gpu to
		// flush and there is some time to sort)

		// sort transparent models(submeshes??)

		// do drawing for transparent models

		// clear all queus

	}

}
