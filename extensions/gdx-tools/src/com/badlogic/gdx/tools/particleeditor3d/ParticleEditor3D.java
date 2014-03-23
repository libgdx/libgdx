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
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader.BitmapFontParameter;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader.ParticleEffectSaveParameter;
import com.badlogic.gdx.graphics.g3d.particles.controllers.BillboardParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ModelInstanceParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ParticleControllerParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.PointSpriteParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.ModelInstanceColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.PointSpriteColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ModelInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ModelInfluencer.ModelInstanceRandomInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ModelInfluencer.ModelInstanceSingleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerInfluencer.ParticleControllerRandomInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerInfluencer.ParticleControllerSingleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomColorInfluencer.BillboardRandomColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomColorInfluencer.ModelInstanceRandomColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomColorInfluencer.PointSpriteRandomColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer.BillboardAnimatedRegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer.BillboardRandomRegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer.BillboardSingleRegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer.PointSpriteAnimatedRegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer.PointSpriteRandomRegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer.PointSpriteSingleRegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer.BillboardScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer.ModelInstanceScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer.ParticleControllerScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer.PointSpriteScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.BillboardSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.ModelInstanceSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.ParticleControllerSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.PointSpriteSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.BillboardVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.BillboardColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.ModelInstanceVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.ParticleControllerVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.PointSpriteVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardBatch;
import com.badlogic.gdx.graphics.g3d.particles.renderers.IParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ModelInstanceParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointSpriteBatch;
import com.badlogic.gdx.graphics.g3d.particles.values.GradientColorValue;
import com.badlogic.gdx.graphics.g3d.particles.values.NumericValue;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.StreamUtils;

import de.matthiasmann.twlthemeeditor.gui.NewClasspathDialog;

public class ParticleEditor3D extends JFrame implements AssetErrorListener {
	public static final String 	DEFAULT_FONT = "default.fnt",
											DEFAULT_BILLBOARD_PARTICLE = "pre_particle.png",
											DEFAULT_MODEL_PARTICLE = "monkey.g3db",
											DEFAULT_PFX = "default.pfx",
											DEFAULT_SKIN = "uiskin.json";
	
	public static final int EVT_ASSET_RELOADED = 0;
	
	
	private static class InfluencerWrapper<T>{
		String string;
		Class<Influencer<T>> type;
		public InfluencerWrapper(String string, Class<Influencer<T>> type){
			this.string = string;
			this.type = type;
		}
		
		@Override
		public String toString () {
			return string;
		}
	}
	
	static InfluencerWrapper[] billboardInfluencers = new InfluencerWrapper[]{
		new InfluencerWrapper("Color Influencer", BillboardColorInfluencer.class),
		new InfluencerWrapper("Random Color Influencer", BillboardRandomColorInfluencer.class),
		new InfluencerWrapper("Single Region Influencer", BillboardSingleRegionInfluencer.class),
		new InfluencerWrapper("Random Region Influencer", BillboardRandomRegionInfluencer.class),
		new InfluencerWrapper("Animated Region Influencer", BillboardAnimatedRegionInfluencer.class),
		new InfluencerWrapper("Scale Influencer", BillboardScaleInfluencer.class),
		new InfluencerWrapper("Spawn Influencer", BillboardSpawnInfluencer.class),
		new InfluencerWrapper("Velocity Influencer", BillboardVelocityInfluencer.class)
	};
	
	static InfluencerWrapper[] pointInfluencers = new InfluencerWrapper[]{
		new InfluencerWrapper("Color Influencer", PointSpriteColorInfluencer.class),
		new InfluencerWrapper("Random Color Influencer", PointSpriteRandomColorInfluencer.class),
		new InfluencerWrapper("Single Region Influencer", PointSpriteSingleRegionInfluencer.class),
		new InfluencerWrapper("Random Region Influencer", PointSpriteRandomRegionInfluencer.class),
		new InfluencerWrapper("Animated Region Influencer", PointSpriteAnimatedRegionInfluencer.class),
		new InfluencerWrapper("Scale Influencer", PointSpriteScaleInfluencer.class),
		new InfluencerWrapper("Spawn Influencer", PointSpriteSpawnInfluencer.class),
		new InfluencerWrapper("Velocity Influencer", PointSpriteVelocityInfluencer.class)
	};
	
	static InfluencerWrapper[] modelInstanceInfluencers = new InfluencerWrapper[]{
		new InfluencerWrapper("Color Influencer", ModelInstanceColorInfluencer.class),
		new InfluencerWrapper("Random Color Influencer", ModelInstanceRandomColorInfluencer.class),
		new InfluencerWrapper("Single Model Influencer", ModelInstanceSingleInfluencer.class),
		new InfluencerWrapper("Random Model Influencer", ModelInstanceRandomInfluencer.class),
		new InfluencerWrapper("Scale Influencer", ModelInstanceScaleInfluencer.class),
		new InfluencerWrapper("Spawn Influencer", ModelInstanceSpawnInfluencer.class),
		new InfluencerWrapper("Velocity Influencer", ModelInstanceVelocityInfluencer.class)
	};
	
	static InfluencerWrapper[] particleControllerInfluencers = new InfluencerWrapper[]{
		new InfluencerWrapper("Single Particle Controller Influencer", ParticleControllerSingleInfluencer.class),
		new InfluencerWrapper("Random Particle Controller Influencer", ParticleControllerRandomInfluencer.class),
		new InfluencerWrapper("Scale Influencer", ParticleControllerScaleInfluencer.class),
		new InfluencerWrapper("Spawn Influencer", ParticleControllerSpawnInfluencer.class),
		new InfluencerWrapper("Velocity Influencer", ParticleControllerVelocityInfluencer.class)
	};
	
	
	LwjglCanvas lwjglCanvas;
	JPanel controllerPropertiesPanel;
	JPanel editorPropertiesPanel;
	EffectPanel effectPanel;
	private JSplitPane splitPane;
	NumericValue fovValue;
	NumericValue deltaMultiplier;
	GradientColorValue backgroundColor;
	AppRenderer renderer;
	AssetManager assetManager;
	JComboBox influencerBox;
	
	ParticleEffect effect;
	final HashMap<ParticleController, ParticleData> particleData = new HashMap();

	public ParticleEditor3D () {
		super("Particle Editor 3D");

		assetManager = new AssetManager();
		assetManager.setErrorListener(this);
		assetManager.setLoader(ParticleEffect.class, new ParticleEffectLoader(new AbsoluteFileHandleResolver()));
		effect = new ParticleEffect();
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
				addRow(editorPropertiesPanel, new NumericPanel(ParticleEditor3D.this, fovValue, "Field of View", ""));
				addRow(editorPropertiesPanel, new NumericPanel(ParticleEditor3D.this, deltaMultiplier, "Delta multiplier", ""));
				addRow(editorPropertiesPanel, new GradientPanel(ParticleEditor3D.this,backgroundColor, "Background color", "", true));
				addRow(editorPropertiesPanel, new DrawPanel(ParticleEditor3D.this, "Draw", ""));
				addRow(editorPropertiesPanel, new TextureLoaderPanel(ParticleEditor3D.this, "Texture", ""));
				addRow(editorPropertiesPanel, new BillboardBatchPanel(ParticleEditor3D.this, renderer.billboardBatch), 1, 1);
				editorPropertiesPanel.repaint();
				
				//Controller props
				ParticleController controller = getEmitter();
				if(controller != null){
					//Reload available influencers
					DefaultComboBoxModel model = (DefaultComboBoxModel)influencerBox.getModel();
					InfluencerWrapper[] values = null;
					if(controller instanceof BillboardParticleController)
						values = billboardInfluencers;
					else if (controller instanceof ModelInstanceParticleController) {
						values = modelInstanceInfluencers;
					}
					else if (controller instanceof ParticleControllerParticleController) {
						values = particleControllerInfluencers;
					}
					else if (controller instanceof PointSpriteParticleController) {
						values = pointInfluencers;
					}
					if(values != null){
						for(Object value : values)
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
							((EditorPanel)component).update(ParticleEditor3D.this);
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
		if(influencer instanceof ColorInfluencer){
			return new ColorInfluencerPanel(this, (ColorInfluencer) influencer);
		}
		if(influencer instanceof RandomColorInfluencer){
			return new InfluencerPanel<RandomColorInfluencer>(this, (RandomColorInfluencer) influencer, 
				"Random Color Influencer", "Assign a random color to the particles") {};
		}
		else if(influencer instanceof ScaleInfluencer){
			return  new ScaleInfluencerPanel(this, (ScaleInfluencer)influencer);
		}
		else if(influencer instanceof SpawnShapeInfluencer){
			return  new SpawnInfluencerPanel(this, (SpawnShapeInfluencer)influencer);
		}
		else if(influencer instanceof VelocityInfluencer){
			return  new VelocityInfluencerPanel(this, (VelocityInfluencer)influencer);
		}
		else if(influencer instanceof ModelInfluencer){
			boolean single = influencer instanceof ModelInfluencer.ModelInstanceSingleInfluencer;
			String name = single ? "Model Single Influencer" : "Model Random Influencer";
			return  new ModelInfluencerPanel(this, (ModelInfluencer)influencer, single, name, "Defines what model will be used for the particles");
		}
		else if(influencer instanceof ParticleControllerInfluencer){
			boolean single = influencer instanceof ParticleControllerInfluencer.ParticleControllerSingleInfluencer;
			String name = single ? "Particle Controller Single Influencer" : "Particle Controller Random Influencer";
			return  new ParticleControllerInfluencerPanel(this, (ParticleControllerInfluencer)influencer, single, name, "Defines what controller will be used for the particles");
		}
		else if(influencer instanceof RegionInfluencer.BillboardSingleRegionInfluencer){
			return  new RegionInfluencerPanel(this, "Billboard Single Region Influencer", 
				"Assign the chosen region to the particles", (RegionInfluencer)influencer);
		}
		else if(influencer instanceof RegionInfluencer.BillboardAnimatedRegionInfluencer){
			return  new RegionInfluencerPanel(this, "Billboard Animated Region Influencer", 
				"Animates the region of the particles", (RegionInfluencer)influencer);
		}
		else if(influencer instanceof RegionInfluencer.BillboardRandomRegionInfluencer){
			return  new RegionInfluencerPanel(this, "Billboard Random Region Influencer", 
				"Assigns a randomly picked (among those selected) region to the particles", (RegionInfluencer)influencer);
		}
		else if(influencer instanceof RegionInfluencer.PointSpriteSingleRegionInfluencer){
			return  new RegionInfluencerPanel(this, "Point Sprite Single Region Influencer", 
				"Assign the chosen region to the particles", (RegionInfluencer)influencer);
		}
		else if(influencer instanceof RegionInfluencer.PointSpriteRandomRegionInfluencer){
			return  new RegionInfluencerPanel(this, "Point Sprite Random Region Influencer", 
				"Assigns a randomly picked (among those selected) region to the particles", (RegionInfluencer)influencer);
		}
		else if(influencer instanceof RegionInfluencer.PointSpriteAnimatedRegionInfluencer){
			return  new RegionInfluencerPanel(this, "Point Sprite Animated Region Influencer", 
				"Animates the region of the particles", (RegionInfluencer)influencer);
		}
		
		return null;
	}

	protected JPanel getPanel (IParticleBatch renderer) {
		if(renderer instanceof PointSpriteBatch){
			return new EmptyPanel(this, "Point Sprite Batch", "It renders particles as point sprites.");
		}
		if(renderer instanceof BillboardBatch){
			return new BillboardBatchPanel(this, (BillboardBatch) renderer);
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

	public ParticleController getEmitter () {
		return effectPanel.editIndex >=0 ? effect.getControllers().get(effectPanel.editIndex) : null;
	}
        
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
					.createTitledBorder("Emitter Properties")));
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

	protected void addInfluencer (Class<Influencer> type, ParticleController controller) {
		if(controller.findInfluencer(type) != null) return;

		try {
			Influencer newInfluencer = type.newInstance();
			boolean replaced = false;
			if(ColorInfluencer.class.isAssignableFrom(type)){
				 replaced = controller.replaceInfluencer(RandomColorInfluencer.class, newInfluencer);
			}
			else if(RandomColorInfluencer.class.isAssignableFrom(type)){
				 replaced = controller.replaceInfluencer(ColorInfluencer.class, newInfluencer);
			}
			else if(RegionInfluencer.class.isAssignableFrom(type)){
				 replaced = controller.replaceInfluencer(RegionInfluencer.class, newInfluencer);
			}
			else if(ModelInfluencer.class.isAssignableFrom(type)){
				ModelInfluencer newModelInfluencer = (ModelInfluencer) newInfluencer;
				ModelInfluencer currentInfluencer = (ModelInfluencer)controller.findInfluencer(ModelInfluencer.class);
				if(currentInfluencer != null){
					if(currentInfluencer instanceof ModelInfluencer.ModelInstanceSingleInfluencer)
						newModelInfluencer.models.add(currentInfluencer.models.first());
					else 
						newModelInfluencer.models.addAll(currentInfluencer.models);
				}
				replaced = controller.replaceInfluencer(ModelInfluencer.class, newInfluencer);
			}
			else if(ParticleControllerInfluencer.class.isAssignableFrom(type)){		
				ParticleControllerInfluencer newModelInfluencer = (ParticleControllerInfluencer) newInfluencer;
				ParticleControllerInfluencer currentInfluencer = (ParticleControllerInfluencer)controller.findInfluencer(ParticleControllerInfluencer.class);
				if(currentInfluencer != null){
					if(currentInfluencer instanceof ParticleControllerInfluencer.ParticleControllerSingleInfluencer)
						newModelInfluencer.templates.add(currentInfluencer.templates.first());
					else 
						newModelInfluencer.templates.addAll(currentInfluencer.templates);
				}
				replaced = controller.replaceInfluencer(ParticleControllerInfluencer.class, newInfluencer);
			}
			
			if(!replaced)
				controller.influencers.add(newInfluencer);

			controller.init();
			effect.start();
			reloadRows();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	protected boolean canAddInfluencer (Class influencerType, ParticleController controller) {
		boolean hasSameInfluencer = controller.findInfluencer(influencerType) != null;
		if(!hasSameInfluencer){
			if( 	(ColorInfluencer.class.isAssignableFrom(influencerType) && controller.findInfluencer(RandomColorInfluencer.class) != null) ||
					(RandomColorInfluencer.class.isAssignableFrom(influencerType) && controller.findInfluencer(ColorInfluencer.class) != null) ){
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

	class AppRenderer extends InputAdapter implements ApplicationListener {
		//Stats
		private float maxActiveTimer;
		private int maxActive, lastMaxActive;
		private int activeCount;
		boolean isUpdate = true;
		
		//Controls
		private CameraInputController cameraInputController;
		
		//UI
		private Stage ui;
		TextButton playPauseButton;
		private Label fpsLabel, countLabel, maxLabel;
		
		//Render
		public PerspectiveCamera worldCamera;
		private boolean isDrawXYZ, isDrawXZPlane;
		private Array<Model> models;
		private ModelInstance xyzInstance, xzPlaneInstance;
		private Environment environment;
		private ModelBatch modelBatch;
		PointSpriteBatch pointSpriteBatch;
		BillboardBatch billboardBatch;
		ModelInstanceParticleBatch modelInstanceParticleBatch;
		
		public void create () {
			if (ui != null) return;
			modelBatch = new ModelBatch();
			environment = new Environment();
			environment.add(new DirectionalLight().set(Color.WHITE, 0,0,-1));
			
			worldCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			worldCamera.position.set(10, 10, 10);
			worldCamera.lookAt(0,0,0);
			worldCamera.near = 0.1f;
			worldCamera.far = 300f;
			worldCamera.update();

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


			//Load default resources
			assetManager.load(DEFAULT_BILLBOARD_PARTICLE, Texture.class);
			assetManager.load(DEFAULT_MODEL_PARTICLE, Model.class);
			assetManager.load(DEFAULT_SKIN, Skin.class);
			assetManager.finishLoading();
			assetManager.get(DEFAULT_MODEL_PARTICLE, Model.class).materials.get(0).set(new BlendingAttribute(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA, 1));
			
			//Ui
			Skin skin = assetManager.get(DEFAULT_SKIN, Skin.class);
			ui = new Stage();
			fpsLabel = new Label("", skin);
			countLabel = new Label("", skin);
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
			table.add(countLabel).expandX().left().row();
			table.add(maxLabel).expandX().left().row();
			table.add(playPauseButton).expand().bottom().left().row();
			ui.addActor(table);
			
			//Batches
			PointSpriteBatch.init();
			pointSpriteBatch = new PointSpriteBatch();
			pointSpriteBatch.setCamera(worldCamera);
			
			billboardBatch = new BillboardBatch();
			billboardBatch.setCamera(worldCamera);
			modelInstanceParticleBatch = new ModelInstanceParticleBatch();
			
			setTexture((Texture)assetManager.get(DEFAULT_BILLBOARD_PARTICLE));
			effectPanel.createDefaultEmitter(BillboardParticleController.class, true, true);
			assetManager.set(ParticleEffect.class, DEFAULT_PFX, 
					new ParticleEffect( effectPanel.createDefaultEmitter(BillboardParticleController.class, false, false)));
		}


		@Override
		public void resize (int width, int height) {
			Gdx.input.setInputProcessor(new InputMultiplexer(ui, this, cameraInputController));
			Gdx.gl.glViewport(0, 0, width, height);

			worldCamera.viewportWidth = width;
			worldCamera.viewportHeight = height;
			worldCamera.update();
			ui.setViewport(width, height);
		}

		public void render () {
			float delta = Math.max(0, Gdx.graphics.getDeltaTime() * deltaMultiplier.getValue());
			update(delta);
			renderWorld();
		}

		private void update (float dt) {
			worldCamera.fieldOfView = fovValue.getValue();
			worldCamera.update();
			cameraInputController.update();
			if(isUpdate){
				activeCount = 0;
				for (ParticleController controller : effect.getControllers()) 
				{
					if (isEnabled(controller)) {	
						controller.update(dt);
						activeCount += controller.emitter.activeCount;
					}
				}
				//Update ui
				maxActive = Math.max(maxActive, activeCount);
				maxActiveTimer += dt;
				if (maxActiveTimer > 3) {
					maxActiveTimer = 0;
					lastMaxActive = maxActive;
					maxActive = 0;
				}
				countLabel.setText("Count: " + activeCount);
				maxLabel.setText("Max: " + lastMaxActive);
			}
			fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
			ui.act(dt);
		}

		
		private void renderWorld () {
			float[] colors = backgroundColor.getColors();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			Gdx.gl.glClearColor(colors[0], colors[1], colors[2], 0);
			modelBatch.begin(worldCamera);
			if(isDrawXYZ) modelBatch.render(xyzInstance);
			if(isDrawXZPlane) modelBatch.render(xzPlaneInstance);
			pointSpriteBatch.begin();
			billboardBatch.begin();
			modelInstanceParticleBatch.begin();
			for (ParticleController controller : effect.getControllers()) 
			{
				if (isEnabled(controller)) {
					controller.draw();
					activeCount += controller.emitter.activeCount;
				}
			}
			modelInstanceParticleBatch.end();
			billboardBatch.end();
			pointSpriteBatch.end();
			
			//Draw
			modelBatch.render(pointSpriteBatch, environment);
			modelBatch.render(billboardBatch, environment);
			modelBatch.render(modelInstanceParticleBatch, environment);
			modelBatch.end();
			ui.draw();
		}

		@Override
		public boolean touchDown (int screenX, int screenY, int pointer, int button) {
			//gainFocus();
			return false;
		}
		
		public boolean touchUp (int x, int y, int pointer, int button) {
			//lostFocus();
			return false;
		}
		
		@Override
		public boolean scrolled (int amount) {
			//gainFocus();
			return false;
		}
		
		public void gainFocus () {
			lwjglCanvas.getCanvas().requestFocus();
			dispatchEvent(new WindowEvent(ParticleEditor3D.this, WindowEvent.WINDOW_LOST_FOCUS));
			//dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_GAINED_FOCUS));
			//lwjglCanvas.requestFocusInWindow();
		}

		public void lostFocus () {
			//dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_LOST_FOCUS));
			dispatchEvent(new WindowEvent(ParticleEditor3D.this, WindowEvent.WINDOW_GAINED_FOCUS));
			ParticleEditor3D.this.requestFocusInWindow();
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
	}

	static class ParticleData {
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

	public PointSpriteBatch getPointSpriteBatch () {
		return renderer.pointSpriteBatch;
	}

	public BillboardBatch getBillboardBatch () {
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
		Array<TextureAtlas> atlases = assetManager.get(TextureAtlas.class, new Array<TextureAtlas>());
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
		assetManager.get(ParticleEffect.class, out);
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
			loader.save(effect, new ParticleEffectSaveParameter(file, assetManager, getPointSpriteBatch(), getBillboardBatch(), getModelInstanceParticleBatch()));
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
				new ParticleEffectLoader.ParticleEffectLoadParameter(getPointSpriteBatch(), getBillboardBatch(), getModelInstanceParticleBatch()));
			loadedEffect = loadedEffect.copy();
			loadedEffect.init();
			if(replaceCurrentWorkspace){
				effect = loadedEffect;
				particleData.clear();
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

}
