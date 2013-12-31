
package com.badlogic.gdx.tests.gles3;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.BufferUtils;

/** This test demonstrates rendering to multiple draw buffers (render targets) through the use of a frame buffer object (FBO). This
 * functionality is the key component of most deferred shading approaches.
 * <p>
 * The test first draws blue and red to 2 textures using a shader with 2 fragment outputs. This is done once, after which the
 * textures are used to fill in a quad and triangle shape, to show that the operation was successful.
 * @author mattijs driel */
public class MultipleDrawBuffersTest extends GdxTest {
	ShaderProgramES3 shader;
	GenericTexture tex0;
	GenericTexture tex1;
	VBOGeometry quad;
	VBOGeometry tri;

	@Override
	public boolean needsGL20 () {
		return true;
	}

	@Override
	public void create () {
		if (Gdx.graphics.getGL30() == null) {
			System.out.println("This test requires OpenGL ES 3.0.");
			System.out.println("Make sure needsGL20() is returning true. (ES 2.0 is a subset of ES 3.0.)");
			System.out
				.println("Otherwise, your system does not support it, or it might not be available yet for the current backend.");
			return;
		}

		// create empty draw target textures
		tex0 = new GenericTexture(128, 128, GL30.GL_RGB8, GL30.GL_RGB, GL30.GL_NEAREST);
		tex1 = new GenericTexture(128, 128, GL30.GL_RGB8, GL30.GL_RGB, GL30.GL_NEAREST);

		writeColorToTextures();

		String vertexShader = "#version 300 es                                                  \n"
			+ "layout(location = 0)in vec4 vPos;                                                 \n"
			+ "void main()                                                                       \n"
			+ "{                                                                                 \n"
			+ "   gl_Position = vPos;                                                            \n"
			+ "}                                                                                 \n";

		String fragmentShader = "#version 300 es                                                \n"
			+ "precision highp float;                                                            \n"
			+ "uniform sampler2D texture0;                                                       \n"
			+ "out vec4 fragColor;                                                               \n"
			+ "void main()                                                                       \n"
			+ "{                                                                                 \n"
			+ "   fragColor = texture2D(texture0, gl_FragCoord.xy);                              \n"
			+ "}                                                                                 \n";

		// load the shader
		shader = new ShaderProgramES3(vertexShader, fragmentShader);
		if (!shader.isCompiled()) {
			System.out.println(shader.getErrorLog());
			return;
		}
		shader.registerTextureSampler("texture0").setBinding(0);

		tri = VBOGeometry.triangleV();
		quad = VBOGeometry.quadV();
	}

	void writeColorToTextures () {

		// generate and bind FBO
		IntBuffer ib = BufferUtils.newIntBuffer(1);
		Gdx.gl30.glGenFramebuffers(1, ib);
		int fboName = ib.get(0);
		Gdx.gl30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboName);

		// bind textures to current FBO
		tex0.bindToFBO(GL30.GL_COLOR_ATTACHMENT0);
		tex1.bindToFBO(GL30.GL_COLOR_ATTACHMENT1);

		// set FBO to draw to textures
		IntBuffer cDrawBuffers = BufferUtils.newIntBuffer(2);
		cDrawBuffers.put(GL30.GL_COLOR_ATTACHMENT0);
		cDrawBuffers.put(GL30.GL_COLOR_ATTACHMENT1);
		cDrawBuffers.position(0);
		Gdx.gl30.glDrawBuffers(2, cDrawBuffers);

		String vertexShader = "#version 300 es                                                  \n"
			+ "layout(location = 0)in vec4 vPos;                                                 \n"
			+ "void main()                                                                       \n"
			+ "{                                                                                 \n"
			+ "   gl_Position = vPos;                                                            \n"
			+ "}                                                                                 \n";

		String fragmentShader = "#version 300 es                                                \n"
			+ "precision highp float;                                                            \n"
			+ "layout(location = 0)out vec4 redOutput;                                           \n"
			+ "layout(location = 1)out vec4 blueOutput;                                          \n"
			+ "void main()                                                                       \n"
			+ "{                                                                                 \n"
			+ "   redOutput = vec4(1,0,0,1);                                                     \n"
			+ "   blueOutput = vec4(0,0,1,1);                                                    \n"
			+ "}                                                                                 \n";

		// load the shader
		shader = new ShaderProgramES3(vertexShader, fragmentShader);
		if (!shader.isCompiled()) {
			System.out.println(shader.getErrorLog());
			return;
		}

		VBOGeometry geom = VBOGeometry.fsQuadV();

		// render to texture
		shader.use();
		geom.bind();
		geom.draw();

		geom.dispose();
		shader.dispose();

		// bind back to normal buffer
		Gdx.gl30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

		ib.position(0);
		Gdx.gl30.glDeleteFramebuffers(1, ib);
	}

	@Override
	public void render () {
		if (Gdx.graphics.getGL30() == null || !shader.isCompiled()) return;

		Gdx.gl30.glClearColor(0, 0, 0, 0);
		Gdx.gl30.glClear(GL30.GL_COLOR_BUFFER_BIT);

		// render results
		shader.use();

		tex0.bind();
		tri.bind();
		tri.draw();

		tex1.bind();
		quad.bind();
		quad.draw();
	}
}
