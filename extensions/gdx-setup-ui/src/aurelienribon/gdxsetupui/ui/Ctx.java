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
	public static final LibraryManager libs = new LibraryManager("https://raw.github.com/libgdx/libgdx/master/extensions/gdx-setup-ui/config/config.txt");
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