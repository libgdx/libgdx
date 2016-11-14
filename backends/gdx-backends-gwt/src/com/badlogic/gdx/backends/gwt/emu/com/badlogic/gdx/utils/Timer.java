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

	/** Schedules a task to occur once after the specified delay and then a number of additional times at the specified interval. */
	public Task scheduleTask (Task task, float delaySeconds, float intervalSeconds, int repeatCount) {
		if (task.repeatCount != CANCELLED) throw new IllegalArgumentException("The same task may not be scheduled twice.");
		task.executeTimeMillis = TimeUtils.nanoTime() / 1000000 + (long)(delaySeconds * 1000);
		task.intervalMillis = (long)(intervalSeconds * 1000);
		task.repeatCount = repeatCount;
		synchronized (tasks) {
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
		synchronized (tasks) {
			for (int i = 0, n = tasks.size; i < n; i++)
				tasks.get(i).cancel();
			tasks.clear();
		}
	}

	long update (long timeMillis, long waitMillis) {
		synchronized (tasks) {
			for (int i = 0, n = tasks.size; i < n; i++) {
				Task task = tasks.get(i);
				if (task.executeTimeMillis > timeMillis) {
					waitMillis = Math.min(waitMillis, task.executeTimeMillis - timeMillis);
					continue;
				}
				if (task.repeatCount != CANCELLED) {
					if (task.repeatCount == 0) {
						// Set cancelled before run so it may be rescheduled in run.
						task.repeatCount = CANCELLED;
					}
					Gdx.app.postRunnable(task);
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
		return waitMillis;
	}

	/** Adds the specified delay to all tasks. */
	public void delay (long delayMillis) {
		synchronized (tasks) {
			for (int i = 0, n = tasks.size; i < n; i++) {
				Task task = tasks.get(i);
				task.executeTimeMillis += delayMillis;
			}
		}
	}

	static void wake () {
		synchronized (instances) {
			thread.schedule(0);
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

		/** If this is the last time the task will be ran or the task is first cancelled, it may be scheduled again in this method. */
		abstract public void run ();

		/** Cancels the task. It will not be executed until it is scheduled again. This method can be called at any time. */
		public void cancel () {
			executeTimeMillis = 0;
			repeatCount = CANCELLED;
		}

		/** Returns true if this task is scheduled to be executed in the future by a timer. */
		public boolean isScheduled () {
			return repeatCount != CANCELLED;
		}

		/** Returns the time when this task will be executed in milliseconds */
		public long getExecuteTimeMillis () {
			return executeTimeMillis;
		}
	}

	/** Manages the single timer thread. Stops thread on libgdx application pause and dispose, starts thread on resume.
	 * @author Nathan Sweet */
	static class TimerThread extends com.google.gwt.user.client.Timer implements Runnable, LifecycleListener {
		private Application app;
		private long pauseMillis;

		public TimerThread () {
			Gdx.app.addLifecycleListener(this);
			resume();
		}

		public void run () {
			synchronized (instances) {
				if (app != Gdx.app) return;
				
				long timeMillis = TimeUtils.nanoTime() / 1000000;
				long waitMillis = 5000;
				for (int i = 0, n = instances.size; i < n; i++) {
					try {
						waitMillis = instances.get(i).update(timeMillis, waitMillis);
					} catch (Throwable ex) {
						throw new GdxRuntimeException("Task failed: " + instances.get(i).getClass().getName(), ex);
					}
				}
				
				if (app != Gdx.app) return;
				
				schedule((int)Math.max(0, waitMillis));
			}
		}

		public void resume () {
			long delayMillis = TimeUtils.nanoTime() / 1000000 - pauseMillis;
			synchronized (instances) {
				for (int i = 0, n = instances.size; i < n; i++)
					instances.get(i).delay(delayMillis);
			}
			app = Gdx.app;
			run();
		}

		public void pause () {
			pauseMillis = TimeUtils.nanoTime() / 1000000;
			synchronized (instances) {
				app = null;
				wake();
			}
		}

		public void dispose () {
			pause();
			Gdx.app.removeLifecycleListener(this);
			thread = null;
			instances.clear();
			instance = null;
		}
	}
}
