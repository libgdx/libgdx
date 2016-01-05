package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.utils.Array;

/** Simple test showing the use of {@link ModelCache}.
 * @author Xoppa */
public class ModelCacheTest extends Benchmark3DTest {
	ModelCache modelCache;
	CheckBox cacheCheckBox;
	
	@Override
	public void create () {
		super.create();
		modelCache = new ModelCache();

		cacheCheckBox = new CheckBox("Cache", skin);
		cacheCheckBox.setChecked(false);
		cacheCheckBox.setPosition(hudWidth - cacheCheckBox.getWidth(), moveCheckBox.getTop());
		hud.addActor(cacheCheckBox);
	}
	
	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		if (cacheCheckBox.isChecked()) {
			modelCache.begin();
			modelCache.add(instances);
			modelCache.end();
			batch.render(modelCache, lighting ? environment : null);
		}
		else {
			batch.render(instances, lighting ? environment : null);
		}
	}
	
	@Override
	public void dispose () {
		super.dispose();
		modelCache.dispose();
	}
}
