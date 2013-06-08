package com.badlogic.gdx.utils.asynch;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * GWT emulation of AsynchExecutor, will call tasks immediately :D
 * @author badlogic
 *
 */
public class AsynchExecutor implements Disposable {
	
	/**
	 * Creates a new AsynchExecutor that allows maxConcurrent
	 * {@link Runnable} instances to run in parallel.
	 * @param maxConcurrent
	 */
	public AsynchExecutor(int maxConcurrent) {
	}
	
	/**
	 * Submits a {@link Runnable} to be executed asynchronously. If
	 * maxConcurrent runnables are already running, the runnable 
	 * will be queued.
	 * @param task the task to execute asynchronously
	 */
	public <T> AsynchResult<T> submit(final AsynchTask<T> task) {
		T result = null;
		boolean error = false;
		try {
			result = task.call();
		} catch(Throwable t) {
			error = true;
		}
		return new AsynchResult(result);
	}
	
	/**
	 * Waits for running {@link AsynchTask} instances to finish,
	 * then destroys any resources like threads. Can not be used
	 * after this method is called.
	 */
	@Override
	public void dispose () {
	}
}
