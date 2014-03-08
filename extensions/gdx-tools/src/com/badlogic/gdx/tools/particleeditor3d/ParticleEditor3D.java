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
package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader.BitmapFontParameter;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.controllers.BillboardParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ModelInstanceRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.Renderer;
import com.badlogic.gdx.graphics.g3d.particles.values.GradientColorValue;
import com.badlogic.gdx.graphics.g3d.particles.values.NumericValue;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ParticleEditor3D extends JFrame implements AssetErrorListener {
	
	public interface ResourceLoadListener<T>{
		public void onLoad(String resourcePath, T resource);
	}
	
	public static final String 	DEFAULT_FONT = "default.fnt",
											DEFAULT_BILLBOARD_PARTICLE = "particle.png",
											DEFAULT_MODEL_PARTICLE = "monkey.g3db"; 
	LwjglCanvas lwjglCanvas;
	JPanel rowsPanel;
	JPanel editRowsPanel;
	EffectPanel effectPanel;
	private JSplitPane splitPane;
	public PerspectiveCamera worldCamera;
	OrthographicCamera textCamera;
	NumericValue fovValue;
	NumericValue deltaMultiplier;
	GradientColorValue backgroundColor;
	AppRenderer renderer;
	AssetManager assetManager;

	ParticleEffect effect;
	final HashMap<ParticleController, ParticleData> particleData = new HashMap();

	public ParticleEditor3D () {
		super("Particle Editor 3D");

		assetManager = new AssetManager();
		assetManager.setErrorListener(this);
		effect = new ParticleEffect();
		lwjglCanvas = new LwjglCanvas(renderer = new AppRenderer(), true);
		addWindowListener(new WindowAdapter() {
			public void windowClosed (WindowEvent event) {
				//System.exit(0);
				Gdx.app.exit();
			}
		});

		initializeComponents();

		setSize(1280, 950);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
		
	}

	void reloadRows () {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				editRowsPanel.removeAll();
				addEditorRow(new NumericPanel(ParticleEditor3D.this, fovValue, "Field of View", ""));
				addEditorRow(new NumericPanel(ParticleEditor3D.this, deltaMultiplier, "Delta multiplier", ""));
				addEditorRow(new GradientPanel(ParticleEditor3D.this,backgroundColor, "Background color", "", true));
				addEditorRow(new DrawPanel(ParticleEditor3D.this, "Draw", ""));
				editRowsPanel.repaint();

				rowsPanel.removeAll();
				ParticleController<Particle> controller = getEmitter();  
				if(controller != null){
					addRow(getPanel(controller.renderer));
					addRow(getPanel(controller.emitter));
					for(Influencer influencer : controller.influencers){
						JPanel panel = getPanel(influencer);
						if(panel != null)
							addRow(panel);
					}
					for (Component component : rowsPanel.getComponents())
						if (component instanceof EditorPanel) 
							((EditorPanel)component).update(ParticleEditor3D.this);
				}
				rowsPanel.repaint();
			}
		});
	}

	protected JPanel getPanel (Emitter emitter) {
		JPanel panel = null;
		if(emitter instanceof RegularEmitter){
			panel = new RegularEmitterPanel(this, (RegularEmitter) emitter);
		}
		return panel;
	}

	protected JPanel getPanel (Influencer influencer) {
		JPanel panel = null;
		if(influencer instanceof RegionInfluencer){
			panel = new RegionInfluencerPanel(this, (RegionInfluencer)influencer);
		}
		else if(influencer instanceof ColorInfluencer){
			panel = new ColorInfluencerPanel(this, (ColorInfluencer) influencer);
		}
		else if(influencer instanceof ScaleInfluencer){
			panel =  new ScaledNumericPanel(this, ((ScaleInfluencer)influencer).scaleValue, "Life", "Scale Influencer", "Particle scale, in world units.");
		}
		else if(influencer instanceof SpawnShapeInfluencer){
			panel =  new SpawnInfluencerPanel(this, ((SpawnShapeInfluencer)influencer).spawnShapeValue);
		}
		else if(influencer instanceof VelocityInfluencer){
			panel =  new VelocityInfluencerPanel(this, (VelocityInfluencer)influencer);
		}
		
		return panel;
	}

	protected JPanel getPanel (Renderer renderer) {
		JPanel panel = null;
		if(renderer instanceof BillboardRenderer){
			panel = new BillboardRenderPanel(this, (BillboardRenderer) renderer);
		}
		if(renderer instanceof ModelInstanceRenderer){
			panel = new EmptyPanel(this, "Model Instance Renderer", "It renders particles as model instances.");
		}
		
		return panel;
	}

	void addRow(JPanel panel, JPanel row) {
		row.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, java.awt.Color.black));
		panel.add(row, new GridBagConstraints(0, -1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(0, 0, 0, 0), 0, 0));
	}
	
	void addEditorRow (JPanel row) {
		addRow(editRowsPanel, row);
	}

	void addRow (JPanel row) {
		addRow(rowsPanel, row);
	}

	public void setVisible (String name, boolean visible) {
		for (Component component : rowsPanel.getComponents())
			if (component instanceof EditorPanel && ((EditorPanel)component).getName().equals(name)) component.setVisible(visible);
	}

	public ParticleController getEmitter () {
		return effectPanel.editIndex >=0 ? effect.getControllers().get(effectPanel.editIndex) : null;
	}

	/*
	public ImageIcon getIcon (ParticleController emitter) {
		ParticleData data = particleData.get(emitter);
		if (data == null) particleData.put(emitter, data = new ParticleData());
		if (data.icon == null && data.imagePath != null) {
			try {
				URL url;
				File file = new File(data.imagePath);
				if (file.exists())
					url = file.toURI().toURL();
				else {
					url = ParticleEditor3D.class.getResource(data.imagePath);
					if (url == null) return null;
				}
				data.icon = new ImageIcon(url);
			} catch (MalformedURLException ex) {
				ex.printStackTrace();
			}
		}
		return data.icon;
	}

	public void setIcon (ParticleController emitters, ImageIcon icon) {
		ParticleData data = particleData.get(emitters);
		if (data == null) particleData.put(emitters, data = new ParticleData());
		data.icon = icon;
	}
	*/
        
	public void setEnabled (ParticleController emitter, boolean enabled) {
		ParticleData data = particleData.get(emitter);
		if (data == null) particleData.put(emitter, data = new ParticleData());
		data.enabled = enabled;
		//emitter.start();
	}

	public boolean isEnabled (ParticleController emitter) {
		ParticleData data = particleData.get(emitter);
		if (data == null) return true;
		return data.enabled;
	}

	private void initializeComponents () {
		splitPane = new JSplitPane();
		splitPane.setUI(new BasicSplitPaneUI() {
			public void paint (Graphics g, JComponent jc) {
			}
		});
		splitPane.setDividerSize(4);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		{
			JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			rightSplit.setUI(new BasicSplitPaneUI() {
				public void paint (Graphics g, JComponent jc) {
				}
			});
			rightSplit.setDividerSize(4);
			splitPane.add(rightSplit, JSplitPane.RIGHT);

			{
				JPanel propertiesPanel = new JPanel(new GridBagLayout());
				rightSplit.add(propertiesPanel, JSplitPane.TOP);
				propertiesPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(3, 0, 6, 6), BorderFactory
					.createTitledBorder("Editor Properties")));
				{
					JScrollPane scroll = new JScrollPane();
					propertiesPanel.add(scroll, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
					{
						editRowsPanel = new JPanel(new GridBagLayout());
						scroll.setViewportView(editRowsPanel);
						scroll.getVerticalScrollBar().setUnitIncrement(70);
					}
				}
			}

			{
				JPanel propertiesPanel = new JPanel(new GridBagLayout());
				rightSplit.add(propertiesPanel, JSplitPane.BOTTOM);
				propertiesPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(3, 0, 6, 6), BorderFactory
					.createTitledBorder("Emitter Properties")));
				{
					JScrollPane scroll = new JScrollPane();
					propertiesPanel.add(scroll, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
					{
						rowsPanel = new JPanel(new GridBagLayout());
						scroll.setViewportView(rowsPanel);
						scroll.getVerticalScrollBar().setUnitIncrement(70);
					}
				}
			}
			rightSplit.setDividerLocation(200);

		}
		{
			JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			leftSplit.setUI(new BasicSplitPaneUI() {
				public void paint (Graphics g, JComponent jc) {
				}
			});
			leftSplit.setDividerSize(4);
			splitPane.add(leftSplit, JSplitPane.LEFT);
			{
				JPanel spacer = new JPanel(new BorderLayout());
				leftSplit.add(spacer, JSplitPane.TOP);
				spacer.add(lwjglCanvas.getCanvas());
				spacer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
			}
			{
				JPanel emittersPanel = new JPanel(new BorderLayout());
				leftSplit.add(emittersPanel, JSplitPane.BOTTOM);
				emittersPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 6, 6, 0), BorderFactory
					.createTitledBorder("Effect Emitters")));
				{
					effectPanel = new EffectPanel(this);
					emittersPanel.add(effectPanel);
				}
			}
			leftSplit.setDividerLocation(625);
		}
		splitPane.setDividerLocation(500);
	}

	class AppRenderer implements ApplicationListener, InputProcessor {
		private float maxActiveTimer;
		private int maxActive, lastMaxActive;
		private int activeCount;
		private BitmapFont font;
		private SpriteBatch spriteBatch;
		private ModelBatch modelBatch;
		private CameraInputController cameraInputController;
		private boolean isDrawXYZ, isDrawXZPlane;
		private Array<Model> models;
		private ModelInstance xyzInstance, xzPlaneInstance;
		private Environment environment;

		public void create () {
			if (spriteBatch != null) return;

			Texture.setEnforcePotImages(false);

			spriteBatch = new SpriteBatch();
			spriteBatch.enableBlending();
			modelBatch = new ModelBatch();
			environment = new Environment();
			environment.add(new DirectionalLight().set(Color.WHITE, 0,0,-1));
			
			worldCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			worldCamera.position.set(10, 10, 10);
			worldCamera.lookAt(0,0,0);
			worldCamera.near = 0.1f;
			worldCamera.far = 300f;
			worldCamera.update();	
			textCamera = new OrthographicCamera();

			cameraInputController = new CameraInputController(worldCamera);

			fovValue = new NumericValue();
			fovValue.setValue(67);
			fovValue.setActive(true);

			deltaMultiplier = new NumericValue();
			deltaMultiplier.setValue(1.0f);
			deltaMultiplier.setActive(true);

			backgroundColor = new GradientColorValue();
			backgroundColor.setColors(new float[] { 0f, 0f, 0f});

			models = new Array<Model>();
			ModelBuilder builder = new ModelBuilder();
			Model 	xyzModel = builder.createXYZCoordinates(10),
				planeModel = builder.createLineGrid(10, 10, 1, 1, Color.WHITE);
			models.add(xyzModel);
			models.add(planeModel);
			xyzInstance = new ModelInstance(xyzModel);
			xzPlaneInstance = new ModelInstance(planeModel);

			setDrawXYZ(true);
			setDrawXZPlane(true);
			Gdx.input.setInputProcessor(new InputMultiplexer(cameraInputController));


			//Load default resources
			BitmapFontParameter fontParams = new BitmapFontLoader.BitmapFontParameter();
			fontParams.flip = true;
			assetManager.load(DEFAULT_FONT,  BitmapFont.class, fontParams);
			assetManager.load(DEFAULT_BILLBOARD_PARTICLE, Texture.class);
			assetManager.load(DEFAULT_MODEL_PARTICLE, Model.class);
			assetManager.finishLoading();
			font = assetManager.get(DEFAULT_FONT);

			effectPanel.createDefaultEmitter(BillboardParticleController.class, true);
		}


		@Override
		public void resize (int width, int height) {
			Gdx.gl.glViewport(0, 0, width, height);

			worldCamera.viewportWidth = width;
			worldCamera.viewportHeight = height;
			worldCamera.update();

			textCamera.setToOrtho(true, width, height);
			textCamera.update();
		}

		public void render () {
			cameraInputController.update();

			float delta = Math.max(0, Gdx.graphics.getDeltaTime() * deltaMultiplier.getValue());

			float[] colors = backgroundColor.getColors();
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			Gdx.gl.glClearColor(colors[0], colors[1], colors[2], 0);

			worldCamera.fieldOfView = fovValue.getValue();
			worldCamera.update();

			modelBatch.begin(worldCamera);
			if(isDrawXYZ) modelBatch.render(xyzInstance);
			if(isDrawXZPlane) modelBatch.render(xzPlaneInstance);

			activeCount = 0;
			for (ParticleController controller : effect.getControllers()) 
			{
				if (isEnabled(controller)) {	
					controller.update(delta);
					modelBatch.render(controller, environment);
					activeCount += controller.emitter.activeCount;
				}
			}

			maxActive = Math.max(maxActive, activeCount);
			maxActiveTimer += delta;
			if (maxActiveTimer > 3) {
				maxActiveTimer = 0;
				lastMaxActive = maxActive;
				maxActive = 0;
			}

			modelBatch.end();

			spriteBatch.begin();
			spriteBatch.setProjectionMatrix(textCamera.combined);

			font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 5, 15);
			font.draw(spriteBatch, "Count: " + activeCount, 5, 35);
			font.draw(spriteBatch, "Max: " + lastMaxActive, 5, 55);
			//font.draw(spriteBatch, (int)(getEmitter().getPercentComplete() * 100) + "%", 5, 75);

			spriteBatch.end();
		}
		
		public boolean keyDown (int keycode) {
			return false;
		}

		public boolean keyUp (int keycode) {
			return false;
		}

		public boolean keyTyped (char character) {
			return false;
		}

		public boolean touchDown (int x, int y, int pointer, int newParam) {
			return false;
		}

		public boolean touchUp (int x, int y, int pointer, int button) {
			ParticleEditor3D.this.dispatchEvent(new WindowEvent(ParticleEditor3D.this, WindowEvent.WINDOW_LOST_FOCUS));
			ParticleEditor3D.this.dispatchEvent(new WindowEvent(ParticleEditor3D.this, WindowEvent.WINDOW_GAINED_FOCUS));
			ParticleEditor3D.this.requestFocusInWindow();
			return false;
		}

		public boolean touchDragged (int x, int y, int pointer) {
			return false;
		}

		@Override
		public void dispose () 
		{
			//for(Model model : mModels) model.dispose();
		}

		@Override
		public void pause () {
		}

		@Override
		public void resume () {
		}

		@Override
		public boolean mouseMoved (int x, int y) {
			return false;
		}

		@Override
		public boolean scrolled (int amount) {
			return false;
		}

		public void setDrawXYZ(boolean isDraw) 
		{
			isDrawXYZ = isDraw;
		}

		public boolean IsDrawXYZ() 
		{
			return isDrawXYZ;
		}

		public void setDrawXZPlane(boolean isDraw) 
		{
			isDrawXZPlane = isDraw;
		}

		public boolean IsDrawXZPlane() 
		{
			return isDrawXZPlane;
		}
	}

	static class ParticleData {
		public ImageIcon icon;
		public String imagePath;
		public boolean enabled = true;
	}

	public static void main (String[] args) {
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (Throwable ignored) {
				}
				break;
			}
		}
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				new ParticleEditor3D();
			}
		});
	}

	public AppRenderer getRenderer() {
		return renderer;
	}

	public <T> T load (String resource, Class<T> type, AssetLoader loader) {	
		AssetLoader<T, AssetLoaderParameters<T>> currentLoader = assetManager.getLoader(type);
		assetManager.setLoader(type, loader);
		assetManager.load(resource, type);
		assetManager.finishLoading();
		String resolvedPath = new String(resource).replaceAll("\\\\", "/");;
		T res = assetManager.get(resolvedPath);
		if(currentLoader != null)
			assetManager.setLoader(type, currentLoader);
		return res;
	}

	/*
	public <T> void load (final String resource, final Class<T> type, final AssetLoader loader, final ResourceLoadListener<T> listener) {
		Gdx.app.postRunnable(new Runnable(){
			@Override
			public void run () {				
				AssetLoader<T, AssetLoaderParameters<T>> currentLoader = assetManager.getLoader(type);
				assetManager.setLoader(type, loader);
				assetManager.load(resource, type);
				assetManager.finishLoading();
				String resolvedPath = new String(resource).replaceAll("\\\\", "/");;
				T res = assetManager.get(resolvedPath);
				listener.onLoad(resource, res);
				assetManager.setLoader(type, currentLoader);
			}
		});
	}
	*/
	
	@Override
	public void error (AssetDescriptor asset, Throwable throwable) {
		throwable.printStackTrace();
	}

}
