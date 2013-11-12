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

package com.badlogic.gdx.graphics.g3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class DepthShader extends DefaultShader {
	public static class Config extends DefaultShader.Config {
		public boolean depthBufferOnly = false;
		
		public Config () {
			super();
		}
		public Config (String vertexShader, String fragmentShader) {
			super(vertexShader, fragmentShader);
		}
	}
	
	private static String defaultVertexShader = null;
	public final static String getDefaultVertexShader() {
		if (defaultVertexShader == null)
			defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/depth.vertex.glsl").readString();
		return defaultVertexShader;
	}
	
	private static String defaultFragmentShader = null;
	public final static String getDefaultFragmentShader() {
		if (defaultFragmentShader == null)
			defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/depth.fragment.glsl").readString();
		return defaultFragmentShader;
	}
	
	public static String createPrefix(final Renderable renderable, final Config config) {
		String prefix = "";
		final long mask = renderable.material.getMask();
		final long attributes = renderable.mesh.getVertexAttributes().getMask();
		if ((attributes & Usage.BoneWeight) == Usage.BoneWeight) {
			final int n = renderable.mesh.getVertexAttributes().size();
			for (int i = 0; i < n; i++) {
				final VertexAttribute attr = renderable.mesh.getVertexAttributes().get(i);
				if (attr.usage == Usage.BoneWeight)
					prefix += "#define boneWeight"+attr.unit+"Flag\n";
			}
		}
		// FIXME Add transparent texture support
//		if ((mask & BlendingAttribute.Type) == BlendingAttribute.Type)
//			prefix += "#define "+BlendingAttribute.Alias+"Flag\n";
//		if ((mask & TextureAttribute.Diffuse) == TextureAttribute.Diffuse)
//			prefix += "#define "+TextureAttribute.DiffuseAlias+"Flag\n";
		if (renderable.bones != null && config.numBones > 0)
			prefix += "#define numBones "+config.numBones+"\n";
		if (!config.depthBufferOnly)
			prefix += "#define PackedDepthFlag\n";
		return prefix;
	}
	
	public final int numBones;
	public final int weights;
	
	public DepthShader(final Renderable renderable) {
		this(renderable, new Config());
	}
	
	public DepthShader(final Renderable renderable, final Config config) {
		this(renderable, config, createPrefix(renderable, config));
	}

	public DepthShader(final Renderable renderable, final Config config, final String prefix) {
		this(renderable, config, prefix, 
				config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(), 
				config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader());
	}
	
	public DepthShader(final Renderable renderable, final Config config, final String prefix, final String vertexShader, final String fragmentShader) {
		this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
	}
	
	public DepthShader(final Renderable renderable, final Config config, final ShaderProgram shaderProgram) {
		super(renderable, config, shaderProgram);
		this.numBones = renderable.bones == null ? 0 : config.numBones;
		int w = 0;
		final int n = renderable.mesh.getVertexAttributes().size();
		for (int i = 0; i < n; i++) {
			final VertexAttribute attr = renderable.mesh.getVertexAttributes().get(i);
			if (attr.usage == Usage.BoneWeight)
				w |= (1 << attr.unit);
		}
		weights = w;
	}
	
	private int originalCullFace;	
	@Override
	public void begin (Camera camera, RenderContext context) {
		originalCullFace = DefaultShader.defaultCullFace;
		DefaultShader.defaultCullFace = GL10.GL_FRONT; //0; //GL10.GL_BACK; //GL10.GL_FRONT;
		super.begin(camera, context);
		//Gdx.gl20.glEnable(GL20.GL_POLYGON_OFFSET_FILL);
		//Gdx.gl20.glPolygonOffset(2.f, 100.f);
	}
	
	@Override
	public void end () {
		super.end();
		DefaultShader.defaultCullFace = originalCullFace;
		Gdx.gl20.glDisable(GL20.GL_POLYGON_OFFSET_FILL);
	}
	
	@Override
	public boolean canRender (Renderable renderable) {
		final boolean skinned = ((renderable.mesh.getVertexAttributes().getMask() & Usage.BoneWeight) == Usage.BoneWeight);
		if (skinned != (numBones > 0))
			return false;
		if (!skinned)
			return true;
		int w = 0;
		final int n = renderable.mesh.getVertexAttributes().size();
		for (int i = 0; i < n; i++) {
			final VertexAttribute attr = renderable.mesh.getVertexAttributes().get(i);
			if (attr.usage == Usage.BoneWeight)
				w |= (1 << attr.unit);
		}
		return w == weights;
	}
}