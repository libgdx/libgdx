package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpawnEllipseSide;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.PointSpriteParticle;
import com.badlogic.gdx.graphics.g3d.particles.controllers.BillboardParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ModelInstanceParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ParticleControllerParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.PointSpriteParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.FaceDirectionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.PointRegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomModelInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomParticleControllerInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomPointRegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.BillboardColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.PointSpriteColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.FaceDirectionInfluencer.ParticleControllerFaceDirectionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomColorInfluencer.ModelInstanceRandomColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer.*;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.BillboardSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.ModelInstanceSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.ParticleControllerSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.PointSpriteSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.BillboardVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.ModelInstanceVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.ParticleControllerVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.PointSpriteVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ModelInstanceRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer.AlignMode;
import com.badlogic.gdx.graphics.g3d.particles.values.AngularVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.EllipseSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.LineSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.MeshSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.graphics.g3d.particles.values.SpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PrimitiveSpawnShapeValue.SpawnSide;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardBrownianVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardPolarVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardRotationVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardWeightVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceBrownianVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceCentripetalVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceRotationVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerCentripetalVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerRotationVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.PointPolarVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.PointRotationVelocityValue;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;

public class ParticleControllerTest extends BaseG3dTest{
	public static final String DEFAULT_PARTICLE = "data/particle-star.png",
										EXPLOSION_TEXTURE = "data/explosion.png",
										CUBE_MODEL = "data/cube.obj",
										SPHERE_MODEL = "data/sphere.obj",
										CAR_MODEL = "data/car.obj",
										MONKEY_MODEL = "data/monkey.g3db";
	Quaternion tmpQuaternion = new Quaternion();
	Matrix4 tmpMatrix = new Matrix4(), tmpMatrix4 = new Matrix4();
	Vector3 tmpVector = new Vector3();
	
	private class RotationAction extends Action{
		private ParticleController emitter;
		Vector3 axis;
		float angle;
		
		public RotationAction (ParticleController emitter, Vector3 axis, float angle) {
			this.emitter = emitter;
			this.axis = axis;
			this.angle = angle;
		}

		@Override
		public boolean act (float delta) {
			emitter.getTransform(tmpMatrix);
			tmpQuaternion.set(axis, angle*delta).toMatrix(tmpMatrix4.val);
			tmpMatrix4.mul(tmpMatrix);
			emitter.setTransform(tmpMatrix4);
			return false;
		}
	}
	
	private class Rectangle{
		
		float w,h,
				x, y, z;
		
		public Rectangle(float x, float y, float z, float w, float h){
			this.x = x;
			this.y = y;
			this.z = z;
			this.w = w;
			this.h = h;
		}
		
		public Vector3 get(Vector3 vector, float t){
			if(0 <= t && t < 0.25f ) vector.set(t/0.25f*w, 0, 0);
			else if(0.25f <= t && t < 0.5f ) vector.set(w, 0, -(t-0.25f)/0.25f*h);
			else if(0.5f <= t && t < 0.75f ) vector.set( (1-(t-0.5f)/0.25f)*w, 0, -h);
			else if(0.75f <= t && t < 1f ) vector.set(0, 0, -(1-(t-0.75f)/0.25f)*h);
			else vector.set(0,0,0);
			return vector.add(x, y, z);
		}
	}
	
	private class RectangleTranslationAction extends Action{
		private ParticleController emitter;
		Rectangle rectangle;
		float t=0;
		
		public RectangleTranslationAction (ParticleController emitter, Rectangle rectangle) {
			this.emitter = emitter;
			this.rectangle = rectangle;
		}

		@Override
		public boolean act (float delta) {
			t = (t + delta)%1f;
			emitter.getTransform(tmpMatrix);
			tmpMatrix.setTranslation(rectangle.get(tmpVector, t));
			emitter.setTransform(tmpMatrix);
			return false;
		}
	}
	
	
	
	Array<ParticleController> emitters;
	Array<Action> actions;
	Environment environment;
	@Override
	public void create () {
		super.create();
		emitters = new Array<ParticleController>();
		actions = new Array<Action>();
		assets.load(DEFAULT_PARTICLE, Texture.class);
		assets.load(EXPLOSION_TEXTURE, Texture.class);
		assets.load(CUBE_MODEL, Model.class);
		assets.load(SPHERE_MODEL, Model.class);
		assets.load(CAR_MODEL, Model.class);
		assets.load(MONKEY_MODEL, Model.class);
		loading = true;
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0f, 0f, 0.1f, 1f));
		environment.add(new DirectionalLight().set(1f, 1f, 1f,  0, -0.5f, -1 ));
	}
	
	@Override
	protected void onLoaded () {
		Texture particleTexture = assets.get(DEFAULT_PARTICLE);
		//Texture particleTexture = assets.get(EXPLOSION_TEXTURE);
		/*
		Model sphere = assets.get(SPHERE_MODEL);
		Material material = sphere.materials.get(0);
		ColorAttribute colorAttribute = (ColorAttribute)material.get(ColorAttribute.Diffuse); 
		colorAttribute.color.set(1, 0.12156863f, 0.047058824f, 1);
		*/
		//material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE, 1));
		
		Model monkey = assets.get(MONKEY_MODEL);
		/*
		instances.add(new ModelInstance(monkey));
		for(Material material : monkey.materials ){
			//material = monkey.materials.get(0);
			//material.remove(ColorAttribute.Specular);
			for(Attribute attribute : material){
				Gdx.app.log("INFERNO", "attribute "+attribute);
			}
		}
		*/
		//colorAttribute = (ColorAttribute)material.get(ColorAttribute.Diffuse); 
		//colorAttribute.color.set(1, 0.12156863f, 0.047058824f, 1);
		
		//X
		ParticleController controller = createBillboardController(new float[] {1, 0.12156863f, 0.047058824f}, particleTexture);
		//ParticleController controller = createModelInstanceController(sphere);
		//ParticleController controller = createModelInstanceController((Model)assets.get(CAR_MODEL));
		//ParticleController controller = createModelInstanceController(new float[] {1, 0.12156863f, 0.047058824f}, monkey);
		//ParticleController controller = createPointController(new float[] {1, 0.12156863f, 0.047058824f}, particleTexture);
		//ParticleController controller = createParticleControllerController(createLineController(new float[] {1, 0.12156863f, 0.047058824f}, particleTexture));
		//controller.translate(Vector3.tmp.set(5,5,0));
		//controller.rotate(Vector3.X, -90);
		//emitter.setAttached(true);
		//actions.add(new RotationAction(controller, Vector3.X, 360));
		//actions.add(new RectangleTranslationAction(controller, new Rectangle(-10, 0, 10, 20, 20)));
		controller.init();
		controller.start();
		emitters.add(controller);
		
		/*
		//Y
		controller = createController(new float[] { 0.12156863f, 1, 0.047058824f}, particleTexture);
		controller.translate(Vector3.tmp.set(0,5,-5));
		controller.rotate(Vector3.Z, -90);
		//emitter.setAttached(true);
		actions.add(new EmitterAction(controller, Vector3.Y, -360));
		emitters.add(controller);
		
		//Z
		controller = createController(new float[] {0.12156863f, 0.047058824f, 1}, particleTexture);
		controller.translate(Vector3.tmp.set(0,5,5));
		controller.rotate(Vector3.Z, -90);
		//emitter.setAttached(true);
		actions.add(new EmitterAction(controller, Vector3.Z, -360));		
		emitters.add(controller);
		*/
	}

	
	private ParticleControllerParticleController createParticleControllerController(ParticleController particleController)
	{
		//Emitter
		RegularEmitter regularEmitter = new RegularEmitter();
		regularEmitter.getDuration().setLow(5000);
		regularEmitter.getEmission().setHigh(100);
		regularEmitter.getLife().setHigh(10000);
		//regularEmitter.getLife().setTimeline(new float[] {0, 1});
		//regularEmitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		//regularEmitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		//regularEmitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		regularEmitter.setMaxParticleCount(10);
		
		//Source
		//MeshSpawnShapeValue spawnShape = new MeshSpawnShapeValue();
		//spawnShape.setMesh(models[0].meshes.get(0));
		//PointSpawnShapeValue spawnShape = new PointSpawnShapeValue();
		EllipseSpawnShapeValue spawnShape = new EllipseSpawnShapeValue();
		spawnShape.spawnDepthValue.setHigh(8);
		spawnShape.spawnWidthValue.setHigh(8);
		spawnShape.spawnHeightValue.setHigh(8);
		spawnShape.setEdges(true);
		spawnShape.setActive(true);
		
		//Renderer
		ParticleControllerRenderer renderer = new ParticleControllerRenderer();
		
		//Influencers
		//Velocity
		ParticleControllerVelocityInfluencer velocityInfluencer = new ParticleControllerVelocityInfluencer();
		ParticleControllerCentripetalVelocityValue velocityValue = new ParticleControllerCentripetalVelocityValue();
		velocityValue.getStrength().setHigh(2);
		velocityValue.getStrength().setActive(true);
		velocityValue.isGlobal = true;
		velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);
		
		//Scale
		ParticleControllerScaleInfluencer scaleInfluencer = new ParticleControllerScaleInfluencer();
		scaleInfluencer.scaleValue.setHigh(1);
		scaleInfluencer.scaleValue.setTimeline(new float[] {0, 1});
		scaleInfluencer.scaleValue.setScaling(new float[] {1, 0});
		
		//Rotation
		ParticleControllerRotationVelocityValue rotationVelocity = new ParticleControllerRotationVelocityValue();
		ScaledNumericValue thetaValue = rotationVelocity.getTheta();
		thetaValue.setHigh(0, 360);
		thetaValue.setActive(true);
		ScaledNumericValue phiValue = rotationVelocity.getPhi();
		phiValue.setHigh(90);
		phiValue.setActive(true);
		rotationVelocity.getStrength().setHigh(360);
		rotationVelocity.getStrength().setActive(true);
		//rotationVelocity.setGlobal(true);
		rotationVelocity.setActive(true);
		velocityInfluencer.velocities.add(rotationVelocity);
		
		ParticleControllerParticleController controller = new ParticleControllerParticleController("controller",
																	regularEmitter, renderer, 
																	new RandomParticleControllerInfluencer(particleController),
																	new ParticleControllerSpawnInfluencer(spawnShape),
																	velocityInfluencer
																	//new ParticleControllerFaceDirectionInfluencer()
																	//scaleInfluencer
																	);
		return controller;
	}


	private ParticleController createPointController (float[] colors, Texture particleTexture) {
		//Emitter
		RegularEmitter regularEmitter = new RegularEmitter();
		regularEmitter.getDuration().setLow(10000);
		regularEmitter.getEmission().setHigh(9000);
		regularEmitter.getLife().setHigh(500, 1000);
		regularEmitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		regularEmitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		regularEmitter.setMaxParticleCount(10000);
		
		//Source
		//SpawnShapeValue spawnShape = new PointSpawnShapeValue();
		//spawnShape.setActive(true);
		EllipseSpawnShapeValue spawnShape = new EllipseSpawnShapeValue();
		spawnShape.spawnDepthValue.setHigh(10);
		spawnShape.spawnWidthValue.setHigh(10);
		spawnShape.spawnHeightValue.setHigh(10);
		spawnShape.setEdges(true);
		//spawnShape.setSide(SpawnSide.top);
		spawnShape.setActive(true);
		
		//Renderer
		PointRenderer renderer = new PointRenderer();
		renderer.setCamera(cam);
		//renderer.setAdditive(true);
		//renderer.setTexture(particleTexture);

		//Influencers
		//Color
		PointSpriteColorInfluencer colorInfluencer = new PointSpriteColorInfluencer();
		colorInfluencer.colorValue.setColors(colors);
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.2f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 1, 0.75f, 0});
		
		//Velocity
		PointSpriteVelocityInfluencer velocityInfluencer = new PointSpriteVelocityInfluencer();
		/*
		BillboardBrownianVelocityValue velocityValue = new BillboardBrownianVelocityValue();
		ScaledNumericValue weight = velocityValue.getStrength();
		weight.setHigh(1, 5);
		weight.setActive(true);
		velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);
		*/
		
		/*
		BillboardWeightVelocityValue velocityValue = new BillboardWeightVelocityValue();
		ScaledNumericValue weight = velocityValue.getStrength();
		weight.setHigh(1f);
		weight.setLow(0);
		weight.setTimeline(new float[] {0, 0.5f, 1});
		weight.setScaling(new float[] {0.5f, 0, 0});
		weight.setActive(true);
		velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);
		*/
		
		PointSpriteScaleInfluencer scaleInfluencer = new PointSpriteScaleInfluencer();
		scaleInfluencer.scaleValue.setHigh(1);
		//scaleInfluencer.scaleValue.setTimeline(new float[] {0, 1});
		//scaleInfluencer.scaleValue.setScaling(new float[] {1, 0});
		
		//Rotation
		PointRotationVelocityValue rotationVelocity = new PointRotationVelocityValue();
		rotationVelocity.strengthValue.setHigh(360);
		rotationVelocity.strengthValue.setActive(true);
		velocityInfluencer.velocities.add(rotationVelocity);

		PointPolarVelocityValue velocityValue2 = new PointPolarVelocityValue();
		ScaledNumericValue thetaValue = velocityValue2.getTheta();
		thetaValue.setHigh(0, 359);
		thetaValue.setActive(true);
		ScaledNumericValue phiValue = velocityValue2.getPhi();
		phiValue.setHigh(45, 135);
		phiValue.setLow(90);
		phiValue.setTimeline(new float[] {0, 0.5f, 1});
		phiValue.setScaling(new float[] {1, 0, 0});
		phiValue.setActive(true);
		velocityValue2.getStrength().setHigh(5, 10);
		velocityValue2.getStrength().setActive(true);
		//velocityValue.setGlobal(true);
		velocityValue2.setActive(true);
		velocityInfluencer.velocities.add(velocityValue2);
		
		
		ParticleController controller = new PointSpriteParticleController("controller",
																	regularEmitter, renderer,
																	new PointSpriteSpawnInfluencer(spawnShape), 
																	//new PointRegionInfluencer(particleTexture),
																	new RandomPointRegionInfluencer(particleTexture, 5, 5),
																	velocityInfluencer
																	//colorInfluencer
																	//scaleInfluencer
																	);
		
		return controller;
	}

	private ParticleController createModelInstanceController (float[] colors, Model...models) {
		//Emitter
		RegularEmitter regularEmitter = new RegularEmitter();
		regularEmitter.getDuration().setLow(10000);
		regularEmitter.getEmission().setHigh(100);
		regularEmitter.getLife().setHigh(10000);
		regularEmitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		regularEmitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		regularEmitter.setMaxParticleCount(100);
		
		//Source
		//MeshSpawnShapeValue spawnShape = new MeshSpawnShapeValue();
		//spawnShape.setMesh(models[0].meshes.get(0));
		//PointSpawnShapeValue spawnShape = new PointSpawnShapeValue();
		EllipseSpawnShapeValue spawnShape = new EllipseSpawnShapeValue();
		spawnShape.spawnDepthValue.setHigh(8);
		spawnShape.spawnWidthValue.setHigh(8);
		spawnShape.spawnHeightValue.setHigh(8);
		spawnShape.setEdges(true);
		spawnShape.setActive(true);
		
		//Renderer
		ModelInstanceRenderer renderer = new ModelInstanceRenderer();
		
		//Influencers
		//Velocity
		ModelInstanceVelocityInfluencer velocityInfluencer = new ModelInstanceVelocityInfluencer();
		ModelInstanceBrownianVelocityValue velocityValue = new ModelInstanceBrownianVelocityValue();
		ScaledNumericValue weight = velocityValue.getStrength();
		weight.setHigh(10);
		weight.setActive(true);
		velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);
		/*
		ModelInstanceCentripetalVelocityValue velocityValue = new ModelInstanceCentripetalVelocityValue();
		velocityValue.getStrength().setHigh(1);
		velocityValue.getStrength().setActive(true);
		//velocityValue.setGlobal(true);
		velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);
		*/
		
		//Color
		ModelInstanceRandomColorInfluencer colorInfluencer = new ModelInstanceRandomColorInfluencer();
		
		//Scale
		/*
		ScaleInfluencer scaleInfluencer = ScaleInfluencer.modelInstance();
		scaleInfluencer.scaleValue.setHigh(1);
		scaleInfluencer.scaleValue.setTimeline(new float[] {0, 1});
		scaleInfluencer.scaleValue.setScaling(new float[] {1, 0});
		*/
		
		//Rotation
		//Rotation around Y axis
		ModelInstanceRotationVelocityValue rotationVelocity = new ModelInstanceRotationVelocityValue();
		ScaledNumericValue thetaValue = rotationVelocity.getTheta();
		thetaValue.setHigh(0, 360);
		thetaValue.setActive(true);
		ScaledNumericValue phiValue = rotationVelocity.getPhi();
		phiValue.setHigh(90);
		phiValue.setActive(true);
		rotationVelocity.getStrength().setHigh(360);
		rotationVelocity.getStrength().setActive(true);
		//rotationVelocity.setGlobal(true);
		rotationVelocity.setActive(true);
		velocityInfluencer.velocities.add(rotationVelocity);
		
		//Rotation around X axis
		rotationVelocity = new ModelInstanceRotationVelocityValue();
		thetaValue = rotationVelocity.getTheta();
		thetaValue.setHigh(45);
		thetaValue.setActive(true);
		phiValue = rotationVelocity.getPhi();
		phiValue.setHigh(0);
		phiValue.setActive(true);
		rotationVelocity.getStrength().setHigh(360);
		rotationVelocity.getStrength().setActive(true);
		//velocityValue.setGlobal(true);
		rotationVelocity.setActive(true);		
		velocityInfluencer.velocities.add(rotationVelocity);
		
		
		//Rotation around X axis
		/*
		velocityValue = new VelocityValue();
		velocityValue.type = VelocityType.polar;
		thetaValue = velocityValue.getTheta();
		thetaValue.setHigh(0);
		thetaValue.setActive(true);
		phiValue = velocityValue.getPhi();
		phiValue.setHigh(360);
		phiValue.setActive(true);
		velocityValue.getStrength().setHigh(360);
		velocityValue.getStrength().setActive(true);
		//velocityValue.setGlobal(true);
		velocityValue.setActive(true);		
		rotationInfluencer.velocities.add(velocityValue);
		*/
		
		ModelInstanceParticleController controller = new ModelInstanceParticleController("controller",
																	regularEmitter, renderer, 
																	new RandomModelInfluencer(models),
																	new ModelInstanceSpawnInfluencer(spawnShape), 
																	velocityInfluencer,
																	//scaleInfluencer,
																	//new FaceDirectionInfluencer(),
																	colorInfluencer
																	//new ModelInstanceRandomRotationInfluencer(1, 360, 360)
																	//rotationInfluencer
																	);
		
		return controller;
	}

	private ParticleController createBillboardController (float[] colors, Texture particleTexture) {
		//Emitter
		RegularEmitter regularEmitter = new RegularEmitter();
		regularEmitter.getDuration().setLow(10000);
		regularEmitter.getEmission().setHigh(800);
		regularEmitter.getLife().setHigh(10000);
		regularEmitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		regularEmitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		regularEmitter.setMaxParticleCount(1000);
		
		//Source
		/*
		SpawnShapeValue spawnShape = new PointSpawnShapeValue();
		spawnShape.setActive(true);
		*/
		EllipseSpawnShapeValue spawnShape = new EllipseSpawnShapeValue();
		spawnShape.spawnDepthValue.setHigh(10);
		spawnShape.spawnWidthValue.setHigh(10);
		spawnShape.spawnHeightValue.setHigh(10);
		spawnShape.setEdges(true);
		//spawnShape.setSide(SpawnSide.top);
		//spawnShape.setSide(SpawnSide.bottom);
		spawnShape.setActive(true);
		
		//Renderer
		BillboardRenderer renderer = new BillboardRenderer(AlignMode.Screen, true);
		renderer.setCamera(cam);
		//renderer.setAdditive(true);

		//Influencers
		//Color
		ColorInfluencer colorInfluencer = new BillboardColorInfluencer();
		colorInfluencer.colorValue.setColors(colors);
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.2f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 1, 0.75f, 0});
		
		//Velocity
		BillboardVelocityInfluencer velocityInfluencer = new BillboardVelocityInfluencer();
		/*
		BillboardBrownianVelocityValue velocityValue = new BillboardBrownianVelocityValue();
		ScaledNumericValue weight = velocityValue.getStrength();
		weight.setHigh(1, 5);
		weight.setActive(true);
		velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);
		*/
		
		/*
		BillboardWeightVelocityValue velocityValue = new BillboardWeightVelocityValue();
		ScaledNumericValue weight = velocityValue.getStrength();
		weight.setHigh(1f);
		weight.setLow(0);
		weight.setTimeline(new float[] {0, 0.5f, 1});
		weight.setScaling(new float[] {0.5f, 0, 0});
		weight.setActive(true);
		velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);
		*/

		/*
		BillboardPolarVelocityValue velocityValue2 = new BillboardPolarVelocityValue();
		ScaledNumericValue thetaValue = velocityValue2.getTheta();
		thetaValue.setHigh(0, 359);
		thetaValue.setActive(true);
		ScaledNumericValue phiValue = velocityValue2.getPhi();
		phiValue.setHigh(45, 135);
		phiValue.setLow(90);
		phiValue.setTimeline(new float[] {0, 0.5f, 1});
		phiValue.setScaling(new float[] {1, 0, 0});
		phiValue.setActive(true);
		velocityValue2.getStrength().setHigh(5, 10);
		velocityValue2.getStrength().setActive(true);
		//velocityValue.setGlobal(true);
		velocityValue2.setActive(true);
		velocityInfluencer.velocities.add(velocityValue2);
		*/
		
		//Scale
		ScaleInfluencer scaleInfluencer = new BillboardScaleInfluencer();
		scaleInfluencer.scaleValue.setHigh(1);
		scaleInfluencer.scaleValue.setTimeline(new float[] {0, 1});
		scaleInfluencer.scaleValue.setScaling(new float[] {1, 0});
		
		//Rotation
		BillboardRotationVelocityValue rotationVelocity = new BillboardRotationVelocityValue();
		rotationVelocity.strengthValue.setHigh(360);
		rotationVelocity.strengthValue.setActive(true);
		velocityInfluencer.velocities.add(rotationVelocity);
		
		
		ParticleController controller = new BillboardParticleController("controller",
																	regularEmitter, renderer, 
																	new RegionInfluencer(new TextureRegion(particleTexture)),
																	new BillboardSpawnInfluencer(spawnShape) 
																	//velocityInfluencer,
																	//colorInfluencer
																	//scaleInfluencer
																	);
		
		return controller;
		
	}
	
	private ParticleController createLineController (float[] colors, Texture particleTexture) {
		//Emitter
		RegularEmitter regularEmitter = new RegularEmitter();
		regularEmitter.getDuration().setLow(10000);
		regularEmitter.getEmission().setHigh(800);
		regularEmitter.getLife().setHigh(500, 1000);
		regularEmitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		regularEmitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		regularEmitter.setMaxParticleCount(1000);
		
		//Source
		LineSpawnShapeValue spawnShape = new LineSpawnShapeValue();
		spawnShape.spawnDepthValue.setHigh(10);
		spawnShape.spawnWidthValue.setHigh(0);
		spawnShape.spawnHeightValue.setHigh(0);
		spawnShape.setActive(true);
		
		/*
		EllipseSpawnShapeValue spawnShape = new EllipseSpawnShapeValue();
		spawnShape.spawnDepthValue.setHigh(10);
		spawnShape.spawnWidthValue.setHigh(10);
		spawnShape.spawnHeightValue.setHigh(10);
		spawnShape.setEdges(true);
		//spawnShape.setSide(SpawnSide.top);
		spawnShape.setActive(true);
		*/
		
		//Renderer
		BillboardRenderer renderer = new BillboardRenderer(AlignMode.Screen, true);
		renderer.setCamera(cam);
		renderer.setAdditive(true);

		//Influencers
		//Color
		BillboardColorInfluencer colorInfluencer = new BillboardColorInfluencer();
		colorInfluencer.colorValue.setColors(colors);
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.2f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 1, 0.75f, 0});
		
		
		BillboardParticleController controller = new BillboardParticleController("controller",
																	regularEmitter, renderer, 
																	new RegionInfluencer(new TextureRegion(particleTexture)),
																	new BillboardSpawnInfluencer(spawnShape), 
																	colorInfluencer
																	);
		
		return controller;
		
	}
	
	
	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		if(emitters.size > 0){
			//Update
			float delta = Gdx.graphics.getDeltaTime();
			for(Action action : actions)
				action.act(delta);
			for (ParticleController controller : emitters){
				controller.update(delta);
				//batch.render(controller);
				batch.render(controller, environment);
			}
		}
		batch.render(instances, environment);
	}
}
