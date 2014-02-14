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
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.Emitter.GradientColorValue;
import com.badlogic.gdx.graphics.g3d.particles.Emitter.NumericValue;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEmitter;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ParticleEditor3D extends JFrame {
        public static final String DEFAULT_PARTICLE = "particle.png"; 
        LwjglCanvas lwjglCanvas;
        JPanel rowsPanel;
        JPanel editRowsPanel;
        EffectPanel effectPanel;
        private JSplitPane splitPane;
        PerspectiveCamera worldCamera;
        OrthographicCamera textCamera;
        NumericValue fovValue;
        NumericValue deltaMultiplier;
        GradientColorValue backgroundColor;
        Renderer renderer;

        ParticleEffect effect = new ParticleEffect();
        final HashMap<ParticleEmitter, ParticleData> particleData = new HashMap();

        public ParticleEditor3D () {
                super("Particle Editor 3D");

                lwjglCanvas = new LwjglCanvas(renderer = new Renderer(), true);
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
                                addEditorRow(new NumericPanel(fovValue, "Fielf of View", ""));
                                addEditorRow(new NumericPanel(deltaMultiplier, "Delta multiplier", ""));
                                addEditorRow(new GradientPanel(backgroundColor, "Background color", "", true));
                                addEditorRow(new DrawPanel(ParticleEditor3D.this, "Draw", ""));
                                
                                rowsPanel.removeAll();
                                ParticleEmitter emitter = getEmitter();
                                addRow(new ImagePanel(ParticleEditor3D.this, "Image", ""));
                                addRow(new CountPanel(ParticleEditor3D.this, "Count",
                                        "Min number of particles at all times, max number of particles allowed."));
                                addRow(new RangedNumericPanel(emitter.getDelay(), "Delay",
                                        "Time from beginning of effect to emission start, in milliseconds.", false));
                                addRow(new RangedNumericPanel(emitter.getDuration(), "Duration", "Time particles will be emitted, in milliseconds."));
                                addRow(new ScaledNumericPanel(emitter.getEmission(), "Duration", "Emission",
                                        "Number of particles emitted per second."));
                                addRow(new ScaledNumericPanel(emitter.getLife(), "Duration", "Life", "Time particles will live, in milliseconds."));
                                addRow(new ScaledNumericPanel(emitter.getLifeOffset(), "Duration", "Life Offset",
                                        "Particle starting life consumed, in milliseconds.", false));
                                addRow(new RangedNumericPanel(emitter.getXOffsetValue(), "X Offset",
                                        "Amount to offset a particle's starting X location, in world units.", false));
                                addRow(new RangedNumericPanel(emitter.getYOffsetValue(), "Y Offset",
                                        "Amount to offset a particle's starting Y location, in world units.", false));
                                addRow(new RangedNumericPanel(emitter.getZOffsetValue(), "Z Offset",
                                        "Amount to offset a particle's starting Z location, in world units.", false));
                                addRow(new FacingPanel(ParticleEditor3D.this, emitter.getFacingValue(), "Facing",
                                        "Controls the direction used to align the particle to the camera."));
                                addRow(new SpawnPanel(ParticleEditor3D.this, emitter.getSpawnShape(), "Spawn", "Shape used to spawn particles."));
                                addRow(new ScaledNumericPanel(emitter.getSpawnWidth(), "Duration", "Spawn Width",
                                        "Width of the spawn shape, in world units."));
                                addRow(new ScaledNumericPanel(emitter.getSpawnHeight(), "Duration", "Spawn Height",
                                        "Height of the spawn shape, in world units."));
                                addRow(new ScaledNumericPanel(emitter.getSpawnDepth(), "Duration", "Spawn Depth",
                                        "Depth of the spawn shape, in world units."));
                                addRow(new ScaledNumericPanel(emitter.getScaleValue(), "Life", "Size", "Particle size, in world units."));
                                addRow(new VelocityPanel(emitter.getVelocityValue(0), "Life", "Velocity 1", "Velocity vector applied to particle, in world units per second (ie. gravity, wind, etc...).", false));
                                addRow(new VelocityPanel(emitter.getVelocityValue(1), "Life", "Velocity 2", "Velocity vector applied to particle, in world units per second (ie. gravity, wind, etc...).", false));
                                addRow(new VelocityPanel(emitter.getVelocityValue(2), "Life", "Velocity 3", "Velocity vector applied to particle, in world units per second (ie. gravity, wind, etc...).", false));
                                addRow(new ScaledNumericPanel(emitter.getRotation(), "Life", "Rotation", "Particle rotation, in degrees.", false));                                
                                addRow(new GradientPanel(emitter.getTint(), "Tint", "", false));
                                addRow(new PercentagePanel(emitter.getTransparency(), "Life", "Transparency", ""));
                                addRow(new OptionsPanel(ParticleEditor3D.this, "Options", ""));
                                for (Component component : rowsPanel.getComponents())
                                        if (component instanceof EditorPanel) ((EditorPanel)component).update(ParticleEditor3D.this);
                                rowsPanel.repaint();
                        }
                });
        }

        void addEditorRow (JPanel row) {
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.black));
                editRowsPanel.add(row, new GridBagConstraints(0, -1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        }

        void addRow (JPanel row) {
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.black));
                rowsPanel.add(row, new GridBagConstraints(0, -1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        }

        public void setVisible (String name, boolean visible) {
                for (Component component : rowsPanel.getComponents())
                        if (component instanceof EditorPanel && ((EditorPanel)component).getName().equals(name)) component.setVisible(visible);
        }

        public ParticleEmitter getEmitter () {
                return effect.getEmitters().get(effectPanel.editIndex);
        }

        public ImageIcon getIcon (ParticleEmitter emitter) {
                ParticleData data = particleData.get(emitter);
                if (data == null) particleData.put(emitter, data = new ParticleData());
                String imagePath = emitter.getImagePath();
                if (data.icon == null && imagePath != null) {
                        try {
                                URL url;
                                File file = new File(imagePath);
                                if (file.exists())
                                        url = file.toURI().toURL();
                                else {
                                        url = ParticleEditor3D.class.getResource(imagePath);
                                        if (url == null) return null;
                                }
                                data.icon = new ImageIcon(url);
                        } catch (MalformedURLException ex) {
                                ex.printStackTrace();
                        }
                }
                return data.icon;
        }

        public void setIcon (ParticleEmitter emitters, ImageIcon icon) {
                ParticleData data = particleData.get(emitters);
                if (data == null) particleData.put(emitters, data = new ParticleData());
                data.icon = icon;
        }

        public void setEnabled (ParticleEmitter emitter, boolean enabled) {
                ParticleData data = particleData.get(emitter);
                if (data == null) particleData.put(emitter, data = new ParticleData());
                data.enabled = enabled;
                emitter.reset();
        }

        public boolean isEnabled (ParticleEmitter emitter) {
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

        class Renderer implements ApplicationListener, InputProcessor {
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

                public void create () {
                        if (spriteBatch != null) return;

                        Texture.setEnforcePotImages(false);

                        spriteBatch = new SpriteBatch();
                        spriteBatch.enableBlending();
                        modelBatch = new ModelBatch();
                        	
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

                        font = new BitmapFont(Gdx.files.getFileHandle("default.fnt", FileType.Internal), Gdx.files.getFileHandle("default.png",
                                FileType.Internal), true);
                        effectPanel.newExampleEmitter("Untitled", true);
                        
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
                        // if (resources.openFile("/editor-bg.png") != null) bgImage = new Image(gl, "/editor-bg.png");
                        Gdx.input.setInputProcessor(new InputMultiplexer(cameraInputController));
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
                	boolean complete = true;
                	for (ParticleEmitter emitter : effect.getEmitters()) 
                	{
                		if (emitter.getRegion() == null && emitter.getImagePath() != null) loadImage(emitter);
                		if (isEnabled(emitter)) 
                		{	
                			emitter.update(delta);
                			if (emitter.getRegion() != null) emitter.render(modelBatch);
                			activeCount += emitter.getActiveCount();
                			if (!emitter.isComplete()) complete = false;
                		}
                	}
                	if (complete) effect.start();

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
                	font.draw(spriteBatch, (int)(getEmitter().getPercentComplete() * 100) + "%", 5, 75);

                	spriteBatch.end();
                }

                private void loadImage (ParticleEmitter emitter) {
                        final String imagePath = emitter.getImagePath();
                        //String imageName = new File(imagePath.replace('\\', '/')).getName();
                        try {
                                FileHandle file;
                                if (imagePath.equals(ParticleEditor3D.DEFAULT_PARTICLE))
                                        file = Gdx.files.classpath(imagePath);
                                else
                                        file = Gdx.files.absolute(imagePath);
                                emitter.setRegionFromTexture(new Texture(file));
                        } catch (GdxRuntimeException ex) {
                                ex.printStackTrace();
                                EventQueue.invokeLater(new Runnable() {
                                        public void run () {
                                                JOptionPane.showMessageDialog(ParticleEditor3D.this, "Error loading image:\n" + imagePath);
                                        }
                                });
                                emitter.setImagePath(null);
                        }
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

		public Renderer getRenderer() {
			return renderer;
		}

}
