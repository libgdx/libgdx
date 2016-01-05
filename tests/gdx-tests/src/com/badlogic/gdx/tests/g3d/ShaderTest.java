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

package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ShaderTest extends GdxTest {
	// Create a custom attribute, see https://github.com/libgdx/libgdx/wiki/Material-and-environment
	// See also: http://blog.xoppa.com/using-materials-with-libgdx/
	public static class TestAttribute extends Attribute {
		public final static String Alias = "Test";
		public final static long ID = register(Alias);

		public float value;

		protected TestAttribute (final float value) {
			super(ID);
			this.value = value;
		}

		@Override
		public Attribute copy () {
			return new TestAttribute(value);
		}

		@Override
		protected boolean equals (Attribute other) {
			return ((TestAttribute)other).value == value;
		}
		
		@Override
		public int compareTo (Attribute o) {
			if (type != o.type) return type < o.type ? -1 : 1;
			float otherValue = ((TestAttribute)o).value;
			return MathUtils.isEqual(value, otherValue) ? 0 : (value < otherValue ? -1 : 1);
		}
	}

	// Create a custom shader, see also http://blog.xoppa.com/creating-a-shader-with-libgdx
	// BaseShader adds some basic functionality used to manage uniforms etc.
	public static class TestShader extends BaseShader {
		// @off
		public final static String vertexShader =
			  "attribute vec3 a_position;\n"
			+ "uniform mat4 u_projTrans;\n"
			+ "uniform mat4 u_worldTrans;\n"
			+ "void main() {\n"
			+ "	gl_Position = u_projTrans * u_worldTrans * vec4(a_position, 1.0);\n"
			+ "}\n";

		public final static String fragmentShader =
			  "#ifdef GL_ES\n"
			+ "#define LOWP lowp\n"
			+ "precision mediump float;\n"
			+ "#else\n"
			+ "#define LOWP\n"
			+ "#endif\n"

			+ "uniform float u_test;\n"
			+ "#ifdef HasDiffuseColor\n"
			+ "uniform vec4 u_color;\n"
			+ "#endif //HasDiffuseColor\n"

			+ "void main() {\n"
			+ "#ifdef HasDiffuseColor\n"
			+ "	gl_FragColor.rgb = u_color.rgb * vec3(u_test);\n"
			+ "#else\n"
			+ "	gl_FragColor.rgb = vec3(u_test);\n"
			+ "#endif //HasDiffuseColor\n"
			+ "}\n";
		// @on

		protected final int u_projTrans = register(new Uniform("u_projTrans"));
		protected final int u_worldTrans = register(new Uniform("u_worldTrans"));
		protected final int u_test = register(new Uniform("u_test"));
		protected final int u_color = register(new Uniform("u_color"));

		protected final ShaderProgram program;
		private boolean withColor;

		public TestShader (Renderable renderable) {
			super();
			withColor = renderable.material.has(ColorAttribute.Diffuse);
			if (withColor)
				Gdx.app.log("ShaderTest", "Compiling test shader with u_color uniform");
			else
				Gdx.app.log("ShaderTest", "Compiling test shader without u_color uniform");

			String prefix = withColor ? "#define HasDiffuseColor\n" : "";
			program = new ShaderProgram(vertexShader, prefix + fragmentShader);

			if (!program.isCompiled()) throw new GdxRuntimeException("Couldn't compile shader " + program.getLog());
			String log = program.getLog();
			if (log.length() > 0) Gdx.app.error("ShaderTest", "Shader compilation log: " + log);
		}

		@Override
		public void init () {
			super.init(program, null);
		}

		@Override
		public int compareTo (Shader other) {
			return 0;
		}

		@Override
		public boolean canRender (Renderable instance) {
			return instance.material.has(TestAttribute.ID) && (instance.material.has(ColorAttribute.Diffuse) == withColor);
		}

		@Override
		public void begin (Camera camera, RenderContext context) {
			program.begin();
			context.setDepthTest(GL20.GL_LEQUAL, 0f, 1f);
			context.setDepthMask(true);
			set(u_projTrans, camera.combined);
		}

		@Override
		public void render (Renderable renderable) {
			set(u_worldTrans, renderable.worldTransform);

			TestAttribute testAttr = (TestAttribute)renderable.material.get(TestAttribute.ID);
			set(u_test, testAttr.value);

			if (withColor) {
				ColorAttribute colorAttr = (ColorAttribute)renderable.material.get(ColorAttribute.Diffuse);
				set(u_color, colorAttr.color);
			}

			renderable.meshPart.render(program);
		}

		@Override
		public void end () {
			program.end();
		}

		@Override
		public void dispose () {
			super.dispose();
			program.dispose();
		}
	}

	public PerspectiveCamera cam;
	public CameraInputController camController;
	public ModelBatch modelBatch;
	public Model model;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public TestAttribute testAttribute1, testAttribute2;

	@Override
	public void create () {
		modelBatch = new ModelBatch(new DefaultShaderProvider() {
			@Override
			protected Shader createShader (Renderable renderable) {
				if (renderable.material.has(TestAttribute.ID)) return new TestShader(renderable);
				return super.createShader(renderable);
			}
		});

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 0f, 20f);
		cam.lookAt(0, 0, 0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		Material testMaterial1 = new Material("TestMaterial1", new TestAttribute(1f));
		Material redMaterial = new Material("RedMaterial", ColorAttribute.createDiffuse(Color.RED));
		Material testMaterial2 = new Material("TestMaterial2", new TestAttribute(1f), ColorAttribute.createDiffuse(Color.BLUE));

		ModelBuilder builder = new ModelBuilder();
		Node node;

		builder.begin();
		node = builder.node();
		node.id = "testCone1";
		node.translation.set(-10, 0f, 0f);
		builder.part("testCone", GL20.GL_TRIANGLES, Usage.Position, testMaterial1).cone(5, 5, 5, 20);

		node = builder.node();
		node.id = "redSphere";
		builder.part("redSphere", GL20.GL_TRIANGLES, Usage.Position, redMaterial).sphere(5, 5, 5, 20, 20);

		node = builder.node();
		node.id = "testCone1";
		node.translation.set(10, 0f, 0f);
		builder.part("testCone", GL20.GL_TRIANGLES, Usage.Position, testMaterial2).cone(5, 5, 5, 20);

		model = builder.end();

		ModelInstance modelInstance;
		modelInstance = new ModelInstance(model);
		testAttribute1 = (TestAttribute)modelInstance.getMaterial("TestMaterial1").get(TestAttribute.ID);
		testAttribute2 = (TestAttribute)modelInstance.getMaterial("TestMaterial2").get(TestAttribute.ID);
		instances.add(modelInstance);
	}

	private float counter;

	@Override
	public void render () {
		counter = (counter + Gdx.graphics.getDeltaTime()) % 2.f;
		testAttribute1.value = Math.abs(1f - counter);
		testAttribute2.value = 1f - testAttribute1.value;

		camController.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(instances);
		modelBatch.end();
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
	}
}
