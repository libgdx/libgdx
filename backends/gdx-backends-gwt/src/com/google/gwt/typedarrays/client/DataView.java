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

/**
 * An ArrayBuffer is a useful object for representing an arbitrary chunk of
 * data. In many cases, such data will be read from disk or from the network,
 * and will not follow the alignment restrictions that are imposed on the typed
 * array views described earlier. In addition, the data will often be
 * heterogeneous in nature and have a defined byte order. The DataView view
 * provides a low-level interface for reading such data from and writing it to
 * an ArrayBuffer.
 *
 * Taken from the Khronos TypedArrays Draft Spec as of Aug 30, 2010.
 */
public class DataView extends ArrayBufferView {

  protected DataView() {
  }

  /**
   * @see #getInt8(int)
   */
  public final native double getDouble(int byteOffset, boolean littleEndian) /*-{
    return this.getDouble(byteOffset, littleEndian);
  }-*/;

  /**
   * @see #getInt8(int)
   */
  public final native float getFloat(int byteOffset, boolean littleEndian) /*-{
    return this.getFloat(byteOffset, littleEndian);
  }-*/;

  /**
   * @see #getInt8(int)
   */
  public final native short getInt16(int byteOffset, boolean littleEndian) /*-{
    return this.getInt16(byteOffset, littleEndian);
  }-*/;

  /**
   * @see #getInt8(int)
   */
  public final native int getInt32(int byteOffset, boolean littleEndian) /*-{
    return this.getInt32(byteOffset, littleEndian);
  }-*/;

  /**
   * Gets the value of the given type at the specified byte offset from the
   * start of the view. There is no alignment constraint; multi-byte values may
   * be fetched from any offset.
   *
   * For multi-byte values, the optional littleEndian argument indicates whether
   * a big-endian or little-endian value should be read. If false or undefined,
   * a big-endian value is read.
   *
   * These methods raise an INDEX_SIZE_ERR exception if they would read beyond
   * the end of the view.
   */
  public final native byte getInt8(int byteOffset) /*-{
    return this.getInt8(byteOffset);
  }-*/;

  /**
   * @see #getInt8(int)
   */
  public final native short getUInt16(int byteOffset, boolean littleEndian) /*-{
    return this.getUInt16(byteOffset, littleEndian);
  }-*/;

  /**
   * @see #getInt8(int)
   */
  public final native int getUInt32(int byteOffset, boolean littleEndian) /*-{
    return this.getUInt32(byteOffset, littleEndian);
  }-*/;

  /**
   * @see #getInt8(int)
   */
  public final native byte getUInt8(int byteOffset) /*-{
    return this.getUInt8(byteOffset);
  }-*/;

  /**
   * @see #setInt8(int, byte, boolean)
   */
  public final native void setDouble(int byteOffset, double value,
      boolean littleEndian) /*-{
    this.setDouble(byteOffset, value, littleEndian);
  }-*/;

  /**
   * @see #setInt8(int, byte, boolean)
   */
  public final native void setFloat(int byteOffset, float value,
      boolean littleEndian) /*-{
    this.setFloat(byteOffset, value, littleEndian);
  }-*/;

  /**
   * @see #setInt8(int, byte, boolean)
   */
  public final native void setInt16(int byteOffset, short value,
      boolean littleEndian) /*-{
    this.setInt16(byteOffset, value, littleEndian);
  }-*/;

  /**
   * @see #setInt8(int, byte, boolean)
   */
  public final native void setInt32(int byteOffset, int value,
      boolean littleEndian) /*-{
    this.setInt32(byteOffset, value, littleEndian);
  }-*/;

  /**
   * Stores a value of the given type at the specified byte offset from the
   * start of the view. There is no alignment constraint; multi-byte values may
   * be stored at any offset.
   *
   * For multi-byte values, the littleEndian argument indicates whether the
   * value should be stored in big-endian or little-endian byte order. If false
   * or undefined, the value is stored in big-endian byte order.
   *
   * These methods throw exceptions if they would write beyond the end of the
   * view.
   */
  public final native void setInt8(int byteOffset, byte value,
      boolean littleEndian) /*-{
    this.setInt8(byteOffset, value, littleEndian);
  }-*/;

  /**
   * @see #setInt8(int, byte, boolean)
   */
  public final native void setUint16(int byteOffset, short value,
      boolean littleEndian) /*-{
    this.setUint16(byteOffset, value, littleEndian);
  }-*/;

  /**
   * @see #setInt8(int, byte, boolean)
   */
  public final native void setUint32(int byteOffset, int value,
      boolean littleEndian) /*-{
    this.setUint32(byteOffset, value, littleEndian);
  }-*/;

  /**
   * @see #setInt8(int, byte, boolean)
   */
  public final native void setUint8(int byteOffset, byte value,
      boolean littleEndian) /*-{
    this.setUint8(byteOffset, value, littleEndian);
  }-*/;
}
