/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.typedarrays.client;

import com.google.gwt.core.client.JsArrayNumber;

/**
 * The typed array view types represent a view of an ArrayBuffer that allows for
 * indexing and manipulation. The length of each of these is fixed.
 *
 * Taken from the Khronos TypedArrays Draft Spec as of Aug 30, 2010.
 */
public class Float32Array extends ArrayBufferView {

  public static final int BYTES_PER_ELEMENT = 4;

  /**
   * @see #create(ArrayBuffer, int, int)
   */
  public static final native Float32Array create(ArrayBuffer buffer) /*-{
    return new Float32Array(buffer);
  }-*/;

  /**
   * @see #create(ArrayBuffer, int, int)
   */
  public static final native Float32Array create(ArrayBuffer buffer,
      int byteOffset) /*-{
    return new Float32Array(buffer, byteOffset);
  }-*/;

  /**
   * Create a new TypedArray object using the passed ArrayBuffer for its
   * storage. Optional byteOffset and length can be used to limit the section of
   * the buffer referenced. The byteOffset indicates the offset in bytes from
   * the start of the ArrayBuffer, and the length is the count of elements from
   * the offset that this TypedArray will reference. If both byteOffset and
   * length are omitted, the TypedArray spans the entire ArrayBuffer range. If
   * the length is omitted, the TypedArray extends from the given byteOffset
   * until the end of the ArrayBuffer.
   *
   * The given byteOffset must be a multiple of the element size of the specific
   * type, otherwise an INDEX_SIZE_ERR exception is raised.
   *
   * If a given byteOffset and length references an area beyond the end of the
   * ArrayBuffer an INDEX_SIZE_ERR exception is raised.
   *
   * If length is not explicitly specified, the length of the ArrayBuffer minus
   * the byteOffset must be a multiple of the element size of the specific type,
   * or an INDEX_SIZE_ERR exception is raised.
   */
  public static final native Float32Array create(ArrayBuffer buffer,
      int byteOffset, int length) /*-{
    return new Float32Array(buffer, byteOffset, length);
  }-*/;

  /**
   * Create a new ArrayBuffer with enough bytes to hold array.length elements of
   * this typed array, then create a typed array view referring to the full
   * buffer. The contents of the new view are initialized to the contents of the
   * given typed array or sequence, with each element converted to the
   * appropriate typed array type.
   */
  public static final Float32Array create(float[] data) {
    return create(ArrayUtils.toJsArray(data));
  }

  /**
   * Create a new ArrayBuffer with enough bytes to hold array.length elements of
   * this typed array, then create a typed array view referring to the full
   * buffer. The contents of the new view are initialized to the contents of the
   * given typed array or sequence, with each element converted to the
   * appropriate typed array type.
   */
  public static final native Float32Array create(Float32Array array) /*-{
    return new Float32Array(array);
  }-*/;

  /**
   * Create a new ArrayBuffer with enough bytes to hold length elements of this
   * typed array, then creates a typed array view referring to the full buffer.
   */
  public static final native Float32Array create(int size) /*-{
    return new Float32Array(size);
  }-*/;

  /**
   * Create a new ArrayBuffer with enough bytes to hold array.length elements of
   * this typed array, then create a typed array view referring to the full
   * buffer. The contents of the new view are initialized to the contents of the
   * given typed array or sequence, with each element converted to the
   * appropriate typed array type.
   */
  public static final native Float32Array create(JsArrayNumber data) /*-{
    return new Float32Array(data);
  }-*/;

  protected Float32Array() {
  }

  /**
   * Returns the element at the given numeric index.
   */
  public native final float get(int index) /*-{
    return this[index];
  }-*/;

  /**
   * The length of the TypedArray in elements, as fixed at construction time.
   */
  public final native int getLength() /*-{
    return this.length;
  }-*/;

  /**
   * @see #set(float[], int)
   */
  public final void set(float[] array) {
    set(array, 0);
  }

  /**
   * Set multiple values, reading input values from the array. The optional
   * offset value indicates the index in the current array where values are
   * written. If omitted, it is assumed to be 0.
   *
   * If the offset plus the length of the given array is out of range for the
   * current TypedArray, an INDEX_SIZE_ERR exception is raised.
   */
  public final void set(float[] array, int offset) {
    set(ArrayUtils.toJsArray(array), offset);
  }

  /**
   * @see #set(Float32Array, int)
   * @param array
   */
  public native final void set(Float32Array array) /*-{
    this.set(array);
  }-*/;

  /**
   * Set multiple values, reading input values from the array. The optional
   * offset value indicates the index in the current array where values are
   * written. If omitted, it is assumed to be 0.
   *
   * The two arrays may use the same underlying ArrayBuffer. In this situation,
   * setting the values takes place as if all the data is first copied into a
   * temporary buffer that does not overlap either of the arrays, and then the
   * data from the temporary buffer is copied into the current array.
   *
   * If the offset plus the length of the given array is out of range for the
   * current TypedArray, an INDEX_SIZE_ERR exception is raised.
   */
  public native final void set(Float32Array array, int offset) /*-{
    this.set(array, offset);
  }-*/;

  /**
   * Sets the element at the given numeric index to the given value.
   */
  public native final void set(int index, float value) /*-{
    this[index] = value;
  }-*/;

  /**
   * @see #set(float[], int)
   */
  public native final void set(JsArrayNumber array) /*-{
    this.set(array);
  }-*/;

  /**
   * @see #set(float[], int)
   */
  public native final void set(JsArrayNumber array, int offset) /*-{
    this.set(array, offset);
  }-*/;

  /**
   * Returns a new TypedArray view of the ArrayBuffer store for this TypedArray,
   * referencing the elements at begin, inclusive, up to end, exclusive. If
   * either begin or end is negative, it refers to an index from the end of the
   * array, as opposed to from the beginning.
   *
   * If end is unspecified, the subarray contains all elements from begin to the
   * end of the TypedArray.
   *
   * The range specified by the begin and end values is clamped to the valid
   * index range for the current array. If the computed length of the new
   * TypedArray would be negative, it is clamped to zero.
   *
   * The returned TypedArray will be of the same type as the array on which this
   * method is invoked.
   */
  public final native Float32Array subarray(int offset, int length) /*-{
    return this.subarray(offset, length);
  }-*/;
}
