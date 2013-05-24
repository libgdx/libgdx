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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

public abstract class BaseShader implements Shader {
	private static class UniformEntry {
		public UniformEntry () {}
		public String name;
		public long material;
		public long attribute;
		public long userFlag;
		public final static Pool<UniformEntry> pool = new Pool<UniformEntry>() {
			@Override
			protected UniformEntry newObject () {
				return new UniformEntry();
			}
		};
	}
	private final Array<UniformEntry> uniformEntries = new Array<UniformEntry>();
	private int uniformLocations[];
	private ShaderProgram program;

	/** Register a uniform which might be used by this shader. Only possible before the call to init().
	 * @return The ID of the uniform to use in this shader. */
	protected int registerUniform(final UniformEntry entry) {
		if (program != null)
			throw new GdxRuntimeException("Cannot register uniforms after initialization");
		uniformEntries.add(entry);
		return uniformEntries.size - 1;
	}
	
	protected int registerUniform(final String name, final long material, final long attribute, final long userFlag) {
		UniformEntry entry = UniformEntry.pool.obtain();
		entry.name = name;
		entry.material = material;
		entry.attribute = attribute;
		entry.userFlag = userFlag;
		return registerUniform(entry);
	}
	
	protected int registerUniform(final String name, final long material, final long attribute) {
		return registerUniform(name, material, attribute, 0);
	}
	
	protected int registerUniform(final String name, final long material) {
		return registerUniform(name, material, 0, 0);
	}
	
	protected int registerUniform(final String name) {
		return registerUniform(name, 0, 0, 0);
	}
	
	/** Initialize this shader, causing all registered uniforms/attributes to be fetched. */
	protected void init(final ShaderProgram program, final long material, final long attributes, final long userMask) {
		if (this.program != null)
			throw new GdxRuntimeException("Already initialized");
		if (!program.isCompiled())
			throw new GdxRuntimeException(program.getLog());
		this.program = program;
		uniformLocations = new int[uniformEntries.size];
		for (int i = 0; i < uniformLocations.length; i++) {
			UniformEntry entry = uniformEntries.get(i);
			if (((material & entry.material) == entry.material) && 
				((attributes & entry.attribute) == entry.attribute) && 
				((userMask & entry.userFlag) == entry.userFlag))  {
				uniformLocations[i] = program.fetchUniformLocation(entry.name, false);
			} else
				uniformLocations[i] = -1;
		}
		UniformEntry.pool.freeAll(uniformEntries);
		uniformEntries.clear();
	}
	
	@Override
	public void dispose () {
		program = null;
		uniformLocations = null;
	}
	
	/** Whether this Shader instance contains the specified uniform. */
	protected boolean hasUniform(int what) {
		return uniformLocations[what] >= 0;
	}
	
	/** The location of the specified attribute or uniform, or negative if not available. */
	protected int loc(int what) {
		return uniformLocations[what];
	}
	
	protected boolean set(int what, final Matrix4 value) {
		if (uniformLocations[what] < 0)
			return false;
		program.setUniformMatrix(uniformLocations[what], value);
		return true;
	}
	
	protected boolean set(int what, final Matrix3 value) {
		if (uniformLocations[what] < 0)
			return false;
		program.setUniformMatrix(uniformLocations[what], value);
		return true;
	}
	
	protected boolean set(int what, final Vector3 value) {
		if (uniformLocations[what] < 0)
			return false;
		program.setUniformf(uniformLocations[what], value);
		return true;
	}
	
	protected boolean set(int what, final Color value) {
		if (uniformLocations[what] < 0)
			return false;
		program.setUniformf(uniformLocations[what], value);
		return true;
	}
	
	protected boolean set(int what, final float value) {
		if (uniformLocations[what] < 0)
			return false;
		program.setUniformf(uniformLocations[what], value);
		return true;
	}
}
