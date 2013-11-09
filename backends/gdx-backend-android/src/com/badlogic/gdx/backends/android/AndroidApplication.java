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

package com.badlogic.gdx.backends.android;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Clipboard;

/** An implementation of the {@link Application} interface for Android.
 * Create an {@link Activity} that derives from this class. In
 * the {@link Activity#onCreate(Bundle)} method call the {@link #initialize(ApplicationListener, boolean)} method specifying the
 * configuration for the GLSurfaceView.
 * 
 * @author Centril */
public class AndroidApplication extends Activity implements Application {
	protected AndroidApplicationBackend backend;

	protected void initBackend() {
		if ( backend == null ) {
			backend = new AndroidApplicationBackend();
			backend.setActvitiy(this);
		}
	}

	public void initialize (ApplicationListener listener, boolean useGL2IfAvailable) {
		initBackend();
		backend.initialize(listener, useGL2IfAvailable);
	}

	public void initialize (ApplicationListener listener, AndroidApplicationConfiguration config) {
		initBackend();
		backend.initialize(listener, config);
	}

	public View initializeForView (ApplicationListener listener, boolean useGL2IfAvailable) {
		initBackend();
		return backend.initializeForView(listener, useGL2IfAvailable);
	}

	public View initializeForView (ApplicationListener listener, AndroidApplicationConfiguration config) {
		initBackend();
		return backend.initializeForView(listener, config);
	}

	protected void createWakeLock (AndroidApplicationConfiguration config) {
		backend.createWakeLock(config);
	}

	protected void hideStatusBar (AndroidApplicationConfiguration config) {
		backend.hideStatusBar(config);
	}

	protected FrameLayout.LayoutParams createLayoutParams () {
		return backend.createLayoutParams();
	}

	@Override
	protected void onPause () {
		backend.onPause();
		super.onPause();
	}

	@Override
	protected void onResume () {
		backend.onResume();
		super.onResume();
	}

	@Override
	public void onConfigurationChanged (Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		backend.onConfigurationChanged(newConfig);
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return backend.getApplicationListener();
	}

	@Override
	public Graphics getGraphics () {
		return backend.getGraphics();
	}

	@Override
	public Audio getAudio () {
		return backend.getAudio();
	}

	@Override
	public Input getInput () {
		return backend.getInput();
	}

	@Override
	public Files getFiles () {
		return backend.getFiles();
	}

	@Override
	public Net getNet () {
		return backend.getNet();
	}

	@Override
	public void log (String tag, String message) {
		backend.log(tag, message);
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		backend.log(tag, message, exception);
	}

	@Override
	public void error (String tag, String message) {
		backend.error(tag, message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		backend.error(tag, message, exception);
	}

	@Override
	public void debug (String tag, String message) {
		backend.debug(tag, message);
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		backend.debug(tag, message, exception);
	}

	@Override
	public void setLogLevel (int logLevel) {
		backend.setLogLevel(logLevel);
	}

	@Override
	public int getLogLevel () {
		return backend.getLogLevel();
	}

	@Override
	public ApplicationType getType () {
		return backend.getType();
	}

	@Override
	public int getVersion () {
		return backend.getVersion();
	}

	@Override
	public long getJavaHeap () {
		return backend.getJavaHeap();
	}

	@Override
	public long getNativeHeap () {
		return backend.getNativeHeap();
	}

	@Override
	public Preferences getPreferences (String name) {
		return backend.getPreferences(name);
	}

	@Override
	public Clipboard getClipboard () {
		return backend.getClipboard();
	}

	@Override
	public void postRunnable (Runnable runnable) {
		backend.postRunnable(runnable);
	}

	@Override
	public void exit () {
		backend.exit();
	}

	@Override
	public void addLifecycleListener (LifecycleListener listener) {
		backend.addLifecycleListener(listener);
	}

	@Override
	public void removeLifecycleListener (LifecycleListener listener) {
		backend.removeLifecycleListener(listener);
	}
}
