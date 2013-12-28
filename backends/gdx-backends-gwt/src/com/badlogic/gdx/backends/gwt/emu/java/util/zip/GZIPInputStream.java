
package java.util.zip;

import java.io.InputStream;

/** Dummy emulation. Throws a GdxRuntimeException on first read.
 * @author hneuer */
public class GZIPInputStream extends InflaterInputStream {
	public GZIPInputStream (InputStream in, int size) {
		super(in);
	}
}
