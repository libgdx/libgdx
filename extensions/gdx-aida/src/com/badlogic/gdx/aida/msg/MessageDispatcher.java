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

package com.badlogic.gdx.aida.msg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.aida.Agent;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * @author davebaol
 */
public class MessageDispatcher {

	private static final String LOG_TAG = MessageDispatcher.class
			.getSimpleName();

	private static final float NANOS_PER_SEC = 1000000000f;

	private static final MessageDispatcher instance = new MessageDispatcher();

	public static final long START = TimeUtils.nanoTime();

	public static long getCurrentTime() {
		return TimeUtils.nanoTime() - START;
	}

	private PriorityQueue<Telegram> queue = new PriorityQueue<Telegram>();

	private final Pool<Telegram> pool;

	private long timeGranularity;

	private boolean debugEnabled;

	private MessageDispatcher() {
		this.pool = new Pool<Telegram>(64) {
			protected Telegram newObject() {
				return new Telegram();
			}
		};
		setTimeGranularity(0.25f);
	}

	public float getTimeGranularity() {
		return timeGranularity / NANOS_PER_SEC;
	}

	public void setTimeGranularity(float timeGranularity) {
		boolean uniqueness = timeGranularity <= 0;
		this.timeGranularity = uniqueness ? 0
				: (long) (timeGranularity * NANOS_PER_SEC);
		this.queue.setUniqueness(uniqueness);

	}

	// this class is a singleton
	public static MessageDispatcher getInstance() {
		return instance;
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	public void setDebugEnabled(boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

	public void clear() {
		// Iterator<Telegram> it = queue.iterator();
		// while (it.hasNext()) {
		// pool.free(it.next());
		// it.remove();
		// }
		for (int i = 0; i < queue.size(); i++) {
			pool.free(queue.get(i));
		}
		queue.clear();
	}

	/**
	 * Given a message, a receiver, a sender and any time delay, this method
	 * routes the message to the correct agent (if no delay) or stores in the
	 * message queue to be dispatched at the correct time.
	 */
	public void dispatchMessage(float delay, Agent sender, Agent receiver,
			int msg, Object extraInfo) {

		// Make sure the receiver is valid
		if (receiver == null) {
			if (debugEnabled) {
				Gdx.app.log(LOG_TAG, "Warning! No Receiver specified");
			}
			return;
		}

		// Get a telegram from the pool
		Telegram telegram = pool.obtain();
		telegram.sender = sender;
		telegram.receiver = receiver;
		telegram.message = msg;
		telegram.extraInfo = extraInfo;

		// If there is no delay, route telegram immediately
		if (delay <= 0.0f) {
			if (debugEnabled)
				Gdx.app.log(LOG_TAG, "Instant telegram dispatched at time: "
						+ getCurrentTime() + " by " + sender + " for "
						+ receiver + ". Msg is " + msg);

			// Send the telegram to the recipient
			discharge(telegram);
		} else {
			// Set the timestamp for the delayed telegram
			long currentTime = getCurrentTime();
			telegram.setTimestamp(currentTime + (long) (delay * NANOS_PER_SEC),
					timeGranularity);

			// Put it in the queue
			queue.add(telegram);

			if (debugEnabled) {
				Gdx.app.log(LOG_TAG, "\nDelayed telegram from " + sender
						+ " recorded at time " + getCurrentTime() + " for "
						+ receiver + ". Msg is " + msg);
			}
		}
	}

	/**
	 * This function dispatches any telegrams with a timestamp that has expired.
	 * Any dispatched telegrams are removed from the queue
	 * 
	 * send out any delayed messages. This method is called each time through
	 * the main game loop.
	 */
	public void dispatchDelayedMessages() {
		if (queue.size() == 0)
			return;

		// Get current time
		long currentTime = getCurrentTime();

		// now peek at the queue to see if any telegrams need dispatching.
		// remove all telegrams from the front of the queue that have gone
		// past their sell by date
		do {
			// read the telegram from the front of the queue
			// final Telegram telegram = queue.first();
			final Telegram telegram = queue.peek();
			if (telegram.getTimestamp() < currentTime)
				break;

			if (debugEnabled) {
				Gdx.app.log(LOG_TAG,
						"Queued telegram ready for dispatch: Sent to "
								+ telegram.receiver + ". Msg is "
								+ telegram.message);
			}

			// Send the telegram to the recipient
			discharge(telegram);

			// Remove it from the queue
			// queue.remove(telegram);
			queue.poll();
		} while (queue.size() > 0);

	}

	/**
	 * This method is used by DispatchMessage or DispatchDelayedMessages. This
	 * method calls the message handling member function of the receiving agent
	 * with the newly created telegram.
	 */
	private void discharge(Telegram telegram) {
		if (!telegram.receiver.handleMessage(telegram)) {
			// Telegram could not be handled
			if (debugEnabled)
				Gdx.app.log(LOG_TAG, "Message not handled");
		}

		pool.free(telegram);
	}
}