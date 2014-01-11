
package java.util.zip;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

/** Dummy emulation. Throws a GdxRuntimeException on first read.
 * @author hneuer */
public class InflaterInputStream extends InputStream {
	private InputStream in;

	public InflaterInputStream (InputStream in) {
		this.in = in;
	}

	@Override
	public int read () throws IOException {
		throw new GdxRuntimeException("InflaterInputStream not supported in GWT");
	}

	@Override
	public void close () throws IOException {
		super.close();
		StreamUtils.closeQuietly(in);
	}
}
