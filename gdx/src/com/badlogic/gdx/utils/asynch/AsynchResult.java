package com.badlogic.gdx.utils.asynch;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Returned by {@link AsynchExecutor#submit(AsynchTask)}, allows to poll
 * for the result of the asynch workload.
 * @author badlogic
 *
 */
public class AsynchResult<T> {
	private final Future<T> future;
	
	AsynchResult(Future<T> future) {
		this.future = future;
	}
	
	/**
	 * @return whether the {@link AsynchTask} is done
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
