package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.particles.Emitter.SpawnShape;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEmitter;
import com.badlogic.gdx.graphics.g3d.particles.Emitter.ScaledNumericValue;
import com.badlogic.gdx.graphics.g3d.particles.Emitter.VelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEmitterNode;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class ParticleEmitterAnimationTest extends BaseG3dTest{
	public static final String DEFAULT_PARTICLE = "data/particle.png",
										KNIGHT_MODEL = "data/g3d/knight.g3db";
	public static final float ANIMATION_STEP = 0.01f;
	ParticleEmitter emitter;
	ObjectMap<ModelInstance, AnimationController> animationControllers = new ObjectMap<ModelInstance, AnimationController>(); 
	boolean isAnimationRunning = true;
	
	@Override
	public void create () {
		super.create();
		assets.load(DEFAULT_PARTICLE, Texture.class);
		assets.load(KNIGHT_MODEL, Model.class);
		loading = true;
		showAxes = false;
		Gdx.input.setInputProcessor(new InputMultiplexer(this, inputController));
	}
	
	@Override
	protected void onLoaded () {
		Texture particleTexture = assets.get(DEFAULT_PARTICLE);
		Model model = assets.get(KNIGHT_MODEL);

		emitter = createEmitter(new float[] {0.12156863f, 0.047058824f, 1}, particleTexture);
		ParticleEmitterNode node = new ParticleEmitterNode(emitter);
		node.id = "emitter";
		node.translation.set(2.5f, 0, 0);
		node.rotation.set(Vector3.Z,-90);
		//node.scale.set(0.5f, 0.5f, 0.5f);
		Node swordNode = model.getNode("sword", true, true);
		swordNode.children.add(node);
		//model.nodes.add(node);
		ModelInstance instance = new ModelInstance(model);
		instances.add(instance);
		emitter = ((ParticleEmitterNode)instance.getNode("emitter")).emitter;
		
		animationControllers.put(instance, new AnimationController(instance));
	}
	
	protected void switchAnimation() {
		for (ObjectMap.Entry<ModelInstance, AnimationController> e : animationControllers.entries()) {
			int animIndex = 0;
			if (e.value.current != null) {
				for (int i = 0; i < e.key.animations.size; i++) {
					final Animation animation = e.key.animations.get(i);
					if (e.value.current.animation == animation) {
						animIndex = i;
						break;
					}
				}
			}
			animIndex = (animIndex + 1) % e.key.animations.size;
			e.value.animate(e.key.animations.get(animIndex).id, -1, 1f, null, 0.2f);
		}
	}

	private ParticleEmitter createEmitter (float[] color, Texture texture) {
		ParticleEmitter emitter = new ParticleEmitter();

		emitter.getDuration().setLow(3000);
		emitter.setMaxParticleCount(250);
		emitter.getEmission().setHigh(50);
		emitter.getLife().setHigh(500, 1000);
		emitter.getScaleValue().setHigh(1);

		//Velocity
		VelocityValue velocityValue = emitter.getVelocityValue(0); 
		ScaledNumericValue thetaValue = velocityValue.getTheta();
		thetaValue.setHigh(-90);
		thetaValue.setActive(true);
		
		ScaledNumericValue phiValue = velocityValue.getPhi();
		phiValue.setHigh(45, 135);
		phiValue.setLow(90);
		phiValue.setTimeline(new float[] {0, 0.5f, 1});
		phiValue.setScaling(new float[] {1, 0, 0});
		phiValue.setActive(true);

		velocityValue.getStrength().setHigh(5);
		velocityValue.getStrength().setActive(true);
		velocityValue.setActive(true);


		//Spawn
		emitter.getSpawnShape().setShape(SpawnShape.line);
		emitter.getSpawnDepth().setHigh(0);
		emitter.getSpawnHeight().setHigh(5);
		emitter.getSpawnWidth().setHigh(0);
		
		//Color
		emitter.getTint().setColors(color);
		emitter.getTransparency().setHigh(1, 1);
		emitter.getTransparency().setTimeline(new float[] {0, 0.2f, 0.8f, 1});
		emitter.getTransparency().setScaling(new float[] {0, 1, 0.75f, 0});

		emitter.setContinuous(true);
		emitter.setRegionFromTexture(texture);
		emitter.setAttached(true);
		emitter.setCamera(cam);
		return emitter;
	}

	Quaternion tmpQuaternion = new Quaternion();
	Matrix4 tmpMatrix = new Matrix4(), tmpMatrix4 = new Matrix4();
	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		if(instances.size > 0)
		{
			float delta = Gdx.graphics.getDeltaTime();
			
			//Emitters
			boolean complete = true;
			emitter.update(delta);
			if (!emitter.isComplete()) complete = false;
			if (complete) emitter.start();
			
			//Animations
			if(isAnimationRunning){
				for (ObjectMap.Entry<ModelInstance, AnimationController> e : animationControllers.entries())
					e.value.update(delta);
			}
			
			//Render
			batch.render(instances);
		}
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
	
	@Override
	public boolean keyUp (int keycode) {
		if (keycode == Keys.ENTER)
			switchAnimation();
		if (keycode == Keys.SPACE)
			setAnimationRunning(!isAnimationRunning);
		if (keycode == Keys.UP)
			increaseAnimationSpeed(ANIMATION_STEP);
		if (keycode == Keys.DOWN)
			increaseAnimationSpeed(-ANIMATION_STEP);
		return super.keyUp(keycode);
	}
				
	private void setAnimationRunning ( boolean running) {
		isAnimationRunning = running;
	}

	protected void increaseAnimationSpeed(float amount){
		for (ObjectMap.Entry<ModelInstance, AnimationController> e : animationControllers.entries()) {
			if (e.value.current != null)
				e.value.current.speed += amount;
		}
	}
}
