package com.badlogic.gdx.utils.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;

/**
 * Returned by {@link AsyncExecutor#submit(AsyncTask)}, allows to poll
 * for the result of the asynch workload.
 * @author badlogic
 *
 */
public class AsyncResult<T> {
	private final T result;
	
	AsyncResult(T result) {
		this.result = result;
	}
	
	/**
	 * @return whether the {@link AsyncTask} is done
	 */
	public boolean isDone() {
		return true;
	}
	
	/**
	 * @return the result, or null if there was an error, no result, or the task is still running
	 */
	public T get() {
		return result;
	}
}
