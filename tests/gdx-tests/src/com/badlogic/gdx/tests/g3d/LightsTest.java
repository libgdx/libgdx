package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
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
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
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
		model = modelBuilder.createSphere(2f, 3f, 2f, 10, 10, GL10.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)), Usage.Position | Usage.Normal);
		renderable = model.nodes.get(0).parts.get(0).setRenderable(new Renderable());
		renderable.environment = environment;
		if (false) {
			DefaultShader.Config config = new DefaultShader.Config();
			config.numPointLights = 2;
			renderable.shader = shader = new DefaultShader(renderable, config);
		} else {
			DefaultShader.Config config = new DefaultShader.Config(Gdx.files.internal("data/g3d/shaders/lighttest.vertex.glsl").readString(), Gdx.files.internal("data/g3d/shaders/lighttest.fragment.glsl").readString());
			config.numPointLights = 2;
			renderable.shader = shader = new DefaultShader(renderable, config) {
				protected final int u_pointLightPosition	= register(new Uniform("u_pointLightPosition"), new Setter() {
					@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
					@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
						shader.set(inputID, pointLight.position);
					}
				});
				protected final int u_pointLightIntensity	= register(new Uniform("u_pointLightIntensity"), new Setter() {
					@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
					@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
						shader.set(inputID, pointLight.intensity);
					}
				});
				protected final int u_pointLightColor		= register(new Uniform("u_pointLightColor"), new Setter() {
					@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
					@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
						shader.set(inputID, pointLight.color.g, pointLight.color.r, pointLight.color.b);
					}
				});
				
				@Override
				public void init () {
					super.init();
	//				if (!has(u_pointLightPosition))
	//					Gdx.app.error("LightsTest", "No uniform called: u_pointLightPosition");
	//				if (!has(u_pointLightColor))
	//					Gdx.app.error("LightsTest", "No uniform called: u_pointLightColor");
					if (!has(u_pointLightIntensity))
						Gdx.app.error("LightsTest", "No uniform called: u_pointLightIntensity");
					Gdx.app.log("LightsTest", "Shader log: "+program.getLog());
					Gdx.app.log("LightsTest", "pointLightsLoc = "+pointLightsLoc);
					Gdx.app.log("LightsTest", "pointLightsColorOffset = "+pointLightsColorOffset);
					Gdx.app.log("LightsTest", "pointLightsPositionOffset = "+pointLightsPositionOffset);
					Gdx.app.log("LightsTest", "pointLightsIntensityOffset = "+pointLightsIntensityOffset);
					Gdx.app.log("LightsTest", "pointLightsSize = "+pointLightsSize);
					for (String uniform : program.getUniforms())
						Gdx.app.log("LightsTest", "Uniform: name="+uniform+", size="+program.getUniformSize(uniform)+", type="+program.getUniformType(uniform)+", location="+program.getUniformLocation(uniform));
				}
				
				boolean lightsSet;
				@Override
				public void begin (Camera camera, RenderContext context) {
					lightsSet = false;
					super.begin(camera, context);
				}
				
				@Override
				protected final void bindLights(final Renderable renderable) {
					final Environment lights = renderable.environment;
					final Array<DirectionalLight> dirs = lights.directionalLights; 
					final Array<PointLight> points = lights.pointLights;
					
					if (dirLightsLoc >= 0) {
						for (int i = 0; i < directionalLights.length; i++) {
							if (dirs == null || i >= dirs.size) {
								if (lightsSet && directionalLights[i].color.r == 0f && directionalLights[i].color.g == 0f && directionalLights[i].color.b == 0f)
									continue;
								directionalLights[i].color.set(0,0,0,1);
							} else if (lightsSet && directionalLights[i].equals(dirs.get(i)))
								continue;
							else
								directionalLights[i].set(dirs.get(i));
							
							int idx = dirLightsLoc + i * dirLightsSize; 
							program.setUniformf(idx+dirLightsColorOffset, directionalLights[i].color.r, directionalLights[i].color.g, directionalLights[i].color.b);
							program.setUniformf(idx+dirLightsDirectionOffset, directionalLights[i].direction);
							if (dirLightsSize <= 0)
								break;
						}
					}
					
					if (pointLightsLoc >= 0) {
						for (int i = 0; i < pointLights.length; i++) {
							if (points == null || i >= points.size) {
								if (lightsSet && pointLights[i].intensity == 0f)
									continue;
								pointLights[i].intensity = 0f;
							} else if (lightsSet && pointLights[i].equals(points.get(i)))
								continue;
							else
								pointLights[i].set(points.get(i));

							int idx = pointLightsLoc + i * pointLightsSize;
							program.setUniformf(idx+pointLightsColorOffset, pointLights[i].color.r, pointLights[i].color.g, pointLights[i].color.b, 1f);
							program.setUniformf(idx+pointLightsPositionOffset, pointLights[i].position.x, pointLights[i].position.y, pointLights[i].position.z, 0f);
							if (pointLightsIntensityOffset >= 0)
								program.setUniformf(idx+pointLightsIntensityOffset, pointLights[i].intensity);
							if (pointLightsSize <= 0)
								break;
						}
					}

					if (lights.has(ColorAttribute.Fog)) {
						set(u_fogColor, ((ColorAttribute)lights.get(ColorAttribute.Fog)).color);
					}
					
					if (lights.shadowMap != null) {
						set(u_shadowMapProjViewTrans, lights.shadowMap.getProjViewTrans());
						set(u_shadowTexture, lights.shadowMap.getDepthMap());
						set(u_shadowPCFOffset, 1.f / (float)(2f * lights.shadowMap.getDepthMap().texture.getWidth()));
					}
					
					lightsSet = true;
				}
			};
		}
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
