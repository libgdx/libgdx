
package java.nio;

import gwt.g3d.client.gl2.array.ArrayBufferView;

public interface HasArrayBufferView {

	public ArrayBufferView getTypedArray ();

	public int getElementSize ();
}
