package aurelienribon.gdxsetupui.ui;

import aurelienribon.gdxsetupui.LibraryDef;
import aurelienribon.gdxsetupui.LibraryManager;
import aurelienribon.gdxsetupui.ProjectSetupConfiguration;
import aurelienribon.gdxsetupui.ProjectUpdateConfiguration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class Ctx {
	public static enum Mode {INIT, SETUP, UPDATE}
	public static Mode mode = Mode.INIT;

	public static final ProjectSetupConfiguration cfgSetup = new ProjectSetupConfiguration();
	public static final ProjectUpdateConfiguration cfgUpdate = new ProjectUpdateConfiguration();
	public static final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	public static final LibraryManager libs = new LibraryManager("http://libgdx.googlecode.com/svn/trunk/extensions/gdx-setup-ui/config/config.txt");
	public static String testLibUrl = null;
	public static LibraryDef testLibDef = null;

	// -------------------------------------------------------------------------
	// Events
	// -------------------------------------------------------------------------

	public static class Listener {
		public void modeChanged() {}
		public void cfgSetupChanged() {}
		public void cfgUpdateChanged() {}
	}

	public static void fireModeChangedChanged() {for (Listener l : listeners) l.modeChanged();}
	public static void fireCfgSetupChanged() {for (Listener l : listeners) l.cfgSetupChanged();}
	public static void fireCfgUpdateChanged() {for (Listener l : listeners) l.cfgUpdateChanged();}
}
