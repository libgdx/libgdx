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

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** GWT emulation of AsynchExecutor, will call tasks immediately :D
 * @author badlogic */
public class AsyncExecutor implements Disposable {

	/** Creates a new AsynchExecutor that allows maxConcurrent {@link Runnable} instances to run in parallel.
	 * @param maxConcurrent */
	public AsyncExecutor (int maxConcurrent) {
	}

	/** Submits a {@link Runnable} to be executed asynchronously. If maxConcurrent runnables are already running, the runnable will
	 * be queued.
	 * @param task the task to execute asynchronously */
	public <T> AsyncResult<T> submit (final AsyncTask<T> task) {
		T result = null;
		try {
			result = task.call();
		} catch (Throwable t) {
			throw new GdxRuntimeException("Could not submit AsyncTask: " + t.getMessage(), t);
		}
		return new AsyncResult(result);
	}

	/** Waits for running {@link AsyncTask} instances to finish, then destroys any resources like threads. Can not be used after
	 * this method is called. */
	@Override
	public void dispose () {
	}
}
