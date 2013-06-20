package com.badlogic.gdx.graphics.g3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

/** @author Xoppa
 * A BaseShader is a wrapper around a ShaderProgram that keeps track of the uniform and attribute locations.
 * It does not manage the ShaderPogram, you are still responsible for disposing the ShaderProgram. */
public abstract class BaseShader implements Shader {
	/** the Input is a vertex attribute */ 
	public final static int VERTEX_ATTRIBUTE = 1;
	/** the Input is a global uniform (independent of the renderable) */
	public final static int GLOBAL_UNIFORM = 2; // FIXME rename to something better
	/** the Input is a local uniform (depends on the renderable) */
	public final static int LOCAL_UNIFORM = 3; // FIXME rename to something better
	
	public static class Input {
		public interface Setter {
			void set(BaseShader shader, ShaderProgram program, Input input, Camera camera, RenderContext context, Renderable renderable);
		}
		
		/** The scope of the input: VERTEX_ATTRIBUTE, GLOBAL_UNIFORM or LOCAL_UNIFORM */
		public final int scope;
		/** The name of the input. */
		public final String name;
		/** The flags the materialMask must contain for this Input to be applicable */
		public final long materialFlags;
		/** The flags the vertexMask must contain for this Input to be applicable */
		public final long vertexFlags;
		/** The flags the userMask must contain for this Input to be applicable */
		public final long userFlags;
		/** The setter (if any, null otherwise) is responsible for setting the input with the appropriate value */ 
		public final Setter setter;
		/** The location within the ShaderProgram of this input */ 
		public int location = -1;
		
		public boolean compare(final long materialMask, final long vertexMask, final long userMask) {
			return (((materialMask & this.materialFlags) == this.materialFlags) && 
				((vertexMask & this.vertexFlags) == this.vertexFlags) && 
				((userMask & this.userFlags) == this.userFlags)); 
		}
		
		public Input(final int scope, final String name, final long materialFlags, final long vertexFlags, final long userFlags, final Setter setter) {
			this.scope = scope;
			this.name = name;
			this.materialFlags = materialFlags;
			this.vertexFlags = vertexFlags;
			this.userFlags = userFlags;
			this.setter = setter;
		}
		
		public Input(final int scope, final String name, final long materialFlags, final long vertexFlags, final long userFlags) {
			this(scope, name, materialFlags, vertexFlags, userFlags, null);
		}

		public Input(final int scope, final String name, final long materialFlags, final long vertexFlags, final Setter setter) {
			this(scope, name, materialFlags, vertexFlags, 0, setter);
		}
		
		public Input(final int scope, final String name, final long materialFlags, final long vertexFlags) {
			this(scope, name, materialFlags, vertexFlags, 0);
		}

		public Input(final int scope, final String name, final long materialFlags, final Setter setter) {
			this(scope, name, materialFlags, 0, 0, setter);
		}
		
		public Input(final int scope, final String name, final long materialFlags) {
			this(scope, name, materialFlags, 0, 0);
		}

		public Input(final int scope, final String name, final Setter setter) {
			this(scope, name, 0, 0, 0, setter);
		}
		
		public Input(final int scope, final String name) {
			this(scope, name, 0, 0, 0);
		}
	}
	
	private final Array<Input> inputs = new Array<Input>();
	public final Array<Input> vertexAttributes = new Array<Input>();
	public final Array<Input> globalUniforms = new Array<Input>();
	public final Array<Input> localUniforms = new Array<Input>();
	
	public ShaderProgram program;
	public RenderContext context;
	public Camera camera;
	
	/** Register an input which might be used by this shader. Only possible before the call to init().
	 * @return The ID of the input to use in this shader. */
	public Input register(final Input input) {
		if (program != null)
			throw new GdxRuntimeException("Cannot register input after initialization");
		final Input existing = getInput(input.name);
		if (existing != null) {
			if (existing.scope != input.scope)
				throw new GdxRuntimeException(input.name+": An input with the same name but different scope is already registered.");
			return existing;
		}
		inputs.add(input);
		return input;
	}
	
	public Iterable<Input> getInputs() {
		return inputs;
	}
	
	/** @return The input or null if not found. */
	public Input getInput(final String alias) {
		for (final Input input : inputs)
			if (alias.equals(input.name))
				return input;
		return null;
	}

	/** Initialize this shader, causing all registered uniforms/attributes to be fetched. */
	public void init(final ShaderProgram program, final long materialMask, final long vertexMask, final long userMask) {
		if (this.program != null)
			throw new GdxRuntimeException("Already initialized");
		if (!program.isCompiled())
			throw new GdxRuntimeException(program.getLog());
		this.program = program;
		for (Input input : inputs) {
			if (input.compare(materialMask, vertexMask, userMask))  {
				if (input.scope == GLOBAL_UNIFORM) {
					input.location = program.fetchUniformLocation(input.name, false);
					if (input.location >= 0 && input.setter != null)
						globalUniforms.add(input);
				} else if (input.scope == LOCAL_UNIFORM) {
					input.location = program.fetchUniformLocation(input.name, false);
					if (input.location >= 0 && input.setter != null)
						localUniforms.add(input);
				} else if (input.scope == VERTEX_ATTRIBUTE) {
					input.location = program.getAttributeLocation(input.name);
					if (input.location >= 0)
						vertexAttributes.add(input);
				} else
					input.location = -1;
			} else
				input.location = -1;
		}
	}
	
	@Override
	public void begin (Camera camera, RenderContext context) {
		this.camera = camera;
		this.context = context;
		program.begin();
		for (final Input input : globalUniforms)
			input.setter.set(this, program, input, camera, context, null);
	}

	@Override
	public void render (Renderable renderable) {
		for (final Input input : localUniforms)
			input.setter.set(this, program, input, camera, context, renderable);
		renderable.mesh.render(program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);
		// FIXME Use vertexAttributes to bind and render the mesh
	}

	@Override
	public void end () {
		program.end();
	}
	
	@Override
	public void dispose () {
		program = null;
		inputs.clear();
		vertexAttributes.clear();
		localUniforms.clear();
		globalUniforms.clear();
	}
	
	/** Whether this Shader instance implements the specified attribute or uniform, only valid after a call to init(). */
	public final boolean has(final Input input) {
		return input.location >= 0;
	}
	
	public final boolean set(final Input uniform, final Matrix4 value) {
		if (uniform.location < 0)
			return false;
		program.setUniformMatrix(uniform.location, value);
		return true;
	}
	
	public final boolean set(final Input uniform, final Matrix3 value) {
		if (uniform.location < 0)
			return false;
		program.setUniformMatrix(uniform.location, value);
		return true;
	}
	
	public final boolean set(final Input uniform, final Vector3 value) {
		if (uniform.location < 0)
			return false;
		program.setUniformf(uniform.location, value);
		return true;
	}
	
	public final boolean set(final Input uniform, final Vector2 value) {
		if (uniform.location < 0)
			return false;
		program.setUniformf(uniform.location, value);
		return true;
	}
	
	public final boolean set(final Input uniform, final Color value) {
		if (uniform.location < 0)
			return false;
		program.setUniformf(uniform.location, value);
		return true;
	}
	
	public final boolean set(final Input uniform, final float value) {
		if (uniform.location < 0)
			return false;
		program.setUniformf(uniform.location, value);
		return true;
	}
	
	public final boolean set(final Input uniform, final float v1, final float v2) {
		if (uniform.location < 0)
			return false;
		program.setUniformf(uniform.location, v1, v2);
		return true;
	}
	
	public final boolean set(final Input uniform, final float v1, final float v2, final float v3) {
		if (uniform.location < 0)
			return false;
		program.setUniformf(uniform.location, v1, v2, v3);
		return true;
	}
	
	public final boolean set(final Input uniform, final float v1, final float v2, final float v3, final float v4) {
		if (uniform.location < 0)
			return false;
		program.setUniformf(uniform.location, v1, v2, v3, v4);
		return true;
	}
	
	public final boolean set(final Input uniform, final int value) {
		if (uniform.location < 0)
			return false;
		program.setUniformi(uniform.location, value);
		return true;
	}
	
	public final boolean set(final Input uniform, final int v1, final int v2) {
		if (uniform.location < 0)
			return false;
		program.setUniformi(uniform.location, v1, v2);
		return true;
	}
	
	public final boolean set(final Input uniform, final int v1, final int v2, final int v3) {
		if (uniform.location < 0)
			return false;
		program.setUniformi(uniform.location, v1, v2, v3);
		return true;
	}
	
	public final boolean set(final Input uniform, final int v1, final int v2, final int v3, final int v4) {
		if (uniform.location < 0)
			return false;
		program.setUniformi(uniform.location, v1, v2, v3, v4);
		return true;
	}
}
