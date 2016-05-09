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

package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.badlogic.gdx.backends.gwt.preloader.Preloader.PreloaderCallback;
import com.badlogic.gdx.backends.gwt.preloader.Preloader.PreloaderState;
import com.badlogic.gdx.backends.gwt.soundmanager2.SoundManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.Window.Navigator;

/** Implementation of an {@link Application} based on GWT. Clients have to override {@link #getConfig()} and
 * {@link #createApplicationListener()}. Clients can override the default loading screen via
 * {@link #getPreloaderCallback()} and implement any loading screen drawing via GWT widgets.
 * @author mzechner */
public abstract class GwtApplication implements EntryPoint, Application {
	private ApplicationListener listener;
	GwtApplicationConfiguration config;
	GwtGraphics graphics;
	private GwtInput input;
	private GwtNet net;
	private Panel root = null;
	private TextArea log = null;
	private int logLevel = LOG_ERROR;
	private Array<Runnable> runnables = new Array<Runnable>();
	private Array<Runnable> runnablesHelper = new Array<Runnable>();
	private Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();
	private int lastWidth;
	private int lastHeight;
	Preloader preloader;
	private static AgentInfo agentInfo;
	private ObjectMap<String, Preferences> prefs = new ObjectMap<String, Preferences>();
	private Clipboard clipboard;
	LoadingListener loadingListener;

	/** @return the configuration for the {@link GwtApplication}. */
	public abstract GwtApplicationConfiguration getConfig ();

	
	public String getPreloaderBaseURL()
	{
		return GWT.getHostPageBaseURL() + "assets/";
	}
	
	@Override
	public ApplicationListener getApplicationListener() {
		return listener;
	}
	
	public abstract ApplicationListener createApplicationListener();
	
	@Override
	public void onModuleLoad () {
		GwtApplication.agentInfo = computeAgentInfo();
		this.listener = createApplicationListener();
		this.config = getConfig();
		this.log = config.log;

		addEventListeners();

		if (config.rootPanel != null) {
			this.root = config.rootPanel;
		} else {
			Element element = Document.get().getElementById("embed-" + GWT.getModuleName());
			if (element == null) {
				VerticalPanel panel = new VerticalPanel();
				panel.setWidth("" + config.width + "px");
				panel.setHeight("" + config.height + "px");
				panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				RootPanel.get().add(panel);
				RootPanel.get().setWidth("" + config.width + "px");
				RootPanel.get().setHeight("" + config.height + "px");
				this.root = panel;
			} else {
				VerticalPanel panel = new VerticalPanel();
				panel.setWidth("" + config.width + "px");
				panel.setHeight("" + config.height + "px");
				panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				element.appendChild(panel.getElement());
				root = panel;
			}
		}

		// initialize SoundManager2
		SoundManager.init(GWT.getModuleBaseURL(), 9, config.preferFlash, new SoundManager.SoundManagerCallback(){

			@Override
			public void onready () {
				final PreloaderCallback callback = getPreloaderCallback();
				preloader = createPreloader();
				preloader.preload("assets.txt", new PreloaderCallback() {
					@Override
					public void error (String file) {
						callback.error(file);
					}

					@Override
					public void update (PreloaderState state) {
						callback.update(state);
						if (state.hasEnded()) {
							getRootPanel().clear();
							if(loadingListener != null)
								loadingListener.beforeSetup();
							setupLoop();
							if(loadingListener != null)
								loadingListener.afterSetup();
						}
					}
				});
			}

			@Override
			public void ontimeout (String status, String errorType) {
				error("SoundManager", status + " " + errorType);
			}
			
		});
	}

	void setupLoop () {
		// setup modules
		try {			
			graphics = new GwtGraphics(root, config);			
		} catch (Throwable e) {
			root.clear();
			root.add(new Label("Sorry, your browser doesn't seem to support WebGL"));
			return;
		}
		lastWidth = graphics.getWidth();
		lastHeight = graphics.getHeight();
		Gdx.app = this;
		Gdx.audio = new GwtAudio();
		Gdx.graphics = graphics;
		Gdx.gl20 = graphics.getGL20();
		Gdx.gl = Gdx.gl20;
		Gdx.files = new GwtFiles(preloader);
		this.input = new GwtInput(graphics.canvas);
		Gdx.input = this.input;
		this.net = new GwtNet();
		Gdx.net = this.net;
		this.clipboard = new GwtClipboard();

		// tell listener about app creation
		try {
			listener.create();
			listener.resize(graphics.getWidth(), graphics.getHeight());
		} catch (Throwable t) {
			error("GwtApplication", "exception: " + t.getMessage(), t);
			t.printStackTrace();
			throw new RuntimeException(t);
		}

		AnimationScheduler.get().requestAnimationFrame(new AnimationCallback() {
			@Override
			public void execute (double timestamp) {
				try {
					mainLoop();
				} catch (Throwable t) {
					error("GwtApplication", "exception: " + t.getMessage(), t);
					throw new RuntimeException(t);
				}
				AnimationScheduler.get().requestAnimationFrame(this, graphics.canvas);
			}
		}, graphics.canvas);
	}

	void mainLoop() {
		graphics.update();
		if (Gdx.graphics.getWidth() != lastWidth || Gdx.graphics.getHeight() != lastHeight) {
			GwtApplication.this.listener.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			lastWidth = graphics.getWidth();
			lastHeight = graphics.getHeight();
			Gdx.gl.glViewport(0, 0, lastWidth, lastHeight);
		}
		runnablesHelper.addAll(runnables);
		runnables.clear();
		for (int i = 0; i < runnablesHelper.size; i++) {
			runnablesHelper.get(i).run();
		}
		runnablesHelper.clear();
		graphics.frameId++;
		listener.render();
		input.reset();
	}
	
	public Panel getRootPanel () {
		return root;
	}

	long loadStart = TimeUtils.nanoTime();

	public Preloader createPreloader() {
		return new Preloader(getPreloaderBaseURL());
	}

	public PreloaderCallback getPreloaderCallback () {
		final Panel preloaderPanel = new VerticalPanel();
		preloaderPanel.setStyleName("gdx-preloader");
		final Image logo = new Image(GWT.getModuleBaseURL() + "logo.png");
		logo.setStyleName("logo");		
		preloaderPanel.add(logo);
		final Panel meterPanel = new SimplePanel();
		meterPanel.setStyleName("gdx-meter");
		meterPanel.addStyleName("red");
		final InlineHTML meter = new InlineHTML();
		final Style meterStyle = meter.getElement().getStyle();
		meterStyle.setWidth(0, Unit.PCT);
		meterPanel.add(meter);
		preloaderPanel.add(meterPanel);
		getRootPanel().add(preloaderPanel);
		return new PreloaderCallback() {

			@Override
			public void error (String file) {
				System.out.println("error: " + file);
			}
			
			@Override
			public void update (PreloaderState state) {
				meterStyle.setWidth(100f * state.getProgress(), Unit.PCT);
			}			
			
		};
	}

	@Override
	public Graphics getGraphics () {
		return graphics;
	}

	@Override
	public Audio getAudio () {
		return Gdx.audio;
	}

	@Override
	public Input getInput () {
		return Gdx.input;
	}

	@Override
	public Files getFiles () {
		return Gdx.files;
	}
	
	@Override
	public Net getNet() {
		return Gdx.net;
	}

	private void checkLogLabel () {
		if (log == null) {
			log = new TextArea();
			log.setSize(graphics.getWidth() + "px", "200px");
			log.setReadOnly(true);
			root.add(log);
		}
	}

	@Override
	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO) {
			checkLogLabel();
			log.setText(log.getText() + "\n" + tag + ": " + message);
			log.setCursorPos(log.getText().length() - 1);
			System.out.println(tag + ": " + message);
		}
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_INFO) {
			checkLogLabel();
			log.setText(log.getText() + "\n" + tag + ": " + message + "\n" + getMessages(exception) + "\n");
			log.setCursorPos(log.getText().length() - 1);
			System.out.println(tag + ": " + message + "\n" + exception.getMessage());
			System.out.println(getStackTrace(exception));
		}
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR) {
			checkLogLabel();
			log.setText(log.getText() + "\n" + tag + ": " + message + "\n");
			log.setCursorPos(log.getText().length() - 1);
			System.err.println(tag + ": " + message);
		}
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) {
			checkLogLabel();
			log.setText(log.getText() + "\n" + tag + ": " + message + "\n" + getMessages(exception) + "\n");
			log.setCursorPos(log.getText().length() - 1);
			System.err.println(tag + ": " + message + "\n" + exception.getMessage() + "\n");
			System.out.println(getStackTrace(exception));
		}
	}

	@Override
	public void debug (String tag, String message) {
		if (logLevel >= LOG_DEBUG) {
			checkLogLabel();
			log.setText(log.getText() + "\n" + tag + ": " + message + "\n");
			log.setCursorPos(log.getText().length() - 1);
			System.out.println(tag + ": " + message + "\n");
		}
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) {
			checkLogLabel();
			log.setText(log.getText() + "\n" + tag + ": " + message + "\n" + getMessages(exception) + "\n");
			log.setCursorPos(log.getText().length() - 1);
			System.out.println(tag + ": " + message + "\n" + exception.getMessage());
			System.out.println(getStackTrace(exception));
		}
	}
	
	private String getMessages (Throwable e) {
		StringBuffer buffer = new StringBuffer();
		while (e != null) {
			buffer.append(e.getMessage() + "\n");
			e = e.getCause();
		}
		return buffer.toString();
	}
	
	private String getStackTrace (Throwable e) {
		StringBuffer buffer = new StringBuffer();
		for (StackTraceElement trace : e.getStackTrace()) {
			buffer.append(trace.toString() + "\n");
		}
		return buffer.toString();
	}

	@Override
	public void setLogLevel (int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public int getLogLevel() {
		return logLevel;
	}

	@Override
	public ApplicationType getType () {
		return ApplicationType.WebGL;
	}
	
	@Override
	public SystemType getOS() {
		String platform = Navigator.getPlatform().toLowerCase();
		String agent = Navigator.getUserAgent().toLowerCase();
		
		if(platform.contains("win"))
			return SystemType.Windows;
		if(platform.contains("mac"))
			return SystemType.OSX;
		if(platform.contains("iphone") || platform.contains("ipad") || platform.contains("ipod"))
			return SystemType.iOS;
		if(platform.contains("pike v7") && agent.contains("(iphone;")) // Opera Mini on iOS
			return SystemType.iOS;
		if(platform.contains("android") || (platform.equals("linux") && agent.contains("android")))
			return SystemType.Android;
		if(platform.contains("linux"))
			return SystemType.Linux;
		else
			return null;
	}

	@Override
	public int getVersion () {
		return 0;
	}

	@Override
	public long getJavaHeap () {
		return 0;
	}

	@Override
	public long getNativeHeap () {
		return 0;
	}

	@Override
	public Preferences getPreferences (String name) {
		Preferences pref = prefs.get(name);
		if (pref == null) {
			pref = new GwtPreferences(name);
			prefs.put(name, pref);
		}
		return pref;
	}

	@Override
	public Clipboard getClipboard () {
		return clipboard;
	}
	
	@Override
	public void postRunnable (Runnable runnable) {
		runnables.add(runnable);
	}

	@Override
	public void exit () {
	}

	/** Contains precomputed information on the user-agent. Useful for dealing with browser and OS behavioral differences. Kindly
	 * borrowed from PlayN */
	public static AgentInfo agentInfo () {
		return agentInfo;
	}

	/** kindly borrowed from PlayN **/
	private static native AgentInfo computeAgentInfo () /*-{
																			var userAgent = navigator.userAgent.toLowerCase();
																			return {
																			// browser type flags
																			isFirefox : userAgent.indexOf("firefox") != -1,
																			isChrome : userAgent.indexOf("chrome") != -1,
																			isSafari : userAgent.indexOf("safari") != -1,
																			isOpera : userAgent.indexOf("opera") != -1,
																			isIE : userAgent.indexOf("msie") != -1,
																			// OS type flags
																			isMacOS : userAgent.indexOf("mac") != -1,
																			isLinux : userAgent.indexOf("linux") != -1,
																			isWindows : userAgent.indexOf("win") != -1
																			};
																			}-*/;

	/** Returned by {@link #agentInfo}. Kindly borrowed from PlayN. */
	public static class AgentInfo extends JavaScriptObject {
		public final native boolean isFirefox () /*-{
																return this.isFirefox;
																}-*/;

		public final native boolean isChrome () /*-{
																return this.isChrome;
																}-*/;

		public final native boolean isSafari () /*-{
																return this.isSafari;
																}-*/;

		public final native boolean isOpera () /*-{
															return this.isOpera;
															}-*/;

		public final native boolean isIE () /*-{
														return this.isIE;
														}-*/;

		public final native boolean isMacOS () /*-{
															return this.isMacOS;
															}-*/;

		public final native boolean isLinux () /*-{
															return this.isLinux;
															}-*/;

		public final native boolean isWindows () /*-{
																return this.isWindows;
																}-*/;

		protected AgentInfo () {
		}
	}

	public String getBaseUrl () {
		return preloader.baseUrl;
	}

	public Preloader getPreloader () {
		return preloader;
	}
	
	public CanvasElement getCanvasElement(){
		return graphics.canvas;
	}

	public LoadingListener getLoadingListener () {
		return loadingListener;
	}

	public void setLoadingListener (LoadingListener loadingListener) {
		this.loadingListener = loadingListener;
	}

	@Override
	public void addLifecycleListener (LifecycleListener listener) {
		synchronized(lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener (LifecycleListener listener) {
		synchronized(lifecycleListeners) {
			lifecycleListeners.removeValue(listener, true);
		}		
	}
	
	native static public void consoleLog(String message) /*-{
		console.log( "GWT: " + message );
	}-*/;
	
	private native void addEventListeners () /*-{
		var self = this;
		$doc.addEventListener('visibilitychange', function (e) {
			self.@com.badlogic.gdx.backends.gwt.GwtApplication::onVisibilityChange(Z)($doc['hidden'] !== true);
		});
	}-*/;

	private void onVisibilityChange (boolean visible) {
		if (visible) {
			for (LifecycleListener listener : lifecycleListeners) {
				listener.resume();
			}
			listener.resume();
		} else {
			for (LifecycleListener listener : lifecycleListeners) {
				listener.pause();
			}
			listener.pause();
		}
	}
	
	/**
	 * LoadingListener interface main purpose is to do some things before or after {@link GwtApplication#setupLoop()}
	 */
	public interface LoadingListener{
		/**
		 * Method called before the setup
		 */
		public void beforeSetup();
		
		/**
		 * Method called after the setup
		 */
		public void afterSetup();
	}
}
