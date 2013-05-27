package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.StringBuilder;

public class SkeletonTest extends BaseG3dHudTest {
	ShapeRenderer shapeRenderer;
	
	@Override
	public void create () {
		super.create();
		showAxes = false;
		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setColor(Color.WHITE);
		onModelClicked("g3d/knight.g3db");
	}
	
	private final static Vector3 tmpV = new Vector3();
	private final static Pool<Vector3> vectorPool = new Pool<Vector3>() {
		@Override
		protected Vector3 newObject () {
			return new Vector3();
		}
	};
	private final static Quaternion tmpQ = new Quaternion();
	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		for (final ModelInstance instance : instances) {
			updateAnimation(instance);
			renderSkeleton(instance);
		}
		batch.render(instances);
	}
	
	public void renderSkeleton(final ModelInstance instance) {
		shapeRenderer.setProjectionMatrix(cam.combined);
		shapeRenderer.begin(ShapeType.Line);
		for (Node node : instance.nodes) {
			shapeRenderer.setColor(node.isAnimated ? Color.RED : Color.YELLOW);
			node.globalTransform.getTranslation(tmpV);
			shapeRenderer.box(tmpV.x, tmpV.y, tmpV.z, 0.5f, 0.5f, 0.5f);
			for (Node child : node.children)
				renderSkeleton(tmpV, child);
		}
		shapeRenderer.end();
	}
	
	public void renderSkeleton(final Vector3 from, final Node node) {
		final Vector3 pos = vectorPool.obtain();
		node.globalTransform.getTranslation(pos);
		shapeRenderer.setColor(node.isAnimated ? Color.RED : Color.YELLOW);
		shapeRenderer.box(pos.x, pos.y, pos.z, 0.5f, 0.5f, 0.5f);
		shapeRenderer.setColor(Color.WHITE);
		shapeRenderer.line(from.x, from.y, from.z, pos.x, pos.y, pos.z);
		for (Node child : node.children)
			renderSkeleton(pos, child);
		vectorPool.free(pos);
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
		final ModelInstance instance = new ModelInstance(assets.get(currentlyLoading, Model.class));
		instance.materials.get(0).set(new BlendingAttribute(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA, 0.5f));
		instances.add(instance);
		currentlyLoading = null;
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
	
	public void updateAnimation(final ModelInstance instance) {
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
}
