package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.loaders.collada.ColladaLoader;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class StillModelViewer implements ApplicationListener {

	PerspectiveCamera cam;
	StillModel model;
	Texture texture = null;
	boolean hasNormals = false;
	BoundingBox bounds = new BoundingBox();
	ImmediateModeRenderer renderer;
	float angle = 0;
	String fileName;
	String textureFileName;
	
	public StillModelViewer(String fileName, String textureFileName) {
		this.fileName = fileName;
		this.textureFileName = textureFileName;
	}
	
	@Override public void create () {
		if(fileName.endsWith(".dae")) model = ColladaLoader.loadStillModel(Gdx.files.internal(fileName));
		else throw new GdxRuntimeException("Unknown file format '" + fileName + "'");		
		if(textureFileName != null) texture = new Texture(Gdx.files.internal(textureFileName));		
		hasNormals = hasNormals();
		
		model.getBoundingBox(bounds);
		float len = bounds.getDimensions().len();
		System.out.println("bounds: " + bounds);	
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(bounds.getCenter().cpy().add(len*2, len*2, len*2));
		cam.lookAt(bounds.getCenter().x, bounds.getCenter().y, bounds.getCenter().z);
		cam.near = 0.1f;
		cam.far = 1000;
		
		renderer = new ImmediateModeRenderer();
	}
	
	private boolean hasNormals() {
		for(StillSubMesh mesh: model.subMeshes) {
			if(mesh.mesh.getVertexAttribute(Usage.Normal) == null) return false;
		}
		return true;
	}

	@Override public void resume () {
		
	}

	float[] lightColor = {1, 1, 1, 0};
	float[] lightPosition = {2, 5, 10, 0};
	@Override public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);		
		
		cam.update();
		cam.apply(Gdx.gl10);		
		
		drawAxes();
		
		if(hasNormals) {
			Gdx.gl.glEnable(GL10.GL_LIGHTING);
			Gdx.gl.glEnable(GL10.GL_COLOR_MATERIAL);
			Gdx.gl.glEnable(GL10.GL_LIGHT0);
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightColor, 0);
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);			
		}
		
		if(texture != null) {
			Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			texture.bind();
		}
		
		angle += 45 * Gdx.graphics.getDeltaTime();
		Gdx.gl10.glRotatef(angle, 0, 1, 0);
		model.render();
		
		if(texture != null) {
			Gdx.gl.glDisable(GL10.GL_TEXTURE_2D);
		}
		
		if(hasNormals) {
			Gdx.gl.glDisable(GL10.GL_LIGHTING);
		}
	}
	
	private void drawAxes() {
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

	@Override public void resize (int width, int height) {
		
	}

	@Override public void pause () {
		
	}

	@Override public void dispose () {		
	}	
	
	public static void main(String[] argv) {
		if(argv.length != 1 && argv.length != 2) {
			System.out.println("StillModelViewer <filename> ?<texture-filename>");
			System.exit(-1);
		}
		new JoglApplication(new StillModelViewer(argv[0], argv.length==2?argv[1]:null), "StillModel Viewer", 800, 480, false);
	}
}
