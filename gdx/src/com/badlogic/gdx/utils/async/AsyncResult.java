package com.badlogic.gdx.utils.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Returned by {@link AsyncExecutor#submit(AsyncTask)}, allows to poll
 * for the result of the asynch workload.
 * @author badlogic
 *
 */
public class AsyncResult<T> {
	private final Future<T> future;
	
	AsyncResult(Future<T> future) {
		this.future = future;
	}
	
	/**
	 * @return whether the {@link AsyncTask} is done
	 */
	public boolean isDone() {
		return future.isDone();
	}
	
	/**
	 * @return the result, or null if there was an error, no result, or the task is still running
	 */
	public T get() {
		try {
			return future.get();
		} catch (InterruptedException e) {
			return null;
		} catch (ExecutionException e) {
			return null;
		}
	}
}
