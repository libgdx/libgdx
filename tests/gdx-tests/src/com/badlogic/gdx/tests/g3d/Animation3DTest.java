package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

public class Animation3DTest extends BaseG3dHudTest {
	ModelInstance skydome;
	Model floorModel;
	ModelInstance character;
	AnimationController animation;
	DirectionalShadowLight shadowLight;
	ModelBatch shadowBatch;
	
	Lights lights;

	@Override
	public void create () {
		super.create();
		lights = new Lights(0.4f, 0.4f, 0.4f).add(
			(shadowLight = new DirectionalShadowLight(1024, 1024, 30f, 30f, 1f, 100f)).set(0.8f, 0.8f, 0.8f, -1f, -.8f, -.2f)
		);
		lights.shadowMap = shadowLight;
		inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
		cam.position.set(25, 25, 25);
		cam.lookAt(0, 0, 0);
		cam.update();
		modelsWindow.setVisible(false);
		assets.load("data/g3d/skydome.g3db", Model.class);
		assets.load("data/g3d/concrete.png", Texture.class);
		loading = true;
		trForward.translation.set(0,0,8f);
		trBackward.translation.set(0,0,-8f);
		trLeft.rotation.setFromAxis(Vector3.Y, 90);
		trRight.rotation.setFromAxis(Vector3.Y, -90);
		
		ModelBuilder builder = new ModelBuilder();
		builder.begin();
		MeshPartBuilder part = builder.part("floor", GL10.GL_TRIANGLES, Usage.Position | Usage.TextureCoordinates | Usage.Normal, new Material());
		for (float x = -200f; x < 200f; x += 10f) {
			for (float z = -200f; z < 200f; z += 10f) {
				part.rect(x, 0, z+10f, x+10f, 0, z+10f, x+10f, 0, z, x, 0, z, 0, 1, 0);
			}
		}
		floorModel = builder.end();
		
		shadowBatch = new ModelBatch(new DepthShaderProvider());
	}

	final AnimationController.Transform trTmp = new AnimationController.Transform();
	final AnimationController.Transform trForward = new AnimationController.Transform();
	final AnimationController.Transform trBackward = new AnimationController.Transform();
	final AnimationController.Transform trRight = new AnimationController.Transform();
	final AnimationController.Transform trLeft = new AnimationController.Transform();
	final Matrix4 tmpMatrix = new Matrix4();
	final Vector3 tmpVector = new Vector3();
	int status = 0;
	final static int idle = 1;
	final static int walk = 2;
	final static int back = 3;
	final static int attack = 4;
	float angle = 0f;
	@Override
	public void render () {
		if (character != null) {
			animation.update(Gdx.graphics.getDeltaTime());
			if (upKey) {
				if (!animation.inAction) {
					trTmp.idt().lerp(trForward, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
					character.transform.mul(trTmp.toMatrix4(tmpMatrix));
				}
				if (status != walk) {
					animation.animate("Walk", -1, 1f, null, 0.2f);
					status = walk;
				}
			} else if (downKey) {
				if (!animation.inAction) {
					trTmp.idt().lerp(trBackward, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
					character.transform.mul(trTmp.toMatrix4(tmpMatrix));
				}
				if (status != back) {
					animation.animate("Walk", -1, -1f, null, 0.2f);
					status = back;
				}
			} else if (status != idle) {
					animation.animate("Idle", -1, 1f, null, 0.2f);
					status = idle;
			}
			if (rightKey && (status == walk || status == back) && !animation.inAction) {
				trTmp.idt().lerp(trRight, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
				character.transform.mul(trTmp.toMatrix4(tmpMatrix));
			} else if (leftKey && (status == walk || status == back) && !animation.inAction) {
				trTmp.idt().lerp(trLeft, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
				character.transform.mul(trTmp.toMatrix4(tmpMatrix));
			}
			if (spaceKey && !animation.inAction) {
				animation.action("Attack", 1, 1f, null, 0.2f);
			}
		}
		
		if (character != null) {
			shadowLight.begin(character.transform.getTranslation(tmpVector), cam.direction);
			shadowBatch.begin(shadowLight.getCamera());
			if (character != null)
				shadowBatch.render(character);
			shadowBatch.end();
			shadowLight.end();
		}
		super.render();
	}
	
	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		batch.render(instances, lights);
		if (skydome != null)
			batch.render(skydome);
	}
	
	@Override
	protected void getStatus (StringBuilder stringBuilder) {
		super.getStatus(stringBuilder);
		stringBuilder.append(" use arrow keys to walk around, space to attack.");
	}
	
	@Override
	protected void onModelClicked(final String name) {	}
	
	@Override
	protected void onLoaded() {
		if (skydome == null) {
			skydome = new ModelInstance(assets.get("data/g3d/skydome.g3db", Model.class));
			floorModel.materials.get(0).set(TextureAttribute.createDiffuse(assets.get("data/g3d/concrete.png", Texture.class)));
			instances.add(new ModelInstance(floorModel));
			assets.load("data/g3d/knight.g3db", Model.class);
			loading = true;
		}
		else if (character == null) {
			character = new ModelInstance(assets.get("data/g3d/knight.g3db", Model.class));
			BoundingBox bbox = new BoundingBox();
			character.calculateBoundingBox(bbox);
			character.transform.setToRotation(Vector3.Y, 180).trn(0, -bbox.min.y, 0);
			instances.add(character);
			animation = new AnimationController(character);
			animation.animate("Idle", -1, 1f, null, 0.2f);
			status = idle;
			for (Animation anim : character.animations)
				Gdx.app.log("Test", anim.id);
		}
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
	
	boolean rightKey, leftKey, upKey, downKey, spaceKey;
	@Override
	public boolean keyUp (int keycode) {
		if (keycode == Keys.LEFT)
			leftKey = false;
		if (keycode == Keys.RIGHT)
			rightKey = false;
		if (keycode == Keys.UP)
			upKey = false;
		if (keycode == Keys.DOWN)
			downKey = false;
		if (keycode == Keys.SPACE)
			spaceKey = false;
		return super.keyUp(keycode);
	}
	
	@Override
	public boolean keyDown (int keycode) {
		if (keycode == Keys.LEFT)
			leftKey = true;
		if (keycode == Keys.RIGHT)
			rightKey = true;
		if (keycode == Keys.UP)
			upKey = true;
		if (keycode == Keys.DOWN)
			downKey = true;
		if (keycode == Keys.SPACE)
			spaceKey = true;
		return super.keyDown(keycode);
	}
	
	@Override
	public void dispose () {
		super.dispose();
		floorModel.dispose();
		shadowLight.dispose();
	}
}