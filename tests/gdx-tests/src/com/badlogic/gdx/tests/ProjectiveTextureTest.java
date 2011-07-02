package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderOld;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.ComboBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.PerspectiveCamController;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ProjectiveTextureTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	PerspectiveCamera cam;
	PerspectiveCamera projector;
	Texture texture;
	Mesh plane;
	Mesh cube;
	Matrix4 planeTrans = new Matrix4();
	Matrix4 cubeTrans = new Matrix4();	
	Matrix4 modelNormal = new Matrix4();
	ShaderProgram projTexShader;
	Stage ui;
	InputMultiplexer multiplexer = new InputMultiplexer();
	PerspectiveCamController controller;
	ImmediateModeRenderer20 renderer;
	
	float angle = 0;
	
	@Override public void create() {
		setupScene();
		setupUI();
		setupShaders();
		
		multiplexer.addProcessor(ui);
		multiplexer.addProcessor(controller);
		Gdx.input.setInputProcessor(multiplexer);
		
		renderer = new ImmediateModeRenderer20(false, true, 0);
	}
	
	public void setupScene() {
		plane = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 3, "a_Position"),
											  new VertexAttribute(Usage.Normal, 3, "a_Normal"));
		plane.setVertices(new float[] { -10, -1, 10, 0, 1, 0, 
												   10, -1, 10, 0, 1, 0, 
												   10, -1, -10, 0, 1, 0,
												   -10, -1, -10, 0, 1, 0 });
		plane.setIndices(new short[] { 3, 2, 1, 1, 0, 3 });
		cube = ModelLoaderOld.loadObj(Gdx.files.internal("data/cube.obj").read());		
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 5, 10);
		cam.lookAt(0,  0, 0);
		cam.update();
		controller = new PerspectiveCamController(cam);		
					
		projector = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		projector.position.set(2, 3, 2);
		projector.lookAt(0,  0, 0);
		projector.normalizeUp();			
		projector.update();
	}
	
	public void setupUI() {
		ui = new Stage(480, 320, false);
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.xml"), Gdx.files.internal("data/uiskin.png"));
		Button reload = skin.newButton("reload", "Reload Shaders");
		ComboBox camera = skin.newComboBox("camera", new String[] { "Camera", "Light" }, ui);
		Label fps = skin.newLabel("fps", "fps: ");
		
		Container container = new Container("container", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		container.add(reload).spacingRight(5);
		container.add(camera).spacingRight(5);
		container.add(fps);
		ui.addActor(container);
		
		reload.setClickListener(new ClickListener() {			
			@Override public void click (Button button) {
				ShaderProgram prog = new ShaderProgram(Gdx.files.internal("data/shaders/projtex-vert.glsl").readString(), Gdx.files.internal("data/shaders/projtex-frag.glsl").readString());
				if(prog.isCompiled() == false) {
					Gdx.app.log("GLSL ERROR", "Couldn't reload shaders:\n" + prog.getLog());
				} else {
					projTexShader.dispose();
					projTexShader = prog;
				}
			}
		});			
	}
	
	public void setupShaders() {
		ShaderProgram.pedantic = false;
		projTexShader = new ShaderProgram(Gdx.files.internal("data/shaders/projtex-vert.glsl").readString(), Gdx.files.internal("data/shaders/projtex-frag.glsl").readString());
		if(!projTexShader.isCompiled()) throw new GdxRuntimeException("Couldn't compile shader: " + projTexShader.getLog());
	}
	
	@Override public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		
		angle += Gdx.graphics.getDeltaTime() * 20.0f;
		cubeTrans.setToRotation(Vector3.Y, angle);		
		
		cam.update();
		projector.update();
		
		texture.bind();
		projTexShader.begin();
		
		if(((ComboBox)ui.findActor("camera")).getSelectionIndex() == 0) {
			renderMesh(projTexShader, cam.combined, projector.combined, planeTrans, plane, Color.WHITE);
			renderMesh(projTexShader, cam.combined, projector.combined, cubeTrans, cube, Color.WHITE);
		} else {
			renderMesh(projTexShader, projector.combined, projector.combined, planeTrans, plane, Color.WHITE);
			renderMesh(projTexShader, projector.combined, projector.combined, cubeTrans, cube, Color.WHITE);
		}
		
		projTexShader.end();			
		
		Label label = (Label)ui.findActor("fps");
		label.setText("fps: " + Gdx.graphics.getFramesPerSecond());
		ui.draw();
	}
	
	Vector3 position = new Vector3();	
	private void renderMesh(ShaderProgram shader, Matrix4 cam, Matrix4 projector, Matrix4 model, Mesh mesh, Color color) {
		position.set(this.projector.position);
		modelNormal.set(model).toNormalMatrix();
		
		shader.setUniformMatrix("u_camera", cam);
		shader.setUniformMatrix("u_projector", projector);
		shader.setUniformf("u_projectorPos", position.x, position.y, position.z);
		shader.setUniformMatrix("u_model", model);
		shader.setUniformMatrix("u_modelNormal", modelNormal);
		shader.setUniformf("u_color", color.r, color.g, color.b);
		shader.setUniformi("u_texture", 0);
		mesh.render(shader, GL20.GL_TRIANGLES);
	}
}
