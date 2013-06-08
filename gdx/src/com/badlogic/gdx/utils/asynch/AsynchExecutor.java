package com.badlogic.gdx.utils.asynch;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Allows asnynchronous execution of {@link AsynchTask} instances on a separate thread.
 * Needs to be disposed via a call to {@link #dispose()} when no longer used, in which
 * case the executor waits for running tasks to finish. Scheduled but not yet
 * running tasks will not be executed. 
 * @author badlogic
 *
 */
public class AsynchExecutor implements Disposable {
	private final ExecutorService executor;
	
	/**
	 * Creates a new AsynchExecutor that allows maxConcurrent
	 * {@link Runnable} instances to run in parallel.
	 * @param maxConcurrent
	 */
	public AsynchExecutor(int maxConcurrent) {
		executor = Executors.newFixedThreadPool(maxConcurrent, new ThreadFactory() {
			@Override
			public Thread newThread (Runnable r) {
				Thread thread = new Thread(r, "AsynchExecutor-Thread");
				thread.setDaemon(true);
				return thread;
			}
		});
	}
	
	/**
	 * Submits a {@link Runnable} to be executed asynchronously. If
	 * maxConcurrent runnables are already running, the runnable 
	 * will be queued.
	 * @param task the task to execute asynchronously
	 */
	public <T> AsynchResult<T> submit(final AsynchTask<T> task) {
		return new AsynchResult(executor.submit(new Callable<T>() {
			@Override
			public T call () throws Exception {
				return task.call();
			}
		}));
	}
	
	/**
	 * Waits for running {@link AsynchTask} instances to finish,
	 * then destroys any resources like threads. Can not be used
	 * after this method is called.
	 */
	@Override
	public void dispose () {
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			new GdxRuntimeException("Couldn't shutdown loading thread");
		}
	}
}
