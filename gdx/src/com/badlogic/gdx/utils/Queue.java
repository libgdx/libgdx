/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.reflect.ArrayReflection;

/** Automatically resizing FIFO queue.
 * Values in backing queue rotate around so push and pop are O(1) (unless resizing in push).
 * This collection is not thread-safe. */
public class Queue <T> {

    /** Contains values waiting in this queue.
     * Pop and push indices go in circle around this array, wrapping at the end.
     * For simplicity, one index (pointed at by pushIndex) is always kept empty.
     * Therefore, values.length = 8 can only hold 7 queued values. */
    protected T[] values;

    /** Index to pop from. Logically smaller than pushIndex.
     * Unless popIndex == pushIndex, it points to a valid element inside queue. */
    protected int popIndex = 0;
    /** Index to push to. Logically bigger than popIndex.
     * Always points to an empty values position. */
    protected int pushIndex = 0;

    /** Create a new Queue which can hold 15 values without resizing. */
    public Queue(){
        this(16);
    }

    /** Create a new Queue which can hold `initialSize` values without resizing. */
    public Queue(int initialSize) {
        //noinspection unchecked
        values = (T[]) new Object[initialSize + 1];
    }

    /** Create a new Queue which can hold `initialSize` values without resizing.
     * This creates backing array of correct type via reflection.
     * Use this only if you are accessing backing array directly.
     * <p/>
     * NOTE: Worth using only if you know what are you doing. */
    public Queue(int initialSize, Class<T> type) {
        //noinspection unchecked
        values = (T[]) ArrayReflection.newInstance(type, initialSize + 1);
    }

    /** Enqueue given object.
     * Unless backing array needs resizing, operates in O(1) time.
     * @param object can be null
     */
    public void add(T object){
        final T[] values = this.values;
        int pushIndex = this.pushIndex;
        final int popIndex = this.popIndex;

        values[pushIndex] = object;
        pushIndex++;
        if(pushIndex == values.length){
            pushIndex = 0;
        }

        if(pushIndex == popIndex){
            //Must resize
            final int newSize = values.length << 1;
            //noinspection unchecked
            final T[] newArray = (T[]) ArrayReflection.newInstance(values.getClass().getComponentType(), newSize);
            final int currentSize = values.length;
            for (int i = 0; i < currentSize; i++) {
                newArray[i] = values[(popIndex + i) % currentSize];
            }
            this.popIndex = 0;
            pushIndex = currentSize;

            this.values = newArray;
        }
        this.pushIndex = pushIndex;
    }

    /** Dequeue next object in queue
     * @return next object in queue or null if empty */
    public T remove(){
        return remove(null);
    }

    /** Dequeue next object in queue
     * @return next object in queue or `defaultValue` if empty */
    public T remove(T defaultValue){
        final T[] values = this.values;
        int popIndex = this.popIndex;

        if(popIndex == pushIndex){
            //Underflow
            return defaultValue;
        }
        T result = values[popIndex];
        values[popIndex] = null;

        popIndex++;
        if(popIndex == values.length){
            popIndex = 0;
        }
        this.popIndex = popIndex;

        return result;
    }

    /** Same as {@link Queue#remove()} but the value is kept in the queue. */
    public T peek(){
        return peek(null);
    }

    /** Same as {@link Queue#remove(T)} but the value is kept in the queue. */
    public T peek(T defaultValue){
        final int popIndex = this.popIndex;

        if(popIndex == pushIndex){
            //Underflow
            return defaultValue;
        }
        return values[popIndex];
    }

    /** @return true if this queue holds no values to remove */
    public boolean isEmpty(){
        return popIndex == pushIndex;
    }

    /** @return amount of values waiting in this queue */
    public int size(){
        final int pushIndex = this.pushIndex;
        final int popIndex = this.popIndex;

        if(pushIndex == popIndex)return 0;
        if(popIndex < pushIndex)return (pushIndex - popIndex);
        return values.length - (popIndex - pushIndex);
    }

    /** Removes all values from this queue.
     * (Values in backing array are set to null to prevent memory leak, so this operates in O(n).)*/
    public void clear(){
        final T[] values = this.values;
        while(popIndex != pushIndex){
            values[popIndex] = null;
            popIndex++;
            if(popIndex == values.length){
                popIndex = 0;
            }
        }
    }

    public String toString(){
        if(isEmpty()){
            return "Queue []";
        }
        final T[] values = this.values;
        final int popIndex = this.popIndex;
        final int pushIndex = this.pushIndex;

        StringBuilder sb = new StringBuilder(64);
        sb.append("Queue [");
        sb.append(values[popIndex]);
        for (int i = popIndex+1; i != pushIndex; i = (i+1) % values.length) {
            sb.append(", ").append(values[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
