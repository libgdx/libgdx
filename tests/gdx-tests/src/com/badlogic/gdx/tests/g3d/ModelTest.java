package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;

public class ModelTest extends BaseG3dHudTest {
	Lights lights = new Lights(0.4f, 0.4f, 0.4f).add(
		//new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -.8f, -.2f)
		new PointLight().set(1f, 0f, 0f, 5f, 5f, 5f, 20f),
		new PointLight().set(0f, 0f, 1f, -5f, 5f, 5f, 15f),
		new PointLight().set(0f, 1f, 0f, 0f, 5f, -5f, 7f)
	);
	
	ObjectMap<ModelInstance, AnimationController> animationControllers = new ObjectMap<ModelInstance, AnimationController>(); 

	@Override
	public void create () {
		super.create();
		showAxes = true;
		//DefaultShader.defaultCullFace = 0;
		onModelClicked("g3d/teapot.g3db");
	}

	private final static Vector3 tmpV = new Vector3();
	private final static Quaternion tmpQ = new Quaternion();
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
