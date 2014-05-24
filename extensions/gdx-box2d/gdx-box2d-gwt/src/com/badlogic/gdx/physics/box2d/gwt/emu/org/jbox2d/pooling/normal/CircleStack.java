/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.jbox2d.pooling.normal;

import org.jbox2d.pooling.IOrderedStack;

public abstract class CircleStack<E> implements IOrderedStack<E>{

  private final Object[] pool;
  private int index;
  private final int size;
  private final Object[] container;

  public CircleStack(int argStackSize, int argContainerSize) {
    size = argStackSize;
    pool = new Object[argStackSize];
    for (int i = 0; i < argStackSize; i++) {
      pool[i] = newInstance();
    }
    index = 0;
    container = new Object[argContainerSize];
  }

  @SuppressWarnings("unchecked")
  public final E pop() {
    index++;
    if(index >= size){
      index = 0;
    }
    return (E) pool[index];
  }

  @SuppressWarnings("unchecked")
  public final E[] pop(int argNum) {
    assert (argNum <= container.length) : "Container array is too small";
    if(index + argNum < size){
      System.arraycopy(pool, index, container, 0, argNum);
      index += argNum;
    }else{
      int overlap = (index + argNum) - size;
      System.arraycopy(pool, index, container, 0, argNum - overlap);
      System.arraycopy(pool, 0, container, argNum - overlap, overlap);
      index = overlap;
    }
    return (E[]) container;
  }

  @Override
  public void push(int argNum) {}

  /** Creates a new instance of the object contained by this stack. */
  protected abstract E newInstance();
}
