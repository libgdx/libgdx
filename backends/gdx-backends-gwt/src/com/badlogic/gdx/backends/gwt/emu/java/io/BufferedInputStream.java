package java.io;

public class BufferedInputStream extends InputStream {
	InputStream in;
	
	public BufferedInputStream(InputStream in) {
		this.in = in;
	}
	
	public BufferedInputStream(InputStream in, int size) {
		this.in = in;
	}
	
	@Override
	public int read () throws IOException {
		return in.read();
	}
}
