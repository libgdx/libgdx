
package com.badlogic.gdx.graphics.particles;

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
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglGraphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Sprite;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ParticleEditor extends JFrame {
	LwjglCanvas lwjglCanvas;
	JPanel rowsPanel;
	EffectPanel effectPanel;
	private JSplitPane splitPane;

	ParticleEffect effect = new ParticleEffect();
	final HashMap<ParticleEmitter, ParticleData> particleData = new HashMap();

	public ParticleEditor () {
		super("Particle Editor");

		lwjglCanvas = new LwjglCanvas(new Renderer(), false);
		addWindowListener(new WindowAdapter() {
			public void windowClosed (WindowEvent event) {
				System.exit(0);
				// Gdx.app.quit();
			}
		});

		initializeComponents();

		setSize(950, 950);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	void reloadRows () {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				rowsPanel.removeAll();
				ParticleEmitter emitter = getEmitter();
				addRow(new ImagePanel(ParticleEditor.this));
				addRow(new RangedNumericPanel("Delay", emitter.getDelay()));
				addRow(new RangedNumericPanel("Duration", emitter.getDuration()));
				addRow(new CountPanel(ParticleEditor.this));
				addRow(new ScaledNumericPanel("Emission", "Duration", emitter.getEmission()));
				addRow(new ScaledNumericPanel("Life", "Duration", emitter.getLife()));
				addRow(new ScaledNumericPanel("Life Offset", "Duration", emitter.getLifeOffset()));
				addRow(new RangedNumericPanel("X Offset", emitter.getXOffsetValue()));
				addRow(new RangedNumericPanel("Y Offset", emitter.getYOffsetValue()));
				addRow(new SpawnPanel(emitter.getSpawnShape(), ParticleEditor.this));
				addRow(new ScaledNumericPanel("Spawn Width", "Duration", emitter.getSpawnWidth()));
				addRow(new ScaledNumericPanel("Spawn Height", "Duration", emitter.getSpawnHeight()));
				addRow(new ScaledNumericPanel("Size", "Life", emitter.getScale()));
				addRow(new ScaledNumericPanel("Velocity", "Life", emitter.getVelocity()));
				addRow(new ScaledNumericPanel("Angle", "Life", emitter.getAngle()));
				addRow(new ScaledNumericPanel("Rotation", "Life", emitter.getRotation()));
				addRow(new ScaledNumericPanel("Wind", "Life", emitter.getWind()));
				addRow(new ScaledNumericPanel("Gravity", "Life", emitter.getGravity()));
				addRow(new GradientPanel("Tint", emitter.getTint()));
				addRow(new PercentagePanel("Transparency", "Life", emitter.getTransparency()));
				addRow(new OptionsPanel(ParticleEditor.this));
				for (Component component : rowsPanel.getComponents())
					if (component instanceof EditorPanel) ((EditorPanel)component).update(ParticleEditor.this);
				rowsPanel.repaint();
			}
		});
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
					url = ParticleEditor.class.getResource(imagePath);
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
		// {
		// JMenuBar menuBar = new JMenuBar();
		// setJMenuBar(menuBar);
		// JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		// JMenu fileMenu = new JMenu("File");
		// menuBar.add(fileMenu);
		// }
		splitPane = new JSplitPane();
		splitPane.setUI(new BasicSplitPaneUI() {
			public void paint (Graphics g, JComponent jc) {
			}
		});
		splitPane.setDividerSize(4);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		{
			JPanel propertiesPanel = new JPanel(new GridBagLayout());
			splitPane.add(propertiesPanel, JSplitPane.RIGHT);
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
		splitPane.setDividerLocation(325);
	}

	class Renderer implements ApplicationListener, InputProcessor {
		private float maxActiveTimer;
		private int maxActive, lastMaxActive;
		private boolean mouseDown;
		private int activeCount;
		private int mouseX, mouseY;
		private BitmapFont font;
		private SpriteBatch spriteBatch;
		private Sprite bgImage; // BOZO - Add setting background image to UI.

		public void create () {
			if (spriteBatch != null) return;

			((LwjglGraphics)Gdx.graphics).setEnforcePotImages(false);

			spriteBatch = new SpriteBatch();

			font = new BitmapFont(Gdx.files.getFileHandle("default.fnt", FileType.Internal), Gdx.files.getFileHandle(
				"default.png", FileType.Internal), true);
			effectPanel.newEmitter("Untitled", true);
			// if (resources.openFile("/editor-bg.png") != null) bgImage = new Image(gl, "/editor-bg.png");
			Gdx.input.setInputProcessor(this);
		}

		@Override public void resize (int width, int height) {
			Gdx.gl.glViewport(0, 0, width, height);
			spriteBatch.getProjectionMatrix().setToOrtho(0, width, height, 0, 0, 1);

			effect.setPosition(width / 2, height / 2);
		}

		public void render () {
			int viewWidth = Gdx.graphics.getWidth();
			int viewHeight = Gdx.graphics.getHeight();

			float delta = Gdx.graphics.getDeltaTime();

			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

			spriteBatch.begin();
			spriteBatch.enableBlending();
			spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

			if (bgImage != null) {
				bgImage.setPosition(viewWidth / 2 - bgImage.getWidth() / 2, viewHeight / 2 - bgImage.getHeight() / 2);
				bgImage.draw(spriteBatch);
			}

			activeCount = 0;
			boolean complete = true;
			for (ParticleEmitter emitter : effect.getEmitters()) {
				if (emitter.getSprite() == null && emitter.getImagePath() != null) loadImage(emitter);
				boolean enabled = isEnabled(emitter);
				if (enabled) {
					if (emitter.getSprite() != null) emitter.draw(spriteBatch, delta);
					activeCount += emitter.getActiveCount();
					if (emitter.isContinuous()) complete = false;
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

			if (mouseDown) {
				// gl.drawLine(mouseX - 6, mouseY, mouseX + 5, mouseY);
				// gl.drawLine(mouseX, mouseY - 5, mouseX, mouseY + 6);
			}

			font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 10, Color.WHITE);
			font.draw(spriteBatch, "Count: " + activeCount, 10, 30, Color.WHITE);
			font.draw(spriteBatch, "Max: " + lastMaxActive, 10, 50, Color.WHITE);
			font.draw(spriteBatch, (int)(getEmitter().getPercentComplete() * 100) + "%", 10, 70, Color.WHITE);

			spriteBatch.end();

			// gl.drawLine((int)(viewWidth * getCurrentParticles().getPercentComplete()), viewHeight - 1, viewWidth, viewHeight -
			// 1);
		}

		private void loadImage (ParticleEmitter emitter) {
			final String imagePath = emitter.getImagePath();
			String imageName = new File(imagePath.replace('\\', '/')).getName();
			try {
				FileHandle file;
				if (imagePath.equals("particle.png"))
					file = Gdx.files.classpath(imagePath);
				else
					file = Gdx.files.absolute(imagePath);
				emitter.setSprite(new Sprite(Gdx.graphics.newTexture(file, TextureFilter.Linear, TextureFilter.Linear,
					TextureWrap.ClampToEdge, TextureWrap.ClampToEdge)));
			} catch (GdxRuntimeException ex) {
				ex.printStackTrace();
				EventQueue.invokeLater(new Runnable() {
					public void run () {
						JOptionPane.showMessageDialog(ParticleEditor.this, "Error loading image:\n" + imagePath);
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

		public boolean touchDown (int x, int y, int pointer) {
			effect.setPosition(x, y);
			return false;
		}

		public boolean touchUp (int x, int y, int pointer) {
			return false;
		}

		public boolean touchDragged (int x, int y, int pointer) {
			effect.setPosition(x, y);
			return false;
		}

		@Override public void dispose () {
		}

		@Override public void pause () {
		}

		@Override public void resume () {
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
			}
		});
		new ParticleEditor();
	}
}
