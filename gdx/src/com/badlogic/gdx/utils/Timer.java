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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;

/** Executes tasks in the future on the main loop thread.
 * @author Nathan Sweet */
public class Timer {
	static final Array<Timer> instances = new Array(1);
	static TimerThread thread;
	static private final int CANCELLED = -1;
	static private final int FOREVER = -2;

	/** Timer instance for general application wide usage. Static methods on {@link Timer} make convenient use of this instance. */
	static Timer instance = new Timer();

	static public Timer instance () {
		if (instance == null) {
			instance = new Timer();
		}
		return instance;
	}

	private final Array<Task> tasks = new Array(false, 8);

	public Timer () {
		start();
	}

	/** Schedules a task to occur once as soon as possible, but not sooner than the start of the next frame. */
	public Task postTask (Task task) {
		return scheduleTask(task, 0, 0, 0);
	}

	/** Schedules a task to occur once after the specified delay. */
	public Task scheduleTask (Task task, float delaySeconds) {
		return scheduleTask(task, delaySeconds, 0, 0);
	}

	/** Schedules a task to occur once after the specified delay and then repeatedly at the specified interval until cancelled. */
	public Task scheduleTask (Task task, float delaySeconds, float intervalSeconds) {
		return scheduleTask(task, delaySeconds, intervalSeconds, FOREVER);
	}

	/** Schedules a task to occur once after the specified delay and then a number of additional times at the specified
	 * interval. */
	public Task scheduleTask (Task task, float delaySeconds, float intervalSeconds, int repeatCount) {
		synchronized (task) {
			if (task.repeatCount != CANCELLED) throw new IllegalArgumentException("The same task may not be scheduled twice.");
			task.executeTimeMillis = System.nanoTime() / 1000000 + (long)(delaySeconds * 1000);
			task.intervalMillis = (long)(intervalSeconds * 1000);
			task.repeatCount = repeatCount;
		}
		synchronized (this) {
			tasks.add(task);
		}
		wake();

		return task;
	}

	/** Stops the timer, tasks will not be executed and time that passes will not be applied to the task delays. */
	public void stop () {
		synchronized (instances) {
			instances.removeValue(this, true);
		}
	}

	/** Starts the timer if it was stopped. */
	public void start () {
		synchronized (instances) {
			if (instances.contains(this, true)) return;
			instances.add(this);
			if (thread == null) thread = new TimerThread();
			wake();
		}
	}

	/** Cancels all tasks. */
	public void clear () {
		synchronized (this) {
			for (int i = 0, n = tasks.size; i < n; i++)
				tasks.get(i).cancel();
			tasks.clear();
		}
	}

	/** Returns true if the timer has no tasks in the queue. Note that this can change at any time. Synchronize on the timer
	 * instance to prevent tasks being added, removed, or updated. */
	public boolean isEmpty () {
		synchronized (this) {
			return tasks.size == 0;
		}
	}

	long update (long timeMillis, long waitMillis) {
		synchronized (this) {
			for (int i = 0, n = tasks.size; i < n; i++) {
				Task task = tasks.get(i);
				synchronized (task) {
					if (task.executeTimeMillis > timeMillis) {
						waitMillis = Math.min(waitMillis, task.executeTimeMillis - timeMillis);
						continue;
					}
					if (task.repeatCount != CANCELLED) {
						if (task.repeatCount == 0) task.repeatCount = CANCELLED;
						task.app.postRunnable(task);
					}
					if (task.repeatCount == CANCELLED) {
						tasks.removeIndex(i);
						i--;
						n--;
					} else {
						task.executeTimeMillis = timeMillis + task.intervalMillis;
						waitMillis = Math.min(waitMillis, task.intervalMillis);
						if (task.repeatCount > 0) task.repeatCount--;
					}
				}
			}
		}
		return waitMillis;
	}

	/** Adds the specified delay to all tasks. */
	public void delay (long delayMillis) {
		synchronized (this) {
			for (int i = 0, n = tasks.size; i < n; i++) {
				Task task = tasks.get(i);
				synchronized (task) {
					task.executeTimeMillis += delayMillis;
				}
			}
		}
	}

	static void wake () {
		synchronized (instances) {
			instances.notifyAll();
		}
	}

	/** Schedules a task on {@link #instance}.
	 * @see #postTask(Task) */
	static public Task post (Task task) {
		return instance().postTask(task);
	}

	/** Schedules a task on {@link #instance}.
	 * @see #scheduleTask(Task, float) */
	static public Task schedule (Task task, float delaySeconds) {
		return instance().scheduleTask(task, delaySeconds);
	}

	/** Schedules a task on {@link #instance}.
	 * @see #scheduleTask(Task, float, float) */
	static public Task schedule (Task task, float delaySeconds, float intervalSeconds) {
		return instance().scheduleTask(task, delaySeconds, intervalSeconds);
	}

	/** Schedules a task on {@link #instance}.
	 * @see #scheduleTask(Task, float, float, int) */
	static public Task schedule (Task task, float delaySeconds, float intervalSeconds, int repeatCount) {
		return instance().scheduleTask(task, delaySeconds, intervalSeconds, repeatCount);
	}

	/** Runnable with a cancel method.
	 * @see Timer
	 * @author Nathan Sweet */
	static abstract public class Task implements Runnable {
		long executeTimeMillis;
		long intervalMillis;
		int repeatCount = CANCELLED;
		Application app;

		public Task () {
			app = Gdx.app; // Need to store the app when the task was created for multiple LwjglAWTCanvas.
			if (app == null) throw new IllegalStateException("Gdx.app not available.");
		}

		/** If this is the last time the task will be ran or the task is first cancelled, it may be scheduled again in this
		 * method. */
		abstract public void run ();

		/** Cancels the task. It will not be executed until it is scheduled again. This method can be called at any time. */
		public synchronized void cancel () {
			executeTimeMillis = 0;
			repeatCount = CANCELLED;
		}

		/** Returns true if this task is scheduled to be executed in the future by a timer. The execution time may be reached after
		 * calling this method which may change the scheduled state. To prevent the scheduled state from changing, synchronize on
		 * this task object, eg:
		 * 
		 * <pre>
		 * synchronized (task) {
		 * 	if (!task.isScheduled()) { ... }
		 * }
		 * </pre>
		*/
		public synchronized boolean isScheduled () {
			return repeatCount != CANCELLED;
		}

		/** Returns the time when this task will be executed in milliseconds */
		public synchronized long getExecuteTimeMillis () {
			return executeTimeMillis;
		}
	}

	/** Manages the single timer thread. Stops thread on libgdx application pause and dispose, starts thread on resume.
	 * @author Nathan Sweet */
	static class TimerThread implements Runnable, LifecycleListener {
		Files files;
		private long pauseMillis;

		public TimerThread () {
			Gdx.app.addLifecycleListener(this);
			resume();
		}

		public void run () {
			while (true) {
				synchronized (instances) {
					if (files != Gdx.files) return;

					long timeMillis = System.nanoTime() / 1000000;
					long waitMillis = 5000;
					for (int i = 0, n = instances.size; i < n; i++) {
						try {
							waitMillis = instances.get(i).update(timeMillis, waitMillis);
						} catch (Throwable ex) {
							throw new GdxRuntimeException("Task failed: " + instances.get(i).getClass().getName(), ex);
						}
					}

					if (files != Gdx.files) return;

					try {
						if (waitMillis > 0) instances.wait(waitMillis);
					} catch (InterruptedException ignored) {
					}
				}
			}
		}

		public void resume () {
			long delayMillis = System.nanoTime() / 1000000 - pauseMillis;
			synchronized (instances) {
				for (int i = 0, n = instances.size; i < n; i++) {
					instances.get(i).delay(delayMillis);
				}
			}
			files = Gdx.files;
			Thread t = new Thread(this, "Timer");
			t.setDaemon(true);
			t.start();
			thread = this;
		}

		public void pause () {
			pauseMillis = System.nanoTime() / 1000000;
			synchronized (instances) {
				files = null;
				wake();
			}
			thread = null;
		}

		public void dispose () {
			pause();
			Gdx.app.removeLifecycleListener(this);
			instances.clear();
			instance = null;
		}
	}
}
