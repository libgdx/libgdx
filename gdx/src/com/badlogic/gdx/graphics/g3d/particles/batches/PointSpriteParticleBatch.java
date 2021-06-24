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

package com.badlogic.gdx.graphics.g3d.particles.batches;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader.ParticleType;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointSpriteControllerRenderData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/** This class is used to draw particles as point sprites.
 * @author Inferno */
public class PointSpriteParticleBatch extends BufferedParticleBatch<PointSpriteControllerRenderData> {

	private class RenderablePool extends Pool<Renderable> {
		RenderablePool () {
		}

		@Override
		public Renderable newObject () {
			return allocRenderable();
		}
	}

	private static boolean pointSpritesEnabled = false;
	protected static final Vector3 TMP_V1 = new Vector3();
	protected static final int sizeAndRotationUsage = 1 << 9;
	protected static final VertexAttributes CPU_ATTRIBUTES = new VertexAttributes(new VertexAttribute(Usage.Position, 3,
		ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
		new VertexAttribute(Usage.TextureCoordinates, 4, "a_region"), new VertexAttribute(sizeAndRotationUsage, 3,
			"a_sizeAndRotation"));
	protected static final int CPU_VERTEX_SIZE = (short)(CPU_ATTRIBUTES.vertexSize / 4),
		CPU_POSITION_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.Position).offset / 4),
		CPU_COLOR_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.ColorUnpacked).offset / 4),
		CPU_REGION_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.TextureCoordinates).offset / 4),
		CPU_SIZE_AND_ROTATION_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(sizeAndRotationUsage).offset / 4);
	protected static final int MAX_PARTICLES_PER_MESH = Short.MAX_VALUE, MAX_VERTICES_PER_MESH = Short.MAX_VALUE;

	private static void enablePointSprites () {
		Gdx.gl.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
		if (Gdx.app.getType() == ApplicationType.Desktop) {
			Gdx.gl.glEnable(0x8861); // GL_POINT_OES
		}
		pointSpritesEnabled = true;
	}

	private float[] vertices;
	protected BlendingAttribute blendingAttribute;
	protected DepthTestAttribute depthTestAttribute;
	private RenderablePool renderablePool;
	protected Array<Renderable> renderables;
	protected Shader shader;
	protected Texture texture;

	public PointSpriteParticleBatch () {
		this(1000);
	}

	public PointSpriteParticleBatch (int capacity) {
		this(capacity, new ParticleShader.Config(ParticleType.Point));
	}
	
	public PointSpriteParticleBatch (int capacity, ParticleShader.Config shaderConfig) {
		this(capacity, shaderConfig, null, null);
	}

	public PointSpriteParticleBatch (int capacity, ParticleShader.Config shaderConfig, BlendingAttribute blendingAttribute,
		DepthTestAttribute depthTestAttribute) {
		super(PointSpriteControllerRenderData.class);

		if (!pointSpritesEnabled) enablePointSprites();

		this.blendingAttribute = blendingAttribute;
		this.depthTestAttribute = depthTestAttribute;

		if (this.blendingAttribute == null)
			this.blendingAttribute = new BlendingAttribute(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f);
		if (this.depthTestAttribute == null) this.depthTestAttribute = new DepthTestAttribute(GL20.GL_LEQUAL, false);

		renderables = new Array<Renderable>();
		renderablePool = new RenderablePool();
		blendingAttribute = new BlendingAttribute(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f);
		depthTestAttribute = new DepthTestAttribute(GL20.GL_LEQUAL, false);
		clearRenderablesPool();
		allocShader(shaderConfig);
		resetCapacity();
		ensureCapacity(capacity);
		allocRenderables(bufferedParticlesCount);
	}

	@Override
	protected void allocParticlesData (int capacity) {
		vertices = new float[capacity * CPU_VERTEX_SIZE];
		allocRenderables(capacity);
	}

	protected Renderable allocRenderable () {
		Renderable renderable = new Renderable();
		renderable.meshPart.primitiveType = GL20.GL_POINTS;
		renderable.meshPart.offset = 0;
		renderable.material = new Material(blendingAttribute, depthTestAttribute, TextureAttribute.createDiffuse(texture));
		renderable.meshPart.mesh = new Mesh(false, MAX_VERTICES_PER_MESH, 0, CPU_ATTRIBUTES);
		renderable.shader = shader;
		return renderable;
	}

	private void allocRenderables (int capacity) {
		// Free old meshes
		int meshCount = MathUtils.ceil(capacity / MAX_PARTICLES_PER_MESH), free = renderablePool.getFree();
		if (free < meshCount) {
			for (int i = 0, left = meshCount - free; i < left; ++i)
				renderablePool.free(renderablePool.newObject());
		}
	}

	private void allocShader (ParticleShader.Config shaderConfig) {
		Renderable newRenderable = allocRenderable();
		shader = newRenderable.shader = new ParticleShader(newRenderable, shaderConfig);
		newRenderable.shader.init();
		renderablePool.free(newRenderable);
	}

	@Override
	public void begin () {
		super.begin();
		renderablePool.freeAll(renderables);
		renderables.clear();
	}

	private void clearRenderablesPool () {
		renderablePool.freeAll(renderables);
		for (int i = 0, free = renderablePool.getFree(); i < free; ++i) {
			Renderable renderable = renderablePool.obtain();
			renderable.meshPart.mesh.dispose();
		}
		renderables.clear();
	}

	public void setTexture (Texture texture) {
		renderablePool.freeAll(renderables);
		renderables.clear();
		for (int i = 0, free = renderablePool.getFree(); i < free; ++i) {
			Renderable renderable = renderablePool.obtain();
			TextureAttribute attribute = (TextureAttribute)renderable.material.get(TextureAttribute.Diffuse);
			attribute.textureDescription.texture = texture;
		}
		this.texture = texture;
	}

	public Texture getTexture () {
		return texture;
	}

	public BlendingAttribute getBlendingAttribute () {
		return blendingAttribute;
	}

	@Override
	protected void flush (int[] offsets) {
		int tp = 0;
		for (PointSpriteControllerRenderData data : renderData) {
			FloatChannel scaleChannel = data.scaleChannel;
			FloatChannel regionChannel = data.regionChannel;
			FloatChannel positionChannel = data.positionChannel;
			FloatChannel colorChannel = data.colorChannel;
			FloatChannel rotationChannel = data.rotationChannel;

			int lastTp = tp;
			for (int p = 0; p < data.controller.particles.size; ++p, ++tp) {
				int offset = offsets[tp] * CPU_VERTEX_SIZE;
				int regionOffset = p * regionChannel.strideSize;
				int positionOffset = p * positionChannel.strideSize;
				int colorOffset = p * colorChannel.strideSize;
				int rotationOffset = p * rotationChannel.strideSize;

				vertices[offset + CPU_POSITION_OFFSET] = positionChannel.data[positionOffset + ParticleChannels.XOffset];
				vertices[offset + CPU_POSITION_OFFSET + 1] = positionChannel.data[positionOffset + ParticleChannels.YOffset];
				vertices[offset + CPU_POSITION_OFFSET + 2] = positionChannel.data[positionOffset + ParticleChannels.ZOffset];
				vertices[offset + CPU_COLOR_OFFSET] = colorChannel.data[colorOffset + ParticleChannels.RedOffset];
				vertices[offset + CPU_COLOR_OFFSET + 1] = colorChannel.data[colorOffset + ParticleChannels.GreenOffset];
				vertices[offset + CPU_COLOR_OFFSET + 2] = colorChannel.data[colorOffset + ParticleChannels.BlueOffset];
				vertices[offset + CPU_COLOR_OFFSET + 3] = colorChannel.data[colorOffset + ParticleChannels.AlphaOffset];
				vertices[offset + CPU_SIZE_AND_ROTATION_OFFSET] = scaleChannel.data[p * scaleChannel.strideSize];
				vertices[offset + CPU_SIZE_AND_ROTATION_OFFSET + 1] = rotationChannel.data[rotationOffset
					+ ParticleChannels.CosineOffset];
				vertices[offset + CPU_SIZE_AND_ROTATION_OFFSET + 2] = rotationChannel.data[rotationOffset
					+ ParticleChannels.SineOffset];
				vertices[offset + CPU_REGION_OFFSET] = regionChannel.data[regionOffset + ParticleChannels.UOffset];
				vertices[offset + CPU_REGION_OFFSET + 1] = regionChannel.data[regionOffset + ParticleChannels.VOffset];
				vertices[offset + CPU_REGION_OFFSET + 2] = regionChannel.data[regionOffset + ParticleChannels.U2Offset];
				vertices[offset + CPU_REGION_OFFSET + 3] = regionChannel.data[regionOffset + ParticleChannels.V2Offset];
			}
			if (splitRenderablesPerController) {
				int offsetVertices = lastTp * CPU_VERTEX_SIZE;
				int numVertices = data.controller.particles.size;
				Vector3 center = data.controller.transform.getTranslation(TMP_V1);
				addRenderable(offsetVertices, numVertices, center);
			}
		}
		if(!splitRenderablesPerController){
			Renderable renderable = allocRenderable();
			renderable.meshPart.size = bufferedParticlesCount;
			renderable.meshPart.mesh.setVertices(vertices, 0, bufferedParticlesCount * CPU_VERTEX_SIZE);
			renderable.meshPart.update();
			renderables.add(renderable);
		}
	}

	/** Obtains a Renderable from the pool and sets its vertices. If the number of vertices is too large to fit into one Mesh,
	 * multiple Renderables will be used.
	 *
	 * @param offsetVertices Offset into the vertices array.
	 * @param numVertices Number of vertices.
	 * @param center Vector to use as the center of the Mesh (in model space, which is equal to world space in this case). */
	private void addRenderable (int offsetVertices, int numVertices, Vector3 center) {
		Vector3 halfExtents = null;
		float radius = -1;
		int meshVertexCount;
		for (int totalVertexCount = 0; totalVertexCount < numVertices; totalVertexCount += meshVertexCount) {
			meshVertexCount = Math.min(numVertices - totalVertexCount, MAX_VERTICES_PER_MESH);
			Renderable renderable = renderablePool.obtain();
			renderable.meshPart.size = meshVertexCount;
			renderable.meshPart.mesh.setVertices(vertices, offsetVertices + totalVertexCount * CPU_VERTEX_SIZE,
				meshVertexCount * CPU_VERTEX_SIZE);
			if (halfExtents == null) {
				renderable.meshPart.update();
				halfExtents = renderable.meshPart.halfExtents;
				radius = renderable.meshPart.radius;
			} else {
				// If this particle controller was split into multiple meshes, use cached values to avoid costly MeshPart#update()
				renderable.meshPart.halfExtents.set(halfExtents);
				renderable.meshPart.radius = radius;
			}
			renderable.meshPart.center.set(center);
			renderables.add(renderable);
		}
	}

	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		for (Renderable renderable : this.renderables)
			renderables.add(pool.obtain().set(renderable));
	}

	@Override
	public void save (AssetManager manager, ResourceData resources) {
		SaveData data = resources.createSaveData("pointSpriteBatch");
		data.saveAsset(manager.getAssetFileName(getTexture()), Texture.class);
	}

	@Override
	public void load (AssetManager manager, ResourceData resources) {
		SaveData data = resources.getSaveData("pointSpriteBatch");
		if (data != null) setTexture((Texture)manager.get(data.loadAsset()));
	}
}
