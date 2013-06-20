package com.badlogic.gdx.graphics.g3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.subshaders.DiffuseColorTextureShader;
import com.badlogic.gdx.graphics.g3d.shaders.subshaders.SubShader;
import com.badlogic.gdx.graphics.g3d.shaders.subshaders.TransformShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * {@link Shader} that consists of one or more SubShader instances. Each SubShader provides a GLSL snippet
 * for the full shader sources. A SubShader may implement functionality like lighting, transformation & skinning
 * and so on.
 * @author badlogic
 *
 */
public class CompositeShader implements Shader {
	private Camera camera;
	private RenderContext context;
	private ShaderProgram program;
	private final Array<SubShader> subShaders = new Array<SubShader>();
	private long attributesMask;
	private long materialMask;
	private boolean lightingEnabled;
	private final Matrix3 normalMatrix = new Matrix3();
	
	public CompositeShader(Renderable renderable) {
		subShaders.add(new TransformShader());
		subShaders.add(new DiffuseColorTextureShader());
		init(renderable);
	}
	
	public CompositeShader(Renderable renderable, Array<SubShader> shaders) {
		subShaders.addAll(shaders);
		init(renderable);
	}
	
	private void init(Renderable renderable) {
		// get masks
		materialMask = renderable.material.getMask();
		attributesMask = renderable.mesh.getVertexAttributes().getMask();
		lightingEnabled = renderable.lights != null;
		
		// initialize subshaders
		for(SubShader subShader: subShaders) {
			subShader.init(renderable);
		}
		
		// compose shader source code
		StringBuffer vertexShader = new StringBuffer();
		StringBuffer fragmentShader = new StringBuffer();
		
		// precision modifiers
		fragmentShader.append("#ifdef GL_ES\n" +  
									 "  #define LOWP lowp\n" +
									 "  #define MED mediump\n" +
									 "  #define HIGH highp\n" +
									 "  precision mediump float;\n" +
									 "#else\n" +
									 "  #define MED\n" +
									 "  #define LOWP\n" +
									 "  #define HIGH\n" +
									 "#endif\n\n");
		
		// global uniforms in vertex shader
		vertexShader.append("uniform mat4 u_projTrans;\n");
		vertexShader.append("uniform vec3 u_cameraPosition;\n");
		vertexShader.append("uniform vec3 u_cameraDirection;\n");
		vertexShader.append("uniform vec3 u_cameraUp;\n");
		vertexShader.append("uniform mat3 u_normalMatrix;\n");
		
		// generate attributes, uniforms, varyings etc.
		for(SubShader subShader: subShaders) {
			for(String line: subShader.getVertexShaderVars()) {
				vertexShader.append(line);
				vertexShader.append("\n");
			}
			vertexShader.append("\n");
			
			for(String line: subShader.getFragmentShaderVars()) {
				fragmentShader.append(line);
				fragmentShader.append("\n");
			}
			fragmentShader.append("\n");
		}
		
		// generate main methods and code
		vertexShader.append("void main() {\n");
		for(SubShader subShader: subShaders) {
			for(String line: subShader.getVertexShaderCode()) {
				vertexShader.append("  ");
				vertexShader.append(line);
				vertexShader.append("\n");
			}
		}
		vertexShader.append("  gl_Position = position;\n"); // we asume at least one sub shader outputs a position
		vertexShader.append("}");
		
		fragmentShader.append("void main() {\n");
		for(SubShader subShader: subShaders) {
			for(String line: subShader.getFragmentShaderCode()) {
				fragmentShader.append("  ");
				fragmentShader.append(line);
				fragmentShader.append("\n");
			}
		}
		fragmentShader.append("  gl_FragColor = color;\n"); // we assume at least one sub shader outputs a color
		fragmentShader.append("}");
		
		program = new ShaderProgram(vertexShader.toString(), fragmentShader.toString());
		if(!program.isCompiled()) {
			throw new GdxRuntimeException("Couldn't compile composite shader\n" +
												   "------ vertex shader ------\n"+
												   vertexShader + "\n" +
												   "------ fragment shader ------\n"+
												   fragmentShader + "\n" +
												   "------ error log ------\n" +
												   program.getLog());
		} else {
			Gdx.app.log("CompositeShader", "\n" +
			   "------ vertex shader ------\n"+
			   vertexShader + "\n" +
			   "------ fragment shader ------\n"+
			   fragmentShader + "\n" +
			   "------ error log ------\n" +
			   program.getLog());
		}
	}
	
	@Override
	public void init () {
	}

	@Override
	public int compareTo (Shader other) {
		return 0;
	}

	@Override
	public boolean canRender (Renderable renderable) {
		return materialMask == renderable.material.getMask() && 
				 attributesMask == renderable.mesh.getVertexAttributes().getMask() && 
			    (renderable.lights != null) == lightingEnabled;
	}

	@Override
	public void begin (Camera camera, RenderContext context) {
		program.begin();
		this.camera = camera;
		this.context = context;
		context.setDepthTest(true, GL20.GL_LEQUAL);
	}

	@Override
	public void render (Renderable renderable) {
		ShaderProgram.pedantic = false;
		// set global uniforms
		program.setUniformMatrix("u_projTrans", camera.combined);
		program.setUniformf("u_cameraPosition", camera.position);
		program.setUniformf("u_cameraDirection", camera.direction);
		program.setUniformf("u_cameraUp", camera.up);
		program.setUniformMatrix("u_normalMatrix", normalMatrix.set(camera.combined));
		
		// apply sub shaders
		for(SubShader shader: subShaders) {
			shader.apply(program, context, camera, renderable);
		}
		
		// render the mesh
		renderable.mesh.render(program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);
	}

	@Override
	public void end () {
		program.end();
	}

	@Override
	public void dispose () {
		program.dispose();
	}
}
