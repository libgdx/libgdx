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

package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.OgreXmlLoader;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonAnimation;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonModel;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonSubMesh;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SkeletonModelViewer implements ApplicationListener {
	PerspectiveCamera cam;
	SkeletonModel model;
	Texture texture = null;
	boolean hasNormals = false;
	BoundingBox bounds = new BoundingBox();
	ImmediateModeRenderer10 renderer;
	float angle = 0;
	String fileName;
	String textureFileName;
	SkeletonAnimation anim;
	float animTime = 0;
	SpriteBatch batch;
	BitmapFont font;

	public SkeletonModelViewer (String fileName, String textureFileName) {
		this.fileName = fileName;
		this.textureFileName = textureFileName;
	}

	@Override
	public void create () {
		if (fileName.endsWith(".xml")) {
			model = new OgreXmlLoader().load(Gdx.files.internal(fileName),
				Gdx.files.internal(fileName.replace("mesh.xml", "skeleton.xml")));
		} else
			throw new GdxRuntimeException("Unknown file format '" + fileName + "'");
		if (textureFileName != null) texture = new Texture(Gdx.files.internal(textureFileName));
		hasNormals = hasNormals();
		Material material = new Material("material", new TextureAttribute(texture, 0, "s_tex"));
		model.setMaterial(material);
		anim = (SkeletonAnimation)model.getAnimations()[0];

		model.getBoundingBox(bounds);
		float len = bounds.getDimensions().len();
		System.out.println("bounds: " + bounds);

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(bounds.getCenter().cpy().add(len, len, len));
		cam.lookAt(bounds.getCenter().x, bounds.getCenter().y, bounds.getCenter().z);
		cam.near = 1f;
		cam.far = 1000;

		renderer = new ImmediateModeRenderer10();
		batch = new SpriteBatch();
		font = new BitmapFont();
	}

	private boolean hasNormals () {
		for (SkeletonSubMesh mesh : model.subMeshes) {
			if (mesh.mesh.getVertexAttribute(Usage.Normal) == null) return false;
		}
		return true;
	}

	@Override
	public void resume () {

	}

	float[] lightColor = {1, 1, 1, 0};
	float[] lightPosition = {2, 5, 10, 0};

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);

		cam.update();
		cam.apply(Gdx.gl10);

		drawAxes();

		if (hasNormals) {
			Gdx.gl.glEnable(GL10.GL_LIGHTING);
			Gdx.gl.glEnable(GL10.GL_COLOR_MATERIAL);
			Gdx.gl.glEnable(GL10.GL_LIGHT0);
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightColor, 0);
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);
		}

		if (texture != null) {
			Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		}

		angle += 45 * Gdx.graphics.getDeltaTime();
		Gdx.gl10.glRotatef(angle, 0, 1, 0);
		animTime += Gdx.graphics.getDeltaTime() / 10;
		if (animTime > anim.totalDuration) {
			animTime = 0;
		}
		model.setAnimation(anim.name, animTime, false);
		model.render();

		if (texture != null) {
			Gdx.gl.glDisable(GL10.GL_TEXTURE_2D);
		}

		if (hasNormals) {
			Gdx.gl.glDisable(GL10.GL_LIGHTING);
		}

		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 20, 30);
		batch.end();
	}

	private void drawAxes () {
		float len = bounds.getDimensions().len();
		renderer.begin(GL10.GL_LINES);
		renderer.color(1, 0, 0, 1);
		renderer.vertex(0, 0, 0);
		renderer.color(1, 0, 0, 1);
		renderer.vertex(len, 0, 0);
		renderer.color(0, 1, 0, 1);
		renderer.vertex(0, 0, 0);
		renderer.color(0, 1, 0, 1);
		renderer.vertex(0, len, 0);
		renderer.color(0, 0, 1, 1);
		renderer.vertex(0, 0, 0);
		renderer.color(0, 0, 1, 1);
		renderer.vertex(0, 0, len);
		renderer.end();
		Gdx.gl10.glColor4f(1, 1, 1, 1);
	}

	@Override
	public void resize (int width, int height) {

	}

	@Override
	public void pause () {

	}

	@Override
	public void dispose () {
	}

	public static void main (String[] argv) {
// if(argv.length != 1 && argv.length != 2) {
// System.out.println("KeyframedModelViewer <filename> ?<texture-filename>");
// System.exit(-1);
// }
		new LwjglApplication(new SkeletonModelViewer("data/models/ninja.mesh.xml", "data/models/ninja.jpg"), "SkeletonModel Viewer", 800, 480,
			false);
	}
}
