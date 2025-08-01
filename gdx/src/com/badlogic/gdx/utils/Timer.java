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
	// TimerThread access is synchronized using threadLock.
	// Timer access is synchronized using the Timer instance.
	// Task access is synchronized using the Task instance.
	// Posted tasks are synchronized using TimerThread#postedTasks.

	static final Object threadLock = new Object();
	static TimerThread thread;

	/** Timer instance singleton for general application wide usage. Static methods on {@link Timer} make convenient use of this
	 * instance. */
	static public Timer instance () {
		synchronized (threadLock) {
			TimerThread thread = thread();
			if (thread.instance == null) thread.instance = new Timer();
			return thread.instance;
		}
	}

	static TimerThread thread () {
		synchronized (threadLock) {
			if (thread == null || thread.files != Gdx.files) {
				if (thread != null) thread.dispose();
				thread = new TimerThread();
			}
			return thread;
		}
	}

	final Array<Task> tasks = new Array(false, 8);
	long stopTimeMillis;

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
		return scheduleTask(task, delaySeconds, intervalSeconds, -1);
	}

	/** Schedules a task to occur once after the specified delay and then a number of additional times at the specified interval.
	 * @param repeatCount If negative, the task will repeat forever. */
	public Task scheduleTask (Task task, float delaySeconds, float intervalSeconds, int repeatCount) {
		synchronized (threadLock) {
			synchronized (this) {
				synchronized (task) {
					if (task.timer != null) throw new IllegalArgumentException("The same task may not be scheduled twice.");
					task.timer = this;
					long timeMillis = System.nanoTime() / 1000000;
					long executeTimeMillis = timeMillis + (long)(delaySeconds * 1000);
					if (thread.pauseTimeMillis > 0) executeTimeMillis -= timeMillis - thread.pauseTimeMillis;
					task.executeTimeMillis = executeTimeMillis;
					task.intervalMillis = (long)(intervalSeconds * 1000);
					task.repeatCount = repeatCount;
					tasks.add(task);
				}
			}
			threadLock.notifyAll();
		}
		return task;
	}

	/** Stops the timer if it was started. Tasks will not be executed while stopped. */
	public void stop () {
		synchronized (threadLock) {
			if (thread().instances.removeValue(this, true)) stopTimeMillis = System.nanoTime() / 1000000;
		}
	}

	/** Starts the timer if it was stopped. Tasks are delayed by the time passed while stopped. */
	public void start () {
		synchronized (threadLock) {
			TimerThread thread = thread();
			Array<Timer> instances = thread.instances;
			if (instances.contains(this, true)) return;
			instances.add(this);
			if (stopTimeMillis > 0) {
				delay(System.nanoTime() / 1000000 - stopTimeMillis);
				stopTimeMillis = 0;
			}
			threadLock.notifyAll();
		}
	}

	/** Cancels all tasks. */
	public void clear () {
		synchronized (threadLock) {
			TimerThread thread = thread();
			synchronized (this) {
				synchronized (thread.postedTasks) {
					for (int i = 0, n = tasks.size; i < n; i++) {
						Task task = tasks.get(i);
						thread.removePostedTask(task);
						task.reset();
					}
				}
				tasks.clear();
			}
		}
	}

	/** Returns true if the timer has no tasks in the queue. Note that this can change at any time. Synchronize on the timer
	 * instance to prevent tasks being added, removed, or updated. */
	public synchronized boolean isEmpty () {
		return tasks.size == 0;
	}

	synchronized long update (TimerThread thread, long timeMillis, long waitMillis) {
		for (int i = 0, n = tasks.size; i < n; i++) {
			Task task = tasks.get(i);
			synchronized (task) {
				if (task.executeTimeMillis > timeMillis) {
					waitMillis = Math.min(waitMillis, task.executeTimeMillis - timeMillis);
					continue;
				}
				if (task.repeatCount == 0) {
					task.timer = null;
					tasks.removeIndex(i);
					i--;
					n--;
				} else {
					task.executeTimeMillis = timeMillis + task.intervalMillis;
					waitMillis = Math.min(waitMillis, task.intervalMillis);
					if (task.repeatCount > 0) task.repeatCount--;
				}
				thread.addPostedTask(task);
			}
		}
		return waitMillis;
	}

	/** Adds the specified delay to all tasks. */
	public synchronized void delay (long delayMillis) {
		for (int i = 0, n = tasks.size; i < n; i++) {
			Task task = tasks.get(i);
			synchronized (task) {
				task.executeTimeMillis += delayMillis;
			}
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

	/** Runnable that can be scheduled on a {@link Timer}.
	 * @author Nathan Sweet */
	static abstract public class Task implements Runnable {
		final Application app;
		long executeTimeMillis, intervalMillis;
		int repeatCount;
		volatile Timer timer;

		public Task () {
			app = Gdx.app; // Store which app to postRunnable (eg for multiple LwjglAWTCanvas).
			if (app == null) throw new IllegalStateException("Gdx.app not available.");
		}

		/** If this is the last time the task will be ran or the task is first cancelled, it may be scheduled again in this
		 * method. */
		abstract public void run ();

		/** Cancels the task. It will not be executed until it is scheduled again. This method can be called at any time. */
		public void cancel () {
			synchronized (threadLock) {
				thread().removePostedTask(this);
				Timer timer = this.timer;
				if (timer != null) {
					synchronized (timer) {
						timer.tasks.removeValue(this, true);
						reset();
					}
				} else
					reset();
			}
		}

		synchronized void reset () {
			executeTimeMillis = 0;
			this.timer = null;
		}

		/** Returns true if this task is scheduled to be executed in the future by a timer. The execution time may be reached at any
		 * time after calling this method, which may change the scheduled state. To prevent the scheduled state from changing,
		 * synchronize on this task object, eg:
		 * 
		 * <pre>
		 * synchronized (task) {
		 * 	if (!task.isScheduled()) { ... }
		 * }
		 * </pre>
		 */
		public boolean isScheduled () {
			return timer != null;
		}

		/** Returns the time in milliseconds when this task will be executed next. */
		public synchronized long getExecuteTimeMillis () {
			return executeTimeMillis;
		}
	}

	/** Manages a single thread for updating timers. Uses libgdx application events to pause, resume, and dispose the thread.
	 * @author Nathan Sweet */
	static class TimerThread implements Runnable, LifecycleListener {
		final Files files;
		final Application app;
		final Array<Timer> instances = new Array(1);
		Timer instance;
		long pauseTimeMillis;

		final Array<Task> postedTasks = new Array(2);
		final Array<Task> runTasks = new Array(2);
		private final Runnable runPostedTasks = new Runnable() {
			public void run () {
				runPostedTasks();
			}
		};

		public TimerThread () {
			files = Gdx.files;
			app = Gdx.app;
			app.addLifecycleListener(this);
			resume();

			Thread thread = new Thread(this, "Timer");
			thread.setDaemon(true);
			thread.start();
		}

		public void run () {
			while (true) {
				synchronized (threadLock) {
					if (thread != this || files != Gdx.files) break;

					long waitMillis = 5000;
					if (pauseTimeMillis == 0) {
						long timeMillis = System.nanoTime() / 1000000;
						for (int i = 0, n = instances.size; i < n; i++) {
							try {
								waitMillis = instances.get(i).update(this, timeMillis, waitMillis);
							} catch (Throwable ex) {
								throw new GdxRuntimeException("Task failed: " + instances.get(i).getClass().getName(), ex);
							}
						}
					}

					if (thread != this || files != Gdx.files) break;

					try {
						if (waitMillis > 0) threadLock.wait(waitMillis);
					} catch (InterruptedException ignored) {
					}
				}
			}
			dispose();
		}

		void runPostedTasks () {
			synchronized (postedTasks) {
				runTasks.addAll(postedTasks);
				postedTasks.clear();
			}
			Object[] items = runTasks.items;
			for (int i = 0, n = runTasks.size; i < n; i++)
				((Task)items[i]).run();
			runTasks.clear();
		}

		void addPostedTask (Task task) {
			synchronized (postedTasks) {
				if (postedTasks.isEmpty()) task.app.postRunnable(runPostedTasks);
				postedTasks.add(task);
			}
		}

		void removePostedTask (Task task) {
			synchronized (postedTasks) {
				Object[] items = postedTasks.items;
				for (int i = postedTasks.size - 1; i >= 0; i--)
					if (items[i] == task) postedTasks.removeIndex(i);
			}
		}

		public void resume () {
			synchronized (threadLock) {
				long delayMillis = System.nanoTime() / 1000000 - pauseTimeMillis;
				for (int i = 0, n = instances.size; i < n; i++)
					instances.get(i).delay(delayMillis);
				pauseTimeMillis = 0;
				threadLock.notifyAll();
			}
		}

		public void pause () {
			synchronized (threadLock) {
				pauseTimeMillis = System.nanoTime() / 1000000;
				threadLock.notifyAll();
			}
		}

		public void dispose () { // OK to call multiple times.
			synchronized (threadLock) {
				synchronized (postedTasks) {
					postedTasks.clear();
				}
				if (thread == this) thread = null;
				instances.clear();
				threadLock.notifyAll();
			}
			app.removeLifecycleListener(this);
		}
	}
}
