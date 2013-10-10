package com.badlogic.gdx.utils.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.async.AsyncTask;

/**
 * GWT emulation of AsynchExecutor, will call tasks immediately :D
 * @author badlogic
 *
 */
public class AsyncExecutor implements Disposable {
	
	/**
	 * Creates a new AsynchExecutor that allows maxConcurrent
	 * {@link Runnable} instances to run in parallel.
	 * @param maxConcurrent
	 */
	public AsyncExecutor(int maxConcurrent) {
	}
	
	/**
	 * Submits a {@link Runnable} to be executed asynchronously. If
	 * maxConcurrent runnables are already running, the runnable 
	 * will be queued.
	 * @param task the task to execute asynchronously
	 */
	public <T> AsyncResult<T> submit(final AsyncTask<T> task) {
		T result = null;
		boolean error = false;
		try {
			result = task.call();
		} catch(Throwable t) {
			error = true;
		}
		return new AsyncResult(result);
	}
	
	/**
	 * Waits for running {@link AsyncTask} instances to finish,
	 * then destroys any resources like threads. Can not be used
	 * after this method is called.
	 */
	@Override
	public void dispose () {
	}
}
