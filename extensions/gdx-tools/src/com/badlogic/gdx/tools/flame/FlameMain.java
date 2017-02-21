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
package com.badlogic.gdx.tools.flame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.Writer;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader.ParticleEffectSaveParameter;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ModelInstanceParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ModelInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerFinalizerInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ModelInstanceRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerControllerRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointSpriteRenderer;
import com.badlogic.gdx.graphics.g3d.particles.values.GradientColorValue;
import com.badlogic.gdx.graphics.g3d.particles.values.NumericValue;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.StringBuilder;

/** @author Inferno */
public class FlameMain extends JFrame implements AssetErrorListener {
	public static final String 	DEFAULT_FONT = "default.fnt",
											DEFAULT_BILLBOARD_PARTICLE = "pre_particle.png",
											DEFAULT_MODEL_PARTICLE = "monkey.g3db",
											//DEFAULT_PFX = "default.pfx",
											DEFAULT_TEMPLATE_PFX = "defaultTemplate.pfx",
											DEFAULT_SKIN = "uiskin.json";
	
	public static final int EVT_ASSET_RELOADED = 0;
	
	static class ControllerData {
		public boolean enabled = true;
		public ParticleController controller;
		public ControllerData (ParticleController emitter) {
			controller = emitter;
		}
	}
	
	private static class InfluencerWrapper<T>{
		String string;
		Class<Influencer> type;
		public InfluencerWrapper(String string, Class<Influencer> type){
			this.string = string;
			this.type = type;
		}
		
		@Override
		public String toString () {
			return string;
		}
	}

	public enum ControllerType{
		Billboard( "Billboard", new InfluencerWrapper[]{
			new InfluencerWrapper("Single Color", ColorInfluencer.Single.class),
			new InfluencerWrapper("Random Color", ColorInfluencer.Random.class),
			new InfluencerWrapper("Single Region", RegionInfluencer.Single.class),
			new InfluencerWrapper("Random Region", RegionInfluencer.Random.class),
			new InfluencerWrapper("Animated Region", RegionInfluencer.Animated.class),
			new InfluencerWrapper("Scale", ScaleInfluencer.class),
			new InfluencerWrapper("Spawn", SpawnInfluencer.class),
			new InfluencerWrapper("Dynamics", DynamicsInfluencer.class)}), 
		PointSprite("Point Sprite", new InfluencerWrapper[]{
				new InfluencerWrapper("Single Color", ColorInfluencer.Single.class),
				new InfluencerWrapper("Random Color", ColorInfluencer.Random.class),
				new InfluencerWrapper("Single Region", RegionInfluencer.Single.class),
				new InfluencerWrapper("Random Region", RegionInfluencer.Random.class),
				new InfluencerWrapper("Animated Region", RegionInfluencer.Animated.class),
				new InfluencerWrapper("Scale", ScaleInfluencer.class),
				new InfluencerWrapper("Spawn", SpawnInfluencer.class),
				new InfluencerWrapper("Dynamics", DynamicsInfluencer.class)}),
		ModelInstance( "Model Instance", new InfluencerWrapper[]{
					new InfluencerWrapper("Single Color", ColorInfluencer.Single.class),
					new InfluencerWrapper("Random Color", ColorInfluencer.Random.class),
					new InfluencerWrapper("Single Model", ModelInfluencer.Single.class),
					new InfluencerWrapper("Random Model", ModelInfluencer.Random.class),
					new InfluencerWrapper("Scale", ScaleInfluencer.class),
					new InfluencerWrapper("Spawn", SpawnInfluencer.class),
					new InfluencerWrapper("Dynamics", DynamicsInfluencer.class)}),
		ParticleController("Particle Controller", new InfluencerWrapper[]{
						new InfluencerWrapper("Single Particle Controller", ParticleControllerInfluencer.Single.class),
						new InfluencerWrapper("Random Particle Controller", ParticleControllerInfluencer.Random.class),
						new InfluencerWrapper("Scale", ScaleInfluencer.class),
						new InfluencerWrapper("Spawn", SpawnInfluencer.class),
						new InfluencerWrapper("Dynamics", DynamicsInfluencer.class)});

		public String desc;
		public InfluencerWrapper[] wrappers;
		private ControllerType(String desc, InfluencerWrapper[] wrappers){
			this.desc = desc;
			this.wrappers = wrappers;
		}
	}
	
	LwjglCanvas lwjglCanvas;
	JPanel controllerPropertiesPanel;
	JPanel editorPropertiesPanel;
	EffectPanel effectPanel;
	JSplitPane splitPane;
	NumericValue fovValue;
	NumericValue deltaMultiplier;
	GradientColorValue backgroundColor;
	AppRenderer renderer;
	AssetManager assetManager;
	JComboBox influencerBox;
	
	private ParticleEffect effect;
	/** READ only */
	public Array<ControllerData> controllersData;
	ParticleSystem particleSystem;

	public FlameMain () {
		super("Flame");
		MathUtils.random = new RandomXS128();
		particleSystem = ParticleSystem.get();
		effect = new ParticleEffect();
		particleSystem.add(effect);
		assetManager = new AssetManager();
		assetManager.setErrorListener(this);
		assetManager.setLoader(ParticleEffect.class, new ParticleEffectLoader(new InternalFileHandleResolver()));
		controllersData = new Array<ControllerData>();
		
		lwjglCanvas = new LwjglCanvas(renderer = new AppRenderer());
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
	
	public ControllerType getControllerType () {
		ParticleController controller = getEmitter();
		ControllerType type = null;
		if(controller.renderer instanceof BillboardRenderer)
			type = ControllerType.Billboard;
		else if(controller.renderer instanceof PointSpriteRenderer)
			type = ControllerType.PointSprite;
		else if(controller.renderer instanceof ModelInstanceRenderer)
			type = ControllerType.ModelInstance;
		else if(controller.renderer instanceof ParticleControllerControllerRenderer)
			type = ControllerType.ParticleController;	
		return type;
	}

	void reloadRows () {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				
				//Ensure no listener is left watching for events
				EventManager.get().clear();
				
				//Clear
				editorPropertiesPanel.removeAll();
				influencerBox.removeAllItems();
				controllerPropertiesPanel.removeAll();
				
				//Editor props
				addRow(editorPropertiesPanel, new NumericPanel(FlameMain.this, fovValue, "Field of View", ""));
				addRow(editorPropertiesPanel, new NumericPanel(FlameMain.this, deltaMultiplier, "Delta multiplier", ""));
				addRow(editorPropertiesPanel, new GradientPanel(FlameMain.this,backgroundColor, "Background color", "", true));
				addRow(editorPropertiesPanel, new DrawPanel(FlameMain.this, "Draw", ""));
				addRow(editorPropertiesPanel, new TextureLoaderPanel(FlameMain.this, "Texture", ""));
				addRow(editorPropertiesPanel, new BillboardBatchPanel(FlameMain.this, renderer.billboardBatch), 1, 1);
				editorPropertiesPanel.repaint();
				
				//Controller props
				ParticleController controller = getEmitter();
				if(controller != null){
					//Reload available influencers
					DefaultComboBoxModel model = (DefaultComboBoxModel)influencerBox.getModel();			
					ControllerType type = getControllerType();
					if(type != null){
						for(Object value : type.wrappers)
							model.addElement(value);
					}
					JPanel panel = null;
					addRow(controllerPropertiesPanel, getPanel(controller.emitter));
					for(int i=0, c = controller.influencers.size; i < c; ++i){
						Influencer influencer = (Influencer)controller.influencers.get(i);
						panel = getPanel(influencer);
						if(panel != null)
							addRow(controllerPropertiesPanel, panel, 1, i == c-1 ? 1 : 0);
					}
					for (Component component : controllerPropertiesPanel.getComponents())
						if (component instanceof EditorPanel) 
							((EditorPanel)component).update(FlameMain.this);
				}
				controllerPropertiesPanel.repaint();
			}
		});
	}

	protected JPanel getPanel (Emitter emitter) {
		if(emitter instanceof RegularEmitter){
			return new RegularEmitterPanel(this, (RegularEmitter) emitter);
		}
		return null;
	}

	protected JPanel getPanel (Influencer influencer) {
		if(influencer instanceof ColorInfluencer.Single){
			return new ColorInfluencerPanel(this, (ColorInfluencer.Single) influencer);
		}
		if(influencer instanceof ColorInfluencer.Random){
			return new InfluencerPanel<ColorInfluencer.Random>(this, (ColorInfluencer.Random) influencer, 
				"Random Color Influencer", "Assign a random color to the particles") {};
		}
		else if(influencer instanceof ScaleInfluencer){
			return  new ScaleInfluencerPanel(this, (ScaleInfluencer)influencer);
		}
		else if(influencer instanceof SpawnInfluencer){
			return  new SpawnInfluencerPanel(this, (SpawnInfluencer)influencer);
		}
		else if(influencer instanceof DynamicsInfluencer){
			return  new DynamicsInfluencerPanel(this, (DynamicsInfluencer)influencer);
		}
		else if(influencer instanceof ModelInfluencer){
			boolean single = influencer instanceof ModelInfluencer.Single;
			String name = single ? "Model Single Influencer" : "Model Random Influencer";
			return  new ModelInfluencerPanel(this, (ModelInfluencer)influencer, single, name, "Defines what model will be used for the particles");
		}
		else if(influencer instanceof ParticleControllerInfluencer){
			boolean single = influencer instanceof ParticleControllerInfluencer.Single;
			String name = single ? "Particle Controller Single Influencer" : "Particle Controller Random Influencer";
			return  new ParticleControllerInfluencerPanel(this, (ParticleControllerInfluencer)influencer, single, name, "Defines what controller will be used for the particles");
		}
		else if(influencer instanceof RegionInfluencer.Single){
			return  new RegionInfluencerPanel(this, "Billboard Single Region Influencer", 
				"Assign the chosen region to the particles", (RegionInfluencer.Single)influencer);
		}
		else if(influencer instanceof RegionInfluencer.Animated){
			return  new RegionInfluencerPanel(this, "Billboard Animated Region Influencer", 
				"Animates the region of the particles", (RegionInfluencer.Animated)influencer);
		}
		else if(influencer instanceof RegionInfluencer.Random){
			return  new RegionInfluencerPanel(this, "Billboard Random Region Influencer", 
				"Assigns a randomly picked (among those selected) region to the particles", (RegionInfluencer.Random)influencer);
		}
		else if(influencer instanceof ParticleControllerFinalizerInfluencer){
			return new InfluencerPanel<ParticleControllerFinalizerInfluencer>(this, (ParticleControllerFinalizerInfluencer) influencer, 
				"ParticleControllerFinalizer Influencer", "This is required when dealing with a controller of controllers, it will update the controller assigned to each particle, it MUST be the last influencer always.", 
				true, false) {};
		}
		
		return null;
	}

	protected JPanel getPanel (ParticleBatch renderer) {
		if(renderer instanceof PointSpriteParticleBatch){
			return new EmptyPanel(this, "Point Sprite Batch", "It renders particles as point sprites.");
		}
		if(renderer instanceof BillboardParticleBatch){
			return new BillboardBatchPanel(this, (BillboardParticleBatch) renderer);
		}
		else if(renderer instanceof ModelInstanceParticleBatch){
			return new EmptyPanel(this, "Model Instance Batch", "It renders particles as model instances.");
		}
		
		return null;
	}

	void addRow(JPanel panel, JPanel row) {
		addRow(panel, row, 1, 0);
	}
	
	void addRow(JPanel panel, JPanel row, float wx, float wy) {
		row.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, java.awt.Color.black));
		panel.add(row, new GridBagConstraints(0, -1, 1, 1, wx, wy, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(0, 0, 0, 0), 0, 0));
	}

	public void setVisible (String name, boolean visible) {
		for (Component component : controllerPropertiesPanel.getComponents())
			if (component instanceof EditorPanel && ((EditorPanel)component).getName().equals(name)) component.setVisible(visible);
	}

	private void rebuildActiveControllers () {
		//rebuild list
		Array<ParticleController> effectControllers = effect.getControllers();
		effectControllers.clear();
		for(ControllerData controllerData : controllersData){
			if(controllerData.enabled)
				effectControllers.add(controllerData.controller);
		}
		//System.out.println("rebuilding active controllers");

		effect.init();
		effect.start();
	}

	public ParticleController getEmitter () {
		return effectPanel.editIndex >=0 ? controllersData.get(effectPanel.editIndex).controller : null;
	}
	
	public void addEmitter (ParticleController emitter) {
		controllersData.add(new ControllerData(emitter));
		rebuildActiveControllers();
	}

	public void removeEmitter (int row) {
		controllersData.removeIndex(row).controller.dispose();
		rebuildActiveControllers();
	}
        
	public void setEnabled (int emitterIndex, boolean enabled) {
		ControllerData data = controllersData.get(emitterIndex);
		data.enabled = enabled;
		rebuildActiveControllers();
	}

	public boolean isEnabled (int emitterIndex) {
		return controllersData.get(emitterIndex).enabled;
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
						editorPropertiesPanel = new JPanel(new GridBagLayout());
						scroll.setViewportView(editorPropertiesPanel);
						scroll.getVerticalScrollBar().setUnitIncrement(70);
					}
				}
			}

			{	
				JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
				rightSplitPane.setUI(new BasicSplitPaneUI() {
					public void paint (Graphics g, JComponent jc) {
					}
				});
				rightSplitPane.setDividerSize(4);
				rightSplitPane.setDividerLocation(100);
				rightSplit.add(rightSplitPane, JSplitPane.BOTTOM);

				JPanel propertiesPanel = new JPanel(new GridBagLayout());
				rightSplitPane.add(propertiesPanel, JSplitPane.TOP);
				propertiesPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(3, 0, 6, 6), BorderFactory
					.createTitledBorder("Influencers")));
				{
					JScrollPane scroll = new JScrollPane();
					propertiesPanel.add(scroll, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
					{
						JPanel influencersPanel = new JPanel(new GridBagLayout());
						influencerBox = new JComboBox(new DefaultComboBoxModel());
						JButton addInfluencerButton = new JButton("Add");
						addInfluencerButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed (ActionEvent e) {
								InfluencerWrapper wrapper = (InfluencerWrapper)influencerBox.getSelectedItem();
								ParticleController controller = getEmitter();
								if(controller != null)
									addInfluencer(wrapper.type, controller);

							}
						});
						influencersPanel.add(influencerBox, new GridBagConstraints(0, 0, 1, 1, 0, 1, GridBagConstraints.NORTHWEST,
							GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						influencersPanel.add(addInfluencerButton, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
							GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						scroll.setViewportView(influencersPanel);
						scroll.getVerticalScrollBar().setUnitIncrement(70);
					}
				}
				

				propertiesPanel = new JPanel(new GridBagLayout());
				rightSplitPane.add(propertiesPanel, JSplitPane.BOTTOM);
				propertiesPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(3, 0, 6, 6), BorderFactory
					.createTitledBorder("Particle Controller Components")));
				{
					JScrollPane scroll = new JScrollPane();
					propertiesPanel.add(scroll, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
					{
						controllerPropertiesPanel = new JPanel(new GridBagLayout());
						scroll.setViewportView(controllerPropertiesPanel);
						scroll.getVerticalScrollBar().setUnitIncrement(70);
					}
				}
			}
			
			rightSplit.setDividerLocation(250);

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
					.createTitledBorder("Particle Controllers")));
				{
					effectPanel = new EffectPanel(this);
					emittersPanel.add(effectPanel);
				}
			}
			leftSplit.setDividerLocation(625);
		}
		splitPane.setDividerLocation(500);
	}

	protected void addInfluencer (Class<Influencer> type, ParticleController controller) {
		if(controller.findInfluencer(type) != null) return;

		try {
			controller.end();
			
			Influencer newInfluencer = type.newInstance();
			boolean replaced = false;
			if(ColorInfluencer.class.isAssignableFrom(type)){
				 replaced = controller.replaceInfluencer(ColorInfluencer.class, (ColorInfluencer)newInfluencer);
			}
			else if(RegionInfluencer.class.isAssignableFrom(type)){
				 replaced = controller.replaceInfluencer(RegionInfluencer.class, (RegionInfluencer)newInfluencer);
			}
			else if(ModelInfluencer.class.isAssignableFrom(type)){
				ModelInfluencer newModelInfluencer = (ModelInfluencer) newInfluencer;
				ModelInfluencer currentInfluencer = (ModelInfluencer)controller.findInfluencer(ModelInfluencer.class);
				if(currentInfluencer != null){
						newModelInfluencer.models.add(currentInfluencer.models.first());
				}
				replaced = controller.replaceInfluencer(ModelInfluencer.class, (ModelInfluencer)newInfluencer);
			}
			else if(ParticleControllerInfluencer.class.isAssignableFrom(type)){		
				ParticleControllerInfluencer newModelInfluencer = (ParticleControllerInfluencer) newInfluencer;
				ParticleControllerInfluencer currentInfluencer = (ParticleControllerInfluencer)controller.findInfluencer(ParticleControllerInfluencer.class);
				if(currentInfluencer != null){
						newModelInfluencer.templates.add(currentInfluencer.templates.first());
				}
				replaced = controller.replaceInfluencer(ParticleControllerInfluencer.class, (ParticleControllerInfluencer)newInfluencer);
			}
			
			if(!replaced){
				if(getControllerType() != ControllerType.ParticleController)
					controller.influencers.add(newInfluencer);
				else{
					Influencer finalizer = controller.influencers.pop();
					controller.influencers.add(newInfluencer);
					controller.influencers.add(finalizer);
				}
			}

			controller.init();
			effect.start();
			reloadRows();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	protected boolean canAddInfluencer (Class influencerType, ParticleController controller) {
		boolean hasSameInfluencer = controller.findInfluencer(influencerType) != null;
		if(!hasSameInfluencer){
			if( 	(ColorInfluencer.Single.class.isAssignableFrom(influencerType) && controller.findInfluencer(ColorInfluencer.Random.class) != null) ||
					(ColorInfluencer.Random.class.isAssignableFrom(influencerType) && controller.findInfluencer(ColorInfluencer.Single.class) != null) ){
				return false;
			}
			
			if(RegionInfluencer.class.isAssignableFrom(influencerType)){
				return controller.findInfluencer(RegionInfluencer.class) == null;
			}
			else if(ModelInfluencer.class.isAssignableFrom(influencerType)){
				return controller.findInfluencer(ModelInfluencer.class) == null;
			}
			else if(ParticleControllerInfluencer.class.isAssignableFrom(influencerType)){
				return controller.findInfluencer(ParticleControllerInfluencer.class) == null;
			}
		}
		return !hasSameInfluencer;
	}

	class AppRenderer implements ApplicationListener {
		//Stats
		private float maxActiveTimer;
		private int maxActive, lastMaxActive;
		boolean isUpdate = true;
		
		//Controls
		private CameraInputController cameraInputController;
		
		//UI
		private Stage ui;
		TextButton playPauseButton;
		private Label fpsLabel, pointCountLabel, billboardCountLabel, modelInstanceCountLabel, maxLabel;
		StringBuilder stringBuilder;
		
		//Render
		public PerspectiveCamera worldCamera;
		private boolean isDrawXYZ, isDrawXZPlane, isDrawXYPlane;
		private Array<Model> models;
		private ModelInstance xyzInstance, xzPlaneInstance, xyPlaneInstance;
		private Environment environment;
		private ModelBatch modelBatch;
		PointSpriteParticleBatch pointSpriteBatch;
		BillboardParticleBatch billboardBatch;
		ModelInstanceParticleBatch modelInstanceParticleBatch;
		
		public void create () {
			if (ui != null) return;
			int w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
			modelBatch = new ModelBatch();
			environment = new Environment();
			environment.add(new DirectionalLight().set(Color.WHITE, 0,0,-1));
			
			worldCamera = new PerspectiveCamera(67, w, h);
			worldCamera.position.set(10, 10, 10);
			worldCamera.lookAt(0,0,0);
			worldCamera.near = 0.1f;
			worldCamera.far = 300f;
			worldCamera.update();

			cameraInputController = new CameraInputController(worldCamera);

			//Batches
			pointSpriteBatch = new PointSpriteParticleBatch();
			pointSpriteBatch.setCamera(worldCamera);
			
			billboardBatch = new BillboardParticleBatch();
			billboardBatch.setCamera(worldCamera);
			modelInstanceParticleBatch = new ModelInstanceParticleBatch();
			
			particleSystem.add(billboardBatch);
			particleSystem.add(pointSpriteBatch);
			particleSystem.add(modelInstanceParticleBatch);
			
			fovValue = new NumericValue();
			fovValue.setValue(67);
			fovValue.setActive(true);

			deltaMultiplier = new NumericValue();
			deltaMultiplier.setValue(1.0f);
			deltaMultiplier.setActive(true);

			backgroundColor = new GradientColorValue();
			Color color = Color.valueOf("878787");
			backgroundColor.setColors(new float[] { color.r, color.g, color.b});

			models = new Array<Model>();
			ModelBuilder builder = new ModelBuilder();
			Model 	xyzModel = builder.createXYZCoordinates(10, new Material(), Usage.Position|Usage.ColorPacked),
				planeModel = builder.createLineGrid(10, 10, 1, 1, new Material(ColorAttribute.createDiffuse(Color.WHITE)), Usage.Position);
			models.add(xyzModel);
			models.add(planeModel);
			xyzInstance = new ModelInstance(xyzModel);
			xzPlaneInstance = new ModelInstance(planeModel);
			xyPlaneInstance = new ModelInstance(planeModel);
			xyPlaneInstance.transform.rotate(1f, 0f, 0f, 90f);

			setDrawXYZ(true);
			setDrawXZPlane(true);


			//Load default resources
			ParticleEffectLoader.ParticleEffectLoadParameter params = new ParticleEffectLoader.ParticleEffectLoadParameter(particleSystem.getBatches());
			assetManager.load(DEFAULT_BILLBOARD_PARTICLE, Texture.class);
			assetManager.load(DEFAULT_MODEL_PARTICLE, Model.class);
			assetManager.load(DEFAULT_SKIN, Skin.class);
			assetManager.load(DEFAULT_TEMPLATE_PFX, ParticleEffect.class, params);
			
			assetManager.finishLoading();
			assetManager.setLoader(ParticleEffect.class, new ParticleEffectLoader(new AbsoluteFileHandleResolver()));
			assetManager.get(DEFAULT_MODEL_PARTICLE, Model.class).materials.get(0).set(new BlendingAttribute(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA, 1));
			
			//Ui
			stringBuilder = new StringBuilder();
			Skin skin = assetManager.get(DEFAULT_SKIN, Skin.class);
			ui = new Stage();
			fpsLabel = new Label("", skin);
			pointCountLabel = new Label("", skin);
			billboardCountLabel = new Label("", skin);
			modelInstanceCountLabel = new Label("", skin);
			
			maxLabel = new Label("", skin);
			playPauseButton = new TextButton("Pause", skin);
			playPauseButton.addListener(new ClickListener(){
				@Override
				public void clicked (InputEvent event, float x, float y) {
					isUpdate = !isUpdate;
					playPauseButton.setText(isUpdate ? "Pause" : "Play");
				}
			});
			Table table = new Table(skin);
			table.setFillParent(true);
			table.pad(5);
			table.add(fpsLabel).expandX().left().row();
			table.add(pointCountLabel).expandX().left().row();
			table.add(billboardCountLabel).expandX().left().row();
			table.add(modelInstanceCountLabel).expandX().left().row();
			table.add(maxLabel).expandX().left().row();
			table.add(playPauseButton).expand().bottom().left().row();
			ui.addActor(table);
			
			setTexture((Texture)assetManager.get(DEFAULT_BILLBOARD_PARTICLE));
			effectPanel.createDefaultEmitter(ControllerType.Billboard, true, true);
		}

		@Override
		public void resize (int width, int height) {
			Gdx.input.setInputProcessor(new InputMultiplexer(ui, cameraInputController));
			Gdx.gl.glViewport(0, 0, width, height);

			worldCamera.viewportWidth = width;
			worldCamera.viewportHeight = height;
			worldCamera.update();
			ui.getViewport().setWorldSize(width, height);
			ui.getViewport().update(width, height, true);
		}

		public void render () {
			//float delta = Math.max(0, Gdx.graphics.getDeltaTime() * deltaMultiplier.getValue());
			update();
			renderWorld();
		}

		private void update () {
			worldCamera.fieldOfView = fovValue.getValue();
			worldCamera.update();
			cameraInputController.update();
			if(isUpdate){
				particleSystem.update();
				//Update ui
				stringBuilder.delete(0, stringBuilder.length);
				stringBuilder.append("Point Sprites : ").append(pointSpriteBatch.getBufferedCount());
				pointCountLabel.setText(stringBuilder);
				stringBuilder.delete(0, stringBuilder.length);
				stringBuilder.append("Billboards : ").append(billboardBatch.getBufferedCount());
				billboardCountLabel.setText(stringBuilder);
				stringBuilder.delete(0, stringBuilder.length);
				stringBuilder.append("Model Instances : ").append(modelInstanceParticleBatch.getBufferedCount());
				modelInstanceCountLabel.setText(stringBuilder);	
			}
			stringBuilder.delete(0, stringBuilder.length);
			stringBuilder.append("FPS : ").append(Gdx.graphics.getFramesPerSecond());
			fpsLabel.setText(stringBuilder);
			ui.act(Gdx.graphics.getDeltaTime());
		}

		private void renderWorld () {
			float[] colors = backgroundColor.getColors();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			Gdx.gl.glClearColor(colors[0], colors[1], colors[2], 0);
			modelBatch.begin(worldCamera);
			if(isDrawXYZ) modelBatch.render(xyzInstance);
			if(isDrawXZPlane) modelBatch.render(xzPlaneInstance);
			if(isDrawXYPlane) modelBatch.render(xyPlaneInstance);
			particleSystem.begin();
			particleSystem.draw();
			particleSystem.end();
			
			//Draw
			modelBatch.render(particleSystem, environment);
			modelBatch.end();
			ui.draw();
		}

		@Override
		public void dispose () {}

		@Override
		public void pause () {}

		@Override
		public void resume () {}
		
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
		
		public void setDrawXYPlane(boolean isDraw) 
		{
			isDrawXYPlane = isDraw;
		}

		public boolean IsDrawXYPlane() 
		{
			return isDrawXYPlane;
		}
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
				new FlameMain();
			}
		});
	}

	public AppRenderer getRenderer() {
		return renderer;
	}

	String lastDir;
	public File showFileLoadDialog () {
		return showFileDialog("Open", FileDialog.LOAD);
	}
	
	public File showFileSaveDialog () {
		return showFileDialog("Save", FileDialog.SAVE);
	}
	
	private File showFileDialog (String title, int mode ) {
		FileDialog dialog = new FileDialog(this, title, mode);
		if (lastDir != null) dialog.setDirectory(lastDir);
		dialog.setVisible(true);
		final String file = dialog.getFile();
		final String dir = dialog.getDirectory();
		if (dir == null || file == null || file.trim().length() == 0) 
			return null;
		lastDir = dir;
		return new File(dir, file);
	}
	
	@Override
	public void error (AssetDescriptor asset, Throwable throwable) {
		throwable.printStackTrace();
	}

	public PointSpriteParticleBatch getPointSpriteBatch () {
		return renderer.pointSpriteBatch;
	}

	public BillboardParticleBatch getBillboardBatch () {
		return renderer.billboardBatch;
	}

	public ModelInstanceParticleBatch getModelInstanceParticleBatch () {
		return renderer.modelInstanceParticleBatch;
	}
	
	public void setAtlas(TextureAtlas atlas){
		//currentAtlas = atlas;
		setTexture(atlas.getTextures().first());
	}
	
	public void setTexture(Texture texture){
		renderer.billboardBatch.setTexture(texture);
		renderer.pointSpriteBatch.setTexture(texture);
	}
	
	public Texture getTexture(){
		return renderer.billboardBatch.getTexture();
	}

	public TextureAtlas getAtlas(Texture texture){
		Array<TextureAtlas> atlases = assetManager.getAll(TextureAtlas.class, new Array<TextureAtlas>());
		for(TextureAtlas atlas : atlases){
			if(atlas.getTextures().contains(texture))
				return atlas;
		}
		return null;
	}
	
	public TextureAtlas getAtlas(){
		return getAtlas(renderer.billboardBatch.getTexture());
	}
	
	public boolean isUsingDefaultTexture () {
		return renderer.billboardBatch.getTexture() == assetManager.get(DEFAULT_BILLBOARD_PARTICLE, Texture.class);
	}

	public Array<ParticleEffect> getParticleEffects (Array<ParticleController> controllers, Array<ParticleEffect> out) {
		out.clear();
		assetManager.getAll(ParticleEffect.class, out);
		for(int i=0; i < out.size;){
			ParticleEffect effect = out.get(i);
			Array<ParticleController> effectControllers = effect.getControllers();
			boolean remove = true;
			for(ParticleController controller : controllers){
				if(effectControllers.contains(controller, true)){
					remove = false;
					break;
				}
			}
			
			if(remove){
				out.removeIndex(i);
				continue;
			}
			
			++i;
		}
		
		return out;
	}

	public void saveEffect (File file) {
		Writer fileWriter = null;
		try {
			ParticleEffectLoader loader = (ParticleEffectLoader)assetManager.getLoader(ParticleEffect.class);
			loader.save(effect, new ParticleEffectSaveParameter(new FileHandle(file.getAbsolutePath()), assetManager, particleSystem.getBatches()));
		} catch (Exception ex) {
			System.out.println("Error saving effect: " + file.getAbsolutePath());
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error saving effect.");
		} finally {
			StreamUtils.closeQuietly(fileWriter);
		}
	}

	public ParticleEffect openEffect (File file, boolean replaceCurrentWorkspace) {
		try {
			ParticleEffect loadedEffect = load(file.getAbsolutePath(), ParticleEffect.class, null, 
				new ParticleEffectLoader.ParticleEffectLoadParameter(particleSystem.getBatches()));
			loadedEffect = loadedEffect.copy();
			loadedEffect.init();
			if(replaceCurrentWorkspace){
				effect = loadedEffect;
				controllersData.clear();
				particleSystem.removeAll();
				particleSystem.add(effect);
				for(ParticleController controller : effect.getControllers())
					controllersData.add(new ControllerData(controller));
				rebuildActiveControllers();
			}
			reloadRows();
			return loadedEffect;
		} catch (Exception ex) {
			System.out.println("Error loading effect: " + file.getAbsolutePath());
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error opening effect.");
		}
		return null;
	}
	
	public <T> T load (String resource, Class<T> type, AssetLoader loader, AssetLoaderParameters<T> params) {	
		String resolvedPath = new String(resource).replaceAll("\\\\", "/");
		boolean exist = assetManager.isLoaded(resolvedPath, type);
		T oldAsset = null;
		if(exist){
			oldAsset = assetManager.get(resolvedPath, type);
			for(int i=assetManager.getReferenceCount(resolvedPath); i > 0; --i)
				assetManager.unload(resolvedPath);
		}
		
		AssetLoader<T, AssetLoaderParameters<T>> currentLoader = assetManager.getLoader(type);
		if(loader != null)
			assetManager.setLoader(type, loader); 

		assetManager.load(resource, type, params);
		assetManager.finishLoading();
		T res = assetManager.get(resolvedPath);
		if(currentLoader != null)
			assetManager.setLoader(type, currentLoader);
		
		if(exist)
			EventManager.get().fire(EVT_ASSET_RELOADED, new Object[]{oldAsset, res});
		
		return res;
	}

	public void restart () {
		effect.init();
		effect.start();
	}
}
