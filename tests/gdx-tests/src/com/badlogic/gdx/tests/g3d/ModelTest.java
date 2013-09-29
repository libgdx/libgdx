package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;

public class ModelTest extends BaseG3dHudTest {
	Environment lights;
	
	ObjectMap<ModelInstance, AnimationController> animationControllers = new ObjectMap<ModelInstance, AnimationController>(); 

	@Override
	public void create () {
		super.create();
		lights = new Environment();
		lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1.0f, -0.8f));
		
		cam.position.set(1,1,1);
		cam.lookAt(0,0,0);
		cam.update();
		showAxes = true;
		
		onModelClicked("g3d/teapot.g3db");
	}

	private final Vector3 tmpV = new Vector3();
	private final Quaternion tmpQ = new Quaternion();
	private final BoundingBox bounds = new BoundingBox();
	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		for (ObjectMap.Entry<ModelInstance, AnimationController> e : animationControllers.entries())
			e.value.update(Gdx.graphics.getDeltaTime());
		batch.render(instances, lights);
	}
	
	@Override
	protected void getStatus (StringBuilder stringBuilder) {
		super.getStatus(stringBuilder);

		for (final ModelInstance instance : instances) {
			if (instance.animations.size > 0) {
				stringBuilder.append(" press space or menu to switch animation");
				break;
			}
		}
	}
	
	protected String currentlyLoading;
	@Override
	protected void onModelClicked(final String name) {
		if (name == null)
			return;
		
		currentlyLoading = "data/"+name; 
		assets.load(currentlyLoading, Model.class);
		loading = true;
	}

	@Override
	protected void onLoaded() {
		if (currentlyLoading == null || currentlyLoading.isEmpty())
			return;
		
		instances.clear();
		animationControllers.clear();
		final ModelInstance instance = new ModelInstance(assets.get(currentlyLoading, Model.class));
		instances.add(instance);
		if (instance.animations.size > 0)
			animationControllers.put(instance, new AnimationController(instance));
		currentlyLoading = null;
		
		instance.calculateBoundingBox(bounds);
		cam.position.set(1,1,1).nor().scl(bounds.getDimensions().len() * 0.75f + bounds.getCenter().len());
		cam.up.set(0,1,0);
		cam.lookAt(0,0,0);
		cam.far = bounds.getDimensions().len() * 2.0f;
		cam.update();
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
			animIndex = (animIndex + 1) % (e.key.animations.size + 1);
			e.value.animate((animIndex == e.key.animations.size) ? null : e.key.animations.get(animIndex).id, -1, 1f, null, 0.2f);
		}
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
	
	@Override
	public boolean keyUp (int keycode) {
		if (keycode == Keys.SPACE || keycode == Keys.MENU)
			switchAnimation();
		return super.keyUp(keycode);
	}
}
