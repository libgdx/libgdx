
package com.badlogic.gdx.tests.gles3;

import java.nio.IntBuffer;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

/** A basic implementation of a shader program with some OpenGL ES 3.0 function support.
 * <p>
 * List of functionality:
 * <li>Arbitrary attributes
 * <li>Uniform blocks (through {@link UniformBufferObject})
 * 
 * @author mattijs driel */
public class ShaderProgramES3 implements Disposable {

	int program;

	public final String vertexShaderSource;
	public final String fragmentShaderSource;

	/** Buffer for a single int */
	private IntBuffer intbuf = null;

	/** Log containing any gl error encountered during construction. */
	private String log = null;

	private HashMap<String, UniformBlockInfo> uniformBlocks = new HashMap<String, UniformBlockInfo>();

	/** Create a shader with a vertex and fragment shader. **/
	public ShaderProgramES3 (String vertexSource, String fragmentSource) {
		GL30 gl = Gdx.graphics.getGL30();
		this.vertexShaderSource = vertexSource;
		this.fragmentShaderSource = fragmentSource;

		if (gl == null)
			log = "This program requires ES 3.0";
		else if (vertexSource == null)
			log = "Vertex shader source is null";
		else if (fragmentSource == null)
			log = "Fragment shader source is null";
		else
			initProgram(gl);

		if (!isCompiled()) dispose();
	}

	private void initProgram (GL30 gl) {

		program = gl.glCreateProgram();
		if (program == -1) {
			log = "Could not create a program name";
			return;
		}

		intbuf = BufferUtils.newIntBuffer(1);

		// compile individual shader sources
		int vertexShader, fragmentShader;
		if ((vertexShader = initShader(gl, vertexShaderSource, GL20.GL_VERTEX_SHADER)) == 0) return;
		if ((fragmentShader = initShader(gl, fragmentShaderSource, GL20.GL_FRAGMENT_SHADER)) == 0) return;

		gl.glLinkProgram(program);
		intbuf.position(0);
		gl.glGetProgramiv(program, GL20.GL_LINK_STATUS, intbuf);
		if (intbuf.get(0) == 0) {
			log = gl.glGetProgramInfoLog(program);
			return;
		}

		// remove shaders, program will remain
		gl.glDetachShader(program, vertexShader);
		gl.glDetachShader(program, fragmentShader);
		gl.glDeleteShader(vertexShader);
		gl.glDeleteShader(fragmentShader);

		// find all named inputs and outputs
		initUniformBlocks(gl);
	}

	private int initShader (GL30 gl, String source, int type) {
		int id = gl.glCreateShader(type);
		gl.glShaderSource(id, source);
		gl.glCompileShader(id);
		intbuf.position(0);
		gl.glGetShaderiv(id, GL20.GL_COMPILE_STATUS, intbuf);
		if (intbuf.get(0) == 0) {
			log = gl.glGetShaderInfoLog(id);
			gl.glDeleteShader(id);
			return 0;
		}
		gl.glAttachShader(program, id);
		return id;
	}

	private void initUniformBlocks (GL30 gl) {
		intbuf.position(0);
		gl.glGetProgramiv(program, GL30.GL_ACTIVE_UNIFORM_BLOCKS, intbuf);
		int numBlocks = intbuf.get(0);
		for (int i = 0; i < numBlocks; ++i) {
			String blockName = gl.glGetActiveUniformBlockName(program, i);
			uniformBlocks.put(blockName, new UniformBlockInfo(this, i));
		}
	}

	public boolean isCompiled () {
		return log == null;
	}

	public String getErrorLog () {
		return log;
	}

	public int getProgramName () {
		return program;
	}

	public void use () {
		GL30 gl = Gdx.graphics.getGL30();
		if (gl == null) return;

		gl.glUseProgram(program);
	}

	public UniformBlockInfo getUniformBlock (String blockName) {
		return uniformBlocks.get(blockName);
	}

	@Override
	public void dispose () {
		GL30 gl = Gdx.graphics.getGL30();
		if (gl == null) return;

		gl.glDeleteProgram(program);
	}

	public static class UniformBlockInfo {
		public final ShaderProgramES3 owner;
		public final int blockIndex;

		private int currentBindingPoint;

		UniformBlockInfo (ShaderProgramES3 owner, int index) {
			this.owner = owner;
			this.blockIndex = index;
		}

		public int getCurrentBindingPoint () {
			return currentBindingPoint;
		}

		public void bindToBindingPoint (int bindingPoint) {
			currentBindingPoint = bindingPoint;
			Gdx.gl30.glUniformBlockBinding(owner.program, blockIndex, currentBindingPoint);
		}

		@Override
		public String toString () {
			IntBuffer ib = BufferUtils.newIntBuffer(16);

			// name
			String name = Gdx.gl30.glGetActiveUniformBlockName(owner.program, blockIndex);

			// size
			Gdx.gl30.glGetActiveUniformBlockiv(owner.program, blockIndex, GL30.GL_UNIFORM_BLOCK_DATA_SIZE, ib);
			int sizeBytes = ib.get(0);

			// num uniforms
			ib.position(0);
			Gdx.gl30.glGetActiveUniformBlockiv(owner.program, blockIndex, GL30.GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, ib);
			int numUniforms = ib.get(0);
			String[] uniformNames = new String[numUniforms];
			int[] uniformSizes = new int[numUniforms];
			int[] uniformOffsets = new int[numUniforms];
			IntBuffer ibuniforms = BufferUtils.newIntBuffer(numUniforms);

			// uniform indices
			ib.position(0);
			Gdx.gl30.glGetActiveUniformBlockiv(owner.program, blockIndex, GL30.GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, ib);
			ib.position(0);
			while (ibuniforms.hasRemaining())
				ibuniforms.put(ib.get());

			// uniform names
			ibuniforms.position(0);
			for (int i = 0; i < numUniforms; ++i) {
				ib.position(0);
				uniformNames[i] = Gdx.gl30.glGetActiveUniform(owner.program, ibuniforms.get(), ib, null);
			}

			// uniform sizes
			ib.position(0);
			ibuniforms.position(0);
			Gdx.gl30.glGetActiveUniformsiv(owner.program, numUniforms, ibuniforms, GL30.GL_UNIFORM_SIZE, ib);
			ib.position(0);
			for (int i = 0; i < numUniforms; ++i)
				uniformSizes[i] = ib.get();

			// uniform offsets
			ib.position(0);
			ibuniforms.position(0);
			Gdx.gl30.glGetActiveUniformsiv(owner.program, numUniforms, ibuniforms, GL30.GL_UNIFORM_OFFSET, ib);
			ib.position(0);
			for (int i = 0; i < numUniforms; ++i)
				uniformOffsets[i] = ib.get();

			String t = "  ";

			String s = "";
			s += name + "\n";
			s += t + "byte size: " + sizeBytes + "\n";
			s += t + "{\n";
			for (int i = 0; i < numUniforms; ++i) {
				s += t + t + uniformNames[i] + "\n";
				s += t + t + t + "chunk count: " + uniformSizes[i] + "\n";
				s += t + t + t + "byte offset: " + uniformOffsets[i] + "\n";
			}
			s += t + "}\n";
			return s;
		}
	}

}
