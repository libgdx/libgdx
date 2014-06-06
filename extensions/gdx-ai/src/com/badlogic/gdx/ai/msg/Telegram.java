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

import com.badlogic.gdx.ai.Agent;
import com.badlogic.gdx.utils.Pool.Poolable;

/** A Telegram is the container of a message. The {@link MessageDispatcher} manages telegram life-cycle.
 * @author davebaol */
public class Telegram implements Comparable<Telegram>, Poolable {

	/** The agent that sent this telegram */
	public Agent sender;

	/** The agent that is to receive this telegram */
	public Agent receiver;

	/** The message type. */
	public int message;

	/** Messages can be dispatched immediately or delayed for a specified amount of time. If a delay is necessary, this field is
	 * stamped with the time the message should be dispatched. */
	private long timestamp;
	private long discreteTimestamp;

	/** Any additional information that may accompany the message */
	public Object extraInfo;

	/** Package private constructor. */
	Telegram () {
	}

	/** Returns the time stamp of this telegram. */
	public long getTimestamp () {
		return timestamp;
	}

	/** Sets the time stamp of this telegram. It also sets the discrete time stamp based on the specified time granularity. */
	public void setTimestamp (long timestamp, long timeGranularity) {
		this.timestamp = timestamp;
		this.discreteTimestamp = timeGranularity <= 0 ? timestamp : timestamp / timeGranularity;
	}

	@Override
	public void reset () {
		this.sender = null;
		this.receiver = null;
		this.message = 0;
		this.extraInfo = null;
		this.timestamp = 0;
		this.discreteTimestamp = 0;
	}

	@Override
	public int compareTo (Telegram other) {
		if (this.equals(other)) return 0;
		return (this.timestamp - other.timestamp < 0) ? -1 : 1;
	}

	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + message;
		result = prime * result + ((receiver == null) ? 0 : receiver.hashCode());
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result + (int)(discreteTimestamp ^ (discreteTimestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Telegram other = (Telegram)obj;
		if (message != other.message) return false;
		if (sender == null) {
			if (other.sender != null) return false;
		} else if (!sender.equals(other.sender)) return false;
		if (receiver == null) {
			if (other.receiver != null) return false;
		} else if (!receiver.equals(other.receiver)) return false;
		if (discreteTimestamp != other.discreteTimestamp) return false;
		return true;
	}

}
