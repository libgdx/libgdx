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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;

/** @author Xoppa A BaseShader is a wrapper around a ShaderProgram that keeps track of the uniform and attribute locations. It does
 *         not manage the ShaderPogram, you are still responsible for disposing the ShaderProgram. */
public abstract class BaseShader implements Shader {
	public interface Validator {
		/** @return True if the input is valid for the renderable, false otherwise. */
		boolean validate (final BaseShader shader, final int inputID, final Renderable renderable);
	}

	public interface Setter {
		/** @return True if the uniform only has to be set once per render call, false if the uniform must be set for each renderable. */
		boolean isGlobal (final BaseShader shader, final int inputID);

		void set (final BaseShader shader, final int inputID, final Renderable renderable, final Attributes combinedAttributes);
	}

	public abstract static class GlobalSetter implements Setter {
		@Override
		public boolean isGlobal (final BaseShader shader, final int inputID) {
			return true;
		}
	}

	public abstract static class LocalSetter implements Setter {
		@Override
		public boolean isGlobal (final BaseShader shader, final int inputID) {
			return false;
		}
	}

	public static class Uniform implements Validator {
		public final String alias;
		public final long materialMask;
		public final long environmentMask;
		public final long overallMask;

		public Uniform (final String alias, final long materialMask, final long environmentMask, final long overallMask) {
			this.alias = alias;
			this.materialMask = materialMask;
			this.environmentMask = environmentMask;
			this.overallMask = overallMask;
		}

		public Uniform (final String alias, final long materialMask, final long environmentMask) {
			this(alias, materialMask, environmentMask, 0);
		}

		public Uniform (final String alias, final long overallMask) {
			this(alias, 0, 0, overallMask);
		}

		public Uniform (final String alias) {
			this(alias, 0, 0);
		}

		public boolean validate (final BaseShader shader, final int inputID, final Renderable renderable) {
			final long matFlags = (renderable != null && renderable.material != null) ? renderable.material.getMask() : 0;
			final long envFlags = (renderable != null && renderable.environment != null) ? renderable.environment.getMask() : 0;
			return ((matFlags & materialMask) == materialMask) && ((envFlags & environmentMask) == environmentMask)
				&& (((matFlags | envFlags) & overallMask) == overallMask);
		}
	}

	private final Array<String> uniforms = new Array<String>();
	private final Array<Validator> validators = new Array<Validator>();
	private final Array<Setter> setters = new Array<Setter>();
	private int locations[];
	private final IntArray globalUniforms = new IntArray();
	private final IntArray localUniforms = new IntArray();
	private final IntIntMap attributes = new IntIntMap();

	public ShaderProgram program;
	public RenderContext context;
	public Camera camera;
	private Mesh currentMesh;

	/** Register an uniform which might be used by this shader. Only possible prior to the call to init().
	 * @return The ID of the uniform to use in this shader. */
	public int register (final String alias, final Validator validator, final Setter setter) {
		if (locations != null) throw new GdxRuntimeException("Cannot register an uniform after initialization");
		final int existing = getUniformID(alias);
		if (existing >= 0) {
			validators.set(existing, validator);
			setters.set(existing, setter);
			return existing;
		}
		uniforms.add(alias);
		validators.add(validator);
		setters.add(setter);
		return uniforms.size - 1;
	}

	public int register (final String alias, final Validator validator) {
		return register(alias, validator, null);
	}

	public int register (final String alias, final Setter setter) {
		return register(alias, null, setter);
	}

	public int register (final String alias) {
		return register(alias, null, null);
	}

	public int register (final Uniform uniform, final Setter setter) {
		return register(uniform.alias, uniform, setter);
	}

	public int register (final Uniform uniform) {
		return register(uniform, null);
	}

	/** @return the ID of the input or negative if not available. */
	public int getUniformID (final String alias) {
		final int n = uniforms.size;
		for (int i = 0; i < n; i++)
			if (uniforms.get(i).equals(alias)) return i;
		return -1;
	}

	/** @return The input at the specified id. */
	public String getUniformAlias (final int id) {
		return uniforms.get(id);
	}

	/** Initialize this shader, causing all registered uniforms/attributes to be fetched. */
	public void init (final ShaderProgram program, final Renderable renderable) {
		if (locations != null) throw new GdxRuntimeException("Already initialized");
		if (!program.isCompiled()) throw new GdxRuntimeException(program.getLog());
		this.program = program;

		final int n = uniforms.size;
		locations = new int[n];
		for (int i = 0; i < n; i++) {
			final String input = uniforms.get(i);
			final Validator validator = validators.get(i);
			final Setter setter = setters.get(i);
			if (validator != null && !validator.validate(this, i, renderable))
				locations[i] = -1;
			else {
				locations[i] = program.fetchUniformLocation(input, false);
				if (locations[i] >= 0 && setter != null) {
					if (setter.isGlobal(this, i))
						globalUniforms.add(i);
					else
						localUniforms.add(i);
				}
			}
			if (locations[i] < 0) {
				validators.set(i, null);
				setters.set(i, null);
			}
		}
		if (renderable != null) {
			final VertexAttributes attrs = renderable.meshPart.mesh.getVertexAttributes();
			final int c = attrs.size();
			for (int i = 0; i < c; i++) {
				final VertexAttribute attr = attrs.get(i);
				final int location = program.getAttributeLocation(attr.alias);
				if (location >= 0) attributes.put(attr.getKey(), location);
			}
		}
	}

	@Override
	public void begin (Camera camera, RenderContext context) {
		this.camera = camera;
		this.context = context;
		program.begin();
		currentMesh = null;
		for (int u, i = 0; i < globalUniforms.size; ++i)
			if (setters.get(u = globalUniforms.get(i)) != null) setters.get(u).set(this, u, null, null);
	}

	private final IntArray tempArray = new IntArray();

	private final int[] getAttributeLocations (final VertexAttributes attrs) {
		tempArray.clear();
		final int n = attrs.size();
		for (int i = 0; i < n; i++) {
			tempArray.add(attributes.get(attrs.get(i).getKey(), -1));
		}
		return tempArray.items;
	}

	private Attributes combinedAttributes = new Attributes();

	@Override
	public void render (Renderable renderable) {
		if (renderable.worldTransform.det3x3() == 0) return;
		combinedAttributes.clear();
		if (renderable.environment != null) combinedAttributes.set(renderable.environment);
		if (renderable.material != null) combinedAttributes.set(renderable.material);
		render(renderable, combinedAttributes);
	}

	public void render (Renderable renderable, final Attributes combinedAttributes) {
		for (int u, i = 0; i < localUniforms.size; ++i)
			if (setters.get(u = localUniforms.get(i)) != null) setters.get(u).set(this, u, renderable, combinedAttributes);
		if (currentMesh != renderable.meshPart.mesh) {
			if (currentMesh != null) currentMesh.unbind(program, tempArray.items);
			currentMesh = renderable.meshPart.mesh;
			currentMesh.bind(program, getAttributeLocations(renderable.meshPart.mesh.getVertexAttributes()));
		}
		renderable.meshPart.render(program, false);
	}

	@Override
	public void end () {
		if (currentMesh != null) {
			currentMesh.unbind(program, tempArray.items);
			currentMesh = null;
		}
		program.end();
	}

	@Override
	public void dispose () {
		program = null;
		uniforms.clear();
		validators.clear();
		setters.clear();
		localUniforms.clear();
		globalUniforms.clear();
		locations = null;
	}

	/** Whether this Shader instance implements the specified uniform, only valid after a call to init(). */
	public final boolean has (final int inputID) {
		return inputID >= 0 && inputID < locations.length && locations[inputID] >= 0;
	}

	public final int loc (final int inputID) {
		return (inputID >= 0 && inputID < locations.length) ? locations[inputID] : -1;
	}

	public final boolean set (final int uniform, final Matrix4 value) {
		if (locations[uniform] < 0) return false;
		program.setUniformMatrix(locations[uniform], value);
		return true;
	}

	public final boolean set (final int uniform, final Matrix3 value) {
		if (locations[uniform] < 0) return false;
		program.setUniformMatrix(locations[uniform], value);
		return true;
	}

	public final boolean set (final int uniform, final Vector3 value) {
		if (locations[uniform] < 0) return false;
		program.setUniformf(locations[uniform], value);
		return true;
	}

	public final boolean set (final int uniform, final Vector2 value) {
		if (locations[uniform] < 0) return false;
		program.setUniformf(locations[uniform], value);
		return true;
	}

	public final boolean set (final int uniform, final Color value) {
		if (locations[uniform] < 0) return false;
		program.setUniformf(locations[uniform], value);
		return true;
	}

	public final boolean set (final int uniform, final float value) {
		if (locations[uniform] < 0) return false;
		program.setUniformf(locations[uniform], value);
		return true;
	}

	public final boolean set (final int uniform, final float v1, final float v2) {
		if (locations[uniform] < 0) return false;
		program.setUniformf(locations[uniform], v1, v2);
		return true;
	}

	public final boolean set (final int uniform, final float v1, final float v2, final float v3) {
		if (locations[uniform] < 0) return false;
		program.setUniformf(locations[uniform], v1, v2, v3);
		return true;
	}

	public final boolean set (final int uniform, final float v1, final float v2, final float v3, final float v4) {
		if (locations[uniform] < 0) return false;
		program.setUniformf(locations[uniform], v1, v2, v3, v4);
		return true;
	}

	public final boolean set (final int uniform, final int value) {
		if (locations[uniform] < 0) return false;
		program.setUniformi(locations[uniform], value);
		return true;
	}

	public final boolean set (final int uniform, final int v1, final int v2) {
		if (locations[uniform] < 0) return false;
		program.setUniformi(locations[uniform], v1, v2);
		return true;
	}

	public final boolean set (final int uniform, final int v1, final int v2, final int v3) {
		if (locations[uniform] < 0) return false;
		program.setUniformi(locations[uniform], v1, v2, v3);
		return true;
	}

	public final boolean set (final int uniform, final int v1, final int v2, final int v3, final int v4) {
		if (locations[uniform] < 0) return false;
		program.setUniformi(locations[uniform], v1, v2, v3, v4);
		return true;
	}

	public final boolean set (final int uniform, final TextureDescriptor textureDesc) {
		if (locations[uniform] < 0) return false;
		program.setUniformi(locations[uniform], context.textureBinder.bind(textureDesc));
		return true;
	}

	public final boolean set (final int uniform, final GLTexture texture) {
		if (locations[uniform] < 0) return false;
		program.setUniformi(locations[uniform], context.textureBinder.bind(texture));
		return true;
	}
}
