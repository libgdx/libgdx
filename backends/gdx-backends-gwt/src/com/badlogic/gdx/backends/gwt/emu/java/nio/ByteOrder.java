/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio;

//import org.apache.harmony.luni.platform.Platform;

/** Defines byte order constants.
 * 
 * @since Android 1.0 */
public final class ByteOrder {

	/** This constant represents big endian.
	 * 
	 * @since Android 1.0 */
	public static final ByteOrder BIG_ENDIAN = new ByteOrder("BIG_ENDIAN"); //$NON-NLS-1$

	/** This constant represents little endian.
	 * 
	 * @since Android 1.0 */
	public static final ByteOrder LITTLE_ENDIAN = new ByteOrder("LITTLE_ENDIAN"); //$NON-NLS-1$

	private static final ByteOrder NATIVE_ORDER;

	static {
// if (Platform.getMemorySystem().isLittleEndian()) {
		NATIVE_ORDER = LITTLE_ENDIAN;
// } else {
// NATIVE_ORDER = BIG_ENDIAN;
// }
	}

	/** Returns the current platform byte order.
	 * 
	 * @return the byte order object, which is either LITTLE_ENDIAN or BIG_ENDIAN.
	 * @since Android 1.0 */
	public static ByteOrder nativeOrder () {
		return NATIVE_ORDER;
	}

	private final String name;

	private ByteOrder (String name) {
		super();
		this.name = name;
	}

	/** Returns a string that describes this object.
	 * 
	 * @return "BIG_ENDIAN" for {@link #BIG_ENDIAN ByteOrder.BIG_ENDIAN} objects, "LITTLE_ENDIAN" for {@link #LITTLE_ENDIAN
	 *         ByteOrder.LITTLE_ENDIAN} objects.
	 * @since Android 1.0 */
	public String toString () {
		return name;
	}
}
