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

package com.badlogic.gdx.utils;

/** A pausable thread. The runnable must not execute an inifite loop but should return control to the thread as often as possible
 * so that the thread can actually pause.
 * 
 * @author mzechner */
public class PauseableThread extends Thread {
	final Runnable runnable;
	boolean paused = false;
	boolean exit = false;

	/** Constructs a new thread setting the runnable which will be called repeatedly in a loop.
	 * 
	 * @param runnable the runnable. */
	public PauseableThread (Runnable runnable) {
		this.runnable = runnable;
	}

	public void run () {
		while (true) {
			synchronized (this) {
				try {
					while (paused)
						wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (exit) return;

			runnable.run();
		}
	}

	/** Pauses the thread. This call is non-blocking */
	public void onPause () {
		paused = true;
	}

	/** Resumes the thread. This call is non-blocking */
	public void onResume () {
		synchronized (this) {
			paused = false;
			this.notifyAll();
		}
	}

	/** @return whether this thread is paused or not */
	public boolean isPaused () {
		return paused;
	}

	/** Stops this thread */
	public void stopThread () {
		exit = true;
		if (paused) onResume();
	}
}
