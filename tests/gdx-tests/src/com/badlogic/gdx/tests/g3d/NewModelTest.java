package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class NewModelTest extends BaseG3dHudTest {
	Lights lights = new Lights(0.4f, 0.4f, 0.4f).add(
		new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -1f, 0f)
		//new PointLight().set(1f, 0f, 0f, 5f, 5f, 5f, 15f),
		//new PointLight().set(0f, 0f, 1f, -5f, 5f, 5f, 15f),
		//new PointLight().set(0f, 1f, 0f, 0f, 5f, -5f, 7f)
		//new Light(0.5f, 0.5f, 0.5f, 1f),
		//new Light(0.5f, 0.5f, 0.5f, 1f, -1f, -2f, -3f)
	);

	@Override
	public void create () {
		super.create();
		showAxes = false;
		onModelClicked("g3d/knight.g3dj");
	}

	private final static Vector3 tmpV = new Vector3();
	private final static Quaternion tmpQ = new Quaternion();
	float counter;
	String currentAsset;
	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
//		if ((counter += Gdx.graphics.getDeltaTime()) > 1.f) {
//			assets.unload(currentAsset);
//			onModelClicked("normalbox.g3dj");
//			counter = 0f;
//		}
		for (final ModelInstance instance : instances) {
			if (instance.currentAnimation != null) {
				instance.currentAnimTime = (instance.currentAnimTime + Gdx.graphics.getDeltaTime()) % instance.currentAnimation.duration;
				for (final NodeAnimation nodeAnim : instance.currentAnimation.nodeAnimations) {
					nodeAnim.node.isAnimated = true;
					final int n = nodeAnim.keyframes.size - 1;
					if (n == 0) {
						nodeAnim.node.localTransform.idt().
							translate(nodeAnim.keyframes.get(0).translation).
							rotate(nodeAnim.keyframes.get(0).rotation).
							scl(nodeAnim.keyframes.get(0).scale);					
					}
					for (int i = 0; i < n; i++) {
						if (instance.currentAnimTime >= nodeAnim.keyframes.get(i).keytime && instance.currentAnimTime <= nodeAnim.keyframes.get(i+1).keytime) {
							final float t = (instance.currentAnimTime - nodeAnim.keyframes.get(i).keytime) / (nodeAnim.keyframes.get(i+1).keytime - nodeAnim.keyframes.get(i).keytime);
							nodeAnim.node.localTransform.idt().
								translate(tmpV.set(nodeAnim.keyframes.get(i).translation).lerp(nodeAnim.keyframes.get(i+1).translation, t)).
								rotate(tmpQ.set(nodeAnim.keyframes.get(i).rotation).slerp(nodeAnim.keyframes.get(i+1).rotation, t)).
								scl(tmpV.set(nodeAnim.keyframes.get(i).scale).lerp(nodeAnim.keyframes.get(i+1).scale, t));
							break;
						}
					}
				}
				instance.calculateTransforms();
			}
		}
		batch.render(instances, lights);
	}
	
	@Override
	protected void onModelClicked(final String name) {
		if (name == null)
			return;
		assets.load("data/"+name, Model.class);
		assets.finishLoading();
		
		instances.clear();
		final ModelInstance instance = new ModelInstance(assets.get(currentAsset = "data/"+name, Model.class));
		instances.add(instance);
		
		/*for (float x = -10; x <= 10; x += 10) {
			for (float z = -10; z <= 10; z += 10) {
				final ModelInstance instance = new ModelInstance(assets.get("data/"+name, Model.class));
				instance.transform.translate(x, 9.492372f, z);
				instances.add(instance);
			}
		}*/
	}
	
	protected void switchAnimation() {
		for (final ModelInstance instance : instances) {
			if (instance.animations.size > 0) {
				if (instance.currentAnimation != null) {
					for (final NodeAnimation nodeAnim : instance.currentAnimation.nodeAnimations)
						nodeAnim.node.isAnimated = false;
					instance.calculateTransforms();
				}
				int animIndex = -1;
				for (int i = 0; i < instance.animations.size; i++) {
					final Animation animation = instance.animations.get(i);
					if (instance.currentAnimation == animation) {
						animIndex = i;
						break;
					}
				}
				animIndex = (animIndex + 1) % (instance.animations.size + 1);
				instance.currentAnimation = animIndex == instance.animations.size ? null : instance.animations.get(animIndex);
				instance.currentAnimTime = 0f;
			}
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
