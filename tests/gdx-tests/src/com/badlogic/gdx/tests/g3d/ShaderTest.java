package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.Material.Attribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ShaderTest extends GdxTest {
	public static class TestAttribute extends Material.Attribute {
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
	}
	
	public static class TestShader extends BaseShader {
		public final static String vertexShader = 
			"attribute vec3 a_position;\n"+
			"uniform mat4 u_projTrans;\n"+
			"uniform mat4 u_worldTrans;\n"+
			"uniform float u_test;\n"+
			"varying float v_test;\n"+
			"void main() {\n"+
			"	v_test = u_test;\n"+
			"	gl_Position = u_projTrans * u_worldTrans * vec4(a_position, 1.0);\n"+
			"}\n";
		public final static String fragmentShader = 
			"varying float v_test;\n" +
			"void main() {\n" +
			"	gl_FragColor.rgb = vec3(v_test);\n" +
			"}\n";
		
		protected final int u_projTrans	= register(new Uniform("u_projTrans"));
		protected final int u_worldTrans	= register(new Uniform("u_worldTrans"));
		protected final int u_test			= register(new Uniform("u_test"));
		
		protected final ShaderProgram program;
		
		public TestShader () {
			super();
			program = new ShaderProgram(vertexShader, fragmentShader);
			if (!program.isCompiled())
				throw new GdxRuntimeException("Couldn't compile shader " + program.getLog());
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
			return true;
		}

		@Override
		public void begin (Camera camera, RenderContext context) {
			program.begin();
			set(u_projTrans, camera.combined);
		}

		@Override
		public void render (Renderable renderable) {
			set(u_worldTrans, renderable.worldTransform);
			TestAttribute attr = (TestAttribute)renderable.material.get(TestAttribute.ID);
			set(u_test, attr == null ? 1f : attr.value);
			renderable.mesh.render(program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);
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
	public ModelInstance instance;
	public TestAttribute testAttribute;
	
	@Override
	public void create () {
		modelBatch = new ModelBatch(new BaseShaderProvider() {
			@Override
			protected Shader createShader (Renderable renderable) {
				return new TestShader();
			}
		});

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0,0,0);
		cam.near = 0.1f;
		cam.far = 300f;
		cam.update();

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);
		
		Material material = new Material(new TestAttribute(1f));
		ModelBuilder builder = new ModelBuilder();
		model = builder.createCone(5, 5, 5, 20, material, Usage.Position);
		instance = new ModelInstance(model);
		testAttribute = (TestAttribute)instance.materials.get(0).get(TestAttribute.ID);
	}

	private float counter;
	@Override
	public void render () {
		counter = (counter + Gdx.graphics.getDeltaTime()) % 2.f;
		testAttribute.value = Math.abs(1f - counter); 
			
		camController.update();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(instance);
		modelBatch.end();
	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
	}
	
	public boolean needsGL20 () {
		return true;
	}
}
