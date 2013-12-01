package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader.Uniform;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class LightsTest extends GdxTest {
	PerspectiveCamera cam;
	CameraInputController inputController;
	ModelBatch modelBatch;
	Model model;
	Renderable renderable;
	Environment environment;
	PointLight pointLight;
	Vector3 tmpV = new Vector3();
	Shader shader;
	
	@Override
	public void create () {
		modelBatch = new ModelBatch();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1.0f));
		environment.add(pointLight = new PointLight().set(0.2f, 0.8f, 0.2f, 0f, 0f, 0f, 100f));
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 0f, 5f);
		cam.lookAt(0,0,0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();		
		
		ModelBuilder modelBuilder = new ModelBuilder();
		model = modelBuilder.createCone(2f, 3f, 2f, 10, GL10.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)), Usage.Position | Usage.Normal);
		renderable = model.nodes.get(0).parts.get(0).setRenderable(new Renderable());
		renderable.environment = environment;
		renderable.shader = shader = new DefaultShader(renderable, new DefaultShader.Config(Gdx.files.internal("data/g3d/shaders/lighttest.vertex.glsl").readString(), Gdx.files.internal("data/g3d/shaders/lighttest.fragment.glsl").readString())) {
			protected final int u_pointLightPosition	= register(new Uniform("u_pointLightPosition"), new Setter() {
				@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
				@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
					shader.set(inputID, pointLight.position);
					//Gdx.app.log("Test", "Position = "+pointLight.position.toString());
				}
			});
			protected final int u_pointLightIntensity	= register(new Uniform("u_pointLightIntensity"), new Setter() {
				@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
				@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
					shader.set(inputID, pointLight.intensity);
					//Gdx.app.log("Test", "Intensity = "+pointLight.intensity);
				}
			});
			protected final int u_pointLightColor		= register(new Uniform("u_pointLightColor"), new Setter() {
				@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
				@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
					shader.set(inputID, pointLight.color.r, pointLight.color.g, pointLight.color.b);
					//Gdx.app.log("Test", "Color = "+pointLight.color.toString());
				}
			});
			
			@Override
			public void init () {
				super.init();
				if (!has(u_pointLightPosition))
					Gdx.app.error("LightsTest", "No uniform called: u_pointLightPosition");
				if (!has(u_pointLightColor))
					Gdx.app.error("LightsTest", "No uniform called: u_pointLightColor");
				if (!has(u_pointLightIntensity))
					Gdx.app.error("LightsTest", "No uniform called: u_pointLightIntensity");
				Gdx.app.log("LightsTest", "Shader log: "+program.getLog());
			}
		};
		shader.init();
      pointLight.position.set(0,3f,0);
      pointLight.intensity = 9f;
		
		Gdx.input.setInputProcessor(inputController = new CameraInputController(cam));
	}

	@Override
	public void render () {
      final float delta = Gdx.graphics.getDeltaTime();
      
      pointLight.position.rotate(Vector3.X, delta * 50f);
      pointLight.position.rotate(Vector3.Y, delta * 13f);
      pointLight.position.rotate(Vector3.Z, delta * 3f);
		
		inputController.update();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(renderable);
		modelBatch.end();
	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
		shader.dispose();
	}
	
	public boolean needsGL20 () {
		return true;
	}
}
