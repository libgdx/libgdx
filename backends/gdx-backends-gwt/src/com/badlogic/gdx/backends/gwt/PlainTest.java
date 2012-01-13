package com.badlogic.gdx.backends.gwt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import gwt.g2d.client.util.FpsTimer;
import gwt.g3d.client.Surface3D;
import gwt.g3d.client.gl2.GL2;
import gwt.g3d.client.gl2.WebGLContextAttributes;
import gwt.g3d.client.gl2.WebGLProgram;
import gwt.g3d.client.gl2.WebGLShader;
import gwt.g3d.client.gl2.enums.ClearBufferMask;
import gwt.g3d.client.gl2.enums.ErrorCode;
import gwt.g3d.client.gl2.enums.ProgramParameter;
import gwt.g3d.client.gl2.enums.ShaderParameter;
import gwt.g3d.client.gl2.enums.ShaderType;

import com.badlogic.gdx.graphics.GL20;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class PlainTest implements EntryPoint {
	private Surface3D surface;

	@Override
	public void onModuleLoad() {
		// create surface per configuration
		WebGLContextAttributes contextAttribs = new WebGLContextAttributes();
		surface = new Surface3D(500, 500, contextAttribs);
		RootPanel.get().add(surface);

		// check whether WebGL is supported
		final GL2 gl = surface.getGL();
		if (gl == null) {
			throw new RuntimeException("WebGL not supported");
		}

		// set initial viewport to cover entire surface and
		gl.viewport(0, 0, surface.getWidth(), surface.getHeight());
		
		String vertexShader = "attribute vec4 a_position;\n" +
				"attribute vec4 a_normal;\n" + 
				"uniform mat4 u_projView;\n" +
				"varying vec4 v_color;\n" +
				"void main() {\n" +
				"v_color = vec4(1, 0, 0, 1);\n"+
				"gl_Position = u_projView * a_position;\n" +
				"}\n";
		String fragmentShader = "#ifdef GL_ES\n" +
									 	"precision mediump float;\n" +
									 	"#endif\n" +
									 	"varying vec4 v_color;\n" +
									 	"void main() {\n" +
									 	"gl_FragColor = v_color;\n" +
									 	"}\n";
		setupShader(vertexShader, fragmentShader);
		setupLoop();
	}
	
	private void setupShader(String vertexShader, String fragmentShader) {
		GL2 gl = surface.getGL();
		WebGLShader vs = compileShader(vertexShader, true);
		WebGLShader fs = compileShader(fragmentShader, false);

		WebGLProgram program = gl.createProgram();

		gl.attachShader(program, vs);
		gl.attachShader(program, fs);
		gl.linkProgram(program);

		boolean result = gl.getProgramParameterb(program, ProgramParameter.LINK_STATUS);
		ErrorCode error = gl.getError();
	}
	
	private WebGLShader compileShader(String source, boolean isVS) {
		GL2 gl = surface.getGL();
		WebGLShader shader = gl.createShader(isVS?ShaderType.VERTEX_SHADER:ShaderType.FRAGMENT_SHADER);
		gl.shaderSource(shader, source);
		gl.compileShader(shader);
		boolean result = gl.getShaderParameterb(shader, ShaderParameter.COMPILE_STATUS);
		return shader;
	}

	private void setupLoop() {
		// setup rendering timer
		FpsTimer timer = new FpsTimer(60) {
			@Override
			public void update() {
				GL2 gl = surface.getGL();
				gl.clearColor((float)Math.random(), 0, 0, 1);
				gl.clear(ClearBufferMask.COLOR_BUFFER_BIT);
			}
		};
		timer.start();
	}
}