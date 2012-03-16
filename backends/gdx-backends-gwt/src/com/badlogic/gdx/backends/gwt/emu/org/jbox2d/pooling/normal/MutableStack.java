/*******************************************************************************
 * Copyright (c) 2011, Daniel Murphy
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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import org.jbox2d.pooling.IDynamicStack;

public class MutableStack<E, T extends E> implements IDynamicStack<E> {

	private Object[] stack;
	private int index;
	private int size;
	private final Generator<T> gen;

	public MutableStack (Generator<T> gen, int argInitSize) {
		index = 0;
		this.gen = gen;
		stack = null;
		index = 0;
		extendStack(argInitSize);
	}

	@SuppressWarnings("unchecked")
	private void extendStack (int argSize) {
		Object[] newStack = new Object[argSize]; // (T[]) Array.newInstance(sClass, argSize);
		if (stack != null) {
			System.arraycopy(stack, 0, newStack, 0, size);
		}
		for (int i = 0; i < newStack.length; i++) {
			newStack[i] = gen.gen();
		}
		stack = newStack;
		size = newStack.length;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.jbox2d.pooling.IDynamicStack#pop() */
	public final E pop () {
		if (index >= size) {
			extendStack(size * 2);
		}
		return (E)stack[index++];
	}

	/* (non-Javadoc)
	 * 
	 * @see org.jbox2d.pooling.IDynamicStack#push(E) */
	@SuppressWarnings("unchecked")
	public final void push (E argObject) {
		assert (index > 0);
		stack[--index] = (T)argObject;
	}
}
