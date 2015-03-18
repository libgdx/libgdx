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

package com.badlogic.gdx.utils.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Allows asnynchronous execution of {@link AsyncTask} instances on a separate thread. Needs to be disposed via a call to
 * {@link #dispose()} when no longer used, in which case the executor waits for running tasks to finish. Scheduled but not yet
 * running tasks will not be executed.
 * @author badlogic */
public class AsyncExecutor implements Disposable {
	private final ExecutorService executor;

	/** Creates a new AsynchExecutor that allows maxConcurrent {@link Runnable} instances to run in parallel.
	 * @param maxConcurrent */
	public AsyncExecutor (int maxConcurrent) {
		executor = Executors.newFixedThreadPool(maxConcurrent, new ThreadFactory() {
			@Override
			public Thread newThread (Runnable r) {
				Thread thread = new Thread(r, "AsynchExecutor-Thread");
				thread.setDaemon(true);
				return thread;
			}
		});
	}

	/** Submits a {@link Runnable} to be executed asynchronously. If maxConcurrent runnables are already running, the runnable will
	 * be queued.
	 * @param task the task to execute asynchronously */
	public <T> AsyncResult<T> submit (final AsyncTask<T> task) {
		if (executor.isShutdown()) {
			throw new GdxRuntimeException("Cannot run tasks on an executor that has been shutdown (disposed)");
		}
		return new AsyncResult(executor.submit(new Callable<T>() {
			@Override
			public T call () throws Exception {
				return task.call();
			}
		}));
	}

	/** Waits for running {@link AsyncTask} instances to finish, then destroys any resources like threads. Can not be used after
	 * this method is called. */
	@Override
	public void dispose () {
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new GdxRuntimeException("Couldn't shutdown loading thread", e);
		}
	}
}
