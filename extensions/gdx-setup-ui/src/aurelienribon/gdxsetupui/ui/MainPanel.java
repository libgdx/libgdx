package aurelienribon.gdxsetupui.ui;

import aurelienribon.gdxsetupui.LibraryDef;
import aurelienribon.gdxsetupui.ui.panels.AboutPanel;
import aurelienribon.gdxsetupui.ui.panels.AdvancedSettingsPanel;
import aurelienribon.gdxsetupui.ui.panels.ClasspathsPanel;
import aurelienribon.gdxsetupui.ui.panels.ConfigSetupPanel;
import aurelienribon.gdxsetupui.ui.panels.ConfigUpdatePanel;
import aurelienribon.gdxsetupui.ui.panels.GoPanel;
import aurelienribon.gdxsetupui.ui.panels.HelpFixHtmlPanel;
import aurelienribon.gdxsetupui.ui.panels.HelpImportPanel;
import aurelienribon.gdxsetupui.ui.panels.LibraryInfoPanel;
import aurelienribon.gdxsetupui.ui.panels.LibrarySelectionPanel;
import aurelienribon.gdxsetupui.ui.panels.PreviewPanel;
import aurelienribon.gdxsetupui.ui.panels.ProcessSetupPanel;
import aurelienribon.gdxsetupui.ui.panels.ProcessUpdatePanel;
import aurelienribon.gdxsetupui.ui.panels.TaskPanel;
import aurelienribon.slidinglayout.SLAnimator;
import aurelienribon.slidinglayout.SLConfig;
import aurelienribon.slidinglayout.SLKeyframe;
import aurelienribon.slidinglayout.SLPanel;
import static aurelienribon.slidinglayout.SLSide.*;
import aurelienribon.ui.components.Button;
import aurelienribon.ui.components.PaintedPanel;
import aurelienribon.ui.css.Style;
import aurelienribon.utils.HttpUtils;
import aurelienribon.utils.HttpUtils.DownloadListener;
import aurelienribon.utils.HttpUtils.DownloadTask;
import aurelienribon.utils.Res;
import aurelienribon.utils.SwingUtils;
import aurelienribon.utils.VersionLabel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JLabel;
import org.apache.commons.io.IOUtils;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class MainPanel extends PaintedPanel {
	// Panels
	private final ConfigSetupPanel configSetupPanel = new ConfigSetupPanel(this);
	private final ConfigUpdatePanel configUpdatePanel = new ConfigUpdatePanel(this);
	private final LibrarySelectionPanel librarySelectionPanel = new LibrarySelectionPanel(this);
	private final PreviewPanel previewPanel = new PreviewPanel();
	private final GoPanel goPanel = new GoPanel(this);
	private final TaskPanel taskPanel = new TaskPanel();
	private final AdvancedSettingsPanel advancedSettingsPanel = new AdvancedSettingsPanel();
	private final LibraryInfoPanel libraryInfoPanel = new LibraryInfoPanel(this);
	private final ClasspathsPanel classpathsPanel = new ClasspathsPanel(this);
	private final ProcessSetupPanel processSetupPanel = new ProcessSetupPanel(this);
	private final ProcessUpdatePanel processUpdatePanel = new ProcessUpdatePanel(this);
	private final AboutPanel aboutPanel = new AboutPanel(this);
	private final HelpImportPanel helpImportPanel = new HelpImportPanel(this);
	private final HelpFixHtmlPanel helpFixHtmlPanel = new HelpFixHtmlPanel(this);

	// Start panel components
	private final JLabel startLogoLabel = new JLabel(Res.getImage("gfx/logo.png"));
	private final JLabel startQuestionLabel = new JLabel("<html>Do you want to create"
		+ " a new project, or to update the libraries of an existing one?");
	private final Button startSetupBtn = new Button() {{setText("Create");}};
	private final Button startUpdateBtn = new Button() {{setText("Update");}};

	// Misc components
	private final VersionLabel versionLabel = new VersionLabel();
	private final Button changeModeBtn = new Button() {{setText("Change mode");}};

	// SlidingLayout
	private final SLPanel rootPanel = new SLPanel();
	private final float transitionDuration = 0.5f;
	private final int gap = 10;

	public MainPanel() {
		SwingUtils.importFont(Res.getStream("fonts/SquareFont.ttf"));
		setLayout(new BorderLayout());
		add(rootPanel, BorderLayout.CENTER);

		HttpUtils.setReferer("http://aurelienribon-dev.com/gdx-setup-ui");

		versionLabel.initAndCheck("3.0.0", "versions",
			"https://raw.github.com/libgdx/libgdx/master/extensions/gdx-setup-ui/config/config.txt",
			"https://github.com/AurelienRibon/gdx-setup-ui/downloads");

		initUI();
		initLibgdx();
		initStyle();
		initConfigurations();
		rootPanel.initialize(initCfg);

		SLAnimator.start();
		rootPanel.setTweenManager(SLAnimator.createTweenManager());
		taskPanel.setTweenManager(SLAnimator.createTweenManager());

		if (Ctx.testLibUrl != null) Ctx.libs.addUrl("__test_url__", Ctx.testLibUrl);
		if (Ctx.testLibDef != null) Ctx.libs.addDef("__test_def__", Ctx.testLibDef);
		if (Ctx.testLibDef != null) librarySelectionPanel.rebuildLibraries();

		SwingUtils.addWindowListener(this, new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				DownloadTask task = Ctx.libs.downloadConfigFile();
				task.addListener(configFileDownloadListener);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				Ctx.libs.cleanUpDownloads();
			}
		});
	}

	private void initLibgdx() {
		try {
			String rawDef = IOUtils.toString(Res.getStream("libgdx.txt"));
			LibraryDef def = new LibraryDef(rawDef);
			Ctx.libs.addDef("libgdx", def);
			Ctx.cfgSetup.libraries.add("libgdx");
			Ctx.cfgUpdate.libraries.add("libgdx");
			librarySelectionPanel.initializeLibgdx();
		} catch (IOException ex) {
			assert false;
		}
	}

	private void initUI() {
		startSetupBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {showSetupView();}
		});

		startUpdateBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {showUpdateView();}
		});

		changeModeBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {showInitView();}
		});

		JLabel aboutLabel = new JLabel("About this app >");
		Style.registerCssClasses(aboutLabel, ".linkLabel");
		aboutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		versionLabel.setLayout(new BorderLayout());
		versionLabel.add(aboutLabel, BorderLayout.EAST);

		aboutLabel.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {showAboutPanel();}
		});
	}

	private final DownloadListener configFileDownloadListener = new DownloadListener() {
		@Override
		public void onComplete() {
			for (String name : Ctx.libs.getNames()) {
				DownloadTask task = Ctx.libs.downloadDef(name);

				task.addListener(new DownloadListener() {
					@Override public void onComplete() {
						librarySelectionPanel.rebuildLibraries();
					}
				});
			}
		}
	};

	// -------------------------------------------------------------------------
	// Style
	// -------------------------------------------------------------------------

	private void initStyle() {
		Style.registerCssClasses(this, ".rootPanel");
		Style.registerCssClasses(configSetupPanel, ".groupPanel", "#configSetupPanel");
		Style.registerCssClasses(configUpdatePanel, ".groupPanel", "#configUpdatePanel");
		Style.registerCssClasses(librarySelectionPanel, ".groupPanel", "#librarySelectionPanel");
		Style.registerCssClasses(previewPanel, ".groupPanel", "#previewPanel");
		Style.registerCssClasses(goPanel, ".groupPanel", "#goPanel");
		Style.registerCssClasses(advancedSettingsPanel, ".groupPanel", "#advancedSettingsPanel");
		Style.registerCssClasses(libraryInfoPanel, ".groupPanel", "#libraryInfoPanel");
		Style.registerCssClasses(classpathsPanel, ".groupPanel", "#classpathsPanel");
		Style.registerCssClasses(processSetupPanel, ".groupPanel", "#processSetupPanel");
		Style.registerCssClasses(processUpdatePanel, ".groupPanel", "#processUpdatePanel");
		Style.registerCssClasses(aboutPanel, ".groupPanel", "#aboutPanel");
		Style.registerCssClasses(helpImportPanel, ".groupPanel", "#helpImportPanel");
		Style.registerCssClasses(helpFixHtmlPanel, ".groupPanel", "#helpFixHtmlPanel");
		Style.registerCssClasses(startQuestionLabel, ".startQuestionLabel");
		Style.registerCssClasses(startSetupBtn, ".startButton");
		Style.registerCssClasses(startUpdateBtn, ".startButton");
		Style.registerCssClasses(versionLabel, ".versionLabel");
		Style.registerCssClasses(changeModeBtn, ".bold", ".center");

		Component[] targets = new Component[] {
			this, configSetupPanel, configUpdatePanel, librarySelectionPanel,
			previewPanel, goPanel, taskPanel, advancedSettingsPanel,
			libraryInfoPanel, classpathsPanel, processSetupPanel, processUpdatePanel,
			aboutPanel, helpImportPanel, helpFixHtmlPanel,
			changeModeBtn, versionLabel, startQuestionLabel, startSetupBtn, startUpdateBtn
		};

		Style style = new Style(Res.getUrl("css/style.css"));
		for (Component target : targets) Style.apply(target, style);
	}

	// -------------------------------------------------------------------------
	// Commands
	// -------------------------------------------------------------------------

	public void launchUpdateProcess() {
		processUpdatePanel.launch();
	}

	// -------------------------------------------------------------------------
	// Configurations
	// -------------------------------------------------------------------------

	private SLConfig initCfg, setupCfg, updateCfg;
	private SLConfig libraryInfoCfg;
	private SLConfig setupAdvSettingsCfg;
	private SLConfig setupProcessCfg;
	private SLConfig updateAdvSettingsCfg;
	private SLConfig updateProcessCfg;
	private SLConfig aboutCfg;
	private SLConfig helpImportCfg;
	private SLConfig helpFixHtmlCfg;

	private String currentLibraryInfo;

	private void initConfigurations() {
		initCfg = new SLConfig(rootPanel)
			.gap(gap, gap)
			.row(1f).row(30).col(1f)
			.beginGrid(0, 0)
				.row(startLogoLabel.getPreferredSize().height)
				.row(1f)
				.col(1f)
				.place(0, 0, startLogoLabel)
				.beginGrid(1, 0)
					.row(1f).row(50).row(80).row(1f)
					.col(1f).col(400).col(1f)
					.place(1, 1, startQuestionLabel)
					.beginGrid(2, 1)
						.row(1f)
						.col(1f).col(1f)
						.place(0, 0, startSetupBtn)
						.place(0, 1, startUpdateBtn)
					.endGrid()
				.endGrid()
			.endGrid()
			.place(1, 0, taskPanel);

		setupCfg = new SLConfig(rootPanel)
			.gap(gap, gap)
			.row(1f).row(30).col(1f)
			.beginGrid(0, 0)
				.row(1f).col(1f).col(1f).col(1f)
				.beginGrid(0, 0)
					.row(configSetupPanel.getPreferredSize().height)
					.row(versionLabel.getPreferredSize().height)
					.col(1f)
					.place(0, 0, configSetupPanel)
					.place(1, 0, versionLabel)
				.endGrid()
				.beginGrid(0, 1)
					.row(1f)
					.col(1f)
					.place(0, 0, librarySelectionPanel)
				.endGrid()
				.beginGrid(0, 2)
					.row(1f)
					.row(goPanel.getPreferredSize().height)
					.col(1f)
					.place(0, 0, previewPanel)
					.place(1, 0, goPanel)
				.endGrid()
			.endGrid()
			.beginGrid(1, 0)
				.row(1f).col(100).col(1f)
				.place(0, 0, changeModeBtn)
				.place(0, 1, taskPanel)
			.endGrid();

		updateCfg = new SLConfig(rootPanel)
			.gap(gap, gap)
			.row(1f).row(30).col(1f)
			.beginGrid(0, 0)
				.row(1f).col(1f).col(1f).col(1f)
				.beginGrid(0, 0)
					.row(configUpdatePanel.getPreferredSize().height)
					.row(versionLabel.getPreferredSize().height)
					.col(1f)
					.place(0, 0, configUpdatePanel)
					.place(1, 0, versionLabel)
				.endGrid()
				.beginGrid(0, 1)
					.row(1f)
					.col(1f)
					.place(0, 0, librarySelectionPanel)
				.endGrid()
				.beginGrid(0, 2)
					.row(goPanel.getPreferredSize().height)
					.col(1f)
					.place(0, 0, goPanel)
				.endGrid()
			.endGrid()
			.beginGrid(1, 0)
				.row(1f).col(100).col(1f)
				.place(0, 0, changeModeBtn)
				.place(0, 1, taskPanel)
			.endGrid();

		libraryInfoCfg = new SLConfig(rootPanel)
			.gap(gap, gap)
			.row(1f).row(30).col(1f)
			.beginGrid(0, 0)
				.row(1f).col(1f).col(2f)
				.place(0, 0, librarySelectionPanel)
				.place(0, 1, libraryInfoPanel)
			.endGrid()
			.place(1, 0, taskPanel);

		setupAdvSettingsCfg = new SLConfig(rootPanel)
			.gap(gap, gap)
			.row(1f).col(1f).col(2f)
			.beginGrid(0, 0)
				.row(configSetupPanel.getPreferredSize().height)
				.col(1f)
				.place(0, 0, configSetupPanel)
			.endGrid()
			.place(0, 1, advancedSettingsPanel);

		setupProcessCfg = new SLConfig(rootPanel)
			.gap(gap, gap)
			.row(1f).col(2f).col(1f)
			.beginGrid(0, 1)
				.row(1f).col(1f)
				.place(0, 0, previewPanel)
			.endGrid()
			.place(0, 0, processSetupPanel);

		updateAdvSettingsCfg = new SLConfig(rootPanel)
			.gap(gap, gap)
			.row(1f).col(1f).col(2f)
			.beginGrid(0, 0)
				.row(configUpdatePanel.getPreferredSize().height)
				.col(1f)
				.place(0, 0, configUpdatePanel)
			.endGrid()
			.place(0, 1, advancedSettingsPanel);

		updateProcessCfg = new SLConfig(rootPanel)
			.gap(gap, gap)
			.row(1f).col(2f).col(1f)
			.place(0, 0, classpathsPanel)
			.place(0, 1, processUpdatePanel);

		aboutCfg = new SLConfig(rootPanel)
			.gap(250, 100)
			.row(1f).col(1f)
			.place(0, 0, aboutPanel);

		helpImportCfg = new SLConfig(rootPanel)
			.gap(gap, gap)
			.row(1f).col(1f)
			.place(0, 0, helpImportPanel);

		helpFixHtmlCfg = new SLConfig(rootPanel)
			.gap(gap, gap)
			.row(1f).col(1f)
			.place(0, 0, helpFixHtmlPanel);
	}

	public void showSetupView() {
		Ctx.mode = Ctx.Mode.SETUP;
		Ctx.fireModeChangedChanged();

		rootPanel.createTransition()
			.push(new SLKeyframe(setupCfg, transitionDuration)
				.setStartSideForNewCmps(RIGHT)
				.setStartSide(LEFT, changeModeBtn)
				.setEndSideForOldCmps(LEFT))
			.play();
	}

	public void showUpdateView() {
		Ctx.mode = Ctx.Mode.UPDATE;
		Ctx.fireModeChangedChanged();

		rootPanel.createTransition()
			.push(new SLKeyframe(updateCfg, transitionDuration)
				.setStartSideForNewCmps(RIGHT)
				.setStartSide(LEFT, changeModeBtn)
				.setEndSideForOldCmps(LEFT))
			.play();
	}

	public void showInitView() {
		Ctx.mode = Ctx.Mode.INIT;
		Ctx.fireModeChangedChanged();

		rootPanel.createTransition()
			.push(new SLKeyframe(initCfg, transitionDuration)
				.setStartSideForNewCmps(LEFT)
				.setEndSideForOldCmps(RIGHT)
				.setEndSide(LEFT, changeModeBtn))
			.play();
	}

	public boolean showAdvancedSettings() {
		switch (Ctx.mode) {
			case SETUP:
				return rootPanel.createTransition()
					.push(new SLKeyframe(setupAdvSettingsCfg, transitionDuration)
						.setEndSideForOldCmps(BOTTOM)
						.setStartSideForNewCmps(TOP))
					.play();

			case UPDATE:
				return rootPanel.createTransition()
					.push(new SLKeyframe(updateAdvSettingsCfg, transitionDuration)
						.setEndSideForOldCmps(BOTTOM)
						.setStartSideForNewCmps(TOP))
					.play();
		}

		return false;
	}

	public boolean hideAdvancedSettings() {
		switch (Ctx.mode) {
			case SETUP:
				return rootPanel.createTransition()
					.push(new SLKeyframe(setupCfg, transitionDuration)
						.setEndSideForOldCmps(TOP)
						.setStartSideForNewCmps(BOTTOM))
					.play();

			case UPDATE:
				return rootPanel.createTransition()
					.push(new SLKeyframe(updateCfg, transitionDuration)
						.setEndSideForOldCmps(TOP)
						.setStartSideForNewCmps(BOTTOM))
					.play();
		}

		return false;
	}

	public void showLibraryInfo(String libraryName) {
		if (currentLibraryInfo != null) {
			if (currentLibraryInfo.equals(libraryName)) {
				hideLibraryInfo();
			} else {
				currentLibraryInfo = libraryName;
				libraryInfoPanel.setup(libraryName);
			}
			return;
		}

		currentLibraryInfo = libraryName;
		libraryInfoPanel.setup(libraryName);

		switch (Ctx.mode) {
			case SETUP:
				rootPanel.createTransition()
					.push(new SLKeyframe(libraryInfoCfg, transitionDuration)
						.setEndSide(LEFT, configSetupPanel, versionLabel, changeModeBtn)
						.setEndSide(RIGHT, previewPanel, goPanel)
						.setStartSide(TOP, libraryInfoPanel)
						.setDelay(transitionDuration, libraryInfoPanel))
					.play();
				break;

			case UPDATE:
				rootPanel.createTransition()
					.push(new SLKeyframe(libraryInfoCfg, transitionDuration)
						.setEndSide(LEFT, configUpdatePanel, versionLabel, changeModeBtn)
						.setEndSide(RIGHT, goPanel)
						.setStartSide(TOP, libraryInfoPanel)
						.setDelay(transitionDuration, libraryInfoPanel))
					.play();
				break;
		}
	}

	public void hideLibraryInfo() {
		currentLibraryInfo = null;

		switch (Ctx.mode) {
			case SETUP:
				rootPanel.createTransition()
					.push(new SLKeyframe(setupCfg, transitionDuration)
						.setEndSide(RIGHT, libraryInfoPanel)
						.setStartSide(RIGHT, previewPanel, goPanel)
						.setStartSide(LEFT, configSetupPanel, versionLabel, changeModeBtn)
						.setDelay(transitionDuration, previewPanel, goPanel))
					.play();
				break;

			case UPDATE:
				rootPanel.createTransition()
					.push(new SLKeyframe(updateCfg, transitionDuration)
						.setEndSide(RIGHT, libraryInfoPanel)
						.setStartSide(RIGHT, goPanel)
						.setStartSide(LEFT, configUpdatePanel, versionLabel, changeModeBtn)
						.setDelay(transitionDuration, goPanel))
					.play();
				break;
		}
	}

	public void showProcessSetupPanel() {
		rootPanel.createTransition()
			.push(new SLKeyframe(setupProcessCfg, transitionDuration)
				.setEndSide(TOP, configSetupPanel, versionLabel, librarySelectionPanel)
				.setEndSide(BOTTOM, taskPanel, changeModeBtn, goPanel)
				.setStartSide(BOTTOM, processSetupPanel))
			.play();
	}

	public void hideGenerationCreatePanel() {
		rootPanel.createTransition()
			.push(new SLKeyframe(setupCfg, transitionDuration)
				.setEndSide(BOTTOM, processSetupPanel)
				.setStartSide(TOP, configSetupPanel, versionLabel, librarySelectionPanel)
				.setStartSide(BOTTOM, taskPanel, changeModeBtn, goPanel))
			.play();
	}

	public void showProcessUpdatePanel() {
		rootPanel.createTransition()
			.push(new SLKeyframe(updateProcessCfg, transitionDuration)
				.setEndSide(TOP, configUpdatePanel, versionLabel, librarySelectionPanel)
				.setEndSide(BOTTOM, taskPanel, changeModeBtn, goPanel)
				.setStartSide(BOTTOM, classpathsPanel)
				.setStartSide(TOP, processUpdatePanel))
			.play();
	}

	public void hideGenerationUpdatePanel() {
		rootPanel.createTransition()
			.push(new SLKeyframe(updateCfg, transitionDuration)
				.setEndSide(BOTTOM, classpathsPanel)
				.setEndSide(TOP, processUpdatePanel)
				.setStartSide(TOP, configUpdatePanel, versionLabel, librarySelectionPanel)
				.setStartSide(BOTTOM, taskPanel, changeModeBtn, goPanel))
			.play();
	}

	public void showAboutPanel() {
		rootPanel.createTransition()
			.push(new SLKeyframe(aboutCfg, transitionDuration)
				.setEndSideForOldCmps(LEFT)
				.setStartSideForNewCmps(RIGHT))
			.play();
	}

	public void hideAboutPanel() {
		switch (Ctx.mode) {
			case SETUP:
				rootPanel.createTransition()
					.push(new SLKeyframe(setupCfg, transitionDuration)
						.setEndSideForOldCmps(RIGHT)
						.setStartSideForNewCmps(LEFT))
					.play();

			case UPDATE:
				rootPanel.createTransition()
					.push(new SLKeyframe(updateCfg, transitionDuration)
						.setEndSideForOldCmps(RIGHT)
						.setStartSideForNewCmps(LEFT))
					.play();
		}
	}

	public void showHelpImportPanel() {
		rootPanel.createTransition()
			.push(new SLKeyframe(helpImportCfg, transitionDuration)
				.setEndSideForOldCmps(TOP)
				.setStartSideForNewCmps(BOTTOM))
			.play();
	}

	public void showHelpFixHtmlPanel() {
		rootPanel.createTransition()
			.push(new SLKeyframe(helpFixHtmlCfg, transitionDuration)
				.setEndSideForOldCmps(TOP)
				.setStartSideForNewCmps(BOTTOM))
			.play();
	}

	public void hideHelpPanel() {
		rootPanel.createTransition()
			.push(new SLKeyframe(setupProcessCfg, transitionDuration)
				.setEndSideForOldCmps(BOTTOM)
				.setStartSideForNewCmps(TOP))
			.play();
	}
}
