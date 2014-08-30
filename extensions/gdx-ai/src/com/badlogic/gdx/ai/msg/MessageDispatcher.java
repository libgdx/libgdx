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

package com.badlogic.gdx.ai.msg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;

/** The MessageDispatcher is a singleton in charge of the creation, dispatch, and management of telegrams.
 * 
 * @author davebaol */
public class MessageDispatcher {

	private static final String LOG_TAG = MessageDispatcher.class.getSimpleName();

	private static final float NANOS_PER_SEC = 1000000000f;

	private static final MessageDispatcher instance = new MessageDispatcher();

	private static final long START = TimeUtils.nanoTime();

	private PriorityQueue<Telegram> queue = new PriorityQueue<Telegram>();

	private final Pool<Telegram> pool;

	private IntMap<Array<Telegraph>> msgListeners = new IntMap<Array<Telegraph>>();

	private long timeGranularity;

	private boolean debugEnabled;

	/** Don't let anyone else instantiate this class */
	private MessageDispatcher () {
		this.pool = new Pool<Telegram>(64) {
			protected Telegram newObject () {
				return new Telegram();
			}
		};
		setTimeGranularity(0.25f);
	}

	/** Returns the singleton instance of the message dispatcher. */
	public static MessageDispatcher getInstance () {
		return instance;
	}

	/** Returns the current time in nanoseconds.
	 * <p>
	 * This implementation returns the value of the system timer minus a constant value determined when this class was loaded the
	 * first time in order to ensure it takes increasing values (for 2 ^ 63 nanoseconds, i.e. 292 years) since the time stamp is
	 * used to order the telegrams in the queue. */
	public static long getCurrentTime () {
		return TimeUtils.nanoTime() - START;
	}

	/** Returns the time granularity. */
	public float getTimeGranularity () {
		return timeGranularity / NANOS_PER_SEC;
	}

	/** Sets the time granularity. Delayed telegrams having the same sender, recipient and message type are considered identical
	 * when they belong to the same time slot. If time granularity is greater than 0 identical telegrams are not doubled into the
	 * queue. This prevents many similar telegrams from bunching up in the queue and being delivered en masse, thus flooding an
	 * agent with identical messages. To eliminate time granularity just set it to 0. */
	public void setTimeGranularity (float timeGranularity) {
		boolean uniqueness = timeGranularity > 0;
		this.timeGranularity = uniqueness ? (long)(timeGranularity * NANOS_PER_SEC) : 0;
		this.queue.setUniqueness(uniqueness);
	}

	/** Returns true if debug mode is on; false otherwise. */
	public boolean isDebugEnabled () {
		return debugEnabled;
	}

	/** Sets debug mode on/off. */
	public void setDebugEnabled (boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

	/** Registers a listener for the specified message code. Messages without an explicit receiver are broadcasted to all its
	 * registered listeners.
	 * @param msg the message code
	 * @param listener the listener to add */
	@Deprecated
	public void addListener (int msg, Telegraph listener) {
		addListener(listener, msg);
	}

	/** Registers a listener for the specified message code. Messages without an explicit receiver are broadcasted to all its
	 * registered listeners.
	 * @param msg the message code
	 * @param listener the listener to add */
	public void addListener (Telegraph listener, int msg) {
		Array<Telegraph> listeners = msgListeners.get(msg);
		if (listeners == null) {
			// Associate an empty unordered array with the message code
			listeners = new Array<Telegraph>(false, 16);
			msgListeners.put(msg, listeners);
		}
		listeners.add(listener);
	}

	/** Registers a listener for a selection of message types. Messages without an explicit receiver are broadcasted to all its
	 * registered listeners.
	 * 
	 * @param listener the listener to add
	 * @param msgs the message codes */
	public void addListeners (Telegraph listener, int... msgs) {
		for (int msg : msgs)
			addListener(listener, msg);
	}

	/** Unregister the specified listener for the specified message code.
	 * @param msg the message code
	 * @param listener the listener to remove
	 * @deprecated Use {@link #removeListener(Telegraph, int)} instead. */
	@Deprecated
	public void removeListener (int msg, Telegraph listener) {
		removeListener(listener, msg);
	}

	/** Unregister the specified listener for the specified message code.
	 * @param msg the message code
	 * @param listener the listener to remove */
	public void removeListener (Telegraph listener, int msg) {
		Array<Telegraph> listeners = msgListeners.get(msg);
		if (listeners != null) {
			listeners.removeValue(listener, true);
		}
	}

	/** Unregister the specified listener for the selection of message codes.
	 * 
	 * @param listener the listener to remove
	 * @param msgs the message codes */
	public void removeListener (Telegraph listener, int... msgs) {
		for (int msg : msgs)
			removeListener(listener, msg);
	}

	/** Unregisters all the listeners for the specified message code.
	 * @param msg the message code */
	public void clearListeners (int msg) {
		msgListeners.remove(msg);
	}

	/** Unregisters all the listeners for the given message codes.
	 * 
	 * @param msgs the message codes */
	public void clearListeners (int... msgs) {
		for (int msg : msgs)
			clearListeners(msg);
	}

	/** Removes all the registered listeners for all the message codes. */
	public void clearListeners () {
		msgListeners.clear();
	}

	/** Removes all the telegrams from the queue and releases them to the internal pool. */
	public void clearQueue () {
		for (int i = 0; i < queue.size(); i++) {
			pool.free(queue.get(i));
		}
		queue.clear();
	}

	/** Removes all the telegrams from the queue and the registered listeners for all the messages. */
	public void clear () {
		clearQueue();
		clearListeners();
	}

	/** Sends an immediate message to all registered listeners, with no extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(0, sender,
	 * null, msg, null)}
	 * 
	 * @param sender the sender of the telegram
	 * @param msg the message code */
	public void dispatchMessage (Telegraph sender, int msg) {
		dispatchMessage(0f, sender, null, msg, null);
	}

	/** Sends an immediate message to all registered listeners, with extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(0, sender,
	 * null, msg, extraInfo)}
	 * 
	 * @param sender the sender of the telegram
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (Telegraph sender, int msg, Object extraInfo) {
		dispatchMessage(0f, sender, null, msg, extraInfo);
	}

	/** Sends an immediate message to the specified receiver with no extra info. The receiver doesn't need to be a register listener
	 * for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(0, sender,
	 * receiver, msg, null)}
	 * 
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code */
	public void dispatchMessage (Telegraph sender, Telegraph receiver, int msg) {
		dispatchMessage(0f, sender, receiver, msg, null);
	}

	/** Sends an immediate message to the specified receiver with extra info. The receiver doesn't need to be a register listener
	 * for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(0, sender,
	 * receiver, msg, extraInfo)}
	 * 
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (Telegraph sender, Telegraph receiver, int msg, Object extraInfo) {
		dispatchMessage(0f, sender, receiver, msg, extraInfo);
	}

	/** Sends a message to all registered listeners, with the specified delay but no extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(delay,
	 * sender, null, msg, null)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param msg the message code */
	public void dispatchMessage (float delay, Telegraph sender, int msg) {
		dispatchMessage(delay, sender, null, msg, null);
	}

	/** Sends a message to all registered listeners, with the specified delay and extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(delay,
	 * sender, null, msg, extraInfo)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (float delay, Telegraph sender, int msg, Object extraInfo) {
		dispatchMessage(delay, sender, null, msg, extraInfo);
	}

	/** Sends a message to the specified receiver, with the specified delay but no extra info. The receiver doesn't need to be a
	 * register listener for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(delay,
	 * sender, receiver, msg, null)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code */
	public void dispatchMessage (float delay, Telegraph sender, Telegraph receiver, int msg) {
		dispatchMessage(delay, sender, receiver, msg, null);
	}

	/** Given a message, a receiver, a sender and any time delay, this method routes the message to the correct agents (if no delay)
	 * or stores in the message queue to be dispatched at the correct time.
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (float delay, Telegraph sender, Telegraph receiver, int msg, Object extraInfo) {

		// Get a telegram from the pool
		Telegram telegram = pool.obtain();
		telegram.sender = sender;
		telegram.receiver = receiver;
		telegram.message = msg;
		telegram.extraInfo = extraInfo;

		// If there is no delay, route telegram immediately
		if (delay <= 0.0f) {
			if (debugEnabled)
				Gdx.app.log(LOG_TAG, "Instant telegram dispatched at time: " + getCurrentTime() + " by " + sender + " for "
					+ receiver + ". Msg is " + msg);

			// Send the telegram to the recipient
			discharge(telegram);
		} else {
			// Set the timestamp for the delayed telegram
			long currentTime = getCurrentTime();
			telegram.setTimestamp(currentTime + (long)(delay * NANOS_PER_SEC), timeGranularity);

			// Put the telegram in the queue
			boolean added = queue.add(telegram);

			// Return it to the pool if has been rejected
			if (!added) pool.free(telegram);

			if (debugEnabled) {
				if (added)
					Gdx.app.log(LOG_TAG, "Delayed telegram from " + sender + " for " + receiver + " recorded at time "
						+ getCurrentTime() + ". Msg is " + msg);
				else
					Gdx.app.log(LOG_TAG, "Delayed telegram from " + sender + " for " + receiver + " rejected by the queue. Msg is "
						+ msg);
			}
		}
	}

	/** Dispatches any telegrams with a timestamp that has expired. Any dispatched telegrams are removed from the queue.
	 * <p>
	 * This method must be called each time through the main game loop. */
	public void dispatchDelayedMessages () {
		if (queue.size() == 0) return;

		// Get current time
		long currentTime = getCurrentTime();

		// Now peek at the queue to see if any telegrams need dispatching.
		// Remove all telegrams from the front of the queue that have gone
		// past their time stamp.
		do {
			// Read the telegram from the front of the queue
			final Telegram telegram = queue.peek();
			if (telegram.getTimestamp() > currentTime) break;

			if (debugEnabled) {
				Gdx.app.log(LOG_TAG, "Queued telegram ready for dispatch: Sent to " + telegram.receiver + ". Msg is "
					+ telegram.message);
			}

			// Send the telegram to the recipient
			discharge(telegram);

			// Remove it from the queue
			queue.poll();
		} while (queue.size() > 0);

	}

	/** This method is used by {@link #dispatchMessage} or {@link #dispatchDelayedMessages}. It first calls the message handling
	 * method of the receiving agents with the specified telegram then returns the telegram to the pool.
	 * @param telegram the telegram to discharge */
	private void discharge (Telegram telegram) {
		if (telegram.receiver != null) {
			// Dispatch the telegram to the receiver specified by the telegram itself
			if (!telegram.receiver.handleMessage(telegram)) {
				// Telegram could not be handled
				if (debugEnabled) Gdx.app.log(LOG_TAG, "Message " + telegram.message + " not handled");
			}
		} else {
			// Dispatch the telegram to all the registered receivers
			int handledCount = 0;
			Array<Telegraph> listeners = msgListeners.get(telegram.message);
			if (listeners != null) {
				for (int i = 0; i < listeners.size; i++) {
					if (listeners.get(i).handleMessage(telegram)) {
						handledCount++;
					}
				}
			}
			// Telegram could not be handled
			if (debugEnabled && handledCount == 0) Gdx.app.log(LOG_TAG, "Message " + telegram.message + " not handled");
		}

		// Release the telegram to the pool
		pool.free(telegram);
	}

}
