package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.StringBuilder;

public abstract class BaseG3dHudTest extends BaseG3dTest {
	protected Stage hud;
	protected Skin skin;
	protected Label fpsLabel;
	protected CollapsableWindow modelsWindow;
	protected final StringBuilder stringBuilder = new StringBuilder();

	protected  String models[] = new String[] {
		"car.obj", "cube.obj", "scene.obj", "scene2.obj", "sphere.obj", "wheel.obj", 
		"g3d/cube_anim.g3dj", "g3d/cubes.g3dj", "g3d/head_parented.g3dj", "g3d/head.g3dj", "g3d/head2.g3dj", "g3d/teapot.g3db", "g3d/test.g3dj"
	};

	@Override
	public void create () {
		super.create();

		createHUD();

		Gdx.input.setInputProcessor(new InputMultiplexer(this, hud, inputController));
	}
	
	private void createHUD() {
		hud = new Stage();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		modelsWindow = new CollapsableWindow("Models", skin);
		final List list = new List(models, skin);
		list.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!modelsWindow.isCollapsed() && getTapCount() == 2) {
					onModelClicked(list.getSelection());
					modelsWindow.collapse();
				}
			}
		});
		modelsWindow.row();
		modelsWindow.add(list);
		modelsWindow.pack();
		modelsWindow.pack();
		modelsWindow.setY(Gdx.graphics.getHeight()-modelsWindow.getHeight());
		modelsWindow.collapse();
		hud.addActor(modelsWindow);
		fpsLabel = new Label("FPS: 999", skin);
		hud.addActor(fpsLabel);
	}

	protected abstract void onModelClicked(final String name);
	
	protected void getStatus(final StringBuilder stringBuilder) {
		stringBuilder.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
	}
	
	@Override
	public void render () {
		super.render();
		
		stringBuilder.setLength(0);
		getStatus(stringBuilder);
		fpsLabel.setText(stringBuilder);
		hud.act(Gdx.graphics.getDeltaTime());
		hud.draw();
	}
	
	@Override
	public void dispose () {
		super.dispose();
		skin.dispose();
		skin = null;
	}
	
	/** Double click title to expand/collapse */
	public static class CollapsableWindow extends Window {
		private boolean collapsed;
		private float collapseHeight = 20f;
		private float expandHeight;
		public CollapsableWindow (String title, Skin skin) {
			super(title, skin);
			addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					if (getTapCount() == 2 && getHeight() - y <= getPadTop() && y < getHeight() && x > 0 && x < getWidth())
						toggleCollapsed();
				}
			});
		}
		public void expand() {
			if (!collapsed) return;
			setHeight(expandHeight);
			setY(getY()-expandHeight+collapseHeight);
			collapsed = false;
		}
		public void collapse() {
			if (collapsed) return;
			expandHeight = getHeight();
			setHeight(collapseHeight);
			setY(getY()+expandHeight-collapseHeight);
			collapsed = true;
		}
		public void toggleCollapsed() {
			if (collapsed)
				expand();
			else
				collapse();
		}
		public boolean isCollapsed() {
			return collapsed;
		}
	}
}
