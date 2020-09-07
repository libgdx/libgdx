/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ModelInstanceParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ModelInfluencer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Arrays;

/** @author jsjolund */
public class ParticleSystemBlendingTest extends BaseG3dTest {

	private static final String PARTICLE_TEX = "data/pre_particle.png";
	private static final String BILLBOARD_PFX = "data/billboard_template.pfx"; // Red flame
	private static final String POINTSPRITE_PFX = "data/pointsprite_template.pfx"; // Blue flame
	private static final String MODELINSTANCE_PFX = "data/modelinstance_template.pfx"; // Spheres
	private static final String CUBE_MODEL = "data/cube.obj";
	private static final String SCENE_MODEL = "data/scene.obj";

	private ParticleSystem particleSystem;
	private BillboardParticleBatch billboardParticleBatch;
	private ModelInstanceParticleBatch modelInstanceParticleBatch;
	private PointSpriteParticleBatch pointSpriteParticleBatch;
	Environment environment;

	private ParticleEffect addEffect (String pfx, Vector3 position) {
		ParticleEffect effect = assets.get(pfx, ParticleEffect.class).copy();
		effect.translate(position);
		effect.init();
		effect.start();
		effect.setBatch(particleSystem.getBatches());
		particleSystem.add(effect);
		return effect;
	}

	private ModelInstance addModel (String model, Vector3 position, float opacity) {
		ModelInstance modelInstance = new ModelInstance(assets.get(model, Model.class));
		if (opacity < 1) modelInstance.materials.first().set(new BlendingAttribute(opacity));
		modelInstance.transform.rotate(Vector3.X, 180).setTranslation(position);
		instances.add(modelInstance);
		return modelInstance;
	}

	@Override
	public void create () {
		super.create();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, 0.1f, 1f));
		environment.add(new DirectionalLight().set(1f, 1f, 1f, -0.5f, -0.5f, -1f));

		particleSystem = new ParticleSystem();

		modelInstanceParticleBatch = new ModelInstanceParticleBatch();

		pointSpriteParticleBatch = new PointSpriteParticleBatch();
		pointSpriteParticleBatch.setCamera(cam);

		billboardParticleBatch = new BillboardParticleBatch();
		billboardParticleBatch.setCamera(cam);
		billboardParticleBatch.setUseGpu(false);
		billboardParticleBatch.setAlignMode(ParticleShader.AlignMode.ViewPoint);

		particleSystem.add(modelInstanceParticleBatch);
		particleSystem.add(billboardParticleBatch);
		particleSystem.add(pointSpriteParticleBatch);

		assets.load(CUBE_MODEL, Model.class);
		assets.load(SCENE_MODEL, Model.class);
		assets.load(PARTICLE_TEX, Texture.class);
		assets.load(BILLBOARD_PFX, ParticleEffect.class);
		assets.load(MODELINSTANCE_PFX, ParticleEffect.class);
		assets.load(POINTSPRITE_PFX, ParticleEffect.class);

		loading = true;
	}

	@Override
	protected void onLoaded () {
		pointSpriteParticleBatch.setTexture(assets.get(PARTICLE_TEX, Texture.class));
		billboardParticleBatch.setTexture(assets.get(PARTICLE_TEX, Texture.class));

		// Scale down the model of ModelInstance particle effect
		assets.get(MODELINSTANCE_PFX, ParticleEffect.class).getControllers().first().findInfluencer(ModelInfluencer.class).models
			.first().nodes.first().scale.set(0.2f, 0.2f, 0.2f);

		String[] pfx = new String[] {MODELINSTANCE_PFX, BILLBOARD_PFX, POINTSPRITE_PFX};
		float d = 3f;
		for (int z = -1; z <= 1; z++) {
			// Add translucent boxes
			for (int x = -1; x <= 1; x++)
				addModel(CUBE_MODEL, new Vector3(d * x, 1, d * z), 0.5f);

			// Add particles to the side of the boxes.
			addEffect(pfx[(z + 1) % pfx.length], new Vector3(d * 1.5f, 1f, d * z));
			addEffect(pfx[(z + 3) % pfx.length], new Vector3(d * -1.5f, 1f, d * z));

			// FIXME: Billboard and PointSprite particles intersecting translucent models are not rendered correctly.
			// addEffect(pfx[(z + 2) % pfx.length], new Vector3(0f, 1.5f, d * z));
		}
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		for (ModelInstance instance : instances) {
			batch.render(instance, environment);
		}
		particleSystem.update();
		particleSystem.begin();
		particleSystem.draw();
		particleSystem.end();
		batch.render(particleSystem, environment);
	}

}
