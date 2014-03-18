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
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
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
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.controllers.BillboardParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ModelInstanceParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ParticleControllerParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.PointSpriteParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.ModelInstanceColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.PointSpriteColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.FaceDirectionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.FaceDirectionInfluencer.ParticleControllerFaceDirectionInfluencer;
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
import com.badlogic.gdx.graphics.g3d.particles.influencers.FaceDirectionInfluencer.ModelInstanceFaceDirectionInfluencer;
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
import com.badlogic.gdx.utils.Array;

public class ParticleEditor3D extends JFrame implements AssetErrorListener {
	public static final String 	DEFAULT_FONT = "default.fnt",
											DEFAULT_BILLBOARD_PARTICLE = "pre_particle.png",
											DEFAULT_MODEL_PARTICLE = "monkey.g3db",
											DEFAULT_PFX = "default.pfx";
	
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
		new InfluencerWrapper("Face Direction Influencer", ModelInstanceFaceDirectionInfluencer.class),
		new InfluencerWrapper("Velocity Influencer", ModelInstanceVelocityInfluencer.class)
	};
	
	static InfluencerWrapper[] particleControllerInfluencers = new InfluencerWrapper[]{
		new InfluencerWrapper("Single Particle Controller Influencer", ParticleControllerSingleInfluencer.class),
		new InfluencerWrapper("Random Particle Controller Influencer", ParticleControllerRandomInfluencer.class),
		new InfluencerWrapper("Scale Influencer", ParticleControllerScaleInfluencer.class),
		new InfluencerWrapper("Spawn Influencer", ParticleControllerSpawnInfluencer.class),
		new InfluencerWrapper("Face Direction Influencer", ParticleControllerFaceDirectionInfluencer.class),
		new InfluencerWrapper("Velocity Influencer", ParticleControllerVelocityInfluencer.class)
	};
	
	
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
	TextureAtlas currentAtlas;
	Texture texture;
	boolean isUsingAtlas = false;
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
				editRowsPanel.removeAll();
				influencerBox.removeAllItems();
				rowsPanel.removeAll();
				addEditorRow(new NumericPanel(ParticleEditor3D.this, fovValue, "Field of View", ""));
				addEditorRow(new NumericPanel(ParticleEditor3D.this, deltaMultiplier, "Delta multiplier", ""));
				addEditorRow(new GradientPanel(ParticleEditor3D.this,backgroundColor, "Background color", "", true));
				addEditorRow(new DrawPanel(ParticleEditor3D.this, "Draw", ""));
				addEditorRow(new TextureLoaderPanel(ParticleEditor3D.this, "Texture", ""));
				editRowsPanel.repaint();
				

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
					panel = getPanel(controller.batch);
					if(panel != null) 
						addRow(panel);
					addRow(getPanel(controller.emitter));
					for(int i=0, c = controller.influencers.size; i < c; ++i){
						Influencer influencer = (Influencer)controller.influencers.get(i);
						panel = getPanel(influencer);
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
		if(influencer instanceof FaceDirectionInfluencer){
			return new InfluencerPanel<FaceDirectionInfluencer>(this, (FaceDirectionInfluencer) influencer, 
				"Face Direction Influencer", "Let the particle face its traveling direction, (local Z axis will match velocity direction)") {};
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
		row.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, java.awt.Color.black));
		panel.add(row, new GridBagConstraints(0, -1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
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

	class AppRenderer implements ApplicationListener, InputProcessor {
		private float maxActiveTimer;
		private int maxActive, lastMaxActive;
		private int activeCount;
		private BitmapFont font;
		private SpriteBatch spriteBatch;
		private ModelBatch modelBatch;
		PointSpriteBatch pointSpriteBatch;
		BillboardBatch billboardBatch;
		ModelInstanceParticleBatch modelInstanceParticleBatch;
		private CameraInputController cameraInputController;
		private boolean isDrawXYZ, isDrawXZPlane;
		private Array<Model> models;
		private ModelInstance xyzInstance, xzPlaneInstance;
		private Environment environment;

		public void create () {
			if (spriteBatch != null) return;

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
			assetManager.get(DEFAULT_MODEL_PARTICLE, Model.class).materials.get(0).set(new BlendingAttribute(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA, 1));

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
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			Gdx.gl.glClearColor(colors[0], colors[1], colors[2], 0);

			worldCamera.fieldOfView = fovValue.getValue();
			worldCamera.update();

			modelBatch.begin(worldCamera);
			if(isDrawXYZ) modelBatch.render(xyzInstance);
			if(isDrawXZPlane) modelBatch.render(xzPlaneInstance);

			activeCount = 0;
			pointSpriteBatch.begin();
			billboardBatch.begin();
			modelInstanceParticleBatch.begin();
			for (ParticleController controller : effect.getControllers()) 
			{
				if (isEnabled(controller)) {	
					controller.update(delta);
					controller.draw();
					activeCount += controller.emitter.activeCount;
				}
			}
			modelInstanceParticleBatch.end();
			billboardBatch.end();
			pointSpriteBatch.end();

			modelBatch.render(pointSpriteBatch);
			modelBatch.render(billboardBatch);
			modelBatch.render(modelInstanceParticleBatch);
			
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

	public <T> T load (String resource, Class<T> type, AssetLoader loader, AssetLoaderParameters<T> params) {	
		AssetLoader<T, AssetLoaderParameters<T>> currentLoader = assetManager.getLoader(type);
		if(loader != null)
			assetManager.setLoader(type, loader); 
		assetManager.load(resource, type, params);
		assetManager.finishLoading();
		String resolvedPath = new String(resource).replaceAll("\\\\", "/");;
		T res = assetManager.get(resolvedPath);
		if(currentLoader != null)
			assetManager.setLoader(type, currentLoader);
		return res;
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
	
	public TextureAtlas getAtlas(){
		return currentAtlas;
	}

	public void setAtlas(TextureAtlas atlas){
		currentAtlas = atlas;
		setTexture(atlas.getTextures().first());
	}
	
	public void setTexture(Texture texture){
		this.texture = texture;
		renderer.billboardBatch.setTexture(texture);
		renderer.pointSpriteBatch.setTexture(texture);
	}
	
	public Texture getTexture(){
		return texture;
	}

	public boolean isUsingDefaultTexture () {
		return texture == assetManager.get(DEFAULT_BILLBOARD_PARTICLE, Texture.class);
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

	/** Remove all duplicates. uses == to compare items.*/
	private <T> void removeDuplicates (Array<T> out) {
		for(int i=0; i < out.size; ++i){
			T it = out.get(i);
			for(int j=i+1; j < out.size;){
				if(out.get(j) == it){
					out.removeIndex(j);
					continue;
				}
				++j;
			}
		}
	}

}
