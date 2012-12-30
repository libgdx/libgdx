package java.io;

public class FilterInputStream extends InputStream {
    protected InputStream in;

    protected FilterInputStream(InputStream in) {
        this.in = in;
    }

    public int read() throws IOException {
        return in.read();
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return 0;
    }

    public int available() {
        return 0;
    }

    public void close() throws IOException {
        in.close();
    }

    public synchronized void mark(int readlimit) {
    }

    public synchronized void reset() throws IOException {
    }

    public boolean markSupported() {
   	 return false;
    }
}
