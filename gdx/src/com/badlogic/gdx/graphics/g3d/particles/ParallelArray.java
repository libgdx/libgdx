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

package com.badlogic.gdx.graphics.g3d.particles;

import java.util.Arrays;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ArrayReflection;

/** This class represents an group of elements like an array, but the properties of the elements are stored as separate arrays.
 * These arrays are called {@link Channel} and are represented by {@link ChannelDescriptor}. It's not necessary to store primitive
 * types in the channels but doing so will "exploit" data locality in the JVM, which is ensured for primitive types. Use
 * {@link FloatChannel}, {@link IntChannel}, {@link ObjectChannel} to store the data.
 * @author inferno */
public class ParallelArray {

	/** This class describes the content of a {@link Channel} */
	public static class ChannelDescriptor {
		public int id;
		public Class<?> type;
		public int count;

		public ChannelDescriptor (int id, Class<?> type, int count) {
			this.id = id;
			this.type = type;
			this.count = count;
		}
	}

	/** This class represents a container of values for all the elements for a given property */
	public abstract class Channel {
		public int id;
		public Object data;
		public int strideSize;

		public Channel (int id, Object data, int strideSize) {
			this.id = id;
			this.strideSize = strideSize;
			this.data = data;
		}

		public abstract void add (int index, Object... objects);

		public abstract void swap (int i, int k);

		protected abstract void setCapacity (int requiredCapacity);
	}

	/** This interface is used to provide custom initialization of the {@link Channel} data */
	public static interface ChannelInitializer<T extends Channel> {
		public void init (T channel);
	}

	public class FloatChannel extends Channel {
		public float[] data;

		public FloatChannel (int id, int strideSize, int size) {
			super(id, new float[size * strideSize], strideSize);
			this.data = (float[])super.data;
		}

		@Override
		public void add (int index, Object... objects) {
			for (int i = strideSize * size, c = i + strideSize, k = 0; i < c; ++i, ++k) {
				data[i] = (Float)objects[k];
			}
		}

		@Override
		public void swap (int i, int k) {
			float t;
			i = strideSize * i;
			k = strideSize * k;
			for (int c = i + strideSize; i < c; ++i, ++k) {
				t = data[i];
				data[i] = data[k];
				data[k] = t;
			}
		}

		@Override
		public void setCapacity (int requiredCapacity) {
			float[] newData = new float[strideSize * requiredCapacity];
			System.arraycopy(data, 0, newData, 0, Math.min(data.length, newData.length));
			super.data = data = newData;
		}
	}

	public class IntChannel extends Channel {
		public int[] data;

		public IntChannel (int id, int strideSize, int size) {
			super(id, new int[size * strideSize], strideSize);
			this.data = (int[])super.data;
		}

		@Override
		public void add (int index, Object... objects) {
			for (int i = strideSize * size, c = i + strideSize, k = 0; i < c; ++i, ++k) {
				data[i] = (Integer)objects[k];
			}
		}

		@Override
		public void swap (int i, int k) {
			int t;
			i = strideSize * i;
			k = strideSize * k;
			for (int c = i + strideSize; i < c; ++i, ++k) {
				t = data[i];
				data[i] = data[k];
				data[k] = t;
			}
		}

		@Override
		public void setCapacity (int requiredCapacity) {
			int[] newData = new int[strideSize * requiredCapacity];
			System.arraycopy(data, 0, newData, 0, Math.min(data.length, newData.length));
			super.data = data = newData;
		}
	}

	@SuppressWarnings("unchecked")
	public class ObjectChannel<T> extends Channel {
		Class<T> componentType;
		public T[] data;

		public ObjectChannel (int id, int strideSize, int size, Class<T> type) {
			super(id, ArrayReflection.newInstance(type, size * strideSize), strideSize);
			componentType = type;
			this.data = (T[])super.data;
		}

		@Override
		public void add (int index, Object... objects) {
			for (int i = strideSize * size, c = i + strideSize, k = 0; i < c; ++i, ++k) {
				this.data[i] = (T)objects[k];
			}
		}

		@Override
		public void swap (int i, int k) {
			T t;
			i = strideSize * i;
			k = strideSize * k;
			for (int c = i + strideSize; i < c; ++i, ++k) {
				t = data[i];
				data[i] = data[k];
				data[k] = t;
			}
		}

		@Override
		public void setCapacity (int requiredCapacity) {
			T[] newData = (T[])ArrayReflection.newInstance(componentType, strideSize * requiredCapacity);
			System.arraycopy(data, 0, newData, 0, Math.min(data.length, newData.length));
			super.data = data = newData;
		}
	}

	/** the channels added to the array */
	Array<Channel> arrays;
	/** the maximum amount of elements that this array can hold */
	public int capacity;
	/** the current amount of defined elements, do not change manually unless you know what you are doing. */
	public int size;

	public ParallelArray (int capacity) {
		arrays = new Array<Channel>(false, 2, Channel.class);
		this.capacity = capacity;
		size = 0;
	}

	/** Adds and returns a channel described by the channel descriptor parameter. If a channel with the same id already exists, no
	 * allocation is performed and that channel is returned. */
	public <T extends Channel> T addChannel (ChannelDescriptor channelDescriptor) {
		return addChannel(channelDescriptor, null);
	}

	/** Adds and returns a channel described by the channel descriptor parameter. If a channel with the same id already exists, no
	 * allocation is performed and that channel is returned. Otherwise a new channel is allocated and initialized with the
	 * initializer. */
	public <T extends Channel> T addChannel (ChannelDescriptor channelDescriptor, ChannelInitializer<T> initializer) {
		T channel = getChannel(channelDescriptor);
		if (channel == null) {
			channel = allocateChannel(channelDescriptor);
			if (initializer != null) initializer.init(channel);
			arrays.add(channel);
		}
		return channel;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <T extends Channel> T allocateChannel (ChannelDescriptor channelDescriptor) {
		if (channelDescriptor.type == float.class) {
			return (T)new FloatChannel(channelDescriptor.id, channelDescriptor.count, capacity);
		} else if (channelDescriptor.type == int.class) {
			return (T)new IntChannel(channelDescriptor.id, channelDescriptor.count, capacity);
		} else {
			return (T)new ObjectChannel(channelDescriptor.id, channelDescriptor.count, capacity, channelDescriptor.type);
		}
	}

	/** Removes the channel with the given id */
	public <T> void removeArray (int id) {
		arrays.removeIndex(findIndex(id));
	}

	private int findIndex (int id) {
		for (int i = 0; i < arrays.size; ++i) {
			Channel array = arrays.items[i];
			if (array.id == id) return i;
		}
		return -1;
	}

	/** Adds an element considering the values in the same order as the current channels in the array. The n_th value must have the
	 * same type and stride of the given channel at position n */
	public void addElement (Object... values) {
		/* FIXME make it grow... */
		if (size == capacity) throw new GdxRuntimeException("Capacity reached, cannot add other elements");

		int k = 0;
		for (Channel strideArray : arrays) {
			strideArray.add(k, values);
			k += strideArray.strideSize;
		}
		++size;
	}

	/** Removes the element at the given index and swaps it with the last available element */
	public void removeElement (int index) {
		int last = size - 1;
		// Swap
		for (Channel strideArray : arrays) {
			strideArray.swap(index, last);
		}
		size = last;
	}

	/** @return the channel with the same id as the one in the descriptor */
	@SuppressWarnings("unchecked")
	public <T extends Channel> T getChannel (ChannelDescriptor descriptor) {
		for (Channel array : arrays) {
			if (array.id == descriptor.id) return (T)array;
		}
		return null;
	}

	/** Removes all the channels and sets size to 0 */
	public void clear () {
		arrays.clear();
		size = 0;
	}

	/** Sets the capacity. Each contained channel will be resized to match the required capacity and the current data will be
	 * preserved. */
	public void setCapacity (int requiredCapacity) {
		if (capacity != requiredCapacity) {
			for (Channel channel : arrays) {
				channel.setCapacity(requiredCapacity);
			}
			capacity = requiredCapacity;
		}
	}

}
