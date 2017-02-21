/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

public class Animation3DTest extends BaseG3dHudTest {
	ModelInstance skydome;
	Model floorModel;
	ModelInstance character;
	Node ship;
	ModelInstance tree;
	AnimationController animation;
	DirectionalShadowLight shadowLight;
	ModelBatch shadowBatch;

	Environment lights;

	@Override
	public void create () {
		super.create();
		lights = new Environment();
		lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		lights.add((shadowLight = new DirectionalShadowLight(1024, 1024, 30f, 30f, 1f, 100f))
			.set(0.8f, 0.8f, 0.8f, -.4f, -.4f, -.4f));
		lights.shadowMap = shadowLight;
		inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
		cam.position.set(25, 25, 25);
		cam.lookAt(0, 0, 0);
		cam.update();
		modelsWindow.setVisible(false);
		assets.load("data/g3d/skydome.g3db", Model.class);
		assets.load("data/g3d/concrete.png", Texture.class);
		assets.load("data/tree.png", Texture.class);
		assets.load("data/g3d/ship.obj", Model.class);
		loading = true;
		trForward.translation.set(0, 0, 8f);
		trBackward.translation.set(0, 0, -8f);
		trLeft.rotation.setFromAxis(Vector3.Y, 90);
		trRight.rotation.setFromAxis(Vector3.Y, -90);

		ModelBuilder builder = new ModelBuilder();
		builder.begin();
		builder.node().id = "floor";
		MeshPartBuilder part = builder.part("floor", GL20.GL_TRIANGLES, Usage.Position | Usage.TextureCoordinates | Usage.Normal,
			new Material("concrete"));
		((MeshBuilder)part).ensureRectangles(1600);
		for (float x = -200f; x < 200f; x += 10f) {
			for (float z = -200f; z < 200f; z += 10f) {
				part.rect(x, 0, z + 10f, x + 10f, 0, z + 10f, x + 10f, 0, z, x, 0, z, 0, 1, 0);
			}
		}
		builder.node().id = "tree";
		part = builder.part("tree", GL20.GL_TRIANGLES, Usage.Position | Usage.TextureCoordinates | Usage.Normal,
			new Material("tree"));
		part.rect( 0f, 0f, -10f, 10f, 0f, -10f, 10f, 10f, -10f,  0f, 10f, -10f, 0, 0, 1f);
		part.setUVRange(1, 0, 0, 1);
		part.rect(10f, 0f, -10f,  0f, 0f, -10f,  0f, 10f, -10f, 10f, 10f, -10f, 0, 0, -1f);
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
			if (Gdx.input.isKeyPressed(Keys.UP)) {
				if (!animation.inAction) {
					trTmp.idt().lerp(trForward, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
					character.transform.mul(trTmp.toMatrix4(tmpMatrix));
				}
				if (status != walk) {
					animation.animate("Walk", -1, 1f, null, 0.2f);
					status = walk;
				}
			} else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
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
			if (Gdx.input.isKeyPressed(Keys.RIGHT) && (status == walk || status == back) && !animation.inAction) {
				trTmp.idt().lerp(trRight, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
				character.transform.mul(trTmp.toMatrix4(tmpMatrix));
			} else if (Gdx.input.isKeyPressed(Keys.LEFT) && (status == walk || status == back) && !animation.inAction) {
				trTmp.idt().lerp(trLeft, Gdx.graphics.getDeltaTime() / animation.current.animation.duration);
				character.transform.mul(trTmp.toMatrix4(tmpMatrix));
			}
			if (Gdx.input.isKeyPressed(Keys.SPACE) && !animation.inAction) {
				animation.action("Attack", 1, 1f, null, 0.2f);
			}
			if (Gdx.input.isKeyJustPressed(Keys.Z))
				ship.parts.get(0).enabled = !ship.parts.get(0).enabled; 
		}

		if (character != null) {
			shadowLight.begin(character.transform.getTranslation(tmpVector), cam.direction);
			shadowBatch.begin(shadowLight.getCamera());
			if (character != null) shadowBatch.render(character);
			if (tree != null) shadowBatch.render(tree);
			shadowBatch.end();
			shadowLight.end();
		}
		super.render();
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		batch.render(instances, lights);
		if (skydome != null) batch.render(skydome);
	}

	@Override
	protected void getStatus (StringBuilder stringBuilder) {
		super.getStatus(stringBuilder);
		stringBuilder.append(" use arrow keys to walk around, space to attack, Z to toggle attached node.");
	}

	@Override
	protected void onModelClicked (final String name) {
	}

	@Override
	protected void onLoaded () {
		if (skydome == null) {
			skydome = new ModelInstance(assets.get("data/g3d/skydome.g3db", Model.class));
			floorModel.getMaterial("concrete").set(TextureAttribute.createDiffuse(assets.get("data/g3d/concrete.png", Texture.class)));
			floorModel.getMaterial("tree").set(
				TextureAttribute.createDiffuse(assets.get("data/tree.png", Texture.class)),
				new BlendingAttribute()
				);
			instances.add(new ModelInstance(floorModel, "floor"));
			instances.add(tree = new ModelInstance(floorModel, "tree"));
			assets.load("data/g3d/knight.g3db", Model.class);
			loading = true;
		} else if (character == null) {
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
			// Now attach the node of another model at the tip of this knights sword:
			ship = assets.get("data/g3d/ship.obj", Model.class).nodes.get(0).copy();
			ship.detach();
			ship.translation.x = 10f; // offset from the sword node to the tip of the sword, in rest pose
			ship.rotation.set(Vector3.Z, 90f);
			ship.scale.scl(5f);
			ship.parts.get(0).enabled = false;
			character.getNode("sword").addChild(ship);
		}
	}

	@Override
	public void dispose () {
		super.dispose();
		floorModel.dispose();
		shadowLight.dispose();
	}
}
