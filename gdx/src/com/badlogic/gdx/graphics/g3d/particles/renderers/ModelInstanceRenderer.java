/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.batches.ModelInstanceParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/** A {@link ParticleControllerRenderer} which will render particles as {@link ModelInstance} to a
 * {@link ModelInstanceParticleBatch}.
 * @author Inferno */
public class ModelInstanceRenderer extends
	ParticleControllerRenderer<ModelInstanceControllerRenderData, ModelInstanceParticleBatch> {
	private boolean hasColor, hasScale, hasRotation;

	public ModelInstanceRenderer () {
		super(new ModelInstanceControllerRenderData());
	}

	public ModelInstanceRenderer (ModelInstanceParticleBatch batch) {
		this();
		setBatch(batch);
	}

	@Override
	public void allocateChannels () {
		renderData.positionChannel = controller.particles.addChannel(ParticleChannels.Position);
	}

	@Override
	public void init () {
		renderData.modelInstanceChannel = controller.particles.getChannel(ParticleChannels.ModelInstance);
		renderData.colorChannel = controller.particles.getChannel(ParticleChannels.Color);
		renderData.scaleChannel = controller.particles.getChannel(ParticleChannels.Scale);
		renderData.rotationChannel = controller.particles.getChannel(ParticleChannels.Rotation3D);
		hasColor = renderData.colorChannel != null;
		hasScale = renderData.scaleChannel != null;
		hasRotation = renderData.rotationChannel != null;
	}

	@Override
	public void update () {
		Quaternion rot = TMP_Q2;
		Vector3 pos = TMP_V2;
		Vector3 scl = TMP_V3;
		Matrix4 worldTransform = null;
		Quaternion worldTransformRot = null;
		Vector3 worldTransformScl = null;
		if (renderData.controller.worldTransform != null) {
			worldTransform = TMP_M4.set(renderData.controller.worldTransform);
			worldTransformRot = worldTransform.getRotation(TMP_Q, true);
			worldTransformScl = worldTransform.getScale(TMP_V1);
		}
		for (int i = 0, positionOffset = 0, c = controller.particles.size; i < c; ++i, positionOffset += renderData.positionChannel.strideSize) {
			ModelInstance instance = renderData.modelInstanceChannel.data[i];
			float scale = hasScale ? renderData.scaleChannel.data[i] : 1;
			scl.set(scale, scale, scale);
			if (worldTransformScl != null && renderData.controller.worldTransformScalesParticles) scl.scl(worldTransformScl);

			if (hasRotation) {
				int rotationOffset = i * renderData.rotationChannel.strideSize;
				rot.x = renderData.rotationChannel.data[rotationOffset + ParticleChannels.XOffset];
				rot.y = renderData.rotationChannel.data[rotationOffset + ParticleChannels.YOffset];
				rot.z = renderData.rotationChannel.data[rotationOffset + ParticleChannels.ZOffset];
				rot.w = renderData.rotationChannel.data[rotationOffset + ParticleChannels.WOffset];
			} else {
				rot.idt();
			}
			if (worldTransformRot != null) rot.mul(worldTransformRot);

			pos.x = renderData.positionChannel.data[positionOffset + ParticleChannels.XOffset];
			pos.y = renderData.positionChannel.data[positionOffset + ParticleChannels.YOffset];
			pos.z = renderData.positionChannel.data[positionOffset + ParticleChannels.ZOffset];
			if (worldTransform != null) pos.mul(worldTransform);

			instance.transform.set(pos, rot, scl);
			if (hasColor) {
				int colorOffset = i * renderData.colorChannel.strideSize;
				ColorAttribute colorAttribute = (ColorAttribute)instance.materials.get(0).get(ColorAttribute.Diffuse);
				BlendingAttribute blendingAttribute = (BlendingAttribute)instance.materials.get(0).get(BlendingAttribute.Type);
				colorAttribute.color.r = renderData.colorChannel.data[colorOffset + ParticleChannels.RedOffset];
				colorAttribute.color.g = renderData.colorChannel.data[colorOffset + ParticleChannels.GreenOffset];
				colorAttribute.color.b = renderData.colorChannel.data[colorOffset + ParticleChannels.BlueOffset];
				if (blendingAttribute != null)
					blendingAttribute.opacity = renderData.colorChannel.data[colorOffset + ParticleChannels.AlphaOffset];
			}
		}
		super.update();
	}

	@Override
	public ParticleControllerComponent copy () {
		return new ModelInstanceRenderer(batch);
	}

	@Override
	public boolean isCompatible (ParticleBatch<?> batch) {
		return batch instanceof ModelInstanceParticleBatch;
	}

}
